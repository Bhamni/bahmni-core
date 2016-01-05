package org.bahmni.module.bahmnicore.service.impl;

import org.apache.commons.lang3.time.DateUtils;
import org.bahmni.module.bahmnicore.BaseIntegrationTest;
import org.bahmni.module.bahmnicore.contract.drugorder.DrugOrderConfigResponse;
import org.bahmni.module.bahmnicore.model.BahmniFeedDrugOrder;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.DrugOrder;
import org.openmrs.Encounter;
import org.openmrs.EncounterProvider;
import org.openmrs.Order;
import org.openmrs.Patient;
import org.openmrs.Visit;
import org.openmrs.VisitType;
import org.openmrs.api.EncounterService;
import org.openmrs.api.PatientService;
import org.openmrs.api.ProviderService;
import org.openmrs.api.VisitService;
import org.openmrs.api.context.Context;
import org.openmrs.module.emrapi.encounter.domain.EncounterTransaction;
import org.springframework.beans.factory.annotation.Autowired;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

public class BahmniDrugOrderServiceImplIT extends BaseIntegrationTest {

    public static final String TEST_VISIT_TYPE = "TEST VISIT TYPE";
    @Autowired
    private BahmniDrugOrderServiceImpl bahmniDrugOrderService;
    @Autowired
    private VisitService visitService;
    @Autowired
    private PatientService patientService;
    @Autowired
    private EncounterService encounterService;
    @Autowired
    private ProviderService providerService;
    private DateFormat dateOnly;

    @Before
    public void setUp() throws Exception {
        dateOnly = new SimpleDateFormat("dd.MM.yyyy");
        executeDataSet("drugOrdersTestData.xml");
        executeDataSet("visitAttributeDataSet.xml");
    }

    @Test
    public void shouldCreateNewEncounterAndAddDrugOrdersWhenActiveVisitExists() {
        Patient patient = patientService.getPatient(1);
        Visit activeVisit = createActiveVisit(patient);
        assertNull(activeVisit.getEncounters());
        Date orderDate = new Date();
        BahmniFeedDrugOrder calpol = new BahmniFeedDrugOrder("3e4933ff-7799-11e3-a96a-0800271c1b75", 2.0, 10, 20.0,"mg", "order-uuid-1");
        BahmniFeedDrugOrder cetrizine = new BahmniFeedDrugOrder("f5bf0aa6-7855-11e3-bd53-328f386b70f0", 3.0, 5, 21.0, "mg", "order-uuid-2");
        BahmniFeedDrugOrder cetzine = new BahmniFeedDrugOrder("f5bf0aa6-7855-11e3-bd53-328f386b70fa", 0.0, 0, 10.0, "mg", "order-uuid-3");
        List<BahmniFeedDrugOrder> drugOrders = Arrays.asList(calpol, cetrizine, cetzine);

        bahmniDrugOrderService.add("GAN200000", orderDate, drugOrders, "System", TEST_VISIT_TYPE);

        Visit visit = visitService.getVisit(activeVisit.getId());
        Encounter encounter = (Encounter) visit.getEncounters().toArray()[0];
        EncounterProvider encounterProvider = (EncounterProvider) encounter.getEncounterProviders().toArray()[0];
        assertEquals("System", encounterProvider.getProvider().getName());
        assertEquals("Unknown", encounterProvider.getEncounterRole().getName());
        assertEquals("Consultation", encounter.getEncounterType().getName());
        assertEquals(3, encounter.getOrders().size());

        assertDrugOrder(encounter.getOrders(), "Calpol", orderDate, calpol.getDosage(), calpol.getNumberOfDays());
        assertDrugOrder(encounter.getOrders(), "Cetirizine", orderDate, cetrizine.getDosage(), cetrizine.getNumberOfDays());
        assertDrugOrder(encounter.getOrders(), "Cetzine", orderDate, cetzine.getDosage(), cetzine.getNumberOfDays());
    }

