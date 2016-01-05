package org.bahmni.module.bahmnicore.contract.patient;

import org.apache.commons.lang.StringUtils;
import org.openmrs.module.webservices.rest.web.RequestContext;

import java.util.Map;

public class PatientSearchParameters {
    private String identifier;
    private String name;
    private String addressFieldName;
    private String addressFieldValue;
    private Integer start;
    private Integer length;
    private String customAttribute;
    private String[] patientAttributes;
    private String identifierPrefix;

    public PatientSearchParameters(RequestContext context) {
        String query = context.getParameter("q");
        String identifier = context.getParameter("identifier");
        String identifierPrefix = context.getParameter("identifierPrefix");
        if (identifier != null) {
            this.setIdentifier(identifier);
            this.setIdentifierPrefix(identifierPrefix);
        } else if (query != null) {
            if (query.matches(".*\\d+.*")) {
                this.setIdentifier(query);
                this.setIdentifierPrefix("");
            } else {
                this.setName(query);
            }
        }
        this.setStart(context.getStartIndex());
        this.setLength(context.getLimit());
        this.setCustomAttribute(context.getParameter("custom_attribute"));
        String addressFieldNameFromRequest = context.getParameter("address_field_name");
        if  (StringUtils.isNotEmpty(addressFieldNameFromRequest)){
            this.setAddressFieldName(addressFieldNameFromRequest);
        } else {
            this.setAddressFieldName("city_village");
        }
        this.setAddressFieldValue(context.getParameter("address_field_value"));
        Map parameterMap = context.getRequest().getParameterMap();
        this.patientAttributes = (String[]) parameterMap.get("patientAttributes");
    }

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public String getIdentifierPrefix() {return identifierPrefix;}

    public void setIdentifierPrefix(String identifierPrefix) {this.identifierPrefix = identifierPrefix;}

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddressFieldName() {
        return addressFieldName;
    }

    public void setAddressFieldName(String addressFieldName) {
        this.addressFieldName = addressFieldName;
    }

    public String getAddressFieldValue() {
        return addressFieldValue;
    }

    public void setAddressFieldValue(String addressFieldValue) {
        this.addressFieldValue = addressFieldValue;
    }

    public Integer getStart() {
        return start;
    }

    public void setStart(Integer start) {
        this.start = start;
    }

    public Integer getLength() {
        return length;
    }

    public void setLength(Integer length) {
        this.length = length;
    }

    public String getCustomAttribute() {
        return customAttribute;
    }

    public void setCustomAttribute(String customAttribute) {
        this.customAttribute = customAttribute;
    }

    public String[] getPatientAttributes() {
        return patientAttributes;
    }

    public void setPatientAttributes(String[] patientAttributes) {
        this.patientAttributes = patientAttributes;
    }
}
