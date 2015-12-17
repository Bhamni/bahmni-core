package org.bahmni.module.bahmnicore.model.bahmniPatientProgram;


import org.openmrs.Auditable;
import org.openmrs.BaseCustomizableData;
import org.openmrs.PatientProgram;
import org.openmrs.customdatatype.Customizable;


public class BahmniPatientProgram extends BaseCustomizableData<PatientProgramAttribute> implements Auditable, Customizable<PatientProgramAttribute> {

    private PatientProgram patientProgram;

    public BahmniPatientProgram(PatientProgram patientProgram){
        this.patientProgram = patientProgram;
    }

    @Override
    public Integer getId() {
        return this.patientProgram.getPatientProgramId();
    }

    @Override
    public void setId(Integer patientProgramId) {
        this.patientProgram.setPatientProgramId(patientProgramId);
    }

    public PatientProgram getPatientProgram() {
        return patientProgram;
    }

    public void setPatientProgram(PatientProgram patientProgram) {
        this.patientProgram = patientProgram;
    }
}
