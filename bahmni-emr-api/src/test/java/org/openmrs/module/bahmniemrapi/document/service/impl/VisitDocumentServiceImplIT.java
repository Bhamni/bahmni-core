package org.openmrs.module.bahmniemrapi.document.service.impl;

import static org.hamcrest.Matchers.equalTo;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.Encounter;
import org.openmrs.Obs;
import org.openmrs.Visit;
import org.openmrs.api.EncounterService;
import org.openmrs.api.ObsService;
import org.openmrs.api.VisitService;
import org.openmrs.api.context.Context;
import org.openmrs.module.bahmniemrapi.document.contract.Document;
import org.openmrs.module.bahmniemrapi.document.contract.VisitDocumentRequest;
import org.openmrs.module.bahmniemrapi.document.service.VisitDocumentService;
import org.openmrs.test.BaseModuleContextSensitiveTest;
import org.springframework.beans.factory.annotation.Autowired;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.*;

@org.springframework.test.context.ContextConfiguration(locations = {"classpath:TestingApplicationContext.xml"}, inheritLocations = true)
public class VisitDocumentServiceImplIT extends BaseModuleContextSensitiveTest {

    public static final String LOCATION_UUID = "8d6c993e-c2cc-11de-8d13-0040c6dffd0f";
    @Autowired
    VisitDocumentService visitDocumentService;
    @Autowired
    EncounterService encounterService;
    @Autowired
    ObsService obsService;
    @Autowired
    VisitService visitService;

    VisitDocumentRequest visitDocumentRequest;

    @Before
    public void setUp() throws Exception {
        executeDataSet("visitDocumentData.xml");
    }

    @Test
    public void shouldDeleteObservationsOfPreviousEncounters() throws ParseException {
        Date visitStartDate = getDateFromString("2014-06-22 00:00:00");
        Date encounterDate = getDateFromString("2014-06-23 00:00:00");

        List<Document> documents = new ArrayList<>();
        documents.add(new Document("/patient_file", null, "3f596de5-5caa-11e3-a4c0-0800271c1b75", "6d0ae386-707a-4629-9850-f15206e63j8s", encounterDate, true));

        visitDocumentRequest = new VisitDocumentRequest("86526ed5-3c11-11de-a0ba-001e378eb67a",
                "d794516f-210d-4c4e-8978-467d97969f31",
                "f01c54cb-2225-471a-9cd5-d348552c337c",
                visitStartDate,
                null,
                "759799ab-c9a5-435e-b671-77773ada74e4",
                encounterDate,
                documents,
                "331c6bf8-7846-11e3-a96a-0800271c1b75", null);
        visitDocumentService.upload(visitDocumentRequest);

        Encounter encounter = encounterService.getEncounterByUuid("6d0ae386-707a-4629-9850-f15206e63ab0");
        for (Obs obs : encounter.getAllObs()) {
            if(obs.getUuid().equals("6d0ae386-707a-4629-9850-f15206e63j8s"))
                assertThat(obs.getVoided(), is(true));
        }
    }


    @Test
    public void shouldNotChangeObservationsIfSameDetailsProvidedOnceAgain() throws Exception {
        Date visitStartDate = getDateFromString("2014-06-22 00:00:00");
        Date encounterDate = getDateFromString("2014-06-23 00:00:00");

        List<Document> documents = new ArrayList<>();
        documents.add(new Document("/radiology/foo.jpg", null, "4f596de5-5caa-11e3-a4c0-0800271c1b75", "6d0ae386-707a-4629-9850-f15206e63kj0", encounterDate, false));


        VisitDocumentRequest visitDocumentRequest = new VisitDocumentRequest("86526ed5-3c11-11de-a0ba-001e378eb67a",
                "ad41fb41-a41a-4ad6-8835-2f59099acf5a",
                "f01c54cb-2225-471a-9cd5-d348552c337c",
                visitStartDate,
                null,
                "4ee21921-01cc-4720-a6bf-a61a17c4d05b",
                encounterDate,
                documents,
                "331c6bf8-7846-11e3-a96a-0800271c1333", "899c993e-c2cc-11de-8d13-0040c6dffd0f");
        visitDocumentService.upload(visitDocumentRequest);

        Encounter encounter = encounterService.getEncounterByUuid("6d0ae386-707a-4629-9850-f15206e63222");

        Obs savedDoc = getSavedDocument(encounter.getAllObs(), "4f596de5-5caa-11e3-a4c0-0800271c1b75");

        assertNotNull(savedDoc);
        Set<Obs> groupMembers = savedDoc.getGroupMembers();
        assertThat(groupMembers.size(), is(equalTo(1)));
        assertThat(groupMembers.iterator().next().getValueText(), is("/radiology/foo.jpg"));
        assertThat(groupMembers.iterator().next().getUuid(), is("6d0ae386-707a-4629-9850-f15606e63666"));
    }

