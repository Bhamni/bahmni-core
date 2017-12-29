package org.bahmni.module.admin.csv.persister;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.bahmni.csv.EntityPersister;
import org.bahmni.csv.Messages;
import org.bahmni.module.admin.csv.models.LabResultRow;
import org.bahmni.module.admin.csv.models.LabResultsRow;
import org.bahmni.module.admin.csv.service.PatientMatchService;
import org.openmrs.CareSetting;
import org.openmrs.Concept;
import org.openmrs.ConceptNumeric;
import org.openmrs.Encounter;
import org.openmrs.EncounterRole;
import org.openmrs.Obs;
import org.openmrs.Order;
import org.openmrs.Patient;
import org.openmrs.Provider;
import org.openmrs.Visit;
import org.openmrs.api.APIException;
import org.openmrs.api.ConceptService;
import org.openmrs.api.EncounterService;
import org.openmrs.api.OrderService;
import org.openmrs.api.ProviderService;
import org.openmrs.api.context.Context;
import org.openmrs.api.context.UserContext;
import org.openmrs.module.auditlog.service.AuditLogService;
import org.openmrs.module.bahmniemrapi.encountertransaction.command.impl.BahmniVisitAttributeService;
import org.openmrs.module.bahmniemrapi.encountertransaction.service.VisitIdentificationHelper;
import org.openmrs.module.bahmniemrapi.laborder.contract.LabOrderResult;
import org.openmrs.module.bahmniemrapi.laborder.mapper.LabOrderResultMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.text.ParseException;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;

@Component
public class LabResultPersister implements EntityPersister<LabResultsRow> {
    private static final Log log = LogFactory.getLog(LabResultPersister.class);
    public static final String LAB_RESULT_ENCOUNTER_TYPE = "LAB_RESULT";
    public static final String LAB_ORDER_TYPE = "Lab Order";
    private String patientMatchingAlgorithmClassName;
    private boolean shouldMatchExactPatientId;

    @Autowired
    private PatientMatchService patientMatchService;
    @Autowired
    private ConceptService conceptService;
    @Autowired
    private OrderService orderService;
    @Autowired
    private ProviderService providerService;
    @Autowired
    private EncounterService encounterService;
    @Autowired
    private VisitIdentificationHelper visitIdentificationHelper;
    @Autowired
    private LabOrderResultMapper labOrderResultMapper;
    @Autowired
    private BahmniVisitAttributeService bahmniVisitAttributeSaveCommand;
    @Autowired
    private AuditLogService auditLogService;
    private UserContext userContext;
    private String loginLocationUuid;

    public void init(UserContext userContext, String patientMatchingAlgorithmClassName, boolean shouldMatchExactPatientId, String loginLocationUuid) {
        this.userContext = userContext;
        this.patientMatchingAlgorithmClassName = patientMatchingAlgorithmClassName;
        this.shouldMatchExactPatientId = shouldMatchExactPatientId;
        this.loginLocationUuid = loginLocationUuid;
    }

    @Override
    public Messages persist(LabResultsRow labResultsRow) {
        try {
            Patient patient = patientMatchService.getPatient(patientMatchingAlgorithmClassName, labResultsRow.getPatientAttributes(), labResultsRow.getPatientIdentifier(), shouldMatchExactPatientId);
            Visit visit = visitIdentificationHelper.getVisitFor(patient, labResultsRow.getVisitType(), labResultsRow.getTestDate(), labResultsRow.getTestDate(), labResultsRow.getTestDate(), loginLocationUuid);
            Encounter encounter = new Encounter();
            encounter.setPatient(patient);
            encounter.setEncounterDatetime(labResultsRow.getTestDate());
            encounter.setEncounterType(encounterService.getEncounterType(LAB_RESULT_ENCOUNTER_TYPE));
            encounter.addProvider(encounterService.getEncounterRoleByUuid(EncounterRole.UNKNOWN_ENCOUNTER_ROLE_UUID), getProvider());
            HashSet<Obs> resultObservations = new HashSet<>();
            for (LabResultRow labResultRow : labResultsRow.getTestResults()) {
                org.openmrs.Concept concept = conceptService.getConceptByName(labResultRow.getTest());
                if (concept.isNumeric()) {
                    ConceptNumeric cn = (ConceptNumeric) concept;
                    if (!cn.isAllowDecimal() && labResultRow.getResult().contains(".")) {
                        throw new APIException("Decimal is not allowed for " + cn.getName() + " concept");
                    }
                }
                Order testOrder = getTestOrder(patient, labResultRow, labResultsRow.getTestDate());
                encounter.addOrder(testOrder);
                resultObservations.add(getResultObs(labResultRow.getResult(), testOrder, concept));
            }
            visit.addEncounter(encounter);
            Encounter savedEncounter = encounterService.saveEncounter(encounter);
            bahmniVisitAttributeSaveCommand.save(savedEncounter);
            Boolean isAuditLogEnabled = Boolean.valueOf(Context.getAdministrationService().getGlobalProperty("bahmni.enableAuditLog"));
            if (isAuditLogEnabled) {
                Map<String, String> params = new HashMap<>();
                params.put("encounterUuid", savedEncounter.getUuid());
                params.put("encounterType", savedEncounter.getEncounterType().getName());
                auditLogService.createAuditLog(patient.getUuid(), "EDIT_ENCOUNTER", "EDIT_ENCOUNTER_MESSAGE", params, "MODULE_LABEL_ADMIN_KEY");
            }
            saveResults(encounter, resultObservations);
            return new Messages();
        } catch (Exception e) {
            throw new APIException(e.getMessage(), e);
        }
    }

    // Hack: OpenMRS doesn't allow saving order and its associated observations in single call
    // throws error object references an unsaved transient instance - save the transient instance before flushing
    private void saveResults(Encounter encounter, HashSet<Obs> resultObservations) {
        for (Obs obs : resultObservations) {
            encounter.addObs(obs);
        }
        encounterService.saveEncounter(encounter);
    }

    private Order getTestOrder(Patient patient, LabResultRow labResultRow, Date testDate) throws ParseException {
        Order order = new Order();
        order.setConcept(conceptService.getConceptByName(labResultRow.getTest()));
        order.setDateActivated(testDate);
        order.setAutoExpireDate(testDate);
        order.setPatient(patient);
        order.setOrderType(orderService.getOrderTypeByName(LAB_ORDER_TYPE));
        order.setCareSetting(orderService.getCareSettingByName(CareSetting.CareSettingType.OUTPATIENT.toString()));
        order.setOrderer(getProvider());
        return order;
    }

    private Obs getResultObs(String labResult, Order testOrder, Concept concept) {
        LabOrderResult labOrderResult = new LabOrderResult();
        labOrderResult.setResult(labResult);
        labOrderResult.setResultDateTime(testOrder.getDateActivated());
        if (concept.getDatatype().getHl7Abbreviation().equals(org.openmrs.ConceptDatatype.CODED)) {
            Concept resultConcept = conceptService.getConceptByName(labResult);
            if (resultConcept != null)
                labOrderResult.setResultUuid(resultConcept.getUuid());
            else
                log.warn(String.format("Concept is not available in OpenMRS for concept name: %s", labResult));
        }
        return labOrderResultMapper.map(labOrderResult, testOrder, testOrder.getConcept());
    }

    private Provider getProvider() {
        Collection<Provider> providers = providerService.getProvidersByPerson(userContext.getAuthenticatedUser().getPerson());
        return providers.size() > 0 ? providers.iterator().next() : null;
    }

    @Override
    public Messages validate(LabResultsRow labResultsRow) {
        return new Messages();
    }
}