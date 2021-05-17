package org.bahmni.module.bahmnicore.dao;

import org.bahmni.module.bahmnicore.contract.patient.PatientSearchParameters;
import org.bahmni.module.bahmnicore.contract.patient.response.PatientResponse;
import org.openmrs.Location;
import org.openmrs.Patient;
import org.openmrs.RelationshipType;

import java.util.List;
import java.util.function.Supplier;

public interface PatientDao {

    /***
     * Please do not use this method, use the getPatients(PatientSearchParameters ...) instead.
     */
    @Deprecated
    public List<PatientResponse> getPatients(String identifier, String name, String customAttribute,
                                             String addressFieldName, String addressFieldValue, Integer length, Integer offset,
                                             String[] patientAttributes, String programAttribute, String programAttributeField,
                                             String[] addressSearchResultFields, String[] patientSearchResultFields, String loginLocationUuid, Boolean filterPatientsByLocation, Boolean filterOnAllIdentifiers);

    List<PatientResponse> getPatientsUsingLuceneSearch(String identifier, String name, String customAttribute,
                                                       String addressFieldName, String addressFieldValue, Integer length,
                                                       Integer offset, String[] customAttributeFields, String programAttributeFieldValue,
                                                       String programAttributeFieldName, String[] addressSearchResultFields,
                                                       String[] patientSearchResultFields, String loginLocationUuid, Boolean filterPatientsByLocation, Boolean filterOnAllIdentifiers);

    public Patient getPatient(String identifier);

    public List<Patient> getPatients(String partialIdentifier, boolean shouldMatchExactPatientId);

    public List<RelationshipType> getByAIsToB(String aIsToB);

    public List<PatientResponse> getPatients(PatientSearchParameters searchParameters, Supplier<Location> visitLocation, Supplier<List<String>> configuredAddressFields);

    public List<String> getConfiguredPatientAddressFields();
}
