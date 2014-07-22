package org.bahmni.module.bahmnicore.service.impl;

import org.apache.commons.lang3.time.DateUtils;
import org.bahmni.module.bahmnicore.model.BahmniDrugOrder;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.*;
import org.openmrs.api.EncounterService;
import org.openmrs.api.PatientService;
import org.openmrs.api.ProviderService;
import org.openmrs.api.VisitService;
import org.openmrs.web.test.BaseModuleWebContextSensitiveTest;
import org.springframework.beans.factory.annotation.Autowired;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import static org.junit.Assert.*;

@org.springframework.test.context.ContextConfiguration(locations = {"classpath:TestingApplicationContext.xml"}, inheritLocations = true)
public class BahmniDrugOrderServiceImplIT extends BaseModuleWebContextSensitiveTest {

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

    @Before
    public void setUp() throws Exception {
        executeDataSet("drugOrdersTestData.xml");
    }

    @Test
    public void shouldCreateNewEncounterAndAddDrugOrdersWhenActiveVisitExists() {
        Patient patient = patientService.getPatient(1);
        Visit activeVisit = createActiveVisit(patient);
        assertNull(activeVisit.getEncounters());
        Date orderDate = new Date();
        BahmniDrugOrder calpol = new BahmniDrugOrder("3e4933ff-7799-11e3-a96a-0800271c1b75", 2.0, 20.0, "mg", orderDate);
        BahmniDrugOrder cetrizine = new BahmniDrugOrder("f5bf0aa6-7855-11e3-bd53-328f386b70f0", 3.0, 21.0, "mg", orderDate);
        BahmniDrugOrder cetzine = new BahmniDrugOrder("f5bf0aa6-7855-11e3-bd53-328f386b70fa", 0.0, 10.0, "mg", orderDate);
        List<BahmniDrugOrder> drugOrders = Arrays.asList(calpol, cetrizine, cetzine);

        bahmniDrugOrderService.add("GAN200000", orderDate, drugOrders, "System");

        Visit visit = visitService.getVisit(activeVisit.getId());
        Encounter encounter = (Encounter) visit.getEncounters().toArray()[0];
        EncounterProvider encounterProvider = (EncounterProvider) encounter.getEncounterProviders().toArray()[0];
        assertEquals("System", encounterProvider.getProvider().getName());
        assertEquals("Unknown", encounterProvider.getEncounterRole().getName());
        assertEquals("OPD", encounter.getEncounterType().getName());
        assertEquals(3, encounter.getOrders().size());

        assertDrugOrder(encounter.getOrders(), "Calpol", orderDate, calpol.getDosage(), calpol.getNumberOfDays());
        assertDrugOrder(encounter.getOrders(), "Cetirizine", orderDate, cetrizine.getDosage(), cetrizine.getNumberOfDays());
        assertDrugOrder(encounter.getOrders(), "Cetzine", orderDate, cetzine.getDosage(), cetzine.getNumberOfDays());
    }

    @Test
    public void shouldCreateNewEncounterAndAddOrdersToVisitOnOrderDateWhenActiveVisitDoesNotExist() throws ParseException {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd-MM-yyyy");
        Date orderDate = simpleDateFormat.parse("01-01-2014");

        BahmniDrugOrder calpol = new BahmniDrugOrder("3e4933ff-7799-11e3-a96a-0800271c1b75", 2.0, 20.0, "mg", orderDate);
        BahmniDrugOrder cetrizine = new BahmniDrugOrder("f5bf0aa6-7855-11e3-bd53-328f386b70f0", 3.0, 21.0, "mg", orderDate);
        BahmniDrugOrder cetzine = new BahmniDrugOrder("f5bf0aa6-7855-11e3-bd53-328f386b70fa", 0.0, 10.0, "mg", orderDate);
        List<BahmniDrugOrder> drugOrders = Arrays.asList(calpol, cetrizine, cetzine);

        Patient patient = patientService.getPatient(1);
        Visit visit = createVisitForDate(patient, null, orderDate, false, DateUtils.addDays(orderDate, 1));
        assertNull(visit.getEncounters());

        bahmniDrugOrderService.add("GAN200000", orderDate, drugOrders, "System");

        Visit savedVisit = visitService.getVisit(visit.getId());
        Encounter encounter = (Encounter) savedVisit.getEncounters().toArray()[0];
        EncounterProvider encounterProvider = (EncounterProvider) encounter.getEncounterProviders().toArray()[0];
        assertEquals("System", encounterProvider.getProvider().getName());
        assertEquals("Unknown", encounterProvider.getEncounterRole().getName());
        assertEquals("OPD", encounter.getEncounterType().getName());
        assertEquals(3, encounter.getOrders().size());

        assertDrugOrder(encounter.getOrders(), "Calpol", orderDate, calpol.getDosage(), calpol.getNumberOfDays());
        assertDrugOrder(encounter.getOrders(), "Cetirizine", orderDate, cetrizine.getDosage(), cetrizine.getNumberOfDays());
        assertDrugOrder(encounter.getOrders(), "Cetzine", orderDate, cetzine.getDosage(), cetzine.getNumberOfDays());
    }

