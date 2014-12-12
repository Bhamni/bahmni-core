package org.openmrs.module.bahmniemrapi.visit.service;

import org.openmrs.Visit;

import java.util.Date;
import java.util.List;

public interface BahmniVisitService {
    public Visit getLatestVisit(String patientUuid, String conceptName);
    public List<Visit> getVisitOn(Date visitDate);
}
