package org.bahmni.module.bahmnicore.model.bahmniPatientProgram;

import org.openmrs.PatientProgram;
import org.openmrs.customdatatype.CustomValueDescriptor;
import org.openmrs.customdatatype.Customizable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class BahmniPatientProgram extends PatientProgram implements Customizable<PatientProgramAttribute> {

    private Set<PatientProgramAttribute> attributes = new LinkedHashSet();

    public BahmniPatientProgram() {
        super();
    }

    public BahmniPatientProgram(PatientProgram patientProgram) {
        super(patientProgram.getPatientProgramId());
    }

    @Override
    public Set<PatientProgramAttribute> getAttributes() {
        return attributes;
    }

    @Override
    public Collection<PatientProgramAttribute> getActiveAttributes() {
        ArrayList<PatientProgramAttribute> ret = new ArrayList<>();

        if (this.getAttributes() != null) {
            for (PatientProgramAttribute attr : this.getAttributes()) {
                if (!attr.isVoided()) {
                    ret.add(attr);
                }
            }
        }

        return ret;
    }

    @Override
    public List<PatientProgramAttribute> getActiveAttributes(CustomValueDescriptor ofType) {
        ArrayList<PatientProgramAttribute> ret = new ArrayList<>();

        if (this.getAttributes() != null) {
            for (PatientProgramAttribute attr : this.getAttributes()) {
                if (attr.getAttributeType().equals(ofType) && !attr.isVoided()) {
                    ret.add(attr);
                }
            }
        }

        return ret;
    }

    @Override
    public void addAttribute(PatientProgramAttribute attribute) {
        if (this.getAttributes() == null) {
            this.setAttributes(new LinkedHashSet());
        }

        this.getAttributes().add(attribute);
        attribute.setOwner(this);
    }

    public void setAttributes(Set<PatientProgramAttribute> attributes) {
        this.attributes = attributes;
    }

    public void setAttribute(PatientProgramAttribute attribute) {
        if (this.getAttributes() == null) {
            this.addAttribute(attribute);
        } else {
            if (this.getActiveAttributes(attribute.getAttributeType()).size() == 1) {
                PatientProgramAttribute i$ = this.getActiveAttributes(attribute.getAttributeType()).get(0);
                if (!i$.getValue().equals(attribute.getValue())) {
                    if (i$.getId() != null) {
                        i$.setVoided(Boolean.valueOf(true));
                    } else {
                        this.getAttributes().remove(i$);
                    }

                    this.getAttributes().add(attribute);
                    attribute.setOwner(this);
                }
            } else {
                for (PatientProgramAttribute existing : this.getActiveAttributes(attribute.getAttributeType())) {
                    if (existing.getAttributeType().equals(attribute.getAttributeType())) {
                        if (existing.getId() != null) {
                            existing.setVoided(Boolean.valueOf(true));
                        } else {
                            this.getAttributes().remove(existing);
                        }
                    }
                }

                this.getAttributes().add(attribute);
                attribute.setOwner(this);
            }
        }
    }
}
