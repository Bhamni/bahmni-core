package org.openmrs.module.bahmniemrapi.encountertransaction.command.impl;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.openmrs.Concept;
import org.openmrs.ConceptDatatype;
import org.openmrs.Encounter;
import org.openmrs.Obs;
import org.openmrs.api.ConceptService;
import org.openmrs.api.EncounterService;
import org.openmrs.api.ObsService;
import org.openmrs.module.bahmniemrapi.BahmniEmrAPIException;
import org.openmrs.module.bahmniemrapi.diagnosis.contract.BahmniDiagnosis;
import org.openmrs.module.bahmniemrapi.diagnosis.contract.BahmniDiagnosisRequest;
import org.openmrs.module.bahmniemrapi.diagnosis.helper.BahmniDiagnosisMetadata;
import org.openmrs.module.bahmniemrapi.encountertransaction.contract.BahmniEncounterTransaction;
import org.openmrs.module.emrapi.encounter.domain.EncounterTransaction;

import java.util.Arrays;
import java.util.HashSet;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

public class BahmniDiagnosisSaveCommandImplTest {

    @Mock
    private ObsService obsService;

    @Mock
    private ConceptService conceptService;

    @Mock
    private EncounterService encounterService;

    @Mock
    private BahmniDiagnosisMetadata bahmniDiagnosisMetadata;
    private BahmniDiagnosisSaveCommandImpl bahmniDiagnosisSaveCommand;

    @Before
    public void before() {
        initMocks(this);
        bahmniDiagnosisSaveCommand = new BahmniDiagnosisSaveCommandImpl(obsService, encounterService, bahmniDiagnosisMetadata);

    }

    @Test
    public void shouldSaveWithExistingObs () {
        BahmniEncounterTransaction bahmniEncounterTransaction = new BahmniEncounterTransaction();
        BahmniDiagnosisRequest bahmniDiagnosisRequest = new BahmniDiagnosisRequest();
        bahmniDiagnosisRequest.setEncounterUuid("encounterUuid");
        bahmniDiagnosisRequest.setExistingObs("existingUuid");
        bahmniDiagnosisRequest.setFirstDiagnosis(new BahmniDiagnosis());
        bahmniEncounterTransaction.setBahmniDiagnoses(Arrays.asList(bahmniDiagnosisRequest));
        EncounterTransaction updatedEncounterTransaction = new EncounterTransaction("visitUUid", "encounterUuid");
        updatedEncounterTransaction.setDiagnoses(Arrays.asList(new EncounterTransaction.Diagnosis().setExistingObs("existingUuid")));

        Encounter currentEncounter = setUpData();
        EncounterTransaction transaction = bahmniDiagnosisSaveCommand.save(bahmniEncounterTransaction, currentEncounter, updatedEncounterTransaction);

        verify(encounterService).saveEncounter(currentEncounter);
        assertEquals(transaction, updatedEncounterTransaction);
    }

    @Test
    public void shouldSaveNewObs () {
        BahmniEncounterTransaction bahmniEncounterTransaction = new BahmniEncounterTransaction();
        BahmniDiagnosisRequest bahmniDiagnosisRequest = new BahmniDiagnosisRequest();
        bahmniDiagnosisRequest.setEncounterUuid("encounterUuid");
        bahmniDiagnosisRequest.setFreeTextAnswer("new Obs");
        bahmniDiagnosisRequest.setFirstDiagnosis(new BahmniDiagnosis());
        bahmniEncounterTransaction.setBahmniDiagnoses(Arrays.asList(bahmniDiagnosisRequest));

        EncounterTransaction updatedEncounterTransaction = new EncounterTransaction("visitUUid", "encounterUuid");
        updatedEncounterTransaction.setDiagnoses(Arrays.asList(new EncounterTransaction.Diagnosis().setFreeTextAnswer("new Obs").setExistingObs("existingUuid")));

        Encounter currentEncounter = setUpData();
        EncounterTransaction transaction = bahmniDiagnosisSaveCommand.save(bahmniEncounterTransaction, currentEncounter, updatedEncounterTransaction);
        verify(encounterService).saveEncounter(currentEncounter);
        assertEquals(transaction, updatedEncounterTransaction);

    }

    @Test(expected = BahmniEmrAPIException.class)
    public void shouldThrowErrorForNotFindingAMatchingObservation () {
        BahmniEncounterTransaction bahmniEncounterTransaction = new BahmniEncounterTransaction();
        BahmniDiagnosisRequest bahmniDiagnosisRequest = new BahmniDiagnosisRequest();
        bahmniDiagnosisRequest.setEncounterUuid("encounterUuid");
        bahmniDiagnosisRequest.setCodedAnswer(new EncounterTransaction.Concept("conceptId", "conceptname"));
        bahmniDiagnosisRequest.setFirstDiagnosis(new BahmniDiagnosis());
        bahmniEncounterTransaction.setBahmniDiagnoses(Arrays.asList(bahmniDiagnosisRequest));

        EncounterTransaction updatedEncounterTransaction = new EncounterTransaction("visitUUid", "encounterUuid");
        updatedEncounterTransaction.setDiagnoses(Arrays.asList(new EncounterTransaction.Diagnosis().setFreeTextAnswer("different Obs").setExistingObs("existingUuid")));

        Encounter currentEncounter = setUpData();
        EncounterTransaction transaction = bahmniDiagnosisSaveCommand.save(bahmniEncounterTransaction, currentEncounter, updatedEncounterTransaction);

    }

    private Encounter setUpData() {
        Encounter currentEncounter = new Encounter();

        Concept concept = new Concept();
        concept.setConceptId(123);
        ConceptDatatype conceptDatatype = new ConceptDatatype();
        conceptDatatype.setUuid("ConceptUuid");
        concept.setDatatype(conceptDatatype);
        Obs obs2 = new Obs();
        obs2.setObsId(2);
        obs2.setConcept(concept);

        when(conceptService.getConceptByName(anyString())).thenReturn(concept);
        when(obsService.getObsByUuid(anyString())).thenReturn(obs2);

        Obs obs1 = new Obs();
        obs1.setObsId(1);
        obs1.setUuid("existingUuid");
        HashSet<Obs> groupMembers = new HashSet<Obs>();
        groupMembers.add(obs2);
        obs1.setGroupMembers(groupMembers);

        HashSet<Obs> obs = new HashSet<Obs>();
        obs.add(obs1);
        currentEncounter.setObs(obs);
        return currentEncounter;
    }

}