package org.bahmni.module.referencedatafeedclient.worker;

import org.bahmni.module.referencedatafeedclient.*;
import org.bahmni.module.referencedatafeedclient.dao.*;
import org.bahmni.module.referencedatafeedclient.domain.*;
import org.bahmni.module.referencedatafeedclient.service.*;
import org.bahmni.webclients.*;
import org.ict4h.atomfeed.client.domain.*;
import org.junit.*;
import org.mockito.*;
import org.openmrs.*;
import org.openmrs.api.*;
import org.openmrs.web.test.*;
import org.springframework.beans.factory.annotation.*;

import static org.mockito.Mockito.*;

@org.springframework.test.context.ContextConfiguration(locations = {"classpath:TestingApplicationContext.xml"}, inheritLocations = true)
public class TestUnitOfMeasureEventWorkerIT extends BaseModuleWebContextSensitiveTest {
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
    private BahmniTestUnitsDao bahmniTestUnitsDao;

    private TestUnitOfMeasureEventWorker testUnitOfMeasureEventWorker;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        testUnitOfMeasureEventWorker = new TestUnitOfMeasureEventWorker(httpClient, referenceDataFeedProperties, conceptService, referenceDataConceptService, new EventWorkerUtility(conceptService), bahmniTestUnitsDao);
        when(referenceDataFeedProperties.getReferenceDataUri()).thenReturn(referenceDataUri);
        executeDataSet("testUnitOfMeasureEventWorkerTestData.xml");
    }
//
//    @Override
//    public Properties getRuntimeProperties() {
//        Properties props = super.getRuntimeProperties();
//        props.setProperty("hibernate.show_sql", "true");
//        props.setProperty("hibernate.format_sql", "true");
//        return props;
//    }

    @org.junit.Test
    @Ignore
    public void shouldCreateNewConceptForGivenTest() throws Exception {
        TestUnitOfMeasure testUnitOfMeasure = new TestUnitOfMeasure("5463d0e4-8254-12e3-baa7-0830200e9a66");
        testUnitOfMeasure.setName("mg/dl");

        Event testUnitOfMeasureEvent = new Event("xxxx-yyyyy-2", "/reference-data/test_unit_of_measure/5463d0e4-8254-12e3-baa7-0830200e9a66");
        when(httpClient.get(referenceDataUri + testUnitOfMeasureEvent.getContent(), TestUnitOfMeasure.class)).thenReturn(testUnitOfMeasure);
        testUnitOfMeasureEventWorker.process(testUnitOfMeasureEvent);

        Concept testUnitOfMeasureConcept = conceptService.getConceptByUuid("5463d0e4-8254-12e3-baa7-0830200e9a66");
        Assert.assertEquals("mg/dl", testUnitOfMeasureConcept.getName().getName());
        Assert.assertEquals("mg/dl", testUnitOfMeasureConcept.getDescription().getDescription());
        Assert.assertFalse(testUnitOfMeasureConcept.isRetired());
    }

    @org.junit.Test
    @Ignore
    public void shouldUpdateConceptIfAlreadyPresent() throws Exception {
        TestUnitOfMeasure testUnitOfMeasure = new TestUnitOfMeasure("7463d0e4-8254-12e3-baa7-0830200e9a67");
        testUnitOfMeasure.setName("mg/dl");

        Concept testUnitOfMeasureConcept = conceptService.getConceptByUuid("7463d0e4-8254-12e3-baa7-0830200e9a67");
        Assert.assertEquals("mg", testUnitOfMeasureConcept.getName().getName());

        Event testUnitOfMeasureEvent = new Event("xxxx-yyyyy-2", "/reference-data/test_unit_of_measure/7463d0e4-8254-12e3-baa7-0830200e9a67");
        when(httpClient.get(referenceDataUri + testUnitOfMeasureEvent.getContent(), TestUnitOfMeasure.class)).thenReturn(testUnitOfMeasure);
        testUnitOfMeasureEventWorker.process(testUnitOfMeasureEvent);

        testUnitOfMeasureConcept = conceptService.getConceptByUuid("7463d0e4-8254-12e3-baa7-0830200e9a67");
        Assert.assertEquals("mg/dl", testUnitOfMeasureConcept.getName().getName());
        Assert.assertEquals("mg/dl", testUnitOfMeasureConcept.getDescription().getDescription());
        Assert.assertFalse(testUnitOfMeasureConcept.isRetired());
    }

    @org.junit.Test
    @Ignore
    public void shouldUpdateConceptAndAllTestWithUnitIfAlreadyPresent() throws Exception {
        Concept testUnitOfMeasureConcept = conceptService.getConceptByUuid("7463d0e4-8254-12e3-baa7-0830200e9a67");
        ConceptNumeric testConcept = (ConceptNumeric)conceptService.getConcept(105);
        Assert.assertEquals("mg", testUnitOfMeasureConcept.getName().getName());
        Assert.assertEquals("mg", testConcept.getUnits());

        TestUnitOfMeasure testUnitOfMeasure = new TestUnitOfMeasure("7463d0e4-8254-12e3-baa7-0830200e9a67");
        testUnitOfMeasure.setName("mg/dl");

        Event testUnitOfMeasureEvent = new Event("xxxx-yyyyy-2", "/reference-data/test_unit_of_measure/7463d0e4-8254-12e3-baa7-0830200e9a67");
        when(httpClient.get(referenceDataUri + testUnitOfMeasureEvent.getContent(), TestUnitOfMeasure.class)).thenReturn(testUnitOfMeasure);
        testUnitOfMeasureEventWorker.process(testUnitOfMeasureEvent);

        testUnitOfMeasureConcept = conceptService.getConceptByUuid("7463d0e4-8254-12e3-baa7-0830200e9a67");
        testConcept = (ConceptNumeric)conceptService.getConcept(105);
        Assert.assertEquals("mg/dl", testConcept.getUnits());
        Assert.assertFalse(testUnitOfMeasureConcept.isRetired());
    }

    @org.junit.Test
    @Ignore
    public void shouldSetTestUnitsToNullIfTUOMIsRetiered() throws Exception {
        Concept testUnitOfMeasureConcept = conceptService.getConceptByUuid("7463d0e4-8254-12e3-baa7-0830200e9a67");
        ConceptNumeric testConcept = (ConceptNumeric)conceptService.getConcept(105);
        Assert.assertEquals("mg", testUnitOfMeasureConcept.getName().getName());
        Assert.assertEquals("mg", testConcept.getUnits());

        TestUnitOfMeasure testUnitOfMeasure = new TestUnitOfMeasure("7463d0e4-8254-12e3-baa7-0830200e9a67");
        testUnitOfMeasure.setName("mg");
        testUnitOfMeasure.setIsActive(false);

        Event testUnitOfMeasureEvent = new Event("xxxx-yyyyy-2", "/reference-data/test_unit_of_measure/7463d0e4-8254-12e3-baa7-0830200e9a67");
        when(httpClient.get(referenceDataUri + testUnitOfMeasureEvent.getContent(), TestUnitOfMeasure.class)).thenReturn(testUnitOfMeasure);
        testUnitOfMeasureEventWorker.process(testUnitOfMeasureEvent);

        testUnitOfMeasureConcept = conceptService.getConceptByUuid("7463d0e4-8254-12e3-baa7-0830200e9a67");
        testConcept = (ConceptNumeric)conceptService.getConcept(105);
        Assert.assertEquals(null, testConcept.getUnits());
        Assert.assertTrue(testUnitOfMeasureConcept.isRetired());
    }
}