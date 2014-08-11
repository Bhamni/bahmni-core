package org.bahmni.module.referencedatafeedclient.worker;

import org.bahmni.module.referencedatafeedclient.*;
import org.bahmni.module.referencedatafeedclient.domain.*;
import org.bahmni.module.referencedatafeedclient.domain.Test;
import org.bahmni.module.referencedatafeedclient.service.*;
import org.bahmni.webclients.*;
import org.ict4h.atomfeed.client.domain.*;
import org.junit.*;
import org.mockito.*;
import org.mockito.Mock;
import org.openmrs.*;
import org.openmrs.api.*;
import org.openmrs.web.test.*;
import org.springframework.beans.factory.annotation.*;

import java.io.*;
import java.util.*;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.*;

@org.springframework.test.context.ContextConfiguration(locations = {"classpath:TestingApplicationContext.xml"}, inheritLocations = true)
public class TestEventWorkerIT extends BaseModuleWebContextSensitiveTest {
    @Mock
    private HttpClient httpClient;
    @Mock
    private ReferenceDataFeedProperties referenceDataFeedProperties;
    private final String referenceDataUri = "http://localhost";

    @Autowired
    private ConceptService conceptService;
    @Autowired
    private ReferenceDataConceptService referenceDataConceptService;
    @Autowired
    private EventWorkerUtility eventWorkerUtility;

