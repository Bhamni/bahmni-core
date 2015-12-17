package org.bahmni.module.bahmnicore.model.bahmniPatientProgram;


import org.openmrs.attribute.AttributeType;
import org.openmrs.attribute.BaseAttributeType;

public class PatientProgramAttributeType extends BaseAttributeType<BahmniPatientProgram> implements AttributeType<BahmniPatientProgram> {
    private Integer patientProgramAttributeTypeId;

    @Override
    public Integer getId() {
        return getPatientProgramAttributeTypeId();
    }

    @Override
    public void setId(Integer id) {
        setPatientProgramAttributeTypeId(id);
    }

    public Integer getPatientProgramAttributeTypeId() {
        return patientProgramAttributeTypeId;
    }

    public void setPatientProgramAttributeTypeId(Integer patientProgramAttributeTypeId) {
        this.patientProgramAttributeTypeId = patientProgramAttributeTypeId;
    }
}
