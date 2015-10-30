package org.bahmni.module.bahmnicore.web.v1_0.mapper;

import org.bahmni.test.builder.ConceptBuilder;
import org.bahmni.test.builder.DrugOrderBuilder;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openmrs.DrugOrder;
import org.openmrs.Order;
import org.openmrs.api.context.Context;
import org.openmrs.api.context.UserContext;
import org.openmrs.module.bahmniemrapi.drugogram.contract.Regimen;
import org.openmrs.module.bahmniemrapi.drugogram.contract.RegimenRow;
import org.openmrs.module.emrapi.encounter.domain.EncounterTransaction;
import org.openmrs.util.LocaleUtility;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.MockitoAnnotations.initMocks;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.when;

@RunWith(PowerMockRunner.class)
@PrepareForTest({LocaleUtility.class})
public class DrugOrderToRegimenMapperTest {

    private DrugOrderToRegimenMapper drugOrderToRegimenMapper;

    @Before
    public void setUp() throws Exception {
        initMocks(this);
        mockStatic(LocaleUtility.class);
        when(LocaleUtility.getDefaultLocale()).thenReturn(Locale.ENGLISH);
        Context.setUserContext(new UserContext());
        drugOrderToRegimenMapper = new DrugOrderToRegimenMapper();
    }

    @Test
    public void shouldMapDrugOrdersWhichStartOnSameDateAndEndOnDifferentDateAndCrossEachOther() throws Exception {
        ArrayList<Order> drugOrders = new ArrayList<>();
        Date now = new Date();
        DrugOrder ibeprofen = new DrugOrderBuilder().withDrugName("Ibeprofen").withDateActivated(now).withDose(1000.0).withAutoExpireDate(addDays(now, 5)).withConcept(new ConceptBuilder().withName("Ibeprofen").withSet(false).withDataType("N/A").build()).build();
        DrugOrder paracetemol = new DrugOrderBuilder().withDrugName("Paracetemol").withDateActivated(now).withDose(200.0).withAutoExpireDate(addDays(now, 6)).withConcept(new ConceptBuilder().withName("Paracetemol").withSet(false).withDataType("N/A").build()).build();
        drugOrders.add(ibeprofen);
        drugOrders.add(paracetemol);

        Regimen regimen = drugOrderToRegimenMapper.map(drugOrders, null);

        assertNotNull(regimen);
        assertEquals(2, regimen.getHeaders().size());
        Iterator<EncounterTransaction.Concept> headerIterator = regimen.getHeaders().iterator();
        assertEquals("Ibeprofen", headerIterator.next().getName());
        assertEquals("Paracetemol", headerIterator.next().getName());
        assertEquals(3, regimen.getRows().size());
        Iterator<RegimenRow> rowIterator = regimen.getRows().iterator();

        RegimenRow startDateRow = rowIterator.next();
        assertEquals(getOnlyDate(now), startDateRow.getDate());
        assertEquals("1000.0", startDateRow.getDrugs().get("Ibeprofen"));
        assertEquals("200.0", startDateRow.getDrugs().get("Paracetemol"));

        RegimenRow stoppedDateRow = rowIterator.next();
        assertEquals(getOnlyDate(addDays(now, 5)), stoppedDateRow.getDate());
        assertEquals("STOP", stoppedDateRow.getDrugs().get("Ibeprofen"));
        assertEquals("200.0", stoppedDateRow.getDrugs().get("Paracetemol"));

        RegimenRow thirdRow = rowIterator.next();
        assertEquals(getOnlyDate(addDays(now, 6)), thirdRow.getDate());
        assertEquals(null, thirdRow.getDrugs().get("Ibeprofen"));
        assertEquals("STOP", thirdRow.getDrugs().get("Paracetemol"));
    }

