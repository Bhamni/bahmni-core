package org.bahmni.module.bahmnicore.service.impl;

import org.bahmni.module.bahmnicore.dao.ObsDao;
import org.bahmni.module.bahmnicore.dao.OrderDao;
import org.bahmni.module.bahmnicore.service.BahmniDrugOrderService;
import org.bahmni.test.builder.DrugOrderBuilder;
import org.joda.time.DateTime;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.openmrs.DrugOrder;
import org.openmrs.Order;
import org.openmrs.api.ConceptService;
import org.openmrs.api.PatientService;
import org.openmrs.api.PersonService;
import org.openmrs.module.emrapi.encounter.domain.EncounterTransaction;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.*;


@RunWith(PowerMockRunner.class)
public class BahmniBridgeTest {

    @Mock
    private ObsDao obsDao;
    @Mock
    private PatientService patientService;
    @Mock
    private PersonService personService;
    @Mock
    private OrderDao orderDao;
    @Mock
    private BahmniDrugOrderService bahmniDrugOrderService;
    @Mock
    private ConceptService conceptService;
    BahmniBridge bahmniBridge;

    String patientUuid = "patient-uuid";

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        bahmniBridge = new BahmniBridge(obsDao, patientService, personService, conceptService, orderDao, bahmniDrugOrderService);
        bahmniBridge.forPatient(patientUuid);
    }

    @Test
    public void shouldNotGetOrdersWhichAreScheduledInFuture() throws Exception {
        Date futureDate = DateTime.now().plusDays(10).toDate();
        Date autoExpireDate = DateTime.now().plusDays(40).toDate();
        DrugOrder scheduledDrugOrder = new DrugOrderBuilder().withScheduledDate(futureDate).withAutoExpireDate(autoExpireDate).build();
        PowerMockito.when(bahmniDrugOrderService.getActiveDrugOrders(patientUuid)).thenReturn(Arrays.asList(scheduledDrugOrder));

        List<EncounterTransaction.DrugOrder> drugOrders = bahmniBridge.activeDrugOrdersForPatient();
        Assert.assertEquals(0, drugOrders.size());
    }

    @Test
    public void shouldGetActiveOrders() throws Exception {
        DrugOrder activeOrder = new DrugOrderBuilder().withScheduledDate(null).withAutoExpireDate(DateTime.now().plusMonths(2).toDate()).build();
        PowerMockito.when(bahmniDrugOrderService.getActiveDrugOrders(patientUuid)).thenReturn(Arrays.asList(activeOrder));

        List<EncounterTransaction.DrugOrder> drugOrders = bahmniBridge.activeDrugOrdersForPatient();
        Assert.assertEquals(1, drugOrders.size());
    }

    @Test
    public void shouldGetScheduledOrdersWhichHasBecomeActive() throws Exception {
        DrugOrder scheduledDrugOrder = new DrugOrderBuilder().withScheduledDate(DateTime.now().minusMonths(1).toDate()).build();
        PowerMockito.when(bahmniDrugOrderService.getActiveDrugOrders(patientUuid)).thenReturn(Arrays.asList(scheduledDrugOrder));

        List<EncounterTransaction.DrugOrder> drugOrders = bahmniBridge.activeDrugOrdersForPatient();
        Assert.assertEquals(1, drugOrders.size());
    }

    @Test
    public void shouldGetFirstDrugActivatedDate() throws Exception {
        List<Order> allDrugOrders = new ArrayList<>();
        Order order1 = new Order();
        Date now = new Date();
        order1.setDateActivated(addDays(now, 10));
        allDrugOrders.add(order1);
        Order order2 = new Order();
        order2.setDateActivated(now);
        allDrugOrders.add(order2);
        PowerMockito.when(bahmniDrugOrderService.getAllDrugOrders(patientUuid, null, null, null, null)).thenReturn(allDrugOrders);

        Assert.assertEquals(now, bahmniBridge.getStartDateOfTreatment());

    }

    @Test
    public void shouldGetSchuledDateIfTheDrugIsScheduled() throws Exception {
        List<Order> allDrugOrders = new ArrayList<>();
        Order order1 = new Order();
        Date now = new Date();
        order1.setDateActivated(addDays(now, 10));
        allDrugOrders.add(order1);
        Order order2 = new Order();
        order2.setScheduledDate(addDays(now, 2));
        order2.setDateActivated(now);
        allDrugOrders.add(order2);
        PowerMockito.when(bahmniDrugOrderService.getAllDrugOrders(patientUuid, null, null, null, null)).thenReturn(allDrugOrders);

        Assert.assertEquals(addDays(now, 2), bahmniBridge.getStartDateOfTreatment());

    }

    public Date addDays(Date now, int days) {
        Calendar c = Calendar.getInstance();
        c.setTime(now);
        c.add(Calendar.DATE, days);
        return c.getTime();
    }
}