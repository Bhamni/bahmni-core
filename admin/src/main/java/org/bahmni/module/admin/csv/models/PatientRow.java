package org.bahmni.module.admin.csv.models;

import org.bahmni.csv.CSVEntity;
import org.bahmni.csv.KeyValue;
import org.bahmni.csv.annotation.CSVHeader;
import org.bahmni.csv.annotation.CSVRegexHeader;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.bahmni.module.admin.csv.utils.CSVUtils.getDateFromString;

public class PatientRow extends CSVEntity {
    @CSVHeader(name = "First Name")
    public String firstName;
    @CSVHeader(name = "Middle Name")
    public String middleName;
    @CSVHeader(name = "Last Name")
    public String lastName;
    @CSVHeader(name = "Registration Number")
    public String registrationNumber;
    @CSVHeader(name = "Gender")
    public String gender;
    @CSVHeader(name = "Age", optional = true)
    public String age;
    @CSVHeader(name = "Birth Date", optional = true)
    public String birthdate;
    @CSVHeader(name = "Registration Date", optional = true)
    public String registrationDate;

    @CSVRegexHeader(pattern = "Address.*")
    public List<KeyValue> addressParts;

    @CSVRegexHeader(pattern = "Attribute.*")
    public List<KeyValue> attributes = new ArrayList<>();

    public Date getRegistrationDate() throws ParseException {
        if (registrationDate == null)
            return null;
        return getDateFromString(registrationDate);
    }
}