    @Test
    public void shouldPreferVoidOverUpdateWhenEditingADocument() throws Exception {
        Date visitStartDate = getDateFromString("2014-06-22 00:00:00");
        Date encounterDate = getDateFromString("2014-06-23 00:00:00");

        List<Document> documents = new ArrayList<>();
        documents.add(new Document("/radiology/foo.jpg", null, "3f596de5-5caa-11e3-a4c0-0800271c1b75", "6d0ae386-707a-4629-9850-f15206e63kj0", encounterDate, true));


        VisitDocumentRequest visitDocumentRequest = new VisitDocumentRequest("86526ed5-3c11-11de-a0ba-001e378eb67a",
                "ad41fb41-a41a-4ad6-8835-2f59099acf5a",
                "f01c54cb-2225-471a-9cd5-d348552c337c",
                visitStartDate,
                null,
                "4ee21921-01cc-4720-a6bf-a61a17c4d05b",
                encounterDate,
                documents,
                "331c6bf8-7846-11e3-a96a-0800271c1333", "899c993e-c2cc-11de-8d13-0040c6dffd0f");
        visitDocumentService.upload(visitDocumentRequest);

        Encounter encounter = encounterService.getEncounterByUuid("6d0ae386-707a-4629-9850-f15206e63222");

        Boolean isObservationVoided = obsService.getObsByUuid("6d0ae386-707a-4629-9850-f15206e63kj0").isVoided();
        assertTrue("Observation is not voided", isObservationVoided);


        Obs savedDoc = getSavedDocument(encounter.getAllObs(), "3f596de5-5caa-11e3-a4c0-0800271c1b75");

        assertNull(savedDoc);
    }

    @Test
    public void shouldChangeObservationsOfPreviousEncounters() throws Exception {
        Date visitStartDate = getDateFromString("2014-06-22 00:00:00");
        Date encounterDate = getDateFromString("2014-06-23 00:00:00");

        List<Document> documents = new ArrayList<>();
        documents.add(new Document("/radiology/foo.jpg", null, "5f596de5-5caa-11e3-a4c0-0800271c1b75", "6d0ae386-707a-4629-9850-f15206e63kj0", encounterDate, false));


        VisitDocumentRequest visitDocumentRequest = new VisitDocumentRequest("86526ed5-3c11-11de-a0ba-001e378eb67a",
                "ad41fb41-a41a-4ad6-8835-2f59099acf5a",
                "f01c54cb-2225-471a-9cd5-d348552c337c",
                visitStartDate,
                null,
                "4ee21921-01cc-4720-a6bf-a61a17c4d05b",
                encounterDate,
                documents,
                "331c6bf8-7846-11e3-a96a-0800271c1333", "899c993e-c2cc-11de-8d13-0040c6dffd0f");
        executeDataSet("visitDocumentData.xml");
        visitDocumentService.upload(visitDocumentRequest);

        Encounter encounter = encounterService.getEncounterByUuid("6d0ae386-707a-4629-9850-f15206e63222");
        for (Obs obs : encounter.getAllObs()) {
            if(obs.getUuid().equals("6d0ae386-707a-4629-9850-f15606e63666")){
                assertThat(obs.getVoided(), is(true));
            }
        }

        Obs savedDoc = getSavedDocument(encounter.getAllObs(), "5f596de5-5caa-11e3-a4c0-0800271c1b75");

        assertNotNull(savedDoc);
        assertThat(savedDoc.getConcept().getId(),is(333));
        assertThat(savedDoc.getGroupMembers().iterator().next().getValueText(),is("/radiology/foo.jpg"));
        assertEquals(LOCATION_UUID, encounter.getLocation().getUuid());
    }


    @Test
    public void shouldCreateObservations() throws Exception {
        Date visitStartDate = getDateFromString("2014-06-22 00:00:00");
        Date encounterDate = getDateFromString("2014-06-23 00:00:00");
        Date obsDate = getDateFromString("2014-06-24 00:10:00");

        List<Document> documents = new ArrayList<>();
        documents.add(new Document("/radiology/fooo-bar.jpg", null, "4f596de5-5caa-11e3-a4c0-0800271c1b75", null, obsDate, false));

        visitDocumentRequest = new VisitDocumentRequest("86526ed5-3c11-11de-a0ba-001e378eb67a",
                "d794516f-210d-4c4e-8978-467d97969f31",
                "f01c54cb-2225-471a-9cd5-d348552c337c",
                visitStartDate,
                null,
                "759799ab-c9a5-435e-b671-77773ada74e4",
                encounterDate,
                documents,
                "331c6bf8-7846-11e3-a96a-0800271c1b75", LOCATION_UUID);

        visitDocumentService.upload(visitDocumentRequest);

        Encounter encounter = encounterService.getEncounterByUuid("6d0ae386-707a-4629-9850-f15206e63ab0");

        Obs savedDoc = getSavedDocument(encounter.getAllObs(),"4f596de5-5caa-11e3-a4c0-0800271c1b75");

        assertNotNull(savedDoc);
        assertThat(savedDoc.getConcept().getId(),is(222));
        assertThat(savedDoc.getGroupMembers().iterator().next().getValueText(),is("/radiology/fooo-bar.jpg"));
        assertEquals(LOCATION_UUID, encounter.getLocation().getUuid());
    }

