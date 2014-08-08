package org.bahmni.module.admin.csv;

import org.apache.log4j.Logger;
import org.bahmni.csv.EntityPersister;
import org.bahmni.csv.RowResult;
import org.bahmni.module.admin.csv.models.EncounterRow;
import org.openmrs.Concept;
import org.openmrs.Patient;
import org.openmrs.PatientIdentifierType;
import org.openmrs.api.ConceptService;
import org.openmrs.api.PatientService;
import org.openmrs.api.VisitService;
import org.openmrs.module.bahmniemrapi.diagnosis.contract.BahmniDiagnosisRequest;
import org.openmrs.module.bahmniemrapi.encountertransaction.contract.BahmniEncounterTransaction;
import org.openmrs.module.bahmniemrapi.encountertransaction.service.BahmniEncounterTransactionService;
import org.openmrs.module.emrapi.EmrApiConstants;
import org.openmrs.module.emrapi.encounter.domain.EncounterTransaction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class EncounterPersister implements EntityPersister<EncounterRow> {
    private static final Logger log = Logger.getLogger(EncounterPersister.class);

    @Autowired
    private PatientService patientService;

    @Autowired
    private BahmniEncounterTransactionService bahmniEncounterTransactionService;

    @Autowired
    private ConceptService conceptService;

    private HashMap<String, EncounterTransaction.Concept> cachedConcepts = new HashMap<>();

    @Override
    public RowResult<EncounterRow> validate(EncounterRow encounterRow) {
        return new RowResult<>(encounterRow);
    }

    public EncounterPersister() {
    }

    @Override
    public RowResult<EncounterRow> persist(EncounterRow encounterRow) {
        String patientIdentifier = encounterRow.patientIdentifier;
        try {
            List<Patient> matchingPatients = patientService.getPatients(null, patientIdentifier, new ArrayList<PatientIdentifierType>(), true);
            if (matchingPatients.size() > 1)
                return new RowResult<>(encounterRow, String.format("More than 1 matching patients found for identifier:'%s'", patientIdentifier));

            if (matchingPatients.isEmpty())
                return new RowResult<>(encounterRow, String.format("No matching patients found for identifier:'%s'", patientIdentifier));

            Patient patient = matchingPatients.get(0);

            bahmniEncounterTransactionService.save(getBahmniEncounterTransaction(encounterRow, patient));

            return new RowResult<>(encounterRow);
        } catch (Exception e) {
            log.error(e);
            return new RowResult<>(encounterRow, e);
        }
    }

    private BahmniEncounterTransaction getBahmniEncounterTransaction(EncounterRow encounterRow, Patient patient) {
        BahmniEncounterTransaction bahmniEncounterTransaction = new BahmniEncounterTransaction();
        bahmniEncounterTransaction.setBahmniDiagnoses(getBahmniDiagnosis(encounterRow.getDiagnoses()));
        //bahmniEncounterTransaction.setProviders();
        bahmniEncounterTransaction.setPatientUuid(patient.getUuid());
        //bahmniEncounterTransaction.setEncounterTypeUuid(patient.getUuid());
        return bahmniEncounterTransaction;
    }

    private List<BahmniDiagnosisRequest> getBahmniDiagnosis(List<String> diagnoses) {
        List<BahmniDiagnosisRequest> bahmniDiagnoses = new ArrayList<>();
        for (String diagnosis : diagnoses) {
            EncounterTransaction.Concept diagnosisConcept = getDiagnosisConcept(diagnosis);
            BahmniDiagnosisRequest bahmniDiagnosisRequest = new BahmniDiagnosisRequest();
            bahmniDiagnosisRequest.setCodedAnswer(diagnosisConcept);
            bahmniDiagnosisRequest.setOrder(EmrApiConstants.CONCEPT_CODE_DIAGNOSIS_ORDER_PRIMARY);
            bahmniDiagnosisRequest.setCertainty(EmrApiConstants.CONCEPT_CODE_DIAGNOSIS_CERTAINTY_CONFIRMED);
            bahmniDiagnoses.add(bahmniDiagnosisRequest);
        }
        return bahmniDiagnoses;
    }

    private EncounterTransaction.Concept getDiagnosisConcept(String diagnosis) {
        if(cachedConcepts.get(diagnosis) == null) {
            Concept diagnosisConcept = conceptService.getConceptByName(diagnosis);
            cachedConcepts.put(diagnosis, getEncounterTransactionConcept(diagnosisConcept));
        }
        return cachedConcepts.get(diagnosis);
    }


    private EncounterTransaction.Concept getEncounterTransactionConcept(Concept diagnosisConcept) {
        EncounterTransaction.Concept concept = new EncounterTransaction.Concept();
        concept.setUuid(diagnosisConcept.getUuid());
        return concept;
    }
}
