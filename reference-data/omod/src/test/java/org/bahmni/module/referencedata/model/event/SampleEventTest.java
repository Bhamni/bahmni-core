package org.bahmni.module.referencedata.model.event;

import org.bahmni.module.bahmnicore.mapper.builder.ConceptBuilder;
import org.bahmni.module.referencedata.model.Operation;
import org.ict4h.atomfeed.server.service.Event;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.openmrs.Concept;
import org.openmrs.ConceptClass;
import org.openmrs.ConceptSet;
import org.openmrs.api.ConceptService;
import org.openmrs.api.context.Context;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static org.bahmni.module.referencedata.advice.ConceptOperationEventInterceptorTest.getConceptSets;
import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

@PrepareForTest(Context.class)
@RunWith(PowerMockRunner.class)
public class SampleEventTest {
    public static final String SAMPLE_CONCEPT_UUID = "aebc57b7-0683-464e-ac48-48b8838abdfc";

    private Concept concept;

    @Mock
    private ConceptService conceptService;
    private Concept parentConcept;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);

        concept = new ConceptBuilder().withClassUUID(ConceptClass.LABSET_UUID).withUUID(SAMPLE_CONCEPT_UUID).build();

        parentConcept = new ConceptBuilder().withName(SampleEvent.SAMPLE_PARENT_CONCEPT_NAME).withSetMember(concept).build();

        List<ConceptSet> conceptSets = getConceptSets(parentConcept, concept);

        when(conceptService.getSetsContainingConcept(any(Concept.class))).thenReturn(conceptSets);

        Locale defaultLocale = new Locale("en", "GB");
        PowerMockito.mockStatic(Context.class);
        when(Context.getConceptService()).thenReturn(conceptService);
        PowerMockito.when(Context.getLocale()).thenReturn(defaultLocale);
    }


    @Test
    public void create_event_for_sample_event() throws Exception {
        Event event = new Operation(ConceptService.class.getMethod("saveConcept", Concept.class)).apply(new Object[]{concept}).get(0);
        Event anotherEvent = new Operation(ConceptService.class.getMethod("saveConcept", Concept.class)).apply(new Object[]{concept}).get(0);
        assertNotNull(event);
        assertFalse(event.getUuid().equals(anotherEvent.getUuid()));
        assertEquals(event.getTitle(), "sample");
        assertEquals(event.getCategory(), "lab");
    }

    @Test
    public void should_not_create_event_for_sample_event_if_there_is_different_concept_class() throws Exception {
        concept = new ConceptBuilder().withClassUUID("some").withUUID(SAMPLE_CONCEPT_UUID).build();
        List<Event> events = new Operation(ConceptService.class.getMethod("saveConcept", Concept.class)).apply(new Object[]{concept});
        assertTrue(events.isEmpty());
    }

    @Test
    public void should_not_create_event_for_sample_event_if_parent_concept_is_missing() throws Exception {
        when(conceptService.getSetsContainingConcept(any(Concept.class))).thenReturn(new ArrayList<ConceptSet>());
        List<Event> events = new Operation(ConceptService.class.getMethod("saveConcept", Concept.class)).apply(new Object[]{concept});
        assertTrue(events.isEmpty());
    }


    @Test
    public void should_not_create_event_for_sample_event_if_parent_concept_is_wrong() throws Exception {
        parentConcept = new ConceptBuilder().withName("Some wrong name").withSetMember(concept).build();
        when(conceptService.getSetsContainingConcept(any(Concept.class))).thenReturn(getConceptSets(parentConcept, concept));
        List<Event> events = new Operation(ConceptService.class.getMethod("saveConcept", Concept.class)).apply(new Object[]{concept});
        assertTrue(events.isEmpty());
    }

}