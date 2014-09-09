package org.openmrs.module.bahmniemrapi.encountertransaction.impl;

import org.apache.commons.lang.StringUtils;
import org.openmrs.Encounter;
import org.openmrs.EncounterType;
import org.openmrs.Obs;
import org.openmrs.api.ConceptService;
import org.openmrs.api.EncounterService;
import org.openmrs.api.ObsService;
import org.openmrs.api.VisitService;
import org.openmrs.module.bahmniemrapi.BahmniEmrAPIException;
import org.openmrs.module.bahmniemrapi.accessionnote.mapper.AccessionNotesMapper;
import org.openmrs.module.bahmniemrapi.diagnosis.contract.BahmniDiagnosis;
import org.openmrs.module.bahmniemrapi.diagnosis.contract.BahmniDiagnosisRequest;
import org.openmrs.module.bahmniemrapi.diagnosis.helper.BahmniDiagnosisHelper;
import org.openmrs.module.bahmniemrapi.encountertransaction.contract.BahmniEncounterTransaction;
import org.openmrs.module.bahmniemrapi.encountertransaction.mapper.BahmniEncounterTransactionMapper;
import org.openmrs.module.bahmniemrapi.encountertransaction.mapper.EncounterTransactionDiagnosisMapper;
import org.openmrs.module.bahmniemrapi.encountertransaction.mapper.EncounterTransactionObsMapper;
import org.openmrs.module.bahmniemrapi.encountertransaction.service.BahmniEncounterTransactionService;
import org.openmrs.module.emrapi.encounter.EmrEncounterService;
import org.openmrs.module.emrapi.encounter.EncounterTransactionMapper;
import org.openmrs.module.emrapi.encounter.domain.EncounterTransaction;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Transactional
public class BahmniEncounterTransactionServiceImpl implements BahmniEncounterTransactionService {

    private ConceptService conceptService;
    private EncounterService encounterService;
    private EmrEncounterService emrEncounterService;
    private EncounterTransactionMapper encounterTransactionMapper;
    private ObsService obsService;
    private AccessionNotesMapper accessionNotesMapper;
    private EncounterTransactionObsMapper encounterTransactionObsMapper;

    public BahmniEncounterTransactionServiceImpl(ConceptService conceptService,
                                                 EncounterService encounterService, ObsService obsService,
                                                 EmrEncounterService emrEncounterService, EncounterTransactionMapper encounterTransactionMapper,
                                                 EncounterTransactionObsMapper encounterTransactionObsMapper, AccessionNotesMapper accessionNotesMapper) {
        this.conceptService = conceptService;
        this.encounterService = encounterService;
        this.emrEncounterService = emrEncounterService;
        this.encounterTransactionMapper = encounterTransactionMapper;
        this.obsService = obsService;
        this.accessionNotesMapper = accessionNotesMapper;
        this.encounterTransactionObsMapper = encounterTransactionObsMapper;
    }

    @Override
    public BahmniEncounterTransaction save(BahmniEncounterTransaction bahmniEncounterTransaction) {
        // TODO : Mujir - map string VisitType to the uuids and set on bahmniEncounterTransaction object
        String encounterTypeString = bahmniEncounterTransaction.getEncounterType();
        if (bahmniEncounterTransaction.getEncounterTypeUuid() == null && StringUtils.isNotEmpty(encounterTypeString)) {
            EncounterType encounterType = encounterService.getEncounterType(encounterTypeString);
            if (encounterType == null) {
                throw new RuntimeException("Encounter type:'" + encounterTypeString + "' not found.");
            }
            bahmniEncounterTransaction.setEncounterTypeUuid(encounterType.getUuid());
        }

        new EncounterTransactionDiagnosisMapper().populateDiagnosis(bahmniEncounterTransaction);

        EncounterTransaction encounterTransaction = emrEncounterService.save(bahmniEncounterTransaction);

        //Get the saved encounter transaction from emr-api
        String encounterUuid = encounterTransaction.getEncounterUuid();
        Encounter currentEncounter = encounterService.getEncounterByUuid(encounterUuid);
        EncounterTransaction updatedEncounterTransaction = encounterTransactionMapper.map(currentEncounter, true);

        //Update the diagnosis information with Meta Data managed by Bahmni
        BahmniDiagnosisHelper bahmniDiagnosisHelper = new BahmniDiagnosisHelper(obsService, conceptService);
        for (BahmniDiagnosisRequest bahmniDiagnosis : bahmniEncounterTransaction.getBahmniDiagnoses()) {
            EncounterTransaction.Diagnosis diagnosis = getMatchingEncounterTransactionDiagnosis(bahmniDiagnosis,
                    updatedEncounterTransaction.getDiagnoses());
            bahmniDiagnosisHelper.updateDiagnosisMetaData(bahmniDiagnosis, diagnosis, currentEncounter);
        }
        encounterService.saveEncounter(currentEncounter);

        // Void the previous diagnosis if required
        for (BahmniDiagnosisRequest bahmniDiagnosis : bahmniEncounterTransaction.getBahmniDiagnoses()) {
            String previousDiagnosisObs = bahmniDiagnosis.getPreviousObs();
            if (previousDiagnosisObs == null) continue;

            Obs diagnosisObs = obsService.getObsByUuid(previousDiagnosisObs);
            Encounter encounterForDiagnosis = encounterService.getEncounterByUuid(diagnosisObs.getEncounter().getUuid());
            if (!encounterForDiagnosis.equals(currentEncounter)) {
                bahmniDiagnosisHelper.markAsRevised(encounterForDiagnosis, diagnosisObs.getUuid());
                encounterService.saveEncounter(encounterForDiagnosis);
            }
        }

        return new BahmniEncounterTransactionMapper(obsService, encounterTransactionMapper, accessionNotesMapper, encounterTransactionObsMapper).map(updatedEncounterTransaction);
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