    @Test
    public void shouldMapDrugOrdersWhichStartAndEndOnSameDate() throws Exception {
        ArrayList<Order> drugOrders = new ArrayList<>();
        Date now = new Date();
        DrugOrder ibeprofen = new DrugOrderBuilder().withDrugName("Ibeprofen").withDateActivated(now).withDose(1000.0).withAutoExpireDate(addDays(now, 5)).withConcept(new ConceptBuilder().withName("Ibeprofen").withSet(false).withDataType("N/A").build()).build();
        DrugOrder paracetemol = new DrugOrderBuilder().withDrugName("Paracetemol").withDateActivated(now).withDose(200.0).withAutoExpireDate(addDays(now, 5)).withConcept(new ConceptBuilder().withName("Paracetemol").withSet(false).withDataType("N/A").build()).build();
        drugOrders.add(ibeprofen);
        drugOrders.add(paracetemol);

        Regimen regimen = drugOrderToRegimenMapper.map(drugOrders, null);

        assertNotNull(regimen);
        assertEquals(2, regimen.getHeaders().size());
        Iterator<EncounterTransaction.Concept> headerIterator = regimen.getHeaders().iterator();
        assertEquals("Ibeprofen", headerIterator.next().getName());
        assertEquals("Paracetemol", headerIterator.next().getName());
        assertEquals(2, regimen.getRows().size());
        Iterator<RegimenRow> rowIterator = regimen.getRows().iterator();

        RegimenRow startDateRow = rowIterator.next();
        assertEquals(getOnlyDate(now), startDateRow.getDate());
        assertEquals("1000.0", startDateRow.getDrugs().get("Ibeprofen"));
        assertEquals("200.0", startDateRow.getDrugs().get("Paracetemol"));

        RegimenRow stoppedDateRow = rowIterator.next();
        assertEquals(getOnlyDate(addDays(now, 5)), stoppedDateRow.getDate());
        assertEquals("STOP", stoppedDateRow.getDrugs().get("Ibeprofen"));
        assertEquals("STOP", stoppedDateRow.getDrugs().get("Paracetemol"));
    }

    @Test
    public void shouldMapDrugOrdersWhichStartAndEndOnDifferentDateDoesntOverlap() throws Exception {
        ArrayList<Order> drugOrders = new ArrayList<>();
        Date now = new Date();
        DrugOrder ibeprofen = new DrugOrderBuilder().withDrugName("Ibeprofen").withDateActivated(now).withDose(1000.0).withAutoExpireDate(addDays(now, 2)).withConcept(new ConceptBuilder().withName("Ibeprofen").withSet(false).withDataType("N/A").build()).build();
        DrugOrder paracetemol = new DrugOrderBuilder().withDrugName("Paracetemol").withDateActivated(addDays(now, 3)).withDose(200.0).withAutoExpireDate(addDays(now, 5)).withConcept(new ConceptBuilder().withName("Paracetemol").withSet(false).withDataType("N/A").build()).build();
        drugOrders.add(ibeprofen);
        drugOrders.add(paracetemol);

        Regimen regimen = drugOrderToRegimenMapper.map(drugOrders, null);

        assertNotNull(regimen);
        assertEquals(2, regimen.getHeaders().size());
        Iterator<EncounterTransaction.Concept> headerIterator = regimen.getHeaders().iterator();
        assertEquals("Ibeprofen", headerIterator.next().getName());
        assertEquals("Paracetemol", headerIterator.next().getName());
        assertEquals(4, regimen.getRows().size());
        Iterator<RegimenRow> rowIterator = regimen.getRows().iterator();

        RegimenRow startDateRow = rowIterator.next();
        assertEquals(getOnlyDate(now), startDateRow.getDate());
        assertEquals("1000.0", startDateRow.getDrugs().get("Ibeprofen"));
        assertEquals(null, startDateRow.getDrugs().get("Paracetemol"));

        RegimenRow stoppedDateRow = rowIterator.next();
        assertEquals(getOnlyDate(addDays(now, 2)), stoppedDateRow.getDate());
        assertEquals("STOP", stoppedDateRow.getDrugs().get("Ibeprofen"));
        assertEquals(null, stoppedDateRow.getDrugs().get("Paracetemol"));

        RegimenRow thirdRow = rowIterator.next();
        assertEquals(getOnlyDate(addDays(now, 3)), thirdRow.getDate());
        assertEquals(null, thirdRow.getDrugs().get("Ibeprofen"));
        assertEquals("200.0", thirdRow.getDrugs().get("Paracetemol"));

        RegimenRow fourthRow = rowIterator.next();
        assertEquals(getOnlyDate(addDays(now, 5)), fourthRow.getDate());
        assertEquals(null, fourthRow.getDrugs().get("Ibeprofen"));
        assertEquals("STOP", fourthRow.getDrugs().get("Paracetemol"));
    }

