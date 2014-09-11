package org.bahmni.module.referencedata.web.contract.mapper;

import org.bahmni.module.bahmnicore.mapper.builder.ConceptBuilder;
import org.bahmni.module.referencedata.model.event.DepartmentEvent;
import org.bahmni.module.referencedata.web.contract.Department;
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
import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

@PrepareForTest(Context.class)
@RunWith(PowerMockRunner.class)
public class DepartmentMapperTest {

    private DepartmentMapper departmentMapper;
    private Concept departmentConcept;
    private Date dateCreated;
    private Date dateChanged;
    private Concept labDepartmentConcept;
    @Mock
    private ConceptService conceptService;
    private Double sortWeight;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);

        departmentMapper = new DepartmentMapper();
        dateCreated = new Date();
        dateChanged = new Date();
        Locale defaultLocale = new Locale("en", "GB");
        PowerMockito.mockStatic(Context.class);
        when(Context.getLocale()).thenReturn(defaultLocale);
        departmentConcept = new ConceptBuilder().withUUID("Sample UUID").withDateCreated(dateCreated).
                withDateChanged(dateChanged).withDescription("Some Description").withName("SampleName").build();
        labDepartmentConcept = new ConceptBuilder().withUUID("Laboratory UUID")
                .withName(DepartmentEvent.DEPARTMENT_PARENT_CONCEPT_NAME).withClassUUID(ConceptClass.CONVSET_UUID)
                .withSetMember(departmentConcept).build();
        ConceptSet conceptSet = getConceptSet(labDepartmentConcept, departmentConcept);
        sortWeight = Double.valueOf(999);
        conceptSet.setSortWeight(sortWeight);
        List<ConceptSet> conceptSets = getConceptSets(conceptSet);
        when(conceptService.getSetsContainingConcept(any(Concept.class))).thenReturn(conceptSets);
        when(Context.getConceptService()).thenReturn(conceptService);
    }

    @Test
    public void map_all_sample_fields_from_concept() throws Exception {
        Department departmentData = departmentMapper.map(departmentConcept);
        assertEquals("Sample UUID", departmentData.getId());
        assertEquals(sortWeight, departmentData.getSortOrder());
        assertEquals(dateCreated, departmentData.getDateCreated());
        assertEquals(dateChanged, departmentData.getLastUpdated());
        assertEquals("Some Description", departmentData.getDescription());
    }

    @Test
    public void send_null_for_no_description() throws Exception {
        departmentConcept = new ConceptBuilder().withUUID("Sample UUID").withDateCreated(dateCreated).
                withDateChanged(dateChanged).withName("SampleName").build();
        Department departmentData = departmentMapper.map(departmentConcept);
        assertEquals("Sample UUID", departmentData.getId());
        assertNull(departmentData.getDescription());
    }

    @Test
    public void is_active_true_by_default() throws Exception {
        Department departmentData = departmentMapper.map(departmentConcept);
        assertTrue(departmentData.getIsActive());
    }

    @Test
    public void double_max_as_sort_order_when_sort_order_not_specified() throws Exception {
        ConceptSet conceptSet = getConceptSet(labDepartmentConcept, departmentConcept);
        List<ConceptSet> conceptSets = getConceptSets(conceptSet);
        when(conceptService.getSetsContainingConcept(any(Concept.class))).thenReturn(conceptSets);
        when(Context.getConceptService()).thenReturn(conceptService);
        Department departmentData = departmentMapper.map(departmentConcept);
        assertTrue(departmentData.getSortOrder().equals(Double.MAX_VALUE));
    }
}