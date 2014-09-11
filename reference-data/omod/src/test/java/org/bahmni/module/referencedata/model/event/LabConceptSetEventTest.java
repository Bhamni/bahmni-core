package org.bahmni.module.referencedata.model.event;

import org.bahmni.module.bahmnicore.mapper.builder.ConceptBuilder;
import org.bahmni.module.referencedata.model.Operation;
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

import static org.bahmni.module.referencedata.advice.ConceptOperationEventInterceptorTest.getConceptSet;
import static org.bahmni.module.referencedata.advice.ConceptOperationEventInterceptorTest.getConceptSets;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

@PrepareForTest(Context.class)
@RunWith(PowerMockRunner.class)
public class LabConceptSetEventTest {
    public static final String SAMPLE_CONCEPT_UUID = "aebc57b7-0683-464e-ac48-48b8838abdfc";

    private Concept parentConcept;
    private Concept concept;
    private Concept anotherConcept;

    @Mock
    private ConceptService conceptService;


    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);

        concept = new ConceptBuilder().withClassUUID(ConceptClass.LABSET_UUID).withUUID(SAMPLE_CONCEPT_UUID).build();
        anotherConcept = new ConceptBuilder().withClassUUID(ConceptClass.LABSET_UUID).withUUID(SAMPLE_CONCEPT_UUID).build();

        parentConcept = new ConceptBuilder().withName(SampleEvent.SAMPLE_PARENT_CONCEPT_NAME).withSetMember(concept).withSetMember(anotherConcept).build();

        List<ConceptSet> conceptSets = getConceptSets(parentConcept, concept);

        when(conceptService.getSetsContainingConcept(any(Concept.class))).thenReturn(conceptSets);

        Locale defaultLocale = new Locale("en", "GB");
        PowerMockito.mockStatic(Context.class);
        when(Context.getConceptService()).thenReturn(conceptService);
        PowerMockito.when(Context.getLocale()).thenReturn(defaultLocale);
    }

    @Test
    public void should_publish_conceptset_and_child_concepts_for_laboratory() throws Exception {
        new Operation(ConceptService.class.getMethod("saveConcept", Concept.class)).apply(new Object[]{parentConcept});
        verify(conceptService, times(2)).saveConcept(any(Concept.class));
    }

    @Test
    public void should_publish_conceptset_and_child_concepts_for_department() throws Exception {
        parentConcept = new ConceptBuilder().withName(DepartmentEvent.DEPARTMENT_PARENT_CONCEPT_NAME).withSetMember(concept).withSetMember(anotherConcept).build();
        List<ConceptSet> conceptSets = getConceptSets(parentConcept, concept);
        when(conceptService.getSetsContainingConcept(any(Concept.class))).thenReturn(conceptSets);
        new Operation(ConceptService.class.getMethod("saveConcept", Concept.class)).apply(new Object[]{parentConcept});
        verify(conceptService, times(2)).saveConcept(any(Concept.class));
    }

    @Test
    public void should_publish_conceptset_and_child_concepts_for_test() throws Exception {
        parentConcept = new ConceptBuilder().withName(TestEvent.TEST_PARENT_CONCEPT_NAME).withSetMember(concept).withSetMember(anotherConcept).build();
        List<ConceptSet> conceptSets = getConceptSets(parentConcept, concept);
        when(conceptService.getSetsContainingConcept(any(Concept.class))).thenReturn(conceptSets);
        new Operation(ConceptService.class.getMethod("saveConcept", Concept.class)).apply(new Object[]{parentConcept});
        verify(conceptService, times(2)).saveConcept(any(Concept.class));
    }

    @Test
    public void should_not_publish_parent_concept_if_setmember() throws Exception {
        parentConcept.addSetMember(parentConcept);
        List<ConceptSet> conceptSets = getConceptSets(parentConcept, concept);
        conceptSets.add(getConceptSet(parentConcept, parentConcept));
        when(conceptService.getSetsContainingConcept(any(Concept.class))).thenReturn(conceptSets);
        new Operation(ConceptService.class.getMethod("saveConcept", Concept.class)).apply(new Object[]{parentConcept});
        verify(conceptService, times(2)).saveConcept(any(Concept.class));
    }

    @Test
    public void should_not_publish_anything_if_parent_concept_set_is_empty() throws Exception {
        when(conceptService.getSetsContainingConcept(any(Concept.class))).thenReturn(new ArrayList<ConceptSet>());
        parentConcept = new ConceptBuilder().withName(SampleEvent.SAMPLE_PARENT_CONCEPT_NAME).build();
        new Operation(ConceptService.class.getMethod("saveConcept", Concept.class)).apply(new Object[]{parentConcept});
        verify(conceptService, times(0)).saveConcept(any(Concept.class));
    }
}
