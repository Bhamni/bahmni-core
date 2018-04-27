package org.bahmni.module.bahmnicore.contract.patient.response;



import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.map.JsonSerializer;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.SerializerProvider;
import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.openmrs.api.context.Context;
import org.springframework.stereotype.Component;

public class PatientResponse {

	private static Log log = LogFactory.getLog(PatientResponse.class);
	
    private String uuid;
    private Date birthDate;
    private String extraIdentifiers;
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
    private Object patientProgramAttributeValue;
    private Boolean hasBeenAdmitted;

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

    @JsonSerialize(using=JsonDateSerializer.class)
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
        this.addressFieldValue = localizeAddressFieldValue(addressFieldValue);
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

    public Object getPatientProgramAttributeValue() {
        return patientProgramAttributeValue;
    }

    public void setPatientProgramAttributeValue(Object patientProgramAttributeValue) {
        this.patientProgramAttributeValue = patientProgramAttributeValue;
    }

    public Boolean getHasBeenAdmitted() {
        return hasBeenAdmitted;
    }

    public void setHasBeenAdmitted(Boolean hasBeenAdmitted) {
        this.hasBeenAdmitted = hasBeenAdmitted;
    }

    public String getExtraIdentifiers() {
        return extraIdentifiers;
    }

    public void setExtraIdentifiers(String extraIdentifiers) {
        this.extraIdentifiers = localizePatientIdentifierTypes(extraIdentifiers);
    }

    public int getPersonId() {
        return personId;
    }

    public void setPersonId(int personId) {
        this.personId = personId;
    }

    /**
     * Used to serialize Java.util.Date, which is not a common JSON
     * type, so we have to create a custom serialize method;
     */
    @Component
    public static  class JsonDateSerializer extends JsonSerializer<Date> {
        private static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        @Override
        public void serialize(Date date, JsonGenerator gen, SerializerProvider provider)
                throws IOException {
            String formattedDate = dateFormat.format(date);
            gen.writeString(formattedDate);
        }
    }
    
    /**
     * Localizes the address value of an address field value string.
     * Eg. {"addressField" : "address.highStreet"} -> {"addressField" : "High Street"}
     * 
     * @param addressFieldValue The address field value JSON.
     * @return The localized address field value JSON.
     */
    public static String localizeAddressFieldValue(String addressFieldValue) {
    	try {
    		HashMap<String, String> map = new ObjectMapper().readValue(addressFieldValue, HashMap.class);
    		for (String addressField : map.keySet()) {
    			String addressValue = Context.getMessageSourceService().getMessage(map.get(addressField));
    			map.put(addressField, addressValue);
    		}
    		addressFieldValue = new ObjectMapper().writeValueAsString(map);
    	} catch (Exception e) {
    		log.warn("There was an error while localizing the address field Json in the patient response: \n" + addressFieldValue, e);
    	} 
    	return addressFieldValue;
    }

    /**
     * Localizes the patient identifier types of the extra identifiers.
     * Eg. {"pit.id1" : "98765321", "pit.id2" : "MRS-1234"} -> {"Primary ID" : "98765321", "Secondary ID" : "MRS-1234"} 
     * 
     * @param extraIdentifiers The extra identifiers JSON string.
     * @return The localized extra identifiers JSON string.
     */
    public static String localizePatientIdentifierTypes(String extraIdentifiers) {
    	try {
    		HashMap<String, String> map = new ObjectMapper().readValue(extraIdentifiers, HashMap.class);
    		for (String pit : map.keySet()) {
    			String l10nPit = Context.getMessageSourceService().getMessage(pit);
    			if (!pit.equals(l10nPit)) {
    				extraIdentifiers = extraIdentifiers.replaceAll(pit, l10nPit);
    			}
    		}
    	} catch (Exception e) {
    		log.warn("There was an error while localizing the extra identifiers Json in the patient response: \n" + extraIdentifiers, e);
    	} 
    	return extraIdentifiers;
    }
}
