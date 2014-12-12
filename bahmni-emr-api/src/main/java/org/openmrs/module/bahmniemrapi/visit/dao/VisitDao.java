package org.openmrs.module.bahmniemrapi.visit.dao;

import org.openmrs.Visit;

import java.util.Date;
import java.util.List;

public interface VisitDao {
    public Visit getLatestVisit(String patientUuid, String conceptName);

    public List<Visit> getVisitOn(Date visitDate);
}
