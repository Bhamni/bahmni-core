package org.bahmni.module.bahmnicore.web.v1_0.controller;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.joda.time.LocalDate;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.openmrs.*;
import org.openmrs.api.VisitService;
import org.openmrs.api.context.Context;
import org.openmrs.module.bahmniemrapi.document.contract.VisitDocumentResponse;
import org.openmrs.module.bahmniemrapi.encountertransaction.contract.BahmniEncounterTransaction;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;


@org.springframework.test.context.ContextConfiguration(locations = {"classpath:TestingApplicationContext.xml"}, inheritLocations = true)
public class EncounterControllerDocumentPartIT extends BaseWebControllerTest {

    public static final String TMP_DOCUMENT_IMAGES = "/tmp/document_images";
    private final String image = "R0lGODlhAQABAIAAAAAAAP///yH5BAEAAAAALAAAAAABAAEAAAIBRAA7";
    @Autowired
    private VisitService visitService;
    public static final String IMAGE_CONCEPT_UUID = "e060cf44-3d3d-11e3-bf2b-0800271c1b75";

    @Before
    public void setUp() throws IOException {
        FileUtils.deleteDirectory(new File(TMP_DOCUMENT_IMAGES));
        System.setProperty("bahmnicore.documents.baseDirectory", TMP_DOCUMENT_IMAGES);
    }

    @Test
    public void shouldUploadDocumentsForExistingVisit() throws Exception {
        executeDataSet("uploadDocuments.xml");
        Patient patient = Context.getPatientService().getPatientByUuid("75e04d42-3ca8-11e3-bf2b-0800271c1b75");
        Visit visit = createVisitForDate(patient, null, new LocalDate(2010, 12, 1).toDate(), true);
        String json = "{" +
                    "\"patientUuid\":\"" + "75e04d42-3ca8-11e3-bf2b-0800271c1b75" + "\"," +
                    "\"visitTypeUuid\":\"" + "b45ca846-c79a-11e2-b0c0-8e397087571c" + "\"," +
                    "\"visitStartDate\":\"2010-12-1T18:30:00.000Z\"," +
                    "\"visitEndDate\":\"2010-12-31T18:30:00.000Z\"," +
                    "\"encounterTypeUuid\":\"" + "759799ab-c9a5-435e-b671-77773ada74e4" + "\"," +
                    "\"visitUuid\":\"" + visit.getUuid() + "\"," +
                    "\"encounterDateTime\":\"2010-12-1T19:30:00.000Z\"," +
                    "\"providerUuid\":\"331c6bf8-7846-11e3-a96a-0800271c1b75\"," +
                    "\"documents\": [{\"testUuid\": \"" + "e340cf44-3d3d-11e3-bf2b-0800271c1b75" + "\", \"image\": \"" + image + "\", \"format\": \".jpeg\"}]" +
                "}";


        BahmniEncounterTransaction bahmniEncounterTransaction = deserialize(handle(newPostRequest("/rest/v1/bahmnicore/bahmniencounter", json)), BahmniEncounterTransaction.class);
        Visit existingVisit = visitService.getVisitByUuid(bahmniEncounterTransaction.getVisitUuid());

        assertEquals(visit.getUuid(), existingVisit.getUuid());
        assertEquals(1, existingVisit.getEncounters().size());
        Encounter encounter = new ArrayList<>(existingVisit.getEncounters()).get(0);
        assertEquals(1, encounter.getAllObs().size());
        assertEquals(1, encounter.getEncounterProviders().size());
        EncounterProvider encounterProvider = encounter.getEncounterProviders().iterator().next();
        assertEquals("Jane Doe", encounterProvider.getProvider().getName());
        assertEquals("Unknown", encounterProvider.getEncounterRole().getName());
        Obs parentObs = new ArrayList<>(encounter.getAllObs()).get(0);
        assertEquals(1, parentObs.getGroupMembers().size());
        assertObservationWithImage(parentObs, "e340cf44-3d3d-11e3-bf2b-0800271c1b75", IMAGE_CONCEPT_UUID);
    }

