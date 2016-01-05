package org.bahmni.module.bahmnicore.service.impl;

import org.bahmni.module.bahmnicore.dao.ObsDao;
import org.bahmni.module.bahmnicore.service.BahmniObsService;
import org.bahmni.module.bahmnicore.service.BahmniOrderService;
import org.bahmni.module.bahmnicore.service.OrderService;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import static org.mockito.Matchers.any;
import org.mockito.Mock;
import org.openmrs.*;
import org.openmrs.api.ConceptService;
import org.openmrs.module.bahmniemrapi.order.contract.BahmniOrder;
import org.openmrs.module.emrapi.encounter.ConceptMapper;
import org.openmrs.module.emrapi.encounter.ObservationMapper;
import org.openmrs.module.emrapi.encounter.domain.EncounterTransaction;
import org.openmrs.module.emrapi.encounter.matcher.ObservationTypeMatcher;
import org.openmrs.util.LocaleUtility;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import static org.mockito.Mockito.verify;
import static org.mockito.MockitoAnnotations.initMocks;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.when;

@RunWith(PowerMockRunner.class)
@PrepareForTest(LocaleUtility.class)
public class BahmniOrderServiceImplTest {

    BahmniOrderService bahmniOrderService;

    private String personUUID = "12345";
    private String visitUUID = "54321";
    private Order order;
    private Concept concept;
    private ConceptName conceptName;
    private OrderType orderType;
    private Provider provider;
    private Patient patient;
    private EncounterTransaction.Concept  encounterTransactionconcept;

    @Mock
    ObsDao obsDao;
    @Mock
    private ObservationTypeMatcher observationTypeMatcher;
    @Mock
    private ObservationMapper observationMapper;
    @Mock
    private BahmniObsService bahmniObsService;
    @Mock
    private OrderService orderService;
    @Mock
    private ConceptService conceptService;
    @Mock
    private ConceptMapper conceptMapper;


    @Before
    public void setUp() {
        initMocks(this);

        mockStatic(LocaleUtility.class);
        when(LocaleUtility.getDefaultLocale()).thenReturn(Locale.ENGLISH);
        concept= new org.bahmni.test.builder.ConceptBuilder().withUUID("otUUID").build();
        encounterTransactionconcept = new EncounterTransaction.Concept();
        encounterTransactionconcept.setName("Concept for order");
        encounterTransactionconcept.setConceptClass("otClass");
        encounterTransactionconcept.setDataType("N/A");
        encounterTransactionconcept.setUuid("otUUID");
        when(conceptMapper.map(any(Concept.class))).thenReturn(encounterTransactionconcept);
        bahmniOrderService = new BahmniOrderServiceImpl(orderService, bahmniObsService, conceptMapper);

    }

    @Test
    public void shouldGetBahmniOrdersForOrderType() throws Exception {
        when(orderService.getAllOrdersForVisits(personUUID, "someOrderTypeUuid", 2)).thenReturn(Arrays.asList(createOrder(), createOrder(), createOrder()));
        List<BahmniOrder> bahmniOrders = bahmniOrderService.ordersForOrderType(personUUID, Arrays.asList(concept), 2, null, "someOrderTypeUuid", true);
        verify(orderService).getAllOrdersForVisits(personUUID, "someOrderTypeUuid", 2);
        Assert.assertEquals(3, bahmniOrders.size());
    }

    @Test
    public void shouldGetAllOrdersIfNumberOfVisitsIsNullOrZero() throws Exception {
        when(orderService.getAllOrders(personUUID, "someOrderTypeUuid", null, null)).thenReturn(Arrays.asList(createOrder(), createOrder(), createOrder()));
        List<BahmniOrder> bahmniOrders = bahmniOrderService.ordersForOrderType(personUUID, Arrays.asList(concept), null, null, "someOrderTypeUuid", true);
        verify(orderService).getAllOrders(personUUID, "someOrderTypeUuid", null, null);
        Assert.assertEquals(3, bahmniOrders.size());
    }

    @Test
    public void shouldNotSetObservationIfIncludeObsFlagIsSetToFalse() throws Exception {
        when(orderService.getAllOrders(personUUID, "someOrderTypeUuid", null, null)).thenReturn(Arrays.asList(createOrder(), createOrder(), createOrder()));
        List<BahmniOrder> bahmniOrders = bahmniOrderService.ordersForOrderType(personUUID, Arrays.asList(concept), null, null, "someOrderTypeUuid", false);
        verify(orderService).getAllOrders(personUUID, "someOrderTypeUuid", null, null);
        Assert.assertEquals(3, bahmniOrders.size());
        Assert.assertNull(bahmniOrders.get(0).getBahmniObservations());
    }

    @Test
    public void shouldGetBahmniOrdersForOrder() throws Exception {
        Order order = createOrder();
        when(orderService.getOrderByUuid("someOrderUuid")).thenReturn(order);
        bahmniOrderService.ordersForOrderUuid(personUUID, Arrays.asList(concept), null, "someOrderUuid");
        verify(bahmniObsService).observationsFor(personUUID, Arrays.asList(concept), null, null, false, order, null, null);
    }

    @Test
    public void shouldGetBahmniOrdersForVisit() throws Exception {
        when(orderService.getAllOrdersForVisitUuid(visitUUID, "someOrderTypeUuid")).thenReturn(Arrays.asList(createOrder(), createOrder()));
        List<BahmniOrder> bahmniOrders = bahmniOrderService.ordersForVisit(visitUUID, "someOrderTypeUuid", null, Arrays.asList(concept));
        verify(bahmniObsService).getObservationForVisit(visitUUID, null, Arrays.asList(concept), false, order);
        verify(orderService).getAllOrdersForVisitUuid(visitUUID, "someOrderTypeUuid");
        Assert.assertEquals(2, bahmniOrders.size());
    }

    private Order createOrder() {
        order = new Order();
        patient = new Patient();
        patient.setId(1);
        patient.setUuid(personUUID);
        orderType = new OrderType();
        provider = new Provider();
        conceptName = new ConceptName();
        orderType.setId(1);
        orderType.setUuid("someOrderTypeUuid");
        order.setOrderType(orderType);
        provider.setId(2);
        provider.setName("Superman");
        order.setOrderer(provider);
        order.setConcept(concept);
        order.setId(1);
        order.setPatient(patient);
        CareSetting careSetting = new CareSetting();
        careSetting.setId(1);
        order.setCareSetting(careSetting);
        return order;
    }
}
