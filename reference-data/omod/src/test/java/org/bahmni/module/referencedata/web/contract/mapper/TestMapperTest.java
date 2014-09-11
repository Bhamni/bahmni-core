package org.bahmni.module.referencedata.web.contract.mapper;

import org.bahmni.module.bahmnicore.mapper.builder.ConceptBuilder;
import org.bahmni.module.referencedata.model.event.DepartmentEvent;
import org.bahmni.module.referencedata.model.event.SampleEvent;
import org.bahmni.module.referencedata.model.event.TestEvent;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.openmrs.Concept;
import org.openmrs.ConceptClass;
import org.openmrs.ConceptDatatype;
import org.openmrs.ConceptSet;
import org.openmrs.api.ConceptService;
import org.openmrs.api.context.Context;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.Date;
import java.util.List;
import java.util.Locale;

import static org.bahmni.module.referencedata.advice.RequestConceptOperationEventInterceptorTest.getConceptSet;
import static org.bahmni.module.referencedata.advice.RequestConceptOperationEventInterceptorTest.getConceptSets;
import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

@PrepareForTest(Context.class)
@RunWith(PowerMockRunner.class)
public class TestMapperTest {
    private TestMapper testMapper;
    private Concept sampleConcept;
    private Date dateCreated;
    private Date dateChanged;
    private Concept laboratoryConcept;
    @Mock
    private ConceptService conceptService;
    private Concept departmentConcept;
    private Concept labDepartmentConcept;
    private Concept testConcept;
    private Concept testAndPanelsConcept;
    private List<ConceptSet> sampleConceptSets;
    private List<ConceptSet> departmentConceptSets;
    private List<ConceptSet> testConceptSets;
    private ConceptSet testDepartmentConceptSet;
    private ConceptSet testSampleConceptSet;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);

        testMapper = new TestMapper();
        dateCreated = new Date();
        dateChanged = new Date();
        Locale defaultLocale = new Locale("en", "GB");
        PowerMockito.mockStatic(Context.class);
        when(Context.getLocale()).thenReturn(defaultLocale);
        testConcept = new ConceptBuilder().withUUID("Test UUID").withDateCreated(dateCreated).withClassUUID(ConceptClass.TEST_UUID).withDescription("SomeDescription")
                .withDateChanged(dateChanged).withShortName("ShortName").withName("Test Name Here").withDataType(ConceptDatatype.NUMERIC).build();
        testAndPanelsConcept = new ConceptBuilder().withUUID("Test and Panels UUID").withDateCreated(dateCreated).withClassUUID(ConceptClass.CONVSET_UUID)
                .withDateChanged(dateChanged).withShortName("ShortName").withName(TestEvent.TEST_PARENT_CONCEPT_NAME).withSetMember(testConcept).build();
        sampleConcept = new ConceptBuilder().withUUID("Sample UUID").withDateCreated(dateCreated).withClassUUID(ConceptClass.LABSET_UUID).
                withDateChanged(dateChanged).withSetMember(testConcept).withShortName("ShortName").withName("SampleName").build();
        laboratoryConcept = new ConceptBuilder().withUUID("Laboratory UUID")
                .withName(SampleEvent.SAMPLE_PARENT_CONCEPT_NAME).withClassUUID(ConceptClass.LABSET_UUID)
                .withSetMember(sampleConcept).build();
        departmentConcept = new ConceptBuilder().withUUID("Department UUID").withDateCreated(dateCreated).
                withDateChanged(dateChanged).withClassUUID(ConceptClass.CONVSET_UUID).withSetMember(testConcept).withDescription("Some Description").withName("Department Name").build();
        labDepartmentConcept = new ConceptBuilder().withUUID("Laboratory Department UUID")
                .withName(DepartmentEvent.DEPARTMENT_PARENT_CONCEPT_NAME).withClassUUID(ConceptClass.CONVSET_UUID)
                .withSetMember(departmentConcept).build();
        ConceptSet sampleConceptSet = getConceptSet(laboratoryConcept, sampleConcept);
        ConceptSet departmentConceptSet = getConceptSet(labDepartmentConcept, departmentConcept);
        ConceptSet testConceptSet = getConceptSet(testAndPanelsConcept, testConcept);
        testSampleConceptSet = getConceptSet(sampleConcept, testConcept);
        testDepartmentConceptSet = getConceptSet(departmentConcept, testConcept);
        departmentConceptSets = getConceptSets(departmentConceptSet);
        sampleConceptSets = getConceptSets(sampleConceptSet);
        testConceptSets = getConceptSets(testConceptSet);
        testConceptSets.add(testSampleConceptSet);
        testConceptSets.add(testDepartmentConceptSet);

        when(conceptService.getSetsContainingConcept(any(Concept.class))).thenAnswer(new Answer<List<ConceptSet>>() {
            @Override
            public List<ConceptSet> answer(InvocationOnMock invocation) throws Throwable {
                Object[] arguments = invocation.getArguments();
                Concept concept = (Concept) arguments[0];
                if (concept.getUuid().equals("Test UUID"))
                    return testConceptSets;
                else if (concept.getUuid().equals("Sample UUID"))
                    return sampleConceptSets;
                else if (concept.getUuid().equals("Department UUID"))
                    return departmentConceptSets;

                return null;
            }
        });
        when(Context.getConceptService()).thenReturn(conceptService);
    }

    @Test
    public void map_all_test_fields_from_concept() throws Exception {
        org.bahmni.module.referencedata.web.contract.Test testData = testMapper.map(testConcept);
        assertEquals("Test UUID", testData.getId());
        assertEquals("Test Name Here", testData.getName());
        assertEquals(ConceptDatatype.NUMERIC, testData.getResultType());
        assertNull(testData.getSalePrice());
        assertEquals(dateCreated, testData.getDateCreated());
        assertEquals(dateChanged, testData.getLastUpdated());
        assertEquals("ShortName", testData.getShortName());
        assertEquals("Department UUID", testData.getDepartment().getId());
        assertEquals("Department Name", testData.getDepartment().getName());
        assertEquals("Some Description", testData.getDepartment().getDescription());
        assertEquals("Sample UUID", testData.getSample().getId());
        assertEquals("SampleName", testData.getSample().getName());
    }

    @Test
    public void send_default_for_no_short_name() throws Exception {
        testConcept = new ConceptBuilder().withUUID("Test UUID").withDateCreated(dateCreated).withClassUUID(ConceptClass.TEST_UUID).withDescription("SomeDescription")
                .withDateChanged(dateChanged).withName("Test Name Here").withDataType(ConceptDatatype.NUMERIC).build();
        org.bahmni.module.referencedata.web.contract.Test testData = testMapper.map(testConcept);
        assertEquals("Test UUID", testData.getId());
        assertEquals("Test Name Here", testData.getShortName());
    }

    @Test
    public void is_active_true_by_default() throws Exception {
        org.bahmni.module.referencedata.web.contract.Test testData = testMapper.map(testConcept);
        assertTrue(testData.getIsActive());
    }

    @Test
    public void null_if_department_not_specified() throws Exception {
        testConceptSets.remove(testDepartmentConceptSet);
        org.bahmni.module.referencedata.web.contract.Test testData = testMapper.map(testConcept);
        assertNull(testData.getDepartment());
    }

    @Test
    public void null_if_sample_not_specified() throws Exception {
        testConceptSets.remove(testSampleConceptSet);
        org.bahmni.module.referencedata.web.contract.Test testData = testMapper.map(testConcept);
        assertNull(testData.getSample());
    }
}