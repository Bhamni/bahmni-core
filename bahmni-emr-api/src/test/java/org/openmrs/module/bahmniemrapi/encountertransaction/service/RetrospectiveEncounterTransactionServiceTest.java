package org.openmrs.module.bahmniemrapi.encountertransaction.service;

import org.bahmni.test.builder.VisitBuilder;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.openmrs.Patient;
import org.openmrs.Visit;
import org.openmrs.VisitType;
import org.openmrs.module.bahmniemrapi.diagnosis.contract.BahmniDiagnosisRequest;
import org.openmrs.module.bahmniemrapi.encountertransaction.contract.BahmniEncounterTransaction;
import org.openmrs.module.bahmniemrapi.encountertransaction.contract.BahmniObservation;
import org.openmrs.module.emrapi.encounter.domain.EncounterTransaction;

import java.util.Date;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

public class RetrospectiveEncounterTransactionServiceTest {

    @Mock
    private VisitIdentificationHelper mockVisitIdentificationHelper;

    @Before
    public void setUp(){
        initMocks(this);
    }

    @Test
    public void do_not_update_the_encounter_if_it_is_freshly_created(){
        Patient patient = new Patient();
        BahmniEncounterTransaction bahmniEncounterTransaction = new BahmniEncounterTransaction();

        when(mockVisitIdentificationHelper.getVisitFor(patient, null, null, null, null)).thenReturn(null);

        RetrospectiveEncounterTransactionService retrospectiveService = new RetrospectiveEncounterTransactionService(mockVisitIdentificationHelper);
        BahmniEncounterTransaction updatedEncounterTransaction = retrospectiveService.updatePastEncounters(bahmniEncounterTransaction, patient, null, null);

        assertEquals(bahmniEncounterTransaction,updatedEncounterTransaction);
    }

    @Test
    public void do_not_update_the_encounter_if_it_is_just_created(){
        Date encounterJustCreated = DateTime.now().plusSeconds(10).toDate();

        BahmniEncounterTransaction bahmniEncounterTransaction = new BahmniEncounterTransaction();
        bahmniEncounterTransaction.setEncounterDateTime(encounterJustCreated);

        when(mockVisitIdentificationHelper.getVisitFor(null, null, encounterJustCreated, null, null)).thenReturn(null);

        RetrospectiveEncounterTransactionService retrospectiveService = new RetrospectiveEncounterTransactionService(mockVisitIdentificationHelper);
        BahmniEncounterTransaction updatedEncounterTransaction = retrospectiveService.updatePastEncounters(bahmniEncounterTransaction, null, null, null);

        assertEquals(bahmniEncounterTransaction,updatedEncounterTransaction);
    }

    @Test
    public void update_a_past_encounter_with_matching_visit_details_if_exists() {
        Date jan1_2011 = new DateTime(2014, 1, 1, 10, 0, 0).toDate();
        BahmniEncounterTransaction bahmniEncounterTransaction = new BahmniEncounterTransaction();
        bahmniEncounterTransaction.setEncounterDateTime(jan1_2011);

        Visit existingVisit = getExistingVisit();

        when(mockVisitIdentificationHelper.getVisitFor(null, null, jan1_2011, null, null)).thenReturn(existingVisit);

        RetrospectiveEncounterTransactionService retrospectiveService = new RetrospectiveEncounterTransactionService(mockVisitIdentificationHelper);
        BahmniEncounterTransaction updatedEncounterTransaction = retrospectiveService.updatePastEncounters(bahmniEncounterTransaction, null, null, null);

        assertEquals("visit uuid", updatedEncounterTransaction.getVisitUuid());
        assertEquals("visit-type-uuid", updatedEncounterTransaction.getVisitTypeUuid());
    }

    @Test
    public void update_observation_dates_at_all_levels_to_encounter_date_for_past_encounters() {
        RetrospectiveEncounterTransactionService retrospectiveService = new RetrospectiveEncounterTransactionService(mockVisitIdentificationHelper);

        BahmniEncounterTransaction bahmniEncounterTransaction = new BahmniEncounterTransaction();
        BahmniObservation parentObs = new BahmniObservation();
        parentObs.addGroupMember(new BahmniObservation());
        bahmniEncounterTransaction.addObservation(parentObs);
        Date jan1_2011 = new DateTime(2014, 1, 1, 10, 0, 0).toDate();
        bahmniEncounterTransaction.setEncounterDateTime(jan1_2011);

        when(mockVisitIdentificationHelper.getVisitFor(null, null, jan1_2011, null, null)).thenReturn(getExistingVisit());

        BahmniEncounterTransaction updatedEncounter = retrospectiveService.updatePastEncounters(bahmniEncounterTransaction, null, null, null);

        assertEquals(jan1_2011, updatedEncounter.getObservations().get(0).getObservationDateTime());
        assertEquals(jan1_2011, updatedEncounter.getObservations().get(0).getGroupMembers().get(0).getObservationDateTime());
    }

