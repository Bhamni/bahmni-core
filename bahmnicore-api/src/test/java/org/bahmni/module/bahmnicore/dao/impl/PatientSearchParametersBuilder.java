package org.bahmni.module.bahmnicore.dao.impl;


import org.bahmni.module.bahmnicore.contract.patient.PatientSearchParameters;

public class PatientSearchParametersBuilder {
    private String identifier;
    private String name;

    private String addressFieldName;
    private String addressFieldValue;
    private String[] addressSearchResultFields;

    private String customAttribute;
    private String[] patientAttributes;
    private String[] patientSearchResultFields;

    private String programAttributeFieldName;
    private String programAttributeFieldValue;

    private String loginLocationUuid;

    private Boolean filterPatientsByLocation;
    private Boolean filterOnAllIdentifiers;

    private Integer start;
    private Integer length;

    public static PatientSearchParametersBuilder defaultValues() {
        PatientSearchParametersBuilder pspb = new PatientSearchParametersBuilder();
        pspb.identifier = "";
        pspb.name = "";

        pspb.addressFieldName = "";
        pspb.addressFieldValue  = "";
        pspb.addressSearchResultFields = new String[0];

        pspb.customAttribute = "";
        pspb.patientAttributes = new String[0];
        pspb.patientSearchResultFields = new String[0];

        pspb.programAttributeFieldName = "";
        pspb.programAttributeFieldValue = "";

        pspb.loginLocationUuid = "";

        pspb.filterPatientsByLocation = Boolean.FALSE;
        pspb.filterOnAllIdentifiers = Boolean.FALSE;

        pspb.start = 0;
        pspb.length = 100;
        return pspb;
    }

    public PatientSearchParametersBuilder withIdentifier(String  identifier) {
        this.identifier = identifier;
        return this;
    }


    public PatientSearchParametersBuilder withName(String name) {
        this.name = name;
        return this;
    }

    public PatientSearchParametersBuilder withAddressFieldName(String addressFieldName) {
        this.addressFieldName = addressFieldName;
        return this;
    }

    public PatientSearchParametersBuilder withAddressFieldValue(String addressFieldValue) {
        this.addressFieldValue = addressFieldValue;
        return this;
    }

    public PatientSearchParametersBuilder withAddressSearchResultFields(String[] addressSearchResultFields) {
        this.addressSearchResultFields = addressSearchResultFields;
        return this;
    }

    public PatientSearchParametersBuilder withCustomAttribute(String customAttribute) {
        this.customAttribute = customAttribute;
        return this;
    }

    public PatientSearchParametersBuilder withPatientAttributes(String[] patientAttributes) {
        this.patientAttributes = patientAttributes;
        return this;
    }

    public PatientSearchParametersBuilder withPatientSearchResultFields(String[] patientSearchResultFields) {
        this.patientSearchResultFields = patientSearchResultFields;
        return this;
    }

    public PatientSearchParametersBuilder withProgramAttributeFieldName(String programAttributeFieldName) {
        this.programAttributeFieldName = programAttributeFieldName;
        return this;
    }

    public PatientSearchParametersBuilder withProgramAttributeFieldValue(String programAttributeFieldValue) {
        this.programAttributeFieldValue = programAttributeFieldValue;
        return this;
    }

    public PatientSearchParametersBuilder withLoginLocationUuid(String loginLocationUuid) {
        this.loginLocationUuid = loginLocationUuid;
        return this;
    }

    public PatientSearchParametersBuilder withFilterPatientsByLocation(Boolean filterPatientsByLocation) {
        this.filterPatientsByLocation = filterPatientsByLocation;
        return this;
    }

    public PatientSearchParametersBuilder withFilterOnAllIdentifiers(Boolean filterOnAllIdentifiers) {
        this.filterOnAllIdentifiers = filterOnAllIdentifiers;
        return this;
    }

    public PatientSearchParametersBuilder withStart(Integer start) {
        this.start = start;
        return this;
    }

    public PatientSearchParametersBuilder withLength(Integer length) {
        this.length = length;
        return this;
    }

    public PatientSearchParameters build() {
        PatientSearchParameters searchParameters = new PatientSearchParameters();
        searchParameters.setIdentifier(this.identifier);
        searchParameters.setName(this.name);
        searchParameters.setAddressFieldName(this.addressFieldName);
        searchParameters.setAddressFieldValue(this.addressFieldValue);
        searchParameters.setAddressSearchResultFields(this.addressSearchResultFields);
        searchParameters.setCustomAttribute(this.customAttribute);
        searchParameters.setPatientAttributes(this.patientAttributes);
        searchParameters.setPatientSearchResultFields(this.patientSearchResultFields);
        searchParameters.setProgramAttributeFieldName(this.programAttributeFieldName);
        searchParameters.setProgramAttributeFieldValue(this.programAttributeFieldValue);
        searchParameters.setLoginLocationUuid(this.loginLocationUuid);
        searchParameters.setLength(this.length);
        searchParameters.setStart(this.start);
        searchParameters.setFilterPatientsByLocation(this.filterPatientsByLocation);
        searchParameters.setFilterOnAllIdentifiers(this.filterOnAllIdentifiers);
        return searchParameters;
    }
}