    @Test
    public void shouldCreateNewEncounterAndAddOrdersToVisitOnOrderDateWhenActiveVisitDoesNotExist() throws ParseException {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd-MM-yyyy");
        Date orderDate = simpleDateFormat.parse("01-01-2014");

        BahmniFeedDrugOrder calpol = new BahmniFeedDrugOrder("3e4933ff-7799-11e3-a96a-0800271c1b75", 2.0, 10, 20.0, "mg", "order-uuid-1");
        BahmniFeedDrugOrder cetrizine = new BahmniFeedDrugOrder("f5bf0aa6-7855-11e3-bd53-328f386b70f0", 3.0, 5, 21.0, "mg", "order-uuid-2");
        BahmniFeedDrugOrder cetzine = new BahmniFeedDrugOrder("f5bf0aa6-7855-11e3-bd53-328f386b70fa", 0.0, 0, 10.0, "mg", "order-uuid-3");
        List<BahmniFeedDrugOrder> drugOrders = Arrays.asList(calpol, cetrizine, cetzine);

        Patient patient = patientService.getPatient(1);
        Visit visit = createVisitForDate(patient, null, orderDate, false, DateUtils.addDays(orderDate, 1));
        assertNull(visit.getEncounters());

        bahmniDrugOrderService.add("GAN200000", orderDate, drugOrders, "System", TEST_VISIT_TYPE);

        Visit savedVisit = visitService.getVisit(visit.getId());
        Encounter encounter = (Encounter) savedVisit.getEncounters().toArray()[0];
        EncounterProvider encounterProvider = (EncounterProvider) encounter.getEncounterProviders().toArray()[0];
        assertEquals("System", encounterProvider.getProvider().getName());
        assertEquals("Unknown", encounterProvider.getEncounterRole().getName());
        assertEquals("Consultation", encounter.getEncounterType().getName());
        assertEquals(3, encounter.getOrders().size());

        assertDrugOrder(encounter.getOrders(), "Calpol", orderDate, calpol.getDosage(), calpol.getNumberOfDays());
        assertDrugOrder(encounter.getOrders(), "Cetirizine", orderDate, cetrizine.getDosage(), cetrizine.getNumberOfDays());
        assertDrugOrder(encounter.getOrders(), "Cetzine", orderDate, cetzine.getDosage(), cetzine.getNumberOfDays());
    }

    @Test
    public void shouldCreateNewEncounterAndAddOrdersAndChangeVisitEndDate_ToVisitAtTheDateClosestToOrderDate_WhenActiveVisitDoesNotExist() throws ParseException {
        Date orderDate = new SimpleDateFormat("dd-MM-yyyy").parse("01-01-2014");

        BahmniFeedDrugOrder calpol = new BahmniFeedDrugOrder("3e4933ff-7799-11e3-a96a-0800271c1b75", 2.0, 10, 20.0, "mg", "order-uuid-1");
        BahmniFeedDrugOrder cetrizine = new BahmniFeedDrugOrder("f5bf0aa6-7855-11e3-bd53-328f386b70f0", 3.0, 5, 21.0, "mg", "order-uuid-2");
        BahmniFeedDrugOrder cetzine = new BahmniFeedDrugOrder("f5bf0aa6-7855-11e3-bd53-328f386b70fa", 0.0, 0, 10.0, "mg", "order-uuid-3");
        List<BahmniFeedDrugOrder> drugOrders = Arrays.asList(calpol, cetrizine, cetzine);

        Patient patient = patientService.getPatient(1);
        Visit visit1 = createVisitForDate(patient, null, DateUtils.addDays(orderDate, -5), false,
                DateUtils.addDays(DateUtils.addDays(orderDate, -5), 1));
        Visit visit2 = createVisitForDate(patient, null, DateUtils.addDays(orderDate, -3), false, DateUtils.addDays(DateUtils.addDays(orderDate, -3), 1));
        assertNull(visit2.getEncounters());

        bahmniDrugOrderService.add("GAN200000", orderDate, drugOrders, "System", TEST_VISIT_TYPE);

        Visit savedVisit = visitService.getVisitsByPatient(patient).get(0);

        assertEquals(new SimpleDateFormat("dd-MM-yyyy hh:mm:ss").parse("01-01-2014 23:59:59"), savedVisit.getStopDatetime());

        Encounter encounter = (Encounter) savedVisit.getEncounters().toArray()[0];
        EncounterProvider encounterProvider = (EncounterProvider) encounter.getEncounterProviders().toArray()[0];
        assertEquals("System", encounterProvider.getProvider().getName());
        assertEquals("Unknown", encounterProvider.getEncounterRole().getName());
        assertEquals("Consultation", encounter.getEncounterType().getName());
        assertEquals(3, encounter.getOrders().size());

        assertDrugOrder(encounter.getOrders(), "Calpol", orderDate, calpol.getDosage(), calpol.getNumberOfDays());
        assertDrugOrder(encounter.getOrders(), "Cetirizine", orderDate, cetrizine.getDosage(), cetrizine.getNumberOfDays());
        assertDrugOrder(encounter.getOrders(), "Cetzine", orderDate, cetzine.getDosage(), cetzine.getNumberOfDays());
    }

