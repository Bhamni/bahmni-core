package org.bahmni.module.bahmnicore.service.impl;

import org.apache.commons.collections.CollectionUtils;
import org.bahmni.module.bahmnicore.dao.ObsDao;
import org.bahmni.module.bahmnicore.service.BahmniObsService;
import org.openmrs.*;
import org.openmrs.api.ConceptService;
import org.openmrs.api.ObsService;
import org.openmrs.api.VisitService;
import org.openmrs.module.bahmniemrapi.encountertransaction.contract.BahmniObservation;
import org.openmrs.module.bahmniemrapi.encountertransaction.mapper.OMRSObsToBahmniObsMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class BahmniObsServiceImpl implements BahmniObsService {

    private ObsDao obsDao;
    private OMRSObsToBahmniObsMapper omrsObsToBahmniObsMapper;
    private static final String[] NOT_STANDARD_OBS_CLASSES={"Diagnosis","LabSet","LabTest","Finding"};
    private VisitService visitService;
    private ObsService obsService;
    private ConceptService conceptService;

    @Autowired
    public BahmniObsServiceImpl(ObsDao obsDao, OMRSObsToBahmniObsMapper omrsObsToBahmniObsMapper, VisitService visitService, ObsService obsService, ConceptService conceptService) {
        this.obsDao = obsDao;
        this.omrsObsToBahmniObsMapper = omrsObsToBahmniObsMapper;
        this.visitService = visitService;
        this.obsService = obsService;
        this.conceptService = conceptService;
    }

    @Override
    public List<Obs> getObsForPerson(String identifier) {
        return obsDao.getNumericObsByPerson(identifier);
    }

    @Override
    public Collection<BahmniObservation> observationsFor(String patientUuid, Collection<Concept> concepts, Integer numberOfVisits) {
        if(CollectionUtils.isNotEmpty(concepts)){
            List<String> conceptNames = new ArrayList<>();
            for (Concept concept : concepts) {
                conceptNames.add(concept.getName().getName());
            }
            List<Obs> observations = obsDao.getObsFor(patientUuid, conceptNames, numberOfVisits);
            return omrsObsToBahmniObsMapper.map(observations,concepts);
        }
        return Collections.EMPTY_LIST;
    }

    @Override
    public Collection<BahmniObservation> getLatest(String patientUuid, Collection<Concept> concepts, Boolean flatten) {
        List<Obs> latestObs = new ArrayList<>();
        for (Concept concept : concepts) {
            latestObs.addAll(obsDao.getLatestObsFor(patientUuid, concept.getName().getName(), 1));
        }

        return omrsObsToBahmniObsMapper.map(latestObs, concepts, flatten);
    }

    @Override
    public List<Concept> getNumericConceptsForPerson(String personUUID) {
        return obsDao.getNumericConceptsForPerson(personUUID);
    }

    @Override
    public List<Obs> getLatestObsForConceptSetByVisit(String patientUuid, String conceptName, Integer visitId) {
        return obsDao.getLatestObsForConceptSetByVisit(patientUuid, conceptName, visitId);
    }

    @Override
    public Collection<BahmniObservation> getObservationForVisit(String visitUuid, List<String> conceptNames){
        Visit visit = visitService.getVisitByUuid(visitUuid);
        List<Person> persons = new ArrayList<>();
        persons.add(visit.getPatient());
        List<Obs> observations = obsService.getObservations(persons, new ArrayList<Encounter>(visit.getEncounters()),getConceptsForNames(conceptNames), null, null, null, null, null, null, null, null, false);
        observations = new ArrayList<Obs>(getObsAtTopLevel(observations,conceptNames));
        return omrsObsToBahmniObsMapper.map(observations, null);
    }

    private List<Concept> getConceptsForNames(Collection<String> conceptNames){
        List<Concept> concepts = new ArrayList<>();
        if(conceptNames!= null){
            for (String conceptName : conceptNames) {
                concepts.add(conceptService.getConceptByName(conceptName));
            }
        }
        return concepts;
    }

    private Set<Obs> getObsAtTopLevel(List<Obs> observations, List<String> topLevelconceptNames) {
        if(topLevelconceptNames == null) topLevelconceptNames = new ArrayList<>();
        Set<Obs> ret = new HashSet<Obs>();
        for (Obs o : observations) {
            if (o.getObsGroup() == null || topLevelconceptNames.contains(o.getConcept().getName().getName()))
                ret.add(o);
        }
        return ret;
    }
}
