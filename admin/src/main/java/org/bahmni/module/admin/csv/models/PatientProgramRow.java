package org.bahmni.module.admin.csv.models;

import org.bahmni.csv.CSVEntity;
import org.bahmni.csv.annotation.CSVHeader;
import org.bahmni.csv.annotation.CSVRegexHeader;
import org.bahmni.csv.KeyValue;

import static org.bahmni.module.admin.csv.utils.CSVUtils.getDateFromString;

import java.text.ParseException;
import java.util.Date;
import java.util.List;

public class PatientProgramRow extends CSVEntity {
    @CSVHeader(name = "Registration Number")
    public String patientIdentifier;

    @CSVRegexHeader(pattern = "Patient.*")
    public List<KeyValue> patientAttributes;

    @CSVHeader(name = "Program")
    public String programName;

    @CSVHeader(name = "EnrollmentDate")
    public String enrollmentDateTime;

    public Date getEnrollmentDate() throws ParseException {
        return getDateFromString(enrollmentDateTime);
    }
}