    @Test
    public void shouldMapDrugOrdersWhichStartAndEndOnDifferentDateAndOverlaps() throws Exception {
        ArrayList<Order> drugOrders = new ArrayList<>();
        Date now = new Date();
        DrugOrder ibeprofen = new DrugOrderBuilder().withDrugName("Ibeprofen").withDateActivated(now).withDose(1000.0).withAutoExpireDate(addDays(now, 3)).withConcept(new ConceptBuilder().withName("Ibeprofen").withSet(false).withDataType("N/A").build()).build();
        DrugOrder paracetemol = new DrugOrderBuilder().withDrugName("Paracetemol").withDateActivated(addDays(now, 2)).withDose(200.0).withAutoExpireDate(addDays(now, 5)).withConcept(new ConceptBuilder().withName("Paracetemol").withSet(false).withDataType("N/A").build()).build();
        drugOrders.add(ibeprofen);
        drugOrders.add(paracetemol);

        Regimen regimen = drugOrderToRegimenMapper.map(drugOrders, null);

        assertNotNull(regimen);
        assertEquals(2, regimen.getHeaders().size());
        Iterator<EncounterTransaction.Concept> headerIterator = regimen.getHeaders().iterator();
        assertEquals("Ibeprofen", headerIterator.next().getName());
        assertEquals("Paracetemol", headerIterator.next().getName());
        assertEquals(4, regimen.getRows().size());
        Iterator<RegimenRow> rowIterator = regimen.getRows().iterator();

        RegimenRow startDateRow = rowIterator.next();
        assertEquals(getOnlyDate(now), startDateRow.getDate());
        assertEquals("1000.0", startDateRow.getDrugs().get("Ibeprofen"));
        assertEquals(null, startDateRow.getDrugs().get("Paracetemol"));

        RegimenRow stoppedDateRow = rowIterator.next();
        assertEquals(getOnlyDate(addDays(now, 2)), stoppedDateRow.getDate());
        assertEquals("1000.0", stoppedDateRow.getDrugs().get("Ibeprofen"));
        assertEquals("200.0", stoppedDateRow.getDrugs().get("Paracetemol"));

        RegimenRow thirdRow = rowIterator.next();
        assertEquals(getOnlyDate(addDays(now, 3)), thirdRow.getDate());
        assertEquals("STOP", thirdRow.getDrugs().get("Ibeprofen"));
        assertEquals("200.0", thirdRow.getDrugs().get("Paracetemol"));

        RegimenRow fourthRow = rowIterator.next();
        assertEquals(getOnlyDate(addDays(now, 5)), fourthRow.getDate());
        assertEquals(null, fourthRow.getDrugs().get("Ibeprofen"));
        assertEquals("STOP", fourthRow.getDrugs().get("Paracetemol"));
    }

    @Test
    public void shouldMapTo2RowsIfTheDrugIsStartedAndStoppedOnTheSameDay() throws Exception {
        ArrayList<Order> drugOrders = new ArrayList<>();
        Date now = new Date();
        DrugOrder ibeprofen = new DrugOrderBuilder().withDrugName("Ibeprofen").withDateActivated(now).withDose(1000.0).withAutoExpireDate(now).withConcept(new ConceptBuilder().withName("Ibeprofen").withSet(false).withDataType("N/A").build()).build();
        drugOrders.add(ibeprofen);

        Regimen regimen = drugOrderToRegimenMapper.map(drugOrders, null);

        assertNotNull(regimen);
        assertEquals(1, regimen.getHeaders().size());
        Iterator<EncounterTransaction.Concept> headerIterator = regimen.getHeaders().iterator();
        assertEquals("Ibeprofen", headerIterator.next().getName());
        assertEquals(2, regimen.getRows().size());
        Iterator<RegimenRow> rowIterator = regimen.getRows().iterator();

        RegimenRow startDateRow = rowIterator.next();
        assertEquals(getOnlyDate(now), startDateRow.getDate());
        assertEquals("1000.0", startDateRow.getDrugs().get("Ibeprofen"));

        RegimenRow stoppedDateRow = rowIterator.next();
        assertEquals(getOnlyDate(now), stoppedDateRow.getDate());
        assertEquals("STOP", stoppedDateRow.getDrugs().get("Ibeprofen"));
    }

