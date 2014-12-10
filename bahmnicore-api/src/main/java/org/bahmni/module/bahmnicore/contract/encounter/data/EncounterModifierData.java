package org.bahmni.module.bahmnicore.contract.encounter.data;

import org.openmrs.module.bahmniemrapi.encountertransaction.contract.BahmniEncounterTransaction;

public class EncounterModifierData {
    private BahmniEncounterTransaction bahmniEncounterTransaction;
    private ConceptData conceptSetData;

    public EncounterModifierData(BahmniEncounterTransaction bahmniEncounterTransaction, ConceptData conceptSetData) {
        this.bahmniEncounterTransaction = bahmniEncounterTransaction;
        this.conceptSetData = conceptSetData;
    }

    public EncounterModifierData() {
    }

    public BahmniEncounterTransaction getBahmniEncounterTransaction() {
        return bahmniEncounterTransaction;
    }

    public void setBahmniEncounterTransaction(BahmniEncounterTransaction bahmniEncounterTransaction) {
        this.bahmniEncounterTransaction = bahmniEncounterTransaction;
    }

    public ConceptData getConceptSetData() {
        return conceptSetData;
    }

    public void setConceptSetData(ConceptData conceptSetData) {
        this.conceptSetData = conceptSetData;
    }
}