    @Test
    @Ignore
    public void shouldDoMultipleUploadsToSameTest() throws Exception {
        executeDataSet("uploadDocuments.xml");
        Patient patient = Context.getPatientService().getPatientByUuid("75e04d42-3ca8-11e3-bf2b-0800271c1b75");
        Visit visit = createVisitForDate(patient, null, new Date(), true);

        String firstRequest = "{" +
                "\"patientUuid\":\"" + "75e04d42-3ca8-11e3-bf2b-0800271c1b75" + "\"," +
                "\"visitTypeUuid\":\"" + "b45ca846-c79a-11e2-b0c0-8e397087571c" + "\"," +
                "\"visitStartDate\":\"2019-12-31T18:30:00.000Z\"," +
                "\"visitEndDate\":\"2019-12-31T18:30:00.000Z\"," +
                "\"encounterTypeUuid\":\"" + "759799ab-c9a5-435e-b671-77773ada74e4" + "\"," +
                "\"visitUuid\":\"" + visit.getUuid() + "\"," +
                "\"encounterDateTime\":\"2019-12-31T18:30:00.000Z\"," +
                "\"providerUuid\":\"331c6bf8-7846-11e3-a96a-0800271c1b75\"," +
                "\"documents\": [{\"testUuid\": \"" + "e340cf44-3d3d-11e3-bf2b-0800271c1b75" + "\", \"image\": \"" + image + "\", \"format\": \".jpeg\"}]" +
                "}";


        deserialize(handle(newPostRequest("/rest/v1/bahmnicore/visitDocument", firstRequest)), VisitDocumentResponse.class);

        String secondRequest = "{" +
                "\"patientUuid\":\"" + "75e04d42-3ca8-11e3-bf2b-0800271c1b75" + "\"," +
                "\"visitTypeUuid\":\"" + "b45ca846-c79a-11e2-b0c0-8e397087571c" + "\"," +
                "\"visitStartDate\":\"2019-12-31T18:30:00.000Z\"," +
                "\"visitEndDate\":\"2019-12-31T18:30:00.000Z\"," +
                "\"encounterTypeUuid\":\"" + "759799ab-c9a5-435e-b671-77773ada74e4" + "\"," +
                "\"visitUuid\":\"" + visit.getUuid() + "\"," +
                "\"encounterDateTime\":\"2019-12-31T18:30:00.000Z\"," +
                "\"providerUuid\":\"331c6bf8-7846-11e3-a96a-0800271c1b75\"," +
                "\"documents\": [{\"testUuid\": \"" + "e340cf44-3d3d-11e3-bf2b-0800271c1b75" + "\", \"image\": \"" + image + "\", \"format\": \".png\"}]" +
                "}";

        VisitDocumentResponse visitDocumentResponse = deserialize(handle(newPostRequest("/rest/v1/bahmnicore/visitDocument", secondRequest)), VisitDocumentResponse.class);
        Visit existingVisit = visitService.getVisitByUuid(visitDocumentResponse.getVisitUuid());

        assertEquals(visit.getUuid(), existingVisit.getUuid());
        assertEquals(1, existingVisit.getEncounters().size());
        Encounter encounter = new ArrayList<>(existingVisit.getEncounters()).get(0);
        assertEquals(1, encounter.getEncounterProviders().size());
        assertEquals(2, encounter.getAllObs().size());
        Obs parentObs1 = new ArrayList<>(encounter.getAllObs()).get(0);
        Obs parentObs2 = new ArrayList<>(encounter.getAllObs()).get(1);
        assertEquals(1, parentObs1.getGroupMembers().size());
        assertEquals(1, parentObs1.getGroupMembers().size());

        String imageUrl = parentObs1.getGroupMembers().iterator().next().getValueText();
        assertTrue(imageUrl.contains("jpeg") || imageUrl.contains("png"));
        imageUrl = parentObs2.getGroupMembers().iterator().next().getValueText();
        assertTrue(imageUrl.contains("jpeg") || imageUrl.contains("png"));
    }

