package org.bahmni.module.bahmnicore.model.bahmniPatientProgram;


import org.openmrs.attribute.Attribute;
import org.openmrs.attribute.BaseAttribute;


public class BahmniPatientProgramAttribute extends BaseAttribute<BahmniPatientProgramAttributeType, BahmniPatientProgram> implements Attribute<BahmniPatientProgramAttributeType, BahmniPatientProgram> {

    private Integer patientProgramAttributeId;

    @Override
    public Integer getId() {
        return getPatientProgramAttributeId();
    }

    @Override
    public void setId(Integer id) {
        setPatientProgramAttributeId(id);
    }

    public BahmniPatientProgram getPatientProgram() {
        return getOwner();
    }

    public void setPatientProgram(BahmniPatientProgram patientProgram) {
        setOwner(patientProgram);
    }

    public Integer getPatientProgramAttributeId() {
        return patientProgramAttributeId;
    }

    public void setPatientProgramAttributeId(Integer patientProgramAttribute) {
        this.patientProgramAttributeId = patientProgramAttribute;
    }
}
