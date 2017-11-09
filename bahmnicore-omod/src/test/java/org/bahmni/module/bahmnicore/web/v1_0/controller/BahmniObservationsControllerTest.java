package org.bahmni.module.bahmnicore.web.v1_0.controller;

import org.bahmni.module.bahmnicore.extensions.BahmniExtensions;
import org.bahmni.module.bahmnicore.service.BahmniConceptService;
import org.bahmni.module.bahmnicore.service.BahmniObsService;
import org.bahmni.module.bahmnicore.web.v1_0.controller.display.controls.BahmniObservationsController;
import org.bahmni.test.builder.VisitBuilder;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.openmrs.Concept;
import org.openmrs.ConceptName;
import org.openmrs.Visit;
import org.openmrs.api.VisitService;
import org.openmrs.module.bahmniemrapi.builder.BahmniObservationBuilder;
import org.openmrs.module.bahmniemrapi.encountertransaction.contract.BahmniObservation;
import org.openmrs.module.emrapi.encounter.domain.EncounterTransaction;

import java.text.ParseException;
import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.*;


public class BahmniObservationsControllerTest {

    @Mock
    private BahmniObsService bahmniObsService;
    @Mock
    private BahmniConceptService bahmniConceptService;
    @Mock
    private VisitService visitService;
    @Mock
    private BahmniExtensions bahmniExtensions;

    @Mock
    private Concept openmrsConcept;

