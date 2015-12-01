package org.openmrs.module.bahmniemrapi.encountertransaction.contract;

import org.apache.commons.lang.time.DateUtils;
import org.joda.time.DateTime;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.module.bahmniemrapi.diagnosis.contract.BahmniDiagnosisRequest;
import org.openmrs.module.bahmniemrapi.obsrelation.contract.ObsRelationship;
import org.openmrs.module.emrapi.diagnosis.Diagnosis;
import org.openmrs.module.emrapi.encounter.domain.EncounterTransaction;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
		Map<String, Object> test = new HashMap<>();
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

	@Test
	public void shouldClearDrugOrderFromExistingET() {
		EncounterTransaction.DrugOrder firstDrugOrder = new EncounterTransaction.DrugOrder();
		EncounterTransaction.DrugOrder secondDrugOrder = new EncounterTransaction.DrugOrder();
		List<EncounterTransaction.DrugOrder> drugOrders = Arrays.asList(firstDrugOrder, secondDrugOrder);
		BahmniEncounterTransaction bahmniEncounterTransaction = new BahmniEncounterTransaction();
		bahmniEncounterTransaction.setDrugOrders(drugOrders);

		bahmniEncounterTransaction.clearDrugOrders();

		assertEquals(new ArrayList<EncounterTransaction.DrugOrder>(), bahmniEncounterTransaction.getDrugOrders());
	}

	@Test
	public void shouldReturnTrueIfThereAreAnyPastDrugOrders() {
		DateTime dateTime = new DateTime();
		dateTime.plusDays(-2);
		EncounterTransaction.DrugOrder drugOrder = new EncounterTransaction.DrugOrder();
		drugOrder.setDateActivated(dateTime.toDate()); //This is a past drug order

		BahmniEncounterTransaction bahmniEncounterTransaction = new BahmniEncounterTransaction();
		bahmniEncounterTransaction.setEncounterDateTime(new Date());
		bahmniEncounterTransaction.setDrugOrders(Arrays.asList(drugOrder));
		Assert.assertEquals(true, bahmniEncounterTransaction.hasPastDrugOrders());
	}

	@Test
	public void shouldReturnTrueIfThereAreSomePastAndSomeFutureDrugOrders() {
		DateTime dateTime = new DateTime();
		dateTime.plusDays(-2);

		DateTime futureDateActivated = new DateTime();
		futureDateActivated.plusDays(2);

		EncounterTransaction.DrugOrder drugOrder = new EncounterTransaction.DrugOrder();
		drugOrder.setDateActivated(dateTime.toDate()); //This is a past drug order

		EncounterTransaction.DrugOrder drugOrder1 = new EncounterTransaction.DrugOrder();
		drugOrder1.setDateActivated(futureDateActivated.toDate());

		BahmniEncounterTransaction bahmniEncounterTransaction = new BahmniEncounterTransaction();
		bahmniEncounterTransaction.setEncounterDateTime(new Date());
		bahmniEncounterTransaction.setDrugOrders(Arrays.asList(drugOrder, drugOrder1));
		Assert.assertEquals(true, bahmniEncounterTransaction.hasPastDrugOrders());
	}

	private ArrayList<BahmniObservation> createBahmniObservations() {
		final BahmniObservation targetObs = createBahmniObservation("target-uuid", "target-value",
				createConcept("target-concept-uuid", "target-obs-concept"), obsDate, null);
		final BahmniObservation targetObs2 = createBahmniObservation("target-uuid-2", "target-value-2",
				createConcept("target-concept-uuid", "target-obs-concept"), obsDate, null);
		return new ArrayList<BahmniObservation>() {{
			this.add(createBahmniObservation("obs-uuid", "obs-value1", createConcept("concept-uuid", "obs-concept"), obsDate,
					createObsRelationShip("obs-relation", targetObs)));
			this.add(createBahmniObservation("obs-uuid-1", "obs-value2", createConcept("concept-uuid-2", "obs-concept-2"),
					obsDate, createObsRelationShip("obs-relation-2", targetObs2)));
		}};
	}

	@Test
	public void shouldReturnFalseIfThereAreNoDrugs() {
		BahmniEncounterTransaction bahmniEncounterTransaction = new BahmniEncounterTransaction();

		assertEquals(false, bahmniEncounterTransaction.hasPastDrugOrders());
	}

	@Test
	public void shouldCopyRequiredFieldsOnCloneForDrugOrders() {
		Set<EncounterTransaction.Provider> providers = new HashSet<EncounterTransaction.Provider>();
		EncounterTransaction.Provider provider = new EncounterTransaction.Provider();
		provider.setUuid("providerUuid");
		providers.add(provider);

		DateTime pastDateActivated = new DateTime();
		pastDateActivated.plusDays(-2);
		DateTime futureDateActivated = new DateTime();
		futureDateActivated.plusDays(2);

		EncounterTransaction.DrugOrder drugOrder = new EncounterTransaction.DrugOrder();
		drugOrder.setScheduledDate(futureDateActivated.toDate());
		EncounterTransaction.DrugOrder drugOrder1 = new EncounterTransaction.DrugOrder();
		drugOrder1.setScheduledDate(pastDateActivated.toDate());

		BahmniEncounterTransaction bahmniEncounterTransaction = new BahmniEncounterTransaction();
		bahmniEncounterTransaction.setDrugOrders(Arrays.asList(drugOrder, drugOrder1));
		bahmniEncounterTransaction.setEncounterTypeUuid("encounterTypeUuid");
		bahmniEncounterTransaction.setLocationUuid("locationUuid");
		bahmniEncounterTransaction.setPatientUuid("patientUuid");
		bahmniEncounterTransaction.setProviders(providers);

		BahmniEncounterTransaction clonedEncounterTransaction = bahmniEncounterTransaction.cloneForPastDrugOrders();
		List<EncounterTransaction.DrugOrder> drugOrders = clonedEncounterTransaction.getDrugOrders();

		assertEquals(drugOrder, drugOrders.get(0));
		assertEquals(drugOrder1, drugOrders.get(1));

		assertEquals(pastDateActivated.toDate(), clonedEncounterTransaction.getEncounterDateTime());
		assertEquals("encounterTypeUuid", clonedEncounterTransaction.getEncounterTypeUuid());
		assertEquals("locationUuid", clonedEncounterTransaction.getLocationUuid());
		assertEquals("patientUuid", clonedEncounterTransaction.getPatientUuid());
		assertEquals(providers, clonedEncounterTransaction.getProviders());
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

	private BahmniObservation createBahmniObservation(String uuid, String value, EncounterTransaction.Concept concept,
	                                                  Date obsDate, ObsRelationship targetObs) {
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
