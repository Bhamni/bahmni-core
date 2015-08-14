package org.bahmni.module.admin.retrospectiveEncounter.service;

import java.util.Collection;

import org.bahmni.module.admin.retrospectiveEncounter.domain.DuplicateObservationsMatcher;
import org.openmrs.Patient;
import org.openmrs.Visit;
import org.openmrs.api.VisitService;
import org.openmrs.module.bahmniemrapi.diagnosis.contract.BahmniDiagnosisRequest;
import org.openmrs.module.bahmniemrapi.encountertransaction.contract.BahmniEncounterTransaction;
import org.openmrs.module.bahmniemrapi.encountertransaction.contract.BahmniObservation;
import org.openmrs.module.bahmniemrapi.encountertransaction.service.VisitIdentificationHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;

@Component
public class DuplicateObservationService {
    protected final VisitIdentificationHelper visitIdentificationHelper;

    @Autowired
    public DuplicateObservationService(VisitService visitService) {
        visitIdentificationHelper = new VisitIdentificationHelper(visitService);
    }

    public void filter(BahmniEncounterTransaction bahmniEncounterTransaction, Patient patient, Date visitStartDate, Date visitEndDate) {
        Visit matchingVisit = visitIdentificationHelper.getVisitFor(patient, bahmniEncounterTransaction.getVisitType(),
                bahmniEncounterTransaction.getEncounterDateTime(), visitStartDate, visitEndDate);

        DuplicateObservationsMatcher duplicateObservationsMatcher = new DuplicateObservationsMatcher(matchingVisit, bahmniEncounterTransaction.getEncounterType());
        Collection<BahmniObservation> uniqueObservations = duplicateObservationsMatcher.getNewlyAddedBahmniObservations(bahmniEncounterTransaction.getObservations(), bahmniEncounterTransaction.getEncounterDateTime());
        List<BahmniDiagnosisRequest> uniqueDiagnoses = duplicateObservationsMatcher.getUniqueDiagnoses(bahmniEncounterTransaction.getBahmniDiagnoses());

        bahmniEncounterTransaction.setObservations(uniqueObservations);
        bahmniEncounterTransaction.setBahmniDiagnoses(uniqueDiagnoses);
    }
}