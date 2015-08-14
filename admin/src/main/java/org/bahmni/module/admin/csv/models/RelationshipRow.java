package org.bahmni.module.admin.csv.models;

import org.bahmni.csv.CSVEntity;
import org.bahmni.csv.annotation.CSVHeader;

public class RelationshipRow extends CSVEntity {

    @CSVHeader(name = "PersonA")
    private String personA;

    @CSVHeader(name = "PersonB")
    private String personB;

    @CSVHeader(name = "AIsToB")
    private String aIsToB;

    @CSVHeader(name = "BIsToA")
    private String bIsToA;

    @CSVHeader(name = "StartDate")
    private String startDate;

    @CSVHeader(name = "EndDate")
    private String endDate;

    public RelationshipRow(String personA, String personB, String aIsToB, String bIsToA, String startDate, String endDate) {
        this.personA = personA;
        this.personB = personB;
        this.aIsToB = aIsToB;
        this.bIsToA = bIsToA;
        this.startDate = startDate;
        this.endDate = endDate;
    }

    public RelationshipRow() {
    }

    public String getPersonA() {
        return personA;
    }

    public void setPersonA(String personA) {
        this.personA = personA;
    }

    public String getPersonB() {
        return personB;
    }

    public void setPersonB(String personB) {
        this.personB = personB;
    }

    public String getaIsToB() {
        return aIsToB;
    }

    public void setaIsToB(String aIsToB) {
        this.aIsToB = aIsToB;
    }

    public String getbIsToA() {
        return bIsToA;
    }

    public void setbIsToA(String bIsToA) {
        this.bIsToA = bIsToA;
    }

    public String getStartDate() {
        return startDate;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    public String getEndDate() {
        return endDate;
    }

    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }
}
