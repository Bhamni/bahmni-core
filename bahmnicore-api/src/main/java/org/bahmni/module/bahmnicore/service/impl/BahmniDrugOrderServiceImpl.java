package org.bahmni.module.bahmnicore.service.impl;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.bahmni.module.bahmnicore.dao.BahmniPatientDao;
import org.bahmni.module.bahmnicore.dao.OrderDao;
import org.bahmni.module.bahmnicore.model.BahmniDrugOrder;
import org.bahmni.module.bahmnicore.service.BahmniDrugOrderService;
import org.bahmni.module.bahmnicore.util.VisitIdentificationHelper;
import org.joda.time.DateTime;
import org.joda.time.Days;
import org.openmrs.*;
import org.openmrs.api.*;
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
    private BahmniPatientDao bahmniPatientDao;
    private PatientService openmrsPatientService;
    private OrderDao orderDao;
    private OrderType drugOrderType = null;
    private Provider systemProvider = null;
    private EncounterRole unknownEncounterRole = null;
    private EncounterType consultationEncounterType = null;
    private String systemUserName = null;
    public static final String PHARMACY_VISIT = "PHARMACY VISIT";

    @Autowired
    public BahmniDrugOrderServiceImpl(VisitService visitService, ConceptService conceptService, OrderService orderService,
                                      ProviderService providerService, EncounterService encounterService,
                                      UserService userService, BahmniPatientDao bahmniPatientDao,
                                      PatientService patientService, OrderDao orderDao) {
        this.visitService = visitService;
        this.conceptService = conceptService;
        this.orderService = orderService;
        this.providerService = providerService;
        this.encounterService = encounterService;
        this.userService = userService;
        this.bahmniPatientDao = bahmniPatientDao;
        this.openmrsPatientService = patientService;
        this.orderDao = orderDao;
    }

    @Override
    public void add(String patientId, Date orderDate, List<BahmniDrugOrder> bahmniDrugOrders, String systemUserName) {
        if (StringUtils.isEmpty(patientId))
            throwPatientNotFoundException(patientId);

        Patient patient = bahmniPatientDao.getPatient(patientId);
        if (patient == null)
            throwPatientNotFoundException(patientId);

        this.systemUserName = systemUserName;
        Visit visitForDrugOrders = new VisitIdentificationHelper(visitService).getVisitFor(patient, orderDate, PHARMACY_VISIT);
        addDrugOrdersToVisit(orderDate, bahmniDrugOrders, patient, visitForDrugOrders);
    }

    @Override
    public List<DrugOrder> getActiveDrugOrders(String patientUuid) {
        return getActiveDrugOrders(patientUuid, new Date());
    }

    @Override
    public List<DrugOrder> getActiveDrugOrders(String patientUuid, Date asOfDate) {
        Patient patient = openmrsPatientService.getPatientByUuid(patientUuid);
        return (List<DrugOrder>)(List<? extends Order>)orderService.getActiveOrders(patient, orderService.getOrderTypeByName("Drug Order"), orderService.getCareSettingByName("Outpatient"), asOfDate);
    }

    @Override
    public List<DrugOrder> getPrescribedDrugOrders(String patientUuid, Boolean includeActiveVisit, Integer numberOfVisits) {
        Patient patient = openmrsPatientService.getPatientByUuid(patientUuid);
        return orderDao.getPrescribedDrugOrders(patient, includeActiveVisit, numberOfVisits);
    }

    private void throwPatientNotFoundException(String patientId) {
        throw new RuntimeException("Patient Id is null or empty. PatientId='" + patientId + "'. Patient may have been directly created in billing system.");
    }

    private void addDrugOrdersToVisit(Date orderDate, List<BahmniDrugOrder> bahmniDrugOrders, Patient patient, Visit visit) {
        Set<DrugOrder> drugOrders = createOrders(patient, orderDate, bahmniDrugOrders);
        Set<DrugOrder> remainingNewDrugOrders = checkOverlappingOrderAndUpdate(drugOrders, patient.getUuid(), orderDate);
        if(remainingNewDrugOrders.isEmpty()) return;

        Encounter systemConsultationEncounter = createNewSystemConsultationEncounter(orderDate, patient);
        for (Order drugOrder : remainingNewDrugOrders) {
            drugOrder.setEncounter(systemConsultationEncounter);
            systemConsultationEncounter.addOrder(drugOrder);
        }
        visit.addEncounter(systemConsultationEncounter);
        visitService.saveVisit(visit);
        for (Encounter encounter : visit.getEncounters()) {
            encounterService.saveEncounter(encounter);
        }
    }

    private Set<DrugOrder> checkOverlappingOrderAndUpdate(Set<DrugOrder> newDrugOrders, String patientUuid, Date orderDate) {
        List<DrugOrder> activeDrugOrders = getActiveDrugOrders(patientUuid, orderDate);
        Iterator<DrugOrder> newDrugOrdersIterator = newDrugOrders.iterator();

        while (newDrugOrdersIterator.hasNext()) {
            DrugOrder newDrugOrder = newDrugOrdersIterator.next();
            for(DrugOrder activeDrugOrder: activeDrugOrders) {
                if(newDrugOrder.hasSameOrderableAs(activeDrugOrder)) {
                    Encounter encounter = activeDrugOrder.getEncounter();
                    newDrugOrder.setEncounter(encounter);
                    encounter.addOrder(newDrugOrder);
                    int numberOfDaysBetweenDrugs = Days.daysBetween(new DateTime(activeDrugOrder.getDateActivated()), new DateTime(newDrugOrder.getDateActivated())).getDays();
                    int activeDrugOrderNumberOfDays = Days.daysBetween(new DateTime(activeDrugOrder.getDateActivated()), new DateTime(activeDrugOrder.getAutoExpireDate())).getDays();
                    newDrugOrder.setAutoExpireDate(DateUtils.addDays(newDrugOrder.getAutoExpireDate(), (activeDrugOrderNumberOfDays - numberOfDaysBetweenDrugs)));
                    newDrugOrder.setDateActivated(activeDrugOrder.getDateActivated());
                    newDrugOrder.setQuantity(activeDrugOrder.getQuantity() + newDrugOrder.getQuantity());
                    activeDrugOrder.setVoided(true);
                    activeDrugOrder.setVoidReason("To create a new drug order of same concept");
                    encounterService.saveEncounter(encounter);
                    newDrugOrdersIterator.remove();
                }
            }
        }
        return newDrugOrders;
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
            consultationEncounterType = encounterService.getEncounterType("OPD");
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

    private Set<DrugOrder> createOrders(Patient patient, Date orderDate, List<BahmniDrugOrder> bahmniDrugOrders) {
        Set<DrugOrder> orders = new HashSet<>();
        for (BahmniDrugOrder bahmniDrugOrder : bahmniDrugOrders) {
            DrugOrder drugOrder = new DrugOrder();
            Drug drug = conceptService.getDrugByUuid(bahmniDrugOrder.getProductUuid());
            drugOrder.setDrug(drug);
            drugOrder.setConcept(drug.getConcept());
            drugOrder.setDateActivated(orderDate);
            drugOrder.setAutoExpireDate(DateUtils.addDays(orderDate, bahmniDrugOrder.getNumberOfDays()));
            drugOrder.setPatient(patient);
            drugOrder.setPrn(false);
            drugOrder.setOrderType(getDrugOrderType());
            drugOrder.setOrderer(getSystemProvider());
            drugOrder.setCareSetting(orderService.getCareSettingByName("Outpatient"));
            drugOrder.setDosingType(DrugOrder.DosingType.FREE_TEXT);
            drugOrder.setDosingInstructions(createInstructions(bahmniDrugOrder, drugOrder));
            drugOrder.setQuantity(bahmniDrugOrder.getQuantity());
            drugOrder.setQuantityUnits(drug.getDosageForm());
            drugOrder.setNumRefills(0);
            orders.add(drugOrder);
        }
        return orders;
    }

    private String createInstructions(BahmniDrugOrder bahmniDrugOrder, DrugOrder drugOrder) {
        return bahmniDrugOrder.getDosage() + " " + drugOrder.getDrug().getDosageForm().getDisplayString();
    }

    private OrderType getDrugOrderType() {
        if (drugOrderType == null) {
            List<OrderType> allOrderTypes = orderService.getOrderTypes(true);
            for (OrderType type : allOrderTypes) {
                if (type.getName().toLowerCase().equals("drug order")) {
                    drugOrderType = type;
                }
            }
        }
        return drugOrderType;
    }
}
