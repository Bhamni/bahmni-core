package org.bahmni.module.bahmnicore.service;

import org.bahmni.module.bahmnicore.contract.patient.response.PatientConfigResponse;
import org.bahmni.module.bahmnicore.contract.patient.response.PatientResponse;
import org.bahmni.module.bahmnicore.contract.patient.PatientSearchParameters;
import org.bahmni.module.bahmnicore.model.BahmniPatient;
import org.openmrs.Patient;

import java.util.List;

public interface BahmniPatientService {
    public PatientConfigResponse getConfig();
    public Patient createPatient(BahmniPatient bahmniPatient);
    public List<PatientResponse> search(PatientSearchParameters searchParameters);
    public List<Patient> get(String partialIdentifier);
}