    private Visit visit;
    private Concept concept;
    private BahmniObservationsController bahmniObservationsController;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        visit = new VisitBuilder().build();
        concept = new Concept();
        bahmniObservationsController = new BahmniObservationsController(bahmniObsService, bahmniConceptService, visitService, bahmniExtensions);
        when(visitService.getVisitByUuid("visitId")).thenReturn(visit);
    }

    @Test
    public void returnLatestObservations() throws Exception {
        BahmniObservation latestObs = new BahmniObservation();
        latestObs.setUuid("initialId");
        when(bahmniConceptService.getConceptsByFullySpecifiedName(Arrays.asList("Weight"))).thenReturn(Arrays.asList(concept));
        when(bahmniObsService.getLatestObsByVisit(visit, Arrays.asList(concept), null, true)).thenReturn(Arrays.asList(latestObs));

        Collection<BahmniObservation> bahmniObservations = bahmniObservationsController.get("visitId", "latest", Arrays.asList("Weight"), null, true);

        verify(bahmniObsService, never()).getInitialObsByVisit(visit, Arrays.asList(concept), null, false);
        assertEquals(1, bahmniObservations.size());
    }

    @Test
    public void returnInitialObservation() throws Exception {
        EncounterTransaction.Concept cpt = new EncounterTransaction.Concept();
        cpt.setShortName("Concept1");

        BahmniObservation initialObs = new BahmniObservation();
        initialObs.setUuid("initialId");
        initialObs.setConcept(cpt);

        when(bahmniConceptService.getConceptsByFullySpecifiedName(Arrays.asList("Weight"))).thenReturn(Arrays.asList(concept));
        when(bahmniObsService.getInitialObsByVisit(visit, Arrays.asList(this.concept), null, true)).thenReturn(Arrays.asList(initialObs));

        Collection<BahmniObservation> bahmniObservations = bahmniObservationsController.get("visitId", "initial", Arrays.asList("Weight"), null, true);

        assertEquals(1, bahmniObservations.size());
    }

    @Test
    public void returnAllObservations() throws Exception {
        BahmniObservation obs = new BahmniObservation();
        List<String> conceptNames = Arrays.asList("Weight");
        ArrayList<Concept> obsIgnoreList = new ArrayList<>();
        when(bahmniObsService.getObservationForVisit("visitId", conceptNames, obsIgnoreList, true, null)).thenReturn(Arrays.asList(obs));

        Collection<BahmniObservation> bahmniObservations = bahmniObservationsController.get("visitId", null, conceptNames, null, true);

        verify(bahmniObsService, never()).getLatestObsByVisit(visit, Arrays.asList(concept), null, false);
        verify(bahmniObsService, never()).getInitialObsByVisit(visit, Arrays.asList(concept), null, false);
        verify(bahmniObsService, times(1)).getObservationForVisit("visitId", conceptNames, obsIgnoreList, true, null);

        assertEquals(1, bahmniObservations.size());
    }

    @Test
    public void shouldMakeACallToGetObsForEncounterAndConceptsSpecified() throws Exception {
        ArrayList<String> conceptNames = new ArrayList<>();
        String encounterUuid = "encounterUuid";
        String obsUuid = "ObsUuid";
        ArrayList<BahmniObservation> bahmniObservations = new ArrayList<>();
        bahmniObservations.add(new BahmniObservationBuilder().withUuid(obsUuid).build());
        when(bahmniObsService.getObservationsForEncounter(encounterUuid, conceptNames)).thenReturn(bahmniObservations);

        Collection<BahmniObservation> actualResult = bahmniObservationsController.get(encounterUuid, conceptNames);

        verify(bahmniObsService, times(1)).getObservationsForEncounter(encounterUuid, conceptNames);
        assertEquals(1, actualResult.size());
        assertEquals(obsUuid, actualResult.iterator().next().getUuid());
    }

    @Test
    public void shouldGetObsForPatientProgramWhenPatientProgramUuidIsSpecified() throws Exception {
        String patientProgramUuid = "patientProgramUuid";
        List<String> conceptNames = Arrays.asList("Weight");
        when(bahmniExtensions.getExtension("observationsAdder","CurrentMonthOfTreatment.groovy")).thenReturn(null);

        bahmniObservationsController.get(patientProgramUuid, conceptNames, null, null);

        verify(bahmniObsService, times(1)).getObservationsForPatientProgram(patientProgramUuid, conceptNames);
    }

    @Test
    public void shouldNotGetObsForPatientProgramWhenPatientProgramUuidIsSpecified() throws Exception {
        List<String> conceptNames = new ArrayList<String>();
        String patientProgramUuid = null;
        when(bahmniExtensions.getExtension("observationsAdder","CurrentMonthOfTreatment.groovy")).thenReturn(null);

        bahmniObservationsController.get(patientProgramUuid, null, null, null);

        verify(bahmniObsService, times(0)).getObservationsForPatientProgram(patientProgramUuid, conceptNames);
    }

    @Test
    public void shouldGetLatestObsForPatientProgramWhenPatientProgramUuidAndScopeLatestIsSpecified() throws Exception {
        List<String> conceptNames = new ArrayList<String>();
        String patientProgramUuid = "patientProgramUuid";
        String scope = "latest";

        bahmniObservationsController.get(patientProgramUuid, conceptNames, scope, null);

        verify(bahmniObsService, times(1)).getLatestObservationsForPatientProgram(patientProgramUuid, conceptNames);
    }

    @Test
    public void shouldGetInitialObsForPatientProgramWhenPatientProgramUuidAndScopeLatestIsSpecified() throws Exception {
        List<String> conceptNames = new ArrayList<String>();
        String patientProgramUuid = "patientProgramUuid";
        String scope = "initial";
        when(bahmniExtensions.getExtension("observationsAdder","CurrentMonthOfTreatment.groovy")).thenReturn(null);

        bahmniObservationsController.get(patientProgramUuid, conceptNames, scope, null);

        verify(bahmniObsService, times(1)).getInitialObservationsForPatientProgram(patientProgramUuid, conceptNames);
    }

    @Test
    public void shouldGetBahmniObservationWithTheGivenObservationUuid() throws Exception {
        String observationUuid = "observationUuid";
        BahmniObservation expectedBahmniObservation = new BahmniObservation();
        when(bahmniObsService.getBahmniObservationByUuid(observationUuid)).thenReturn(expectedBahmniObservation);

        BahmniObservation actualBahmniObservation = bahmniObservationsController.get(observationUuid, "");

        verify(bahmniObsService, times(1)).getBahmniObservationByUuid("observationUuid");
        assertNotNull("BahmniObservation should not be null", actualBahmniObservation);
        assertEquals(expectedBahmniObservation, actualBahmniObservation);
    }

    @Test
    public void shouldFilterObservationsByTheObsSelectList() throws Exception {
        BahmniObservation systolicObs = new BahmniObservation();
        EncounterTransaction.Concept systolicConcept = new EncounterTransaction.Concept("uuid2", "Systolic", false);
        systolicObs.setConceptSortWeight(0);
        systolicObs.setConcept(systolicConcept);

        BahmniObservation diastolicObs = new BahmniObservation();
        EncounterTransaction.Concept diastolicConcept = new EncounterTransaction.Concept("uuid4", "Diastolic", false);
        diastolicObs.setConcept(diastolicConcept);

        BahmniObservation diastolicDataObs = new BahmniObservation();
        EncounterTransaction.Concept diastolicDataConcept = new EncounterTransaction.Concept("uuid3", "Diastolic Data", false);
        diastolicDataObs.setConcept(diastolicDataConcept);
        diastolicDataObs.setConceptSortWeight(1);
        diastolicDataObs.addGroupMember(diastolicObs);

        BahmniObservation bloodPressureObs = new BahmniObservation();
        EncounterTransaction.Concept bloodPressureConcept = new EncounterTransaction.Concept("uuid1", "bloodPressure", true);
        bloodPressureObs.setConcept(bloodPressureConcept);


        List<BahmniObservation> groupMembers = new ArrayList<>();
        groupMembers.add(diastolicDataObs);
        groupMembers.add(systolicObs);
        bloodPressureObs.setGroupMembers(groupMembers);

        Collection<Concept> concepts = new ArrayList<>();
        concepts.add(openmrsConcept);
        String patientUuid = "patientUuid";
        List<String> obsSelectList = new ArrayList<>();
        obsSelectList.add("Systolic");
        obsSelectList.add("Diastolic");
        when(openmrsConcept.getName()).thenReturn(new ConceptName("bloodPressure", Locale.ENGLISH));
        when(bahmniConceptService.getConceptsByFullySpecifiedName(Arrays.asList("bloodPressure"))).thenReturn(Arrays.asList(openmrsConcept));
        when(bahmniObsService.getLatest(patientUuid,concepts,1,null ,false, null)).thenReturn(Collections.singletonList(bloodPressureObs));

        String scope = "latest";

        Collection<BahmniObservation> selectedObservations = bahmniObservationsController.get(patientUuid, Arrays.asList("bloodPressure"), scope, 1, null, obsSelectList, false);
        assertEquals(2,selectedObservations.size());

        Iterator<BahmniObservation> bahmniObservationIterator = selectedObservations.iterator();
        assertEquals(systolicObs, bahmniObservationIterator.next());
        assertEquals(diastolicObs,bahmniObservationIterator.next());
    }

    @Test
    public void shouldFilterObservationsInAPatientProgramByObsSelectList() throws ParseException {
        BahmniObservation systolicObs = new BahmniObservation();
        EncounterTransaction.Concept systolicConcept = new EncounterTransaction.Concept("uuid2", "Systolic", false);
        systolicObs.setConcept(systolicConcept);
        systolicObs.setConceptSortWeight(0);

        BahmniObservation diastolicObs = new BahmniObservation();
        EncounterTransaction.Concept diastolicConcept = new EncounterTransaction.Concept("uuid4", "Diastolic", false);
        diastolicObs.setConcept(diastolicConcept);

        BahmniObservation diastolicDataObs = new BahmniObservation();
        EncounterTransaction.Concept diastolicDataConcept = new EncounterTransaction.Concept("uuid3", "Diastolic Data", false);
        diastolicDataObs.setConcept(diastolicDataConcept);
        diastolicDataObs.setConceptSortWeight(1);
        diastolicDataObs.addGroupMember(diastolicObs);

        BahmniObservation bloodPressureObs = new BahmniObservation();
        EncounterTransaction.Concept bloodPressureConcept = new EncounterTransaction.Concept("uuid1", "bloodPressure", true);
        bloodPressureObs.setConcept(bloodPressureConcept);

        List<BahmniObservation> groupMembers = new ArrayList<>();
        groupMembers.add(diastolicDataObs);
        groupMembers.add(systolicObs);
        bloodPressureObs.setGroupMembers(groupMembers);

        Collection<Concept> concepts = new ArrayList<>();
        concepts.add(openmrsConcept);
        List<String> obsSelectList = new ArrayList<>();
        obsSelectList.add("Systolic");
        obsSelectList.add("Diastolic");

        List<String> conceptNames = new ArrayList<String>();
        conceptNames.add("bloodPressure");
        String patientProgramUuid = "patientProgramUuid";
        String scope = "latest";

        when(openmrsConcept.getName()).thenReturn(new ConceptName("bloodPressure", Locale.ENGLISH));
        when(bahmniConceptService.getConceptsByFullySpecifiedName(Arrays.asList("bloodPressure"))).thenReturn(Arrays.asList(openmrsConcept));
        when(bahmniExtensions.getExtension("observationsAdder","CurrentMonthOfTreatment.groovy")).thenReturn(null);
        when(bahmniObsService.getLatestObservationsForPatientProgram(patientProgramUuid, conceptNames)).thenReturn(Collections.singletonList(bloodPressureObs));

        Collection<BahmniObservation> selectedObservations =  bahmniObservationsController.get(patientProgramUuid, conceptNames, scope, obsSelectList);
        assertEquals(2,selectedObservations.size());

        Iterator<BahmniObservation> bahmniObservationIterator = selectedObservations.iterator();
        assertEquals(systolicObs, bahmniObservationIterator.next());
        assertEquals(diastolicObs,bahmniObservationIterator.next());
    }
}