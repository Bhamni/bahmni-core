package org.bahmni.module.referencedatafeedclient.worker;

import org.bahmni.module.referencedatafeedclient.*;
import org.bahmni.module.referencedatafeedclient.domain.*;
import org.bahmni.module.referencedatafeedclient.domain.Test;
import org.bahmni.module.referencedatafeedclient.service.*;
import org.bahmni.module.referencedatafeedclient.worker.util.*;
import org.bahmni.webclients.*;
import org.ict4h.atomfeed.client.domain.*;
import org.junit.*;
import org.mockito.*;
import org.mockito.Mock;
import org.openmrs.*;
import org.openmrs.api.*;
import org.openmrs.web.test.*;
import org.springframework.beans.factory.annotation.*;

import java.util.*;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.*;

@org.springframework.test.context.ContextConfiguration(locations = {"classpath:TestingApplicationContext.xml"}, inheritLocations = true)
public class PanelEventWorkerIT extends BaseModuleWebContextSensitiveTest {
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

    private PanelEventWorker panelEventWorker;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        panelEventWorker = new PanelEventWorker(httpClient, referenceDataFeedProperties, conceptService, referenceDataConceptService, new EventWorkerUtility(conceptService));
        when(referenceDataFeedProperties.getReferenceDataUri()).thenReturn(referenceDataUri);
        executeDataSet("panelEventWorkerTestData.xml");
    }

    @org.junit.Test
    public void shouldCreateNewConceptForGivenPanel() throws Exception {
        Event event = new Event("xxxx-yyyyy", "/reference-data/panel/8471dbe5-0465-4eac-94ba-8f8708f3f529");
        Sample sample = new Sample("dc8ac8c0-8716-11e3-baa7-0800200c9a66");
        Test test1 = new Test("5923d0e0-8734-11e3-baa7-0800200c9a66");
        Test test2 = new Test("7923d0e0-8734-11e3-baa7-0800200c9a66");
        HashSet<Test> tests = new HashSet<>(Arrays.asList(test1, test2));
        Panel panel = new Panel("59474920-8734-11e3-baa7-0800200c9a66", "Routine Blood", "Routine Blood Description", "RB", true, sample, tests, 12);
        when(httpClient.get(referenceDataUri + event.getContent(), Panel.class)).thenReturn(panel);

        panelEventWorker.process(event);

        Concept panelConcept = conceptService.getConceptByUuid(panel.getId());
        assertNotNull(panelConcept);
        assertEquals(2, panelConcept.getNames().size());
        assertEquals(panel.getName(), panelConcept.getFullySpecifiedName(Locale.ENGLISH).getName());
        assertEquals(panel.getShortName(), panelConcept.getShortNameInLocale(Locale.ENGLISH).getName());
        assertEquals(1, panelConcept.getDescriptions().size());
        assertEquals(panel.getDescription(), panelConcept.getDescription().getDescription());
        assertEquals(false, panelConcept.isRetired());
        assertEquals(ConceptDatatype.N_A_UUID, panelConcept.getDatatype().getUuid());
        assertEquals(PanelEventWorker.LAB_SET, panelConcept.getConceptClass().getName());
        assertEquals(true, panelConcept.isSet());
        Concept sampleConcept = conceptService.getConceptByUuid(sample.getId());
        assertTrue(sampleConcept.getSetMembers().contains(panelConcept));
        assertEquals(2, panelConcept.getSetMembers().size());
        assertTrue(panelConcept.getSetMembers().contains(conceptService.getConceptByUuid(test1.getId())));
        assertTrue(panelConcept.getSetMembers().contains(conceptService.getConceptByUuid(test2.getId())));

        Concept allTestsAndPanelsConcept = conceptService.getConceptByName(PanelEventWorker.ALL_TESTS_AND_PANELS);
        ConceptSet matchingConceptSet = eventWorkerUtility.getMatchingConceptSet(allTestsAndPanelsConcept.getConceptSets(), panelConcept);
        assertNotNull(matchingConceptSet);
        assertEquals(12, matchingConceptSet.getSortWeight(), 0.001);
    }

    @org.junit.Test
    public void updating_sample_for_panel_moves_the_panel_from_oldsample_to_newsample() throws Exception {
        Sample oldBloodSample = new Sample("dc8ac8c0-8716-11e3-baa7-0800200c9a66");
        HashSet<Test> routineBloodTests = new HashSet<>(Arrays.asList(new Test("5923d0e0-8734-11e3-baa7-0800200c9a66"), new Test("7923d0e0-8734-11e3-baa7-0800200c9a66")));
        Panel routineBloodPanel = new Panel("59474920-8734-11e3-baa7-0800200c9a66", "Routine Blood", "Routine Blood Description", "RB", true, oldBloodSample, routineBloodTests, 14);
        Event routineBloodPanelCreationEvent = new Event("xxxx-yyyyy-1", "/reference-data/panel/59474920-8734-11e3-baa7-0800200c9a66");
        when(httpClient.get(referenceDataUri + routineBloodPanelCreationEvent.getContent(), Panel.class)).thenReturn(routineBloodPanel);
        panelEventWorker.process(routineBloodPanelCreationEvent);

        Sample newUrineSample = new Sample("788ac8c0-8716-11e3-baa7-0800200c9a66");
        routineBloodPanel.setSample(newUrineSample);
        Event routineBloodPanelUpdatedEvent = new Event("xxxx-yyyyy-2", "/reference-data/panel/59474920-8734-11e3-baa7-0800200c9a66");
        when(httpClient.get(referenceDataUri + routineBloodPanelUpdatedEvent.getContent(), Panel.class)).thenReturn(routineBloodPanel);
        panelEventWorker.process(routineBloodPanelUpdatedEvent);

        Concept bloodSampleConcept = conceptService.getConceptByUuid(oldBloodSample.getId());
        Concept routineBloodPanelConcept = conceptService.getConceptByUuid(routineBloodPanel.getId());
        assertFalse("Older sample should not contain the panel", bloodSampleConcept.getSetMembers().contains(routineBloodPanelConcept));

        Concept newUrineConcept = conceptService.getConceptByUuid(newUrineSample.getId());
        assertTrue("New Sample should contain the panel", newUrineConcept.getSetMembers().contains(routineBloodPanelConcept));
    }

    @org.junit.Test
    public void retire_the_panel_when_isActive_is_false() throws Exception {
        String fileContents = new FileReader("inActivePanelEventFeedData.json").readFile();
        Panel panel = ObjectMapperRepository.objectMapper.readValue(fileContents, Panel.class);
        Assert.assertFalse("panel is not active", panel.getIsActive());

        Event panelEvent = new Event("xxxx-yyyyy-2", "/reference-data/panel/dc8ac8c0-8716-11e3-baa7-0800200c9a66");
        when(httpClient.get(referenceDataUri + panelEvent.getContent(), Panel.class)).thenReturn(panel);
        panelEventWorker.process(panelEvent);

        Concept panelConcept = conceptService.getConceptByUuid(panel.getId());
        Concept sampleConcept = conceptService.getConceptByUuid(panel.getSample().getId());

        Assert.assertFalse(sampleConcept.getSetMembers().contains(panelConcept));
    }

    @org.junit.Test
    public void not_retire_the_panel_when_isActive_is_true() throws Exception {
        String fileContents = new FileReader("activePanelEventFeedData.json").readFile();
        Panel panel = ObjectMapperRepository.objectMapper.readValue(fileContents, Panel.class);
        Assert.assertTrue("panel is not active", panel.getIsActive());

        Event updatedSampleEvent = new Event("xxxx-yyyyy-2", "/reference-data/panel/dc8ac8c0-8716-11e3-baa7-0800200c9a66");
        when(httpClient.get(referenceDataUri + updatedSampleEvent.getContent(), Panel.class)).thenReturn(panel);
        panelEventWorker.process(updatedSampleEvent);

        Concept panelConcept = conceptService.getConcept(panel.getName());
        Assert.assertNotNull(panelConcept);

        Concept sampleConcept = conceptService.getConceptByUuid(panel.getSample().getId());
        Assert.assertTrue(sampleConcept.getSetMembers().contains(panelConcept));
    }

    @org.junit.Test
    public void prepend_Panel_to_panelname_when_a_test_exists_with_same_name() throws Exception {
        Panel panel = new Panel("12207b51-0d2f-4e0a-9ff7-65fc14aa362e", "Platelet Count", "description", "PC", true, new Sample("dc8ac8c0-8716-11e3-baa7-0800200c9a66"), new HashSet<Test>(), 12);

        Event panelEventWithSameNameAsTest = new Event("xxxx-yyyyy-2", "/reference-data/panel/12207b51-0d2f-4e0a-9ff7-65fc14aa362e");
        when(httpClient.get(referenceDataUri + panelEventWithSameNameAsTest.getContent(), Panel.class)).thenReturn(panel);
        panelEventWorker.process(panelEventWithSameNameAsTest);

        Concept panelConcept = conceptService.getConcept(panel.getName());
        Assert.assertEquals("Platelet Count (Panel)", panelConcept.getName().getName());
    }

}