    @Test
    public void shouldUseVisitStartTimeAsEncounterDateTimeForPreviousVisits() throws Exception {
        Date visitStartDate = getDateFromString("2010-09-22 00:00:00");

        List<Document> documents = new ArrayList<>();
        documents.add(new Document("/radiology/foo-lalala.jpg", null, "3f596de5-5caa-11e3-a4c0-0800271c1b75", null, null, false));


        VisitDocumentRequest visitDocumentRequest = new VisitDocumentRequest("86526ed5-3c11-11de-a0ba-001e378eb67a",
                "ad41fb41-a41a-4ad6-8835-2f59099acf5a",
                "f01c54cb-2225-471a-9cd5-d348552c337c",
                null,
                null,
                "759799ab-c9a5-435e-b671-77773ada74e4",
                null,
                documents,
                "331c6bf8-7846-11e3-a96a-0800271c1333", null);
        executeDataSet("visitDocumentData.xml");

//        Date currentDate = new Date(System.currentTimeMillis() - 1000);
        visitDocumentService.upload(visitDocumentRequest);
        Visit visit = visitService.getVisit(1);
        Set<Encounter> encounters = visit.getEncounters();
        Encounter encounter = getEncounterByTypeUuid(encounters,"759799ab-c9a5-435e-b671-77773ada74e4");
        boolean condition = encounter.getEncounterDatetime().compareTo(visitStartDate) >= 0;
        assertTrue(condition);

        Set<Obs> allObs = encounter.getAllObs();
        Obs savedDocument = getSavedDocument(allObs, "3f596de5-5caa-11e3-a4c0-0800271c1b75");
        assertTrue(savedDocument.getObsDatetime().compareTo(visitStartDate)==0);
    }

    @Test
    public void shouldUseNewDateAsEncounterDateTimeForActiveVisits() throws Exception {
        Date visitStartDate = getDateFromString("2014-06-22 00:00:00");
        Date encounterDate = getDateFromString("2014-06-23 00:00:00");
        Date obsDate = getDateFromString("2014-06-24 00:10:00");

        List<Document> documents = new ArrayList<>();
        documents.add(new Document("/radiology/fooo-bar.jpg", null, "4f596de5-5caa-11e3-a4c0-0800271c1b75", null, obsDate, false));

        visitDocumentRequest = new VisitDocumentRequest("86526ed5-3c11-11de-a0ba-001e378eb67a",
                "d794516f-210d-4c4e-8978-467d97969f31",
                "f01c54cb-2225-471a-9cd5-d348552c337c",
                visitStartDate,
                null,
                "4ee21921-01cc-4720-a6bf-a61a17c4d05b",
                encounterDate,
                documents,
                "331c6bf8-7846-11e3-a96a-0800271c1b75", null);

        Date currentDate = new Date(System.currentTimeMillis() - 1000);
        visitDocumentService.upload(visitDocumentRequest);
        Visit visit = visitService.getVisit(2);
        Set<Encounter> encounters = visit.getEncounters();
        Encounter encounter = getEncounterByTypeUuid(encounters, "4ee21921-01cc-4720-a6bf-a61a17c4d05b");
        boolean condition = encounter.getEncounterDatetime().compareTo(currentDate) >= 0;
        assertTrue(condition);

        Set<Obs> allObs = encounter.getAllObs();
        Obs savedDocument = getSavedDocument(allObs, "4f596de5-5caa-11e3-a4c0-0800271c1b75");
        assertTrue(savedDocument.getObsDatetime().compareTo(currentDate)>=0);
    }

    private Encounter getEncounterByTypeUuid(Set<Encounter> encounters, String encounterUuid) {
        for (Encounter encounter : encounters) {
            if(encounter.getEncounterType().getUuid().equals(encounterUuid)){
                return encounter;
            }
        }
        return null;
    }

    private Obs getSavedDocument(Set<Obs> allObs,String conceptUuid) {
        for (Obs obs : allObs) {
            if(obs.getConcept().getUuid().equals(conceptUuid)){
                return obs;
            }
        }
        return null;
    }

    private Date getDateFromString(String date) throws ParseException {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        return simpleDateFormat.parse(date);
    }
}