package org.bahmni.module.bahmnicore.service.impl;

import org.bahmni.module.bahmnicore.util.VisitIdentificationHelper;
import org.openmrs.Patient;
import org.openmrs.Visit;
import org.openmrs.api.VisitService;
import org.openmrs.module.bahmniemrapi.encountertransaction.contract.BahmniEncounterTransaction;
import org.openmrs.module.bahmniemrapi.encountertransaction.service.BahmniEncounterTransactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
public class RetrospectiveEncounterTransactionService {
    private BahmniEncounterTransactionService bahmniEncounterTransactionService;
    protected final VisitIdentificationHelper visitIdentificationHelper;

    @Autowired
    public RetrospectiveEncounterTransactionService(BahmniEncounterTransactionService bahmniEncounterTransactionService, VisitService visitService) {
        this.bahmniEncounterTransactionService = bahmniEncounterTransactionService;
        visitIdentificationHelper = new VisitIdentificationHelper(visitService);
    }

    public BahmniEncounterTransaction save(BahmniEncounterTransaction bahmniEncounterTransaction, Patient patient, Date visitStartDate, Date visitEndDate) {
        Visit matchingVisit = visitIdentificationHelper.getVisitFor(patient, bahmniEncounterTransaction.getVisitType(),
                bahmniEncounterTransaction.getEncounterDateTime(), visitStartDate, visitEndDate);
        bahmniEncounterTransaction.setVisitUuid(matchingVisit.getUuid());
        // TODO : Mujir - this should not happen here. Just set the visitType. BahmniEncounterTransaction should handle string visitTypes.
        bahmniEncounterTransaction.setVisitTypeUuid(matchingVisit.getVisitType().getUuid());

        return bahmniEncounterTransactionService.save(bahmniEncounterTransaction);
    }
}