    @Test
    public void shouldCreateNewEncounterAndAddOrdersAndChangeVisitEndDate_ToVisitAtTheDateClosestToOrderDate_WhenActiveVisitDoesNotExist() throws ParseException {
        Date orderDate = new SimpleDateFormat("dd-MM-yyyy").parse("01-01-2014");

        BahmniDrugOrder calpol = new BahmniDrugOrder("3e4933ff-7799-11e3-a96a-0800271c1b75", 2.0, 20.0, "mg", orderDate);
        BahmniDrugOrder cetrizine = new BahmniDrugOrder("f5bf0aa6-7855-11e3-bd53-328f386b70f0", 3.0, 21.0, "mg", orderDate);
        BahmniDrugOrder cetzine = new BahmniDrugOrder("f5bf0aa6-7855-11e3-bd53-328f386b70fa", 0.0, 10.0, "mg", orderDate);
        List<BahmniDrugOrder> drugOrders = Arrays.asList(calpol, cetrizine, cetzine);

        Patient patient = patientService.getPatient(1);
        Visit visit1 = createVisitForDate(patient, null, DateUtils.addDays(orderDate, -5), false, DateUtils.addDays(DateUtils.addDays(orderDate, -5), 1));
        Visit visit2 = createVisitForDate(patient, null, DateUtils.addDays(orderDate, -3), false, DateUtils.addDays(DateUtils.addDays(orderDate, -3), 1));
        assertNull(visit2.getEncounters());

        bahmniDrugOrderService.add("GAN200000", orderDate, drugOrders, "System");

        Visit savedVisit = visitService.getVisitsByPatient(patient).get(0);

        assertEquals(new SimpleDateFormat("dd-MM-yyyy hh:mm:ss").parse("01-01-2014 23:59:59"), savedVisit.getStopDatetime());

        Encounter encounter = (Encounter) savedVisit.getEncounters().toArray()[0];
        EncounterProvider encounterProvider = (EncounterProvider) encounter.getEncounterProviders().toArray()[0];
        assertEquals("System", encounterProvider.getProvider().getName());
        assertEquals("Unknown", encounterProvider.getEncounterRole().getName());
        assertEquals("OPD", encounter.getEncounterType().getName());
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

        BahmniDrugOrder calpol = new BahmniDrugOrder("3e4933ff-7799-11e3-a96a-0800271c1b75", 2.0, 20.0, "mg", orderDate);
        BahmniDrugOrder cetrizine = new BahmniDrugOrder("f5bf0aa6-7855-11e3-bd53-328f386b70f0", 3.0, 21.0, "mg", orderDate);
        BahmniDrugOrder cetzine = new BahmniDrugOrder("f5bf0aa6-7855-11e3-bd53-328f386b70fa", 0.0, 10.0, "mg", orderDate);
        List<BahmniDrugOrder> drugOrders = Arrays.asList(calpol, cetrizine, cetzine);

        Encounter systemConsultationEncounter = createSystemConsultationEncounter(patient, visitStartDate);
        Visit visit = createVisitForDate(patient, systemConsultationEncounter, visitStartDate, false, DateUtils.addDays(visitStartDate, 1));
        assertEquals(1, visit.getEncounters().size());

        bahmniDrugOrderService.add("GAN200000", orderDate, drugOrders, "System");

        Visit savedVisit = visitService.getVisit(visit.getId());
        assertEquals(2, savedVisit.getEncounters().size());
        Set<Encounter> encounters = savedVisit.getEncounters();
        Set<Order> orders = new HashSet<>();
        for (Encounter encounter : encounters) {
            orders.addAll(encounter.getOrders());
        }

        assertEquals(3, orders.size());
        assertDrugOrder(orders, "Calpol", orderDate, calpol.getDosage(), calpol.getNumberOfDays());
        assertDrugOrder(orders, "Cetirizine", orderDate, cetrizine.getDosage(), cetrizine.getNumberOfDays());
        assertDrugOrder(orders, "Cetzine", orderDate, cetzine.getDosage(), cetzine.getNumberOfDays());
    }

    private Encounter createSystemConsultationEncounter(Patient patient, Date encounterDate) {
        Encounter systemConsultationEncounter = new Encounter();
        systemConsultationEncounter.setEncounterType(encounterService.getEncounterType("OPD"));
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

    private void assertDrugOrder(Set<Order> orders, String drugName, Date orderDate, Double dosage, int numberOfDays) {
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
