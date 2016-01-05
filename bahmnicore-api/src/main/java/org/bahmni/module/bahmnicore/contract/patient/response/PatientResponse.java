package org.bahmni.module.bahmnicore.contract.patient.response;


import java.util.Calendar;
import java.util.Date;

public class PatientResponse {

    private String uuid;
    private Date birthDate;

    public int getPersonId() {
        return personId;
    }

    public void setPersonId(int personId) {
        this.personId = personId;
    }

    private int personId;
    private Date deathDate;
    private String identifier;
    private String addressFieldValue;
    private String givenName;
    private String middleName;
    private String familyName;
    private String gender;
    private Date dateCreated;
    private String activeVisitUuid;
    private String customAttribute;

    public PatientResponse() {
    }

    public String getAge() {
        if (birthDate == null)
            return null;

        // Use default end date as today.
        Calendar today = Calendar.getInstance();

        // If date given is after date of death then use date of death as end date
        if (getDeathDate() != null && today.getTime().after(getDeathDate())) {
            today.setTime(getDeathDate());
        }

        Calendar bday = Calendar.getInstance();
        bday.setTime(birthDate);

        int age = today.get(Calendar.YEAR) - bday.get(Calendar.YEAR);

        // Adjust age when today's date is before the person's birthday
        int todaysMonth = today.get(Calendar.MONTH);
        int bdayMonth = bday.get(Calendar.MONTH);
        int todaysDay = today.get(Calendar.DAY_OF_MONTH);
        int bdayDay = bday.get(Calendar.DAY_OF_MONTH);

        if (todaysMonth < bdayMonth) {
            age--;
        } else if (todaysMonth == bdayMonth && todaysDay < bdayDay) {
            // we're only comparing on month and day, not minutes, etc
            age--;
        }

        return Integer.toString(age);
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public Date getBirthDate() {
        return birthDate;
    }

    public void setBirthDate(Date birthDate) {
        this.birthDate = birthDate;
    }

    public Date getDeathDate() {
        return deathDate;
    }

    public void setDeathDate(Date deathDate) {
        this.deathDate = deathDate;
    }

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public String getAddressFieldValue() {
        return addressFieldValue;
    }

    public void setAddressFieldValue(String addressFieldValue) {
        this.addressFieldValue = addressFieldValue;
    }

    public String getGivenName() {
        return givenName;
    }

    public void setGivenName(String givenName) {
        this.givenName = givenName;
    }

    public String getMiddleName() {
        return middleName;
    }

    public void setMiddleName(String middleName) {
        this.middleName = middleName;
    }

    public String getFamilyName() {
        return familyName;
    }

    public void setFamilyName(String familyName) {
        this.familyName = familyName;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public Date getDateCreated() {
        return dateCreated;
    }

    public void setDateCreated(Date dateCreated) {
        this.dateCreated = dateCreated;
    }

    public String getActiveVisitUuid() {
        return activeVisitUuid;
    }

    public void setActiveVisitUuid(String activeVisitUuid) {
        this.activeVisitUuid = activeVisitUuid;
    }

    public String getCustomAttribute() {
        return customAttribute;
    }

    public void setCustomAttribute(String customAttribute) {
        this.customAttribute = customAttribute;
    }
}
