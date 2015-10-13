package org.bahmni.module.bahmnicore.service.impl;

import org.bahmni.module.bahmnicore.service.BahmniDiagnosisService;
import org.bahmni.test.builder.ConceptBuilder;
import org.bahmni.test.builder.DiagnosisBuilder;
import org.bahmni.test.builder.EncounterBuilder;
import org.bahmni.test.builder.ObsBuilder;
import org.codehaus.groovy.transform.powerassert.SourceText;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.*;
import org.openmrs.*;
import org.openmrs.api.*;
import org.openmrs.api.context.Context;
import org.openmrs.module.bahmniemrapi.diagnosis.contract.BahmniDiagnosis;
import org.openmrs.module.bahmniemrapi.diagnosis.contract.BahmniDiagnosisRequest;
import org.openmrs.module.bahmniemrapi.diagnosis.helper.BahmniDiagnosisMetadata;
import org.openmrs.module.emrapi.EmrApiProperties;
import org.openmrs.module.emrapi.diagnosis.Diagnosis;
import org.openmrs.module.emrapi.diagnosis.DiagnosisMetadata;
import org.openmrs.module.emrapi.encounter.DiagnosisMapper;
import org.openmrs.module.emrapi.encounter.domain.EncounterTransaction;
import org.openmrs.util.LocaleUtility;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;

@PrepareForTest(LocaleUtility.class)
@RunWith(PowerMockRunner.class)
public class BahmniDiagnosisServiceTest {
    @Mock
    private EncounterService encounterService;
    @Mock
    private ObsService obsService;

    @Mock
    private PatientService patientService;

    @Mock
    private BahmniDiagnosisMetadata bahmniDiagnosisMetadata;

    @Mock
    private ConceptService conceptService;

    @InjectMocks
    @Spy
    BahmniDiagnosisServiceImpl bahmniDiagnosisService = new BahmniDiagnosisServiceImpl(encounterService, obsService);

