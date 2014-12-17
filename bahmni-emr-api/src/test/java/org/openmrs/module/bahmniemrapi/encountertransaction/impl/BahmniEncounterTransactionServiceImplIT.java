package org.openmrs.module.bahmniemrapi.encountertransaction.impl;

import org.junit.Before;
import org.junit.Test;
import org.openmrs.module.bahmniemrapi.encountertransaction.contract.BahmniEncounterTransaction;
import org.openmrs.module.bahmniemrapi.encountertransaction.contract.BahmniObservation;
import org.openmrs.module.bahmniemrapi.encountertransaction.service.BahmniEncounterTransactionService;
import org.openmrs.module.bahmniemrapi.obsrelation.contract.ObsRelationship;
import org.openmrs.module.emrapi.encounter.domain.EncounterTransaction;
import org.openmrs.web.test.BaseModuleWebContextSensitiveTest;
import org.springframework.beans.factory.annotation.Autowired;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@org.springframework.test.context.ContextConfiguration(locations = {"classpath:TestingApplicationContext.xml"}, inheritLocations = true)
public class BahmniEncounterTransactionServiceImplIT extends BaseModuleWebContextSensitiveTest {

    @Autowired
    BahmniEncounterTransactionService bahmniEncounterTransactionService;

    @Before
    public void setUp() throws Exception {
        executeDataSet("diagnosisMetadata.xml");
        executeDataSet("dispositionMetadata.xml");
        executeDataSet("obsRelationshipDataset.xml");
    }

    @Test
    public void shouldSaveBahmniEncounterTransactionWithBahmniObservationsWithGivenUuid(){
        Date obsDate = new Date();
        String obsUuid = UUID.randomUUID().toString();
        String visitUuid = "4e663d66-6b78-11e0-93c3-18a905e044dc";
        String patientUuid = "da7f524f-27ce-4bb2-86d6-6d1d05312bd5";

        BahmniObservation bahmniObservation = createBahmniObservation(obsUuid, "obs-value", createConcept("96408258-000b-424e-af1a-403919332938", "FAVORITE FOOD, NON-CODED"), obsDate, null);
        BahmniEncounterTransaction bahmniEncounterTransaction = new BahmniEncounterTransaction();
        bahmniEncounterTransaction.addObservation(bahmniObservation);

        bahmniEncounterTransaction.setEncounterTypeUuid("07000be2-26b6-4cce-8b40-866d8435b613");
        bahmniEncounterTransaction.setPatientUuid(patientUuid);

        bahmniEncounterTransaction.setVisitUuid(visitUuid);
        BahmniEncounterTransaction encounterTransaction = bahmniEncounterTransactionService.save(bahmniEncounterTransaction);

        assertNotNull(encounterTransaction);
        assertEquals(1, encounterTransaction.getObservations().size());
        assertEquals(bahmniObservation.getValue(),encounterTransaction.getObservations().get(0).getValue());
        assertEquals(obsUuid, encounterTransaction.getObservations().get(0).getUuid());

    }

    @Test
    public void shouldSaveObsRelationShipWhenBothObservationsAreInSameEncounter(){
        Date obsDate = new Date();
        String srcObsUuid = UUID.randomUUID().toString();
        String targetObsUuid = UUID.randomUUID().toString();
        String visitUuid = "4e663d66-6b78-11e0-93c3-18a905e044dc";
        String patientUuid = "da7f524f-27ce-4bb2-86d6-6d1d05312bd5";

        BahmniEncounterTransaction bahmniEncounterTransaction = new BahmniEncounterTransaction();
        bahmniEncounterTransaction.setEncounterTypeUuid("07000be2-26b6-4cce-8b40-866d8435b613");
        bahmniEncounterTransaction.setPatientUuid(patientUuid);
        bahmniEncounterTransaction.setVisitUuid(visitUuid);

        EncounterTransaction.Concept targetConcept = createConcept("c607c80f-1ea9-4da3-bb88-6276ce8868dd", "WEIGHT (KG)");
        BahmniObservation targetObs = createBahmniObservation(targetObsUuid, 150.0, targetConcept, obsDate, null);
        bahmniEncounterTransaction.addObservation(targetObs);

        EncounterTransaction.Concept srcConcept = createConcept("96408258-000b-424e-af1a-403919332938", "FAVORITE FOOD, NON-CODED");
        BahmniObservation srcObs = createBahmniObservation(srcObsUuid, "src-value", srcConcept, obsDate, targetObs);
        bahmniEncounterTransaction.addObservation(srcObs);

        BahmniEncounterTransaction encounterTransaction = bahmniEncounterTransactionService.save(bahmniEncounterTransaction);

        assertEquals(2, encounterTransaction.getObservations().size());

        BahmniObservation savedSrcObs = getObservationByConceptUuid(encounterTransaction.getObservations(), srcConcept.getUuid());
        assertEquals(srcObs.getValue(), savedSrcObs.getValue());
        assertEquals(srcObsUuid, savedSrcObs.getUuid());
        assertEquals(srcConcept.getUuid(), savedSrcObs.getConceptUuid());

        assertEquals(targetObs.getValue(), savedSrcObs.getTargetObsRelation().getTargetObs().getValue());
        assertEquals(targetObsUuid, savedSrcObs.getTargetObsRelation().getTargetObs().getUuid());
        assertEquals(targetConcept.getUuid(), savedSrcObs.getTargetObsRelation().getTargetObs().getConceptUuid());
    }

