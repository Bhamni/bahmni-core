package org.bahmni.module.elisatomfeedclient.api.mapper;

import org.bahmni.module.elisatomfeedclient.api.Constants;
import org.bahmni.module.elisatomfeedclient.api.ElisAtomFeedProperties;
import org.bahmni.module.elisatomfeedclient.api.builder.OpenElisAccessionBuilder;
import org.bahmni.module.elisatomfeedclient.api.builder.OpenElisTestDetailBuilder;
import org.bahmni.module.elisatomfeedclient.api.domain.AccessionDiff;
import org.bahmni.module.elisatomfeedclient.api.domain.OpenElisAccession;
import org.bahmni.module.elisatomfeedclient.api.domain.OpenElisTestDetail;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.openmrs.Concept;
import org.openmrs.Encounter;
import org.openmrs.EncounterRole;
import org.openmrs.EncounterType;
import org.openmrs.Order;
import org.openmrs.OrderType;
import org.openmrs.Patient;
import org.openmrs.Person;
import org.openmrs.Provider;
import org.openmrs.User;
import org.openmrs.Visit;
import org.openmrs.VisitType;
import org.openmrs.api.AdministrationService;
import org.openmrs.api.ConceptService;
import org.openmrs.api.EncounterService;
import org.openmrs.api.LocationService;
import org.openmrs.api.OrderService;
import org.openmrs.api.PatientService;
import org.openmrs.api.ProviderService;
import org.openmrs.api.UserService;
import org.openmrs.api.VisitService;
import org.openmrs.api.context.Context;
import org.openmrs.module.bahmniemrapi.visitlocation.BahmniVisitLocationService;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.anyBoolean;
import static org.mockito.Mockito.anyCollection;
import static org.mockito.Mockito.anyMap;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

@PrepareForTest(Context.class)
@RunWith(PowerMockRunner.class)

public class AccessionHelperTest {
    @Mock
    EncounterService encounterService;
    @Mock
    PatientService patientService;
    @Mock
    VisitService visitService;
    @Mock
    ConceptService conceptService;

    @Mock
    BahmniVisitLocationService bahmniVisitLocationService;

    @Mock
    private ElisAtomFeedProperties feedProperties;
    @Mock
    private UserService userService;
    @Mock
    private ProviderService providerService;
    @Mock
    private OrderService orderService;
    @Mock
    private LocationService locationService;
    @Mock
    private AdministrationService administrationService;

    private AccessionHelper accessionHelper;
    private static final String VISIT_START_DATE = "2014-01-15 15:25:43+0530";
    private static final String ENCOUNTER_START_DATE = "2014-01-17T17:25:43Z";
    private static String DATE_FORMAT = "yyyy-MM-dd HH:mm:ssZ";
    private SimpleDateFormat simpleDateFormat;


    @Before
    public void setUp() {
        initMocks(this);
        accessionHelper = new AccessionHelper(encounterService, patientService, visitService, conceptService, userService, providerService, orderService, feedProperties,bahmniVisitLocationService, administrationService);
        simpleDateFormat = new SimpleDateFormat(DATE_FORMAT);
    }

    @Test
    public void shouldMapToNewEncounter() throws ParseException {
        OpenElisTestDetail panel1 = new OpenElisTestDetailBuilder().withPanelUuid("panel1").withTestUuid("test1").build();
        OpenElisTestDetail test2 = new OpenElisTestDetailBuilder().withTestUuid("test2").build();
        HashSet<OpenElisTestDetail> testDetails = new HashSet<>();

        testDetails.add(panel1);
        testDetails.add(test2);
        Patient patient = new Patient();
        List<Visit> visits = createVisits(1);

        User provider = new User();

        when(patientService.getPatientByUuid(any(String.class))).thenReturn(patient);
        when(encounterService.getEncounterType("Consultation")).thenReturn(new EncounterType());
        when(conceptService.getConceptByUuid("panel1")).thenReturn(getConceptByUuid("panel1"));
        when(conceptService.getConceptByUuid("test2")).thenReturn(getConceptByUuid("test2"));
        when(visitService.saveVisit(any(Visit.class))).thenReturn(visits.get(0));
        when(visitService.getVisitTypes("LAB_RESULTS")).thenReturn(Arrays.asList(visits.get(0).getVisitType()));
        when(visitService.getVisits(anyCollection(), anyCollection(), anyCollection(), anyCollection(), any(Date.class), any(Date.class), any(Date.class), any(Date.class), anyMap(), anyBoolean(), anyBoolean())).thenReturn(visits);
        when(userService.getUserByUsername(anyString())).thenReturn(provider);
        when(providerService.getProvidersByPerson(any(Person.class))).thenReturn(Arrays.asList(new Provider()));
        when(encounterService.getEncounterRoleByUuid(EncounterRole.UNKNOWN_ENCOUNTER_ROLE_UUID)).thenReturn(new EncounterRole());
        when(orderService.getOrderTypes(true)).thenReturn(Arrays.asList(getOrderType()));
        PowerMockito.mockStatic(Context.class);
        when(Context.getAuthenticatedUser()).thenReturn(provider);
        when(Context.getLocationService()).thenReturn(locationService);
        when(locationService.getLocationByUuid(anyString())).thenReturn(null);

        OpenElisAccession openElisAccession = new OpenElisAccessionBuilder().withTestDetails(testDetails).build();
        openElisAccession.setDateTime(ENCOUNTER_START_DATE);
        Encounter encounter = accessionHelper.mapToNewEncounter(openElisAccession, "LAB_RESULTS");

        Set<Order> orders = encounter.getOrders();
        Assert.assertEquals(2, orders.size());
        verify(conceptService, never()).getConceptByUuid("test1");

        for (Order order : orders) {
            Assert.assertTrue(order.getConcept().getUuid().equals("panel1") || order.getConcept().getUuid().equals("test2"));
        }
    }

