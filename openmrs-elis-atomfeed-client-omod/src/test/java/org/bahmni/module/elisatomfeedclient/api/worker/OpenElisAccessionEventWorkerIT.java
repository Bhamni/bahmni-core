package org.bahmni.module.elisatomfeedclient.api.worker;

import org.bahmni.module.elisatomfeedclient.api.ElisAtomFeedProperties;
import org.bahmni.module.elisatomfeedclient.api.builder.OpenElisAccessionBuilder;
import org.bahmni.module.elisatomfeedclient.api.builder.OpenElisTestDetailBuilder;
import org.bahmni.module.elisatomfeedclient.api.client.impl.HealthCenterFilterRule;
import org.bahmni.module.elisatomfeedclient.api.domain.OpenElisAccession;
import org.bahmni.module.elisatomfeedclient.api.domain.OpenElisAccessionNote;
import org.bahmni.module.elisatomfeedclient.api.domain.OpenElisTestDetail;
import org.bahmni.module.elisatomfeedclient.api.mapper.AccessionHelper;
import org.bahmni.webclients.HttpClient;
import org.ict4h.atomfeed.client.domain.Event;
import org.junit.Before;
import org.junit.Test;
import org.mockito.*;
import org.openmrs.Encounter;
import org.openmrs.EncounterType;
import org.openmrs.Obs;
import org.openmrs.Visit;
import org.openmrs.api.EncounterService;
import org.openmrs.api.VisitService;
import org.openmrs.api.context.Context;
import org.openmrs.web.test.BaseModuleWebContextSensitiveTest;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;

import static org.junit.Assert.*;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

@org.springframework.test.context.ContextConfiguration(locations = {"classpath:TestingApplicationContext.xml"}, inheritLocations = true)
public class OpenElisAccessionEventWorkerIT extends BaseModuleWebContextSensitiveTest {

