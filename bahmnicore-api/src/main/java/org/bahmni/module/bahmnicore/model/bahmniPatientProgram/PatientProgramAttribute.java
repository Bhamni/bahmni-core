package org.bahmni.module.bahmnicore.model.bahmniPatientProgram;

import org.hibernate.search.annotations.DocumentId;
import org.hibernate.search.annotations.Indexed;
import org.openmrs.attribute.Attribute;
import org.openmrs.attribute.BaseAttribute;

@Indexed
public class PatientProgramAttribute extends BaseAttribute<ProgramAttributeType, BahmniPatientProgram> implements Attribute<ProgramAttributeType, BahmniPatientProgram> {

    @DocumentId
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
        setOwner(new BahmniPatientProgram(patientProgram));
    }

    public Integer getPatientProgramAttributeId() {
        return patientProgramAttributeId;
    }

    public void setPatientProgramAttributeId(Integer id) {
        this.patientProgramAttributeId = id;
    }
}