    @Test
    public void shouldMapTo2RowsIf2DrugsAreStartedAndStoppedOnTheSameDay() throws Exception {
        ArrayList<Order> drugOrders = new ArrayList<>();
        Date now = new Date();
        DrugOrder ibeprofen = new DrugOrderBuilder().withDrugName("Ibeprofen").withDateActivated(now).withDose(1000.0).withAutoExpireDate(now).withConcept(new ConceptBuilder().withName("Ibeprofen").withSet(false).withDataType("N/A").build()).build();
        DrugOrder paracetamol = new DrugOrderBuilder().withDrugName("Paracetamol").withDateActivated(now).withDose(500.0).withAutoExpireDate(now).withConcept(new ConceptBuilder().withName("Paracetamol").withSet(false).withDataType("N/A").build()).build();
        DrugOrder lignocaine = new DrugOrderBuilder().withDrugName("Lignocaine").withDateActivated(now).withDose(300.0).withAutoExpireDate(addDays(now, 3)).withConcept(new ConceptBuilder().withName("Lignocaine").withSet(false).withDataType("N/A").build()).build();
        DrugOrder magnesium = new DrugOrderBuilder().withDrugName("Magnesium").withDateActivated(now).withDose(5000.0).withAutoExpireDate(addDays(now, 10)).withConcept(new ConceptBuilder().withName("Magnesium").withSet(false).withDataType("N/A").build()).build();
        drugOrders.add(ibeprofen);
        drugOrders.add(paracetamol);
        drugOrders.add(lignocaine);
        drugOrders.add(magnesium);

        Regimen regimen = drugOrderToRegimenMapper.map(drugOrders, null);

        assertNotNull(regimen);
        assertEquals(4, regimen.getHeaders().size());
        Iterator<EncounterTransaction.Concept> headerIterator = regimen.getHeaders().iterator();
        assertEquals("Ibeprofen", headerIterator.next().getName());
        assertEquals("Paracetamol", headerIterator.next().getName());
        assertEquals("Lignocaine", headerIterator.next().getName());
        assertEquals("Magnesium", headerIterator.next().getName());
        assertEquals(4, regimen.getRows().size());
        Iterator<RegimenRow> rowIterator = regimen.getRows().iterator();

        RegimenRow firstRow = rowIterator.next();
        assertEquals(getOnlyDate(now), firstRow.getDate());
        assertEquals("1000.0", firstRow.getDrugs().get("Ibeprofen"));
        assertEquals("500.0", firstRow.getDrugs().get("Paracetamol"));
        assertEquals("300.0", firstRow.getDrugs().get("Lignocaine"));
        assertEquals("5000.0", firstRow.getDrugs().get("Magnesium"));

        RegimenRow secondRow = rowIterator.next();
        assertEquals(getOnlyDate(now), secondRow.getDate());
        assertEquals("STOP", secondRow.getDrugs().get("Ibeprofen"));
        assertEquals("STOP", secondRow.getDrugs().get("Paracetamol"));
        assertEquals("300.0", secondRow.getDrugs().get("Lignocaine"));
        assertEquals("5000.0", secondRow.getDrugs().get("Magnesium"));

        RegimenRow thirdRow = rowIterator.next();
        assertEquals(getOnlyDate(addDays(now, 3)), thirdRow.getDate());
        assertEquals(null, thirdRow.getDrugs().get("Ibeprofen"));
        assertEquals(null, thirdRow.getDrugs().get("Paracetamol"));
        assertEquals("STOP", thirdRow.getDrugs().get("Lignocaine"));
        assertEquals("5000.0", thirdRow.getDrugs().get("Magnesium"));

        RegimenRow fourthRow = rowIterator.next();
        assertEquals(getOnlyDate(addDays(now, 10)), fourthRow.getDate());
        assertEquals(null, fourthRow.getDrugs().get("Ibeprofen"));
        assertEquals(null, fourthRow.getDrugs().get("Paracetamol"));
        assertEquals(null, fourthRow.getDrugs().get("Lignocaine"));
        assertEquals("STOP", fourthRow.getDrugs().get("Magnesium"));
    }

