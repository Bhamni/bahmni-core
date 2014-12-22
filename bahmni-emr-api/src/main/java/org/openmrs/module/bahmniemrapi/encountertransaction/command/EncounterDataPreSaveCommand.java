package org.openmrs.module.bahmniemrapi.encountertransaction.command;

import org.openmrs.module.bahmniemrapi.encountertransaction.contract.BahmniEncounterTransaction;

public interface EncounterDataPreSaveCommand {

    BahmniEncounterTransaction update(BahmniEncounterTransaction bahmniEncounterTransaction);
}
