package org.bahmni.module.admin.observation;

import org.apache.commons.lang.StringUtils;
import org.bahmni.csv.KeyValue;
import org.bahmni.module.admin.csv.models.EncounterRow;
import org.openmrs.Concept;
import org.openmrs.ConceptName;
import org.openmrs.api.ConceptService;
import org.openmrs.api.context.Context;
import org.openmrs.module.emrapi.encounter.domain.EncounterTransaction;
import org.openmrs.module.emrapi.encounter.exception.ConceptNotFoundException;

import java.text.ParseException;
import java.util.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component(value = "adminObservationMapper")
public class ObservationMapper {
    private final ConceptCache conceptCache;

    private ConceptService conceptService;

    @Autowired
    public ObservationMapper(ConceptService conceptService) {
        this.conceptCache = new ConceptCache(conceptService);
        this.conceptService = conceptService;
    }

    public List<EncounterTransaction.Observation> getObservations(EncounterRow encounterRow) throws ParseException {
        List<EncounterTransaction.Observation> observations = new ArrayList<>();
        if (encounterRow.hasObservations()) {
            Date encounterDate = encounterRow.getEncounterDate();
            for (KeyValue obsRow : encounterRow.obsRows) {
                if (obsRow.getValue() != null && !StringUtils.isEmpty(obsRow.getValue().trim())) {
                    List<String> conceptNames = new ArrayList<>(Arrays.asList(obsRow.getKey().split("\\.")));
                    EncounterTransaction.Observation existingObservation = getRootObservationIfExists(observations, conceptNames, null);
                    if (existingObservation == null) {
                        observations.add(createObservation(conceptNames, encounterDate, obsRow));
                    } else {
                        updateObservation(conceptNames, existingObservation, encounterDate, obsRow);

                    }
                }
            }
        }
        return observations;

    }

    protected Concept getConcept(String conceptName) {
        return conceptCache.getConcept(conceptName);
    }

    private void updateObservation(List<String> conceptNames, EncounterTransaction.Observation existingObservation, Date encounterDate, KeyValue obsRow) throws ParseException {
        existingObservation.addGroupMember(createObservation(conceptNames, encounterDate, obsRow));
    }

    private EncounterTransaction.Observation getRootObservationIfExists(List<EncounterTransaction.Observation> observations, List<String> conceptNames, EncounterTransaction.Observation existingObservation) {
        for (EncounterTransaction.Observation observation : observations) {
            if (observation.getConcept().getName().equals(conceptNames.get(0))) {
                conceptNames.remove(0);
                if (conceptNames.size() == 0) {
                    conceptNames.add(observation.getConcept().getName());
                    return existingObservation;
                }
                existingObservation = observation;
                return getRootObservationIfExists(observation.getGroupMembers(), conceptNames, existingObservation);
            }
        }
        return existingObservation;
    }

    private EncounterTransaction.Observation createObservation(List<String> conceptNames, Date encounterDate, KeyValue obsRow) throws ParseException {
        Concept obsConcept = conceptCache.getConcept(conceptNames.get(0));
        EncounterTransaction.Concept concept = new EncounterTransaction.Concept(obsConcept.getUuid(), obsConcept.getName().getName());

        EncounterTransaction.Observation observation = new EncounterTransaction.Observation();
        observation.setConcept(concept);
        observation.setObservationDateTime(encounterDate);
        if (conceptNames.size() == 1) {
            observation.setValue(getValue(obsRow, obsConcept));
        } else {
            conceptNames.remove(0);
            observation.addGroupMember(createObservation(conceptNames, encounterDate, obsRow));
        }
        return observation;
    }

    private String getValue(KeyValue obsRow, Concept obsConcept) throws ParseException {
        if (obsConcept.getDatatype().isCoded()) {
            List<Concept> valueConcepts = conceptService.getConceptsByName(obsRow.getValue());
            Concept valueConcept = null;
            for (Concept concept : valueConcepts) {
                ConceptName name = concept.getFullySpecifiedName(Context.getLocale()) != null ? concept.getFullySpecifiedName(Context.getLocale()) : concept.getName();
                if (name.getName().equalsIgnoreCase(obsRow.getValue())) {
                    valueConcept = concept;
                    break;
                }
            }
            if (valueConcept == null)
                throw new ConceptNotFoundException(obsRow.getValue() + " not found");
            return valueConcept.getUuid();
        }
        return obsRow.getValue();
    }

}
