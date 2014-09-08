package org.bahmni.module.admin.mapper;

import org.bahmni.csv.KeyValue;
import org.bahmni.module.admin.csv.models.EncounterRow;
import org.openmrs.api.ConceptService;
import org.openmrs.module.bahmniemrapi.diagnosis.contract.BahmniDiagnosisRequest;
import org.openmrs.module.emrapi.diagnosis.Diagnosis;
import org.openmrs.module.emrapi.encounter.domain.EncounterTransaction;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class DiagnosisMapper extends ObservationMapper {

    public DiagnosisMapper(ConceptService conceptService) {
        super(conceptService);
    }

    public List<BahmniDiagnosisRequest> getBahmniDiagnosis(EncounterRow multipleEncounterRow) throws ParseException {
        List<BahmniDiagnosisRequest> bahmniDiagnoses = new ArrayList<>();
        if (multipleEncounterRow.hasDiagnoses()) {
            Date encounterDate = multipleEncounterRow.getEncounterDate();
            for (KeyValue uniqueDiagnosisKeyValue : multipleEncounterRow.diagnosesRows) {
                BahmniDiagnosisRequest bahmniDiagnosisRequest = createDiagnosis(encounterDate, uniqueDiagnosisKeyValue.getValue());
                bahmniDiagnoses.add(bahmniDiagnosisRequest);
            }
        }
        return bahmniDiagnoses;
    }

    private BahmniDiagnosisRequest createDiagnosis(Date encounterDate, String diagnosis) throws ParseException {
        EncounterTransaction.Concept diagnosisConcept = getConcept(diagnosis);

        BahmniDiagnosisRequest bahmniDiagnosisRequest = new BahmniDiagnosisRequest();
        bahmniDiagnosisRequest.setCodedAnswer(diagnosisConcept);
        bahmniDiagnosisRequest.setOrder(String.valueOf(Diagnosis.Order.PRIMARY));
        bahmniDiagnosisRequest.setCertainty(String.valueOf(Diagnosis.Certainty.CONFIRMED));
        bahmniDiagnosisRequest.setDiagnosisDateTime(encounterDate);
        return bahmniDiagnosisRequest;
    }

}