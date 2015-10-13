package org.openmrs.module.bahmniemrapi.diagnosis.helper;

import org.openmrs.Concept;
import org.openmrs.Encounter;
import org.openmrs.Obs;
import org.openmrs.api.ConceptService;
import org.openmrs.api.ObsService;
import org.openmrs.module.bahmniemrapi.diagnosis.contract.BahmniDiagnosis;
import org.openmrs.module.bahmniemrapi.diagnosis.contract.BahmniDiagnosisRequest;
import org.openmrs.module.emrapi.EmrApiProperties;
import org.openmrs.module.emrapi.diagnosis.Diagnosis;
import org.openmrs.module.emrapi.encounter.EncounterTransactionMapper;
import org.openmrs.module.emrapi.encounter.domain.EncounterTransaction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;
@Component
public class BahmniDiagnosisMetadata {

    private static final String BAHMNI_DIAGNOSIS_STATUS = "Bahmni Diagnosis Status";
    private static final String BAHMNI_DIAGNOSIS_REVISED = "Bahmni Diagnosis Revised";
    private static final String BAHMNI_INITIAL_DIAGNOSIS = "Bahmni Initial Diagnosis";

    private ObsService obsService;

    private ConceptService conceptService;

    private EmrApiProperties emrApiProperties;

    private EncounterTransactionMapper encounterTransactionMapper;

    private Concept bahmniInitialDiagnosis;

    private Concept bahmniDiagnosisRevised;

    private Concept bahmniDiagnosisStatus;

    public Concept getBahmniInitialDiagnosis() {
        if (bahmniInitialDiagnosis == null) {
            bahmniInitialDiagnosis = conceptService.getConceptByName(BAHMNI_INITIAL_DIAGNOSIS);
        }
        return bahmniInitialDiagnosis;
    }

    public Concept getBahmniDiagnosisRevised() {
        if (bahmniDiagnosisRevised == null) {
            bahmniDiagnosisRevised = conceptService.getConceptByName(BAHMNI_DIAGNOSIS_REVISED);
        }
        return bahmniDiagnosisRevised;
    }

    public Concept getBahmniDiagnosisStatus() {
        if (bahmniDiagnosisStatus == null) {
            bahmniDiagnosisStatus = conceptService.getConceptByName(BAHMNI_DIAGNOSIS_STATUS);
        }
        return bahmniDiagnosisStatus;
    }

    @Autowired
    public BahmniDiagnosisMetadata(ObsService obsService, ConceptService conceptService, EmrApiProperties emrApiProperties, EncounterTransactionMapper encounterTransactionMapper) {
        this.obsService = obsService;
        this.conceptService = conceptService;
        this.emrApiProperties = emrApiProperties;
        this.encounterTransactionMapper = encounterTransactionMapper;
    }

    public String findInitialDiagnosisUuid(Obs visitDiagnosisObs) {
        for (Obs obs : visitDiagnosisObs.getGroupMembers()) {
            if (obs.getConcept().getName().getName().equals(BAHMNI_INITIAL_DIAGNOSIS)) {
                return obs.getValueText();
            }
        }
        return null;
    }

    public List<BahmniDiagnosisRequest> map(List<EncounterTransaction.Diagnosis> diagnoses, boolean includeAll) {
        List<BahmniDiagnosisRequest> bahmniDiagnoses = new ArrayList<>();
        for (EncounterTransaction.Diagnosis diagnosis : diagnoses) {
            bahmniDiagnoses.add(mapBahmniDiagnosis(diagnosis,null, true, includeAll));
        }
        return bahmniDiagnoses;
    }

    public BahmniDiagnosisRequest mapBahmniDiagnosis(EncounterTransaction.Diagnosis diagnosis, EncounterTransaction.Diagnosis latestDiagnosis,
                                                     boolean mapFirstDiagnosis, boolean includeAll) {
        BahmniDiagnosisRequest bahmniDiagnosis = mapBasicDiagnosis(diagnosis);
        bahmniDiagnosis.setExistingObs(diagnosis.getExistingObs());

        Obs diagnosisObsGroup = obsService.getObsByUuid(diagnosis.getExistingObs());
        if (diagnosisSchemaContainsStatus()){
            Obs statusObs = findObs(diagnosisObsGroup, BAHMNI_DIAGNOSIS_STATUS);
            if (statusObs != null){
                Concept statusConcept = statusObs.getValueCoded();
                if (statusConcept != null ) {
                    bahmniDiagnosis.setDiagnosisStatusConcept(new EncounterTransaction.Concept(statusConcept.getUuid(), statusConcept.getName().getName()));
                }
            }
        }

        if (mapFirstDiagnosis) {
            Obs initialDiagnosisObsGroup = obsService.getObsByUuid(findObs(diagnosisObsGroup, BAHMNI_INITIAL_DIAGNOSIS).getValueText());
            EncounterTransaction encounterTransactionWithInitialDiagnosis = encounterTransactionMapper.map(initialDiagnosisObsGroup.getEncounter(), includeAll);
            EncounterTransaction.Diagnosis initialDiagnosis = findInitialDiagnosis(encounterTransactionWithInitialDiagnosis, initialDiagnosisObsGroup);
            bahmniDiagnosis.setFirstDiagnosis(mapBahmniDiagnosis(initialDiagnosis, null, false, includeAll));
        }

        if(latestDiagnosis!=null){
            bahmniDiagnosis.setLatestDiagnosis(mapBahmniDiagnosis(latestDiagnosis,null,false,includeAll));
        }

        Obs revisedObs = findObs(diagnosisObsGroup, BAHMNI_DIAGNOSIS_REVISED);
        bahmniDiagnosis.setRevised(revisedObs.getValueAsBoolean());
        bahmniDiagnosis.setComments(diagnosisObsGroup.getComment());

        bahmniDiagnosis.setEncounterUuid(diagnosisObsGroup.getEncounter().getUuid());
        bahmniDiagnosis.setCreatorName(diagnosisObsGroup.getCreator().getPersonName().toString());
        return bahmniDiagnosis;
    }

