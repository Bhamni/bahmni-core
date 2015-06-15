package org.bahmni.module.bahmnicore.service.impl;

import org.bahmni.module.bahmnicore.dao.ObsDao;
import org.bahmni.module.bahmnicore.service.BahmniObsService;
import org.bahmni.module.bahmnicore.service.BahmniOrderService;
import org.bahmni.module.bahmnicore.service.OrderService;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.openmrs.*;
import org.openmrs.api.ConceptService;
import org.openmrs.module.bahmniemrapi.order.contract.BahmniOrder;
import org.openmrs.module.emrapi.encounter.ObservationMapper;
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
    private Order order;
    private Concept concept;
    private ConceptName conceptName;
    private OrderType orderType;
    private Provider provider;
    private Patient patient;

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

    @Before
    public void setUp() {
        initMocks(this);

        mockStatic(LocaleUtility.class);
        when(LocaleUtility.getDefaultLocale()).thenReturn(Locale.ENGLISH);
        bahmniOrderService = new BahmniOrderServiceImpl(orderService, bahmniObsService);

    }

    @Test
    public void shouldGetLatestObservationsAndOrdersForOrderType() throws Exception {
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
    public void shouldGetLatestObservationsForOrder() throws Exception {
        Order order = createOrder();
        when(orderService.getOrderByUuid("someOrderUuid")).thenReturn(order);
        bahmniOrderService.ordersForOrder(personUUID, Arrays.asList(concept), null, "someOrderUuid");
        verify(bahmniObsService).observationsFor(personUUID, Arrays.asList(concept), null, null, false);
    }

    private Order createOrder() {
        order = new Order();
        patient = new Patient();
        patient.setId(1);
        patient.setUuid(personUUID);
        concept= new Concept();
        orderType = new OrderType();
        provider = new Provider();
        conceptName = new ConceptName();
        orderType.setId(1);
        orderType.setUuid("someOrderTypeUuid");
        order.setOrderType(orderType);
        provider.setId(2);
        provider.setName("Superman");
        order.setOrderer(provider);
        conceptName.setName("someConceptName");
        concept.setNames(Arrays.asList(conceptName));
        order.setConcept(concept);
        order.setId(1);
        order.setPatient(patient);
        CareSetting careSetting = new CareSetting();
        careSetting.setId(1);
        order.setCareSetting(careSetting);
        return order;
    }
}
