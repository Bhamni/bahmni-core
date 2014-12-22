package org.openmrs.module.bahmniemrapi.encountertransaction.command.impl;

import org.bahmni.module.obsrelationship.api.ObsRelationService;
import org.bahmni.module.obsrelationship.model.ObsRelationship;
import org.bahmni.module.obsrelationship.model.ObsRelationshipType;
import org.openmrs.Encounter;
import org.openmrs.Obs;
import org.openmrs.api.ObsService;
import org.openmrs.module.bahmniemrapi.encountertransaction.command.EncounterDataPostSaveCommand;
import org.openmrs.module.bahmniemrapi.encountertransaction.contract.BahmniEncounterTransaction;
import org.openmrs.module.bahmniemrapi.encountertransaction.contract.BahmniObservation;
import org.openmrs.module.emrapi.encounter.domain.EncounterTransaction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class BahmniObservationSaveCommandImpl implements EncounterDataPostSaveCommand {
    private ObsRelationService obsRelationService;
    private ObsService obsService;

    @Autowired
    public BahmniObservationSaveCommandImpl(ObsRelationService obsRelationService, ObsService obsService) {
        this.obsRelationService = obsRelationService;
        this.obsService = obsService;
    }

    @Override
    public EncounterTransaction save(BahmniEncounterTransaction bahmniEncounterTransaction, Encounter currentEncounter, EncounterTransaction updatedEncounterTransaction) {
        for (BahmniObservation  bahmniObservation : bahmniEncounterTransaction.getObservations()) {
            if(bahmniObservation.hasTargetObsRelation()){
                Obs srcObservation =findMatchingObservation(bahmniObservation, currentEncounter);
                Obs targetObservation =findMatchingObservation(bahmniObservation.getTargetObsRelation().getTargetObs(), currentEncounter);

                if(targetObservation == null){
                    String uuid = bahmniObservation.getTargetObsRelation().getTargetObs().getUuid();
                    targetObservation = obsService.getObsByUuid(uuid);
                }
                ObsRelationshipType obsRelationshipType = obsRelationService.getRelationshipTypeByName(bahmniObservation.getTargetObsRelation().getRelationshipType());
                ObsRelationship obsRelation =  createNewIfDoesNotExist(bahmniObservation.getTargetObsRelation().getUuid());
                obsRelation.setSourceObs(srcObservation);
                obsRelation.setTargetObs(targetObservation);
                obsRelation.setObsRelationshipType(obsRelationshipType);

                obsRelationService.saveOrUpdate(obsRelation);
            }
        }
        return updatedEncounterTransaction;
    }

    private ObsRelationship createNewIfDoesNotExist(String obsRelationUuid){
        ObsRelationship obsRelation = new ObsRelationship();
        if(obsRelationUuid!= null){
            obsRelation = obsRelationService.getRelationByUuid(obsRelationUuid);
            if(obsRelation == null){
                obsRelation = new ObsRelationship();
            }
        }
        return obsRelation;
    }

    private Obs findMatchingObservation(BahmniObservation bahmniObservation, Encounter currentEncounter) {
        for (Obs obs : currentEncounter.getAllObs()) {
            if(bahmniObservation.isSameAs(obs)){
                return obs;
            }
        }
        return null;
    }
}
