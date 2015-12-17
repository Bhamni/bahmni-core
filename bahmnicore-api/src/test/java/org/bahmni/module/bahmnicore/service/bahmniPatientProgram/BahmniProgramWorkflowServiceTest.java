package org.bahmni.module.bahmnicore.service.bahmniPatientProgram;

import org.bahmni.module.bahmnicore.model.bahmniPatientProgram.PatientProgramAttribute;
import org.junit.Test;
import org.mockito.Mock;
import org.openmrs.test.BaseContextSensitiveTest;
import org.openmrs.test.Verifies;

class BahmniProgramWorkflowServiceTest extends BaseContextSensitiveTest {

    @Mock
    private PatientProgramAttribute patientProgramAttribute;

    @Test
    @Verifies(value = "should save a patient program attribute", method = "savePatientProgramAttribute()")
    public void shouldSaveTheGivenPatientProgramAttribute() throws Exception {

    }
}
