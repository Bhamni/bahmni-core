package org.openmrs.module.bahmniemrapi.encountertransaction.command.impl;

import org.openmrs.Encounter;
import org.openmrs.Obs;
import org.openmrs.api.ConceptService;
import org.openmrs.api.EncounterService;
import org.openmrs.api.ObsService;
import org.openmrs.module.bahmniemrapi.BahmniEmrAPIException;
import org.openmrs.module.bahmniemrapi.diagnosis.contract.BahmniDiagnosis;
import org.openmrs.module.bahmniemrapi.diagnosis.contract.BahmniDiagnosisRequest;
import org.openmrs.module.bahmniemrapi.diagnosis.helper.BahmniDiagnosisHelper;
import org.openmrs.module.bahmniemrapi.encountertransaction.command.EncounterDataPostSaveCommand;
import org.openmrs.module.bahmniemrapi.encountertransaction.contract.BahmniEncounterTransaction;
import org.openmrs.module.emrapi.encounter.domain.EncounterTransaction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
@Component
public class BahmniDiagnosisSaveCommandImpl implements EncounterDataPostSaveCommand {
    private ObsService obsService;
    private ConceptService conceptService;
    private EncounterService encounterService;
    protected BahmniDiagnosisHelper bahmniDiagnosisHelper;

    @Autowired
    public BahmniDiagnosisSaveCommandImpl(ObsService obsService, ConceptService conceptService, EncounterService encounterService) {
        this.obsService = obsService;
        this.conceptService = conceptService;
        this.encounterService = encounterService;
    }

    @Override
    public EncounterTransaction save(BahmniEncounterTransaction bahmniEncounterTransaction, Encounter currentEncounter, EncounterTransaction updatedEncounterTransaction) {
        if(bahmniEncounterTransaction.getBahmniDiagnoses().size() == 0){
            return updatedEncounterTransaction;
        }
        return saveDiagnoses(bahmniEncounterTransaction,currentEncounter,updatedEncounterTransaction);
    }

    private EncounterTransaction saveDiagnoses(BahmniEncounterTransaction bahmniEncounterTransaction, Encounter currentEncounter,EncounterTransaction updatedEncounterTransaction) {
        //Update the diagnosis information with Meta Data managed by Bahmni
        for (BahmniDiagnosisRequest bahmniDiagnosis : bahmniEncounterTransaction.getBahmniDiagnoses()) {
            EncounterTransaction.Diagnosis diagnosis = getMatchingEncounterTransactionDiagnosis(bahmniDiagnosis, updatedEncounterTransaction.getDiagnoses());
            getBahmniDiagnosisHelper().updateDiagnosisMetaData(bahmniDiagnosis, diagnosis, currentEncounter);
        }
        encounterService.saveEncounter(currentEncounter);

        // Void the previous diagnosis if required
        for (BahmniDiagnosisRequest bahmniDiagnosis : bahmniEncounterTransaction.getBahmniDiagnoses()) {
            String previousDiagnosisObs = bahmniDiagnosis.getPreviousObs();
            if (previousDiagnosisObs == null) continue;

            Obs diagnosisObs = obsService.getObsByUuid(previousDiagnosisObs);
            Encounter encounterForDiagnosis = encounterService.getEncounterByUuid(diagnosisObs.getEncounter().getUuid());
            if (!encounterForDiagnosis.equals(currentEncounter)) {
                getBahmniDiagnosisHelper().markAsRevised(encounterForDiagnosis, diagnosisObs.getUuid());
                encounterService.saveEncounter(encounterForDiagnosis);
            }
        }
        return updatedEncounterTransaction;
    }

    private BahmniDiagnosisHelper getBahmniDiagnosisHelper() {
        if (bahmniDiagnosisHelper == null)
            bahmniDiagnosisHelper = new BahmniDiagnosisHelper(obsService, conceptService);

        return bahmniDiagnosisHelper;
    }

    private EncounterTransaction.Diagnosis getMatchingEncounterTransactionDiagnosis(BahmniDiagnosis bahmniDiagnosis, List<EncounterTransaction.Diagnosis> encounterTransactionDiagnoses) {
        for (EncounterTransaction.Diagnosis diagnosis : encounterTransactionDiagnoses) {
            if (bahmniDiagnosis.isSame(diagnosis)) {
                return diagnosis;
            }
        }
        throw new BahmniEmrAPIException("Error fetching the saved diagnosis for  " + bahmniDiagnosis.getCodedAnswer().getName());
    }

}
