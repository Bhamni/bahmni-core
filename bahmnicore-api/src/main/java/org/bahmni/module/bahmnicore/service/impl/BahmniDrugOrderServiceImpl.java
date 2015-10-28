package org.bahmni.module.bahmnicore.service.impl;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.bahmni.module.bahmnicore.contract.drugorder.ConceptData;
import org.bahmni.module.bahmnicore.contract.drugorder.DrugOrderConfigResponse;
import org.bahmni.module.bahmnicore.contract.drugorder.OrderFrequencyData;
import org.bahmni.module.bahmnicore.dao.OrderDao;
import org.bahmni.module.bahmnicore.dao.PatientDao;
import org.bahmni.module.bahmnicore.model.BahmniFeedDrugOrder;
import org.bahmni.module.bahmnicore.service.BahmniDrugOrderService;
import org.joda.time.DateTime;
import org.joda.time.Days;
import org.openmrs.*;
import org.openmrs.api.*;
import org.openmrs.api.context.Context;
import org.openmrs.module.bahmniemrapi.drugorder.contract.BahmniOrderAttribute;
import org.openmrs.module.bahmniemrapi.drugorder.dosinginstructions.FlexibleDosingInstructions;
import org.openmrs.module.bahmniemrapi.encountertransaction.command.impl.BahmniVisitAttributeSaveCommandImpl;
import org.openmrs.module.bahmniemrapi.encountertransaction.service.VisitIdentificationHelper;
import org.openmrs.module.emrapi.encounter.ConceptMapper;
import org.openmrs.module.emrapi.encounter.domain.EncounterTransaction;
import org.openmrs.module.emrapi.utils.HibernateLazyLoader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class BahmniDrugOrderServiceImpl implements BahmniDrugOrderService {
    private VisitService visitService;
    private ConceptService conceptService;
    private OrderService orderService;
    private EncounterService encounterService;
    private ProviderService providerService;
    private UserService userService;
    private PatientDao patientDao;
    private PatientService openmrsPatientService;
    private OrderDao orderDao;
    private OrderType drugOrderType;
    private Provider systemProvider;
    private EncounterRole unknownEncounterRole;
    private EncounterType consultationEncounterType;
    private String systemUserName;
    private ConceptMapper conceptMapper = new ConceptMapper();
    private BahmniVisitAttributeSaveCommandImpl bahmniVisitAttributeSaveCommand;

    private static final String GP_DOSING_INSTRUCTIONS_CONCEPT_UUID = "order.dosingInstructionsConceptUuid";

    @Autowired
    public BahmniDrugOrderServiceImpl(VisitService visitService, ConceptService conceptService, OrderService orderService,
                                      ProviderService providerService, EncounterService encounterService,
                                      UserService userService, PatientDao patientDao,
                                      PatientService patientService, OrderDao orderDao, BahmniVisitAttributeSaveCommandImpl bahmniVisitAttributeSaveCommand) {
        this.visitService = visitService;
        this.conceptService = conceptService;
        this.orderService = orderService;
        this.providerService = providerService;
        this.encounterService = encounterService;
        this.userService = userService;
        this.patientDao = patientDao;
        this.openmrsPatientService = patientService;
        this.orderDao = orderDao;
        this.bahmniVisitAttributeSaveCommand = bahmniVisitAttributeSaveCommand;
    }

    @Override
    public void add(String patientId, Date orderDate, List<BahmniFeedDrugOrder> bahmniDrugOrders, String systemUserName, String visitTypeName) {
        if (StringUtils.isEmpty(patientId))
            throwPatientNotFoundException(patientId);

        Patient patient = patientDao.getPatient(patientId);
        if (patient == null)
            throwPatientNotFoundException(patientId);

        this.systemUserName = systemUserName;
        Visit visitForDrugOrders = new VisitIdentificationHelper(visitService).getVisitFor(patient, visitTypeName, orderDate);
        addDrugOrdersToVisit(orderDate, bahmniDrugOrders, patient, visitForDrugOrders);
    }

    @Override
    public List<DrugOrder> getActiveDrugOrders(String patientUuid) {
        return getActiveDrugOrders(patientUuid, new Date());
    }

    private List<DrugOrder> getActiveDrugOrders(String patientUuid, Date asOfDate) {
        Patient patient = openmrsPatientService.getPatientByUuid(patientUuid);
        List<Order> orders = orderService.getActiveOrders(patient, orderService.getOrderTypeByName("Drug order"),
                orderService.getCareSettingByName(CareSetting.CareSettingType.OUTPATIENT.toString()), asOfDate);
        return getDrugOrders(orders);
    }

    private List<DrugOrder> getDrugOrders(List<Order> orders) {
        HibernateLazyLoader hibernateLazyLoader = new HibernateLazyLoader();
        List<DrugOrder> drugOrders = new ArrayList<>();
        for (Order order : orders) {
            order = hibernateLazyLoader.load(order);
            if (order instanceof DrugOrder) {
                drugOrders.add((DrugOrder) order);
            }
        }
        return drugOrders;
    }

    @Override
    public List<DrugOrder> getPrescribedDrugOrders(String patientUuid, Boolean includeActiveVisit, Integer numberOfVisits) {
        Patient patient = openmrsPatientService.getPatientByUuid(patientUuid);
        return orderDao.getPrescribedDrugOrders(patient, includeActiveVisit, numberOfVisits);
    }

    @Override
    public List<DrugOrder> getPrescribedDrugOrders(List<String> visitUuids) {
        return orderDao.getPrescribedDrugOrders(visitUuids);
    }

    @Override
    public List<DrugOrder> getPrescribedDrugOrdersForConcepts(Patient patient, Boolean includeActiveVisit, List<Visit> visits, List<Concept> concepts) {
        if (concepts.isEmpty() || concepts == null) {
            return new ArrayList<>();
        }
        return orderDao.getPrescribedDrugOrdersForConcepts(patient, includeActiveVisit, visits, concepts);
    }

    @Override
    public DrugOrderConfigResponse getConfig() {
        DrugOrderConfigResponse response = new DrugOrderConfigResponse();
        response.setFrequencies(getFrequencies());
        response.setRoutes(mapConcepts(orderService.getDrugRoutes()));
        response.setDoseUnits(mapConcepts(orderService.getDrugDosingUnits()));
        response.setDurationUnits(mapConcepts(orderService.getDurationUnits()));
        response.setDispensingUnits(mapConcepts(orderService.getDrugDispensingUnits()));
        response.setDosingInstructions(mapConcepts(getSetMembersOfConceptSetFromGP(GP_DOSING_INSTRUCTIONS_CONCEPT_UUID)));
        response.setOrderAttributes(fetchOrderAttributeConcepts());
        return response;
    }

    @Override
    public List<Order> getAllDrugOrders(String patientUuid) {
        Patient patientByUuid = openmrsPatientService.getPatientByUuid(patientUuid);
        OrderType orderTypeByUuid = orderService.getOrderTypeByUuid(OrderType.DRUG_ORDER_TYPE_UUID);
        return orderDao.getAllOrders(patientByUuid, Arrays.asList(orderTypeByUuid));
    }

    private List<EncounterTransaction.Concept> fetchOrderAttributeConcepts() {
        Concept orderAttributesConceptSet = conceptService.getConceptByName(BahmniOrderAttribute.ORDER_ATTRIBUTES_CONCEPT_SET_NAME);
        if (orderAttributesConceptSet != null) {
            List<EncounterTransaction.Concept> etOrderAttributeConcepts = new ArrayList<>();
            List<Concept> orderAttributes = orderAttributesConceptSet.getSetMembers();
            for (Concept orderAttribute : orderAttributes) {
                etOrderAttributeConcepts.add(conceptMapper.map(orderAttribute));
            }
            return etOrderAttributeConcepts;
        }
        return Collections.EMPTY_LIST;
    }

    private List<Concept> getSetMembersOfConceptSetFromGP(String globalProperty) {
        String conceptUuid = Context.getAdministrationService().getGlobalProperty(globalProperty);
        Concept concept = Context.getConceptService().getConceptByUuid(conceptUuid);
        if (concept != null && concept.isSet()) {
            return concept.getSetMembers();
        }
        return Collections.emptyList();
    }

    private List<ConceptData> mapConcepts(List<Concept> drugDosingUnits) {
        List<ConceptData> listOfDoseUnits = new ArrayList<>();
        for (Concept drugDosingUnit : drugDosingUnits) {
            listOfDoseUnits.add(new ConceptData(drugDosingUnit));
        }
        return listOfDoseUnits;
    }

    private List<OrderFrequencyData> getFrequencies() {
        List<OrderFrequencyData> listOfFrequencyData = new ArrayList<>();
        for (OrderFrequency orderFrequency : orderService.getOrderFrequencies(false)) {
            listOfFrequencyData.add(new OrderFrequencyData(orderFrequency));
        }
        return listOfFrequencyData;
    }

    private void throwPatientNotFoundException(String patientId) {
        throw new RuntimeException("Patient Id is null or empty. PatientId='" + patientId + "'. Patient may have been directly created in billing system.");
    }

    private void addDrugOrdersToVisit(Date orderDate, List<BahmniFeedDrugOrder> bahmniDrugOrders, Patient patient, Visit visit) {
        List<DrugOrder> drugOrders = createOrders(patient, orderDate, bahmniDrugOrders);
        List<DrugOrder> remainingNewDrugOrders = checkOverlappingOrderAndUpdate(drugOrders, patient.getUuid(), orderDate);
        if (remainingNewDrugOrders.isEmpty()) return;

        Encounter systemConsultationEncounter = createNewSystemConsultationEncounter(orderDate, patient);
        for (Order drugOrder : remainingNewDrugOrders) {
            drugOrder.setEncounter(systemConsultationEncounter);
            systemConsultationEncounter.addOrder(drugOrder);
        }
        visit.addEncounter(systemConsultationEncounter);
        Encounter savedEncounter = encounterService.saveEncounter(systemConsultationEncounter);
        bahmniVisitAttributeSaveCommand.save(savedEncounter);
    }

    private List<DrugOrder> checkOverlappingOrderAndUpdate(List<DrugOrder> newDrugOrders, String patientUuid, Date orderDate) {
        List<DrugOrder> activeDrugOrders = getActiveDrugOrders(patientUuid, orderDate);
        List<DrugOrder> drugOrdersToRemove = new ArrayList<>();
        for (DrugOrder newDrugOrder : newDrugOrders) {
            for (DrugOrder activeDrugOrder : activeDrugOrders) {
                if (newDrugOrder.hasSameOrderableAs(activeDrugOrder)) {
                    Encounter encounter = activeDrugOrder.getEncounter();
                    newDrugOrder.setEncounter(encounter);
                    encounter.addOrder(newDrugOrder);
                    int totalNumberOfDays = getNumberOfDays(activeDrugOrder) + getNumberOfDays(newDrugOrder);
                    newDrugOrder.setDateActivated(activeDrugOrder.getDateActivated());
                    setDuration(newDrugOrder, totalNumberOfDays);
                    newDrugOrder.setQuantity(activeDrugOrder.getQuantity() + newDrugOrder.getQuantity());
                    activeDrugOrder.setVoided(true);
                    activeDrugOrder.setVoidReason("To create a new drug order of same concept");
                    encounterService.saveEncounter(encounter);
                    drugOrdersToRemove.add(newDrugOrder);
                }
            }
        }
        newDrugOrders.removeAll(drugOrdersToRemove);
        return newDrugOrders;
    }

    private int getNumberOfDays(DrugOrder activeDrugOrder) {
        return Days.daysBetween(new DateTime(activeDrugOrder.getDateActivated()), new DateTime(activeDrugOrder.getAutoExpireDate())).getDays();
    }

    private Encounter createNewSystemConsultationEncounter(Date orderDate, Patient patient) {
        Encounter systemConsultationEncounter;
        systemConsultationEncounter = new Encounter();
        systemConsultationEncounter.setProvider(getEncounterRole(), getSystemProvider());
        systemConsultationEncounter.setEncounterType(getConsultationEncounterType());
        systemConsultationEncounter.setPatient(patient);
        systemConsultationEncounter.setEncounterDatetime(orderDate);
        return systemConsultationEncounter;
    }

    private EncounterType getConsultationEncounterType() {
        if (consultationEncounterType == null) {
            consultationEncounterType = encounterService.getEncounterType("Consultation");
        }
        return consultationEncounterType;
    }

    private EncounterRole getEncounterRole() {
        if (unknownEncounterRole == null) {
            for (EncounterRole encounterRole : encounterService.getAllEncounterRoles(false)) {
                if (encounterRole.getName().equalsIgnoreCase("unknown")) {
                    unknownEncounterRole = encounterRole;
                }
            }
        }
        return unknownEncounterRole;
    }

    private Provider getSystemProvider() {
        if (systemProvider == null) {
            User systemUser = userService.getUserByUsername(systemUserName);
            Collection<Provider> providers = providerService.getProvidersByPerson(systemUser.getPerson());
            systemProvider = providers == null ? null : providers.iterator().next();
        }
        return systemProvider;
    }

    private List<DrugOrder> createOrders(Patient patient, Date orderDate, List<BahmniFeedDrugOrder> bahmniDrugOrders) {
        List<DrugOrder> orders = new ArrayList<>();
        for (BahmniFeedDrugOrder bahmniDrugOrder : bahmniDrugOrders) {
            DrugOrder drugOrder = new DrugOrder();
            Drug drug = conceptService.getDrugByUuid(bahmniDrugOrder.getProductUuid());
            drugOrder.setDrug(drug);
            drugOrder.setConcept(drug.getConcept());
            drugOrder.setDateActivated(orderDate);
            drugOrder.setPatient(patient);
            drugOrder.setAsNeeded(false);
            drugOrder.setOrderType(getDrugOrderType());
            drugOrder.setOrderer(getSystemProvider());
            drugOrder.setCareSetting(orderService.getCareSettingByName(CareSetting.CareSettingType.OUTPATIENT.toString()));
            drugOrder.setDosingType(FlexibleDosingInstructions.class);
            drugOrder.setDosingInstructions(createInstructions(bahmniDrugOrder, drugOrder));
            drugOrder.setQuantity(bahmniDrugOrder.getQuantity());
            drugOrder.setQuantityUnits(conceptService.getConceptByName("Unit(s)"));
            drugOrder.setNumRefills(0);
            drugOrder.setUuid(bahmniDrugOrder.getOrderUuid());
            setDuration(drugOrder, bahmniDrugOrder.getNumberOfDays());
            orders.add(drugOrder);
        }
        return orders;
    }

    private void setDuration(DrugOrder drugOrder, int numberOfDays) {
        drugOrder.setAutoExpireDate(DateUtils.addDays(drugOrder.getDateActivated(), numberOfDays));
        drugOrder.setDuration(numberOfDays);
        drugOrder.setDurationUnits(conceptService.getConceptByMapping(Duration.SNOMED_CT_DAYS_CODE, Duration.SNOMED_CT_CONCEPT_SOURCE_HL7_CODE));
    }

    private String createInstructions(BahmniFeedDrugOrder bahmniDrugOrder, DrugOrder drugOrder) {
        return "{\"dose\":\"" + bahmniDrugOrder.getDosage() + "\", \"doseUnits\":\"" + drugOrder.getDrug().getDosageForm().getDisplayString() + "\"}";
    }

    private OrderType getDrugOrderType() {
        if (drugOrderType == null) {
            drugOrderType = orderService.getOrderTypeByName("Drug order");
        }
        return drugOrderType;
    }
}