    private String initialDiagnosisObsUUID = "initialDiagnosisObsUUID";
    private String modifiedDiagnosisObsUUID = "modifiedDiagnosisObsUUID";
    private String initialEncounterUUID = "initialEncounterUUID";
    private Obs initialVisitDiagnosesObs;
    private Obs modifiedVisitDiagnosis;
    private Encounter initialEncounter;
    private Encounter modifiedEncounter;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);

        PowerMockito.mockStatic(LocaleUtility.class);
        PowerMockito.when(LocaleUtility.getLocalesInOrder()).thenReturn(new HashSet<>(Arrays.asList(Locale.getDefault())));
    }

    @Test
    public void deleteADiagnosis() throws Exception {
        String diagnosisObsUUID = "diagnosisObsUUID";

        Obs visitDiagnosisObs = new DiagnosisBuilder().withUuid(diagnosisObsUUID).withDefaults().withFirstObs(diagnosisObsUUID).build();
        Set<Obs> allObsForDiagnosisEncounter = new HashSet<>();
        allObsForDiagnosisEncounter.add(new DiagnosisBuilder().withUuid("someOtherDiagnosisUUID").withDefaults().withFirstObs("initialDiagnosisObsUUID").build());
        allObsForDiagnosisEncounter.add(visitDiagnosisObs);
        allObsForDiagnosisEncounter.add(new ObsBuilder().withUUID("nonDiagnosisUuid").withConcept("Some Concept", Locale.getDefault()).build());

        Encounter diagnosisEncounter = new EncounterBuilder().withDatetime(new Date()).build();
        visitDiagnosisObs.setEncounter(diagnosisEncounter);
        diagnosisEncounter.setObs(allObsForDiagnosisEncounter);

        when(obsService.getObsByUuid(diagnosisObsUUID)).thenReturn(visitDiagnosisObs);
        when(obsService.getObservationsByPersonAndConcept(visitDiagnosisObs.getPerson(), visitDiagnosisObs.getConcept())).thenReturn(Arrays.asList(visitDiagnosisObs));
        when(encounterService.saveEncounter(diagnosisEncounter)).thenReturn(diagnosisEncounter);
        when(bahmniDiagnosisMetadata.findInitialDiagnosisUuid(visitDiagnosisObs)).thenReturn(diagnosisObsUUID);

        bahmniDiagnosisService.delete(diagnosisObsUUID);
        ArgumentCaptor<Encounter> argToCapture = ArgumentCaptor.forClass(Encounter.class);
        verify(encounterService).saveEncounter(argToCapture.capture());
        assertVoided(argToCapture.getValue(), diagnosisObsUUID);
    }

    @Test
    public void initialDiagnosisIsDeletedOnDeletingADiagnosis() throws Exception {
        setUpInitialVisitDiagnosis();
        setUpModifiedVisitDiagnosis();

        when(obsService.getObsByUuid(modifiedDiagnosisObsUUID)).thenReturn(modifiedVisitDiagnosis);
        when(obsService.getObservationsByPersonAndConcept(modifiedVisitDiagnosis.getPerson(), modifiedVisitDiagnosis.getConcept())).
                thenReturn(Arrays.asList(modifiedVisitDiagnosis, initialVisitDiagnosesObs));
        when(encounterService.saveEncounter(initialEncounter)).thenReturn(initialEncounter);
        when(encounterService.saveEncounter(modifiedEncounter)).thenReturn(modifiedEncounter);
        when(bahmniDiagnosisMetadata.findInitialDiagnosisUuid(modifiedVisitDiagnosis)).thenReturn(initialDiagnosisObsUUID);

        bahmniDiagnosisService.delete(modifiedDiagnosisObsUUID);

        ArgumentCaptor<Encounter> argToCapture = ArgumentCaptor.forClass(Encounter.class);
        verify(encounterService, times(2)).saveEncounter(argToCapture.capture());

        assertVoided(argToCapture.getAllValues().get(0), modifiedDiagnosisObsUUID);
        assertVoided(argToCapture.getAllValues().get(1), initialDiagnosisObsUUID);
    }

    @Test
    public void otherDiagnosisWithSameInitialDiagnosisIsDeletedOnDeletingADiagnosis() throws Exception {
        setUpInitialVisitDiagnosis();
        setUpModifiedVisitDiagnosis();
        String anotherDiagnosisUuid = "anotherDiagnosisUuid";


        Obs anotherVisitDiagnosis = new DiagnosisBuilder().withUuid(anotherDiagnosisUuid).withDefaults().withFirstObs(initialDiagnosisObsUUID).build();
        Encounter anotherEncounter = new EncounterBuilder().withDatetime(new Date()).withUUID("anotherEncounterUuid").build();
        anotherEncounter.addObs(anotherVisitDiagnosis);
        anotherVisitDiagnosis.setEncounter(anotherEncounter);

        when(obsService.getObsByUuid(modifiedDiagnosisObsUUID)).thenReturn(modifiedVisitDiagnosis);
        when(obsService.getObservationsByPersonAndConcept(modifiedVisitDiagnosis.getPerson(), modifiedVisitDiagnosis.getConcept())).
                thenReturn(Arrays.asList(modifiedVisitDiagnosis, initialVisitDiagnosesObs, anotherVisitDiagnosis));
        when(encounterService.saveEncounter(initialEncounter)).thenReturn(initialEncounter);
        when(encounterService.saveEncounter(modifiedEncounter)).thenReturn(modifiedEncounter);
        when(bahmniDiagnosisMetadata.findInitialDiagnosisUuid(modifiedVisitDiagnosis)).thenReturn(initialDiagnosisObsUUID);

        bahmniDiagnosisService.delete(modifiedDiagnosisObsUUID);

        ArgumentCaptor<Encounter> argToCapture = ArgumentCaptor.forClass(Encounter.class);
        verify(encounterService, times(3)).saveEncounter(argToCapture.capture());

        assertVoided(argToCapture.getAllValues().get(0), modifiedDiagnosisObsUUID);
        assertVoided(argToCapture.getAllValues().get(1), initialDiagnosisObsUUID);
        assertVoided(argToCapture.getAllValues().get(2), anotherDiagnosisUuid);
    }

    @Test
    public void shouldGetBahmniDiagnosisByPatientAndVisit(){

        Patient patient = mock(Patient.class);
        VisitService visitService = mock(VisitService.class);
        Visit visit = mock(Visit.class);
        Concept diagnosisSetConcept = new ConceptBuilder().withUUID("uuid").build();
        Concept bahmniDiagnosisRevised = new ConceptBuilder().withUUID("bahmniDiagnosisRevised").build();
        Diagnosis mockDiagnosis = mock(Diagnosis.class);
        DiagnosisMapper diagnosisMapper = mock(DiagnosisMapper.class);

        when(bahmniDiagnosisMetadata.getDiagnosisSetConcept()).thenReturn(diagnosisSetConcept);

        when(visitService.getVisitByUuid("visitId")).thenReturn(visit);
        when(visit.getEncounters()).thenReturn(new HashSet<Encounter>());
        when(patientService.getPatientByUuid("patientId")).thenReturn(patient);

        Obs diagnosisObs = new DiagnosisBuilder()
                .withDefaults()
                .withFirstObs("firstDiagnosisObsId")
                .withUuid("firstDiagnosisObsId")
                .build();


        when(obsService.getObservations(eq(Arrays.asList((Person) patient)), anyList(), eq(Arrays.asList(diagnosisSetConcept)), anyListOf(Concept.class), anyList(), anyList(), anyList(),
                anyInt(), anyInt(), Matchers.any(java.util.Date.class), Matchers.any(java.util.Date.class), eq(false)))
                .thenReturn(Arrays.asList(diagnosisObs));
        when(bahmniDiagnosisMetadata.buildDiagnosisFromObsGroup(diagnosisObs)).thenReturn(mockDiagnosis);
        EncounterTransaction.Diagnosis etDiagnosis = mock(EncounterTransaction.Diagnosis.class);

        Diagnosis latestDiagnosis = mock(Diagnosis.class);
        EncounterTransaction.Diagnosis etLatestDiagnosis = mock(EncounterTransaction.Diagnosis.class);
        when(diagnosisMapper.convert(mockDiagnosis)).thenReturn(etDiagnosis);

//        when(obsService.getObservations(eq(Arrays.asList((Person) patient)), anyList(), eq(Arrays.asList(bahmniDiagnosisRevised)), anyListOf(Concept.class), anyList(), anyList(), anyList(),
//                anyInt(), anyInt(), Matchers.any(java.util.Date.class), Matchers.any(java.util.Date.class), eq(false)))
//                .thenReturn(Arrays.asList(diagnosisObs));
//        when(bahmniDiagnosisMetadata.getBahmniDiagnosisRevised()).thenReturn(bahmniDiagnosisRevised);
//        when(conceptService.getFalseConcept()).thenReturn(new Concept());
//
//        String obsUuid = "ObsUuid";
//        Obs obs = new ObsBuilder().withUUID(obsUuid).withGroupMembers().withPerson(new Person()).build();
//        when(mockDiagnosis.getExistingObs()).thenReturn(obs);
//        when(bahmniDiagnosisMetadata.findInitialDiagnosisUuid(obs)).thenReturn(obsUuid);
//        when(bahmniDiagnosisMetadata.findInitialDiagnosis(obs)).thenReturn(diagnosisObs);
        doReturn(diagnosisObs.getObsGroup()).when(bahmniDiagnosisService).getLatestObsGroupBasedOnAnyDiagnosis(mockDiagnosis);
        when(bahmniDiagnosisMetadata.buildDiagnosisFromObsGroup(diagnosisObs.getObsGroup())).thenReturn(latestDiagnosis);
        when(diagnosisMapper.convert(latestDiagnosis)).thenReturn(etLatestDiagnosis);

        BahmniDiagnosisRequest bahmniDiagnosisRequest = new BahmniDiagnosisRequest();
        BahmniDiagnosis bahmniDiagnosis = new BahmniDiagnosis();
        bahmniDiagnosis.setExistingObs("existing");
        bahmniDiagnosisRequest.setFirstDiagnosis(bahmniDiagnosis);
        when(bahmniDiagnosisMetadata.mapBahmniDiagnosis(etDiagnosis, etLatestDiagnosis, true, false)).thenReturn(bahmniDiagnosisRequest);
        bahmniDiagnosisService.setPatientService(patientService);
        bahmniDiagnosisService.setVisitService(visitService);
        bahmniDiagnosisService.setBahmniDiagnosisMetadata(bahmniDiagnosisMetadata);
        bahmniDiagnosisService.setDiagnosisMapper(diagnosisMapper);

        List<BahmniDiagnosisRequest> bahmniDiagnosisRequests = bahmniDiagnosisService.getBahmniDiagnosisByPatientAndVisit("patientId", "visitId");
        assertEquals(1,bahmniDiagnosisRequests.size());
        assertEquals(bahmniDiagnosisRequest,bahmniDiagnosisRequests.get(0));
    }

    @Test
    public void shouldGetLatestDiagnosisBasedOnCurrentDiagnosis(){
        Obs diagnosisObs = new DiagnosisBuilder()
                .withDefaults()
                .withFirstObs("firstDiagnosisObsId")
                .withUuid("firstDiagnosisObsId")
                .build();

        Obs updatedDiagnosisObs = new DiagnosisBuilder()
                .withDefaults()
                .withFirstObs("firstDiagnosisObsId")
                .withUuid("finalDiagnosisUuid")
                .build();

        Obs bahmniDiagnosisRevised = new ObsBuilder().withConcept("Bahmni Diagnosis Revised",Locale.getDefault()).withValue("false").build();
        bahmniDiagnosisRevised.setObsGroup(updatedDiagnosisObs);

        when(obsService.getObservations(anyListOf(Person.class), anyList(),anyListOf(Concept.class),anyListOf(Concept.class), anyList(), anyList(), anyList(),
                anyInt(), anyInt(),  Matchers.any(java.util.Date.class), Matchers.any(java.util.Date.class), eq(false)))
                .thenReturn(Arrays.asList(bahmniDiagnosisRevised));

        when(bahmniDiagnosisMetadata.findInitialDiagnosisUuid(diagnosisObs)).thenReturn("firstDiagnosisObsId");
        diagnosisObs.setValueText("firstDiagnosisObsId");
        when(bahmniDiagnosisMetadata.findInitialDiagnosis(updatedDiagnosisObs)).thenReturn(diagnosisObs);

        Diagnosis diagnosis = new Diagnosis();
        diagnosis.setExistingObs(diagnosisObs);

        Obs actualDiagnosisObs = bahmniDiagnosisService.getLatestObsGroupBasedOnAnyDiagnosis(diagnosis);

        Assert.assertEquals(updatedDiagnosisObs, actualDiagnosisObs);
    }


    private void setUpModifiedVisitDiagnosis() {
        modifiedVisitDiagnosis = new DiagnosisBuilder().withUuid(modifiedDiagnosisObsUUID).withDefaults().withFirstObs(initialDiagnosisObsUUID).build();
        modifiedEncounter = new EncounterBuilder().withDatetime(new Date()).withUUID("modifiedEncounterUUID").build();
        modifiedEncounter.addObs(modifiedVisitDiagnosis);
        modifiedVisitDiagnosis.setEncounter(modifiedEncounter);
    }

    private void setUpInitialVisitDiagnosis() {
        initialVisitDiagnosesObs = new DiagnosisBuilder().withUuid(initialDiagnosisObsUUID).withDefaults().withFirstObs(initialDiagnosisObsUUID).build();
        initialEncounter = new EncounterBuilder().withDatetime(new Date()).withUUID(initialEncounterUUID).build();
        initialEncounter.addObs(initialVisitDiagnosesObs);
        initialVisitDiagnosesObs.setEncounter(initialEncounter);
    }


    private void assertVoided(Encounter encounter, String observationUuid) {
        Obs visitDiagnosesObsToSave = getAllObsFor(encounter, observationUuid);
        assertTrue("Parent Diagnosis Obs should be voided", visitDiagnosesObsToSave.isVoided());
        for (Obs childObs : visitDiagnosesObsToSave.getGroupMembers(true)) {
            assertTrue("Child Diagnosis Obs should be voided", childObs.isVoided());
        }
    }

    private Obs getAllObsFor(Encounter encounterToSave, String visitDiagnosisUuid) {
        Set<Obs> allObs = encounterToSave.getAllObs(true);
        for (Obs anObs : allObs) {
            if (anObs.getUuid().equals(visitDiagnosisUuid))
                return anObs;
        }
        return null;
    }
}