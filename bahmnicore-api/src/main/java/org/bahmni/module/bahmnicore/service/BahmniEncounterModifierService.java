package org.bahmni.module.bahmnicore.service;

import org.bahmni.module.bahmnicore.contract.encounter.data.ConceptData;
import org.bahmni.module.bahmnicore.encounterModifier.exception.CannotModifyEncounterException;
import org.openmrs.module.bahmniemrapi.encountertransaction.contract.BahmniEncounterTransaction;

import java.io.IOException;

public interface BahmniEncounterModifierService {
    public BahmniEncounterTransaction getModifiedEncounter(BahmniEncounterTransaction bahmniEncounterTransaction, ConceptData conceptSetData) throws CannotModifyEncounterException, InstantiationException, IllegalAccessException, IOException;
}
