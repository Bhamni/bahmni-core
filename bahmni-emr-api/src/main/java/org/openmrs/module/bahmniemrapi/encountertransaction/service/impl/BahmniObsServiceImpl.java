package org.openmrs.module.bahmniemrapi.encountertransaction.service.impl;

import org.bahmni.module.bahmnicore.dao.ObsDao;
import org.openmrs.Concept;
import org.openmrs.Obs;
import org.openmrs.module.bahmniemrapi.encountertransaction.contract.BahmniObservation;
import org.openmrs.module.bahmniemrapi.encountertransaction.mapper.BahmniObservationMapper;
import org.openmrs.module.bahmniemrapi.encountertransaction.service.BahmniObsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Service
public class BahmniObsServiceImpl implements BahmniObsService {

    private ObsDao obsDao;

    @Autowired
    public BahmniObsServiceImpl(ObsDao obsDao) {
        this.obsDao = obsDao;
    }

    @Override
    public List<Obs> getObsForPerson(String identifier) {
        return obsDao.getNumericObsByPerson(identifier);
    }

    @Override
    public List<BahmniObservation> observationsFor(String patientUuid, Collection<Concept> concepts, Integer numberOfVisits) {
        List<String> conceptNames = new ArrayList<>();
        for (Concept concept : concepts) {
            conceptNames.add(concept.getName().getName());
        }
        List<Obs> obsFor = obsDao.getObsFor(patientUuid, conceptNames, numberOfVisits);
        return BahmniObservationMapper.map(obsFor);
    }

    @Override
    public List<BahmniObservation> getLatest(String patientUuid, List<String> conceptNames) {
        List<Obs> latestObs = new ArrayList<>();
        for (String conceptName : conceptNames) {
            latestObs.addAll(obsDao.getLatestObsFor(patientUuid, conceptName, 1));
        }

        return BahmniObservationMapper.map(latestObs);
    }

    @Override
    public List<Concept> getNumericConceptsForPerson(String personUUID) {
        return obsDao.getNumericConceptsForPerson(personUUID);
    }

    @Override
    public List<Obs> getLatestObsForConceptSetByVisit(String patientUuid, String conceptName, Integer visitId) {
        return obsDao.getLatestObsForConceptSetByVisit(patientUuid, conceptName, visitId);
    }
}
