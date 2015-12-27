package org.openmrs.module.bahmnicore.web.v1_0.resource;

import org.bahmni.module.bahmnicore.model.bahmniPatientProgram.BahmniPatientProgram;
import org.bahmni.module.bahmnicore.service.BahmniProgramWorkflowService;
import org.junit.Before;
import org.openmrs.module.webservices.rest.web.resource.impl.BaseDelegatingResourceTest;
import org.springframework.beans.factory.annotation.Autowired;

public class BahmniProgramEnrollmentResourceTest extends BaseDelegatingResourceTest<BahmniProgramEnrollmentResource, BahmniPatientProgram> {

    @Autowired
    BahmniProgramWorkflowService bahmniProgramWorkflowService;

    @Before
    public void setUp() throws Exception {
        executeDataSet("programEnrollmentDataSet.xml");
    }

    @Override
    public BahmniPatientProgram newObject() {
        return bahmniProgramWorkflowService.getBahmniPatientProgramByUuid(RestConstants.PATIENT_PROGRAM_UUID);
    }

    @Override
    public String getDisplayProperty() {
        return "HIV Program";
    }

    @Override
    public String getUuidProperty() {
        return RestConstants.PATIENT_PROGRAM_UUID;
    }

}