    @Test
    public void do_not_update_observation_dates_when_set() {
        RetrospectiveEncounterTransactionService retrospectiveService = new RetrospectiveEncounterTransactionService(mockVisitIdentificationHelper);

        Date now = new Date();

        BahmniEncounterTransaction bahmniEncounterTransaction = new BahmniEncounterTransaction();
        BahmniObservation childObs = new BahmniObservation();
        childObs.setObservationDateTime(now);
        BahmniObservation parentObs = new BahmniObservation();
        parentObs.addGroupMember(childObs);
        bahmniEncounterTransaction.addObservation(parentObs);

        Date jan1_2011 = new DateTime(2014, 1, 1, 10, 0, 0).toDate();
        bahmniEncounterTransaction.setEncounterDateTime(jan1_2011);

        when(mockVisitIdentificationHelper.getVisitFor(null, null, jan1_2011, null, null)).thenReturn(getExistingVisit());

        BahmniEncounterTransaction updatedEncounter = retrospectiveService.updatePastEncounters(bahmniEncounterTransaction, null, null, null);

        assertEquals(jan1_2011, updatedEncounter.getObservations().get(0).getObservationDateTime());
        assertEquals(now, updatedEncounter.getObservations().get(0).getGroupMembers().get(0).getObservationDateTime());
    }

    @Test
    public void update_diagnosis_dates_to_encounter_date_for_past_encounters() {
        RetrospectiveEncounterTransactionService retrospectiveService = new RetrospectiveEncounterTransactionService(mockVisitIdentificationHelper);

        BahmniDiagnosisRequest bahmniDiagnosisRequest = new BahmniDiagnosisRequest();

        BahmniEncounterTransaction bahmniEncounterTransaction = new BahmniEncounterTransaction();
        bahmniEncounterTransaction.addBahmniDiagnosis(bahmniDiagnosisRequest);
        Date jan1_2011 = new DateTime(2014, 1, 1, 10, 0, 0).toDate();
        bahmniEncounterTransaction.setEncounterDateTime(jan1_2011);

        when(mockVisitIdentificationHelper.getVisitFor(null, null, jan1_2011, null, null)).thenReturn(getExistingVisit());

        BahmniEncounterTransaction updatedEncounter = retrospectiveService.updatePastEncounters(bahmniEncounterTransaction, null, null, null);

        assertEquals(jan1_2011, updatedEncounter.getBahmniDiagnoses().get(0).getDiagnosisDateTime());
    }

    @Test
    public void update_drugOrder_activation_date_to_encounter_date_for_past_encounters() {
        RetrospectiveEncounterTransactionService retrospectiveService = new RetrospectiveEncounterTransactionService(mockVisitIdentificationHelper);

        EncounterTransaction.DrugOrder drugOrder = new EncounterTransaction.DrugOrder();

        BahmniEncounterTransaction bahmniEncounterTransaction = new BahmniEncounterTransaction();
        bahmniEncounterTransaction.addDrugOrder(drugOrder);
        Date jan1_2011 = new DateTime(2014, 1, 1, 10, 0, 0).toDate();
        bahmniEncounterTransaction.setEncounterDateTime(jan1_2011);

        when(mockVisitIdentificationHelper.getVisitFor(null, null, jan1_2011, null, null)).thenReturn(getExistingVisit());

        BahmniEncounterTransaction updatedEncounter = retrospectiveService.updatePastEncounters(bahmniEncounterTransaction, null, null, null);

        assertEquals(jan1_2011, updatedEncounter.getDrugOrders().get(0).getDateActivated());
    }

    @Test
    public void update_disposition_date_to_encounter_date_for_past_encounters() {
        RetrospectiveEncounterTransactionService retrospectiveService = new RetrospectiveEncounterTransactionService(mockVisitIdentificationHelper);

        EncounterTransaction.Disposition disposition = new EncounterTransaction.Disposition();

        BahmniEncounterTransaction bahmniEncounterTransaction = new BahmniEncounterTransaction();
        bahmniEncounterTransaction.setDisposition(disposition);
        Date jan1_2011 = new DateTime(2014, 1, 1, 10, 0, 0).toDate();
        bahmniEncounterTransaction.setEncounterDateTime(jan1_2011);

        when(mockVisitIdentificationHelper.getVisitFor(null, null, jan1_2011, null, null)).thenReturn(getExistingVisit());

        BahmniEncounterTransaction updatedEncounter = retrospectiveService.updatePastEncounters(bahmniEncounterTransaction, null, null, null);

        assertEquals(jan1_2011, updatedEncounter.getDisposition().getDispositionDateTime());
    }

    private Visit getExistingVisit() {
        return new VisitBuilder().withUUID("visit uuid").withVisitType(getExistingVisitType()).build();
    }

    private VisitType getExistingVisitType() {
        VisitType existingVisitType = new VisitType(10);
        existingVisitType.setUuid("visit-type-uuid");
        return existingVisitType;
    }
}