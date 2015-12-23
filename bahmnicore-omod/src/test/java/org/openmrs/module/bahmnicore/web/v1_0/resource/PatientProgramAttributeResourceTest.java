package org.openmrs.module.bahmnicore.web.v1_0.resource;

import org.bahmni.module.bahmnicore.model.bahmniPatientProgram.PatientProgramAttribute;
import org.bahmni.module.bahmnicore.service.BahmniProgramWorkflowService;
import org.junit.Before;
import org.openmrs.module.webservices.rest.web.resource.impl.BaseDelegatingResourceTest;
import org.springframework.beans.factory.annotation.Autowired;

import java.text.ParseException;
import java.text.SimpleDateFormat;

public class PatientProgramAttributeResourceTest extends BaseDelegatingResourceTest<PatientProgramAttributeResource, PatientProgramAttribute> {

    @Autowired
    BahmniProgramWorkflowService bahmniProgramWorkflowService;

    @Before
    public void before() throws Exception {
        executeDataSet("programEnrollmentDataSet.xml");
    }

    @Override
    public PatientProgramAttribute newObject() {
        return bahmniProgramWorkflowService.getPatientProgramAttributeByUuid(RestConstants.PATIENT_PROGRAM_ATTRIBUTE_UUID);
    }

    @Override
    public String getDisplayProperty() {
        try {
            return "Audit Date: " + new SimpleDateFormat("yyyy-MM-dd").parse("2011-04-25");
        }
        catch (ParseException ex) {
            ex.printStackTrace();
            return null;
        }
    }

    @Override
    public String getUuidProperty() {
        return RestConstants.PATIENT_PROGRAM_ATTRIBUTE_UUID;
    }
}