package org.bahmni.module.bahmnicore.service.impl;

import org.apache.log4j.Logger;
import org.bahmni.module.bahmnicore.contract.encounter.data.ConceptData;
import org.bahmni.module.bahmnicore.service.BahmniEncounterModifierService;
import org.openmrs.module.bahmniemrapi.encountertransaction.contract.BahmniEncounterTransaction;
import org.openmrs.module.bahmniemrapi.encountertransaction.contract.BahmniObservation;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class BahmniEncounterModifierServiceImpl implements BahmniEncounterModifierService {

    private static Logger logger = Logger.getLogger(BahmniEncounterModifierServiceImpl.class);

    @Override
    public BahmniEncounterTransaction modifyEncounter(BahmniEncounterTransaction bahmniEncounterTransaction, ConceptData conceptSetData){
        List<BahmniObservation> observationsFromConceptSet = new ArrayList<>();
        BahmniObservation conceptSetObservation = findConceptSetObservationFromEncounterTransaction(bahmniEncounterTransaction.getObservations(), conceptSetData);
        getLeafObservationsFromConceptSet(conceptSetObservation, observationsFromConceptSet);
        return null;
    }

    private BahmniObservation findConceptSetObservationFromEncounterTransaction(List<BahmniObservation> observations, ConceptData conceptSetData) {
        for (BahmniObservation observation : observations) {
            if(observation.getConcept().getUuid().equals(conceptSetData.getUuid())){
                return observation;
            }
        }
        return null;
    }

    private void getLeafObservationsFromConceptSet(BahmniObservation bahmniObservation, List<BahmniObservation> observationsFromConceptSet) {
        if (bahmniObservation.getGroupMembers().size() > 0) {
            for (BahmniObservation groupMember : bahmniObservation.getGroupMembers())
                getLeafObservationsFromConceptSet(groupMember, observationsFromConceptSet);
        } else {
            observationsFromConceptSet.add(bahmniObservation);
        }
    }
}
