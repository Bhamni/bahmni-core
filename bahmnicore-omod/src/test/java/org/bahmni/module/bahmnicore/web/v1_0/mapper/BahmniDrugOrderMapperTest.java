package org.bahmni.module.bahmnicore.web.v1_0.mapper;

import org.apache.commons.lang3.time.DateUtils;
import org.bahmni.test.builder.DrugOrderBuilder;
import org.bahmni.test.builder.EncounterBuilder;
import org.bahmni.test.builder.PersonBuilder;
import org.bahmni.test.builder.VisitBuilder;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.openmrs.*;
import org.openmrs.api.AdministrationService;
import org.openmrs.module.bahmniemrapi.drugorder.contract.BahmniDrugOrder;
import org.openmrs.module.bahmniemrapi.drugorder.dosinginstructions.FlexibleDosingInstructions;
import org.openmrs.module.bahmniemrapi.drugorder.mapper.BahmniDrugOrderMapper;
import org.openmrs.module.bahmniemrapi.drugorder.mapper.BahmniProviderMapper;
import org.openmrs.module.bahmniemrapi.drugorder.mapper.OrderAttributesMapper;
import org.openmrs.module.emrapi.encounter.ConceptMapper;
import org.openmrs.module.emrapi.encounter.domain.EncounterTransaction;
import org.openmrs.util.LocaleUtility;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.verify;
import static org.powermock.api.mockito.PowerMockito.when;

@PrepareForTest(LocaleUtility.class)
@RunWith(PowerMockRunner.class)
public class BahmniDrugOrderMapperTest {

    @Mock
    private AdministrationService administrationService;

    @Mock
    private BahmniProviderMapper providerMapper;

    @Mock
    private ConceptMapper conceptMapper;

    @Mock
    private OrderAttributesMapper orderAttributesMapper;

    @Mock
    private Concept reasonConcept;

