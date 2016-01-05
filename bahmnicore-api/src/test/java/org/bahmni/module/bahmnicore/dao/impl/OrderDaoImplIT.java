package org.bahmni.module.bahmnicore.dao.impl;

import org.bahmni.module.bahmnicore.BaseIntegrationTest;
import org.bahmni.module.bahmnicore.dao.ApplicationDataDirectory;
import org.bahmni.module.bahmnicore.service.OrderService;
import org.bahmni.module.bahmnicore.util.BahmniDateUtil;
import org.junit.Test;
import org.openmrs.*;
import org.openmrs.api.ConceptService;
import org.openmrs.api.PatientService;
import org.openmrs.api.context.Context;
import org.openmrs.module.emrapi.encounter.domain.EncounterTransaction;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.File;
import java.net.URISyntaxException;
import java.util.*;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class OrderDaoImplIT extends BaseIntegrationTest {

    @Autowired
    private OrderDaoImpl orderDao;
    @Autowired
    private ConceptService conceptService;
    @Autowired
    private OrderService orderService;
    @Autowired
    private PatientService patientService;


    @Test
    public void getPrescribedDrugOrders_ShouldNotGetDiscontinueOrders() throws Exception {
        executeDataSet("patientWithDiscontinuedOrders.xml");
        Patient patient = Context.getPatientService().getPatient(1001);

        List<DrugOrder> drugOrdersInLastVisit = orderDao.getPrescribedDrugOrders(patient, true, null, null, null, false);

        assertThat(drugOrdersInLastVisit.size(), is(3));
        assertThat(getOrderIds(drugOrdersInLastVisit), hasItems(15, 16, 18));
    }

    @Test
    public void getPrescribedDrugOrders_ShouldGetRevisedOrdersAloneIfRevisionIsInSameEncounter() throws Exception {
        executeDataSet("patientWithOrderRevisedInSameEncounter.xml");
        Patient patient = Context.getPatientService().getPatient(1001);

        List<DrugOrder> drugOrdersInLastVisit = orderDao.getPrescribedDrugOrders(patient, true, null, null, null, false);

        assertThat(drugOrdersInLastVisit.size(), is(1));
        assertThat(getOrderIds(drugOrdersInLastVisit), hasItems(16));
    }

    @Test
    public void getPrescribedDrugOrders_ShouldGetBothRevisedOrdersAndPreviousOrderIfRevisionIsInDifferentEncounter() throws Exception {
        executeDataSet("patientWithOrderRevisedInDifferentEncounter.xml");
        Patient patient = Context.getPatientService().getPatient(1001);

        List<DrugOrder> drugOrdersInLastVisit = orderDao.getPrescribedDrugOrders(patient, true, null, null, null, false);

        assertThat(drugOrdersInLastVisit.size(), is(2));
        assertThat(getOrderIds(drugOrdersInLastVisit), hasItems(15, 16));
    }

    @Test
    public void getPrescribedDrugOrders_ShouldFetchAllPrescribedDrugOrdersInPastVisits() throws Exception {
        executeDataSet("patientWithOrders.xml");
        Patient patient = Context.getPatientService().getPatient(1001);

        List<DrugOrder> drugOrdersInLastVisit = orderDao.getPrescribedDrugOrders(patient, false, 1, null, null, false);
        assertThat(drugOrdersInLastVisit.size(), is(equalTo(1)));
        assertThat(getOrderIds(drugOrdersInLastVisit), hasItems(17));

        List<DrugOrder> drugOrdersInLastTwoVisit = orderDao.getPrescribedDrugOrders(patient, false, 2, null, null, false);
        assertThat(drugOrdersInLastTwoVisit.size(), is(equalTo(3)));
        assertThat(getOrderIds(drugOrdersInLastTwoVisit), hasItems(15, 16, 17));

        List<DrugOrder> drugOrders = orderDao.getPrescribedDrugOrders(patient, false, null, null, null, false);
        assertThat(drugOrders.size(), is(equalTo(3)));
        assertThat(getOrderIds(drugOrders), hasItems(15, 16, 17));
    }

    @Test
    public void getPrescribedDrugOrders_shouldFetchAllPrescribedDrugOrdersIncludingActiveVisit() throws Exception {
        executeDataSet("patientWithOrders.xml");
        Patient patient = Context.getPatientService().getPatient(1001);

        List<DrugOrder> drugOrders = orderDao.getPrescribedDrugOrders(patient, true, null, null, null, false);
        assertThat(drugOrders.size(), is(equalTo(7)));
        assertThat(getOrderIds(drugOrders), hasItems(15, 16, 17, 19, 21, 23, 24));


        drugOrders = orderDao.getPrescribedDrugOrders(patient, null, null, null, null, false);
        assertThat(drugOrders.size(), is(equalTo(3)));
        assertThat(getOrderIds(drugOrders), hasItems(15, 16, 17));
    }

    @Test
    public void getPrescribedDrugOrders_ShouldFetchAllPrescribedDrugOrdersWithInGivenDateRange() throws Exception{
        executeDataSet("patientWithOrders.xml");
        Patient patient = Context.getPatientService().getPatient(1001);

        Date startDate = BahmniDateUtil.convertToDate("2013-01-01T00:00:00.000", BahmniDateUtil.DateFormatType.UTC);
        Date endDate = BahmniDateUtil.convertToDate("2013-09-09T00:00:00.000", BahmniDateUtil.DateFormatType.UTC);

        List<DrugOrder> drugOrders = orderDao.getPrescribedDrugOrders(patient, true, null, startDate, null, false);
        assertThat(drugOrders.size(), is(equalTo(6)));
        assertThat(getOrderIds(drugOrders), hasItems(16, 17, 19, 21, 23, 24));

        drugOrders = orderDao.getPrescribedDrugOrders(patient, true, null, startDate, endDate, false);
        assertThat(drugOrders.size(), is(equalTo(2)));
        assertThat(getOrderIds(drugOrders), hasItems(16, 17));

        drugOrders = orderDao.getPrescribedDrugOrders(patient, true, null, null, endDate, false);
        assertThat(drugOrders.size(), is(equalTo(3)));
        assertThat(getOrderIds(drugOrders), hasItems(15, 16, 17));

    }

    @Test
    public void getVisitsWithOrders_ShouldFetchVisitsWithGivenOrderType() throws Exception {
        executeDataSet("patientWithOrders.xml");
        Patient patient = Context.getPatientService().getPatient(1001);

        List<Visit> visits = orderDao.getVisitsWithActiveOrders(patient, "Order", true, 1);

        assertThat(visits.size(), is(equalTo(1)));
        assertThat(visits.get(0).getId(), is(equalTo(5)));
    }

    @Test
    public void getPrescribedDrugOrdersForConcepts_shouldFetchAllPrescribedDrugOrdersForGivenConceptsForGivenNoOfVisits() throws Exception {
        executeDataSet("patientWithOrders.xml");
        Patient patient = patientService.getPatient(2);

        List<Concept> concepts = new ArrayList<>();
        concepts.add(conceptService.getConcept(3));
        concepts.add(conceptService.getConcept(25));

        List<Visit> visits = orderService.getVisitsWithOrders(patient, "DrugOrder", true, 1);
        assertEquals(1, visits.size());

        List<DrugOrder> result = orderDao.getPrescribedDrugOrdersForConcepts(patient, true, visits, concepts, null, null);
        assertEquals(2, result.size());
        assertThat(getOrderIds(result), hasItems(55, 57));

    }

    @Test
    public void shouldFetchAllPrescribedDrugOrdersForGivenConceptsForGivenNoOfVisitsWithinGivenDateRange() throws Exception {
        executeDataSet("patientWithOrders.xml");
        Date startDate = BahmniDateUtil.convertToDate("2013-08-07T00:00:00.000", BahmniDateUtil.DateFormatType.UTC);
        Date endDate = BahmniDateUtil.convertToDate("2013-08-09T00:00:00.000", BahmniDateUtil.DateFormatType.UTC);

        Patient patient = patientService.getPatient(2);

        List<Concept> concepts = new ArrayList<>();
        concepts.add(conceptService.getConcept(25));
        concepts.add(conceptService.getConcept(26));

        List<Visit> visits = orderService.getVisitsWithOrders(patient, "DrugOrder", true, 1);
        assertEquals(1, visits.size());

        List<DrugOrder> result = orderDao.getPrescribedDrugOrdersForConcepts(patient, true, visits, concepts, startDate, endDate);
        assertEquals(1, result.size());
        assertThat(getOrderIds(result), hasItems(57));

    }

    @Test
    public void shouldRetrieveAllVisitsRequested() throws Exception {
        executeDataSet("patientWithOrders.xml");
        String visitUuid1 = "1e5d5d48-6b78-11e0-93c3-18a97ba044dc";
        String visitUuid2 = "1e5d5d48-6b78-11e0-93c3-18a97b8ca4dc";
        String[] visitUuids = {visitUuid1, visitUuid2};

        List<Visit> visits = orderDao.getVisitsForUUids(visitUuids);

        assertThat(visits.size(), is(equalTo(visitUuids.length)));
        assertTrue(visitWithUuidExists(visitUuid1, visits));
        assertTrue(visitWithUuidExists(visitUuid2, visits));
    }

    @Test
    public void shouldGetDrugOrdersByVisitUuid() throws Exception {
        executeDataSet("patientWithOrders.xml");
        String visitUuid1 = "1e5d5d48-6b78-11e0-93c3-18a97ba044dc";
        String visitUuid2 = "1e5d5d48-6b78-11e0-93c3-18a97b8ca4dc";

        List<DrugOrder> prescribedDrugOrders = orderDao.getPrescribedDrugOrders(Arrays.asList(visitUuid1, visitUuid2));

        assertEquals(6, prescribedDrugOrders.size());
    }

    @Test
    public void getDrugOrderForRegimen_shouldRetrieveDrugOrdersAssignedToTheRegimen() throws Exception {
        ApplicationDataDirectory applicationDataDirectory = mock(ApplicationDataDirectory.class);
        when(applicationDataDirectory.getFile("ordertemplates/templates.json"))
                .thenReturn(new File(this.getClass().getClassLoader().getResource("templates.json").toURI()));
        orderDao.setApplicationDataDirectory(applicationDataDirectory);


        Collection<EncounterTransaction.DrugOrder> drugOrdersForCancerRegimen = orderDao.getDrugOrderForRegimen("Cancer Regimen, CAF");
        Collection<EncounterTransaction.DrugOrder> drugOrdersForBreastCancer = orderDao.getDrugOrderForRegimen("Breast Cancer - AC");

        assertEquals(1, drugOrdersForCancerRegimen.size());
        EncounterTransaction.DrugOrder drugOrder = drugOrdersForCancerRegimen.iterator().next();
        assertThat(drugOrder.getDrug().getName(), is(equalTo("DNS")));
        assertEquals(10, drugOrdersForBreastCancer.size());

    }

    @Test(expected = NullPointerException.class)
    public void getDrugOrderForRegimen_shouldFailWhenFileDoesNotExist() {
        ApplicationDataDirectory applicationDataDirectory = mock(ApplicationDataDirectory.class);
        when(applicationDataDirectory.getFile("ordertemplates/templates.json")).thenThrow(NullPointerException.class);
        orderDao.setApplicationDataDirectory(applicationDataDirectory);

        orderDao.getDrugOrderForRegimen("Breast Cancer - AC");
    }

    @Test
    public void getDrugOrderForRegimen_shouldReturnEmptyListWhenRegimenNotFound() throws URISyntaxException {
        ApplicationDataDirectory applicationDataDirectory = mock(ApplicationDataDirectory.class);
        when(applicationDataDirectory.getFile("ordertemplates/templates.json"))
                .thenReturn(new File(this.getClass().getClassLoader().getResource("templates.json").toURI()));
        orderDao.setApplicationDataDirectory(applicationDataDirectory);

        Collection<EncounterTransaction.DrugOrder> drugOrders = orderDao.getDrugOrderForRegimen("Non existing regimen");
        assertThat(drugOrders.size(), is(equalTo(0)));

    }

    @Test
    public void getAllOrdersForVisits_shouldReturnEmptyListWhenNoVisitsFound() {
        assertThat(orderDao.getAllOrdersForVisits(null, null).size(), is(equalTo(0)));
        assertThat(orderDao.getAllOrdersForVisits(null, new ArrayList<Visit>()).size(), is(equalTo(0)));
    }

    @Test
    public void getAllOrdersForVisits_shouldReturnAllOrdersGivenAVisitAndAPatient() throws Exception {
        executeDataSet("patientWithOrders.xml");
        Visit visit = Context.getVisitService().getVisit(1);
        Patient patient = null;
        OrderType orderType = Context.getOrderService().getOrderType(15);

        List<Order> allOrdersForVisits = orderDao.getAllOrdersForVisits(orderType, Arrays.asList(visit));

        assertThat(allOrdersForVisits.size(), is(equalTo(2)));

        Order firstOrder = Context.getOrderService().getOrder(15);
        Order secondOrder = Context.getOrderService().getOrder(16);
        assertThat(allOrdersForVisits, hasItems(firstOrder, secondOrder));
    }

    @Test
    public void getActiveDrugOrdersForPatient() throws Exception {
        executeDataSet("patientWithOrders.xml");
        Patient patient = Context.getPatientService().getPatient(1001);
        OrderType orderType = Context.getOrderService().getOrderType(1);
        List<Order> activeOrders = orderDao.getActiveOrders(patient, orderType, null, new Date(), null, null);

        assertEquals(activeOrders.size(), 2);
        assertEquals(activeOrders.get(0).getUuid(), "cba00378-0c03-11e4-bb80-f18addb6f836");
        assertEquals(activeOrders.get(1).getUuid(), "cba00378-0c03-11e4-bb80-f18addb6f838");
    }
    @Test
    public void getActiveDrugOrdersForPatientFilteredByDrugConcepts() throws Exception {
        executeDataSet("patientWithOrders.xml");
        Patient patient = Context.getPatientService().getPatient(1001);
        OrderType orderType = Context.getOrderService().getOrderType(1);
        Concept concept = Context.getConceptService().getConcept(16);
        HashSet<Concept> concepts = new HashSet<Concept>();
        concepts.add(concept);

        List<Order> activeOrders = orderDao.getActiveOrders(patient, orderType, null, new Date(), concepts, null);

        assertEquals(activeOrders.size(), 1);
        assertEquals(activeOrders.get(0).getUuid(), "cba00378-0c03-11e4-bb80-f18addb6f836");
    }

    @Test
    public void getInactiveDrugOrdersForPatient() throws Exception {
        executeDataSet("patientWithOrders.xml");
        Patient patient = Context.getPatientService().getPatient(1001);
        OrderType orderType = Context.getOrderService().getOrderType(1);
        List<Order> activeOrders = orderDao.getInactiveOrders(patient, orderType, null, new Date(), null, null);

        assertEquals(activeOrders.size(), 2);
        assertEquals(activeOrders.get(0).getUuid(), "cba00378-0c03-11e4-bb80-f18addb6f837");
        assertEquals(activeOrders.get(1).getUuid(), "cba00378-0c03-11e4-bb80-f18addb6f839");
    }

    @Test
    public void getInactiveDrugOrdersForPatientFilteredByDrugConcepts() throws Exception {
        executeDataSet("patientWithOrders.xml");
        Patient patient = Context.getPatientService().getPatient(1001);
        OrderType orderType = Context.getOrderService().getOrderType(1);
        Concept concept = Context.getConceptService().getConcept(16);
        HashSet<Concept> concepts = new HashSet<Concept>();
        concepts.add(concept);

        List<Order> activeOrders = orderDao.getInactiveOrders(patient, orderType, null, new Date(), concepts, null);

        assertEquals(activeOrders.size(), 1);
        assertEquals(activeOrders.get(0).getUuid(), "cba00378-0c03-11e4-bb80-f18addb6f839");
    }

    private boolean visitWithUuidExists(String uuid, List<Visit> visits) {
        boolean exists = false;
        for (Visit visit : visits) {
            exists |= visit.getUuid().equals(uuid);
        }
        return exists;
    }

    private List<Integer> getOrderIds(List<DrugOrder> drugOrders) {
        ArrayList<Integer> ids = new ArrayList<>();
        for (DrugOrder drugOrder : drugOrders) {
            ids.add(drugOrder.getOrderId());
        }
        return ids;
    }
}
