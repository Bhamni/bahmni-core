package org.openmrs.module.bahmniemrapi.diagnosis.helper;

import com.sun.org.apache.bcel.internal.generic.GOTO;
import org.openmrs.Concept;
import org.openmrs.Encounter;
import org.openmrs.Obs;
import org.openmrs.api.ConceptService;
import org.openmrs.api.ObsService;
import org.openmrs.module.bahmniemrapi.diagnosis.contract.BahmniDiagnosis;
import org.openmrs.module.bahmniemrapi.diagnosis.contract.BahmniDiagnosisRequest;
import org.openmrs.module.emrapi.encounter.domain.EncounterTransaction;

public class BahmniDiagnosisHelper {

    public static final String BAHMNI_DIAGNOSIS_STATUS = "Bahmni Diagnosis Status";
    public static final String BAHMNI_DIAGNOSIS_REVISED = "Bahmni Diagnosis Revised";
    public static final String BAHMNI_INITIAL_DIAGNOSIS = "Bahmni Initial Diagnosis";

    private ObsService obsService;

    private ConceptService conceptService;

    protected Concept bahmniInitialDiagnosisConcept;
    protected Concept bahmniDiagnosisStatusConcept;
    protected Concept bahmniDiagnosisRevisedConcept;

    public BahmniDiagnosisHelper(ObsService obsService, ConceptService conceptService) {
        this.obsService = obsService;
        this.conceptService = conceptService;
    }

    public void updateDiagnosisMetaData(BahmniDiagnosisRequest bahmniDiagnosis, EncounterTransaction.Diagnosis diagnosis, Encounter encounter) {
        Obs matchingDiagnosisObs = findDiagnosisObsGroup(encounter, diagnosis.getExistingObs());

        updateFirstDiagnosis(matchingDiagnosisObs, bahmniDiagnosis, getBahmniInitialDiagnosisConcept());
        updateStatusConcept(matchingDiagnosisObs, bahmniDiagnosis, getBahmniDiagnosisStatusConcept());
        updateRevisedConcept(matchingDiagnosisObs, getBahmniDiagnosisRevisedConcept());

        matchingDiagnosisObs.setComment(bahmniDiagnosis.getComments());
    }

    private Concept getBahmniDiagnosisRevisedConcept() {
        if (bahmniDiagnosisRevisedConcept == null)
            bahmniDiagnosisRevisedConcept = conceptService.getConceptByName(BAHMNI_DIAGNOSIS_REVISED);

        return bahmniDiagnosisRevisedConcept;
    }

    private Concept getBahmniDiagnosisStatusConcept() {
        if (bahmniDiagnosisStatusConcept == null)
            bahmniDiagnosisStatusConcept = conceptService.getConceptByName(BAHMNI_DIAGNOSIS_STATUS);

        return bahmniDiagnosisStatusConcept;
    }

    private Concept getBahmniInitialDiagnosisConcept() {
        if (bahmniInitialDiagnosisConcept == null)
            bahmniInitialDiagnosisConcept = conceptService.getConceptByName(BAHMNI_INITIAL_DIAGNOSIS);

        return bahmniInitialDiagnosisConcept = conceptService.getConceptByName(BAHMNI_INITIAL_DIAGNOSIS);
    }

    private void updateFirstDiagnosis(Obs diagnosisObs, BahmniDiagnosisRequest bahmniDiagnosis, Concept bahmniInitialDiagnosis) {
        Obs obs = findOrCreateObs(diagnosisObs, bahmniInitialDiagnosis);
        if (bahmniDiagnosis.getPreviousObs() == null && obs.getId() == null) { //Diagnosis captured for first time in this encounter 
            obs.setValueText(diagnosisObs.getUuid());
        } else { //Diagnosis update in the same encounter it was created in AND Diagnosis updated from another encounter
            Obs firstDiagnosisObs = obsService.getObsByUuid(bahmniDiagnosis.getFirstDiagnosis().getExistingObs());
            obs.setValueText(firstDiagnosisObs.getUuid());
        }
        addToObsGroup(diagnosisObs, obs);
    }

    public void markAsRevised(Encounter encounter, String diagnosisObsUUID) {
        Obs diagnosisObs = null;
        for (Obs obs : encounter.getAllObs()) {
            if (obs.getUuid().equals(diagnosisObsUUID)) {
                diagnosisObs = obs;
                break;
            }
        }
        if (diagnosisObs == null)
            throw new AssertionError(String.format("Cannot find revised obs in the diagnosis obs group %s", diagnosisObsUUID));
        Obs revisedObs = findObs(diagnosisObs, BAHMNI_DIAGNOSIS_REVISED);
        revisedObs.setValueBoolean(true);
    }


    
    private Obs findDiagnosisObsGroup(Encounter encounter, String obsUUID) {
        for (Obs obs : encounter.getAllObs()) {
            if (obs.getUuid().equals(obsUUID)) return obs;
        }
        throw new AssertionError(String.format("Should have found observation %s in encounter %s", obsUUID, encounter.getUuid()));
    }

    private Obs findObs(Obs diagnosisObs, String conceptName) {
        for (Obs o : diagnosisObs.getGroupMembers()) {
            if (o.getConcept().hasName(conceptName, null)) {
                return o;
            }
        }
        throw new AssertionError(String.format("Diagnosis found without meta-data for %s, diagnosisObsUUID: %s", conceptName, diagnosisObs.getUuid()));
    }

    private Obs findOrCreateObs(Obs diagnosisObs, Concept concept) {
        for (Obs o : diagnosisObs.getGroupMembers()) {
            if (concept.equals(o.getConcept())) {
                return o;
            }
        }
        Obs obs = new Obs();
        obs.setConcept(concept);
        return obs;
    }

    private void updateStatusConcept(Obs diagnosisObs, BahmniDiagnosis bahmniDiagnosis, Concept bahmniDiagnosisStatusConcept) {
        Obs obs = findOrCreateObs(diagnosisObs, bahmniDiagnosisStatusConcept);
        Concept statusConcept = null;
        if (bahmniDiagnosis.getDiagnosisStatusConcept() != null) {
            statusConcept = conceptService.getConcept(bahmniDiagnosis.getDiagnosisStatusConcept().getName());
        }
        obs.setValueCoded(statusConcept);
        addToObsGroup(diagnosisObs, obs);
    }

    private void updateRevisedConcept(Obs diagnosisObs, Concept bahmniDiagnosisRevisedConcept) {
        Obs obs = findOrCreateObs(diagnosisObs, bahmniDiagnosisRevisedConcept);
        obs.setValueBoolean(false);
        addToObsGroup(diagnosisObs, obs);
    }

    private void addToObsGroup(Obs obsGroup, Obs member) {
        member.setPerson(obsGroup.getPerson());
        member.setObsDatetime(obsGroup.getObsDatetime());
        member.setLocation(obsGroup.getLocation());
        member.setEncounter(obsGroup.getEncounter());
        obsGroup.addGroupMember(member);
    }
}
