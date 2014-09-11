package org.bahmni.module.referencedata.web.contract.mapper;

import org.bahmni.module.bahmnicore.mapper.builder.ConceptBuilder;
import org.bahmni.module.referencedata.model.event.SampleEvent;
import org.bahmni.module.referencedata.web.contract.Sample;
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

import java.util.Date;
import java.util.List;
import java.util.Locale;

import static org.bahmni.module.referencedata.advice.ConceptOperationEventInterceptorTest.getConceptSet;
import static org.bahmni.module.referencedata.advice.ConceptOperationEventInterceptorTest.getConceptSets;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

@PrepareForTest(Context.class)
@RunWith(PowerMockRunner.class)
public class SampleMapperTest {

    private SampleMapper sampleMapper;
    private Concept sampleConcept;
    private Date dateCreated;
    private Date dateChanged;
    private Concept laboratoryConcept;
    @Mock
    private ConceptService conceptService;
    private Double sortWeight;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);

        sampleMapper = new SampleMapper();
        dateCreated = new Date();
        dateChanged = new Date();
        Locale defaultLocale = new Locale("en", "GB");
        PowerMockito.mockStatic(Context.class);
        when(Context.getLocale()).thenReturn(defaultLocale);
        sampleConcept = new ConceptBuilder().withUUID("Sample UUID").withDateCreated(dateCreated).
                withDateChanged(dateChanged).withShortName("ShortName").withName("SampleName").build();
        laboratoryConcept = new ConceptBuilder().withUUID("Laboratory UUID")
                .withName(SampleEvent.SAMPLE_PARENT_CONCEPT_NAME).withClassUUID(ConceptClass.LABSET_UUID)
                .withSetMember(sampleConcept).build();
        ConceptSet conceptSet = getConceptSet(laboratoryConcept, sampleConcept);
        sortWeight = Double.valueOf(999);
        conceptSet.setSortWeight(sortWeight);
        List<ConceptSet> conceptSets = getConceptSets(conceptSet);
        when(conceptService.getSetsContainingConcept(any(Concept.class))).thenReturn(conceptSets);
        when(Context.getConceptService()).thenReturn(conceptService);
    }

    @Test
    public void map_all_sample_fields_from_concept() throws Exception {
        Sample sampleData = sampleMapper.map(sampleConcept);
        assertEquals("Sample UUID", sampleData.getId());
        assertEquals(sortWeight, sampleData.getSortOrder());
        assertEquals(dateCreated, sampleData.getDateCreated());
        assertEquals(dateChanged, sampleData.getLastUpdated());
        assertEquals("ShortName", sampleData.getShortName());
    }

    @Test
    public void send_default_for_no_short_name() throws Exception {
        sampleConcept = new ConceptBuilder().withUUID("Sample UUID").withDateCreated(dateCreated).
                withDateChanged(dateChanged).withName("SampleName").build();
        Sample sampleData = sampleMapper.map(sampleConcept);
        assertEquals("Sample UUID", sampleData.getId());
        assertEquals("SampleName", sampleData.getShortName());
    }

    @Test
    public void is_active_true_by_default() throws Exception {
        Sample sampleData = sampleMapper.map(sampleConcept);
        assertTrue(sampleData.getIsActive());
    }

    @Test
    public void double_max_as_sort_order_when_sort_order_not_specified() throws Exception {
        ConceptSet conceptSet = getConceptSet(laboratoryConcept, sampleConcept);
        List<ConceptSet> conceptSets = getConceptSets(conceptSet);
        when(conceptService.getSetsContainingConcept(any(Concept.class))).thenReturn(conceptSets);
        when(Context.getConceptService()).thenReturn(conceptService);
        Sample sampleData = sampleMapper.map(sampleConcept);
        assertTrue(sampleData.getSortOrder().equals(Double.MAX_VALUE));
    }
}