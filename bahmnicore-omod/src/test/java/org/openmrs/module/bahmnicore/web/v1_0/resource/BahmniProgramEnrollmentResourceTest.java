package org.openmrs.module.bahmnicore.web.v1_0.resource;

import org.bahmni.module.bahmnicore.model.bahmniPatientProgram.BahmniPatientProgram;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.module.webservices.rest.web.resource.impl.BaseDelegatingResourceTest;

public class BahmniProgramEnrollmentResourceTest extends BaseDelegatingResourceTest<BahmniProgramEnrollmentResource, BahmniPatientProgram> {

    BahmniProgramEnrollmentResource resource;

    @Before
    public void setUp() throws Exception {
        resource = new BahmniProgramEnrollmentResource();
        executeDataSet("programEnrollmentDataSet.xml");
    }

    @Override
    public BahmniPatientProgram newObject() {
        return null;
    }

    @Override
    public String getDisplayProperty() {
        return null;
    }

    @Override
    public String getUuidProperty() {
        return null;
    }

    @Test
    public void testGetByUniqueId() throws Exception {
    }

    @Test
    public void testDelete() throws Exception {

    }

    @Test
    public void testNewDelegate() throws Exception {

    }

    @Test
    public void testSave() throws Exception {

    }

    @Test
    public void testPurge() throws Exception {

    }

    @Test
    public void testGetRepresentationDescription() throws Exception {

    }
}