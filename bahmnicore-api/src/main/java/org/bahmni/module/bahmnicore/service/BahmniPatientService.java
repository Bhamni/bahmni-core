package org.bahmni.module.bahmnicore.service;

import org.bahmni.module.bahmnicore.contract.patient.PatientSearchParameters;
import org.bahmni.module.bahmnicore.contract.patient.mapper.PatientResponseMapper;
import org.bahmni.module.bahmnicore.contract.patient.response.PatientConfigResponse;
import org.bahmni.module.bahmnicore.contract.patient.response.PatientResponse;
import org.openmrs.Patient;
import org.openmrs.Person;
import org.openmrs.RelationshipType;

import java.util.List;

public interface BahmniPatientService {
    public PatientConfigResponse getConfig();

    public List<PatientResponse> search(PatientSearchParameters searchParameters);

    List<PatientResponse> luceneSearch(PatientSearchParameters searchParameters);

    List<PatientResponse> searchSimilarPatients(PatientSearchParameters searchParameters);

    public List<Patient> get(String partialIdentifier, boolean shouldMatchExactPatientId);

    public List<RelationshipType> getByAIsToB(String aIsToB);
}
