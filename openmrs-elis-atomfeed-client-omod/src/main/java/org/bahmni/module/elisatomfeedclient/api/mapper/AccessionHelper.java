package org.bahmni.module.elisatomfeedclient.api.mapper;

import org.apache.commons.lang.time.DateUtils;
import org.bahmni.module.elisatomfeedclient.api.ElisAtomFeedProperties;
import org.bahmni.module.elisatomfeedclient.api.domain.AccessionDiff;
import org.bahmni.module.elisatomfeedclient.api.domain.OpenElisAccession;
import org.bahmni.module.elisatomfeedclient.api.domain.OpenElisTestDetail;
import org.bahmni.module.bahmnicore.util.VisitIdentificationHelper;
import org.joda.time.DateTime;
import org.openmrs.*;
import org.openmrs.api.ConceptService;
import org.openmrs.api.EncounterService;
import org.openmrs.api.OrderService;
import org.openmrs.api.PatientService;
import org.openmrs.api.ProviderService;
import org.openmrs.api.UserService;
import org.openmrs.api.VisitService;
import org.openmrs.api.context.Context;

import java.util.*;

public class AccessionHelper {
    private final EncounterService encounterService;
    private final PatientService patientService;
    private final VisitService visitService;
    private final ConceptService conceptService;
    private User labUser;
    private OrderService orderService;
    private ElisAtomFeedProperties properties;
    private final UserService userService;
    private final ProviderService providerService;
    private OrderType labOrderType;

    public AccessionHelper(ElisAtomFeedProperties properties) {
        this(Context.getService(EncounterService.class), Context.getService(PatientService.class), Context.getService(VisitService.class), Context.getService(ConceptService.class), Context.getService(UserService.class), Context.getService(ProviderService.class),Context.getService(OrderService.class), properties);
    }

    AccessionHelper(EncounterService encounterService, PatientService patientService, VisitService visitService, ConceptService conceptService, UserService userService, ProviderService providerService, OrderService orderService, ElisAtomFeedProperties properties) {
        this.encounterService = encounterService;
        this.patientService = patientService;
        this.visitService = visitService;
        this.conceptService = conceptService;
        this.orderService = orderService;
        this.properties = properties;
        this.userService = userService;
        this.providerService = providerService;

    }

    public Encounter mapToNewEncounter(OpenElisAccession openElisAccession, String visitType) {
        Patient patient = patientService.getPatientByUuid(openElisAccession.getPatientUuid());
        if (labUser == null) {
            labUser = userService.getUserByUsername(properties.getLabSystemUserName());
        }

        Provider labSystemProvider = getLabSystemProvider();
        EncounterType encounterType = encounterService.getEncounterType(properties.getEncounterTypeInvestigation());

        Date accessionDate = openElisAccession.fetchDate();
        Visit visit = new VisitIdentificationHelper(visitService).getVisitFor(patient, accessionDate, visitType);

        Encounter encounter = newEncounterInstance(visit, patient, labSystemProvider, encounterType,  accessionDate);
        encounter.setUuid(openElisAccession.getAccessionUuid());

        Set<String> groupedOrders = groupOrders(openElisAccession.getTestDetails());

        Set<Order> orders = createOrders(openElisAccession, groupedOrders, patient);
        addOrdersToEncounter(encounter, orders);
        visit.addEncounter(encounter);
        return encounter;
    }

    public Encounter newEncounterInstance(Visit visit, Patient patient, Provider labSystemProvider, EncounterType encounterType, Date date) {
        Encounter encounter = new Encounter();
        encounter.setEncounterType(encounterType);
        encounter.setPatient(patient);
        encounter.setEncounterDatetime(date);
        EncounterRole encounterRole = encounterService.getEncounterRoleByUuid(EncounterRole.UNKNOWN_ENCOUNTER_ROLE_UUID);
        encounter.setProvider(encounterRole, labSystemProvider);
        encounter.setVisit(visit);
        return encounter;
    }

    public Encounter addOrVoidOrderDifferences(OpenElisAccession openElisAccession, AccessionDiff diff, Encounter previousEncounter) {
        if (diff.getAddedTestDetails().size() > 0) {
            Set<String> addedOrders = groupOrders(diff.getAddedTestDetails());
            Set<Order> newOrders = createOrders(openElisAccession, addedOrders, previousEncounter.getPatient());
            addOrdersToEncounter(previousEncounter, newOrders);
        }

        if (diff.getRemovedTestDetails().size() > 0) {
            Set<String> removedOrders = groupOrders(diff.getRemovedTestDetails());
            voidOrders(previousEncounter, removedOrders);
        }

        return previousEncounter;
    }

    private void addOrdersToEncounter(Encounter encounter, Set<Order> orders) {
        for (Order order : orders) {
            encounter.addOrder(order);
        }
    }