    private TestEventWorker testEventWorker;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        testEventWorker = new TestEventWorker(httpClient, referenceDataFeedProperties, conceptService, referenceDataConceptService, new EventWorkerUtility(conceptService));
        when(referenceDataFeedProperties.getReferenceDataUri()).thenReturn(referenceDataUri);
        executeDataSet("testEventWorkerTestData.xml");
    }

    @org.junit.Test
    @Ignore
    public void shouldCreateNewConceptForGivenTest() throws Exception {
        Event event = new Event("xxxx-yyyyy", "/reference-data/test/8471dbe5-0465-4eac-94ba-8f8708f3f529");
        Sample sample = new Sample("dc8ac8c0-8716-11e3-baa7-0800200c9a66");
        Department department = new Department("e060cf44-3d3d-11e3-bf2b-0800271c1b75");
        Test test = new Test("59474920-8734-11e3-baa7-0800200c9a66", "Haemoglobin", "Haemoglobin Description", "Hb", "Numeric", sample, department, true, 12);

        when(httpClient.get(referenceDataUri + event.getContent(), Test.class)).thenReturn(test);

        testEventWorker.process(event);

        Concept testConcept = conceptService.getConceptByUuid(test.getId());
        assertNotNull(testConcept);
        assertEquals(2, testConcept.getNames().size());
        assertEquals(test.getName(), testConcept.getFullySpecifiedName(Locale.ENGLISH).getName());
        assertEquals(test.getShortName(), testConcept.getShortNameInLocale(Locale.ENGLISH).getName());
        assertEquals(1, testConcept.getDescriptions().size());
        assertEquals(test.getDescription(), testConcept.getDescription().getDescription());
        assertEquals(false, testConcept.isRetired());
        assertEquals(ConceptDatatype.NUMERIC_UUID, testConcept.getDatatype().getUuid());
        assertEquals(TestEventWorker.TEST, testConcept.getConceptClass().getName());
        Concept sampleConcept = conceptService.getConceptByUuid(sample.getId());
        assertTrue(sampleConcept.getSetMembers().contains(testConcept));
        Concept departmentConcept = conceptService.getConceptByUuid(department.getId());
        assertTrue(departmentConcept.getSetMembers().contains(testConcept));

        Concept allTestsAndPanelsConcept = conceptService.getConceptByName(TestEventWorker.ALL_TESTS_AND_PANELS);
        ConceptSet matchingConceptSet = eventWorkerUtility.getMatchingConceptSet(allTestsAndPanelsConcept.getConceptSets(), testConcept);
        assertNotNull(matchingConceptSet);
        assertEquals(12, matchingConceptSet.getSortWeight(), 0.001);
    }

    @org.junit.Test
    @Ignore
    public void shouldUpdateConceptForGivenTest() throws Exception {
        Event event = new Event("xxxx-yyyyy", "/reference-data/test/4923d0e0-8734-11e3-baa7-0800200c9a66");
        Sample sample = new Sample("dc8ac8c0-8716-11e3-baa7-0800200c9a66");
        Department department = new Department("e060cf44-3d3d-11e3-bf2b-0800271c1b75");
        Test test = new Test("4923d0e0-8734-11e3-baa7-0800200c9a66", "Blood Group Updated", "Blood Group Description updated", null, "NNNN", sample, department, false, 12);
        when(httpClient.get(referenceDataUri+event.getContent(), Test.class)).thenReturn(test);

        testEventWorker.process(event);

        Concept testConcept = conceptService.getConceptByUuid(test.getId());
        assertNotNull(testConcept);
        assertEquals(1, testConcept.getNames().size());
        assertEquals(test.getName(), testConcept.getFullySpecifiedName(Locale.ENGLISH).getName());
        assertEquals(test.getShortName(), testConcept.getShortNameInLocale(Locale.ENGLISH));
        assertEquals(1, testConcept.getDescriptions().size());
        assertEquals(test.getDescription(), testConcept.getDescription().getDescription());
        assertEquals(ConceptDatatype.TEXT_UUID, testConcept.getDatatype().getUuid());
        assertEquals(TestEventWorker.TEST, testConcept.getConceptClass().getName());
        assertEquals(true, testConcept.isRetired());
        Concept sampleConcept = conceptService.getConceptByUuid(sample.getId());
        assertFalse(sampleConcept.getSetMembers().contains(testConcept));
        Concept departmentConcept = conceptService.getConceptByUuid(department.getId());
        assertFalse(departmentConcept.getSetMembers().contains(testConcept));
    }


    @org.junit.Test
    @Ignore
    public void updating_sample_for_test_moves_the_test_from_oldsample_to_newsample() throws Exception {
        Sample oldBloodSample = new Sample("dc8ac8c0-8716-11e3-baa7-0800200c9a66");
        Department bioChemistry = new Department("e060cf44-3d3d-11e3-bf2b-0800271c1b76");
        Test test = new Test("4923d0e0-8734-11e3-baa7-0800200c9a66", "Blood Group Updated", "Blood Group Description updated", null, "NNNN", oldBloodSample, bioChemistry, true, 12);

        Event testEvent = new Event("xxxx-yyyyy-1", "/reference-data/test/59474920-8734-11e3-baa7-0800200c9a66");
        when(httpClient.get(referenceDataUri + testEvent.getContent(), Test.class)).thenReturn(test);
        testEventWorker.process(testEvent);

        Sample newUrineSample = new Sample("788ac8c0-8716-11e3-baa7-0800200c9a66");
        test.setSample(newUrineSample);

        Event routineBloodUpdatedEvent = new Event("xxxx-yyyyy-2", "/reference-data/test/59474920-8734-11e3-baa7-0800200c9a66");
        when(httpClient.get(referenceDataUri + routineBloodUpdatedEvent.getContent(), Test.class)).thenReturn(test);
        testEventWorker.process(routineBloodUpdatedEvent);

        Concept bloodSampleConcept = conceptService.getConceptByUuid(oldBloodSample.getId());
        Concept bloodGroupTest = conceptService.getConceptByUuid(test.getId());
        assertFalse("Older sample should not contain the test", bloodSampleConcept.getSetMembers().contains(bloodGroupTest));

        Concept newUrineConcept = conceptService.getConceptByUuid(newUrineSample.getId());
        assertTrue("New Sample should contain the test", newUrineConcept.getSetMembers().contains(bloodGroupTest));
    }

    @org.junit.Test
    @Ignore
    public void updating_department_for_test_moves_the_test_from_old_department_to_new_department() throws Exception {
        Sample bloodSample = new Sample("dc8ac8c0-8716-11e3-baa7-0800200c9a66");
        Department bioChemistry = new Department("e060cf44-3d3d-11e3-bf2b-0800271c1b76");
        Test test = new Test("4923d0e0-8734-11e3-baa7-0800200c9a66", "Blood Group Updated", "Blood Group Description updated", null, "NNNN", bloodSample, bioChemistry, true, 12);

        Event testEvent = new Event("xxxx-yyyyy-1", "/reference-data/test/59474920-8734-11e3-baa7-0800200c9a66");
        when(httpClient.get(referenceDataUri + testEvent.getContent(), Test.class)).thenReturn(test);
        testEventWorker.process(testEvent);

        Department microBiology = new Department("e060cf44-3d3d-11e3-bf2b-0800271c1b77");
        test.setDepartment(microBiology);

        Event updatedTestEvent = new Event("xxxx-yyyyy-2", "/reference-data/test/59474920-8734-11e3-baa7-0800200c9a66");
        when(httpClient.get(referenceDataUri + updatedTestEvent.getContent(), Test.class)).thenReturn(test);
        testEventWorker.process(updatedTestEvent);

        Concept bioChemistryDepartment = conceptService.getConceptByUuid(bioChemistry.getId());
        Concept bloodGroupTest = conceptService.getConceptByUuid(test.getId());
        assertFalse("Older Department should not contain the test", bioChemistryDepartment.getSetMembers().contains(bloodGroupTest));

        Concept microBiologyDepartment = conceptService.getConceptByUuid(microBiology.getId());
        assertTrue("New Department should contain the test", microBiologyDepartment.getSetMembers().contains(bloodGroupTest));
    }

    @org.junit.Test
    @Ignore
    public void remove_the_test_from_panel_when_test_is_inactivated() throws Exception {
        Sample sample = new Sample("dc8ac8c0-8716-11e3-baa7-0800200c9a66");
        Department department = new Department("e060cf44-3d3d-11e3-bf2b-0800271c1b77");
        Test test = new Test("5923d0e0-8734-11e3-baa7-0800200c9a66");
        test.setIsActive(false);
        test.setSample(sample);
        test.setDepartment(department);
        test.setResultType("Numeric");
        test.setName("Test");

        Event updateTestEvent = new Event("xxxx-yyyyy-2", "/reference-data/test/5923d0e0-8734-11e3-baa7-0800200c9a66");
        when(httpClient.get(referenceDataUri + updateTestEvent.getContent(), Test.class)).thenReturn(test);
        testEventWorker.process(updateTestEvent);

        Concept testConcept = conceptService.getConceptByUuid("5923d0e0-8734-11e3-baa7-0800200c9a66");
        Assert.assertTrue(testConcept.isRetired());

        Concept panelConcept = conceptService.getConceptByUuid("e5e25a7d-b3b2-40f4-9081-d440a7f98b77");
        Assert.assertFalse(panelConcept.getSetMembers().contains(testConcept));
    }

    @org.junit.Test
    @Ignore
    public void prepend_Test_to_testname_when_a_panel_exists_with_same_name() throws IOException {
        Sample sample = new Sample("dc8ac8c0-8716-11e3-baa7-0800200c9a66");
        Department department = new Department("e060cf44-3d3d-11e3-bf2b-0800271c1b77");
        Test test = new Test("5923d0e0-8734-11e3-baa7-0800200c9a66");
        test.setIsActive(true);
        test.setSample(sample);
        test.setDepartment(department);
        test.setResultType("Numeric");
        test.setName("Anaemia Panel");
        test.setShortName("AP");

        Event testEventWithSameNameAsPanel = new Event("xxxx-yyyyy-2", "/reference-data/test/5923d0e0-8734-11e3-baa7-0800200c9a66");
        when(httpClient.get(referenceDataUri + testEventWithSameNameAsPanel.getContent(), Test.class)).thenReturn(test);
        testEventWorker.process(testEventWithSameNameAsPanel);

        Concept testConcept = conceptService.getConceptByUuid("5923d0e0-8734-11e3-baa7-0800200c9a66");
        Assert.assertEquals("Anaemia Panel (Test)", testConcept.getName().getName());
    }

    @org.junit.Test
    @Ignore
    public void should_save_units_for_numeric_tests() throws IOException {
        Sample sample = new Sample("dc8ac8c0-8716-11e3-baa7-0800200c9a66");
        Department department = new Department("e060cf44-3d3d-11e3-bf2b-0800271c1b77");
        Test test = new Test("5923d0e4-8734-11e3-baa7-0800200c9a66");
        test.setIsActive(true);
        test.setSample(sample);
        test.setDepartment(department);
        test.setResultType("Numeric");
        test.setName("Haemoglobin");
        test.setShortName("Hb");
        test.setTestUnitOfMeasure(new TestUnitOfMeasure("4223fge0-8734-11e3-caa7-2802202c9a62", "gm/dl", true));

        Event testEvent = new Event("xxxx-yyyyy-2", "/reference-data/test/5923d0e4-8734-11e3-baa7-0800200c9a66");
        when(httpClient.get(referenceDataUri + testEvent.getContent(), Test.class)).thenReturn(test);
        testEventWorker.process(testEvent);

        Concept testConcept = conceptService.getConceptByUuid("5923d0e4-8734-11e3-baa7-0800200c9a66");
        Assert.assertEquals("Haemoglobin", testConcept.getName().getName());
        Assert.assertEquals("gm/dl", ((ConceptNumeric)testConcept).getUnits());
    }

    @org.junit.Test
    @Ignore
    public void should_not_save_units_for_numeric_tests_if_no_units_is_specified() throws IOException {
        Sample sample = new Sample("dc8ac8c0-8716-11e3-baa7-0800200c9a66");
        Department department = new Department("e060cf44-3d3d-11e3-bf2b-0800271c1b77");
        Test test = new Test("5923d0e4-8734-11e3-baa7-0800200c9a66");
        test.setIsActive(true);
        test.setSample(sample);
        test.setDepartment(department);
        test.setResultType("Numeric");
        test.setName("Haemoglobin");
        test.setShortName("Hb");

        Event testEvent = new Event("xxxx-yyyyy-2", "/reference-data/test/5923d0e4-8734-11e3-baa7-0800200c9a66");
        when(httpClient.get(referenceDataUri + testEvent.getContent(), Test.class)).thenReturn(test);
        testEventWorker.process(testEvent);

        Concept testConcept = conceptService.getConceptByUuid("5923d0e4-8734-11e3-baa7-0800200c9a66");
        Assert.assertEquals("Haemoglobin", testConcept.getName().getName());
        Assert.assertNull(((ConceptNumeric)testConcept).getUnits());
    }
}