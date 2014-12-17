package org.bahmni.module.admin.csv.persister;

import org.bahmni.csv.EntityPersister;
import org.bahmni.csv.Messages;
import org.bahmni.module.admin.csv.models.LabResultRow;
import org.bahmni.module.admin.csv.models.LabResultsRow;
import org.bahmni.module.admin.csv.service.PatientMatchService;
import org.openmrs.module.bahmniemrapi.encountertransaction.service.VisitIdentificationHelper;
import org.openmrs.*;
import org.openmrs.api.*;
import org.openmrs.api.context.UserContext;
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
    private UserContext userContext;

    public void init(UserContext userContext, String patientMatchingAlgorithmClassName, boolean shouldMatchExactPatientId) {
        this.userContext = userContext;
        this.patientMatchingAlgorithmClassName = patientMatchingAlgorithmClassName;
        this.shouldMatchExactPatientId = shouldMatchExactPatientId;
    }

    @Override
    public Messages persist(LabResultsRow labResultsRow) {
        try {
            Patient patient = patientMatchService.getPatient(patientMatchingAlgorithmClassName, labResultsRow.getPatientAttributes(), labResultsRow.getPatientIdentifier(), shouldMatchExactPatientId);
            Visit visit = visitIdentificationHelper.getVisitFor(patient, labResultsRow.getVisitType(), labResultsRow.getTestDate(), labResultsRow.getTestDate(), labResultsRow.getTestDate());
            Encounter encounter = new Encounter();
            visit.addEncounter(encounter);
            encounter.setPatient(patient);
            encounter.setEncounterDatetime(labResultsRow.getTestDate());
            encounter.setEncounterType(encounterService.getEncounterType(LAB_RESULT_ENCOUNTER_TYPE));
            encounter.addProvider(encounterService.getEncounterRoleByUuid(EncounterRole.UNKNOWN_ENCOUNTER_ROLE_UUID), getProvider());
            HashSet<Obs> resultObservations = new HashSet<>();
            for (LabResultRow labResultRow : labResultsRow.getTestResults()) {
                TestOrder testOrder = getTestOrder(patient, labResultRow, labResultsRow.getTestDate());
                encounter.addOrder(testOrder);
                resultObservations.add(getResultObs(labResultRow, testOrder));
            }
            encounterService.saveEncounter(encounter);
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

    private TestOrder getTestOrder(Patient patient, LabResultRow labResultRow, Date testDate) throws ParseException {
        TestOrder testOrder = new TestOrder();
        testOrder.setConcept(conceptService.getConceptByName(labResultRow.getTest()));
        testOrder.setDateActivated(testDate);
        testOrder.setAutoExpireDate(testDate);
        testOrder.setPatient(patient);
        testOrder.setOrderType(orderService.getOrderTypeByName(LAB_ORDER_TYPE));
        testOrder.setCareSetting(orderService.getCareSettingByName(CareSetting.CareSettingType.OUTPATIENT.toString()));
        testOrder.setOrderer(getProvider());
        return testOrder;
    }

    private Obs getResultObs(LabResultRow labResultRow, TestOrder testOrder) {
        LabOrderResult labOrderResult = new LabOrderResult();
        labOrderResult.setResult(labResultRow.getResult());
        labOrderResult.setResultDateTime(testOrder.getDateActivated());
        Obs obs = labOrderResultMapper.map(labOrderResult, testOrder, testOrder.getConcept());
        return obs;
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