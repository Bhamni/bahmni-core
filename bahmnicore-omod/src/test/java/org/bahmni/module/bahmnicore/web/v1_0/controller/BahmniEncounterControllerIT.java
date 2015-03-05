package org.bahmni.module.bahmnicore.web.v1_0.controller;

import org.bahmni.test.web.controller.BaseWebControllerTest;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.openmrs.Visit;
import org.openmrs.api.VisitService;
import org.openmrs.api.context.Context;
import org.openmrs.module.bahmnicore.web.v1_0.controller.BahmniEncounterController;
import org.openmrs.module.bahmniemrapi.diagnosis.contract.BahmniDiagnosis;
import org.openmrs.module.bahmniemrapi.diagnosis.contract.BahmniDiagnosisRequest;
import org.openmrs.module.bahmniemrapi.encountertransaction.contract.BahmniEncounterTransaction;
import org.openmrs.module.bahmniemrapi.laborder.contract.LabOrderResults;
import org.openmrs.module.emrapi.diagnosis.Diagnosis;
import org.openmrs.module.emrapi.encounter.domain.EncounterTransaction;
import org.openmrs.module.webservices.rest.web.RestConstants;
import org.openmrs.web.test.BaseModuleWebContextSensitiveTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.ArrayList;
import java.util.Date;

import static org.junit.Assert.*;
@org.springframework.test.context.ContextConfiguration(locations = {"classpath:TestingApplicationContext.xml"}, inheritLocations = true)
public class BahmniEncounterControllerIT extends BaseWebControllerTest {
    @Autowired
    private VisitService visitService;
    @Autowired
    private BahmniEncounterController bahmniEncounterController;

    @Before
    public void setUp() throws Exception {
        executeDataSet("diagnosisMetadata.xml");
        executeDataSet("setup.xml");
    }

    @Test @Ignore
    public void shouldSaveNewDiagnosisWithinTheSameEncounterSession() throws Exception {
        BahmniEncounterTransaction bahmniEncounterTransaction = bahmniEncounterTransaction();
        final String comments = "High fever and symptoms indicate Malaria";
        bahmniEncounterTransaction.setBahmniDiagnoses(new ArrayList<BahmniDiagnosisRequest>() {
            {
                this.add(new BahmniDiagnosisRequest() {{
                    this.setCertainty(Diagnosis.Certainty.CONFIRMED.name());
                    this.setOrder(Diagnosis.Order.PRIMARY.name());
                    this.setCodedAnswer(new EncounterTransaction.Concept("d102c80f-1yz9-4da3-bb88-8122ce8868dh"));
                    this.setDiagnosisStatusConcept(new EncounterTransaction.Concept(null, "Ruled Out"));
                    this.setComments(comments);
                }});
            }
        });

        BahmniEncounterTransaction encounterTransaction = bahmniEncounterController.update(bahmniEncounterTransaction);
        assertEquals("1e5d5d48-6b78-11e0-93c3-18a905e044dc", encounterTransaction.getVisitUuid());
        assertEquals(1, encounterTransaction.getBahmniDiagnoses().size());
        final BahmniDiagnosis bahmniDiagnosisAfterFirstSave = encounterTransaction.getBahmniDiagnoses().get(0);
        assertDiagnosis(bahmniDiagnosisAfterFirstSave, Diagnosis.Certainty.CONFIRMED, Diagnosis.Order.PRIMARY, "Ruled Out", false, comments);
        assertDiagnosis(bahmniDiagnosisAfterFirstSave.getFirstDiagnosis(), Diagnosis.Certainty.CONFIRMED, Diagnosis.Order.PRIMARY, "Ruled Out", false, comments);

        bahmniEncounterTransaction.setBahmniDiagnoses(new ArrayList<BahmniDiagnosisRequest>() {
            {
                this.add(new BahmniDiagnosisRequest() {{
                    this.setCertainty(Diagnosis.Certainty.PRESUMED.name());
                    this.setOrder(Diagnosis.Order.SECONDARY.name());
                    this.setCodedAnswer(new EncounterTransaction.Concept("d102c80f-1yz9-4da3-bb88-8122ce8868dh"));
                    this.setExistingObs(bahmniDiagnosisAfterFirstSave.getExistingObs());
                    this.setFirstDiagnosis(bahmniDiagnosisAfterFirstSave);
                }});
            }
        });
        encounterTransaction = bahmniEncounterController.update(bahmniEncounterTransaction);
        final BahmniDiagnosis bahmniDiagnosisAfterSecondSave = encounterTransaction.getBahmniDiagnoses().get(0);
        assertDiagnosis(bahmniDiagnosisAfterSecondSave, Diagnosis.Certainty.PRESUMED, Diagnosis.Order.SECONDARY, null, false, null);
        assertDiagnosis(bahmniDiagnosisAfterSecondSave.getFirstDiagnosis(), Diagnosis.Certainty.PRESUMED, Diagnosis.Order.SECONDARY, null, false, null);
        Context.flushSession();
        closeVisit(encounterTransaction.getVisitUuid());
    }