    @Test
    public void shouldAddOrdersToNewEncounterWhenAnotherEncounterExists() throws ParseException {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd-MM-yyyy");
        Date orderDate = simpleDateFormat.parse("01-01-2014");
        orderDate = DateUtils.addHours(orderDate, 2);
        Patient patient = patientService.getPatient(1);
        Date visitStartDate = DateUtils.addHours(orderDate, 10);

        BahmniFeedDrugOrder calpol = new BahmniFeedDrugOrder("3e4933ff-7799-11e3-a96a-0800271c1b75", 2.0, 10, 20.0, "mg", "order-uuid-1");
        BahmniFeedDrugOrder cetrizine = new BahmniFeedDrugOrder("f5bf0aa6-7855-11e3-bd53-328f386b70f0", 3.0, 5, 21.0, "mg", "order-uuid-2");
        BahmniFeedDrugOrder cetzine = new BahmniFeedDrugOrder("f5bf0aa6-7855-11e3-bd53-328f386b70fa", 0.0, 0, 10.0, "mg", "order-uuid-3");
        List<BahmniFeedDrugOrder> drugOrders = Arrays.asList(calpol, cetrizine, cetzine);

        Encounter systemConsultationEncounter = createSystemConsultationEncounter(patient, visitStartDate);
        Visit visit = createVisitForDate(patient, systemConsultationEncounter, visitStartDate, false, DateUtils.addDays(visitStartDate, 1));
        assertEquals(1, visit.getEncounters().size());

        bahmniDrugOrderService.add("GAN200000", orderDate, drugOrders, "System", TEST_VISIT_TYPE);

        Visit savedVisit = visitService.getVisit(visit.getId());
        assertEquals(2, savedVisit.getEncounters().size());
        List<Order> orders = getOrders(savedVisit);

        assertEquals(3, orders.size());
        assertDrugOrder(orders, "Calpol", orderDate, calpol.getDosage(), calpol.getNumberOfDays());
        assertDrugOrder(orders, "Cetirizine", orderDate, cetrizine.getDosage(), cetrizine.getNumberOfDays());
        assertDrugOrder(orders, "Cetzine", orderDate, cetzine.getDosage(), cetzine.getNumberOfDays());
    }

    @Test
    public void shouldMergeNewDrugOrderWithActiveOrderOfSameConcept() throws ParseException {
        Date firstOrderDate = createDate("01-01-2014");
        Patient patient = patientService.getPatient(1);
        Visit visit = createVisitForDate(patient, null, firstOrderDate, false, firstOrderDate);
        int firstOrderNumberOfDays = 10;
        BahmniFeedDrugOrder calpolFirstOrder = new BahmniFeedDrugOrder("3e4933ff-7799-11e3-a96a-0800271c1b75", 2.0, firstOrderNumberOfDays, firstOrderNumberOfDays * 2.0, "mg", "order-uuid");
        bahmniDrugOrderService.add("GAN200000", firstOrderDate, Arrays.asList(calpolFirstOrder), "System", TEST_VISIT_TYPE);
        Date secondOrderDate = DateUtils.addDays(firstOrderDate, 1);
        int secondOrderNumberOfDays = 20;
        BahmniFeedDrugOrder calpolSecondOrder = new BahmniFeedDrugOrder("3e4933ff-7799-11e3-a96a-0800271c1b75", 2.0, secondOrderNumberOfDays, secondOrderNumberOfDays * 2.0, "mg", "order-uuid-2");

        bahmniDrugOrderService.add("GAN200000", secondOrderDate, Arrays.asList(calpolSecondOrder), "System", TEST_VISIT_TYPE);

        Visit savedVisit = visitService.getVisit(visit.getId());
        assertEquals(1, savedVisit.getEncounters().size());
        List<Order> orders = getOrders(savedVisit);
        assertEquals(2, orders.size());
        DrugOrder voidedOrder = (DrugOrder)getFirstVoidedOrder(orders);
        DrugOrder nonVoidedOrder = (DrugOrder)getFirstNonVoidedOrder(orders);
        assertEquals(createDate("01-01-2014"), nonVoidedOrder.getDateActivated());
        assertEquals(dateOnly.format(createDate("31-01-2014")), dateOnly.format(nonVoidedOrder.getAutoExpireDate()));
        assertEquals((Integer) 30, nonVoidedOrder.getDuration());
        assertEquals("Days", nonVoidedOrder.getDurationUnits().getName().getName());
        assertNotNull(voidedOrder);
    }

    @Test
    public void shouldReturnOrderAttributeConceptNamesWithGetConfig() throws ParseException {
        DrugOrderConfigResponse config = bahmniDrugOrderService.getConfig();
        List<EncounterTransaction.Concept> orderAttributes = config.getOrderAttributes();

        assertEquals(2,orderAttributes.size());
        assertEquals("dispensed",orderAttributes.get(0).getName());
        assertEquals("administered",orderAttributes.get(1).getName());
    }

