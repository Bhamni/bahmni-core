package org.bahmni.module.admin.encounter;

import org.bahmni.module.admin.csv.models.EncounterRow;
import org.bahmni.module.admin.csv.models.MultipleEncounterRow;
import org.bahmni.module.admin.observation.DiagnosisMapper;
import org.bahmni.module.admin.observation.ObservationMapper;
import org.openmrs.EncounterType;
import org.openmrs.Patient;
import org.openmrs.api.EncounterService;
import org.openmrs.module.bahmniemrapi.diagnosis.contract.BahmniDiagnosisRequest;
import org.openmrs.module.bahmniemrapi.encountertransaction.contract.BahmniEncounterTransaction;
import org.openmrs.module.bahmniemrapi.encountertransaction.mapper.ETObsToBahmniObsMapper;
import org.openmrs.module.bahmniemrapi.encountertransaction.mapper.parameters.AdditionalBahmniObservationFields;
import org.openmrs.module.emrapi.encounter.domain.EncounterTransaction;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class BahmniEncounterTransactionImportService {

    private EncounterService encounterService;
    private ObservationMapper observationMapper;
    private DiagnosisMapper diagnosisMapper;
    private ETObsToBahmniObsMapper fromETObsToBahmniObs;

    @Autowired
    public BahmniEncounterTransactionImportService(EncounterService encounterService,
                                                   ObservationMapper observationMapper, DiagnosisMapper diagnosisMapper,
                                                   ETObsToBahmniObsMapper fromETObsToBahmniObs) {
        this.encounterService = encounterService;
        this.observationMapper = observationMapper;
        this.diagnosisMapper = diagnosisMapper;
        this.fromETObsToBahmniObs = fromETObsToBahmniObs;
    }

    public List<BahmniEncounterTransaction> getBahmniEncounterTransaction(MultipleEncounterRow multipleEncounterRow, Patient patient) throws ParseException {
        if (multipleEncounterRow.encounterRows == null || multipleEncounterRow.encounterRows.isEmpty())
            return new ArrayList<>();

        List<BahmniEncounterTransaction> bahmniEncounterTransactions = new ArrayList<>();

        EncounterType requestedEncounterType = encounterService.getEncounterType(multipleEncounterRow.encounterType);
        if (requestedEncounterType == null) {
            throw new RuntimeException("Encounter type:'" + multipleEncounterRow.encounterType + "' not found.");
        }
        String encounterType = multipleEncounterRow.encounterType;
        String visitType = multipleEncounterRow.visitType;

        for (EncounterRow encounterRow : multipleEncounterRow.getNonEmptyEncounterRows()) {
            List<EncounterTransaction.Observation> allObservations = observationMapper.getObservations(encounterRow);
            List<BahmniDiagnosisRequest> allDiagnosis = diagnosisMapper.getBahmniDiagnosis(encounterRow);

            BahmniEncounterTransaction bahmniEncounterTransaction = new BahmniEncounterTransaction();
            bahmniEncounterTransaction.setPatientUuid(patient.getUuid());
            bahmniEncounterTransaction.setBahmniDiagnoses(allDiagnosis);
            bahmniEncounterTransaction.setObservations(fromETObsToBahmniObs.create(allObservations, new AdditionalBahmniObservationFields(null, encounterRow.getEncounterDate(), null, null)));

            bahmniEncounterTransaction.setEncounterDateTime(encounterRow.getEncounterDate());
            bahmniEncounterTransaction.setEncounterType(encounterType);
            bahmniEncounterTransaction.setVisitType(visitType);

            bahmniEncounterTransactions.add(bahmniEncounterTransaction);
        }


        return bahmniEncounterTransactions;
    }

}
