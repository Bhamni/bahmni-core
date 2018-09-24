package org.openmrs.module.bahmniemrapi.accessionnote.mapper;

import org.openmrs.EncounterType;
import org.openmrs.api.EncounterService;
import org.openmrs.module.bahmniemrapi.accessionnote.contract.AccessionNote;
import org.openmrs.module.emrapi.encounter.domain.EncounterTransaction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Component
public class AccessionNotesMapper {

    private static final String VALIDATION_NOTES_ENCOUNTER_TYPE = "VALIDATION NOTES";
    private static final String ACCESSION_UUID_CONCEPT_NAME = "Accession Uuid";
    private static final String ACCESSION_NOTES_CONCEPT_NAME = "Lab Manager Notes";

    @Autowired
    private EncounterService encounterService;

    private EncounterType validationNotesEncounterType;

    public List<AccessionNote> map(EncounterTransaction encounterTransaction) {
        if(hasValidationNotes(encounterTransaction)){
            String providerName = encounterTransaction.getProviders().iterator().next().getName();
            return getAccessionNotes(encounterTransaction,providerName);
        }
        return Collections.emptyList();
    }

    private List<AccessionNote> getAccessionNotes(EncounterTransaction encounterTransaction, String providerName) {
        List<EncounterTransaction.Observation> observations =    encounterTransaction.getObservations();
        List<AccessionNote> accessionNotes = new ArrayList<>();
        String accessionUuid = getAccessionUuid(observations);
        List<EncounterTransaction.Observation> filteredObservations = new ArrayList<>();
        for (EncounterTransaction.Observation observation : observations) {
            if(observation.getConcept().getName().equals(ACCESSION_NOTES_CONCEPT_NAME)){
                AccessionNote note = new AccessionNote();
                note.setAccessionUuid(accessionUuid);
                note.setText((String) observation.getValue());
                note.setDateTime(observation.getObservationDateTime());
                note.setProviderName(providerName);
                accessionNotes.add(note);
            }
            else if(!observation.getConcept().getName().equals(ACCESSION_UUID_CONCEPT_NAME)){
                filteredObservations.add(observation);
            }
        }
        encounterTransaction.setObservations(filteredObservations);
        return accessionNotes;
    }

    private String getAccessionUuid(List<EncounterTransaction.Observation> observations) {
        for (EncounterTransaction.Observation observation : observations) {
            if(observation.getConcept().getName().equals(ACCESSION_UUID_CONCEPT_NAME)){
                return  (String) observation.getValue();
            }
        }
        return null;
    }

    private boolean hasValidationNotes(EncounterTransaction encounterTransaction) {
        if(validationNotesEncounterType == null){
            validationNotesEncounterType = encounterService.getEncounterType(VALIDATION_NOTES_ENCOUNTER_TYPE);
            if(validationNotesEncounterType == null) return false;
        }
        return encounterTransaction.getEncounterTypeUuid() != null && encounterTransaction.getEncounterTypeUuid().equals(validationNotesEncounterType.getUuid()) && !encounterTransaction.getObservations().isEmpty();
    }

}