    @Test
    public void shouldReturnDiscontinuedOrderMap() throws Exception {
        executeDataSet("patientWithStoppedOrders.xml");
        DrugOrder newFirstOrder = (DrugOrder) Context.getOrderService().getOrder(15);
        DrugOrder revisedFirstOrder = (DrugOrder) Context.getOrderService().getOrder(16);
        DrugOrder newSecondOrder = (DrugOrder) Context.getOrderService().getOrder(18);
        DrugOrder discontinuedFirstOrder = (DrugOrder) Context.getOrderService().getOrder(17);
        DrugOrder discontinuedSecondOrder = (DrugOrder) Context.getOrderService().getOrder(19);

        List<DrugOrder> drugOrdersList=Arrays.asList(newFirstOrder, revisedFirstOrder, newSecondOrder);
        Map<String, DrugOrder> discontinuedOrderMap = bahmniDrugOrderService.getDiscontinuedDrugOrders(drugOrdersList);
        assertEquals(discontinuedOrderMap.get("2").getUuid(), discontinuedFirstOrder.getUuid());
        assertEquals(discontinuedOrderMap.get("4").getUuid(), discontinuedSecondOrder.getUuid());
        assertNull(discontinuedOrderMap.get("1"));

    }

    @Test
    public void shouldReturnEmptyDiscontinuedOrderMapWhenThereAreNoActiveDrugOrders() throws Exception {
        List<DrugOrder> drugOrdersList=new ArrayList<>();
        Map<String, DrugOrder> discontinuedOrderMap = bahmniDrugOrderService.getDiscontinuedDrugOrders(drugOrdersList);
        Assert.assertNotNull(discontinuedOrderMap);
        assertEquals(0, discontinuedOrderMap.size());

    }

    private Order getFirstVoidedOrder(List<Order> orders) {
        for(Order order: orders){
            if(order.getVoided()) return order;
        }
        return null;
    }

    private Order getFirstNonVoidedOrder(List<Order> orders) {
        for(Order order: orders){
            if(!order.getVoided()) return order;
        }
        return null;
    }

    private Date createDate(String str) throws ParseException {
        return DateUtils.parseDate(str, "dd-MM-yyyy");
    }

    private ArrayList<Order> getOrders(Visit savedVisit) {
        Set<Encounter> encounters = savedVisit.getEncounters();
        Set<Order> orders = new HashSet<>();
        for (Encounter encounter : encounters) {
            orders.addAll(encounter.getOrders());
        }
        return new ArrayList<Order>(orders);
    }

    private Encounter createSystemConsultationEncounter(Patient patient, Date encounterDate) {
        Encounter systemConsultationEncounter = new Encounter();
        systemConsultationEncounter.setEncounterType(encounterService.getEncounterType("Consultation"));
        systemConsultationEncounter.setProvider(encounterService.getEncounterRole(2), providerService.getProvider(22));
        systemConsultationEncounter.setPatient(patient);
        systemConsultationEncounter.setEncounterDatetime(encounterDate);
        return systemConsultationEncounter;
    }

    private Visit createVisitForDate(Patient patient, Encounter encounter, Date orderDate, boolean isActive, Date stopDatetime) {
        VisitType regularVisitType = visitService.getVisitType(4);
        Visit visit = new Visit(patient, regularVisitType, orderDate);
        if(encounter != null)
            visit.addEncounter(encounter);
        if (!isActive)
            visit.setStopDatetime(stopDatetime);
        return visitService.saveVisit(visit);
    }

    private Visit createActiveVisit(Patient patient) {
        final Date orderDate = new Date();
        return createVisitForDate(patient, null, orderDate, true, DateUtils.addDays(orderDate, 1));
    }

    private void assertDrugOrder(Collection<Order> orders, String drugName, Date orderDate, Double dosage, int numberOfDays) {
        for (Order order : orders) {
            DrugOrder drugOrder = (DrugOrder) order;
            if (drugOrder.getDrug().getName().equals(drugName)) {
                //TODO: We need to play the story that populates dosageInstructions. Once done, revisit these assertions
//                assertEquals(dosage, drugOrder.getDose());
//                assertEquals(orderDate.getTime(), drugOrder.getStartDate().getTime());
//                assertEquals(orderDate, drugOrder.getStartDate());
//                assertEquals(DateUtils.addDays(orderDate, numberOfDays), drugOrder.getAutoExpireDate());
                return;
            }
        }
        fail("No Drug Order found for drug name : " + drugName);
    }
}