    @Test
    public void shouldDeleteDocumentsForExistingVisit() throws Exception {
        executeDataSet("uploadDocuments.xml");
        String patientUUID = "75e04d42-3ca8-11e3-bf2b-0800271c1b75";
        String encounterTypeUUID = "759799ab-c9a5-435e-b671-77773ada74e4";
        String visitTypeUUID = "b45ca846-c79a-11e2-b0c0-8e397087571c";
        String testUUID = "e340cf44-3d3d-11e3-bf2b-0800271c1b75";

        Patient patient = Context.getPatientService().getPatientByUuid(patientUUID);
        Visit visit = createVisitForDate(patient, null, new Date(), true);

        String addDocumentJSON = "{" +
                "\"patientUuid\":\"" + patientUUID + "\"," +
                "\"visitTypeUuid\":\"" + visitTypeUUID + "\"," +
                "\"visitStartDate\":\"2019-12-31T18:30:00.000Z\"," +
                "\"visitEndDate\":\"2019-12-31T18:30:00.000Z\"," +
                "\"encounterTypeUuid\":\"" + encounterTypeUUID + "\"," +
                "\"visitUuid\":\"" + visit.getUuid() + "\"," +
                "\"encounterDateTime\":\"2019-12-31T18:30:00.000Z\"," +
                "\"providerUuid\":\"331c6bf8-7846-11e3-a96a-0800271c1b75\"," +
                "\"documents\": [{\"testUuid\": \"" + testUUID + "\", \"image\": \"" + image + "\", \"format\": \".jpeg\"}]" +
                "}";

        VisitDocumentResponse documentAddedResponse = deserialize(handle(newPostRequest("/rest/v1/bahmnicore/visitDocument", addDocumentJSON)), VisitDocumentResponse.class);
        Visit addedVisit = visitService.getVisitByUuid(documentAddedResponse.getVisitUuid());
        String obsUuid = addedVisit.getEncounters().iterator().next().getAllObs().iterator().next().getUuid();

        String deleteDocumentJSON = "{" +
                "\"patientUuid\":\"" + patientUUID + "\"," +
                "\"visitTypeUuid\":\"" + visitTypeUUID + "\"," +
                "\"visitStartDate\":\"2019-12-31T18:30:00.000Z\"," +
                "\"visitEndDate\":\"2019-12-31T18:30:00.000Z\"," +
                "\"encounterTypeUuid\":\"" + encounterTypeUUID + "\"," +
                "\"visitUuid\":\"" + visit.getUuid() + "\"," +
                "\"encounterDateTime\":\"2019-12-31T18:30:00.000Z\"," +
                "\"providerUuid\":\"331c6bf8-7846-11e3-a96a-0800271c1b75\"," +
                "\"documents\": [{\"testUuid\": \"" + testUUID + "\", \"image\": \"" + image + "\", \"format\": \".jpeg\", \"voided\" : true, \"obsUuid\" : \""+obsUuid+"\"}]" +
                "}";

        VisitDocumentResponse response = deserialize(handle(newPostRequest("/rest/v1/bahmnicore/visitDocument", deleteDocumentJSON)), VisitDocumentResponse.class);
        Visit updatedVisit = visitService.getVisitByUuid(response.getVisitUuid());

        assertEquals(1, updatedVisit.getEncounters().size());
        Encounter encounter = new ArrayList<>(updatedVisit.getEncounters()).get(0);
        assertEquals(1, encounter.getAllObs(true).size());
        assertEquals(true, encounter.getAllObs(true).iterator().next().getVoided());
    }

