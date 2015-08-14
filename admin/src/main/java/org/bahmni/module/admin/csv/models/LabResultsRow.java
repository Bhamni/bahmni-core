package org.bahmni.module.admin.csv.models;

import org.bahmni.csv.CSVEntity;
import org.bahmni.csv.KeyValue;
import org.bahmni.csv.annotation.CSVHeader;
import org.bahmni.csv.annotation.CSVRegexHeader;
import org.bahmni.csv.annotation.CSVRepeatingHeaders;

import static org.bahmni.module.admin.csv.utils.CSVUtils.getDateFromString;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class LabResultsRow extends CSVEntity {
    @CSVHeader(name = "Registration Number")
    private String patientIdentifier;

    @CSVRegexHeader(pattern = "Patient.*")
    private List<KeyValue> patientAttributes;

    @CSVHeader(name = "Date")
    private String testDateString;

    @CSVHeader(name = "Visit Type")
    private String visitType;

    @CSVRepeatingHeaders(names = {"Test", "Result"}, type = LabResultRow.class)
    private List<LabResultRow> testResults;

    public List<LabResultRow> getTestResults() {
        List<LabResultRow> labResultRows = new ArrayList<>();
        for (LabResultRow testResult : testResults) {
            if (!testResult.isEmpty()) {
                labResultRows.add(testResult);
            }
        }
        return labResultRows;
    }

    public LabResultsRow setTestResults(List<LabResultRow> testResults) {
        this.testResults = testResults;
        return this;
    }

    public Date getTestDate() throws ParseException {
        return getDateFromString(this.testDateString);
    }

    public List<KeyValue> getPatientAttributes() {
        return patientAttributes;
    }

    public LabResultsRow setPatientAttributes(List<KeyValue> patientAttributes) {
        this.patientAttributes = patientAttributes;
        return this;
    }

    public String getPatientIdentifier() {
        return patientIdentifier;
    }

    public LabResultsRow setPatientIdentifier(String patientIdentifier) {
        this.patientIdentifier = patientIdentifier;
        return this;
    }

    public LabResultsRow setTestDateString(String testDateString) {
        this.testDateString = testDateString;
        return this;
    }

    public String getVisitType() {
        return visitType;
    }

    public LabResultsRow setVisitType(String visitType) {
        this.visitType = visitType;
        return this;
    }
}

