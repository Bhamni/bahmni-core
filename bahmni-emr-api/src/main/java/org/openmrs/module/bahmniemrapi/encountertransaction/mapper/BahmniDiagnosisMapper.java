package org.openmrs.module.bahmniemrapi.encountertransaction.mapper;

import org.openmrs.Concept;
import org.openmrs.Obs;
import org.openmrs.api.ObsService;
import org.openmrs.module.bahmniemrapi.diagnosis.contract.BahmniDiagnosisRequest;
import org.openmrs.module.bahmniemrapi.diagnosis.helper.BahmniDiagnosisHelper;
import org.openmrs.module.emrapi.encounter.EncounterTransactionMapper;
import org.openmrs.module.emrapi.encounter.domain.EncounterTransaction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class BahmniDiagnosisMapper {
    private ObsService obsService;
    private EncounterTransactionMapper encounterTransactionMapper;

    @Autowired
    public BahmniDiagnosisMapper(ObsService obsService, EncounterTransactionMapper encounterTransactionMapper) {
        this.obsService = obsService;
        this.encounterTransactionMapper = encounterTransactionMapper;
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
        Obs statusObs = findObs(diagnosisObsGroup, BahmniDiagnosisHelper.BAHMNI_DIAGNOSIS_STATUS);
        Concept statusConcept = statusObs.getValueCoded();
        if (statusConcept != null) {
            bahmniDiagnosis.setDiagnosisStatusConcept(new EncounterTransaction.Concept(statusConcept.getUuid(), statusConcept.getName().getName()));
        }

        if (mapFirstDiagnosis) {
            Obs initialDiagnosisObsGroup = obsService.getObsByUuid(findObs(diagnosisObsGroup, BahmniDiagnosisHelper.BAHMNI_INITIAL_DIAGNOSIS).getValueText());
            EncounterTransaction encounterTransactionWithInitialDiagnosis = encounterTransactionMapper.map(initialDiagnosisObsGroup.getEncounter(), includeAll);
            EncounterTransaction.Diagnosis initialDiagnosis = findInitialDiagnosis(encounterTransactionWithInitialDiagnosis, initialDiagnosisObsGroup);
            bahmniDiagnosis.setFirstDiagnosis(mapBahmniDiagnosis(initialDiagnosis, null, false, includeAll));
        }

        if(latestDiagnosis!=null){
            bahmniDiagnosis.setLatestDiagnosis(mapBahmniDiagnosis(latestDiagnosis,null,false,includeAll));
        }

        Obs revisedObs = findObs(diagnosisObsGroup, BahmniDiagnosisHelper.BAHMNI_DIAGNOSIS_REVISED);
        bahmniDiagnosis.setRevised(revisedObs.getValueAsBoolean());
        bahmniDiagnosis.setComments(diagnosisObsGroup.getComment());

        bahmniDiagnosis.setEncounterUuid(diagnosisObsGroup.getEncounter().getUuid());
        bahmniDiagnosis.setPersonName(diagnosisObsGroup.getCreator().getPersonName().toString());
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

    private Obs findObs(Obs diagnosisObs, String conceptName) {
        for (Obs o : diagnosisObs.getGroupMembers()) {
            if (o.getConcept().hasName(conceptName, null)) {
                return o;
            }
        }
        throw new AssertionError(String.format("Diagnosis found without meta-data for %s, diagnosisObsUUID: %s", conceptName, diagnosisObs.getUuid()));
    }

    private EncounterTransaction.Diagnosis findInitialDiagnosis(EncounterTransaction encounterTransactionWithInitialDiagnosis, Obs initialDiagnosisObs) {
        for (EncounterTransaction.Diagnosis diagnosis : encounterTransactionWithInitialDiagnosis.getDiagnoses()) {
            if (diagnosis.getExistingObs().equals(initialDiagnosisObs.getUuid()))
                return diagnosis;
        }
        throw new AssertionError(String.format("Initial Diagnosis not found for: %s", initialDiagnosisObs.getUuid()));
    }
}
