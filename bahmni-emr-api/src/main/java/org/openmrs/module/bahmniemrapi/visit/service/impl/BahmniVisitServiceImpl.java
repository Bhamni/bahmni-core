package org.openmrs.module.bahmniemrapi.visit.service.impl;

import org.openmrs.Visit;
import org.openmrs.module.bahmniemrapi.visit.dao.VisitDao;
import org.openmrs.module.bahmniemrapi.visit.service.BahmniVisitService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
public class BahmniVisitServiceImpl implements BahmniVisitService {

    private VisitDao visitDao;

    @Autowired
    public BahmniVisitServiceImpl(VisitDao visitDao) {
        this.visitDao = visitDao;
    }

    @Override
    public Visit getLatestVisit(String patientUuid, String conceptName) {
        return visitDao.getLatestVisit(patientUuid, conceptName);
    }

    @Override
    public List<Visit> getVisitOn(Date visitDate) {
        return visitDao.getVisitOn(visitDate);
    }
}
