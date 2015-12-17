package org.bahmni.module.bahmnicore.model.bahmniPatientProgram;


import org.openmrs.attribute.Attribute;
import org.openmrs.attribute.BaseAttribute;


public class PatientProgramAttribute extends BaseAttribute<PatientProgramAttributeType, BahmniPatientProgram> implements Attribute<PatientProgramAttributeType, BahmniPatientProgram> {

    private Integer patientProgramAttributeId;

    @Override
    public Integer getId() {
        return getPatientProgramAttributeId();
    }

    @Override
    public void setId(Integer id) {
        setPatientProgramAttributeId(id);
    }

    public BahmniPatientProgram getBahmniPatientProgram() {
        return getOwner();
    }

    public void setBahmniPatientProgram(BahmniPatientProgram patientProgram) {
        setOwner(patientProgram);
    }

    public Integer getPatientProgramAttributeId() {
        return patientProgramAttributeId;
    }

    public void setPatientProgramAttributeId(Integer id) {
        this.patientProgramAttributeId = id;
    }
}
