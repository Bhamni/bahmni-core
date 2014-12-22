package org.openmrs.module.bahmniemrapi.encountertransaction.command;

import org.openmrs.Encounter;
import org.openmrs.module.bahmniemrapi.encountertransaction.contract.BahmniEncounterTransaction;
import org.openmrs.module.emrapi.encounter.domain.EncounterTransaction;

public interface EncounterDataPostSaveCommand {

    EncounterTransaction save(BahmniEncounterTransaction bahmniEncounterTransaction, Encounter currentEncounter, EncounterTransaction updatedEncounterTransaction);
}
