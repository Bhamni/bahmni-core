package org.bahmni.module.bahmnicore.service;

import org.bahmni.module.bahmnicore.contract.encounter.data.ConceptData;
import org.openmrs.module.bahmniemrapi.encountertransaction.contract.BahmniEncounterTransaction;

public interface BahmniEncounterModifierService {
    public BahmniEncounterTransaction modifyEncounter(BahmniEncounterTransaction bahmniEncounterTransaction, ConceptData conceptSetData);
}
