package org.openmrs.module.bahmnicore.web.v1_0.resource;

import org.bahmni.module.bahmnicore.model.bahmniPatientProgram.ProgramAttributeType;
import org.bahmni.module.bahmnicore.service.BahmniProgramWorkflowService;
import org.junit.Before;
import org.openmrs.module.webservices.rest.web.resource.impl.BaseDelegatingResourceTest;
import org.springframework.beans.factory.annotation.Autowired;

public class ProgramAttributeTypeResourceTest extends BaseDelegatingResourceTest<ProgramAttributeTypeResource, ProgramAttributeType> {

    @Autowired
    BahmniProgramWorkflowService bahmniProgramWorkflowService;

    @Before
    public void before() throws Exception {
        executeDataSet("programEnrollmentDataSet.xml");
    }

    @Override
    public ProgramAttributeType newObject() {
        return bahmniProgramWorkflowService.getProgramAttributeTypeByUuid(RestConstants.PROGRAM_ATTRIBUTE_TYPE_UUID);
    }

    @Override
    public String getDisplayProperty() {
        return "stage";
    }

    @Override
    public String getUuidProperty() {
        return RestConstants.PROGRAM_ATTRIBUTE_TYPE_UUID;
    }
}