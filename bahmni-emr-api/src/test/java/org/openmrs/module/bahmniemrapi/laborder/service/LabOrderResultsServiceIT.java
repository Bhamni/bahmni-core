package org.openmrs.module.bahmniemrapi.laborder.service;

import org.junit.Ignore;
import org.junit.Test;
import org.openmrs.Concept;
import org.openmrs.Encounter;
import org.openmrs.Patient;
import org.openmrs.Visit;
import org.openmrs.api.ConceptService;
import org.openmrs.api.context.Context;
import org.openmrs.module.bahmniemrapi.laborder.contract.LabOrderResult;
import org.openmrs.module.bahmniemrapi.laborder.contract.LabOrderResults;
import org.openmrs.test.BaseModuleContextSensitiveTest;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

@org.springframework.test.context.ContextConfiguration(locations = {"classpath:TestingApplicationContext.xml"}, inheritLocations = true)
public class LabOrderResultsServiceIT extends BaseModuleContextSensitiveTest {
    
    @Autowired
    private LabOrderResultsService labOrderResultsService;

    @Test
    public void shouldMapTestOrdersAndResultsForAllVisits() throws Exception {
        executeDataSet("diagnosisMetadata.xml");
        executeDataSet("dispositionMetadata.xml");
        executeDataSet("labOrderTestData.xml");
        Patient patient = Context.getPatientService().getPatient(1);

        LabOrderResults results = labOrderResultsService.getAll(patient, null);
        List<LabOrderResult> labOrderResults = results.getResults();

        assertNotNull(labOrderResults);
        assertEquals(6, labOrderResults.size());

        assertOrderPresent(labOrderResults, "Haemoglobin", "Blood Panel", 16, "System OpenMRS", "99.0", 200.0, 300.0, true, null, true, null);
        assertOrderPresent(labOrderResults, "ESR", "Blood Panel", 16, "System OpenMRS", "10.0", null, null, false, "Some Notes", false, null);
        assertOrderPresent(labOrderResults, "Urea Nitrogen", null, 16, "System OpenMRS", "20.0", null, null, null, null, false, "8834dedb-dc15-4afe-a491-ea3ca4150bce_sample.jpeg");
        assertOrderPresent(labOrderResults, "HIV ELISA", null, 16, null, null, null, null, null, null, false, null);
        assertOrderPresent(labOrderResults, "PS for Malaria", null, 16, "System OpenMRS", null, null, null, null, null, true, null);
        assertOrderPresent(labOrderResults, "PS for Malaria", null, 17, "System OpenMRS", "Result for PS Malaria", null, null, null, null, false, null);
    }

    @Test
    public void shouldMapTestOrdersAndResultsForGivenVisit() throws Exception {
        executeDataSet("diagnosisMetadata.xml");
        executeDataSet("dispositionMetadata.xml");
        executeDataSet("labOrderTestData.xml");
        Patient patient = Context.getPatientService().getPatient(1);
        Visit visit = Context.getVisitService().getVisit(4);

        LabOrderResults results = labOrderResultsService.getAll(patient, Arrays.asList(visit));
        List<LabOrderResult> labOrderResults = results.getResults();

        assertNotNull(labOrderResults);
        assertEquals(1, labOrderResults.size());

        assertOrderPresent(labOrderResults, "PS for Malaria", null, 17, "System OpenMRS", "Result for PS Malaria", null, null, null, null, false, null);
    }

    @Test
    public void shouldGetLabOrdersForParticularConcepts() throws Exception{
        executeDataSet("diagnosisMetadata.xml");
        executeDataSet("dispositionMetadata.xml");
        executeDataSet("labOrderTestData.xml");

        Patient patient = Context.getPatientService().getPatient(1);

        Collection<String> concepts = new ArrayList<>();
        concepts.add("Blood Panel");

        List<LabOrderResult> results = labOrderResultsService.getAllForConcepts(patient, concepts, null);

        assertEquals(results.size(),2);
        assertOrderPresent(results, "Haemoglobin", "Blood Panel", 16, "System OpenMRS", "99.0", 200.0, 300.0, true, null, true, null);
        assertOrderPresent(results, "ESR", "Blood Panel", 16, "System OpenMRS", "10.0", null, null, false, "Some Notes", false, null);

    }

    private void assertOrderPresent(List<LabOrderResult> labOrderResults, String testName, String panelName, Integer accessionEncounterId, String provider, String value, Double minNormal, Double maxNormal, Boolean abnormal, String notes, Boolean referredOut, String uploadedFileName) {
        Encounter accessionEncounter = Context.getEncounterService().getEncounter(accessionEncounterId);
        for (LabOrderResult labOrderResult : labOrderResults) {
            if(labOrderResult.getTestName().equals(testName) && labOrderResult.getAccessionUuid().equals(accessionEncounter.getUuid())) {
                assertEquals(panelName, labOrderResult.getPanelName());
                assertEquals(accessionEncounter.getEncounterDatetime(), labOrderResult.getAccessionDateTime());
                assertEquals(value, labOrderResult.getResult());
                assertEquals(minNormal, labOrderResult.getMinNormal());
                assertEquals(maxNormal, labOrderResult.getMaxNormal());
                assertEquals(abnormal, labOrderResult.getAbnormal());
                assertEquals(notes, labOrderResult.getNotes());
                assertEquals(referredOut, labOrderResult.getReferredOut());
                assertEquals(provider, labOrderResult.getProvider());
                assertEquals(uploadedFileName, labOrderResult.getUploadedFileName());
                return;
            }
        }
        fail();
    }
}
