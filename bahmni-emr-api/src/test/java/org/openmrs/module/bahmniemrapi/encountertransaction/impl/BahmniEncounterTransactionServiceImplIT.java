package org.openmrs.module.bahmniemrapi.encountertransaction.impl;

import org.joda.time.DateTime;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.DrugOrder;
import org.openmrs.Encounter;
import org.openmrs.Order;
import org.openmrs.Patient;
import org.openmrs.Visit;
import org.openmrs.VisitAttribute;
import org.openmrs.api.EncounterService;
import org.openmrs.api.OrderService;
import org.openmrs.api.PatientService;
import org.openmrs.api.VisitService;
import org.openmrs.api.context.Context;
import org.openmrs.module.bahmniemrapi.BaseIntegrationTest;
import org.openmrs.module.bahmniemrapi.encountertransaction.contract.BahmniEncounterTransaction;
import org.openmrs.module.bahmniemrapi.encountertransaction.contract.BahmniObservation;
import org.openmrs.module.bahmniemrapi.encountertransaction.service.BahmniEncounterTransactionService;
import org.openmrs.module.bahmniemrapi.encountertransaction.service.VisitIdentificationHelper;
import org.openmrs.module.bahmniemrapi.obsrelation.contract.ObsRelationship;
import org.openmrs.module.emrapi.CareSettingType;
import org.openmrs.module.emrapi.encounter.DrugMapper;
import org.openmrs.module.emrapi.encounter.domain.EncounterTransaction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class BahmniEncounterTransactionServiceImplIT extends BaseIntegrationTest {

    @Autowired
    private BahmniEncounterTransactionService bahmniEncounterTransactionService;

    @Autowired
    private VisitService visitService;

    @Autowired
    private EncounterService encounterService;

    @Autowired
    private PatientService patientService;

    @Autowired
    private OrderService orderService;

    @Autowired
    @Qualifier("drugMapper")
    private DrugMapper drugMapper;

    @Before
    public void setUp() throws Exception {
        executeDataSet("diagnosisMetadata.xml");
        executeDataSet("dispositionMetadata.xml");
        executeDataSet("obsRelationshipDataset.xml");
        executeDataSet("visitAttributeDataSet.xml");
        executeDataSet("drugOrderTestData.xml");
    }

    @Test
    public void shouldSaveFutureDrugOrdersInEncounterTransaction() {
        Date obsDate = new Date();
        String obsUuid = UUID.randomUUID().toString();
        String visitUuid = "4e663d66-6b78-11e0-93c3-18a905e044dc";
        String patientUuid = "da7f524f-27ce-4bb2-86d6-6d1d05312bd5";

        Patient patient = patientService.getPatientByUuid(patientUuid);
        List<Order> originalOrders = orderService.getActiveOrders(patient, orderService.getOrderTypeByName("Drug Order"),
                orderService.getCareSettingByName("OUTPATIENT"), null);

        EncounterTransaction.Provider provider = new EncounterTransaction.Provider();
        provider.setUuid(Context.getProviderService().getProvider(1).getUuid());
        Set<EncounterTransaction.Provider> providerSet = new HashSet<EncounterTransaction.Provider>();
        providerSet.add(provider);

        BahmniObservation bahmniObservation = createBahmniObservation(obsUuid, "obs-value",
                createConcept("96408258-000b-424e-af1a-403919332938", "FAVORITE FOOD, NON-CODED"), obsDate, null);
        BahmniEncounterTransaction bahmniEncounterTransaction = new BahmniEncounterTransaction();
        bahmniEncounterTransaction.addObservation(bahmniObservation);
        bahmniEncounterTransaction.setVisitTypeUuid("c0c579b0-8e59-401d-8a4a-976a0b183593");
        bahmniEncounterTransaction.setProviders(providerSet);

        bahmniEncounterTransaction.setEncounterTypeUuid("07000be2-26b6-4cce-8b40-866d8435b613");
        bahmniEncounterTransaction.setPatientUuid(patientUuid);
        bahmniEncounterTransaction.setVisitUuid(visitUuid);

        List<EncounterTransaction.DrugOrder> drugOrders = new ArrayList<>();
        drugOrders.add(createETDrugOrder("1ce527b5-d6de-43f0-bc62-4616abacd77e", null, null,
                new DateTime().plusDays(2).toDate()));
        bahmniEncounterTransaction.setDrugOrders(drugOrders);

        BahmniEncounterTransaction encounterTransaction = bahmniEncounterTransactionService.save(bahmniEncounterTransaction);

        List<Order> latestOrders = orderService.getActiveOrders(patient, orderService.getOrderTypeByName("Drug Order"),
                orderService.getCareSettingByName("OUTPATIENT"), null);
        Assert.assertEquals(originalOrders.size() + 1, latestOrders.size());
        Assert.assertEquals(Order.Action.NEW, latestOrders.get(originalOrders.size()).getAction());
        Assert.assertEquals(1, encounterTransaction.getDrugOrders().size());
    }

    @Test
    public void shouldSavePastDrugOrdersInEncounterTransaction() {
        Date obsDate = new Date();
        String obsUuid = UUID.randomUUID().toString();
        String visitUuid = "4e663d66-6b78-11e0-93c3-18a905e044dc";
        String patientUuid = "da7f524f-27ce-4bb2-86d6-6d1d05312bd5";

        Patient patient = patientService.getPatientByUuid(patientUuid);
        List<Order> originalOrders = orderService.getActiveOrders(patient, orderService.getOrderTypeByName("Drug Order"),
                orderService.getCareSettingByName("OUTPATIENT"), null);

        EncounterTransaction.Provider provider = new EncounterTransaction.Provider();
        provider.setUuid(Context.getProviderService().getProvider(1).getUuid());
        Set<EncounterTransaction.Provider> providerSet = new HashSet<EncounterTransaction.Provider>();
        providerSet.add(provider);

        BahmniObservation bahmniObservation = createBahmniObservation(obsUuid, "obs-value",
                createConcept("96408258-000b-424e-af1a-403919332938", "FAVORITE FOOD, NON-CODED"), obsDate, null);
        BahmniEncounterTransaction bahmniEncounterTransaction = new BahmniEncounterTransaction();
        bahmniEncounterTransaction.addObservation(bahmniObservation);
        bahmniEncounterTransaction.setVisitTypeUuid("c0c579b0-8e59-401d-8a4a-976a0b183593");
        bahmniEncounterTransaction.setProviders(providerSet);

        bahmniEncounterTransaction.setEncounterTypeUuid("07000be2-26b6-4cce-8b40-866d8435b613");
        bahmniEncounterTransaction.setPatientUuid(patientUuid);
        bahmniEncounterTransaction.setVisitUuid(visitUuid);

        Date pastScheduledDateForDrugOrder = new DateTime().minusDays(2).toDate();

        List<EncounterTransaction.DrugOrder> drugOrders = new ArrayList<>();
        drugOrders.add(createETDrugOrder("1ce527b5-d6de-43f0-bc62-4616abacd77e", null, null, pastScheduledDateForDrugOrder));
        bahmniEncounterTransaction.setDrugOrders(drugOrders);

        BahmniEncounterTransaction encounterTransaction = bahmniEncounterTransactionService.save(bahmniEncounterTransaction);

        List<Order> latestOrders = orderService.getActiveOrders(patient, orderService.getOrderTypeByName("Drug Order"),
                orderService.getCareSettingByName("OUTPATIENT"), null);
        Assert.assertEquals(originalOrders.size() + 1, latestOrders.size());
        Assert.assertEquals(Order.Action.NEW, latestOrders.get(originalOrders.size()).getAction());
        Assert.assertEquals(0, encounterTransaction.getDrugOrders().size());

        //Ensure that two encounters are created.
        List<Encounter> encounters = encounterService
                .getEncounters(patient, null, pastScheduledDateForDrugOrder, null, null, null, null, null, null, false);

        Assert.assertEquals(2, encounters.size());
        Assert.assertEquals(1, encounters.get(0).getOrders().size());
        Assert.assertEquals(0, encounters.get(1).getOrders().size());
        Assert.assertEquals(1, encounterTransaction.getObservations().size());
        Assert.assertEquals(obsUuid, encounterTransaction.getObservations().iterator().next().getUuid());

    }

    @Test
    public void shouldSavePastDrugOrdersInEncounterTransactionWhenThereIsNoRetrospectiveVisit() {
        Date obsDate = new Date();
        String obsUuid = UUID.randomUUID().toString();
        String visitUuid = "4e663d66-6b78-11e0-93c3-18a905e044dc";
        String patientUuid = "da7f524f-27ce-4bb2-86d6-6d1d05312bd5";

        Patient patient = patientService.getPatientByUuid(patientUuid);

        EncounterTransaction.Provider provider = new EncounterTransaction.Provider();
        provider.setUuid(Context.getProviderService().getProvider(1).getUuid());
        Set<EncounterTransaction.Provider> providerSet = new HashSet<EncounterTransaction.Provider>();
        providerSet.add(provider);

        BahmniObservation bahmniObservation = createBahmniObservation(obsUuid, "obs-value",
                createConcept("96408258-000b-424e-af1a-403919332938", "FAVORITE FOOD, NON-CODED"), obsDate, null);
        BahmniEncounterTransaction bahmniEncounterTransaction = new BahmniEncounterTransaction();
        bahmniEncounterTransaction.addObservation(bahmniObservation);
        bahmniEncounterTransaction.setVisitTypeUuid("c0c579b0-8e59-401d-8a4a-976a0b183593");
        bahmniEncounterTransaction.setProviders(providerSet);

        bahmniEncounterTransaction.setEncounterTypeUuid("07000be2-26b6-4cce-8b40-866d8435b613");
        bahmniEncounterTransaction.setPatientUuid(patientUuid);
        bahmniEncounterTransaction.setVisitUuid(visitUuid);

        Date pastScheduledDateForDrugOrder = new DateTime().minusYears(12).toDate();

        List<EncounterTransaction.DrugOrder> drugOrders = new ArrayList<>();
        drugOrders.add(createETDrugOrder("1ce527b5-d6de-43f0-bc62-4616abacd77e", null, null, pastScheduledDateForDrugOrder));
        bahmniEncounterTransaction.setDrugOrders(drugOrders);

        BahmniEncounterTransaction encounterTransaction = bahmniEncounterTransactionService.save(bahmniEncounterTransaction);

        //Ensure that two encounters are created.
        List<Encounter> encounters = encounterService
                .getEncounters(patient, null, pastScheduledDateForDrugOrder, pastScheduledDateForDrugOrder, null, null, null,
                        null, null, false);

        Assert.assertEquals(1, encounters.size());
        Assert.assertEquals(1, encounters.get(0).getOrders().size());
        DrugOrder order = (DrugOrder) encounters.get(0).getOrders().iterator().next();
        Assert.assertEquals("1ce527b5-d6de-43f0-bc62-4616abacd77e", order.getDrug().getUuid());
        Assert.assertEquals(1, encounterTransaction.getObservations().size());
        Assert.assertEquals(obsUuid, encounterTransaction.getObservations().iterator().next().getUuid());

    }

    private EncounterTransaction.DrugOrder createETDrugOrder(String drugUuid, String action, String previousOrderUuid,
                                                             Date scheduledDate) {
        EncounterTransaction.Drug encounterTransactionDrug = new EncounterTransaction.Drug();
        encounterTransactionDrug.setUuid(drugUuid);

        EncounterTransaction.DrugOrder drugOrder = new EncounterTransaction.DrugOrder();
        drugOrder.setCareSetting(CareSettingType.OUTPATIENT);
        drugOrder.setAction(action);
        drugOrder.setOrderType("Drug Order");
        drugOrder.setPreviousOrderUuid(previousOrderUuid);
        drugOrder.setDrug(encounterTransactionDrug);
        drugOrder.setDosingInstructionType(
                "org.openmrs.module.bahmniemrapi.drugorder.dosinginstructions.FlexibleDosingInstructions");
        drugOrder.setDuration(10);
        drugOrder.setDurationUnits("Days");

        drugOrder.setScheduledDate(scheduledDate);
        drugOrder.setDateActivated(null);
        drugOrder.setVoided(false);

        EncounterTransaction.DosingInstructions dosingInstructions = new EncounterTransaction.DosingInstructions();
        dosingInstructions.setAdministrationInstructions("{\"instructions\":\"As directed\"}");
        dosingInstructions.setAsNeeded(false);
        dosingInstructions.setDose(1.0);
        dosingInstructions.setDoseUnits("tab (s)");
        dosingInstructions.setFrequency("1/day x 7 days/week");
        dosingInstructions.setNumberOfRefills(0);
        dosingInstructions.setQuantity(10.0);
        dosingInstructions.setQuantityUnits(Context.getConceptService().getConcept(51).getName().getName());
        dosingInstructions.setRoute("UNKNOWN");
        drugOrder.setDosingInstructions(dosingInstructions);

        return drugOrder;
    }

    @Test
    public void shouldSaveBahmniEncounterTransactionWithBahmniObservationsWithGivenUuid() {
        Date obsDate = new Date();
        String obsUuid = UUID.randomUUID().toString();
        String visitUuid = "4e663d66-6b78-11e0-93c3-18a905e044dc";
        String patientUuid = "da7f524f-27ce-4bb2-86d6-6d1d05312bd5";

        BahmniObservation bahmniObservation = createBahmniObservation(obsUuid, "obs-value",
                createConcept("96408258-000b-424e-af1a-403919332938", "FAVORITE FOOD, NON-CODED"), obsDate, null);
        BahmniEncounterTransaction bahmniEncounterTransaction = new BahmniEncounterTransaction();
        bahmniEncounterTransaction.addObservation(bahmniObservation);
        bahmniEncounterTransaction.setVisitTypeUuid("c0c579b0-8e59-401d-8a4a-976a0b183593");

        bahmniEncounterTransaction.setEncounterTypeUuid("07000be2-26b6-4cce-8b40-866d8435b613");
        bahmniEncounterTransaction.setPatientUuid(patientUuid);

        bahmniEncounterTransaction.setVisitUuid(visitUuid);
        BahmniEncounterTransaction encounterTransaction = bahmniEncounterTransactionService.save(bahmniEncounterTransaction);

        assertNotNull(encounterTransaction);
        assertEquals(1, encounterTransaction.getObservations().size());
        assertEquals(bahmniObservation.getValue(), encounterTransaction.getObservations().iterator().next().getValue());
        assertEquals(obsUuid, encounterTransaction.getObservations().iterator().next().getUuid());
        assertEquals("OPD", bahmniEncounterTransaction.getVisitType());
    }

    @Test
    public void shouldCreateANewVisitIfNoActiveVisit() {
        Date obsDate = new Date();
        String obsUuid = UUID.randomUUID().toString();
        String patientUuid = "da7f524f-27ce-4bb2-86d6-6d1d05312bd5";
        String visitType = "OPD";
        Patient patientByUuid = patientService.getPatientByUuid(patientUuid);
        VisitIdentificationHelper visitIdentificationHelper = new VisitIdentificationHelper(visitService);

        BahmniObservation bahmniObservation = createBahmniObservation(obsUuid, "obs-value",
                createConcept("96408258-000b-424e-af1a-403919332938", "FAVORITE FOOD, NON-CODED"), obsDate, null);
        BahmniEncounterTransaction bahmniEncounterTransaction = new BahmniEncounterTransaction();
        bahmniEncounterTransaction.setPatientId("4");
        bahmniEncounterTransaction.setPatientUuid(patientUuid);
        bahmniEncounterTransaction.addObservation(bahmniObservation);
        bahmniEncounterTransaction.setEncounterTypeUuid("02c533ab-b74b-4ee4-b6e5-ffb6d09a0ad1");
        bahmniEncounterTransaction.setVisitType(visitType);

        BahmniEncounterTransaction savedEncounterTransaction = bahmniEncounterTransactionService
                .save(bahmniEncounterTransaction);

        assertNotNull(visitIdentificationHelper.hasActiveVisit(patientByUuid));
        assertNotNull(savedEncounterTransaction);
        assertEquals(savedEncounterTransaction.getObservations().iterator().next().getUuid(), bahmniObservation.getUuid());
    }

    @Test
    public void shouldCreateVisitAttributeOfVisitStatusAsOpdIrrespectiveOfVisitType() {
        BahmniEncounterTransaction bahmniEncounterTransaction = new BahmniEncounterTransaction();
        bahmniEncounterTransaction.setEncounterTypeUuid("07000be2-26b6-4cce-8b40-866d8435b613");
        bahmniEncounterTransaction.setPatientUuid("da7f524f-27ce-4bb2-86d6-6d1d05312bd5");
        bahmniEncounterTransaction.setVisitUuid("1e5d5d48-6b78-11e0-93c3-18a905e044ce");

        BahmniEncounterTransaction savedEncounterTransaction = bahmniEncounterTransactionService
                .save(bahmniEncounterTransaction);

        Visit visit = visitService.getVisitByUuid(savedEncounterTransaction.getVisitUuid());
        assertNotNull(visit);
        assertEquals(1, visit.getAttributes().size());
        assertEquals("OPD", visit.getAttributes().iterator().next().getValue());
    }

    @Test
    public void shouldCreateVisitAttributeOfVisitStatusAsIpdIfTheEncounterIsOfAdmissionType() {
        BahmniEncounterTransaction bahmniEncounterTransaction = new BahmniEncounterTransaction();
        bahmniEncounterTransaction.setEncounterTypeUuid("02c533ab-b74b-4ee4-b6e5-ffb6d09a0ad9");
        bahmniEncounterTransaction.setPatientUuid("da7f524f-27ce-4bb2-86d6-6d1d05312bd5");
        bahmniEncounterTransaction.setVisitUuid("1e5d5d48-6b78-11e0-93c3-18a905e044ce");

        BahmniEncounterTransaction savedEncounterTransaction = bahmniEncounterTransactionService
                .save(bahmniEncounterTransaction);

        Visit visit = visitService.getVisitByUuid(savedEncounterTransaction.getVisitUuid());
        assertNotNull(visit);
        assertEquals(2, visit.getAttributes().size());
        assertEquals("IPD", visit.getAttributes().iterator().next().getValue());
    }

    @Test
    public void shouldCreateVisitAttributeOfAdmissionStatusAsAdmittedIfTheEncounterIsOfAdmissionType() throws Exception {
        BahmniEncounterTransaction bahmniEncounterTransaction = new BahmniEncounterTransaction();
        bahmniEncounterTransaction.setEncounterTypeUuid("02c533ab-b74b-4ee4-b6e5-ffb6d09a0ad9");
        bahmniEncounterTransaction.setPatientUuid("da7f524f-27ce-4bb2-86d6-6d1d05312bd5");
        bahmniEncounterTransaction.setVisitUuid("1e5d5d48-6b78-11e0-93c3-18a905e044ce");

        BahmniEncounterTransaction savedEncounterTransaction = bahmniEncounterTransactionService
                .save(bahmniEncounterTransaction);

        Visit visit = visitService.getVisitByUuid(savedEncounterTransaction.getVisitUuid());
        assertNotNull(visit);
        assertEquals(2, visit.getAttributes().size());
        Iterator<VisitAttribute> visitAttributeIterator = visit.getAttributes().iterator();
        assertEquals("IPD", visitAttributeIterator.next().getValue());
        assertEquals("Admitted", visitAttributeIterator.next().getValue());
    }

    @Test
    public void shouldCreateVisitAttributeOfAdmissionStatusAsDischargedIfTheEncounterIsOfDischargeType() throws Exception {
        BahmniEncounterTransaction bahmniEncounterTransaction = new BahmniEncounterTransaction();
        bahmniEncounterTransaction.setEncounterTypeUuid("02c533ab-b74b-4ee4-b6e5-ffb6d09a0ad0");
        bahmniEncounterTransaction.setPatientUuid("da7f524f-27ce-4bb2-86d6-6d1d05312bd5");
        bahmniEncounterTransaction.setVisitUuid("1e5d5d48-6b78-11e0-93c3-18a905e044ce");

        BahmniEncounterTransaction savedEncounterTransaction = bahmniEncounterTransactionService
                .save(bahmniEncounterTransaction);

        Visit visit = visitService.getVisitByUuid(savedEncounterTransaction.getVisitUuid());
        assertNotNull(visit);
        assertEquals(2, visit.getAttributes().size());
        Iterator<VisitAttribute> visitAttributeIterator = visit.getAttributes().iterator();
        assertEquals("OPD", visitAttributeIterator.next().getValue());
        assertEquals("Discharged", visitAttributeIterator.next().getValue());
    }

    @Test
    public void shouldNotCreateVisitAttributeOfAdmissionStatusIfTheEncounterTypeIsOfOtherThanAdmissionAndDischarged()
            throws Exception {
        BahmniEncounterTransaction bahmniEncounterTransaction = new BahmniEncounterTransaction();
        bahmniEncounterTransaction.setEncounterTypeUuid("07000be2-26b6-4cce-8b40-866d8435b613");
        bahmniEncounterTransaction.setPatientUuid("da7f524f-27ce-4bb2-86d6-6d1d05312bd5");
        bahmniEncounterTransaction.setVisitUuid("1e5d5d48-6b78-11e0-93c3-18a905e044ce");

        BahmniEncounterTransaction savedEncounterTransaction = bahmniEncounterTransactionService
                .save(bahmniEncounterTransaction);

        Visit visit = visitService.getVisitByUuid(savedEncounterTransaction.getVisitUuid());
        assertNotNull(visit);
        assertEquals(1, visit.getAttributes().size());
        Iterator<VisitAttribute> visitAttributeIterator = visit.getAttributes().iterator();
        assertEquals("OPD", visitAttributeIterator.next().getValue());
    }

    @Test
    public void shouldCreateVisitAttributeWhenTheDischargeIsRolledBack() {
        BahmniEncounterTransaction bahmniEncounterTransaction = new BahmniEncounterTransaction();
        bahmniEncounterTransaction.setEncounterTypeUuid("02c533ab-b74b-4ee4-b6e5-ffb6d09a0ad0");//Encounter Type is discharge
        bahmniEncounterTransaction.setPatientUuid("da7f524f-27ce-4bb2-86d6-6d1d05312bd5");
        bahmniEncounterTransaction.setVisitUuid("1e5d5d48-6b78-11e0-93c3-18a905e044ce");
        bahmniEncounterTransaction.setEncounterUuid("bb0af6767-707a-4629-9850-f1529a163ab0");
        bahmniEncounterTransaction.setReason("Undo Discharge");

        bahmniEncounterTransactionService.delete(bahmniEncounterTransaction);

        Encounter encounter = encounterService.getEncounterByUuid("bb0af6767-707a-4629-9850-f1529a163ab0");
        assertTrue(encounter.isVoided());

        Visit visit = visitService.getVisitByUuid("1e5d5d48-6b78-11e0-93c3-18a905e044ce");
        assertNotNull(visit);

        VisitAttribute visitAttribute = getAdmittedVisitAttribute(visit);
        assertNotNull(visitAttribute);
        Assert.assertEquals("Admitted", visitAttribute.getValue());
    }

    private VisitAttribute getAdmittedVisitAttribute(Visit visit) {
        for (VisitAttribute visitAttribute : visit.getAttributes()) {
            if (visitAttribute.getAttributeType().getName().equalsIgnoreCase("Admission Status")) {
                return visitAttribute;
            }
        }
        return null;
    }

    @Test
    public void shouldSaveObsRelationShipWhenBothObservationsAreInSameEncounter() {
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

        EncounterTransaction.Concept srcConcept = createConcept("96408258-000b-424e-af1a-403919332938",
                "FAVORITE FOOD, NON-CODED");
        BahmniObservation srcObs = createBahmniObservation(srcObsUuid, "src-value", srcConcept, obsDate, targetObs);
        bahmniEncounterTransaction.addObservation(srcObs);

        BahmniEncounterTransaction encounterTransaction = bahmniEncounterTransactionService.save(bahmniEncounterTransaction);

        assertEquals(2, encounterTransaction.getObservations().size());

        BahmniObservation savedSrcObs = getObservationByConceptUuid(encounterTransaction.getObservations(),
                srcConcept.getUuid());
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
        BahmniObservation targetObs = createBahmniObservation(targetObsUuid, 175, targetConcept,
                new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.S").parse("2008-08-15 00:00:00.0"), null);

        EncounterTransaction.Concept srcConcept = createConcept("96408258-000b-424e-af1a-403919332938",
                "FAVORITE FOOD, NON-CODED");
        BahmniObservation srcObs = createBahmniObservation(srcObsUuid, "src-value", srcConcept, obsDate, targetObs);

        bahmniEncounterTransaction.addObservation(srcObs);

        BahmniEncounterTransaction mappedBahmniEncounterTransaction = bahmniEncounterTransactionService
                .save(bahmniEncounterTransaction);

        assertEquals(1, mappedBahmniEncounterTransaction.getObservations().size());
        BahmniObservation savedSrcObs = mappedBahmniEncounterTransaction.getObservations().iterator().next();
        assertEquals(srcObs.getValue(), savedSrcObs.getValue());
        assertEquals(srcObsUuid, savedSrcObs.getUuid());
        assertEquals(srcObs.getConcept().getUuid(), savedSrcObs.getConceptUuid());
        assertEquals(targetObs.getValue(), savedSrcObs.getTargetObsRelation().getTargetObs().getValue());
        assertEquals(targetObs.getUuid(), savedSrcObs.getTargetObsRelation().getTargetObs().getUuid());
        assertEquals(targetConcept.getUuid(), savedSrcObs.getTargetObsRelation().getTargetObs().getConceptUuid());
        assertEquals(targetObs.getObservationDateTime(),
                savedSrcObs.getTargetObsRelation().getTargetObs().getObservationDateTime());
    }
    @Test
    public void shouldSavePastDrugOrdersInEncounterTransactionWhenThereIsNoRetrospectiveVisitWithNoVisitTypeUuid() {
        Date obsDate = new Date();
        String obsUuid = UUID.randomUUID().toString();
        String visitUuid = "4e663d66-6b78-11e0-93c3-18a905e044dc";
        String patientUuid = "da7f524f-27ce-4bb2-86d6-6d1d05312bd5";

        Patient patient = patientService.getPatientByUuid(patientUuid);

        EncounterTransaction.Provider provider = new EncounterTransaction.Provider();
        provider.setUuid(Context.getProviderService().getProvider(1).getUuid());
        Set<EncounterTransaction.Provider> providerSet = new HashSet<EncounterTransaction.Provider>();
        providerSet.add(provider);

        BahmniObservation bahmniObservation = createBahmniObservation(obsUuid, "obs-value",
                createConcept("96408258-000b-424e-af1a-403919332938", "FAVORITE FOOD, NON-CODED"), obsDate, null);
        BahmniEncounterTransaction bahmniEncounterTransaction = new BahmniEncounterTransaction();
        bahmniEncounterTransaction.addObservation(bahmniObservation);
        bahmniEncounterTransaction.setVisitType("Hospitalization");
        bahmniEncounterTransaction.setProviders(providerSet);

        bahmniEncounterTransaction.setEncounterTypeUuid("07000be2-26b6-4cce-8b40-866d8435b613");
        bahmniEncounterTransaction.setPatientUuid(patientUuid);
        bahmniEncounterTransaction.setVisitUuid(visitUuid);

        Date pastScheduledDateForDrugOrder = new DateTime().minusYears(12).toDate();

        List<EncounterTransaction.DrugOrder> drugOrders = new ArrayList<>();
        drugOrders.add(createETDrugOrder("1ce527b5-d6de-43f0-bc62-4616abacd77e", null, null, pastScheduledDateForDrugOrder));
        bahmniEncounterTransaction.setDrugOrders(drugOrders);

        BahmniEncounterTransaction encounterTransaction = bahmniEncounterTransactionService.save(bahmniEncounterTransaction);

        //Ensure that two encounters are created.
        List<Encounter> encounters = encounterService
                .getEncounters(patient, null, pastScheduledDateForDrugOrder, pastScheduledDateForDrugOrder, null, null, null,
                        null, null, false);

        Assert.assertEquals(1, encounters.size());
        Assert.assertEquals(1, encounters.get(0).getOrders().size());
        DrugOrder order = (DrugOrder) encounters.get(0).getOrders().iterator().next();
        Assert.assertEquals("1ce527b5-d6de-43f0-bc62-4616abacd77e", order.getDrug().getUuid());
        Assert.assertEquals(1, encounterTransaction.getObservations().size());
        Assert.assertEquals(obsUuid, encounterTransaction.getObservations().iterator().next().getUuid());

    }


    private BahmniObservation getObservationByConceptUuid(Collection<BahmniObservation> bahmniObservations,
                                                          String conceptUuid) {
        for (BahmniObservation bahmniObservation : bahmniObservations) {
            if (conceptUuid.equals(bahmniObservation.getConceptUuid())) {
                return bahmniObservation;
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

    private BahmniObservation createBahmniObservation(String uuid, String value, EncounterTransaction.Concept concept,
                                                      Date obsDate, BahmniObservation bahmniObservation) {
        BahmniObservation bahmniObservation1 = new BahmniObservation();
        bahmniObservation1.setUuid(uuid);
        bahmniObservation1.setValue(value);
        bahmniObservation1.setConcept(concept);
        bahmniObservation1.setComment("comment");
        bahmniObservation1.setObservationDateTime(obsDate);
        bahmniObservation1.setTargetObsRelation(new ObsRelationship(bahmniObservation, null, "qualified-by"));
        return bahmniObservation1;
    }

    private BahmniObservation createBahmniObservation(String uuid, double value, EncounterTransaction.Concept concept,
                                                      Date obsDate, BahmniObservation bahmniObservation) {
        BahmniObservation bahmniObservation1 = new BahmniObservation();
        bahmniObservation1.setUuid(uuid);
        bahmniObservation1.setValue(value);
        bahmniObservation1.setConcept(concept);
        bahmniObservation1.setComment("comment");
        bahmniObservation1.setObservationDateTime(obsDate);
        bahmniObservation1.setTargetObsRelation(new ObsRelationship(bahmniObservation, null, "qualified-by"));
        return bahmniObservation1;
    }
}