    @Test
    public void shouldMapRevisedDrugOrders() throws Exception {
        ArrayList<Order> drugOrders = new ArrayList<>();
        Date now = new Date();
        DrugOrder ibeprofen = new DrugOrderBuilder().withDrugName("Ibeprofen").withDateActivated(now).withDose(1000.0).withAutoExpireDate(addDays(now, 10)).withOrderAction(Order.Action.NEW).withConcept(new ConceptBuilder().withName("Ibeprofen").withUUID("uuid").withSet(false).withDataType("N/A").build()).build();
        DrugOrder ibeprofenRevised = new DrugOrderBuilder().withDrugName("Ibeprofen").withDateActivated(addDays(now, 5)).withDose(500.0).withAutoExpireDate(addDays(now, 10)).withOrderAction(Order.Action.REVISE).withConcept(new ConceptBuilder().withName("Ibeprofen").withUUID("uuid").withSet(false).withDataType("N/A").build()).build();
        drugOrders.add(ibeprofen);
        drugOrders.add(ibeprofenRevised);

        Regimen regimen = drugOrderToRegimenMapper.map(drugOrders, null);

        assertNotNull(regimen);
        assertEquals(1, regimen.getHeaders().size());
        Iterator<EncounterTransaction.Concept> headerIterator = regimen.getHeaders().iterator();
        assertEquals("Ibeprofen", headerIterator.next().getName());
        assertEquals(3, regimen.getRows().size());
        Iterator<RegimenRow> rowIterator = regimen.getRows().iterator();

        RegimenRow startDateRow = rowIterator.next();
        assertEquals(getOnlyDate(now), startDateRow.getDate());
        assertEquals("1000.0", startDateRow.getDrugs().get("Ibeprofen"));

        RegimenRow revisedDateRow = rowIterator.next();
        assertEquals(getOnlyDate(addDays(now, 5)), revisedDateRow.getDate());
        assertEquals("500.0", revisedDateRow.getDrugs().get("Ibeprofen"));

        RegimenRow stoppedDateRow = rowIterator.next();
        assertEquals(getOnlyDate(addDays(now, 10)), stoppedDateRow.getDate());
        assertEquals("STOP", stoppedDateRow.getDrugs().get("Ibeprofen"));
    }

    @Test
    public void shouldMapScheduledDrugOrders() throws Exception {
        ArrayList<Order> drugOrders = new ArrayList<>();
        Date now = new Date();
        DrugOrder pmg = new DrugOrderBuilder().withDrugName("P 500mg").withDateActivated(now).withDose(1000.0).withAutoExpireDate(now).withOrderAction(Order.Action.NEW).withConcept(new ConceptBuilder().withName("P 500mg").withUUID("P 500mg uuid").withSet(false).withDataType("N/A").build()).build();
        DrugOrder caffeine = new DrugOrderBuilder().withDrugName("Caffeine").withScheduledDate(now).withDose(500.0).withAutoExpireDate(addDays(now, 3)).withOrderAction(Order.Action.NEW).withConcept(new ConceptBuilder().withName("Caffeine").withUUID("Caffeine uuid").withSet(false).withDataType("N/A").build()).build();
        DrugOrder lajvanti = new DrugOrderBuilder().withDrugName("Lajvanti").withScheduledDate(addDays(now, 2)).withDose(3.0).withAutoExpireDate(addDays(now, 5)).withOrderAction(Order.Action.NEW).withConcept(new ConceptBuilder().withName("Lajvanti").withUUID("Lajvanti uuid").withSet(false).withDataType("N/A").build()).build();
        drugOrders.add(pmg);
        drugOrders.add(caffeine);
        drugOrders.add(lajvanti);

        Regimen regimen = drugOrderToRegimenMapper.map(drugOrders, null);

        assertNotNull(regimen);
        assertEquals(3, regimen.getHeaders().size());
        Iterator<EncounterTransaction.Concept> headerIterator = regimen.getHeaders().iterator();
        assertEquals("P 500mg", headerIterator.next().getName());
        assertEquals("Caffeine", headerIterator.next().getName());
        assertEquals("Lajvanti", headerIterator.next().getName());
        assertEquals(5, regimen.getRows().size());
        Iterator<RegimenRow> rowIterator = regimen.getRows().iterator();

        RegimenRow firstRow = rowIterator.next();
        assertEquals(getOnlyDate(now), firstRow.getDate());
        assertEquals("1000.0", firstRow.getDrugs().get("P 500mg"));
        assertEquals("500.0", firstRow.getDrugs().get("Caffeine"));
        assertEquals(null, firstRow.getDrugs().get("Lajvanti"));

        RegimenRow secondRow = rowIterator.next();
        assertEquals(getOnlyDate(now), secondRow.getDate());
        assertEquals("STOP", secondRow.getDrugs().get("P 500mg"));
        assertEquals("500.0", secondRow.getDrugs().get("Caffeine"));
        assertEquals(null, secondRow.getDrugs().get("Lajvanti"));

        RegimenRow thirdRow = rowIterator.next();
        assertEquals(getOnlyDate(addDays(now, 2)), thirdRow.getDate());
        assertEquals(null, thirdRow.getDrugs().get("P 500mg"));
        assertEquals("500.0", thirdRow.getDrugs().get("Caffeine"));
        assertEquals("3.0", thirdRow.getDrugs().get("Lajvanti"));

        RegimenRow fourthRow = rowIterator.next();
        assertEquals(getOnlyDate(addDays(now, 3)), fourthRow.getDate());
        assertEquals(null, fourthRow.getDrugs().get("P 500mg"));
        assertEquals("STOP", fourthRow.getDrugs().get("Caffeine"));
        assertEquals("3.0", fourthRow.getDrugs().get("Lajvanti"));

        RegimenRow fifthRow = rowIterator.next();
        assertEquals(getOnlyDate(addDays(now, 5)), fifthRow.getDate());
        assertEquals(null, fifthRow.getDrugs().get("P 500mg"));
        assertEquals(null, fifthRow.getDrugs().get("Caffeine"));
        assertEquals("STOP", fifthRow.getDrugs().get("Lajvanti"));
    }

