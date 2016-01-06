package org.openmrs.module.bahmnicore.web.v1_0.resource;

import org.bahmni.module.bahmnicore.model.bahmniPatientProgram.BahmniPatientProgram;
import org.bahmni.module.bahmnicore.service.BahmniProgramWorkflowService;
import org.junit.Before;
import org.openmrs.PatientProgram;
import org.openmrs.api.context.Context;
import org.openmrs.module.webservices.rest.web.resource.impl.BaseDelegatingResourceTest;

@org.springframework.test.context.ContextConfiguration(locations = {"classpath:TestingApplicationContext.xml"}, inheritLocations = true)
public class BahmniProgramEnrollmentResourceTest extends BaseDelegatingResourceTest<BahmniProgramEnrollmentResource, PatientProgram> {

    @Before
    public void before() throws Exception {
        executeDataSet("programEnrollmentDataSet.xml");
    }

    @Override
    public PatientProgram newObject() {
        return Context.getProgramWorkflowService().getPatientProgramByUuid(getUuidProperty());
    }

    @Override
    public void validateDefaultRepresentation() throws Exception {
        super.validateDefaultRepresentation();
        //assertPropPresent("attributes");
    }

    @Override
    public String getDisplayProperty() {
        return "HIV Program";
    }

    @Override
    public String getUuidProperty() {
        return "9119b9f8-af3d-4ad8-9e2e-2317c3de91c6";
    }
}
