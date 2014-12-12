package org.openmrs.module.bahmniemrapi.encountertransaction.service;

import org.openmrs.Patient;
import org.openmrs.module.bahmniemrapi.encountertransaction.contract.BahmniEncounterTransaction;

import java.util.Date;

public interface BahmniEncounterTransactionService {
    BahmniEncounterTransaction save(BahmniEncounterTransaction encounterTransaction);

    BahmniEncounterTransaction save(BahmniEncounterTransaction bahmniEncounterTransaction, Patient patient, Date visitStartDate, Date visitEndDate);
}
