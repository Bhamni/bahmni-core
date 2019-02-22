//TODO : MIHIR : Add more ITs for tests and panels only happy path will be fine, and resolve all the TODOS in the Bahmni-Core
//TODO : MIHIR : Figure out a way to test the event interceptor.
package org.bahmni.module.referencedata.web.controller;

import org.bahmni.module.referencedata.BaseIntegrationTest;
import org.bahmni.module.referencedata.labconcepts.contract.*;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.Concept;
import org.openmrs.api.ConceptService;
import org.openmrs.api.context.Context;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

public class ConceptOperationControllersIT extends BaseIntegrationTest {
    
    @Autowired
    private ConceptService conceptService;
    private Concept sampleConcept;
    private Concept departmentConcept;
    private Concept testConcept;
    private Concept radiologyTestConcept;

    @Before
    public void setUp() throws Exception {
        executeDataSet("labDataSetup.xml");
        sampleConcept = conceptService.getConcept(102);
        departmentConcept = conceptService.getConcept(202);
        testConcept = conceptService.getConcept(302);
        radiologyTestConcept = conceptService.getConcept(401);
    }

    @Test
    public void shouldPublishSample() throws Exception {
        MockHttpServletRequest sampleRequest = newGetRequest("/rest/v1/reference-data/sample/" + sampleConcept.getUuid());
        MockHttpServletResponse sampleResponse = handle(sampleRequest);
        Sample sampleData = deserialize(sampleResponse, Sample.class);
        assertEquals(sampleConcept.getUuid(), sampleData.getId());
        assertEquals(sampleConcept.getName(Context.getLocale()).getName(), sampleData.getShortName());
        assertEquals(sampleConcept.getName(Context.getLocale()).getName(), sampleData.getName());
        assertNotEquals(sampleConcept.getRetired(), sampleData.getIsActive());
    }


    @Test
    public void shouldPublishDepartment() throws Exception {
        MockHttpServletRequest request = newGetRequest("/rest/v1/reference-data/department/" + departmentConcept.getUuid());
        MockHttpServletResponse response = handle(request);
        Department departmentResponse = deserialize(response, Department.class);
        assertEquals(departmentConcept.getUuid(), departmentResponse.getId());
        assertEquals(departmentConcept.getName(Context.getLocale()).getName(), departmentResponse.getDescription());
        assertEquals(departmentConcept.getName(Context.getLocale()).getName(), departmentResponse.getName());
        assertNotEquals(departmentConcept.getRetired(), departmentResponse.getIsActive());
    }

    @Test
    public void shouldPublishTest() throws Exception {
        MockHttpServletRequest request = newGetRequest("/rest/v1/reference-data/test/" + testConcept.getUuid());
        MockHttpServletResponse response = handle(request);
        LabTest testResponse = deserialize(response, LabTest.class);
        assertEquals(testConcept.getUuid(), testResponse.getId());
        assertEquals(testConcept.getName(Context.getLocale()).getName(), testResponse.getDescription());
        assertEquals(testConcept.getName(Context.getLocale()).getName(), testResponse.getName());
        assertNotEquals(testConcept.getRetired(), testResponse.getIsActive());
        assertEquals("Numeric", testResponse.getResultType());
    }

    @Test
    public void shouldPublishRadiologyTest() throws Exception {
        MockHttpServletRequest request = newGetRequest("/rest/v1/reference-data/radiology/" + radiologyTestConcept.getUuid());
        MockHttpServletResponse response = handle(request);
        RadiologyTest testResponse = deserialize(response, RadiologyTest.class);
        assertEquals(radiologyTestConcept.getUuid(), testResponse.getId());
        assertEquals(radiologyTestConcept.getName(Context.getLocale()).getName(), testResponse.getName());
        assertNotEquals(radiologyTestConcept.getRetired(), testResponse.getIsActive());
    }

    @Test
    public void shouldReturnGenericConceptResource() throws Exception {
        MockHttpServletRequest request = newGetRequest("/rest/v1/reference-data/resources/fe334cb7-t3tb-0037-70f7-kjditree2222");
        MockHttpServletResponse response = handle(request);
        Resource resource = deserialize(response, Resource.class);
        assertEquals("fe334cb7-t3tb-0037-70f7-kjditree2222", resource.getId());
        assertEquals("Dressing Procedure", resource.getName());
        assertEquals("true", resource.getProperties().get("sellable"));
    }
}