package org.bahmni.module.bahmnicore.service;

import org.openmrs.Concept;
import org.openmrs.Obs;
import org.openmrs.Order;
import org.openmrs.Visit;
import org.openmrs.module.bahmniemrapi.encountertransaction.contract.BahmniObservation;

import java.text.ParseException;
import java.util.Collection;
import java.util.Date;
import java.util.List;

public interface BahmniObsService {
    public List<Obs> getObsForPerson(String identifier);
    public Collection<BahmniObservation> getInitial(String patientUuid, Collection<Concept> conceptNames, Integer numberOfVisits, List<String> obsIgnoreList, Boolean filterOutOrderObs, Order order, Date startDate, Date endDate);
    Collection<BahmniObservation> getInitialObsByVisit(Visit visit, List<Concept> rootConcepts, List<String> obsIgnoreList, Boolean filterObsWithOrders);
    public Collection<BahmniObservation> observationsFor(String patientUuid, Collection<Concept> concepts, Integer numberOfVisits, List<String> obsIgnoreList, Boolean filterOutOrderObs, Order order, Date startDate, Date endDate);
    public Collection<BahmniObservation> getLatest(String patientUuid, Collection<Concept> conceptNames, Integer numberOfVisits, List<String> obsIgnoreList, Boolean filterOutOrderObs, Order order, Date startDate, Date endDate);
    public List<Concept> getNumericConceptsForPerson(String personUUID);
    public Collection<BahmniObservation> getLatestObsForConceptSetByVisit(String patientUuid, String conceptName, Integer visitId);
    Collection<BahmniObservation> getObservationForVisit(String visitUuid, List<String> conceptNames, Collection<Concept> obsIgnoreList, Boolean filterOutOrders, Order order);
    Collection<BahmniObservation> getLatestObsByVisit(Visit visit, Collection<Concept> concepts, List<String> obsIgnoreList, Boolean filterObsWithOrders);
    Collection<BahmniObservation> getObservationsForOrder(String orderUuid);

    Collection<BahmniObservation> observationsFor(String patientUuid, Concept rootConcept, Concept childConcept, Integer numberOfVisits, Date startDate, Date endDate) throws ParseException;
}
