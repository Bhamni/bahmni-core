package org.openmrs.module.bahmniemrapi.encountertransaction.service;

import org.openmrs.Concept;
import org.openmrs.Obs;
import org.openmrs.module.bahmniemrapi.encountertransaction.contract.BahmniObservation;

import java.util.Collection;
import java.util.List;

public interface BahmniObsService {
    public List<Obs> getObsForPerson(String identifier);
    public List<BahmniObservation> observationsFor(String patientUuid, Collection<Concept> concepts, Integer numberOfVisits);
    public List<BahmniObservation> getLatest(String patientUuid, List<String> conceptNames);
    public List<Concept> getNumericConceptsForPerson(String personUUID);
    public List<Obs> getLatestObsForConceptSetByVisit(String patientUuid, String conceptName, Integer visitId);
}
