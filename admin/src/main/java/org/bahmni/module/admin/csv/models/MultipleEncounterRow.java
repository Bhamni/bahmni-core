package org.bahmni.module.admin.csv.models;

import org.bahmni.csv.CSVEntity;
import org.bahmni.csv.annotation.CSVHeader;
import org.bahmni.csv.annotation.CSVRegexHeader;
import org.bahmni.csv.annotation.CSVRepeatingRegexHeaders;
import org.bahmni.csv.KeyValue;

import static org.bahmni.module.admin.csv.utils.CSVUtils.getDateFromString;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MultipleEncounterRow extends CSVEntity {

    @CSVHeader(name = "Registration Number")
    public String patientIdentifier;

    @CSVHeader(name = "encounterType")
    public String encounterType;

    @CSVHeader(name = "visitType")
    public String visitType;

    @CSVHeader(name = "Visit Start Date", optional = true)
    public String visitStartDate;

    @CSVHeader(name = "Visit End Date", optional = true)
    public String visitEndDate;

    @CSVRegexHeader(pattern = "Patient.*")
    public List<KeyValue> patientAttributes;

    @CSVRepeatingRegexHeaders(type = EncounterRow.class)
    public List<EncounterRow> encounterRows;

    public List<EncounterRow> getNonEmptyEncounterRows() {
        List<EncounterRow> nonEmptyEncounters = new ArrayList<>();
        if (encounterRows == null)
            return nonEmptyEncounters;

        for (EncounterRow encounterRow : encounterRows) {
            if (!encounterRow.isEmpty()) {
                nonEmptyEncounters.add(encounterRow);
            }
        }
        return nonEmptyEncounters;
    }

    public Date getVisitStartDate() throws ParseException {
        if (visitStartDate == null)
            return null;
        return getDateFromString(visitStartDate);
    }

    public Date getVisitEndDate() throws ParseException {
        if (visitEndDate == null)
            return null;
        return getDateFromString(visitEndDate);
    }
}