    @Test
    public void shouldFindProperVisitAndMapToNewEncounter() throws ParseException {
        OpenElisTestDetail panel1 = new OpenElisTestDetailBuilder().withPanelUuid("panel1").withTestUuid("test1").build();
        OpenElisTestDetail test2 = new OpenElisTestDetailBuilder().withTestUuid("test2").build();
        HashSet<OpenElisTestDetail> testDetails = new HashSet<>();
        testDetails.add(panel1);
        testDetails.add(test2);
        Patient patient = new Patient();
        List<Visit> visits = createVisits(3);
        User provider = new User();

        when(patientService.getPatientByUuid(any(String.class))).thenReturn(patient);
        when(encounterService.getEncounterType("Consultation")).thenReturn(new EncounterType());
        when(conceptService.getConceptByUuid("panel1")).thenReturn(getConceptByUuid("panel1"));
        when(conceptService.getConceptByUuid("test2")).thenReturn(getConceptByUuid("test2"));
        when(visitService.getVisitTypes("LAB_RESULTS")).thenReturn(Arrays.asList(visits.get(0).getVisitType()));
        when(visitService.getVisits(anyCollection(), anyCollection(), anyCollection(), anyCollection(), any(Date.class), any(Date.class), any(Date.class), any(Date.class), anyMap(), anyBoolean(), anyBoolean())).thenReturn(visits);
        when(userService.getUserByUsername(anyString())).thenReturn(provider);
        when(providerService.getProvidersByPerson(any(Person.class))).thenReturn(Arrays.asList(new Provider()));
        when(encounterService.getEncounterRoleByUuid(EncounterRole.UNKNOWN_ENCOUNTER_ROLE_UUID)).thenReturn(new EncounterRole());
        when(orderService.getOrderTypes(true)).thenReturn(Arrays.asList(getOrderType()));
        when(visitService.saveVisit(any(Visit.class))).thenReturn(visits.get(0));
        PowerMockito.mockStatic(Context.class);
        when(Context.getAuthenticatedUser()).thenReturn(provider);
        when(Context.getLocationService()).thenReturn(locationService);
        when(locationService.getLocationByUuid(anyString())).thenReturn(null);

        OpenElisAccession openElisAccession = new OpenElisAccessionBuilder().withTestDetails(testDetails).build();
        openElisAccession.setDateTime(ENCOUNTER_START_DATE);
        Encounter encounter = accessionHelper.mapToNewEncounter(openElisAccession, "LAB_RESULTS");

        Date startDatetime = encounter.getVisit().getStartDatetime();
        Assert.assertTrue("Encounter should be before or after visit start", encounter.getEncounterDatetime().compareTo(startDatetime) >= 0);
        Set<Order> orders = encounter.getOrders();
        Assert.assertEquals(2, orders.size());
        verify(conceptService, never()).getConceptByUuid("test1");

        for (Order order : orders) {
            Assert.assertTrue(order.getConcept().getUuid().equals("panel1") || order.getConcept().getUuid().equals("test2"));
        }
    }

    @Test
    public void shouldMapNewOrdersToExistingEncounter() {
        Encounter previousEncounter = new Encounter();
        Order panel = getOrderWithConceptUuid("panel");
        Order test = getOrderWithConceptUuid("test");
        HashSet<Order> orders = new HashSet<>();
        orders.add(panel);
        orders.add(test);
        previousEncounter.setOrders(orders);
        when(userService.getUserByUsername(anyString())).thenReturn(new User());
        when(providerService.getProvidersByPerson(any(Person.class))).thenReturn(Arrays.asList(new Provider()));
        when(administrationService.getGlobalPropertyValue(Constants.GP_ALLOW_DISCONTINUE_ORDERS, Boolean.TRUE)).thenReturn(Boolean.TRUE);
        AccessionDiff diff = new AccessionDiff();
        diff.addAddedTestDetail(new OpenElisTestDetailBuilder().withTestUuid("test2").build());
        diff.addAddedTestDetail(new OpenElisTestDetailBuilder().withTestUuid("panel1").build());

        Encounter encounter = accessionHelper.addOrDiscontinueOrderDifferences(new OpenElisAccessionBuilder().build(), diff, previousEncounter);

        Assert.assertEquals(4, encounter.getOrders().size());
    }

