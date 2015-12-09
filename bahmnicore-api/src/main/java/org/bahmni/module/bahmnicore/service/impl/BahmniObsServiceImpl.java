package org.bahmni.module.bahmnicore.service.impl;

import org.apache.commons.collections.CollectionUtils;
import org.bahmni.module.bahmnicore.dao.ObsDao;
import org.bahmni.module.bahmnicore.dao.VisitDao;
import org.bahmni.module.bahmnicore.dao.impl.ObsDaoImpl;
import org.bahmni.module.bahmnicore.service.BahmniObsService;
import org.bahmni.module.bahmnicore.util.MiscUtils;
import org.openmrs.*;
import org.openmrs.api.ConceptService;
import org.openmrs.api.VisitService;
import org.openmrs.module.bahmniemrapi.encountertransaction.contract.BahmniObservation;
import org.openmrs.module.bahmniemrapi.encountertransaction.mapper.OMRSObsToBahmniObsMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class BahmniObsServiceImpl implements BahmniObsService {

    private ObsDao obsDao;
    private VisitDao visitDao;
    private OMRSObsToBahmniObsMapper omrsObsToBahmniObsMapper;
    private VisitService visitService;
    private ConceptService conceptService;

    @Autowired
    public BahmniObsServiceImpl(ObsDao obsDao, OMRSObsToBahmniObsMapper omrsObsToBahmniObsMapper, VisitService visitService, ConceptService conceptService, VisitDao visitDao) {
        this.obsDao = obsDao;
        this.omrsObsToBahmniObsMapper = omrsObsToBahmniObsMapper;
        this.visitService = visitService;
        this.conceptService = conceptService;
        this.visitDao = visitDao;
    }

    @Override
    public List<Obs> getObsForPerson(String identifier) {
        return obsDao.getNumericObsByPerson(identifier);
    }

    @Override
    public Collection<BahmniObservation> observationsFor(String patientUuid, Collection<Concept> concepts, Integer numberOfVisits,
                                                         List<String> obsIgnoreList, Boolean filterOutOrderObs, Order order, Date startDate, Date endDate) {
        if (CollectionUtils.isNotEmpty(concepts)) {
            List<String> conceptNames = new ArrayList<>();
            for (Concept concept : concepts) {
                conceptNames.add(concept.getName().getName());
            }

            List<Obs> observations = obsDao.getObsByPatientAndVisit(patientUuid, conceptNames,
                    visitDao.getVisitIdsFor(patientUuid, numberOfVisits), -1, ObsDaoImpl.OrderBy.DESC, obsIgnoreList, filterOutOrderObs, order, startDate, endDate);
            return omrsObsToBahmniObsMapper.map(observations, concepts);
        }
        return Collections.EMPTY_LIST;
    }

    @Override
    public Collection<BahmniObservation> observationsFor(String patientUuid, Concept rootConcept, Concept childConcept, Integer numberOfVisits) {
        List<Obs> observations = obsDao.getObsFor(patientUuid, rootConcept, childConcept, visitDao.getVisitIdsFor(patientUuid, numberOfVisits));
        List<BahmniObservation> bahmniObservations = new ArrayList<>();
        for (Obs observation : observations) {
            BahmniObservation bahmniObservation = omrsObsToBahmniObsMapper.map(observation);
            bahmniObservations.add(bahmniObservation);
        }
        return bahmniObservations;
    }

    @Override
    public Collection<BahmniObservation> getLatest(String patientUuid, Collection<Concept> concepts, Integer numberOfVisits, List<String> obsIgnoreList,
                                                   Boolean filterOutOrderObs, Order order, Date startDate, Date endDate) {
        List<Obs> latestObs = new ArrayList<>();
        for (Concept concept : concepts) {
            if (null != concept) {
                latestObs.addAll(obsDao.getObsByPatientAndVisit(patientUuid, Arrays.asList(concept.getName().getName()),
                        visitDao.getVisitIdsFor(patientUuid, numberOfVisits), 1, ObsDaoImpl.OrderBy.DESC, obsIgnoreList, filterOutOrderObs, order, startDate, endDate));
            }
        }

        return omrsObsToBahmniObsMapper.map(latestObs, concepts);
    }

    @Override
    public Collection<BahmniObservation> getLatestObsByVisit(Visit visit, Collection<Concept> concepts, List<String> obsIgnoreList, Boolean filterOutOrderObs) {
        List<Obs> latestObs = new ArrayList<>();
        for (Concept concept : concepts) {
            latestObs.addAll(obsDao.getObsByPatientAndVisit(visit.getPatient().getUuid(), Arrays.asList(concept.getName().getName()),
                    Arrays.asList(visit.getVisitId()), 1, ObsDaoImpl.OrderBy.DESC, obsIgnoreList, filterOutOrderObs, null, null, null));
        }

        return omrsObsToBahmniObsMapper.map(latestObs, concepts);
    }

    @Override
    public Collection<BahmniObservation> getInitial(String patientUuid, Collection<Concept> conceptNames,
                                                    Integer numberOfVisits, List<String> obsIgnoreList, Boolean filterOutOrderObs, Order order, Date startDate, Date endDate) {
        List<Obs> latestObs = new ArrayList<>();
        for (Concept concept : conceptNames) {
            latestObs.addAll(obsDao.getObsByPatientAndVisit(patientUuid, Arrays.asList(concept.getName().getName()),
                    visitDao.getVisitIdsFor(patientUuid, numberOfVisits), 1, ObsDaoImpl.OrderBy.ASC, obsIgnoreList, filterOutOrderObs, order, startDate, endDate));
        }

        return omrsObsToBahmniObsMapper.map(latestObs, conceptNames);
    }

    @Override
    public Collection<BahmniObservation> getInitialObsByVisit(Visit visit, List<Concept> concepts, List<String> obsIgnoreList, Boolean filterObsWithOrders) {
        List<Obs> latestObs = new ArrayList<>();
        for (Concept concept : concepts) {
            latestObs.addAll(obsDao.getObsByPatientAndVisit(visit.getPatient().getUuid(), Arrays.asList(concept.getName().getName()),
                    Arrays.asList(visit.getVisitId()), 1, ObsDaoImpl.OrderBy.ASC, obsIgnoreList, filterObsWithOrders, null, null, null));
        }

        Collection<BahmniObservation> map = omrsObsToBahmniObsMapper.map(latestObs, concepts);
        return map;
    }

    @Override
    public List<Concept> getNumericConceptsForPerson(String personUUID) {
        return obsDao.getNumericConceptsForPerson(personUUID);
    }

    @Override
    public Collection<BahmniObservation> getLatestObsForConceptSetByVisit(String patientUuid, String conceptName, Integer visitId) {
        List<Obs> obs =  withUniqueConcepts(filterByRootConcept(obsDao.getLatestObsForConceptSetByVisit(patientUuid, conceptName, visitId), conceptName));
        return omrsObsToBahmniObsMapper.map(obs, Arrays.asList(getConceptByName(conceptName)));
    }

    @Override
    public Collection<BahmniObservation> getObservationForVisit(String visitUuid, List<String> conceptNames, Collection<Concept> obsIgnoreList, Boolean filterOutOrders, Order order) {
        Visit visit = visitService.getVisitByUuid(visitUuid);
        List<Person> persons = new ArrayList<>();
        persons.add(visit.getPatient());
        List<Obs> observations = obsDao.getObsForVisits(persons, new ArrayList<>(visit.getEncounters()),
                MiscUtils.getConceptsForNames(conceptNames, conceptService), obsIgnoreList, filterOutOrders, order);
        observations = new ArrayList<>(getObsAtTopLevelAndApplyIgnoreList(observations, conceptNames, obsIgnoreList));
        return omrsObsToBahmniObsMapper.map(observations, null);
    }

    @Override
    public Collection<BahmniObservation> getObservationsForOrder(String orderUuid) {
        List<Obs> observations = obsDao.getObsForOrder(orderUuid);
        return omrsObsToBahmniObsMapper.map(observations, null);
    }

    private Concept getConceptByName(String conceptName) {
        return conceptService.getConceptByName(conceptName);
    }

    private List<Obs> getObsAtTopLevelAndApplyIgnoreList(List<Obs> observations, List<String> topLevelConceptNames, Collection<Concept> obsIgnoreList) {
        List<Obs> topLevelObservations = new ArrayList<>();
        if (topLevelConceptNames == null) topLevelConceptNames = new ArrayList<>();

        Set<String> topLevelConceptNamesWithoutCase = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
        topLevelConceptNamesWithoutCase.addAll(topLevelConceptNames);
        for (Obs o : observations) {
            if (o.getObsGroup() == null || topLevelConceptNamesWithoutCase.contains(o.getConcept().getName().getName().toLowerCase())) {
                if (!removeIgnoredObsOrIgnoreParentItself(o, obsIgnoreList) && !topLevelObservations.contains(o)) {
                    topLevelObservations.add(o);
                }
            }
        }

        return topLevelObservations;
    }

    //Removes groupmembers who are ignored, and ignore a parent if all children are in ignore list
    private boolean removeIgnoredObsOrIgnoreParentItself(Obs o, Collection<Concept> obsIgnoreList) {
        if (CollectionUtils.isNotEmpty(o.getGroupMembers()) && CollectionUtils.isNotEmpty(obsIgnoreList)) {
            int size = o.getGroupMembers().size();
            int matchCount = 0;
            Iterator<Obs> itr = o.getGroupMembers().iterator();
            while (itr.hasNext()) {
                Obs temp = itr.next();
                for (Concept concept : obsIgnoreList) {
                    if (temp.getConcept().getConceptId() == concept.getConceptId()) {
                        itr.remove();
                        matchCount++;
                    }
                }
            }
            if (matchCount == size) {
                return true;
            }
        }
        return false;
    }

    private List<Obs> filterByRootConcept(List<Obs> obs, String parentConceptName) {
        List<Obs> filteredList = new ArrayList<>();
        for (Obs ob : obs) {
            if (partOfParent(ob, parentConceptName)) {
                filteredList.add(ob);
            }
        }
        return filteredList;
    }

    private boolean partOfParent(Obs ob, String parentConceptName) {
        if (ob == null) return false;
        if (ob.getConcept().getName().getName().equals(parentConceptName)) return true;
        return partOfParent(ob.getObsGroup(), parentConceptName);
    }


    private List<Obs> withUniqueConcepts(List<Obs> observations) {
        Map<Integer, Integer> conceptToEncounterMap = new HashMap<>();
        List<Obs> filteredObservations = new ArrayList<>();
        for (Obs obs : observations) {
            Integer encounterId = conceptToEncounterMap.get(obs.getConcept().getId());
            if (encounterId == null) {
                conceptToEncounterMap.put(obs.getConcept().getId(), obs.getEncounter().getId());
                filteredObservations.add(obs);
            } else if (obs.getEncounter().getId().intValue() == encounterId.intValue()) {
                filteredObservations.add(obs);
            }
        }
        return filteredObservations;
    }

}