    @Mock
    private EncounterTransaction.Concept reasonETConcept;
    private BahmniDrugOrderMapper bahmniDrugOrderMapper;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);

        PowerMockito.mockStatic(LocaleUtility.class);
        when(LocaleUtility.getLocalesInOrder()).thenReturn(new HashSet<Locale>(Arrays.asList(Locale.getDefault())));
        when(providerMapper.map(null)).thenReturn(null);
        bahmniDrugOrderMapper = new BahmniDrugOrderMapper();
        bahmniDrugOrderMapper.setMappers(providerMapper,orderAttributesMapper, conceptMapper);
    }


    @Test
    public void shouldMapToResponseForFreeTextOrderDetails() throws Exception {
        DrugOrderBuilder drugBuilder = new DrugOrderBuilder();
        Date visitDate, dateActivated;
        visitDate = dateActivated = new Date();
        Date dateScheduled = DateUtils.addDays(dateActivated, 2);
        Date expireDate = DateUtils.addDays(dateActivated, 20);


        Person person = new PersonBuilder().withUUID("puuid").build();
        Encounter encounter = new EncounterBuilder().build();
        Visit visit = new VisitBuilder().withPerson(person).withUUID("vuuid").withStartDatetime(visitDate).withEncounter(
                encounter).build();

        DrugOrder drugOrder1 = drugBuilder.withDrugName("Paracetamol 120mg/5ml 60ml")
                .withDosingType(FlexibleDosingInstructions.class)
                .withDrugForm("Capsule")
                .withScheduledDate(dateScheduled)
                .withDateActivated(dateActivated)
                .withDurationUnits("Week")
                .withDosingInstructions("{\"dose\": \"2.0\", \"doseUnits\": \"Tablet\"}")
                .withVisit(visit)
                .withDuration(18)
                .withAutoExpireDate(expireDate)
                .withCreator("testPersonName")
                .build();

        List<DrugOrder> drugOrderList = new ArrayList<>();
        drugOrderList.add(drugOrder1);

        List<BahmniDrugOrder> mappedDrugOrders = bahmniDrugOrderMapper.mapToResponse(drugOrderList, null, new HashMap<String, DrugOrder>());
        assertEquals(1, mappedDrugOrders.size());
        BahmniDrugOrder mappedOrder = mappedDrugOrders.get(0);
        EncounterTransaction.DosingInstructions dosingInstructions = mappedOrder.getDosingInstructions();

        assertEquals("Paracetamol 120mg/5ml 60ml", mappedOrder.getDrug().getName());
        assertEquals("Capsule", mappedOrder.getDrug().getForm());
        assertEquals(dateScheduled, mappedOrder.getEffectiveStartDate());
        assertEquals(expireDate, mappedOrder.getEffectiveStopDate());
        assertEquals(18, mappedOrder.getDuration(), 0);
        assertEquals("Week", mappedOrder.getDurationUnits());
        assertEquals("vuuid", mappedOrder.getVisit().getUuid());
        assertEquals("{\"dose\": \"2.0\", \"doseUnits\": \"Tablet\"}",mappedOrder.getDosingInstructions().getAdministrationInstructions());
        assertEquals(visitDate, mappedOrder.getVisit().getStartDateTime());
        verify(providerMapper);
    }

    @Test
    public void shouldMapToResponseForSimpleOrderDetails() throws Exception {
        DrugOrderBuilder drugBuilder = new DrugOrderBuilder();

        Date dateActivated, visitDate;
        dateActivated = visitDate = new Date();
        Date dateScheduled = DateUtils.addDays(dateActivated, 2);
        Date expireDate = DateUtils.addDays(dateActivated, 20);
        Person person = new PersonBuilder().withUUID("puuid").build();
        Encounter encounter = new EncounterBuilder().build();
        Visit visit = new VisitBuilder().withPerson(person).withUUID("vuuid").withStartDatetime(visitDate).withEncounter(encounter).build();

        int duration = 2;
        String dosingInstructions = "{\"instructions\": \"Before meals\", \"additionalInstructions\": \"Take before waking up\"}";
        DrugOrder drugOrder1 = drugBuilder.withDrugName("Paracetamol 120mg/5ml 60ml")
                .withDosingType(SimpleDosingInstructions.class)
                .withDosingInstructions(dosingInstructions)
                .withDrugForm("Tablet")
                .withDateActivated(dateActivated)
                .withDuration(duration)
                .withDurationUnits("Week")
                .withDose(2.0)
                .withVisit(visit)
                .withFrequency("Once a day")
                .withRoute("Orally")
                .withAutoExpireDate(expireDate)
                .withDoseUnits("Capsule")
                .withCreator("testPersonName")
                .build();


        List<DrugOrder> drugOrderList = new ArrayList<>();
        drugOrderList.add(drugOrder1);

        List<BahmniDrugOrder> mappedDrugOrders = bahmniDrugOrderMapper.mapToResponse(drugOrderList, null, new HashMap<String, DrugOrder>());
        assertEquals(1, mappedDrugOrders.size());
        BahmniDrugOrder mappedOrder = mappedDrugOrders.get(0);

        assertEquals("Paracetamol 120mg/5ml 60ml", mappedOrder.getDrug().getName());
        assertEquals("Tablet", mappedOrder.getDrug().getForm());
        assertEquals(2.0, mappedOrder.getDosingInstructions().getDose(), 0);
        assertEquals("Capsule", mappedOrder.getDosingInstructions().getDoseUnits());
        assertEquals(dateActivated, mappedOrder.getEffectiveStartDate());
        assertEquals(expireDate, mappedOrder.getEffectiveStopDate());
        assertEquals(duration, mappedOrder.getDuration(), 0);
        assertEquals("Week", mappedOrder.getDurationUnits());
        assertEquals(dosingInstructions, mappedOrder.getDosingInstructions().getAdministrationInstructions());
        assertEquals("Once a day", mappedOrder.getDosingInstructions().getFrequency());
        assertEquals("Orally", mappedOrder.getDosingInstructions().getRoute());
        assertEquals("vuuid", mappedOrder.getVisit().getUuid());
        assertEquals(visitDate, mappedOrder.getVisit().getStartDateTime());
        verify(providerMapper);
    }

    @Test
    public void shouldFillDrugOrderReasonTextAndReasonConcept() throws Exception {

        Date dateActivated, visitDate;
        dateActivated = visitDate = new Date();
        Date dateScheduled = DateUtils.addDays(dateActivated, 2);
        Date expireDate = DateUtils.addDays(dateActivated, 20);
        Person person = new PersonBuilder().withUUID("puuid").build();
        Encounter encounter = new EncounterBuilder().build();
        Visit visit = new VisitBuilder().withPerson(person).withUUID("vuuid").withStartDatetime(visitDate).withEncounter(encounter).build();

        int duration = 2;
        String dosingInstructions = "{\"instructions\": \"Before meals\", \"additionalInstructions\": \"Take before waking up\"}";
        DrugOrder drugOrder1 = new DrugOrderBuilder().withDrugName("Paracetamol 120mg/5ml 60ml")
                .withDosingType(SimpleDosingInstructions.class)
                .withDosingInstructions(dosingInstructions)
                .withDrugForm("Tablet")
                .withDateActivated(dateActivated)
                .withDuration(duration)
                .withDurationUnits("Week")
                .withDose(2.0)
                .withVisit(visit)
                .withFrequency("Once a day")
                .withRoute("Orally")
                .withAutoExpireDate(expireDate)
                .withDoseUnits("Capsule")
                .withCreator("testPersonName")
                .withOrderAction(Order.Action.NEW)
                .withOrderNumber("1234")
                .build();

        DrugOrder drugOrderDiscontinued = new DrugOrderBuilder().withDrugName("Paracetamol 120mg/5ml 60ml")
                .withDosingType(SimpleDosingInstructions.class)
                .withDosingInstructions(dosingInstructions)
                .withDrugForm("Tablet")
                .withDateActivated(dateActivated)
                .withDuration(duration)
                .withDurationUnits("Week")
                .withDose(2.0)
                .withVisit(visit)
                .withFrequency("Once a day")
                .withRoute("Orally")
                .withAutoExpireDate(expireDate)
                .withDoseUnits("Capsule")
                .withCreator("testPersonName")
                .withOrderAction(Order.Action.DISCONTINUE)
                .withPreviousOrder(drugOrder1)
                .build();

        drugOrderDiscontinued.setOrderReason(reasonConcept);
        drugOrderDiscontinued.setOrderReasonNonCoded("AEID1234");

        when(conceptMapper.map(reasonConcept)).thenReturn(reasonETConcept);

        List<DrugOrder> drugOrderList = new ArrayList<>();
        drugOrderList.add(drugOrder1);

        Map<String,DrugOrder> discontinuedOrders = new HashMap<>();
        discontinuedOrders.put("1234", drugOrderDiscontinued);

        List<BahmniDrugOrder> mappedDrugOrders = bahmniDrugOrderMapper.mapToResponse(drugOrderList, null, discontinuedOrders);
        assertEquals(1, mappedDrugOrders.size());
        BahmniDrugOrder mappedOrder = mappedDrugOrders.get(0);

        assertEquals("Paracetamol 120mg/5ml 60ml", mappedOrder.getDrug().getName());
        assertEquals("Tablet", mappedOrder.getDrug().getForm());
        assertEquals(2.0, mappedOrder.getDosingInstructions().getDose(), 0);
        assertEquals("Capsule", mappedOrder.getDosingInstructions().getDoseUnits());
        assertEquals(dateActivated, mappedOrder.getEffectiveStartDate());
        assertEquals(expireDate, mappedOrder.getEffectiveStopDate());
        assertEquals(duration, mappedOrder.getDuration(), 0);
        assertEquals("Week", mappedOrder.getDurationUnits());
        assertEquals(dosingInstructions, mappedOrder.getDosingInstructions().getAdministrationInstructions());
        assertEquals("Once a day", mappedOrder.getDosingInstructions().getFrequency());
        assertEquals("Orally", mappedOrder.getDosingInstructions().getRoute());
        assertEquals("vuuid", mappedOrder.getVisit().getUuid());
        assertEquals(visitDate, mappedOrder.getVisit().getStartDateTime());
        assertEquals(reasonETConcept,mappedOrder.getOrderReasonConcept());
        assertEquals("AEID1234",mappedOrder.getOrderReasonText());
        verify(providerMapper);
    }
}
