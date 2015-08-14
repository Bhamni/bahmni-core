package org.bahmni.module.admin.retrospectiveEncounter.domain;

import java.util.Collection;

import org.openmrs.Encounter;
import org.openmrs.Obs;
import org.openmrs.Visit;
import org.openmrs.api.context.Context;
import org.openmrs.module.bahmniemrapi.diagnosis.contract.BahmniDiagnosisRequest;
import org.openmrs.module.bahmniemrapi.encountertransaction.contract.BahmniObservation;
import org.openmrs.module.emrapi.EmrApiConstants;
import org.openmrs.module.emrapi.encounter.domain.EncounterTransaction;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

public class DuplicateObservationsMatcher {
    private Visit matchingVisit;
    private BahmniVisit visit;
    private List<Obs> visitObservations;
    private String requestedEncounterType;

    public DuplicateObservationsMatcher(Visit matchingVisit, String requestedEncounterType) {
        this.visit = new BahmniVisit(matchingVisit);
        this.matchingVisit = matchingVisit;
        this.requestedEncounterType = requestedEncounterType;
    }

    public List<EncounterTransaction.Observation> getUniqueObservations(List<EncounterTransaction.Observation> observations) {
        List<Obs> allObs = getObservationsForVisit();

        List<EncounterTransaction.Observation> uniqueObservations = new ArrayList<>();
        for (EncounterTransaction.Observation anObservation : observations) {
            String anObservationValue = (String) anObservation.getValue();
            String observationConceptName = anObservation.getConcept().getName();
            if (isUnique(allObs, anObservationValue, observationConceptName)) {
                uniqueObservations.add(anObservation);
            }
        }
        return uniqueObservations;
    }

    public Collection<BahmniObservation> getNewlyAddedBahmniObservations(Collection<BahmniObservation> observations, Date encounterDateTime) {
        List<Encounter> matchedEncounters = getAllEncountersByDate(encounterDateTime);
        return filterObsIfItIsExists(observations, matchedEncounters);
    }

    private Collection<BahmniObservation> filterObsIfItIsExists(Collection<BahmniObservation> observations, List<Encounter> matchedEncounters) {
        List<BahmniObservation> existingObs = new ArrayList<>();
        for (Encounter matchedEncounter : matchedEncounters) {
            for (BahmniObservation observation : observations) {
                if (isObsExisits(observation, matchedEncounter.getAllObs())) {
                    existingObs.add(observation);
                }
            }
        }
        observations.removeAll(existingObs);
        return observations;
    }

    private boolean isObsExisits(BahmniObservation observation, Set<Obs> existingObs) {
        for (Obs obs : existingObs) {
            if (doesConceptNameMatch(obs, observation.getConcept().getName())) {
                return true;
            }
        }
        return false;
    }

    private List<Encounter> getAllEncountersByDate(Date encounterDateTime) {
        Set<Encounter> encounters = matchingVisit.getEncounters();
        List<Encounter> matchingEncounters = new ArrayList<>();
        for (Encounter encounter : encounters) {
            if (encounterDateTime.equals(encounter.getEncounterDatetime()))
                matchingEncounters.add(encounter);
        }
        return matchingEncounters;
    }


    public List<BahmniDiagnosisRequest> getUniqueDiagnoses(List<BahmniDiagnosisRequest> bahmniDiagnoses) {
        List<Obs> allObs = getObservationsForVisit();

        List<BahmniDiagnosisRequest> uniqueDiagnoses = new ArrayList<>();
        for (BahmniDiagnosisRequest diagnosisRequest : bahmniDiagnoses) {
            if (diagnosisRequest.getCodedAnswer() != null && isUnique(allObs, diagnosisRequest.getCodedAnswer().getUuid(), EmrApiConstants.CONCEPT_CODE_CODED_DIAGNOSIS)) {
                uniqueDiagnoses.add(diagnosisRequest);
            } else if (diagnosisRequest.getCodedAnswer() == null) {
                uniqueDiagnoses.add(diagnosisRequest);
            }
        }
        return uniqueDiagnoses;
    }

    private List<Obs> getObservationsForVisit() {
        if (visitObservations == null)
            visitObservations = visit.obsFor(requestedEncounterType);

        return visitObservations;
    }

    private boolean isUnique(List<Obs> allObs, String anObservationValue, String observationConceptName) {
        for (Obs anObs : allObs) {
            if (doesConceptNameMatch(anObs, observationConceptName) && doesObsValueMatch(anObs, anObservationValue)) {
                return false;
            }
        }
        return true;
    }

    private boolean doesConceptNameMatch(Obs obs, String conceptName) {
        return conceptName.equalsIgnoreCase(obs.getConcept().getName().getName());
    }

    private boolean doesObsValueMatch(Obs obs, String anObservationValue) {
        if (obs.getConcept().getDatatype().isCoded() && obs.getValueCoded() != null) {
            return anObservationValue.equalsIgnoreCase(obs.getValueCoded().getUuid());
        } else if (obs.getConcept().isNumeric()) {
            return Double.parseDouble(anObservationValue) == obs.getValueNumeric();
        }
        return anObservationValue.equalsIgnoreCase(obs.getValueAsString(Context.getLocale()));
    }
}