    private Set<Order> createOrders(OpenElisAccession openElisAccession, Set<String> orderConceptUuids, Patient patient) {
        Set<Order> orders = new HashSet<>();
        if (labUser == null) {
            labUser = userService.getUserByUsername(properties.getLabSystemUserName());
        }
        OrderType orderType = getLabOrderType();
        for (String orderConceptUuid : orderConceptUuids) {
            TestOrder order = new TestOrder();
            order.setConcept(conceptService.getConceptByUuid(orderConceptUuid));
            order.setAccessionNumber(openElisAccession.getAccessionUuid());
            order.setCreator(labUser);
            order.setPatient(patient);
            order.setOrderType(orderType);
            order.setOrderer(getLabSystemProvider());
            order.setDateActivated(openElisAccession.fetchDate());
            order.setAutoExpireDate(order.getDateActivated());
            order.setCareSetting(orderService.getCareSettingByName("Outpatient"));
            orders.add(order);
        }
        return orders;
    }

    private OrderType getLabOrderType() {
        if(labOrderType == null){
            List<OrderType> orderTypes = orderService.getOrderTypes(true);
            for (OrderType orderType : orderTypes) {
                if (orderType.getName().equals(properties.getOrderTypeLabOrderName())){
                    labOrderType = orderType;
                    break;
                }
            }
        }
        return labOrderType;
    }

    private void voidOrders(Encounter previousEncounter, Set<String> removedOrders) {
        for (String removedOrder : removedOrders) {
            for (Order order : previousEncounter.getOrders()) {
                if (removedOrder.equals(order.getConcept().getUuid())) {
                    order.setVoided(true);
                }
            }
        }
    }

    private Set<String> groupOrders(Set<OpenElisTestDetail> openElisAccessionTestDetails) {
        Set<String> orderConceptUuids = new HashSet<>();
        for (OpenElisTestDetail testDetail : openElisAccessionTestDetails) {
            String uuid = null;
            if (testDetail.getPanelUuid() != null) {
                uuid = testDetail.getPanelUuid();
            } else {
                uuid = testDetail.getTestUuid();
            }
            orderConceptUuids.add(uuid);
        }
        return orderConceptUuids;
    }

    private Provider getLabSystemProvider() {
        Collection<Provider> labSystemProviders = providerService.getProvidersByPerson(labUser.getPerson());
        return labSystemProviders == null ? null : labSystemProviders.iterator().next();
    }

    public Visit findOrInitializeVisit(Patient patient, Date visitDate, String visitType) {
        Visit applicableVisit = getVisitForPatientWithinDates(patient, visitDate);
        if (applicableVisit != null){
            return applicableVisit;
        }
        Visit visit = new Visit();
        visit.setPatient(patient);
        visit.setVisitType(getVisitTypeByName(visitType));
        visit.setStartDatetime(visitDate);
        visit.setEncounters(new HashSet<Encounter>());
        visit.setUuid(UUID.randomUUID().toString());

        Visit nextVisit = getVisitForPatientForNearestStartDate(patient, visitDate);
        DateTime startTime = new DateTime(visitDate);
        if (nextVisit == null) {
            if (!DateUtils.isSameDay(visitDate, new Date())) {
                Date stopTime = startTime.withTime(23,59, 59, 000).toDate();
                visit.setStopDatetime(stopTime);
            }
        } else {
            DateTime nextVisitStartTime = new DateTime(nextVisit.getStartDatetime());
            DateTime visitStopDate = startTime.withTime(23,59, 59, 000);
            boolean isEndTimeBeforeNextVisitStart = visitStopDate.isBefore(nextVisitStartTime);
            if (!isEndTimeBeforeNextVisitStart) {
                visitStopDate = nextVisitStartTime.minusSeconds(1);
            }
            visit.setStopDatetime(visitStopDate.toDate());
        }
        return visit;
    }

    protected Visit getVisitForPatientWithinDates(Patient patient, Date startTime) {
        List<Visit> visits = visitService.getVisits(null, Arrays.asList(patient), null, null, null, startTime, startTime, null, null, true, false);
        return visits.isEmpty() ? null : visits.get(0);
    }

    protected Visit getVisitForPatientForNearestStartDate(Patient patient, Date startTime) {
        List<Visit> visits = visitService.getVisits(null, Arrays.asList(patient), null, null, startTime, null, null, null, null, true, false);
        if (visits.isEmpty()) {
            return null;
        }
        Collections.sort(visits, new Comparator<Visit>() {
            @Override
            public int compare(Visit v1, Visit v2) {
                return v1.getStartDatetime().compareTo(v2.getStartDatetime());
            }
        });
        return visits.get(0);
    }

    private VisitType getVisitTypeByName(String visitTypeName) {
        List<VisitType> visitTypes = visitService.getVisitTypes(visitTypeName);
        return visitTypes.isEmpty() ? null : visitTypes.get(0);
    }

}
