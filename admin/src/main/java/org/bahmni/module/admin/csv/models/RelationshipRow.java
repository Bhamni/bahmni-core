package org.bahmni.module.admin.csv.models;

import org.apache.commons.lang3.StringUtils;
import org.bahmni.csv.CSVEntity;
import org.bahmni.csv.annotation.CSVHeader;

public class RelationshipRow extends CSVEntity {

    @CSVHeader(name = "Relationship.personA-registration-number")
    private String personA;

    @CSVHeader(name = "Relationship.personB-registration-number")
    private String personB;

    @CSVHeader(name = "Relationship.type")
    private String relationshipType;

    @CSVHeader(name = "Relationship.start-date")
    private String startDate;

    @CSVHeader(name = "Relationship.end-date")
    private String endDate;

    public RelationshipRow() {
    }

    public RelationshipRow(String personA, String personB, String relationshipType, String startDate, String endDate) {
        this.personA = personA;
        this.personB = personB;
        this.relationshipType = relationshipType;
        this.startDate = startDate;
        this.endDate = endDate;
    }

    public boolean isEmpty() {
        return StringUtils.isBlank(personB) && StringUtils.isBlank(relationshipType);
    }

    public String[] getRowValues() {
        return new String[]{personB, relationshipType, startDate, endDate};
    }

    public RelationshipRow getHeaders() {
        return new RelationshipRow("Relationship.personA-registration-number", "Relationship.personB-registration-number", "Relationship.type-id", "Relationship.start-date", "Relationship.end-date");
    }

    public String getPersonB() {
        return personB;
    }

    public void setPersonB(String personB) {
        this.personB = personB;
    }

    public String getRelationshipType() {
        return relationshipType;
    }

    public void setRelationshipType(String relationshipType) {
        this.relationshipType = relationshipType;
    }

    public String getEndDate() {
        return endDate;
    }

    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }

    public String getStartDate() {
        return startDate;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    public String getPersonA() {
        return personA;
    }

    public void setPersonA(String personA) {
        this.personA = personA;
    }
}
