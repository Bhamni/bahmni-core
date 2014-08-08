package org.bahmni.module.admin.csv.models;

import org.bahmni.csv.CSVEntity;
import org.bahmni.csv.CSVHeader;
import org.bahmni.csv.CSVRegexHeader;
import org.bahmni.csv.KeyValue;

import java.util.ArrayList;
import java.util.List;

public class EncounterRow extends CSVEntity {
    @CSVHeader(name = "registrationNumber")
    public String patientIdentifier;

    @CSVHeader(name = "Registration Date")
    public String encounterDateTime;

    @CSVRegexHeader(pattern = "Patient.*")
    public List<KeyValue> patientAttributes;

    @CSVRegexHeader(pattern = "Obs.*")
    public List<KeyValue> obsRows;

    @CSVRegexHeader(pattern = "Diagnosis.*")
    public List<KeyValue> diagnosesRows;

    public List<String> getDiagnoses() {
        List<String> aDiagnosesRows = new ArrayList<>();
        for (KeyValue diagnosesRow : diagnosesRows) {
            aDiagnosesRows.add(diagnosesRow.getValue());
        }
        return aDiagnosesRows;
    }
}
