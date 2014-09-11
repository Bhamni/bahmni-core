package org.bahmni.module.referencedata.advice;

import org.bahmni.module.bahmnicore.mapper.builder.ConceptBuilder;
import org.bahmni.module.referencedata.model.event.SampleEvent;
import org.bahmni.module.referencedata.model.event.SampleEventTest;
import org.ict4h.atomfeed.server.service.EventService;
import org.ict4h.atomfeed.transaction.AFTransactionWork;
import org.ict4h.atomfeed.transaction.AFTransactionWorkWithoutResult;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.openmrs.Concept;
import org.openmrs.ConceptClass;
import org.openmrs.ConceptSet;
import org.openmrs.api.ConceptService;
import org.openmrs.api.context.Context;
import org.openmrs.module.atomfeed.transaction.support.AtomFeedSpringTransactionManager;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static junit.framework.TestCase.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;


@PrepareForTest(Context.class)
@RunWith(PowerMockRunner.class)
public class ConceptOperationEventInterceptorTest {
    @Mock
    private AtomFeedSpringTransactionManager atomFeedSpringTransactionManager;
    @Mock
    private EventService eventService;
    @Mock
    private ConceptService conceptService;

    private ArgumentCaptor<AFTransactionWorkWithoutResult> captor = ArgumentCaptor.forClass(AFTransactionWorkWithoutResult.class);

    private ConceptOperationEventInterceptor publishedFeed;

    private Concept concept;
    private Concept parentConcept;


    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);

        concept = new ConceptBuilder().withClassUUID(ConceptClass.LABSET_UUID).withUUID(SampleEventTest.SAMPLE_CONCEPT_UUID).build();

        parentConcept = new ConceptBuilder().withName(SampleEvent.SAMPLE_PARENT_CONCEPT_NAME).withSetMember(concept).build();

        List<ConceptSet> conceptSets = getConceptSets(parentConcept, concept);

        when(conceptService.getSetsContainingConcept(any(Concept.class))).thenReturn(conceptSets);

        Locale defaultLocale = new Locale("en", "GB");
        PowerMockito.mockStatic(Context.class);
        when(Context.getConceptService()).thenReturn(conceptService);
        PowerMockito.when(Context.getLocale()).thenReturn(defaultLocale);

        publishedFeed = new ConceptOperationEventInterceptor(atomFeedSpringTransactionManager, eventService);
    }

    public static List<ConceptSet> getConceptSets(Concept parentConcept, Concept conceptMember) {
        List<ConceptSet> conceptSets = new ArrayList<>();
        ConceptSet conceptSet = getConceptSet(parentConcept, conceptMember);
        conceptSets.add(conceptSet);
        return conceptSets;
    }

    public static List<ConceptSet> getConceptSets(ConceptSet conceptSet) {
        List<ConceptSet> conceptSets = new ArrayList<>();
        conceptSets.add(conceptSet);
        return conceptSets;
    }

    public static ConceptSet getConceptSet(Concept parentConcept, Concept conceptMember) {
        ConceptSet conceptSet = new ConceptSet();
        conceptSet.setConceptSet(parentConcept);
        conceptSet.setConcept(conceptMember);
        return conceptSet;
    }

    @Test
    public void shouldPublishUpdateEventToFeedAfterUpdateConceptOperation() throws Throwable {
        Method method = ConceptService.class.getMethod("updateConcept", Concept.class);
        Object[] objects = new Object[]{concept};

        publishedFeed.afterReturning(null, method, objects, null);
        verify(atomFeedSpringTransactionManager).executeWithTransaction(any(AFTransactionWorkWithoutResult.class));
    }

    @Test
    public void shouldPublishUpdateEventToFeedAfterEveryUpdateConceptOperation() throws Throwable {
        Method method = ConceptService.class.getMethod("updateConcept", Concept.class);
        Object[] objects = new Object[]{concept};
        int updates = 2;
        for (int i = 0; i < updates; i++) {
            publishedFeed.afterReturning(null, method, objects, null);
        }
        verify(atomFeedSpringTransactionManager, times(updates)).executeWithTransaction(any(AFTransactionWorkWithoutResult.class));
    }


    @Test
    public void shouldPublishUpdateEventToFeedAfterSaveConceptOperation() throws Throwable {
        Method method = ConceptService.class.getMethod("saveConcept", Concept.class);
        Object[] objects = new Object[]{concept};

        publishedFeed.afterReturning(null, method, objects, null);
        verify(atomFeedSpringTransactionManager).executeWithTransaction(any(AFTransactionWorkWithoutResult.class));
    }

    @Test
    public void shouldPublishUpdateEventToFeedAfterEverySaveConceptOperation() throws Throwable {
        Method method = ConceptService.class.getMethod("saveConcept", Concept.class);
        Object[] objects = new Object[]{concept};
        int updates = 2;
        for (int i = 0; i < updates; i++) {
            publishedFeed.afterReturning(null, method, objects, null);
        }
        verify(atomFeedSpringTransactionManager, times(updates)).executeWithTransaction(any(AFTransactionWorkWithoutResult.class));
    }

    @Test
    public void shouldSaveEventInTheSameTransactionAsTheTrigger() throws Throwable {
        Method method = ConceptService.class.getMethod("updateConcept", Concept.class);
        Object[] objects = new Object[]{concept};

        publishedFeed.afterReturning(null, method, objects, null);
        verify(atomFeedSpringTransactionManager).executeWithTransaction(captor.capture());

        assertEquals(AFTransactionWork.PropagationDefinition.PROPAGATION_REQUIRED, captor.getValue().getTxPropagationDefinition());
    }

}