    @Test
    public void shouldUpdateTestAssociatedToExisitingDocument() throws Exception {
        executeDataSet("uploadDocuments.xml");
        String patientUUID = "75e04d42-3ca8-11e3-bf2b-0800271c1b75";
        String encounterTypeUUID = "759799ab-c9a5-435e-b671-77773ada74e4";
        String visitTypeUUID = "b45ca846-c79a-11e2-b0c0-8e397087571c";
        String testUUID = "e340cf44-3d3d-11e3-bf2b-0800271c1b75";
        String otherTestUUID = "07a90a4b-0fca-42ff-8988-f5b519be06ab";

        Patient patient = Context.getPatientService().getPatientByUuid(patientUUID);
        Visit visit = createVisitForDate(patient, null, new Date(), true);

        String addDocumentJSON = "{" +
                "\"patientUuid\":\"" + patientUUID + "\"," +
                "\"visitTypeUuid\":\"" + visitTypeUUID + "\"," +
                "\"visitStartDate\":\"2019-12-31T18:30:00.000Z\"," +
                "\"visitEndDate\":\"2019-12-31T18:30:00.000Z\"," +
                "\"encounterTypeUuid\":\"" + encounterTypeUUID + "\"," +
                "\"visitUuid\":\"" + visit.getUuid() + "\"," +
                "\"encounterDateTime\":\"2019-12-31T18:30:00.000Z\"," +
                "\"providerUuid\":\"331c6bf8-7846-11e3-a96a-0800271c1b75\"," +
                "\"documents\": [{\"testUuid\": \"" + testUUID + "\", \"image\": \"" + image + "\", \"format\": \".jpeg\"}]" +
                "}";

        VisitDocumentResponse documentAddedResponse = deserialize(handle(newPostRequest("/rest/v1/bahmnicore/visitDocument", addDocumentJSON)), VisitDocumentResponse.class);
        Visit addedVisit = visitService.getVisitByUuid(documentAddedResponse.getVisitUuid());
        String obsUuid = addedVisit.getEncounters().iterator().next().getAllObs().iterator().next().getUuid();

        String updateTestInDocumentJSON = "{" +
                "\"patientUuid\":\"" + patientUUID + "\"," +
                "\"visitTypeUuid\":\"" + visitTypeUUID + "\"," +
                "\"visitStartDate\":\"2019-12-31T18:30:00.000Z\"," +
                "\"visitEndDate\":\"2019-12-31T18:30:00.000Z\"," +
                "\"encounterTypeUuid\":\"" + encounterTypeUUID + "\"," +
                "\"visitUuid\":\"" + visit.getUuid() + "\"," +
                "\"encounterDateTime\":\"2019-12-31T18:30:00.000Z\"," +
                "\"providerUuid\":\"331c6bf8-7846-11e3-a96a-0800271c1b75\"," +
                "\"documents\": [{\"testUuid\": \"" + otherTestUUID + "\", \"image\": \"" + "/x/y/1-1-1-1.jpg" + "\", \"format\": \".jpeg\", \"obsUuid\" : \""+obsUuid+"\"}]" +
                "}";

        VisitDocumentResponse response = deserialize(handle(newPostRequest("/rest/v1/bahmnicore/visitDocument", updateTestInDocumentJSON)), VisitDocumentResponse.class);
        Visit updatedVisit = visitService.getVisitByUuid(response.getVisitUuid());

        assertEquals(1, updatedVisit.getEncounters().size());
        Encounter encounter = new ArrayList<>(updatedVisit.getEncounters()).get(0);
        assertEquals(1, encounter.getAllObs().size());

        Obs parentObs = new ArrayList<>(encounter.getAllObs()).get(0);
        assertEquals(otherTestUUID, parentObs.getConcept().getUuid());
        assertEquals(1, parentObs.getGroupMembers(true).size());
    }

    private Visit createVisitForDate(Patient patient, Encounter encounter, Date orderDate, boolean isActive) {
        VisitType opdVisitType = visitService.getVisitType(1);
        Visit visit = new Visit(patient, opdVisitType, orderDate);
        if (encounter != null)
            visit.addEncounter(encounter);
        if (!isActive)
            visit.setStopDatetime(DateUtils.addDays(orderDate, 1));
        return visitService.saveVisit(visit);
    }


    private Obs assertObservationWithImage(Obs parentObs, String testUUID, String documentUUID) {
        Obs expectedObservation = null;
        assertEquals(parentObs.getConcept().getUuid(), testUUID);
        assertTrue(parentObs.getGroupMembers().size() > 0);
        for (Obs memberObs : parentObs.getGroupMembers()) {
            if (documentUUID.equals(memberObs.getConcept().getUuid())) {
                expectedObservation = memberObs;
                break;
            }
        }
        assertTrue(expectedObservation != null);
        return expectedObservation;
    }
}
