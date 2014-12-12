package org.openmrs.module.bahmniemrapi.encountertransaction.service.impl;

import org.bahmni.module.bahmnicore.util.VisitIdentificationHelper;
import org.openmrs.Patient;
import org.openmrs.Visit;
import org.openmrs.api.VisitService;
import org.openmrs.module.bahmniemrapi.encountertransaction.contract.BahmniEncounterTransaction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Date;

import static org.openmrs.module.bahmniemrapi.utils.DateUtils.isBefore;

@Component
public class RetrospectiveEncounterTransactionService {
    protected final VisitIdentificationHelper visitIdentificationHelper;

    @Autowired
    public RetrospectiveEncounterTransactionService(VisitService visitService) {
        visitIdentificationHelper = new VisitIdentificationHelper(visitService);
    }

    public BahmniEncounterTransaction updateForMatchingVisit(BahmniEncounterTransaction bahmniEncounterTransaction, Patient patient, Date visitStartDate, Date visitEndDate) {
        if (bahmniEncounterTransaction.getEncounterDateTime() == null || !isBefore(bahmniEncounterTransaction.getEncounterDateTime(), new Date())) {
            return bahmniEncounterTransaction;
        }

        Visit matchingVisit = visitIdentificationHelper.getVisitFor(patient, bahmniEncounterTransaction.getVisitType(),
                bahmniEncounterTransaction.getEncounterDateTime(), visitStartDate, visitEndDate);
        bahmniEncounterTransaction.setVisitUuid(matchingVisit.getUuid());
        // TODO : Mujir - this should not happen here. Just set the visitType. BahmniEncounterTransaction should handle string visitTypes.
        bahmniEncounterTransaction.setVisitTypeUuid(matchingVisit.getVisitType().getUuid());
        return bahmniEncounterTransaction;
    }
}
