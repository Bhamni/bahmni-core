package org.bahmni.module.referencedatafeedclient.worker;

import org.bahmni.module.referencedatafeedclient.ReferenceDataFeedProperties;
import org.bahmni.module.referencedatafeedclient.domain.Department;
import org.bahmni.module.referencedatafeedclient.service.ReferenceDataConceptService;
import org.bahmni.webclients.HttpClient;
import org.ict4h.atomfeed.client.domain.Event;
import org.junit.Before;
import org.junit.Test;
import org.mockito.*;
import org.openmrs.Concept;
import org.openmrs.ConceptDatatype;
import org.openmrs.api.ConceptService;
import org.openmrs.web.test.BaseModuleWebContextSensitiveTest;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Locale;

import static org.junit.Assert.*;
import static org.mockito.Mockito.when;

@org.springframework.test.context.ContextConfiguration(locations = {"classpath:TestingApplicationContext.xml"}, inheritLocations = true)
public class DepartmentEventWorkerIT extends BaseModuleWebContextSensitiveTest {
    @Mock
    private HttpClient httpClient;
    @Mock
    private ReferenceDataFeedProperties referenceDataFeedProperties;
    private final String referenceDataUri = "http://localhost";

    @Autowired
    private ConceptService conceptService;
    @Autowired
    private ReferenceDataConceptService referenceDataConceptService;
    private DepartmentEventWorker departmentEventWorker;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        departmentEventWorker = new DepartmentEventWorker(httpClient, referenceDataFeedProperties, conceptService, referenceDataConceptService, new EventWorkerUtility(conceptService));
        when(referenceDataFeedProperties.getReferenceDataUri()).thenReturn(referenceDataUri);
        executeDataSet("departmentEventWorkerTestData.xml");
    }

    @Test
    public void shouldCreateNewConceptForGivenDepartment() throws Exception {
        Event event = new Event("xxxx-yyyyy", "/reference-data/department/8471dbe5-0465-4eac-94ba-8f8708f3f529");
        Department department = new Department("8471dbe5-0465-4eac-94ba-8f8708f3f529", "BioChem", "BioChem Dep", true);
        when(httpClient.get(referenceDataUri + event.getContent(), Department.class)).thenReturn(department);

        departmentEventWorker.process(event);

        Concept departmentConcept = conceptService.getConceptByUuid(department.getId());
        assertNotNull(departmentConcept);
        assertEquals(1, departmentConcept.getNames().size());
        assertEquals("BioChem Department", departmentConcept.getName(Locale.ENGLISH).getName());
        assertEquals(1, departmentConcept.getDescriptions().size());
        assertEquals(department.getDescription(), departmentConcept.getDescription().getDescription());
        assertEquals(ConceptDatatype.N_A_UUID, departmentConcept.getDatatype().getUuid());
        assertEquals(DepartmentEventWorker.CONV_SET, departmentConcept.getConceptClass().getName());
        assertEquals(true, departmentConcept.isSet());
        assertEquals(false, departmentConcept.isRetired());
        Concept labDepartmentsConcept = conceptService.getConceptByName(DepartmentEventWorker.LAB_DEPARTMENTS);
        assertTrue(labDepartmentsConcept.getSetMembers().contains(departmentConcept));
    }

    @Test
    public void shouldUpdateConceptForGivenDepartment() throws Exception {
        Event event = new Event("xxxx-yyyyy", "/reference-data/department/dc8ac8c0-8716-11e3-baa7-0800200c9a66");
        Department department = new Department("dc8ac8c0-8716-11e3-baa7-0800200c9a66", "Haematology updated", "Haematology Description updated", false);
        when(httpClient.get(referenceDataUri+event.getContent(), Department.class)).thenReturn(department);

        departmentEventWorker.process(event);

        Concept departmentConcept = conceptService.getConceptByUuid(department.getId());
        assertNotNull(departmentConcept);
        assertEquals(1, departmentConcept.getNames().size());
        assertEquals("Haematology updated Department",departmentConcept.getName(Locale.ENGLISH).getName());
        assertEquals(1, departmentConcept.getDescriptions().size());
        assertEquals(department.getDescription(), departmentConcept.getDescription().getDescription());
        assertEquals(ConceptDatatype.N_A_UUID, departmentConcept.getDatatype().getUuid());
        assertEquals(DepartmentEventWorker.CONV_SET, departmentConcept.getConceptClass().getName());
        assertEquals(true, departmentConcept.isSet());
        assertEquals(true, departmentConcept.isRetired());
        Concept labDepartmentsConcept = conceptService.getConceptByName(DepartmentEventWorker.LAB_DEPARTMENTS);
        assertTrue(labDepartmentsConcept.getSetMembers().contains(departmentConcept));
    }

    @org.junit.Test
    public void updating_sample_name_keeps_the_test_in_the_same_sample() throws Exception {
        Department department = new Department("e060cf44-3d3d-11e3-bf2b-0800271c1b76");
        department.setName("new Heamotology");

        Event updatedSampleEvent = new Event("xxxx-yyyyy-2", "/reference-data/department/e060cf44-3d3d-11e3-bf2b-0800271c1b76");
        when(httpClient.get(referenceDataUri + updatedSampleEvent.getContent(), Department.class)).thenReturn(department);
        departmentEventWorker.process(updatedSampleEvent);

        Concept departmentConcept = conceptService.getConceptByUuid(department.getId());
        Concept testConcept = conceptService.getConceptByUuid("dc8ac8c0-8716-11e3-baa7-0800200c9a66");
        assertTrue("Department should contain the test", departmentConcept.getSetMembers().contains(testConcept));
        assertEquals("new Heamotology Department", departmentConcept.getName().getName());
    }

}
