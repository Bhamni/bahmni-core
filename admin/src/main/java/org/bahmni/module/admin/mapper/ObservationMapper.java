package org.bahmni.module.admin.mapper;

import org.apache.commons.lang.StringUtils;
import org.bahmni.csv.KeyValue;
import org.bahmni.module.admin.csv.models.EncounterRow;
import org.openmrs.Concept;
import org.openmrs.api.ConceptService;
import org.openmrs.module.emrapi.encounter.domain.EncounterTransaction;
import org.openmrs.module.emrapi.encounter.exception.ConceptNotFoundException;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

public class ObservationMapper {
    private HashMap<String, EncounterTransaction.Concept> cachedConcepts = new HashMap<>();

    private ConceptService conceptService;

    public ObservationMapper(ConceptService conceptService) {
        this.conceptService = conceptService;
    }

    public List<EncounterTransaction.Observation> getObservations(EncounterRow encounterRow) throws ParseException {
        List<EncounterTransaction.Observation> observations = new ArrayList<>();
        if (encounterRow.hasObservations()) {
            Date encounterDate = encounterRow.getEncounterDate();
            for (KeyValue obsRow : encounterRow.obsRows) {
                if (obsRow.getValue() != null && !StringUtils.isEmpty(obsRow.getValue().trim())) {
                    EncounterTransaction.Observation observation = createObservation(encounterDate, obsRow);
                    observations.add(observation);
                }
            }
        }
        return observations;
    }

    protected EncounterTransaction.Concept getConcept(String conceptName) {
        if (!cachedConcepts.containsKey(conceptName)) {
            cachedConcepts.put(conceptName, fetchConcept(conceptName));
        }
        return cachedConcepts.get(conceptName);
    }

    private EncounterTransaction.Observation createObservation(Date encounterDate, KeyValue obsRow) throws ParseException {
        EncounterTransaction.Observation observation = new EncounterTransaction.Observation();
        observation.setConcept(getConcept(obsRow.getKey()));
        observation.setValue(obsRow.getValue());
        observation.setObservationDateTime(encounterDate);
        return observation;
    }

    private EncounterTransaction.Concept fetchConcept(String conceptName) {
        Concept obsConcept = conceptService.getConceptByName(conceptName);
        if (obsConcept == null)
            throw new ConceptNotFoundException("Concept '"+ conceptName +"' not found");

        return new EncounterTransaction.Concept(obsConcept.getUuid(), obsConcept.getName().getName());
    }

}
