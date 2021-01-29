package org.openmrs.module.bahmniemrapi.laborder.service;

import org.junit.Test;
import org.openmrs.Encounter;
import org.openmrs.Patient;
import org.openmrs.Visit;
import org.openmrs.api.context.Context;
import org.openmrs.module.bahmniemrapi.BaseIntegrationTest;
import org.openmrs.module.bahmniemrapi.accessionnote.contract.AccessionNote;
import org.openmrs.module.bahmniemrapi.laborder.contract.LabOrderResult;
import org.openmrs.module.bahmniemrapi.laborder.contract.LabOrderResults;
import org.springframework.beans.factory.annotation.Autowired;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

public class LabOrderResultsServiceIT extends BaseIntegrationTest {

    @Autowired
    private LabOrderResultsService labOrderResultsService;

    @Test
    public void shouldMapTestOrdersAndResultsForAllVisits() throws Exception {
        executeDataSet("diagnosisMetadata.xml");
        executeDataSet("dispositionMetadata.xml");
        executeDataSet("labOrderTestData.xml");
        Patient patient = Context.getPatientService().getPatient(1000000);

        LabOrderResults results = labOrderResultsService.getAll(patient, null, Integer.MAX_VALUE);
        List<LabOrderResult> labOrderResults = results.getResults();

        assertNotNull(labOrderResults);
        assertEquals(6, labOrderResults.size());

        assertOrderPresent(labOrderResults, "Haemoglobin", "Blood Panel", 16, "System OpenMRS", "99.0", 200.0, 300.0, true, null, true, null);
        assertOrderPresent(labOrderResults, "ESR", "Blood Panel", 16, "System OpenMRS", "10.0", null, null, false, "Some Notes", false, null);
        assertOrderPresent(labOrderResults, "Urea Nitrogen", null, 16, "System OpenMRS", "20.0", null, null, null, null, false, "8834dedb-dc15-4afe-a491-ea3ca4150bce_sample.jpeg");
        assertOrderPresent(labOrderResults, "HIV ELISA", null, 16, null, null, null, null, null, null, false, null);
        assertOrderPresent(labOrderResults, "PS for Malaria", null, 16, "System OpenMRS", null, null, null, null, null, true, null);
        assertOrderPresent(labOrderResults, "PS for Malaria", null, 17, "System OpenMRS", "Result for PS Malaria", null, null, null, null, false, null);
        assertFalse(isOrderPresent(labOrderResults, "Chest X-Ray", 16));
    }

    @Test
    public void shouldMapAccessionNotesForAGivenVisit() throws Exception {
        executeDataSet("diagnosisMetadata.xml");
        executeDataSet("dispositionMetadata.xml");
        executeDataSet("labOrderTestData.xml");

        Patient patient = Context.getPatientService().getPatient(1000000);
        Visit visit = Context.getVisitService().getVisit(4);

        LabOrderResults results = labOrderResultsService.getAll(patient, Arrays.asList(visit), Integer.MAX_VALUE);
        List<LabOrderResult> labOrderResults = results.getResults();

        assertEquals(1, labOrderResults.size());
        List<AccessionNote> accessionNotes = labOrderResults.get(0).getAccessionNotes();
        assertNotNull(accessionNotes);
        assertThat(accessionNotes.size(), is(equalTo(1)));
        AccessionNote accessionNote = accessionNotes.get(0);
        assertThat(accessionNote.getAccessionUuid(), is(equalTo("b0a81566-0c0c-11e4-bb80-f18addb6f9bb")));
        assertThat(accessionNote.getProviderName(), is(equalTo("System OpenMRS")));
        assertThat(accessionNote.getText(), is(equalTo("Notes from Lab Manager")));

        assertOrderPresent(labOrderResults, "PS for Malaria", null, 17, "System OpenMRS", "Result for PS Malaria", null, null, null, null, false, null);
    }

    @Test
    public void shouldGetLabOrdersForParticularConcepts() throws Exception {
        executeDataSet("diagnosisMetadata.xml");
        executeDataSet("dispositionMetadata.xml");
        executeDataSet("labOrderTestData.xml");

        Patient patient = Context.getPatientService().getPatient(1000000);

        Collection<String> concepts = new ArrayList<>();
        concepts.add("Blood Panel");

        List<LabOrderResult> results = labOrderResultsService.getAllForConcepts(patient, concepts, null, null, null);

        assertEquals(results.size(), 2);
        assertOrderPresent(results, "Haemoglobin", "Blood Panel", 16, "System OpenMRS", "99.0", 200.0, 300.0, true, null, true, null);
        assertOrderPresent(results, "ESR", "Blood Panel", 16, "System OpenMRS", "10.0", null, null, false, "Some Notes", false, null);

    }

