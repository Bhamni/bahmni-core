package org.bahmni.module.bahmnicore.encounterModifier;

import org.bahmni.module.bahmnicore.contract.encounter.data.ConceptData;
import org.bahmni.module.bahmnicore.encounterModifier.exception.CannotModifyEncounterException;
import org.openmrs.module.bahmniemrapi.encountertransaction.contract.BahmniEncounterTransaction;

public abstract class EncounterModifier {
    public abstract BahmniEncounterTransaction run(BahmniEncounterTransaction bahmniEncounterTransaction, ConceptData conceptSetData) throws CannotModifyEncounterException;
}
