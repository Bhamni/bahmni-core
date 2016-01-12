package org.bahmni.module.bahmnicore.model.bahmniPatientProgram;


import org.openmrs.attribute.AttributeType;
import org.openmrs.attribute.BaseAttributeType;

public class ProgramAttributeType extends BaseAttributeType<BahmniPatientProgram> implements AttributeType<BahmniPatientProgram> {
    private Integer programAttributeTypeId;

    @Override
    public Integer getId() {
        return getProgramAttributeTypeId();
    }

    @Override
    public void setId(Integer id) {
        setProgramAttributeTypeId(id);
    }

    public Integer getProgramAttributeTypeId() {
        return programAttributeTypeId;
    }

    public void setProgramAttributeTypeId(Integer programAttributeTypeId) {
        this.programAttributeTypeId = programAttributeTypeId;
    }
}
