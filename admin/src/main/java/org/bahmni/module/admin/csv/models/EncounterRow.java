package org.bahmni.module.admin.csv.models;

import org.bahmni.csv.CSVEntity;
import org.bahmni.csv.annotation.CSVHeader;
import org.bahmni.csv.annotation.CSVRegexHeader;
import org.bahmni.csv.KeyValue;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class EncounterRow extends CSVEntity {
    public static final String ENCOUNTER_DATE_PATTERN = "d-M-yyyy";

    @CSVHeader(name = "EncounterDate")
    public String encounterDateTime;

    @CSVRegexHeader(pattern = "Obs.*")
    public List<KeyValue> obsRows;

    @CSVRegexHeader(pattern = "Diagnosis.*")
    public List<KeyValue> diagnosesRows;

    @CSVRegexHeader(pattern = "Lab.*")
    public List<KeyValue> labResultRows;

    public Date getEncounterDate() throws ParseException {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(ENCOUNTER_DATE_PATTERN);
        simpleDateFormat.setLenient(false);
        return simpleDateFormat.parse(encounterDateTime);
    }

    public boolean hasObservations() {
        return obsRows != null && !obsRows.isEmpty();
    }

    public boolean hasDiagnoses() {
        return diagnosesRows != null && !diagnosesRows.isEmpty();
    }

}
