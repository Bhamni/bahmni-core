package org.bahmni.module.referencedatafeedclient.worker;

import org.bahmni.module.referencedatafeedclient.ReferenceDataFeedProperties;
import org.bahmni.module.referencedatafeedclient.domain.Sample;
import org.bahmni.module.referencedatafeedclient.service.ReferenceDataConceptService;
import org.bahmni.module.referencedatafeedclient.worker.util.FileReader;
import org.bahmni.webclients.HttpClient;
import org.bahmni.webclients.ObjectMapperRepository;
import org.ict4h.atomfeed.client.domain.Event;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.*;
import org.openmrs.Concept;
import org.openmrs.ConceptDatatype;
import org.openmrs.ConceptSet;
import org.openmrs.api.ConceptService;
import org.openmrs.web.test.BaseModuleWebContextSensitiveTest;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Locale;

import static org.junit.Assert.*;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

@org.springframework.test.context.ContextConfiguration(locations = {"classpath:TestingApplicationContext.xml"}, inheritLocations = true)
public class SampleEventWorkerIT extends BaseModuleWebContextSensitiveTest {
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

    private SampleEventWorker sampleEventWorker;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        sampleEventWorker = new SampleEventWorker(httpClient, referenceDataFeedProperties, conceptService, referenceDataConceptService, new EventWorkerUtility(conceptService));
        when(referenceDataFeedProperties.getReferenceDataUri()).thenReturn(referenceDataUri);
        executeDataSet("sampleEventWorkerTestData.xml");
    }

    @Test
    @Ignore
    public void shouldCreateNewConceptForGivenSample() throws Exception {
        Event event = new Event("xxxx-yyyyy", "/reference-data/sample/8471dbe5-0465-4eac-94ba-8f8708f3f529");
        Sample sample = new Sample("8471dbe5-0465-4eac-94ba-8f8708f3f529", "Urine Microscopy", "Urine Microscopy Sample Description", true, 100);
        when(httpClient.get(referenceDataUri + event.getContent(), Sample.class)).thenReturn(sample);

        sampleEventWorker.process(event);

        Concept sampleConcept = conceptService.getConceptByUuid(sample.getId());
        assertNotNull(sampleConcept);
        assertEquals(2, sampleConcept.getNames().size());
        assertEquals(sample.getName(), sampleConcept.getName(Locale.ENGLISH).getName());
        assertEquals(sample.getShortName(), sampleConcept.getShortNameInLocale(Locale.ENGLISH).getName());
        assertEquals(ConceptDatatype.N_A_UUID, sampleConcept.getDatatype().getUuid());
        assertEquals(SampleEventWorker.LAB_SET, sampleConcept.getConceptClass().getName());
        assertEquals(true, sampleConcept.isSet());
        assertEquals(false, sampleConcept.isRetired());
        Concept labConcept = conceptService.getConceptByName(SampleEventWorker.LABORATORY);
        assertTrue(labConcept.getSetMembers().contains(sampleConcept));

        ConceptSet matchingConceptSet = eventWorkerUtility.getMatchingConceptSet(labConcept.getConceptSets(), sampleConcept);
        assertEquals(100, matchingConceptSet.getSortWeight(), 0.001);
    }

    @Test
    @Ignore
    public void shouldUpdateConceptForGivenSample() throws Exception {
        int newSortOrder = 5;
        Event bloodSampleUpdateEvent = new Event("xxxx-yyyyy", "/reference-data/sample/dc8ac8c0-8716-11e3-baa7-0800200c9a66");
        Sample bloodSample = new Sample("dc8ac8c0-8716-11e3-baa7-0800200c9a66", "Blood Sample Updated", null, false, newSortOrder);
        when(httpClient.get(referenceDataUri + bloodSampleUpdateEvent.getContent(), Sample.class)).thenReturn(bloodSample);

        sampleEventWorker.process(bloodSampleUpdateEvent);

        Concept sampleConcept = conceptService.getConceptByUuid(bloodSample.getId());
        assertNotNull(sampleConcept);
        assertEquals(1, sampleConcept.getNames().size());
        assertEquals(bloodSample.getName(), sampleConcept.getName(Locale.ENGLISH).getName());
        assertEquals(bloodSample.getShortName(), sampleConcept.getShortNameInLocale(Locale.ENGLISH));
        assertEquals(ConceptDatatype.N_A_UUID, sampleConcept.getDatatype().getUuid());
        assertEquals(SampleEventWorker.LAB_SET, sampleConcept.getConceptClass().getName());
        assertEquals(true, sampleConcept.isSet());
        assertEquals(true, sampleConcept.isRetired());

        ConceptSet bloodConceptSet = conceptService.getConceptSetByUuid("4644c0f6-04f7-4db7-9f27-7448af90e5e4");
        assertEquals(newSortOrder, bloodConceptSet.getSortWeight(), 0.0001);

        ConceptSet urineConceptSet = conceptService.getConceptSetByUuid("e4c6e385-74f7-4036-bcc6-cd0ce9172ad2");
        assertEquals(99, urineConceptSet.getSortWeight(), 0.0001);
    }

    @org.junit.Test
    @Ignore
    public void updating_sample_name_keeps_the_test_in_the_same_sample() throws Exception {
        Sample bloodSample = new Sample("dc8ac8c0-8716-11e3-baa7-0800200c9a66");
        bloodSample.setName("newBlood");

        Event updatedSampleEvent = new Event("xxxx-yyyyy-2", "/reference-data/sample/dc8ac8c0-8716-11e3-baa7-0800200c9a66");
        when(httpClient.get(referenceDataUri + updatedSampleEvent.getContent(), Sample.class)).thenReturn(bloodSample);
        sampleEventWorker.process(updatedSampleEvent);

        Concept bloodSampleConcept = conceptService.getConceptByUuid(bloodSample.getId());
        Concept testConcept = conceptService.getConceptByUuid("e060cf44-3d3d-11e3-bf2b-0800271c1b77");
        assertTrue("Sample should contain the test", bloodSampleConcept.getSetMembers().contains(testConcept));
    }

    @org.junit.Test
    @Ignore
    public void retire_the_sample_when_isActive_is_false() throws Exception {
        String fileContents = new FileReader("inActiveSampleEventFeedData.json").readFile();
        Sample sample = ObjectMapperRepository.objectMapper.readValue(fileContents, Sample.class);
        Assert.assertFalse("sample is not active", sample.getIsActive());

        Event updatedSampleEvent = new Event("xxxx-yyyyy-2", "/reference-data/sample/dc8ac8c0-8716-11e3-baa7-0800200c9a66");
        when(httpClient.get(referenceDataUri + updatedSampleEvent.getContent(), Sample.class)).thenReturn(sample);
        sampleEventWorker.process(updatedSampleEvent);

        Concept sampleConcept = conceptService.getConcept(sample.getName());
        Assert.assertNull(sampleConcept);
    }

    @org.junit.Test
    @Ignore
    public void not_retire_the_sample_when_isActive_is_true() throws Exception {
        String fileContents = new FileReader("activeSampleEventFeedData.json").readFile();
        Sample sample = ObjectMapperRepository.objectMapper.readValue(fileContents, Sample.class);
        Assert.assertTrue("sample is not active", sample.getIsActive());

        Event updatedSampleEvent = new Event("xxxx-yyyyy-2", "/reference-data/sample/dc8ac8c0-8716-11e3-baa7-0800200c9a66");
        when(httpClient.get(referenceDataUri + updatedSampleEvent.getContent(), Sample.class)).thenReturn(sample);
        sampleEventWorker.process(updatedSampleEvent);

        Concept sampleConcept = conceptService.getConcept(sample.getName());
        Assert.assertNotNull(sampleConcept);
    }

}