    public static final String ENCOUNTER_TYPE_LAB_RESULT = "LAB_RESULT";
    public static final String VALIDATION_NOTES = "VALIDATION NOTES";
    @Mock
    HttpClient httpClient;
    @Mock
    HealthCenterFilterRule healthCenterFilterRule;
    @Autowired
    private ElisAtomFeedProperties properties;
    private OpenElisAccessionEventWorker openElisAccessionEventWorker;
    private String openElisUrl = "http://localhost:8080/";
    private Event event = new Event("id", "openelis/accession/12-34-56-78", "title", "feedUri");

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        this.openElisAccessionEventWorker = new OpenElisAccessionEventWorker(properties, httpClient,
                Context.getEncounterService(), Context.getConceptService(), new AccessionHelper(properties),
                Context.getProviderService(),
                healthCenterFilterRule);
        when(healthCenterFilterRule.passesWith("GAN")).thenReturn(true);
        when(healthCenterFilterRule.passesWith("ANC")).thenReturn(false);
    }

    @Test
    public void shouldCreateResultEncounterAndObsForTestWithResultAndOtherValues() throws Exception {
        executeDataSet("labResult.xml");

        OpenElisTestDetail test1 = new OpenElisTestDetailBuilder()
                .withTestUuid("7923d0e0-8734-11e3-baa7-0800200c9a66")
                .withResult("10.5")
                .withProviderUuid("331c6bf8-7846-11e3-a96a-09xD371c1b75")
                .withMinNormal("10")
                .withMaxNormal("20.2")
                .withAbnormal("false")
                .withDateTime("2014-01-30T11:50:18+0530")
                .withResultType("N")
                .build();
        OpenElisAccession openElisAccession = new OpenElisAccessionBuilder().withDateTime("2014-01-30T11:50:18+0530").withTestDetails(new HashSet<>(Arrays.asList(test1))).build();
        openElisAccession.setAccessionUuid("6d0af4567-707a-4629-9850-f15206e63ab0");

        when(httpClient.get(openElisUrl + event.getContent(), OpenElisAccession.class)).thenReturn(openElisAccession);

        openElisAccessionEventWorker.associateTestResultsToOrder(openElisAccession);

        Visit visit = Context.getVisitService().getVisit(2);
        Encounter labEncounter = null;
        Set<Encounter> encounters = visit.getEncounters();
        for (Encounter encounter : encounters) {
            if (encounter.getEncounterType().getName().equals(ENCOUNTER_TYPE_LAB_RESULT)) {
                labEncounter = encounter;
            }
        }

        assertEquals(2, encounters.size());
        assertNotNull(labEncounter);
        Set<Obs> topLevelObs = labEncounter.getAllObs();
        assertEquals(1, topLevelObs.size());
        final Set<Obs> testLevelObs = getGroupMembersForObs(topLevelObs);
        assertEquals(1, testLevelObs.size());
        final Set<Obs> resultMembers = getGroupMembersForObs(testLevelObs);
        assertEquals(4, resultMembers.size());
    }

    @Test
    public void shouldCreateResultObsWhenTestIsReferredOut() throws Exception {
        executeDataSet("labResult.xml");

        OpenElisTestDetail test1 = new OpenElisTestDetailBuilder()
                .withTestUuid("7923d0e0-8734-11e3-baa7-0800200c9a66")
                .withStatus("referred out")
                .build();
        OpenElisAccession openElisAccession = new OpenElisAccessionBuilder().withDateTime("2014-01-30T11:50:18+0530").withTestDetails(new HashSet<>(Arrays.asList(test1))).build();
        openElisAccession.setAccessionUuid("6d0af4567-707a-4629-9850-f15206e63ab0");

        when(httpClient.get(openElisUrl + event.getContent(), OpenElisAccession.class)).thenReturn(openElisAccession);

        openElisAccessionEventWorker.associateTestResultsToOrder(openElisAccession);


        Visit visit = Context.getVisitService().getVisit(2);
        Encounter labEncounter = null;
        Set<Encounter> encounters = visit.getEncounters();
        for (Encounter encounter : encounters) {
            if (encounter.getEncounterType().getName().equals(ENCOUNTER_TYPE_LAB_RESULT)) {
                labEncounter = encounter;
            }
        }

        assertEquals(2, encounters.size());
        assertNotNull(labEncounter);
        Set<Obs> topLevelObs = labEncounter.getAllObs();
        assertEquals(1, topLevelObs.size());
        final Set<Obs> testLevelObs = getGroupMembersForObs(topLevelObs);
        assertEquals(1, testLevelObs.size());
        final Set<Obs> resultMembers = getGroupMembersForObs(testLevelObs);
        assertEquals(1, resultMembers.size());
        Obs status = resultMembers.iterator().next();
        assertEquals("Ensure the concept is Referred Out", status.getConcept(), Context.getConceptService().getConcept(108));
    }

    @Test
    public void shouldCreateResultEncounterWithSystemProvider() throws Exception {
        executeDataSet("labResult.xml");

        OpenElisTestDetail test1 = new OpenElisTestDetailBuilder()
                .withTestUuid("7923d0e0-8734-11e3-baa7-0800200c9a66")
                .withResult("10.5")
                .withMinNormal("10")
                .withMaxNormal("20.2")
                .withAbnormal("false")
                .withDateTime("2014-01-30T11:50:18+0530")
                .withResultType("N")
                .build();
        OpenElisAccession openElisAccession = new OpenElisAccessionBuilder().withDateTime("2014-01-30T11:50:18+0530").withTestDetails(new HashSet<>(Arrays.asList(test1))).build();
        openElisAccession.setAccessionUuid("6d0af4567-707a-4629-9850-f15206e63ab0");

        when(httpClient.get(openElisUrl + event.getContent(), OpenElisAccession.class)).thenReturn(openElisAccession);

        openElisAccessionEventWorker.associateTestResultsToOrder(openElisAccession);

        Visit visit = Context.getVisitService().getVisit(2);
        Encounter labEncounter = null;
        Set<Encounter> encounters = visit.getEncounters();
        for (Encounter encounter : encounters) {
            if (encounter.getEncounterType().getName().equals(ENCOUNTER_TYPE_LAB_RESULT)) {
                labEncounter = encounter;
            }
        }

        assertEquals(2, encounters.size());
        assertNotNull(labEncounter);
        assertEquals("LABSYSTEM", labEncounter.getEncounterProviders().iterator().next().getProvider().getIdentifier());

        Set<Obs> topLevelObs = labEncounter.getAllObs();
        assertEquals(1, topLevelObs.size());
        final Set<Obs> testLevelObs = getGroupMembersForObs(topLevelObs);
        assertEquals(1, testLevelObs.size());
        final Set<Obs> resultMembers = getGroupMembersForObs(testLevelObs);
        assertEquals(4, resultMembers.size());
    }

    @Test
    public void shouldCreateResultEncounterAndObsForPanelWithOnetestWithResultAndOtherValues() throws Exception {
        executeDataSet("labResult.xml");

        String panelConceptUuid = "cfc5056c-3f8e-11e3-968c-0800271c1b75";
        String haemoglobinConceptUuid = "7f7379ba-3ca8-11e3-bf2b-0800271c1b75";

        OpenElisTestDetail test1 = new OpenElisTestDetailBuilder()
                .withPanelUuid(panelConceptUuid)
                .withTestUuid(haemoglobinConceptUuid)
                .withResult("10.5")
                .withProviderUuid("331c6bf8-7846-11e3-a96a-09xD371c1b75")
                .withMinNormal("10")
                .withMaxNormal("20.2")
                .withAbnormal("false")
                .withDateTime("2014-01-30T11:50:18+0530")
                .withResultType("N")
                .build();

        OpenElisAccession openElisAccession = new OpenElisAccessionBuilder().withDateTime("2014-01-30T11:50:18+0530").withTestDetails(new HashSet<>(Arrays.asList(test1))).build();
        openElisAccession.setAccessionUuid("6d0af4567-707a-4629-9850-f15206e63ab0");

        when(httpClient.get(openElisUrl + event.getContent(), OpenElisAccession.class)).thenReturn(openElisAccession);

        openElisAccessionEventWorker.associateTestResultsToOrder(openElisAccession);

        Visit visit = Context.getVisitService().getVisit(2);
        Encounter labEncounter = null;
        Set<Encounter> encounters = visit.getEncounters();
        for (Encounter encounter : encounters) {
            if (encounter.getEncounterType().getName().equals(ENCOUNTER_TYPE_LAB_RESULT)) {
                labEncounter = encounter;
            }
        }

        assertEquals(2, encounters.size());
        assertNotNull(labEncounter);

        Set<Obs> obs = labEncounter.getAllObs();
        assertEquals(1, obs.size());
        Obs panelResultObs = getObsByConceptUuid(obs, panelConceptUuid);
        assertNotNull(panelResultObs);
        Set<Obs> panel1ResultMembers = panelResultObs.getGroupMembers();
        assertEquals(1, panel1ResultMembers.size());

        Set<Obs> topLevelObs = panel1ResultMembers;
        assertEquals(1, topLevelObs.size());
        final Set<Obs> testLevelObs = getGroupMembersForObs(topLevelObs);
        assertEquals(1, testLevelObs.size());
        final Set<Obs> resultMembers = getGroupMembersForObs(testLevelObs);
        assertEquals(4, resultMembers.size());

        Obs testResultObs = getObsByConceptUuid(testLevelObs, haemoglobinConceptUuid);
        assertNotNull(testResultObs);
        assertEquals(4, testResultObs.getGroupMembers().size());

    }

    @Test
    public void shouldCreateResultEncounterAndObsForPanelWithMoreThanOnetestWithResultAndOtherValues() throws Exception {
        //same provider for both tests in panel
        executeDataSet("labResult.xml");

        String panelConceptUuid = "cfc5056c-3f8e-11e3-968c-0800271c1b75";
        String haemoglobinConceptUuid = "7f7379ba-3ca8-11e3-bf2b-0800271c1b75";
        String providerUuid = "331c6bf8-7846-11e3-a96a-09xD371c1b75";
        OpenElisTestDetail test1 = new OpenElisTestDetailBuilder()
                .withPanelUuid(panelConceptUuid)
                .withTestUuid(haemoglobinConceptUuid)
                .withResult("10.5")
                .withProviderUuid(providerUuid)
                .withMinNormal("10")
                .withMaxNormal("20.2")
                .withAbnormal("false")
                .withDateTime("2014-01-30T11:50:18+0530")
                .withResultType("N")
                .build();

        String esrConceptUuid = "a04c36be-3f90-11e3-968c-0800271c1b75";
        OpenElisTestDetail test2 = new OpenElisTestDetailBuilder()
                .withPanelUuid(panelConceptUuid)
                .withTestUuid(esrConceptUuid)
                .withResult("16")
                .withProviderUuid(providerUuid)
                .withMinNormal("10")
                .withMaxNormal("20.2")
                .withAbnormal("false")
                .withDateTime("2014-01-30T11:50:18+0530")
                .withResultType("N")
                .build();

        OpenElisAccession openElisAccession = new OpenElisAccessionBuilder().withTestDetails(new HashSet<>(Arrays.asList(test1, test2))).withDateTime("2014-01-30T11:50:18+0530").build();
        openElisAccession.setAccessionUuid("6d0af4567-707a-4629-9850-f15206e63ab0");

        when(httpClient.get(openElisUrl + event.getContent(), OpenElisAccession.class)).thenReturn(openElisAccession);

        openElisAccessionEventWorker.associateTestResultsToOrder(openElisAccession);

        Visit visit = Context.getVisitService().getVisit(2);
        Encounter labEncounter = null;
        Set<Encounter> encounters = visit.getEncounters();
        for (Encounter encounter : encounters) {
            if (encounter.getEncounterType().getName().equals(ENCOUNTER_TYPE_LAB_RESULT)) {
                labEncounter = encounter;
            }
        }

        assertEquals(2, encounters.size());
        assertNotNull(labEncounter);
        Set<Obs> obs = labEncounter.getAllObs();
        assertEquals(1, obs.size());
        Obs panelResultObs = getObsByConceptUuid(obs, panelConceptUuid);
        assertNotNull(panelResultObs);
        Set<Obs> panel1ResultMembers = panelResultObs.getGroupMembers();
        assertEquals(2, panel1ResultMembers.size());

        Obs haemoglobinTestResultObs = getObsByConceptUuid(panel1ResultMembers, haemoglobinConceptUuid);
        assertNotNull(haemoglobinTestResultObs);
        Set<Obs> testLevelObs = haemoglobinTestResultObs.getGroupMembers();
        assertEquals(1, testLevelObs.size());
        Set<Obs> resultMembers = getGroupMembersForObs(testLevelObs);
        assertEquals(4, resultMembers.size());

        Obs esrTestResultObs = getObsByConceptUuid(panel1ResultMembers, esrConceptUuid);
        assertNotNull(esrTestResultObs);
        testLevelObs = esrTestResultObs.getGroupMembers();
        assertEquals(1, testLevelObs.size());
        resultMembers = getGroupMembersForObs(testLevelObs);
        assertEquals(4, resultMembers.size());
    }

    @Test
    public void shouldCreateResultEncounterForPanelAndTest() throws Exception {
        executeDataSet("labResult.xml");

        String panelConceptUuid = "cfc5056c-3f8e-11e3-968c-0800271c1b75";
        String haemoglobinConceptUuid = "7f7379ba-3ca8-11e3-bf2b-0800271c1b75";
        String providerUuid = "331c6bf8-7846-11e3-a96a-09xD371c1b75";
        OpenElisTestDetail test1 = new OpenElisTestDetailBuilder()
                .withPanelUuid(panelConceptUuid)
                .withTestUuid(haemoglobinConceptUuid)
                .withResult("10.5")
                .withProviderUuid(providerUuid)
                .withMinNormal("10")
                .withMaxNormal("20.2")
                .withAbnormal("false")
                .withDateTime("2014-01-30T11:50:18+0530")
                .withResultType("N")
                .build();

        final String nitroUreaConceptUuid = "7923d0e0-8734-11e3-baa7-0800200c9a66";
        OpenElisTestDetail test2 = new OpenElisTestDetailBuilder()
                .withTestUuid(nitroUreaConceptUuid)
                .withResult("10.5")
                .withProviderUuid(providerUuid)
                .withMinNormal("10")
                .withMaxNormal("20.2")
                .withAbnormal("false")
                .withDateTime("2014-01-30T11:50:18+0530")
                .withResultType("N")
                .build();

        OpenElisAccession openElisAccession = new OpenElisAccessionBuilder().withTestDetails(new HashSet<>(Arrays.asList(test1, test2))).withDateTime("2014-01-30T11:50:18+0530").build();
        openElisAccession.setAccessionUuid("6d0af4567-707a-4629-9850-f15206e63ab0");

        when(httpClient.get(openElisUrl + event.getContent(), OpenElisAccession.class)).thenReturn(openElisAccession);

        openElisAccessionEventWorker.associateTestResultsToOrder(openElisAccession);

        Visit visit = Context.getVisitService().getVisit(2);
        Encounter labEncounter = null;
        Set<Encounter> encounters = visit.getEncounters();
        for (Encounter encounter : encounters) {
            if (encounter.getEncounterType().getName().equals(ENCOUNTER_TYPE_LAB_RESULT)) {
                labEncounter = encounter;
            }
        }

        assertEquals(2, encounters.size());
        assertNotNull(labEncounter);
        Set<Obs> obs = labEncounter.getAllObs();
        assertEquals(2, obs.size());
        Obs panelResultObs = getObsByConceptUuid(obs, panelConceptUuid);
        assertNotNull(panelResultObs);
        Set<Obs> panel1ResultMembers = panelResultObs.getGroupMembers();
        assertEquals(1, panel1ResultMembers.size());

        Obs haemoglobinTestResultObs = getObsByConceptUuid(panel1ResultMembers, haemoglobinConceptUuid);
        assertNotNull(haemoglobinTestResultObs);
        Set<Obs> testLevelObs = haemoglobinTestResultObs.getGroupMembers();
        assertEquals(1, testLevelObs.size());
        Set<Obs> resultMembers = getGroupMembersForObs(testLevelObs);
        assertEquals(4, resultMembers.size());

        Obs nirtoTestResultObs = getObsByConceptUuid(obs, nitroUreaConceptUuid);
        assertNotNull(nitroUreaConceptUuid);
        testLevelObs = nirtoTestResultObs.getGroupMembers();
        assertEquals(1, testLevelObs.size());
        resultMembers = getGroupMembersForObs(testLevelObs);
        assertEquals(4, resultMembers.size());
    }

    @Test
    public void shouldUpdateValueForAlreadyExistingTestResult() throws Exception {
        executeDataSet("labResult.xml");

        final String nitroUreaConceptUuid = "7923d0e0-8734-11e3-baa7-0800200c9a66";
        final String accessionUuid = "6d0af4567-707a-4629-9850-f15206e63ab0";

        OpenElisTestDetail test1 = new OpenElisTestDetailBuilder()
                .withTestUuid(nitroUreaConceptUuid)
                .withResult("10.5")
                .withProviderUuid("331c6bf8-7846-11e3-a96a-09xD371c1b75")
                .withMinNormal("10")
                .withMaxNormal("20.2")
                .withAbnormal("false")
                .withDateTime("2014-01-30T11:50:18+0530")
                .withResultType("N")
                .build();
        OpenElisAccession openElisAccession = new OpenElisAccessionBuilder().withDateTime("2014-01-30T11:50:18+0530").withTestDetails(new HashSet<>(Arrays.asList(test1))).build();
        openElisAccession.setAccessionUuid(accessionUuid);

        when(httpClient.get(openElisUrl + event.getContent(), OpenElisAccession.class)).thenReturn(openElisAccession);

        openElisAccessionEventWorker.associateTestResultsToOrder(openElisAccession);

        // on update of value new openElisAccession response
        test1 = new OpenElisTestDetailBuilder()
                .withTestUuid(nitroUreaConceptUuid)
                .withResult("20")
                .withProviderUuid("331c6bf8-7846-11e3-a96a-09xD371c1b75")
                .withMinNormal("10")
                .withMaxNormal("20.2")
                .withAbnormal("false")
                .withDateTime("2014-01-30T11:55:18+0530") //date changed
                .withResultType("N")
                .build();
        openElisAccession = new OpenElisAccessionBuilder().withDateTime("2014-01-30T11:50:18+0530").withTestDetails(new HashSet<>(Arrays.asList(test1))).build();
        openElisAccession.setAccessionUuid(accessionUuid);

        when(httpClient.get(openElisUrl + event.getContent(), OpenElisAccession.class)).thenReturn(openElisAccession);

        openElisAccessionEventWorker.associateTestResultsToOrder(openElisAccession);

        Visit visit = Context.getVisitService().getVisit(2);
        Encounter labEncounter = null;
        Set<Encounter> encounters = visit.getEncounters();
        for (Encounter encounter : encounters) {
            if (encounter.getEncounterType().getName().equals(ENCOUNTER_TYPE_LAB_RESULT)) {
                labEncounter = encounter;
            }
        }

        assertEquals(2, encounters.size());
        assertNotNull(labEncounter);
        final Set<Obs> allObs = labEncounter.getAllObs(true);
        assertEquals(2, allObs.size());

        ArrayList<Obs> voidedObservations = getVoidedObservations(allObs);
        assertEquals(1, voidedObservations.size());


        Set<Obs> nonVoidedObs = labEncounter.getAllObs(false);
        assertEquals(1, nonVoidedObs.size());

        Obs nitroTestResultObs = getObsByConceptUuid(nonVoidedObs, nitroUreaConceptUuid);
        assertNotNull(nitroTestResultObs);

        Set<Obs> testLevelObs = nitroTestResultObs.getGroupMembers();
        assertEquals(1, testLevelObs.size());
        Set<Obs> resultMembers = getGroupMembersForObs(testLevelObs);
        assertEquals(4, resultMembers.size());
        Obs resultObs = getObsByConceptUuid(resultMembers, nitroUreaConceptUuid);
        assertEquals(new Double(20.0), resultObs.getValueNumeric());
    }

    @Test
    public void shouldUpdateResultForPanelWithMultipleTests() throws Exception {
        //same provider updates all results
        executeDataSet("labResult.xml");

        String panelConceptUuid = "cfc5056c-3f8e-11e3-968c-0800271c1b75";
        String haemoglobinConceptUuid = "7f7379ba-3ca8-11e3-bf2b-0800271c1b75";
        String providerUuid = "331c6bf8-7846-11e3-a96a-09xD371c1b75";
        OpenElisTestDetail hbTest = new OpenElisTestDetailBuilder()
                .withPanelUuid(panelConceptUuid)
                .withTestUuid(haemoglobinConceptUuid)
                .withResult("10.5")
                .withProviderUuid(providerUuid)
                .withMinNormal("10")
                .withMaxNormal("20.2")
                .withAbnormal("false")
                .withDateTime("2014-01-30T11:50:18+0530")
                .withResultType("N")
                .build();

        String esrConceptUuid = "a04c36be-3f90-11e3-968c-0800271c1b75";
        OpenElisTestDetail esrTest = new OpenElisTestDetailBuilder()
                .withPanelUuid(panelConceptUuid)
                .withTestUuid(esrConceptUuid)
                .build();

        OpenElisAccession openElisAccession = new OpenElisAccessionBuilder().withTestDetails(new HashSet<>(Arrays.asList(hbTest, esrTest))).withDateTime("2014-01-30T11:50:18+0530").build();
        openElisAccession.setAccessionUuid("6d0af4567-707a-4629-9850-f15206e63ab0");

        when(httpClient.get(openElisUrl + event.getContent(), OpenElisAccession.class)).thenReturn(openElisAccession);

        openElisAccessionEventWorker.associateTestResultsToOrder(openElisAccession);

        Visit visit = Context.getVisitService().getVisit(2);
        Encounter labEncounter = null;
        Set<Encounter> encounters = visit.getEncounters();
        for (Encounter encounter : encounters) {
            if (encounter.getEncounterType().getName().equals(ENCOUNTER_TYPE_LAB_RESULT)) {
                labEncounter = encounter;
            }
        }

        assertEquals(2, encounters.size());
        assertNotNull(labEncounter);
        Set<Obs> obs = labEncounter.getAllObs();
        assertEquals(1, obs.size());
        Obs panelResultObs = getObsByConceptUuid(obs, panelConceptUuid);
        assertNotNull(panelResultObs);
        Set<Obs> panel1ResultMembers = panelResultObs.getGroupMembers();
        assertEquals(1, panel1ResultMembers.size()); //only one test has results


        OpenElisTestDetail hbTestUpdated = new OpenElisTestDetailBuilder()
                .withPanelUuid(panelConceptUuid)
                .withTestUuid(haemoglobinConceptUuid)
                .withResult("9.0")
                .withProviderUuid(providerUuid)
                .withMinNormal("10")
                .withMaxNormal("20.2")
                .withAbnormal("true")
                .withDateTime("2014-01-30T12:50:18+0530")
                .withResultType("N")
                .build();

        OpenElisTestDetail esrTestUpdated = new OpenElisTestDetailBuilder()
                .withPanelUuid(panelConceptUuid)
                .withTestUuid(esrConceptUuid)
                .withResult("16")
                .withProviderUuid(providerUuid)
                .withMinNormal("10")
                .withMaxNormal("20.2")
                .withAbnormal("false")
                .withDateTime("2014-01-30T12:50:18+0530")
                .withResultType("N")
                .build();

        openElisAccession = new OpenElisAccessionBuilder().withTestDetails(new HashSet<>(Arrays.asList(hbTestUpdated, esrTestUpdated))).withDateTime("2014-01-30T11:50:18+0530").build();
        openElisAccession.setAccessionUuid("6d0af4567-707a-4629-9850-f15206e63ab0");
        when(httpClient.get(openElisUrl + event.getContent(), OpenElisAccession.class)).thenReturn(openElisAccession);
        openElisAccessionEventWorker.associateTestResultsToOrder(openElisAccession);

        visit = Context.getVisitService().getVisit(2);
        labEncounter = null;
        encounters = visit.getEncounters();
        for (Encounter encounter : encounters) {
            if (encounter.getEncounterType().getName().equals(ENCOUNTER_TYPE_LAB_RESULT)) {
                labEncounter = encounter;
            }
        }

        assertEquals(2, encounters.size());
        assertNotNull(labEncounter);
        Set<Obs> allObs = labEncounter.getAllObs(true);
        assertEquals(1, allObs.size());
        Obs panelObs = getObsByConceptUuid(allObs, panelConceptUuid);
        final Set<Obs> testObservations = panelObs.getGroupMembers(true);
        assertEquals(3, testObservations.size()); //one voided, 1 updated, 1 new
        assertEquals(1, getVoidedObservations(testObservations).size());
        final Set<Obs> unvoidedObservations = panelObs.getGroupMembers(false);
        assertEquals(2, unvoidedObservations.size());
        final Obs resultsForHaemoglobin = getObsByConceptUuid(unvoidedObservations, haemoglobinConceptUuid);
        assertEquals(new Double(9.0), getConceptResultObs(resultsForHaemoglobin.getGroupMembers(), haemoglobinConceptUuid).getValueNumeric());

    }

    @Test
    public void shouldUpdateResultForPanelWithMultipleTestsWithDiffProviders() throws Exception {
        executeDataSet("labResult.xml");

        String panelConceptUuid = "cfc5056c-3f8e-11e3-968c-0800271c1b75";
        String haemoglobinConceptUuid = "7f7379ba-3ca8-11e3-bf2b-0800271c1b75";
        String labTechProviderUuid = "331c6bf8-7846-11e3-a96a-09xD371c1b75";
        String systemProviderUuid = "331c6bf8-7846-11e3-a96a-0909871c1b75";

        OpenElisTestDetail hbTest = new OpenElisTestDetailBuilder()
                .withPanelUuid(panelConceptUuid)
                .withTestUuid(haemoglobinConceptUuid)
                .withResult("10.5")
                .withProviderUuid(labTechProviderUuid)
                .withMinNormal("10")
                .withMaxNormal("20.2")
                .withAbnormal("false")
                .withDateTime("2014-01-30T11:50:18+0530")
                .withResultType("N")
                .build();

        String esrConceptUuid = "a04c36be-3f90-11e3-968c-0800271c1b75";
        OpenElisTestDetail esrTest = new OpenElisTestDetailBuilder()
                .withPanelUuid(panelConceptUuid)
                .withTestUuid(esrConceptUuid)
                .build();

        OpenElisAccession openElisAccession = new OpenElisAccessionBuilder().withTestDetails(new HashSet<>(Arrays.asList(hbTest, esrTest))).withDateTime("2014-01-30T11:50:18+0530").build();
        String accessionUuid = "6d0af4567-707a-4629-9850-f15206e63ab0";
        openElisAccession.setAccessionUuid(accessionUuid);

        Event firstEvent = stubHttpClientToGetOpenElisAccession(accessionUuid, openElisAccession);
        openElisAccessionEventWorker.process(firstEvent);

        Visit visit = Context.getVisitService().getVisit(2);
        Encounter labEncounter = null;
        Set<Encounter> encounters = visit.getEncounters();
        for (Encounter encounter : encounters) {
            if (encounter.getEncounterType().getName().equals(ENCOUNTER_TYPE_LAB_RESULT)) {
                labEncounter = encounter;
            }
        }

        assertEquals(2, encounters.size());
        assertNotNull(labEncounter);
        Set<Obs> obs = labEncounter.getAllObs();
        assertEquals(1, obs.size());
        Obs panelResultObs = getObsByConceptUuid(obs, panelConceptUuid);
        assertNotNull(panelResultObs);
        Set<Obs> panel1ResultMembers = panelResultObs.getGroupMembers();
        assertEquals(1, panel1ResultMembers.size());
        assertNotNull(getObsByConceptUuid(panel1ResultMembers, haemoglobinConceptUuid));


        OpenElisTestDetail hbTestUpdated = new OpenElisTestDetailBuilder()
                .withPanelUuid(panelConceptUuid)
                .withTestUuid(haemoglobinConceptUuid)
                .withResult("9.0")
                .withProviderUuid(systemProviderUuid)
                .withMinNormal("10")
                .withMaxNormal("20.2")
                .withAbnormal("true")
                .withDateTime("2014-01-30T12:50:18+0530")
                .withResultType("N")
                .build();

        OpenElisTestDetail esrTestUpdated = new OpenElisTestDetailBuilder()
                .withPanelUuid(panelConceptUuid)
                .withTestUuid(esrConceptUuid)
                .withResult("16")
                .withProviderUuid(systemProviderUuid)
                .withMinNormal("10")
                .withMaxNormal("20.2")
                .withAbnormal("false")
                .withDateTime("2014-01-30T12:50:18+0530")
                .withResultType("N")
                .build();

        openElisAccession = new OpenElisAccessionBuilder().withTestDetails(new HashSet<>(Arrays.asList(hbTestUpdated, esrTestUpdated))).withDateTime("2014-01-30T11:50:18+0530").build();
        openElisAccession.setAccessionUuid(accessionUuid);
        firstEvent = stubHttpClientToGetOpenElisAccession(accessionUuid, openElisAccession);
        openElisAccessionEventWorker.process(firstEvent);

        visit = Context.getVisitService().getVisit(2);
        List<Encounter> labEncounters = new ArrayList<>();
        encounters = visit.getEncounters();
        for (Encounter encounter : encounters) {
            if (encounter.getEncounterType().getName().equals(ENCOUNTER_TYPE_LAB_RESULT)) {
                labEncounters.add(encounter);
            }
        }

        assertEquals(3, encounters.size());
        assertEquals(2, labEncounters.size());

        List<Encounter> encountersByLabTech = findEncountersForProvider(labEncounters, labTechProviderUuid);
        assertEquals(1, encountersByLabTech.size());
        final Set<Obs> panelObsByLabTech = encountersByLabTech.get(0).getAllObs(true);
        assertEquals(1, panelObsByLabTech.size());
        Set<Obs> topLevelTestsByLabTech = panelObsByLabTech.iterator().next().getGroupMembers(true);
        assertEquals(1, topLevelTestsByLabTech.size());
        final ArrayList<Obs> voidedObservations = getVoidedObservations(topLevelTestsByLabTech);
        assertEquals(1, voidedObservations.size());
        final Set<Obs> testObs = voidedObservations.get(0).getGroupMembers(true);
        assertEquals(1, testObs.size());
        final Set<Obs> testResults = testObs.iterator().next().getGroupMembers(true);
        for (Obs testOb : testResults) {
            assertTrue(testOb.getVoided());
        }

        List<Encounter> encountersBySystem = findEncountersForProvider(labEncounters, systemProviderUuid);
        assertEquals(1, encountersBySystem.size());
        final Set<Obs> panelObsBySystem = encountersBySystem.get(0).getAllObs(true);
        assertEquals(1, panelObsBySystem.size());
        Set<Obs> topLevelPanelTestsBySystem = panelObsBySystem.iterator().next().getGroupMembers(true);
        assertEquals(2, topLevelPanelTestsBySystem.size());
        assertEquals(0, getVoidedObservations(topLevelPanelTestsBySystem).size());
    }

    @Test
    public void shouldNotVoidObsIfTimeDidntChange() throws Exception {
        executeDataSet("labResult.xml");

        OpenElisTestDetail test1 = new OpenElisTestDetailBuilder()
                .withTestUuid("7923d0e0-8734-11e3-baa7-0800200c9a66")
                .withResult("10.5")
                .withProviderUuid("331c6bf8-7846-11e3-a96a-09xD371c1b75")
                .withMinNormal("10")
                .withMaxNormal("20.2")
                .withAbnormal("false")
                .withDateTime("2014-01-30T11:50:18+0530")
                .withResultType("N")
                .build();
        OpenElisAccession openElisAccession = new OpenElisAccessionBuilder().withDateTime("2014-01-30T11:50:18+0530").withTestDetails(new HashSet<>(Arrays.asList(test1))).build();
        openElisAccession.setAccessionUuid("6d0af4567-707a-4629-9850-f15206e63ab0");

        when(httpClient.get(openElisUrl + event.getContent(), OpenElisAccession.class)).thenReturn(openElisAccession);

        openElisAccessionEventWorker.associateTestResultsToOrder(openElisAccession);

        Visit visit = Context.getVisitService().getVisit(2);
        Encounter labEncounter = null;
        Set<Encounter> encounters = visit.getEncounters();
        for (Encounter encounter : encounters) {
            if (encounter.getEncounterType().getName().equals(ENCOUNTER_TYPE_LAB_RESULT)) {
                labEncounter = encounter;
            }
        }

        assertEquals(2, encounters.size());
        assertNotNull(labEncounter);
        Set<Obs> obs = labEncounter.getAllObs();
        assertEquals(1, obs.size());

        openElisAccessionEventWorker.associateTestResultsToOrder(openElisAccession);
        visit = Context.getVisitService().getVisit(2);
        labEncounter = null;
        encounters = visit.getEncounters();
        for (Encounter encounter : encounters) {
            if (encounter.getEncounterType().getName().equals(ENCOUNTER_TYPE_LAB_RESULT)) {
                labEncounter = encounter;
            }
        }

        assertEquals(2, encounters.size());
        assertNotNull(labEncounter);
        obs = labEncounter.getAllObs(true);
        assertEquals(0, getVoidedObservations(obs).size());
        assertEquals(1, obs.size());
    }

    @Test
    public void shouldCreateOrderEncounterAndAssociateResultsAndLabNotesForNewAccession() throws Exception {
        executeDataSet("labResult.xml");
        EncounterService encounterService = Context.getEncounterService();

        String panelConceptUuid = "cfc5056c-3f8e-11e3-968c-0800271c1b75";
        String haemoglobinConceptUuid = "7f7379ba-3ca8-11e3-bf2b-0800271c1b75";
        String accessionUuid = "6xfe4567-707a-4629-9850-f15206e9b0eX";
        String patientUuidWithNoOrders = "75e04d42-3ca8-11e3-bf2b-ab87271c1b75";

        assertNull(encounterService.getEncounterByUuid(accessionUuid));

        OpenElisTestDetail test1 = new OpenElisTestDetailBuilder()
                .withPanelUuid(panelConceptUuid)
                .withTestUuid(haemoglobinConceptUuid)
                .withResult("10.5")
                .withProviderUuid("331c6bf8-7846-11e3-a96a-09xD371c1b75")
                .withMinNormal("10")
                .withMaxNormal("20.2")
                .withAbnormal("false")
                .withDateTime("2014-01-30T11:50:18+0530")
                .withResultType("N")
                .build();

        OpenElisAccession openElisAccession = new OpenElisAccessionBuilder()
                .withDateTime("2014-01-30T11:50:18+0530")
                .withTestDetails(new HashSet<>(Arrays.asList(test1)))
                .withPatientUuid(patientUuidWithNoOrders)
                .withAccessionNotes(new OpenElisAccessionNote("Note1", "aa1c6bf8-7846-11e3-a96a-09xD371c1b75", "2014-01-30T11:50:18+0530"),
                                    new OpenElisAccessionNote("Note2", "aa1c6bf8-7846-11e3-a96a-09xD371c1b75", "2014-01-30T11:50:18+0530"))
                .build();
        openElisAccession.setAccessionUuid(accessionUuid);

        Event firstEvent = stubHttpClientToGetOpenElisAccession(accessionUuid, openElisAccession);
        openElisAccessionEventWorker.process(firstEvent);
        Encounter orderEncounter = encounterService.getEncounterByUuid(accessionUuid);
        assertNotNull(orderEncounter);
        Visit visit = orderEncounter.getVisit();
        Encounter labEncounter = null;
        Encounter notesEncounter = null;
        List<Encounter> encounters = encounterService.getEncountersByPatient(visit.getPatient());


        for (Encounter encounter : encounters) {
            if (encounter.getEncounterType().getName().equals(ENCOUNTER_TYPE_LAB_RESULT)) {
                labEncounter = encounter;
            }
            else if (encounter.getEncounterType().getName().equals(VALIDATION_NOTES)) {
                notesEncounter = encounter;
            }
        }

        assertEquals(3, encounters.size());

        Set<Obs> obs = labEncounter.getAllObs();
        assertEquals(1, obs.size());
        Obs panelResultObs = getObsByConceptUuid(obs, panelConceptUuid);
        assertNotNull(panelResultObs);
        Set<Obs> panel1ResultMembers = panelResultObs.getGroupMembers();
        assertEquals(1, panel1ResultMembers.size());

        Set<Obs> topLevelObs = panel1ResultMembers;
        assertEquals(1, topLevelObs.size());
        final Set<Obs> testLevelObs = getGroupMembersForObs(topLevelObs);
        assertEquals(1, testLevelObs.size());
        final Set<Obs> resultMembers = getGroupMembersForObs(testLevelObs);
        assertEquals(4, resultMembers.size());

        Obs testResultObs = getObsByConceptUuid(testLevelObs, haemoglobinConceptUuid);
        assertNotNull(testResultObs);
        assertEquals(4, testResultObs.getGroupMembers().size());
        assertNotNull(notesEncounter);
        assertEquals("aa1c6bf8-7846-11e3-a96a-09xD371c1b75",ProviderHelper.getProviderFrom(notesEncounter).getUuid());
        Set<Obs> notesObservations = notesEncounter.getObs();

        assertEquals(3, notesObservations.size());
        boolean containsAccessionUuidObservation = false;
        for (Obs notesObservation : notesObservations) {

            if(notesObservation.getConcept().getName().getName().equals(OpenElisAccessionEventWorker.ACCESSION_UUID_CONCEPT)){
                containsAccessionUuidObservation = true;
                assertEquals("6xfe4567-707a-4629-9850-f15206e9b0eX", notesObservation.getValueText());
            }
            else{
                assertEquals(OpenElisAccessionEventWorker.LAB_MANAGER_NOTES, notesObservation.getConcept().getName().getName());
                assertTrue(Arrays.asList("Note1", "6xfe4567-707a-4629-9850-f15206e9b0eX","Note2").contains(notesObservation.getValueText()));
            }
        }
        assertTrue(containsAccessionUuidObservation);

    }

    @Test
    public void shouldUpdateLabNotesForAccession() throws Exception {
        executeDataSet("labResult.xml");
        EncounterService encounterService = Context.getEncounterService();

        String accessionUuid = "6g0bf6767-707a-4329-9850-f15206e63ab0";
        String patientUuidWithAccessionNotes = "86e04d42-3ca8-11e3-bf2b-0x7009861b97";

        String panelConceptUuid = "cfc5056c-3f8e-11e3-968c-0800271c1b75";
        String haemoglobinConceptUuid = "7f7379ba-3ca8-11e3-bf2b-0800271c1b75";

        OpenElisTestDetail test1 = new OpenElisTestDetailBuilder()
                .withPanelUuid(panelConceptUuid)
                .withTestUuid(haemoglobinConceptUuid)
                .withResult("10.5")
                .withProviderUuid("331c6bf8-7846-11e3-a96a-09xD371c1b75")
                .withMinNormal("10")
                .withMaxNormal("20.2")
                .withAbnormal("false")
                .withDateTime("2014-01-30T11:50:18+0530")
                .withResultType("N")
                .build();

        OpenElisAccession openElisAccession = new OpenElisAccessionBuilder()
                .withDateTime("2014-01-30T11:50:18+0530")
                .withTestDetails(new HashSet<>(Arrays.asList(test1)))
                .withPatientUuid(patientUuidWithAccessionNotes)
                .withAccessionNotes(new OpenElisAccessionNote("Note1", "aa1c6bf8-7846-11e3-a96a-09xD371c1b75", "2014-01-30T11:50:18+0530"),
                        new OpenElisAccessionNote("Note2", "aa1c6bf8-7846-11e3-a96a-09xD371c1b75", "2014-01-30T11:50:18+0530"))
                .build();
        openElisAccession.setAccessionUuid(accessionUuid);
        Encounter notesEncounter1 = encounterService.getEncounter(36);
        Set<Encounter> encounters = notesEncounter1.getVisit().getEncounters();
        assertEquals(2, encounters.size());

        assertEquals(2, notesEncounter1.getObs().size());
        Event firstEvent = stubHttpClientToGetOpenElisAccession(accessionUuid, openElisAccession);
        openElisAccessionEventWorker.process(firstEvent);
        encounters = notesEncounter1.getVisit().getEncounters();
        notesEncounter1 = encounterService.getEncounter(36);
        assertEquals(4, encounters.size());
        assertEquals(3, notesEncounter1.getObs().size());
    }

    @Test
    public void shouldMatchLabNotesForAccessionWithDefaultProvider() throws Exception {
        executeDataSet("labResult.xml");
        EncounterService encounterService = Context.getEncounterService();

        String accessionUuid = "6g0bf6767-707a-4329-9850-f15206e63ab0";
        String patientUuidWithAccessionNotes = "86e04d42-3ca8-11e3-bf2b-0x7009861b97";

        String panelConceptUuid = "cfc5056c-3f8e-11e3-968c-0800271c1b75";
        String haemoglobinConceptUuid = "7f7379ba-3ca8-11e3-bf2b-0800271c1b75";

        OpenElisTestDetail test1 = new OpenElisTestDetailBuilder()
                .withPanelUuid(panelConceptUuid)
                .withTestUuid(haemoglobinConceptUuid)
                .withResult("10.5")
                .withProviderUuid("331c6bf8-7846-11e3-a96a-09xD371c1b75")
                .withMinNormal("10")
                .withMaxNormal("20.2")
                .withAbnormal("false")
                .withDateTime("2014-01-30T11:50:18+0530")
                .withResultType("N")
                .build();

        OpenElisAccession openElisAccession = new OpenElisAccessionBuilder()
                .withDateTime("2014-01-30T11:50:18+0530")
                .withTestDetails(new HashSet<>(Arrays.asList(test1)))
                .withPatientUuid(patientUuidWithAccessionNotes)
                .withAccessionNotes(new OpenElisAccessionNote("Note1", "non-existent-provider", "2014-01-30T11:50:18+0530"),
                        new OpenElisAccessionNote("Note1", "aa1c6bf8-7846-11e3-a96a-09xD371c1b75", "2014-01-30T11:50:18+0530"))
                .build();
        openElisAccession.setAccessionUuid(accessionUuid);
        Encounter notesEncounter1 = encounterService.getEncounter(36);
        Encounter notesEncounter2 = encounterService.getEncounter(38);

        Set<Encounter> encounters = notesEncounter1.getVisit().getEncounters();
        assertEquals(2, encounters.size());

        assertEquals(2, notesEncounter1.getObs().size());
        assertEquals(1, notesEncounter2.getObs().size());

        Event firstEvent = stubHttpClientToGetOpenElisAccession(accessionUuid, openElisAccession);
        openElisAccessionEventWorker.process(firstEvent);

        encounters = notesEncounter1.getVisit().getEncounters();
        notesEncounter1 = encounterService.getEncounter(36);
        notesEncounter2 = encounterService.getEncounter(38);
        assertEquals(4, encounters.size());
        assertEquals(2, notesEncounter1.getObs().size());
        assertEquals(2, notesEncounter2.getObs().size());
    }

    @Test
    public void shouldCreateNewLabNotesEncounterForAccessionWithExistingProvider() throws Exception {
        executeDataSet("labResult.xml");
        EncounterService encounterService = Context.getEncounterService();
        VisitService visitService = Context.getVisitService();

        String accessionUuid = "6g0bf6767-707a-4329-9850-f15206e63ab0";
        String patientUuidWithAccessionNotes = "86e04d42-3ca8-11e3-bf2b-0x7009861b97";

        OpenElisAccession openElisAccession = new OpenElisAccessionBuilder()
                .withDateTime("2014-01-30T11:50:18+0530")
                .withTestDetails(new HashSet<OpenElisTestDetail>())
                .withPatientUuid(patientUuidWithAccessionNotes)
                .withAccessionNotes(new OpenElisAccessionNote("Note1", "331c6bf8-7846-11e3-a96a-09xD371c1b75", "2014-01-30T11:50:18+0530"),
                        new OpenElisAccessionNote("Note1", "aa1c6bf8-7846-11e3-a96a-09xD371c1b75", "2014-01-30T11:50:18+0530"))
                .build();
        openElisAccession.setAccessionUuid(accessionUuid);
        Encounter notesEncounter1 = encounterService.getEncounter(36);

        List<Encounter> encounters = encounterService.getEncountersByPatientId(3);
        assertEquals(2, encounters.size());
        assertEquals(2, notesEncounter1.getObs().size());

        Event firstEvent = stubHttpClientToGetOpenElisAccession(accessionUuid, openElisAccession);
        openElisAccessionEventWorker.process(firstEvent);
        List<Visit> visitsByPatient = visitService.getVisitsByPatient(notesEncounter1.getPatient());
        assertEquals(1,visitsByPatient.size());

        encounters = encounterService.getEncountersByPatientId(3);
        notesEncounter1 = encounterService.getEncounter(36);
        assertEquals(4, encounters.size());
        assertEquals(2, notesEncounter1.getObs().size());

        Encounter newNoteEncounter = null;
        for(Encounter encounter : encounters){
            if(encounter.getEncounterType().getName().equals(VALIDATION_NOTES) && encounter.getId()!= 36 && encounter.getId()!= 38 ){
                newNoteEncounter = encounter;
                break;
            }
        }
        assertNotNull(newNoteEncounter);
        assertEquals((Integer) 23, ProviderHelper.getProviderFrom(newNoteEncounter).getId());
        assertEquals(2, newNoteEncounter.getObs().size());
        for (Obs obs : newNoteEncounter.getObs()) {
            assertTrue(Arrays.asList("Note1", "6g0bf6767-707a-4329-9850-f15206e63ab0").contains(obs.getValueText()));
        }

    }

    @Test
    public void shouldCreateOrderEncounterAndAssociateResultsForNewAccessionWhenTheVisitToOrderEncounterIsClosed() throws Exception {
        executeDataSet("labResult.xml");
        EncounterService encounterService = Context.getEncounterService();

        String panelConceptUuid = "cfc5056c-3f8e-11e3-968c-0800271c1b75";
        String haemoglobinConceptUuid = "7f7379ba-3ca8-11e3-bf2b-0800271c1b75";
        String accessionUuid = "6xfe4567-707a-4629-9850-f15206e9b0eX";
        String patientUuidWithNoOrders = "75e04d42-3ca8-11e3-bf2b-ab87271c1b75";

        assertNull(encounterService.getEncounterByUuid(accessionUuid));

        OpenElisTestDetail test1 = new OpenElisTestDetailBuilder()
                .withPanelUuid(panelConceptUuid)
                .withTestUuid(haemoglobinConceptUuid)
                .build();

        OpenElisAccession openElisAccession = new OpenElisAccessionBuilder()
                .withDateTime("2014-01-30T11:50:18+0530")
                .withTestDetails(new HashSet<>(Arrays.asList(test1)))
                .withPatientUuid(patientUuidWithNoOrders)
                .build();
        openElisAccession.setAccessionUuid(accessionUuid);

        Event firstEvent = stubHttpClientToGetOpenElisAccession(accessionUuid, openElisAccession);
        openElisAccessionEventWorker.process(firstEvent);

        Visit visit = encounterService.getEncounterByUuid(accessionUuid).getVisit();
        Encounter labEncounter = null;
        Set<Encounter> encounters = visit.getEncounters();
        for (Encounter encounter : encounters) {
            if (encounter.getEncounterType().getName().equals(ENCOUNTER_TYPE_LAB_RESULT)) {
                labEncounter = encounter;
            }
        }

        assertEquals(1, encounters.size());
        assertNull(labEncounter);

        visit.setStopDatetime(new Date());
        Context.getVisitService().saveVisit(visit);

        test1 = new OpenElisTestDetailBuilder()
                .withPanelUuid(panelConceptUuid)
                .withTestUuid(haemoglobinConceptUuid)
                .withResult("10.5")
                .withProviderUuid("331c6bf8-7846-11e3-a96a-09xD371c1b75")
                .withMinNormal("10")
                .withMaxNormal("20.2")
                .withAbnormal("false")
                .withDateTime("2014-01-30T11:50:18+0530")
                .withResultType("N")
                .build();

        openElisAccession = new OpenElisAccessionBuilder()
                .withDateTime("2014-01-30T11:50:18+0530")
                .withTestDetails(new HashSet<>(Arrays.asList(test1)))
                .withPatientUuid(patientUuidWithNoOrders)
                .build();
        openElisAccession.setAccessionUuid(accessionUuid);

        firstEvent = stubHttpClientToGetOpenElisAccession(accessionUuid, openElisAccession);
        openElisAccessionEventWorker.process(firstEvent);

        Encounter orderEncounter = encounterService.getEncounterByUuid(accessionUuid);
        labEncounter = null;
        EncounterType labResultEncounterType = encounterService.getEncounterType(ENCOUNTER_TYPE_LAB_RESULT);

        List<Encounter> encounterList = encounterService.getEncounters(orderEncounter.getPatient(),
                null, orderEncounter.getEncounterDatetime(), null, null,
                Arrays.asList(labResultEncounterType),
                null, null, null, false);

        assertEquals(1, encounterList.size());
        labEncounter = encounterList.get(0);
        assertNotNull(labEncounter);

        Set<Obs> obs = labEncounter.getAllObs();
        assertEquals(1, obs.size());
        Obs panelResultObs = getObsByConceptUuid(obs, panelConceptUuid);
        assertNotNull(panelResultObs);
        Set<Obs> panel1ResultMembers = panelResultObs.getGroupMembers();
        assertEquals(1, panel1ResultMembers.size());

        Set<Obs> topLevelObs = panel1ResultMembers;
        assertEquals(1, topLevelObs.size());
        final Set<Obs> testLevelObs = getGroupMembersForObs(topLevelObs);
        assertEquals(1, testLevelObs.size());
        final Set<Obs> resultMembers = getGroupMembersForObs(testLevelObs);
        assertEquals(4, resultMembers.size());

        Obs testResultObs = getObsByConceptUuid(testLevelObs, haemoglobinConceptUuid);
        assertNotNull(testResultObs);
        assertEquals(4, testResultObs.getGroupMembers().size());

    }

    private Visit getVisitByStartDate(List<Visit> visits, Date date) {
        for (Visit visit : visits) {
            if ((visit.getStartDatetime().compareTo(date) <= 0) &&
                    ((visit.getStopDatetime() == null) || (visit.getStopDatetime().compareTo(date) >= 0))) {
                return visit;
            }
        }
        return null;
    }

    private Event stubHttpClientToGetOpenElisAccession(String accessionUuid, OpenElisAccession openElisAccession) throws java.io.IOException {
        Event firstEvent = new Event("id", "openelis/accession/" + accessionUuid, "title", "feedUri");
        when(httpClient.get(properties.getOpenElisUri() + firstEvent.getContent(), OpenElisAccession.class)).thenReturn(openElisAccession);
        return firstEvent;
    }

    private Obs getObsByConceptUuid(Set<Obs> panel1ResultMembers, String conceptUuid) {
        Obs testResultObs = null;
        for (Obs testObs : panel1ResultMembers) {
            if (testObs.getConcept().getUuid().equals(conceptUuid)) {
                testResultObs = testObs;
                break;
            }
        }
        return testResultObs;
    }

    private List<Encounter> findEncountersForProvider(List<Encounter> labEncounters, String providerUuid) {
        List<Encounter> encounters = new ArrayList<>();
        for (Encounter encounter : labEncounters) {
            String encProviderUuid = encounter.getEncounterProviders().iterator().next().getProvider().getUuid();
            if (encProviderUuid.equals(providerUuid)) {
                encounters.add(encounter);
            }
        }
        return encounters;
    }

    private Obs getConceptResultObs(Set<Obs> members, String conceptUuid) {
        Obs obs = getObsByConceptUuid(members, conceptUuid);
        return getObsByConceptUuid(obs.getGroupMembers(), conceptUuid);
    }

    private ArrayList<Obs> getVoidedObservations(Set<Obs> allObs) {
        ArrayList<Obs> voidedObs = new ArrayList<Obs>();
        for (Obs obs : allObs) {
            if (obs.isVoided()) {
                //for individual test
                voidedObs.add(obs);
            } else if (obs.getConcept().isSet()) {
                //for tests in panel
                for (Obs member : obs.getGroupMembers()) {
                    if (member.isVoided()) {
                        voidedObs.add(member);
                    }
                }
            }
        }
        return voidedObs;
    }

    private Set<Obs> getGroupMembersForObs(Set<Obs> obs) {
        Obs testObs = obs.iterator().next();
        return testObs.getGroupMembers();
    }


}
