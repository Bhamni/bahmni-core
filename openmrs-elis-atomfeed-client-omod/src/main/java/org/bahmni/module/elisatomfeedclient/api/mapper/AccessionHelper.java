package org.bahmni.module.elisatomfeedclient.api.mapper;

import org.apache.commons.lang.time.DateUtils;
import org.bahmni.module.elisatomfeedclient.api.Constants;
import org.bahmni.module.elisatomfeedclient.api.ElisAtomFeedProperties;
import org.bahmni.module.elisatomfeedclient.api.domain.AccessionDiff;
import org.bahmni.module.elisatomfeedclient.api.domain.OpenElisAccession;
import org.bahmni.module.elisatomfeedclient.api.domain.OpenElisTestDetail;
import org.joda.time.DateTime;
import org.openmrs.CareSetting;
import org.openmrs.Encounter;
import org.openmrs.EncounterRole;
import org.openmrs.EncounterType;
import org.openmrs.Location;
import org.openmrs.Order;
import org.openmrs.OrderType;
import org.openmrs.Patient;
import org.openmrs.Provider;
import org.openmrs.User;
import org.openmrs.Visit;
import org.openmrs.VisitType;
import org.openmrs.api.AdministrationService;
import org.openmrs.api.ConceptService;
import org.openmrs.api.EncounterService;
import org.openmrs.api.OrderService;
import org.openmrs.api.PatientService;
import org.openmrs.api.ProviderService;
import org.openmrs.api.UserService;
import org.openmrs.api.VisitService;
import org.openmrs.api.context.Context;
import org.openmrs.module.bahmniemrapi.encountertransaction.service.VisitIdentificationHelper;
import org.openmrs.module.bahmniemrapi.visitlocation.BahmniVisitLocationService;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.bahmni.module.elisatomfeedclient.api.Constants.DEFAULT_INVESTIGATION_ENCOUNTER_TYPE;

public class AccessionHelper {
    private final EncounterService encounterService;
    private final PatientService patientService;
    private final VisitService visitService;
    private final ConceptService conceptService;
    private final AdministrationService administrationService;
    private User labUser;
    private OrderService orderService;
    protected ElisAtomFeedProperties properties;
    private final UserService userService;
    private final ProviderService providerService;
    private BahmniVisitLocationService bahmniVisitLocationService;
    private OrderType labOrderType;

    public AccessionHelper(ElisAtomFeedProperties properties) {
        this(Context.getService(EncounterService.class), Context.getService(PatientService.class),
                Context.getService(VisitService.class), Context.getService(ConceptService.class),
                Context.getService(UserService.class), Context.getService(ProviderService.class),
                Context.getService(OrderService.class), properties, Context.getService(BahmniVisitLocationService.class),
                Context.getService(AdministrationService.class));
    }

    AccessionHelper(EncounterService encounterService, PatientService patientService, VisitService visitService, ConceptService conceptService,
                    UserService userService, ProviderService providerService, OrderService orderService,
                    ElisAtomFeedProperties properties, BahmniVisitLocationService bahmniVisitLocationService, AdministrationService administrationService) {
        this.encounterService = encounterService;
        this.patientService = patientService;
        this.visitService = visitService;
        this.conceptService = conceptService;
        this.orderService = orderService;
        this.properties = properties;
        this.userService = userService;
        this.providerService = providerService;
        this.bahmniVisitLocationService = bahmniVisitLocationService;
        this.administrationService = administrationService;
    }

    public Encounter mapToNewEncounter(OpenElisAccession openElisAccession, String visitType) {
        Patient patient = patientService.getPatientByUuid(openElisAccession.getPatientUuid());
        if (labUser == null) {
            labUser = userService.getUserByUsername(Constants.DEFAULT_LAB_SYSTEM_USERNAME);
        }

        Provider labSystemProvider = getLabSystemProvider();
        EncounterType encounterType = encounterService.getEncounterType(DEFAULT_INVESTIGATION_ENCOUNTER_TYPE);

        Date accessionDate = openElisAccession.fetchDate();
        Visit visit = new VisitIdentificationHelper(visitService, bahmniVisitLocationService).getVisitFor(patient, visitType, accessionDate, null, null, openElisAccession.getLabLocationUuid());
        Location location = Context.getLocationService().getLocationByUuid(openElisAccession.getLabLocationUuid());

        Encounter encounter = newEncounterInstance(visit, patient, labSystemProvider, encounterType, accessionDate, location);
        encounter.setUuid(openElisAccession.getAccessionUuid());

        Set<String> groupedOrders = groupOrders(openElisAccession.getTestDetails());

        Set<Order> orders = createOrders(openElisAccession, groupedOrders, patient);
        addOrdersToEncounter(encounter, orders);
        visit.addEncounter(encounter);
        return encounter;
    }

