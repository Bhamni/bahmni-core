package org.bahmni.module.admin.csv.service;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.apache.commons.lang.StringUtils;
import org.bahmni.module.admin.csv.models.RelationshipRow;
import org.openmrs.Patient;
import org.openmrs.Provider;
import org.openmrs.Relationship;
import org.openmrs.RelationshipType;
import org.openmrs.api.AdministrationService;
import org.openmrs.api.PatientService;
import org.openmrs.api.PersonService;
import org.openmrs.api.ProviderService;

import java.text.ParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.bahmni.module.admin.csv.utils.CSVUtils.getDateFromString;
import static org.bahmni.module.admin.csv.utils.CSVUtils.getTodayDate;

public class CSVRelationshipService {

    public static final String BAHMNI_RELATIONSHIP_TYPE_MAP_PROPERTY = "bahmni.relationshipTypeMap";
    public static final String PATIENT_RELATIONSHIP = "patient";
    public static final String PROVIDER_RELATIONSHIP = "provider";

    private PatientService patientService;
    private PersonService personService;
    private ProviderService providerService;
    private AdministrationService administrationService;


    public CSVRelationshipService(PatientService patientService, PersonService personService, ProviderService providerService, AdministrationService administrationService) {
        this.patientService = patientService;
        this.personService = personService;
        this.providerService = providerService;
        this.administrationService = administrationService;
    }

    public Patient save(RelationshipRow relationshipRow) throws ParseException {
        List<Patient> patientsMatchedPersonA = patientService.getPatients(null, relationshipRow.getPersonA(), null, true);
        if (null == patientsMatchedPersonA || patientsMatchedPersonA.size() == 0) {
            throw new RuntimeException("No matching patients found with ID:'" + relationshipRow.getPersonA() + "'");
        } else {
            Patient patientA = patientsMatchedPersonA.get(0);
            patientService.savePatient(patientA);
            saveRelationship(relationshipRow, patientA);

            return patientA;
        }
    }

    private void saveRelationship(RelationshipRow relationshipRow, Patient patientA) throws ParseException {
        String relationshipType = relationshipRow.getRelationshipType();
        if (!StringUtils.isEmpty(relationshipType)) {
            Relationship relationship = createRelationship(relationshipRow, patientA);
            personService.saveRelationship(relationship);
        }
    }

    private Relationship createRelationship(RelationshipRow relationshipRow, Patient patientA) throws ParseException {
        String relationshipType = relationshipRow.getRelationshipType();
        Relationship relationship = new Relationship();

        RelationshipType relationshipTypeByName = personService.getRelationshipTypeByName(relationshipType);

        if (null == relationshipTypeByName) {
            throw new RuntimeException("No matching relationship type found with relationship type name:'" + relationshipRow.getRelationshipType() + "'");
        }

        String relationshipMapProperty = administrationService.getGlobalProperty(BAHMNI_RELATIONSHIP_TYPE_MAP_PROPERTY);
        Map<String, Object> relationshipMap = new Gson().fromJson(relationshipMapProperty, new TypeToken<HashMap<String, Object>>() {
        }.getType());

        if (isPatientRelationship(relationshipMap, relationshipType)) {
            List<Patient> patientsMatchedPersonB = patientService.getPatients(null, relationshipRow.getPersonB(), null, true);
            relationship.setPersonA(patientA);

            if (null == patientsMatchedPersonB || patientsMatchedPersonB.size() == 0) {
                throw new RuntimeException("No matching patients found with ID:'" + relationshipRow.getPersonB() + "'");
            }
            relationship.setPersonB(patientsMatchedPersonB.get(0));

        } else if (isProviderRelationship(relationshipMap, relationshipType)) {
            List<Provider> providersMatchedPersonB = providerService.getProviders(relationshipRow.getPersonB(), null, null, null);
            if (null == providersMatchedPersonB || providersMatchedPersonB.size() == 0) {
                throw new RuntimeException("No matching patients found with ID:'" + relationshipRow.getPersonB() + "'");
            }
            relationship.setPersonA(patientA);
            relationship.setPersonB(personService.getPerson(providersMatchedPersonB.get(0).getId()));
        } else {
            throw new RuntimeException("Invalid relationship type provided.");
        }

        setRelationshipDates(relationshipRow, relationship);
        relationship.setRelationshipType(relationshipTypeByName);

        return relationship;
    }

    private List<String> getRelationshipTypes(Map<String, Object> relationshipMap, String relationship) {
        return (List<String>) relationshipMap.get(relationship);
    }

    private boolean isProviderRelationship(Map<String, Object> relationshipMap, String relationshipType) {
        List<String> relationshipTypes = getRelationshipTypes(relationshipMap, PROVIDER_RELATIONSHIP);
        return relationshipTypes.contains(relationshipType);
    }

    private boolean isPatientRelationship(Map<String, Object> relationshipMap, String relationshipType) {
        List<String> relationshipTypes = getRelationshipTypes(relationshipMap, PATIENT_RELATIONSHIP);
        return relationshipTypes.contains(relationshipType);
    }

    private void setRelationshipDates(RelationshipRow relationshipRow, Relationship relationship) throws ParseException {
        if (!StringUtils.isEmpty(relationshipRow.getStartDate())) {
            relationship.setStartDate(getDateFromString(relationshipRow.getStartDate()));
        } else {
            relationship.setStartDate(getTodayDate());
        }

        if (!StringUtils.isEmpty(relationshipRow.getEndDate())) {
            relationship.setEndDate(getDateFromString(relationshipRow.getEndDate()));
        }
    }
}