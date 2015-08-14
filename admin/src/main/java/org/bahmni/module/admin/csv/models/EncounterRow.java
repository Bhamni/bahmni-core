package org.bahmni.module.admin.csv.models;

import org.apache.commons.lang.StringUtils;
import org.bahmni.csv.CSVEntity;
import org.bahmni.csv.annotation.CSVHeader;
import org.bahmni.csv.annotation.CSVRegexHeader;
import org.bahmni.csv.KeyValue;

import static org.bahmni.module.admin.csv.utils.CSVUtils.getDateFromString;

import java.text.ParseException;
import java.util.Date;
import java.util.List;

public class EncounterRow extends CSVEntity {

    @CSVHeader(name = "EncounterDate")
    public String encounterDateTime;

    @CSVRegexHeader(pattern = "Obs.*")
    public List<KeyValue> obsRows;

    @CSVRegexHeader(pattern = "Diagnosis.*")
    public List<KeyValue> diagnosesRows;

    public Date getEncounterDate() throws ParseException {
        return getDateFromString(encounterDateTime);
    }

    public boolean hasObservations() {
        return obsRows != null && !obsRows.isEmpty();
    }

    public boolean hasDiagnoses() {
        return diagnosesRows != null && !diagnosesRows.isEmpty();
    }

    public boolean isEmpty() {
        return areObservationsEmpty() && areDiagnosesEmpty() && noEncounterDate();
    }

    private boolean areDiagnosesEmpty() {
        if (diagnosesRows == null || diagnosesRows.isEmpty())
            return true;

        for (KeyValue diagnosisRow : diagnosesRows) {
            if (StringUtils.isNotBlank(diagnosisRow.getValue())) {
                return false;
            }
        }

        return true;
    }

    private boolean areObservationsEmpty() {
        if (obsRows == null || obsRows.isEmpty())
            return true;

        for (KeyValue obsRow : obsRows) {
            if (StringUtils.isNotBlank(obsRow.getValue())) {
                return false;
            }
        }

        return true;
    }

    private boolean noEncounterDate() {
        return StringUtils.isBlank(encounterDateTime);
    }
}