    @Test
    public void shouldRetrieveStuff() throws Exception {
        MockHttpServletRequest mockHttpServletRequest = newGetRequest("/rest/" + RestConstants.VERSION_1 + "/bahmnicore/bahmniencounter");
        MockHttpServletResponse response = handle(mockHttpServletRequest);
        Object labOrderResults = deserialize(response, Object.class);
    }

    @Test
    @Ignore
    public void shouldUpdateDiagnosisFromAnotherVisit() throws Exception {
        BahmniEncounterTransaction encounterTransactionForFirstVisit = bahmniEncounterTransaction();
        encounterTransactionForFirstVisit.setBahmniDiagnoses(new ArrayList<BahmniDiagnosisRequest>() {
            {
                this.add(new BahmniDiagnosisRequest() {{
                    this.setCertainty(Diagnosis.Certainty.PRESUMED.name());
                    this.setOrder(Diagnosis.Order.SECONDARY.name());
                    this.setCodedAnswer(new EncounterTransaction.Concept("d102c80f-1yz9-4da3-bb88-8122ce8868dh"));
                }});
            }
        });
        BahmniEncounterTransaction firstEncounterTransaction = bahmniEncounterController.update(encounterTransactionForFirstVisit);
        closeVisit(firstEncounterTransaction.getVisitUuid());

        final BahmniDiagnosis bahmniDiagnosisAfterFirstSave = firstEncounterTransaction.getBahmniDiagnoses().get(0);
        assertDiagnosis(bahmniDiagnosisAfterFirstSave, Diagnosis.Certainty.PRESUMED, Diagnosis.Order.SECONDARY, null, false, null);

        BahmniEncounterTransaction encounterTransactionForSecondVisit = bahmniEncounterTransaction();
        encounterTransactionForSecondVisit.setBahmniDiagnoses(new ArrayList<BahmniDiagnosisRequest>() {
            {
                this.add(new BahmniDiagnosisRequest() {{
                    this.setCertainty(Diagnosis.Certainty.CONFIRMED.name());
                    this.setOrder(Diagnosis.Order.PRIMARY.name());
                    this.setCodedAnswer(new EncounterTransaction.Concept("d102c80f-1yz9-4da3-bb88-8122ce8868dh"));
                    this.setDiagnosisStatusConcept(new EncounterTransaction.Concept(null, "Ruled Out"));
                    this.setExistingObs(null);
                    this.setPreviousObs(bahmniDiagnosisAfterFirstSave.getExistingObs());
                    this.setFirstDiagnosis(bahmniDiagnosisAfterFirstSave);
                }});
            }
        });
        BahmniEncounterTransaction secondEncounterTransaction = bahmniEncounterController.update(encounterTransactionForSecondVisit);
        assertNotEquals(firstEncounterTransaction.getEncounterUuid(), secondEncounterTransaction.getEncounterUuid());

        final BahmniDiagnosis bahmniDiagnosisAfterSecondSave = secondEncounterTransaction.getBahmniDiagnoses().get(0);
        assertNotEquals(bahmniDiagnosisAfterFirstSave.getExistingObs(), bahmniDiagnosisAfterSecondSave.getExistingObs());
        assertDiagnosis(bahmniDiagnosisAfterSecondSave, Diagnosis.Certainty.CONFIRMED, Diagnosis.Order.PRIMARY, "Ruled Out", false, null);
        assertDiagnosis(bahmniDiagnosisAfterSecondSave.getFirstDiagnosis(), Diagnosis.Certainty.PRESUMED, Diagnosis.Order.SECONDARY, null, true, null);
        BahmniEncounterTransaction bahmniEncounterTransaction = bahmniEncounterController.get(firstEncounterTransaction.getEncounterUuid());
        assertTrue(bahmniEncounterTransaction.getBahmniDiagnoses().get(0).isRevised());

        Context.flushSession();
    }

    private void closeVisit(String visitUuid) {
        Visit visit = visitService.getVisitByUuid(visitUuid);
        visit.setStopDatetime(new Date());
        visitService.saveVisit(visit);
    }

    private void assertDiagnosis(BahmniDiagnosis bahmniDiagnosis, Diagnosis.Certainty certainty, Diagnosis.Order order, String status, boolean isRevised, String comments) {
        assertEquals(certainty.name(), bahmniDiagnosis.getCertainty());
        assertEquals(order.name(), bahmniDiagnosis.getOrder());
        if (status != null) {
            assertEquals(status, bahmniDiagnosis.getDiagnosisStatusConcept().getName());
        }
        assertEquals(isRevised, bahmniDiagnosis.isRevised());
        assertEquals(comments, bahmniDiagnosis.getComments());
    }

    private BahmniEncounterTransaction bahmniEncounterTransaction() {
        BahmniEncounterTransaction bahmniEncounterTransaction = new BahmniEncounterTransaction();
        bahmniEncounterTransaction.setPatientUuid("a76e8d23-0c38-408c-b2a8-ea5540f01b51");
        bahmniEncounterTransaction.setVisitTypeUuid("b45ca846-c79a-11e2-b0c0-8e397087571c");
        bahmniEncounterTransaction.setEncounterTypeUuid("2b377dba-62c3-4e53-91ef-b51c68899890");
        return bahmniEncounterTransaction;
    }

}
