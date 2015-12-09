package org.bahmni.module.bahmnicore.web.v1_0.controller;

import org.bahmni.module.bahmnicore.service.BahmniObsService;
import org.bahmni.module.bahmnicore.web.v1_0.controller.display.controls.BahmniObservationsController;
import org.bahmni.test.builder.VisitBuilder;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.openmrs.Concept;
import org.openmrs.Visit;
import org.openmrs.api.ConceptService;
import org.openmrs.api.VisitService;
import org.openmrs.module.bahmniemrapi.encountertransaction.contract.BahmniObservation;
import org.openmrs.module.emrapi.encounter.domain.EncounterTransaction;

import java.util.Arrays;
import java.util.Collection;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;


public class BahmniObservationsControllerTest {

    @Mock
    private BahmniObsService bahmniObsService;

    @Mock
    private ConceptService conceptService;

    @Mock
    private VisitService visitService;

    private Visit visit;
    private Concept concept;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        visit = new VisitBuilder().build();
        concept = new Concept();
        when(visitService.getVisitByUuid("visitId")).thenReturn(visit);
        when(conceptService.getConceptByName("Weight")).thenReturn(concept);
    }

    @Test
    public void returnLatestObservations() throws Exception {
        BahmniObservation latestObs = new BahmniObservation();
        latestObs.setUuid("initialId");
        when(bahmniObsService.getLatestObsByVisit(visit, Arrays.asList(concept), null, true)).thenReturn(Arrays.asList(latestObs));

        BahmniObservationsController bahmniObservationsController = new BahmniObservationsController(bahmniObsService, conceptService, visitService);
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

        when(bahmniObsService.getInitialObsByVisit(visit, Arrays.asList(this.concept), null, true)).thenReturn(Arrays.asList(initialObs));

        BahmniObservationsController bahmniObservationsController = new BahmniObservationsController(bahmniObsService, conceptService, visitService);
        Collection<BahmniObservation> bahmniObservations = bahmniObservationsController.get("visitId", "initial", Arrays.asList("Weight"), null, true);

        assertEquals(1, bahmniObservations.size());
    }

    @Test
    public void returnAllObservations() throws Exception {
        BahmniObservation obs = new BahmniObservation();
        when(bahmniObsService.getObservationForVisit("visitId", Arrays.asList("Weight"), null, true, null)).thenReturn(Arrays.asList(obs));

        BahmniObservationsController bahmniObservationsController = new BahmniObservationsController(bahmniObsService, conceptService, visitService);
        Collection<BahmniObservation> bahmniObservations = bahmniObservationsController.get("visitId", null, Arrays.asList("Weight"), null, true);

        verify(bahmniObsService, never()).getLatestObsByVisit(visit, Arrays.asList(concept), null, false);
        verify(bahmniObsService, never()).getInitialObsByVisit(visit, Arrays.asList(concept), null, false);

        assertEquals(1, bahmniObservations.size());
    }
}