package org.bahmni.module.bahmnicore.dao;

import org.openmrs.Encounter;
import org.openmrs.Patient;
import org.openmrs.Visit;

import java.util.List;

public interface VisitDao {
    public Visit getLatestVisit(String patientUuid, String conceptName);

    Visit getVisitSummary(String visitUuid);

    List<Encounter> getAdmitAndDischargeEncounters(Integer visitId);

    List<Visit> getVisitsByPatient(Patient patient, int numberOfVisits);

    List<Integer> getVisitIdsFor(String patientUuid, Integer numberOfVisits);

}
