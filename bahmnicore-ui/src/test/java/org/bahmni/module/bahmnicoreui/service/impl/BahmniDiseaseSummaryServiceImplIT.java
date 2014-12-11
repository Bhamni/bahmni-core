package org.bahmni.module.bahmnicoreui.service.impl;

import org.bahmni.module.bahmnicore.dao.OrderDao;
import org.bahmni.module.bahmnicore.service.BahmniDrugOrderService;
import org.bahmni.module.bahmnicore.service.BahmniObsService;
import org.bahmni.module.bahmnicoreui.contract.ConceptValue;
import org.bahmni.module.bahmnicoreui.contract.DiseaseDataParams;
import org.bahmni.module.bahmnicoreui.contract.DiseaseSummaryData;
import org.junit.Test;
import org.openmrs.api.ConceptService;
import org.openmrs.api.PatientService;
import org.openmrs.module.bahmniemrapi.laborder.service.LabOrderResultsService;
import org.openmrs.test.BaseModuleContextSensitiveTest;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.Map;
import java.util.Set;

import static org.junit.Assert.*;

@org.springframework.test.context.ContextConfiguration(locations = {"classpath:TestingApplicationContext.xml"}, inheritLocations = true)
public class BahmniDiseaseSummaryServiceImplIT extends BaseModuleContextSensitiveTest {

    @Autowired
    private ConceptService conceptService;

    @Autowired
    private BahmniObsService bahmniObsService;

    private BahmniDiseaseSummaryServiceImpl bahmniDiseaseSummaryData;
    @Autowired
    private PatientService patientService;
    @Autowired
    private LabOrderResultsService labOrderResultsService;
    @Autowired
    private BahmniDrugOrderService drugOrderService;
    @Autowired
    private OrderDao orderDao;

    @org.junit.Before
    public void setUp() throws Exception {
        bahmniDiseaseSummaryData = new BahmniDiseaseSummaryServiceImpl(patientService, bahmniObsService, labOrderResultsService, conceptService, drugOrderService, orderDao);
        executeDataSet("diagnosisMetadata.xml");
        executeDataSet("dispositionMetadata.xml");
    }

    @Test
    public void shouldReturnObsForGivenConceptsAndNoOfVisits() throws Exception {
        executeDataSet("observationsTestData.xml");

        DiseaseDataParams diseaseDataParams = new DiseaseDataParams();
        diseaseDataParams.setNumberOfVisits(1);
        ArrayList<String> obsConcepts = new ArrayList<String>(){{
            add("Blood Pressure");
            add("Weight");
        }};

        diseaseDataParams.setObsConcepts(obsConcepts);
        DiseaseSummaryData diseaseSummary = bahmniDiseaseSummaryData.getDiseaseSummary("86526ed5-3c11-11de-a0ba-001e378eb67a", diseaseDataParams);
        Map<String, Map<String, ConceptValue>> obsTable = diseaseSummary.getTabularData();

        assertNotNull(obsTable);
        assertEquals(1, obsTable.size());

        Map<String, ConceptValue> obsInVisit = obsTable.get("2008-09-18");
        assertEquals(1, obsInVisit.size());
        assertEquals("110.0", obsInVisit.get("Weight").getValue());

    }

    @Test
    public void shouldReturnObsForGivenConceptsForAllVisitsWhenNoOfVisitsNotSpecifed() throws Exception {
        executeDataSet("observationsTestData.xml");

        DiseaseDataParams diseaseDataParams = new DiseaseDataParams();
        ArrayList<String> obsConcepts = new ArrayList<String>(){{
            add("Blood Pressure");
            add("Weight");
        }};

        diseaseDataParams.setObsConcepts(obsConcepts);
        DiseaseSummaryData diseaseSummary = bahmniDiseaseSummaryData.getDiseaseSummary("86526ed5-3c11-11de-a0ba-001e378eb67a", diseaseDataParams);
        Map<String, Map<String, ConceptValue>> obsTable = diseaseSummary.getTabularData();

        assertNotNull(obsTable);
        assertEquals(2, obsTable.size());

        Map<String, ConceptValue> obsForVisit = obsTable.get("2008-09-18");
        assertEquals(1, obsForVisit.size());
        assertEquals("110.0", obsForVisit.get("Weight").getValue());

        obsForVisit = obsTable.get("2008-08-18");
        assertEquals(2, obsForVisit.size());
        assertEquals("120.0", obsForVisit.get("Systolic").getValue());
        assertTrue(obsForVisit.get("Systolic").getAbnormal());
        assertEquals("40.0", obsForVisit.get("Diastolic").getValue());
        assertTrue(obsForVisit.get("Diastolic").getAbnormal());
    }

