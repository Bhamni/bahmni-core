package org.bahmni.module.bahmnicore.mapper.builder;

import org.openmrs.Patient;

import java.util.UUID;

public class PatientBuilder {

    private final Patient patient;

    public PatientBuilder() {
        patient = new Patient();
        withUUID(UUID.randomUUID().toString());
    }

    public PatientBuilder withUUID(String patientUuid) {
        patient.setUuid(patientUuid);
        return this;
    }

    public Patient build() {
        return patient;
    }
}