    @Test
    public void shouldGetLabOrdersForParticularConceptsWithinGivenDateRange() throws Exception {
        executeDataSet("diagnosisMetadata.xml");
        executeDataSet("dispositionMetadata.xml");
        executeDataSet("labOrderTestData.xml");

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");
        Date startDate = simpleDateFormat.parse("2008-08-17T00:00:00.000");
        Date endDate = simpleDateFormat.parse("2008-08-20T00:00:00.000");

        Patient patient = Context.getPatientService().getPatient(1000000);

        Collection<String> concepts = new ArrayList<>();
        concepts.add("Blood Panel");
        concepts.add("Urea Nitrogen");

        List<LabOrderResult> results = labOrderResultsService.getAllForConcepts(patient, concepts, null, startDate, endDate);

        assertEquals(results.size(), 3);
        assertOrderPresent(results, "Haemoglobin", "Blood Panel", 16, "System OpenMRS", "99.0", 200.0, 300.0, true, null, true, null);
        assertOrderPresent(results, "ESR", "Blood Panel", 16, "System OpenMRS", "10.0", null, null, false, "Some Notes", false, null);
        assertOrderPresent(results, "Urea Nitrogen", null, 16, null, null, null, null, null, null, false, null);
    }


    @Test
    public void shouldMapTestOrdersAndResultsForGivenVisit() throws Exception {
        executeDataSet("diagnosisMetadata.xml");
        executeDataSet("dispositionMetadata.xml");
        executeDataSet("labOrderTestData.xml");
        Patient patient = Context.getPatientService().getPatient(1000000);
        Visit visit = Context.getVisitService().getVisit(4);

        LabOrderResults results = labOrderResultsService.getAll(patient, Arrays.asList(visit), Integer.MAX_VALUE);
        List<LabOrderResult> labOrderResults = results.getResults();

        assertNotNull(labOrderResults);
        assertEquals(1, labOrderResults.size());

        assertOrderPresent(labOrderResults, "PS for Malaria", null, 17, "System OpenMRS", "Result for PS Malaria", null, null, null, null, false, null);
    }
    
    @Test
    public void shouldMapTestOrdersRecordedInTestOrderTableWithTheirResultsForGivenVisit() throws Exception {
        executeDataSet("diagnosisMetadata.xml");
        executeDataSet("dispositionMetadata.xml");
        executeDataSet("labOrderTestData.xml");
        executeDataSet("labOrderAssociatedWithTestOrderData.xml");
        Patient patient = Context.getPatientService().getPatient(1000000);
        Visit visit = Context.getVisitService().getVisit(4);

        LabOrderResults results = labOrderResultsService.getAll(patient, Arrays.asList(visit), Integer.MAX_VALUE);
        List<LabOrderResult> labOrderResults = results.getResults();

        assertNotNull(labOrderResults);
        assertEquals(1, labOrderResults.size());

        assertOrderPresent(labOrderResults, "PS for Malaria", null, 17, "System OpenMRS", "Result for PS Malaria", null, null, null, null, false, null);
    }

    @Test
    public void shouldGetLabOrdersWithResultsEvenIfItIsDiscontinued() throws Exception {
        executeDataSet("diagnosisMetadata.xml");
        executeDataSet("dispositionMetadata.xml");
        executeDataSet("labOrderTestData.xml");
        Patient patient = Context.getPatientService().getPatient(1000001);

        Visit visit = Context.getVisitService().getVisit(5);

        LabOrderResults labOrderResults = labOrderResultsService.getAll(patient, Arrays.asList(visit), Integer.MAX_VALUE);
        List<LabOrderResult> labResults = labOrderResults.getResults();

        assertEquals(6, labResults.size());
    }

    private void assertOrderPresent(List<LabOrderResult> labOrderResults, String testName, String panelName, Integer accessionEncounterId, String provider, String value, Double minNormal, Double maxNormal, Boolean abnormal, String notes, Boolean referredOut, String uploadedFileName) {
        Encounter accessionEncounter = Context.getEncounterService().getEncounter(accessionEncounterId);
        for (LabOrderResult labOrderResult : labOrderResults) {
            if (labOrderResult.getTestName().equals(testName) && labOrderResult.getAccessionUuid().equals(accessionEncounter.getUuid())) {
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

    private boolean isOrderPresent(List<LabOrderResult> labOrderResults, String testName, Integer accessionEncounterId) {
        Encounter accessionEncounter = Context.getEncounterService().getEncounter(accessionEncounterId);
        for (LabOrderResult labOrderResult : labOrderResults) {
            if (labOrderResult.getTestName().equals(testName) && labOrderResult.getAccessionUuid().equals(accessionEncounter.getUuid())) {
                return true;
            }
        }
        return false;
    }
}