    private Encounter newEncounterInstance(Visit visit, Patient patient, Provider labSystemProvider, EncounterType encounterType, Date date, Location labLocation) {
        Encounter encounter = new Encounter();
        encounter.setEncounterType(encounterType);
        encounter.setPatient(patient);
        encounter.setEncounterDatetime(date);
        EncounterRole encounterRole = encounterService.getEncounterRoleByUuid(EncounterRole.UNKNOWN_ENCOUNTER_ROLE_UUID);
        encounter.setProvider(encounterRole, labSystemProvider);
        encounter.setVisit(visit);
        encounter.setLocation(labLocation);
        return encounter;
    }

    public Encounter addOrDiscontinueOrderDifferences(OpenElisAccession openElisAccession, AccessionDiff diff,
                                                      Encounter previousEncounter) {
        if (diff.getAddedTestDetails().size() > 0) {
            Set<String> addedOrders = groupOrders(diff.getAddedTestDetails());
            Set<Order> newOrders = createOrders(openElisAccession, addedOrders, previousEncounter.getPatient());
            addOrdersToEncounter(previousEncounter, newOrders);
        }

        Boolean shouldAllowDiscontinueOrdersIfCancelled = administrationService.getGlobalPropertyValue(Constants.GP_ALLOW_DISCONTINUE_ORDERS, Boolean.TRUE);

        if (shouldAllowDiscontinueOrdersIfCancelled && diff.getRemovedTestDetails().size() > 0) {
            Set<String> removedOrders = groupOrders(diff.getRemovedTestDetails());
            discontinueOrders(previousEncounter, removedOrders);
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
            labUser = userService.getUserByUsername(Constants.DEFAULT_LAB_SYSTEM_USERNAME);
        }
        for (String orderConceptUuid : orderConceptUuids) {
            Order order = new Order();
            order.setConcept(conceptService.getConceptByUuid(orderConceptUuid));
            order.setAccessionNumber(openElisAccession.getAccessionUuid());
            order.setCreator(labUser);
            order.setPatient(patient);
            order.setOrderType(getLabOrderType());
            order.setOrderer(getLabSystemProvider());
            order.setCareSetting(orderService.getCareSettingByName(CareSetting.CareSettingType.OUTPATIENT.toString()));
            orders.add(order);
        }
        return orders;
    }

    private OrderType getLabOrderType() {
        if (labOrderType == null) {
            labOrderType = orderService.getOrderTypeByName(Constants.DEFAULT_LAB_ORDER_TYPE);
        }
        return labOrderType;
    }

    private void discontinueOrders(Encounter previousEncounter, Set<String> removedOrders) {
        List<Order> newOrdersForDiscontinue = new ArrayList<>();
        for (String removedOrder : removedOrders) {
            for (Order order : previousEncounter.getOrders()) {
                if (!order.getAction().equals(Order.Action.DISCONTINUE)
                        && order.isActive()
                        && removedOrder.equals(order.getConcept().getUuid())) {
                    Order newOrder = order.cloneForDiscontinuing();
                    newOrder.setOrderer(order.getOrderer());
                    newOrdersForDiscontinue.add(newOrder);
                }
            }
        }
        for (Order order : newOrdersForDiscontinue) {
            previousEncounter.addOrder(order);
        }
    }

    private Set<String> groupOrders(Set<OpenElisTestDetail> openElisAccessionTestDetails) {
        Set<String> orderConceptUuids = new HashSet<>();
        for (OpenElisTestDetail testDetail : openElisAccessionTestDetails) {
            String uuid = testDetail.getPanelUuid() != null ? testDetail.getPanelUuid() : testDetail.getTestUuid();
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
        if (applicableVisit != null) {
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
                Date stopTime = startTime.withTime(23, 59, 59, 000).toDate();
                visit.setStopDatetime(stopTime);
            }
        } else {
            DateTime nextVisitStartTime = new DateTime(nextVisit.getStartDatetime());
            DateTime visitStopDate = startTime.withTime(23, 59, 59, 000);
            boolean isEndTimeBeforeNextVisitStart = visitStopDate.isBefore(nextVisitStartTime);
            if (!isEndTimeBeforeNextVisitStart) {
                visitStopDate = nextVisitStartTime.minusSeconds(1);
            }
            visit.setStopDatetime(visitStopDate.toDate());
        }
        return visit;
    }

    protected Visit getVisitForPatientWithinDates(Patient patient, Date startTime) {
        List<Visit> visits = visitService.getVisits(null, Collections.singletonList(patient), null, null, null, startTime, startTime, null, null, true, false);
        return visits.isEmpty() ? null : visits.get(0);
    }

    protected Visit getVisitForPatientForNearestStartDate(Patient patient, Date startTime) {
        List<Visit> visits = visitService.getVisits(null, Collections.singletonList(patient), null, null, startTime, null, null, null, null, true, false);
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

    public boolean shouldIgnoreAccession(OpenElisAccession openElisAccession) {
        return patientService.getPatientByUuid(openElisAccession.getPatientUuid()) == null;
    }
}