    @Test
    public void shouldRetrieveIfTheDrugStartedAndStoppedOnTheSameDayLiesBetweenOtherDrug() throws Exception {
        ArrayList<Order> drugOrders = new ArrayList<>();
        Date now = new Date();
        DrugOrder pmg = new DrugOrderBuilder().withDrugName("P 500mg").withDateActivated(now).withDose(1000.0).withAutoExpireDate(addDays(now, 2)).withOrderAction(Order.Action.NEW).withConcept(new ConceptBuilder().withName("P 500mg").withUUID("P 500mg uuid").withSet(false).withDataType("N/A").build()).build();
        DrugOrder revisedPmg = new DrugOrderBuilder().withDrugName("P 500mg").withDateActivated(addDays(now, 2)).withDose(10.0).withAutoExpireDate(addDays(now, 10)).withOrderAction(Order.Action.REVISE).withConcept(new ConceptBuilder().withName("P 500mg").withUUID("P 500mg uuid").withSet(false).withDataType("N/A").build()).build();
        DrugOrder caffeine = new DrugOrderBuilder().withDrugName("Caffeine").withDateActivated(addDays(now, 2)).withDose(600.0).withAutoExpireDate(addDays(now, 2)).withOrderAction(Order.Action.NEW).withConcept(new ConceptBuilder().withName("Caffeine").withUUID("Caffeine uuid").withSet(false).withDataType("N/A").build()).build();
        drugOrders.add(pmg);
        drugOrders.add(revisedPmg);
        drugOrders.add(caffeine);

        Regimen regimen = drugOrderToRegimenMapper.map(drugOrders, null);

        assertNotNull(regimen);
        assertEquals(2, regimen.getHeaders().size());
        Iterator<EncounterTransaction.Concept> headerIterator = regimen.getHeaders().iterator();
        assertEquals("Caffeine", headerIterator.next().getName());
        assertEquals("P 500mg", headerIterator.next().getName());
        assertEquals(4, regimen.getRows().size());
        Iterator<RegimenRow> rowIterator = regimen.getRows().iterator();

        RegimenRow firstRow = rowIterator.next();
        assertEquals(getOnlyDate(now), firstRow.getDate());
        assertEquals("1000.0", firstRow.getDrugs().get("P 500mg"));
        assertEquals(null, firstRow.getDrugs().get("Caffeine"));

        RegimenRow secondRow = rowIterator.next();
        assertEquals(getOnlyDate(addDays(now, 2)), secondRow.getDate());
        assertEquals("10.0", secondRow.getDrugs().get("P 500mg"));
        assertEquals("600.0", secondRow.getDrugs().get("Caffeine"));

        RegimenRow thirdRow = rowIterator.next();
        assertEquals(getOnlyDate(addDays(now, 2)), thirdRow.getDate());
        assertEquals("10.0", thirdRow.getDrugs().get("P 500mg"));
        assertEquals("STOP", thirdRow.getDrugs().get("Caffeine"));

        RegimenRow fourthRow = rowIterator.next();
        assertEquals(getOnlyDate(addDays(now, 10)), fourthRow.getDate());
        assertEquals("STOP", fourthRow.getDrugs().get("P 500mg"));
        assertEquals(null, fourthRow.getDrugs().get("Caffeine"));
    }

    private Date addDays(Date now, int days) {
        Calendar c = Calendar.getInstance();
        c.setTime(now);
        c.add(Calendar.DATE, days);
        return c.getTime();
    }

    public Date getOnlyDate(Date date) throws ParseException {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        return sdf.parse(sdf.format(date));
    }

}