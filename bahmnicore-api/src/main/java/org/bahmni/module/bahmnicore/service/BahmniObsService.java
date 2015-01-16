package org.bahmni.module.bahmnicore.service;

import org.openmrs.Concept;
import org.openmrs.Obs;
import org.openmrs.module.bahmniemrapi.encountertransaction.contract.BahmniObservation;

import java.util.Collection;
import java.util.List;

public interface BahmniObsService {
    public List<Obs> getObsForPerson(String identifier);
    public Collection<BahmniObservation> observationsFor(String patientUuid, Collection<Concept> concepts, Integer numberOfVisits);
    public Collection<BahmniObservation> getLatest(String patientUuid, Collection<Concept> conceptNames, Boolean flatten);
    public List<Concept> getNumericConceptsForPerson(String personUUID);
    public List<Obs> getLatestObsForConceptSetByVisit(String patientUuid, String conceptName, Integer visitId);
    Collection<BahmniObservation> getObservationForVisit(String visitUuid, List<String> conceptNames);
}