    @Test
    public void shouldSaveObsRelationShipWhenBothObservationsAreInDifferentEncounter() throws ParseException {
        Date obsDate = new Date();
        String srcObsUuid = UUID.randomUUID().toString();
        String targetObsUuid = "f6ec1267-8eac-415f-a3f0-e47be2c8bb67";
        String visitUuid = "4e663d66-6b78-11e0-93c3-18a905e044dc";
        String patientUuid = "da7f524f-27ce-4bb2-86d6-6d1d05312bd5";

        BahmniEncounterTransaction bahmniEncounterTransaction = new BahmniEncounterTransaction();
        bahmniEncounterTransaction.setEncounterTypeUuid("07000be2-26b6-4cce-8b40-866d8435b613");
        bahmniEncounterTransaction.setPatientUuid(patientUuid);
        bahmniEncounterTransaction.setVisitUuid(visitUuid);

        EncounterTransaction.Concept targetConcept = createConcept("a09ab2c5-878e-4905-b25d-5784167d0216", "CD4 COUNT");
        BahmniObservation targetObs = createBahmniObservation(targetObsUuid, 175, targetConcept, new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.S").parse("2008-08-15 00:00:00.0"), null);

        EncounterTransaction.Concept srcConcept = createConcept("96408258-000b-424e-af1a-403919332938", "FAVORITE FOOD, NON-CODED");
        BahmniObservation srcObs = createBahmniObservation(srcObsUuid, "src-value", srcConcept, obsDate, targetObs);

        bahmniEncounterTransaction.addObservation(srcObs);

        BahmniEncounterTransaction mappedBahmniEncounterTransaction = bahmniEncounterTransactionService.save(bahmniEncounterTransaction);

        assertEquals(1, mappedBahmniEncounterTransaction.getObservations().size());
        BahmniObservation savedSrcObs = mappedBahmniEncounterTransaction.getObservations().get(0);
        assertEquals(srcObs.getValue(), savedSrcObs.getValue());
        assertEquals(srcObsUuid, savedSrcObs.getUuid());
        assertEquals(srcObs.getConcept().getUuid(), savedSrcObs.getConceptUuid());
        assertEquals(targetObs.getValue(), savedSrcObs.getTargetObsRelation().getTargetObs().getValue());
        assertEquals(targetObs.getUuid(), savedSrcObs.getTargetObsRelation().getTargetObs().getUuid());
        assertEquals(targetConcept.getUuid(), savedSrcObs.getTargetObsRelation().getTargetObs().getConceptUuid());
        assertEquals(targetObs.getObservationDateTime(), savedSrcObs.getTargetObsRelation().getTargetObs().getObservationDateTime());
    }

    private BahmniObservation getObservationByConceptUuid(List<BahmniObservation> bahmniObservations, String conceptUuid) {
        for (BahmniObservation bahmniObservation : bahmniObservations) {
            if (conceptUuid.equals(bahmniObservation.getConceptUuid())){
                return  bahmniObservation;
            }
        }
        return null;
    }

    private EncounterTransaction.Concept createConcept(String conceptUuid, String conceptName) {
        EncounterTransaction.Concept concept = new EncounterTransaction.Concept();
        concept.setUuid(conceptUuid);
        concept.setName(conceptName);
        return concept;
    }

    private BahmniObservation createBahmniObservation(String uuid, String value, EncounterTransaction.Concept concept, Date obsDate, BahmniObservation bahmniObservation) {
        BahmniObservation bahmniObservation1 = new BahmniObservation();
        bahmniObservation1.setUuid(uuid);
        bahmniObservation1.setValue(value);
        bahmniObservation1.setConcept(concept);
        bahmniObservation1.setComment("comment");
        bahmniObservation1.setObservationDateTime(obsDate);
        bahmniObservation1.setTargetObsRelation(new ObsRelationship(bahmniObservation,null,"qualified-by"));
        return bahmniObservation1;
    }

    private BahmniObservation createBahmniObservation(String uuid, double value, EncounterTransaction.Concept concept, Date obsDate, BahmniObservation bahmniObservation) {
        BahmniObservation bahmniObservation1 = new BahmniObservation();
        bahmniObservation1.setUuid(uuid);
        bahmniObservation1.setValue(value);
        bahmniObservation1.setConcept(concept);
        bahmniObservation1.setComment("comment");
        bahmniObservation1.setObservationDateTime(obsDate);
        bahmniObservation1.setTargetObsRelation(new ObsRelationship(bahmniObservation,null,"qualified-by"));
        return bahmniObservation1;
    }
}
