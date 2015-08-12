package org.bahmni.module.admin.csv.service;

import org.apache.commons.lang.StringUtils;
import org.bahmni.csv.KeyValue;
import org.bahmni.module.admin.csv.models.PatientRow;
import org.bahmni.module.admin.csv.models.RelationshipRow;
import org.openmrs.*;
import org.openmrs.api.AdministrationService;
import org.openmrs.api.ConceptService;
import org.openmrs.api.PatientService;
import org.openmrs.api.PersonService;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.bahmni.module.admin.csv.utils.CSVUtils.getDateFromString;

public class CSVPatientService {

    private static final String EMR_PRIMARY_IDENTIFIER_TYPE = "emr.primaryIdentifierType";

    private PatientService patientService;
    private PersonService personService;
    private ConceptService conceptService;
    private AdministrationService administrationService;
    private CSVAddressService csvAddressService;

    public CSVPatientService(PatientService patientService, PersonService personService, ConceptService conceptService, AdministrationService administrationService, CSVAddressService csvAddressService) {
        this.patientService = patientService;
        this.personService = personService;
        this.conceptService = conceptService;
        this.administrationService = administrationService;
        this.csvAddressService = csvAddressService;
    }

    public Patient save(PatientRow patientRow) throws ParseException {
        Patient patient = new Patient();
        PersonName personName = new PersonName(patientRow.firstName, patientRow.middleName, patientRow.lastName);
        personName.setPreferred(true);
        patient.addName(personName);

        addPersonAttributes(patient, patientRow);

        if (!StringUtils.isBlank(patientRow.birthdate)) {
            patient.setBirthdate(getDateFromString(patientRow.birthdate));
        } else if (!StringUtils.isBlank(patientRow.age)) {
            patient.setBirthdateFromAge(Integer.parseInt(patientRow.age), new Date());
        }
        patient.setGender(patientRow.gender);
        patient.addIdentifier(new PatientIdentifier(patientRow.registrationNumber, getPatientIdentifierType(), null));

        List<KeyValue> addressParts = patientRow.addressParts;
        PersonAddress personAddress = csvAddressService.getPersonAddress(addressParts);
        if (personAddress != null) {
            patient.addAddress(personAddress);
        }

        patient.setPersonDateCreated(patientRow.getRegistrationDate());

        patientService.savePatient(patient);
        saveRelationships(patientRow, patient);

        return patient;
    }

    private void saveRelationships(PatientRow patientRow, Patient patientA) throws ParseException {
        List<Relationship> relationships = getRelationshipsFromPatientRow(patientRow, patientA);
        for (Relationship relationship : relationships) {
            personService.saveRelationship(relationship);
        }
    }

    private List<Relationship> getRelationshipsFromPatientRow(PatientRow patientRow, Patient patientA) throws ParseException {
        List<Relationship> relationships = new ArrayList<>();

        Relationship relationship;
        Patient patientB;
        for (RelationshipRow relationshipRow : patientRow.relationships) {

            if (StringUtils.isEmpty(relationshipRow.getPersonB())) {
                continue;
            }

            List<Patient> patientsMatchedByIdentifier = patientService.getPatients(null, relationshipRow.getPersonB(), null, true);

            if (null == patientsMatchedByIdentifier || patientsMatchedByIdentifier.size() == 0) {
                throw new RuntimeException("PersonB not found.");
            }

            patientB = patientsMatchedByIdentifier.get(0);

            if (null == patientB) {
                throw new RuntimeException("PersonB not found.");
            }

            relationship = new Relationship();

            try {
                relationship.setRelationshipType(new RelationshipType(Integer.parseInt(relationshipRow.getRelationshipType())));
            } catch (NumberFormatException e) {
                throw new RuntimeException("Invalid relationship type id.");
            }

            relationship.setPersonA(patientA);
            relationship.setPersonB(patientB);

            if (!StringUtils.isBlank(relationshipRow.getStartDate())) {
                relationship.setStartDate(getDateFromString(relationshipRow.getStartDate()));
            }

            if (!StringUtils.isBlank(relationshipRow.getEndDate())) {
                relationship.setEndDate(getDateFromString(relationshipRow.getEndDate()));
            }

            relationships.add(relationship);
        }
        return relationships;
    }

    private void addPersonAttributes(Patient patient, PatientRow patientRow) {
        for (KeyValue attribute : patientRow.attributes) {
            PersonAttributeType personAttributeType = findAttributeType(attribute.getKey());
            if (personAttributeType.getFormat().equalsIgnoreCase("org.openmrs.Concept")) {
                Concept concept = conceptService.getConcept(attribute.getValue());
                if (concept != null) {
                    patient.addAttribute(new PersonAttribute(personAttributeType, concept.getId().toString()));
                } else {
                    throw new RuntimeException("Invalid value for Attribute." + attribute.getKey());
                }
            } else if (personAttributeType.getFormat().equalsIgnoreCase("java.lang.String")) {
                patient.addAttribute(new PersonAttribute(findAttributeType(attribute.getKey()), attribute.getValue()));
            }
        }
    }

    private PersonAttributeType findAttributeType(String key) {
        for (PersonAttributeType personAttributeType : personService.getAllPersonAttributeTypes(false)) {
            if (key.equalsIgnoreCase(personAttributeType.getName())) {
                return personAttributeType;
            }
        }

        throw new RuntimeException(String.format("Person Attribute %s not found", key));
    }

    private PatientIdentifierType getPatientIdentifierType() {
        String globalProperty = administrationService.getGlobalProperty(EMR_PRIMARY_IDENTIFIER_TYPE);
        PatientIdentifierType patientIdentifierByUuid = patientService.getPatientIdentifierTypeByUuid(globalProperty);
        return patientIdentifierByUuid;
    }

}
