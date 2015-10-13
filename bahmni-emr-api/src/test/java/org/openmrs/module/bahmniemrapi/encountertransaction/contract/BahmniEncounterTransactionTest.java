package org.openmrs.module.bahmniemrapi.encountertransaction.contract;

import net.sourceforge.jtds.jdbc.DateTime;
import org.apache.commons.lang.time.DateUtils;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.module.bahmniemrapi.diagnosis.contract.BahmniDiagnosisRequest;
import org.openmrs.module.bahmniemrapi.encountertransaction.utils.DateUtil;
import org.openmrs.module.bahmniemrapi.obsrelation.contract.ObsRelationship;
import org.openmrs.module.emrapi.diagnosis.Diagnosis;
import org.openmrs.module.emrapi.encounter.domain.EncounterTransaction;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class BahmniEncounterTransactionTest {
    private final Date obsDate = new Date();
    BahmniEncounterTransaction bahmniEncounterTransaction;

    @Before
    public void setUp() throws Exception {

    }

    @Test
    public void shouldConvertBahmniEncounterTransactionToET() {
        bahmniEncounterTransaction = new BahmniEncounterTransaction();
        bahmniEncounterTransaction.setBahmniDiagnoses(createBahmniDiagnoses());
        bahmniEncounterTransaction.setObservations(createBahmniObservations());
        bahmniEncounterTransaction.setExtensions(createExtensions());
        EncounterTransaction encounterTransaction = bahmniEncounterTransaction.toEncounterTransaction();

        assertEquals(2, encounterTransaction.getDiagnoses().size());

        EncounterTransaction.Diagnosis diagnosis1 = encounterTransaction.getDiagnoses().get(0);
        assertEquals(Diagnosis.Certainty.CONFIRMED.name(), diagnosis1.getCertainty());
        assertEquals(Diagnosis.Order.PRIMARY.name(), diagnosis1.getOrder());
        assertEquals("d102c80f-1yz9-4da3-bb88-8122ce8868dh", diagnosis1.getCodedAnswer().getUuid());

        EncounterTransaction.Diagnosis diagnosis2 = encounterTransaction.getDiagnoses().get(1);
        assertEquals(Diagnosis.Certainty.PRESUMED.name(), diagnosis2.getCertainty());
        assertEquals(Diagnosis.Order.SECONDARY.name(), diagnosis2.getOrder());
        assertEquals("e102c80f-1yz9-4da3-bb88-8122ce8868dh", diagnosis2.getCodedAnswer().getUuid());

        assertEquals(2, encounterTransaction.getObservations().size());

        EncounterTransaction.Observation observation1 = encounterTransaction.getObservations().get(0);
        assertEquals("comment", observation1.getComment());
        assertEquals("obs-uuid", observation1.getUuid());
        assertEquals("concept-uuid", observation1.getConceptUuid());
        assertEquals("order-uuid", observation1.getOrderUuid());
        assertEquals(obsDate, observation1.getObservationDateTime());
        assertEquals("obs-value1", observation1.getValue());
        assertEquals(true, observation1.getVoided());
        assertEquals("chumma", observation1.getVoidReason());

        EncounterTransaction.Observation observation2 = encounterTransaction.getObservations().get(1);
        assertEquals("comment", observation2.getComment());
        assertEquals("obs-uuid-1", observation2.getUuid());
        assertEquals("concept-uuid-2", observation2.getConceptUuid());
        assertEquals("order-uuid", observation2.getOrderUuid());
        assertEquals(obsDate, observation2.getObservationDateTime());
        assertEquals("obs-value2", observation2.getValue());
        assertEquals(true, observation2.getVoided());
        assertEquals("chumma", observation2.getVoidReason());

        assertNotNull(encounterTransaction.getExtensions());
        assertEquals(1, encounterTransaction.getExtensions().size());
        assertTrue(encounterTransaction.getExtensions().containsKey("extension"));
        assertEquals("Any Object Here", encounterTransaction.getExtensions().get("extension"));
    }

    private Map<String, Object> createExtensions() {
        Map<String,Object> test = new HashMap<>();
        test.put("extension", "Any Object Here");
        return test;
    }

    @Test
    public void isRetrospectiveEntryShouldReturnTrueIfTheEncounterDateTimeIsBeforeToday() throws Exception {
        assertEquals(true, BahmniEncounterTransaction.isRetrospectiveEntry(DateUtils.addDays(new Date(), -2)));
    }

    @Test
    public void isRetrospectiveEntryShouldReturnFalseIfTheEncounterDateTimeIsNull() throws Exception {
        assertEquals(false, BahmniEncounterTransaction.isRetrospectiveEntry(null));
    }

    @Test
    public void isRetrospectiveEntryShouldReturnFalseIfTheEncounterDateTimeSameAsToday() throws Exception {
        assertEquals(false, BahmniEncounterTransaction.isRetrospectiveEntry(new Date()));
    }

    private ArrayList<BahmniDiagnosisRequest> createBahmniDiagnoses() {
        return new ArrayList<BahmniDiagnosisRequest>() {
            {
                this.add(new BahmniDiagnosisRequest() {{
                    this.setCertainty(Diagnosis.Certainty.CONFIRMED.name());
                    this.setOrder(Diagnosis.Order.PRIMARY.name());
                    this.setCodedAnswer(new EncounterTransaction.Concept("d102c80f-1yz9-4da3-bb88-8122ce8868dh"));
                    this.setDiagnosisStatusConcept(new EncounterTransaction.Concept(null, "Ruled Out"));
                    this.setComments("comments");
                    this.setEncounterUuid("enc-uuid");

                }});

                this.add(new BahmniDiagnosisRequest() {{
                    this.setCertainty(Diagnosis.Certainty.PRESUMED.name());
                    this.setOrder(Diagnosis.Order.SECONDARY.name());
                    this.setCodedAnswer(new EncounterTransaction.Concept("e102c80f-1yz9-4da3-bb88-8122ce8868dh"));
                    this.setDiagnosisStatusConcept(new EncounterTransaction.Concept(null, "Ruled Out"));
                    this.setEncounterUuid("enc-uuid");
                }});


            }
        };
    }

    private ArrayList<BahmniObservation> createBahmniObservations() {
        final BahmniObservation targetObs = createBahmniObservation("target-uuid", "target-value", createConcept("target-concept-uuid", "target-obs-concept"), obsDate, null);
        final BahmniObservation targetObs2 = createBahmniObservation("target-uuid-2", "target-value-2", createConcept("target-concept-uuid", "target-obs-concept"), obsDate, null);
        return new ArrayList<BahmniObservation>() {{
            this.add(createBahmniObservation("obs-uuid", "obs-value1", createConcept("concept-uuid", "obs-concept"), obsDate,
                    createObsRelationShip("obs-relation", targetObs)));
            this.add(createBahmniObservation("obs-uuid-1", "obs-value2", createConcept("concept-uuid-2", "obs-concept-2"), obsDate,
                    createObsRelationShip("obs-relation-2", targetObs2)));
        }};
    }

    private BahmniObservation createBahmniObservation(String uuid, String value, EncounterTransaction.Concept concept, Date obsDate, ObsRelationship targetObs) {
        BahmniObservation bahmniObservation = new BahmniObservation();
        bahmniObservation.setUuid(uuid);
        bahmniObservation.setValue(value);
        bahmniObservation.setConcept(concept);
        bahmniObservation.setComment("comment");
        bahmniObservation.setObservationDateTime(obsDate);
        bahmniObservation.setOrderUuid("order-uuid");
        bahmniObservation.setVoided(true);
        bahmniObservation.setVoidReason("chumma");
        bahmniObservation.setTargetObsRelation(targetObs);
        return bahmniObservation;
    }

    private ObsRelationship createObsRelationShip(String relationTypeName, BahmniObservation bahmniObservation) {
        ObsRelationship obsRelationship = new ObsRelationship();
        obsRelationship.setRelationshipType(relationTypeName);
        obsRelationship.setTargetObs(bahmniObservation);
        return obsRelationship;
    }

    private EncounterTransaction.Concept createConcept(String conceptUuid, String conceptName) {
        EncounterTransaction.Concept concept = new EncounterTransaction.Concept();
        concept.setUuid(conceptUuid);
        concept.setName(conceptName);
        return concept;
    }
}
