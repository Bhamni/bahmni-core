package org.bahmni.module.admin.csv.service;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.apache.commons.lang.StringUtils;
import org.bahmni.module.admin.csv.models.RelationshipRow;
import org.bahmni.module.bahmnicore.service.BahmniPatientService;
import org.openmrs.*;
import org.openmrs.api.AdministrationService;
import org.openmrs.api.PersonService;
import org.openmrs.api.ProviderService;

import java.text.ParseException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.bahmni.module.admin.csv.utils.CSVUtils.getDateFromString;
import static org.bahmni.module.admin.csv.utils.CSVUtils.getTodayDate;

public class CSVRelationshipService {

    public static final String BAHMNI_RELATIONSHIP_TYPE_MAP_PROPERTY = "bahmni.relationshipTypeMap";
    public static final String PATIENT_RELATIONSHIP = "patient";
    public static final String PROVIDER_RELATIONSHIP = "provider";

    private BahmniPatientService patientService;
    private PersonService personService;
    private ProviderService providerService;
    private AdministrationService administrationService;


    public CSVRelationshipService(BahmniPatientService patientService, PersonService personService, ProviderService providerService, AdministrationService administrationService) {
        this.patientService = patientService;
        this.personService = personService;
        this.providerService = providerService;
        this.administrationService = administrationService;
    }

    public Relationship save(RelationshipRow relationshipRow) throws ParseException {

        List<Patient> patientsMatchedPersonA = patientService.get(relationshipRow.getPersonA(), true);
        if (null == patientsMatchedPersonA || patientsMatchedPersonA.size() == 0) {
            throw new RuntimeException("No matching patients found with ID:'" + relationshipRow.getPersonA() + "'");
        } else {
            Patient patientA = patientsMatchedPersonA.get(0);
            Relationship relationship = createRelationship(relationshipRow, patientA);
            return personService.saveRelationship(relationship);
        }
    }

    private Relationship createRelationship(RelationshipRow relationshipRow, Patient patientA) throws ParseException {
        Relationship relationship = new Relationship();

        RelationshipType relationshipType = getMatchingRelationship(relationshipRow.getaIsToB(), relationshipRow.getbIsToA());
        relationship.setPersonA(patientA);
        relationship.setPersonB(getPersonB(relationshipRow, patientA, relationshipType));
        relationship.setStartDate(getStartDate(relationshipRow));
        relationship.setEndDate(getEndDate(relationshipRow));
        relationship.setRelationshipType(relationshipType);

        return relationship;
    }


    private Person getPersonB(RelationshipRow relationshipRow, Patient patientA, RelationshipType relationshipType) {
        String relationshipMapProperty = administrationService.getGlobalProperty(BAHMNI_RELATIONSHIP_TYPE_MAP_PROPERTY);
        Map<String, Object> relationshipMap = new Gson().fromJson(relationshipMapProperty, new TypeToken<HashMap<String, Object>>() {
        }.getType());

        if (isPatientRelationship(relationshipMap, relationshipRow.getaIsToB())) {

            List<Patient> matchedPatient = patientService.get(relationshipRow.getPersonB(), true);

            if (null == matchedPatient || matchedPatient.size() == 0) {
                throw new RuntimeException("No matching patients found with ID:'" + relationshipRow.getPersonB() + "'");
            }
            checkForExistingRelationship(relationshipRow, patientA, relationshipType, matchedPatient.get(0));
            return matchedPatient.get(0);
        } else if (isProviderRelationship(relationshipMap, relationshipRow.getaIsToB())) {

            List<Provider> matchedProvider = providerService.getProviders(relationshipRow.getPersonB(), null, null, null);
            if (null == matchedProvider || matchedProvider.size() == 0) {
                throw new RuntimeException("No matching provider found with ID:'" + relationshipRow.getPersonB() + "'");
            }
            checkForExistingRelationship(relationshipRow, patientA, relationshipType, matchedProvider.get(0).getPerson());
            return matchedProvider.get(0).getPerson();

        } else {
            throw new RuntimeException("Relationship map not found for the relationship " + relationshipRow.getaIsToB());
        }
    }

    private void checkForExistingRelationship(RelationshipRow relationshipRow, Patient patientA, RelationshipType relationshipType, Person matchedPatient) {
        List<Relationship> existingRelationship = personService.getRelationships(patientA, matchedPatient, relationshipType);
        if (existingRelationship != null && existingRelationship.size() > 0) {
            throw new RuntimeException("Relationship with " + relationshipRow.getPersonA() + " and " + relationshipRow.getPersonB() + " with the type " + relationshipType.getaIsToB() + " exists.");
        }
    }

    private RelationshipType getMatchingRelationship(String aIsToBRelationship, String bIsToA) {
        List<RelationshipType> relationshipTypeByAIsToB = patientService.getByAIsToB(aIsToBRelationship);

        if (relationshipTypeByAIsToB == null || relationshipTypeByAIsToB.size() == 0) {
            throw new RuntimeException("No matching relationship type found with relationship type name:'" + aIsToBRelationship + "'");
        }

        for (RelationshipType relationshipType : relationshipTypeByAIsToB) {
            if (relationshipTypeByAIsToB.get(0).getbIsToA().equals(bIsToA)) {
                return relationshipType;
            }
        }
        throw new RuntimeException("Relationship aIsToB and bIsToA are not matching.");
    }

    private List<String> getRelationshipTypes(Map<String, Object> relationshipMap, String relationship) {
        return relationshipMap != null ? (List<String>) relationshipMap.get(relationship) : null;
    }

    private boolean isProviderRelationship(Map<String, Object> relationshipMap, String relationshipType) {
        List<String> relationshipTypes = getRelationshipTypes(relationshipMap, PROVIDER_RELATIONSHIP);
        return relationshipTypes != null && relationshipTypes.contains(relationshipType);
    }

    private boolean isPatientRelationship(Map<String, Object> relationshipMap, String relationshipType) {
        List<String> relationshipTypes = getRelationshipTypes(relationshipMap, PATIENT_RELATIONSHIP);
        return relationshipTypes != null && relationshipTypes.contains(relationshipType);
    }

    private Date getStartDate(RelationshipRow relationshipRow) throws ParseException {
        if (!StringUtils.isEmpty(relationshipRow.getStartDate())) {
            return getDateFromString(relationshipRow.getStartDate());
        } else {
            return getTodayDate();
        }
    }

    private Date getEndDate(RelationshipRow relationshipRow) throws ParseException {
        if (!StringUtils.isEmpty(relationshipRow.getEndDate())) {
            return getDateFromString(relationshipRow.getEndDate());
        }
        return null;
    }
}