    @Test
    public void shouldReturnLabResultsForGivenConceptsAndNoOfVisits() throws Exception {
        executeDataSet("labOrderTestData.xml");

        DiseaseDataParams diseaseDataParams = new DiseaseDataParams();
        diseaseDataParams.setNumberOfVisits(1);
        ArrayList<String> labConcepts = new ArrayList<String>(){{
            add("Blood Panel");
        }};

        diseaseDataParams.setLabConcepts(labConcepts);
        DiseaseSummaryData diseaseSummary = bahmniDiseaseSummaryData.getDiseaseSummary("75e04d42-3ca8-11e3-bf2b-0800271c1b75", diseaseDataParams);
        Map<String, Map<String, ConceptValue>> labTable = diseaseSummary.getTabularData();

        assertNotNull(labTable);
        assertEquals(1, labTable.size());

        Map<String, ConceptValue> labResultsInVisit = labTable.get("2005-09-26");
        assertNotNull(labResultsInVisit);
        assertEquals(2, labResultsInVisit.size());
        assertEquals("99.0", labResultsInVisit.get("Haemoglobin").getValue());
        assertEquals("10.0", labResultsInVisit.get("ESR").getValue());
    }

    @Test
    public void shouldReturnLabResultsForGivenConceptsForAllVisits() throws Exception {
        executeDataSet("labOrderTestData.xml");

        DiseaseDataParams diseaseDataParams = new DiseaseDataParams();
        ArrayList<String> labConcepts = new ArrayList<String>(){{
            add("PS for Malaria");
        }};

        diseaseDataParams.setLabConcepts(labConcepts);
        DiseaseSummaryData diseaseSummary = bahmniDiseaseSummaryData.getDiseaseSummary("75e04d42-3ca8-11e3-bf2b-0800271c1b75", diseaseDataParams);
        Map<String, Map<String, ConceptValue>> labTable = diseaseSummary.getTabularData();

        assertNotNull(labTable);
        assertEquals(2, labTable.size());

        Map<String, ConceptValue> labResultsInVisit = labTable.get("2013-09-26");
        assertNotNull(labResultsInVisit);
        assertEquals(1, labResultsInVisit.size());
        assertEquals("Result for PS Malaria", labResultsInVisit.get("PS for Malaria").getValue());

        labResultsInVisit = labTable.get("2005-09-26");
        assertNotNull(labResultsInVisit);
        assertEquals(1, labResultsInVisit.size());
        assertEquals("almost dead of PS Malaria", labResultsInVisit.get("PS for Malaria").getValue());
    }

    @Test
    public void shouldReturnDrugOrdersForGivenConceptsAndNoOfVisits() throws Exception {
        executeDataSet("drugOrderTestData.xml");

        DiseaseDataParams diseaseDataParams = new DiseaseDataParams();
        diseaseDataParams.setNumberOfVisits(1);
        ArrayList<String> drugConcepts = new ArrayList<String>(){{
            add("Calpol 250mg");
        }};

        diseaseDataParams.setDrugConcepts(drugConcepts);
        DiseaseSummaryData diseaseSummary = bahmniDiseaseSummaryData.getDiseaseSummary("75e04d42-3ca8-11e3-bf2b-080027175c1b", diseaseDataParams);
        Map<String, Map<String, ConceptValue>> drugTable = diseaseSummary.getTabularData();

        assertNotNull(drugTable);
        assertEquals(1, drugTable.size());

        Map<String, ConceptValue> durgOrdersInVisit = drugTable.get("2012-12-12");
        assertNotNull(durgOrdersInVisit);
        assertEquals(1, durgOrdersInVisit.size());
        assertEquals("250mg", durgOrdersInVisit.get("Calpol 250mg").getValue());
    }


    @Test
    public void shouldReturnDrugOrdersForGivenConceptsForAllVisits() throws Exception {

    }


    @Test
    public void shouldReturnLeafConceptsNames() throws Exception {
        executeDataSet("observationsTestData.xml");
        DiseaseDataParams diseaseDataParams = new DiseaseDataParams();
        diseaseDataParams.setNumberOfVisits(3);
        ArrayList<String> obsConcepts = new ArrayList<String>(){{
            add("Blood Pressure");
            add("Weight");
        }};
        diseaseDataParams.setObsConcepts(obsConcepts);
        DiseaseSummaryData diseaseSummary = bahmniDiseaseSummaryData.getDiseaseSummary("86526ed5-3c11-11de-a0ba-001e378eb67a", diseaseDataParams);
        Set<String> conceptNames = diseaseSummary.getConceptNames();


        assertNotNull(conceptNames);
        assertEquals(3, conceptNames.size());
        assertTrue(conceptNames.contains("Weight"));
        assertTrue(conceptNames.contains("Systolic"));
        assertTrue(conceptNames.contains("Diastolic"));

    }
}