    private BahmniDiagnosisRequest mapBasicDiagnosis(EncounterTransaction.Diagnosis diagnosis) {
        BahmniDiagnosisRequest bahmniDiagnosis = new BahmniDiagnosisRequest();
        bahmniDiagnosis.setCertainty(diagnosis.getCertainty());
        bahmniDiagnosis.setCodedAnswer(diagnosis.getCodedAnswer());
        bahmniDiagnosis.setFreeTextAnswer(diagnosis.getFreeTextAnswer());
        bahmniDiagnosis.setOrder(diagnosis.getOrder());
        bahmniDiagnosis.setExistingObs(diagnosis.getExistingObs());
        bahmniDiagnosis.setDiagnosisDateTime(diagnosis.getDiagnosisDateTime());
        bahmniDiagnosis.setProviders(diagnosis.getProviders());
        return bahmniDiagnosis;
    }

    private EncounterTransaction.Diagnosis findInitialDiagnosis(EncounterTransaction encounterTransactionWithInitialDiagnosis, Obs initialDiagnosisObs) {
        for (EncounterTransaction.Diagnosis diagnosis : encounterTransactionWithInitialDiagnosis.getDiagnoses()) {
            if (diagnosis.getExistingObs().equals(initialDiagnosisObs.getUuid()))
                return diagnosis;
        }
        throw new AssertionError(String.format("Initial Diagnosis not found for: %s", initialDiagnosisObs.getUuid()));
    }

    public void update(BahmniDiagnosisRequest bahmniDiagnosis, EncounterTransaction.Diagnosis diagnosis, Encounter encounter) {
        Obs matchingDiagnosisObs = findDiagnosisObsGroup(encounter, diagnosis.getExistingObs());

        updateFirstDiagnosis(matchingDiagnosisObs, bahmniDiagnosis);
        if (diagnosisSchemaContainsStatus()) {
            updateStatusConcept(matchingDiagnosisObs, bahmniDiagnosis);
        }
        updateRevisedConcept(matchingDiagnosisObs);

        matchingDiagnosisObs.setComment(bahmniDiagnosis.getComments());
    }

    private boolean diagnosisSchemaContainsStatus() {
        Concept diagnosisSetConcept = getDiagnosisSetConcept();
        return diagnosisSetConcept.getSetMembers().contains(getBahmniDiagnosisStatus());
    }

    private void updateFirstDiagnosis(Obs diagnosisObs, BahmniDiagnosisRequest bahmniDiagnosis) {
        Obs obs = findOrCreateObs(diagnosisObs, getBahmniInitialDiagnosis());
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
        return null;
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

    private void updateStatusConcept(Obs diagnosisObs, BahmniDiagnosis bahmniDiagnosis) {
        Obs obs = findOrCreateObs(diagnosisObs, getBahmniDiagnosisStatus());
        Concept statusConcept = null;
        if (bahmniDiagnosis.getDiagnosisStatusConcept() != null) {
            statusConcept = conceptService.getConcept(bahmniDiagnosis.getDiagnosisStatusConcept().getName());
        }
        obs.setValueCoded(statusConcept);
        addToObsGroup(diagnosisObs, obs);
    }

    private void updateRevisedConcept(Obs diagnosisObs) {
        Obs obs = findOrCreateObs(diagnosisObs, getBahmniDiagnosisRevised());
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

    public Obs findInitialDiagnosis(Obs diagnosisObsGroup) {
        return findObs(diagnosisObsGroup, BAHMNI_INITIAL_DIAGNOSIS);
    }

    public Diagnosis buildDiagnosisFromObsGroup(Obs diagnosisObsGroup) {
        if (diagnosisObsGroup == null)
            return null;

        Diagnosis diagnosis = emrApiProperties.getDiagnosisMetadata().toDiagnosis(diagnosisObsGroup);

        Collection<Concept> nonDiagnosisConcepts = emrApiProperties.getSuppressedDiagnosisConcepts();
        Collection<Concept> nonDiagnosisConceptSets = emrApiProperties.getNonDiagnosisConceptSets();

        Set<Concept> filter = new HashSet<Concept>();
        filter.addAll(nonDiagnosisConcepts);
        for (Concept conceptSet : nonDiagnosisConceptSets) {
            filter.addAll(conceptSet.getSetMembers());
        }

        if (!filter.contains(diagnosis.getDiagnosis().getCodedAnswer())) {
            return diagnosis;
        }
        return null;
    }

    public Concept getDiagnosisSetConcept() {
        return emrApiProperties.getDiagnosisMetadata().getDiagnosisSetConcept();
    }

}
