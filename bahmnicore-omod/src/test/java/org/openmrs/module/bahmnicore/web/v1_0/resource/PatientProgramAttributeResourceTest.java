package org.openmrs.module.bahmnicore.web.v1_0.resource;

import org.bahmni.module.bahmnicore.model.bahmniPatientProgram.PatientProgramAttribute;
import org.bahmni.module.bahmnicore.service.BahmniProgramWorkflowService;
import org.junit.Before;
import org.openmrs.api.context.Context;
import org.openmrs.module.webservices.rest.web.resource.impl.BaseDelegatingResourceTest;

@org.springframework.test.context.ContextConfiguration(locations = {"classpath:TestingApplicationContext.xml"}, inheritLocations = true)
public class PatientProgramAttributeResourceTest extends BaseDelegatingResourceTest<PatientProgramAttributeResource, PatientProgramAttribute> {

    @Before
    public void before() throws Exception {
        executeDataSet("programEnrollmentDataSet.xml");
    }

    @Override
    public PatientProgramAttribute newObject() {
        return Context.getService(BahmniProgramWorkflowService.class).getPatientProgramAttributeByUuid(getUuidProperty());
    }

    @Override
    public void validateDefaultRepresentation() throws Exception {
        super.validateDefaultRepresentation();
        assertPropEquals("value", getObject().getValue());
        assertPropPresent("attributeType");
        assertPropEquals("voided", getObject().getVoided());
    }

    @Override
    public void validateFullRepresentation() throws Exception {
        super.validateFullRepresentation();
        assertPropEquals("value", getObject().getValue());
        assertPropPresent("attributeType");
        assertPropEquals("voided", getObject().getVoided());
        assertPropPresent("auditInfo");
    }

    @Override
    public String getDisplayProperty() {
        return "stage: Stage1";
    }

    @Override
    public String getUuidProperty() {
        return RestConstants.PATIENT_PROGRAM_ATTRIBUTE_UUID;
    }
}