    @Test
    public void shouldMapDeletedOrdersToExistingEncounter() {
        Encounter previousEncounter = new Encounter();
        Order panel = getOrderWithConceptUuid("panel1");
        Order test = getOrderWithConceptUuid("test2");
        HashSet<Order> orders = new HashSet<>();
        orders.add(panel);
        orders.add(test);
        previousEncounter.setOrders(orders);

        AccessionDiff diff = new AccessionDiff();
        diff.addRemovedTestDetails(new OpenElisTestDetailBuilder().withTestUuid("test2").withStatus("Cancelled").build());
        when(administrationService.getGlobalPropertyValue(Constants.GP_ALLOW_DISCONTINUE_ORDERS, Boolean.TRUE)).thenReturn(Boolean.TRUE);

        Encounter encounter = accessionHelper.addOrDiscontinueOrderDifferences(new OpenElisAccessionBuilder().build(), diff, previousEncounter);

        Set<Order> result = encounter.getOrders();
        Assert.assertEquals(3, result.size());
        for (Order order : result) {
            if (order.getAction().equals(Order.Action.DISCONTINUE)) {
                Assert.assertTrue(order.getPreviousOrder().getConcept().getUuid().endsWith(order.getConcept().getUuid()));
            }
        }
    }

    @Test
    public void shouldNotMapDeletedOrdersToExistingEncounter() {
        Encounter previousEncounter = new Encounter();
        Order panel = getOrderWithConceptUuid("panel2");
        Order test = getOrderWithConceptUuid("test3");
        HashSet<Order> orders = new HashSet<>();
        orders.add(panel);
        orders.add(test);
        previousEncounter.setOrders(orders);

        AccessionDiff diff = new AccessionDiff();
        diff.addRemovedTestDetails(new OpenElisTestDetailBuilder().withTestUuid("test3").withStatus("Cancelled").build());
        when(administrationService.getGlobalPropertyValue(Constants.GP_ALLOW_DISCONTINUE_ORDERS, Boolean.TRUE)).thenReturn(Boolean.FALSE);

        Encounter encounter = accessionHelper.addOrDiscontinueOrderDifferences(new OpenElisAccessionBuilder().build(), diff, previousEncounter);

        Set<Order> result = encounter.getOrders();
        Assert.assertEquals(2, result.size());
        for (Order order : result) {
            Assert.assertEquals(order.getAction(), Order.Action.NEW);
        }
    }

    @Test
    public void shouldReturnTrueIfPatientWithTheGivenUuidIsNotPresent() {
        OpenElisAccession openElisAccession = new OpenElisAccessionBuilder().withPatientUuid("uuid1").build();
        when(patientService.getPatientByUuid("uuid1")).thenReturn(new Patient());
        assertThat(accessionHelper.shouldIgnoreAccession(openElisAccession), is(false));
    }

    @Test
    public void shouldReturnFalseIfPatientWithTheGivenUuidIsNotPresent() {
        OpenElisAccession openElisAccession = new OpenElisAccessionBuilder().withPatientUuid("uuid2").build();
        assertThat(accessionHelper.shouldIgnoreAccession(openElisAccession), is(true));
    }

    private Order getOrderWithConceptUuid(String conceptUuid) {
        Order order = new Order();
        Concept concept = new Concept();
        concept.setUuid(conceptUuid);
        order.setConcept(concept);
        order.setDateActivated(new Date());
        return order;
    }

    private Concept getConceptByUuid(String uuid) {
        Concept concept = new Concept();
        concept.setUuid(uuid);
        return concept;
    }

    private List<Visit> createVisits(int i) throws ParseException {
        List<Visit> visits = new ArrayList<>();

        for(int j = 0;j<i;j++){
            Calendar datetime = Calendar.getInstance();
            datetime.setTime(simpleDateFormat.parse(VISIT_START_DATE));
            Visit visit = new Visit();
            datetime.add(Calendar.DAY_OF_MONTH, j);
            visit.setStartDatetime(datetime.getTime());
            datetime.add(Calendar.DAY_OF_MONTH, 1);
            visit.setStopDatetime(datetime.getTime());
            VisitType visitType = new VisitType();
            visitType.setName("LAB_RESULTS");
            visit.setVisitType(visitType);
            visits.add(visit);

        }
        return visits;
    }

    private OrderType getOrderType() {
        OrderType orderType = new OrderType();
        orderType.setName("Order");
        return orderType;
    }
}
