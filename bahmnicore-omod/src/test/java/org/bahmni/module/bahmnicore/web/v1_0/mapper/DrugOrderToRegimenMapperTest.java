package org.bahmni.module.bahmnicore.web.v1_0.mapper;

import org.bahmni.test.builder.ConceptBuilder;
import org.bahmni.test.builder.DrugOrderBuilder;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openmrs.DrugOrder;
import org.openmrs.Order;
import org.openmrs.module.bahmniemrapi.drugogram.contract.Regimen;
import org.openmrs.module.bahmniemrapi.drugogram.contract.RegimenRow;
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
        drugOrderToRegimenMapper = new DrugOrderToRegimenMapper();
    }

    @Test
    public void shouldMapDrugOrdersWhichStartOnSameDateAndEndOnDifferentDateAndCrossEachOther() throws Exception {
        ArrayList<Order> drugOrders = new ArrayList<>();
        Date now = new Date();
        DrugOrder ibeprofen = new DrugOrderBuilder().withDrugName("Ibeprofen").withDateActivated(now).withDose(1000.0).withAutoExpireDate(addDays(now, 5)).withConcept(new ConceptBuilder().withName("Ibeprofen").build()).build();
        DrugOrder paracetemol = new DrugOrderBuilder().withDrugName("Paracetemol").withDateActivated(now).withDose(200.0).withAutoExpireDate(addDays(now, 6)).withConcept(new ConceptBuilder().withName("Paracetemol").build()).build();
        drugOrders.add(ibeprofen);
        drugOrders.add(paracetemol);

        Regimen regimen = drugOrderToRegimenMapper.map(drugOrders);

        assertNotNull(regimen);
        assertEquals(2, regimen.getHeaders().size());
        Iterator<String> headerIterator = regimen.getHeaders().iterator();
        assertEquals("Paracetemol", headerIterator.next());
        assertEquals("Ibeprofen", headerIterator.next());
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
        DrugOrder ibeprofen = new DrugOrderBuilder().withDrugName("Ibeprofen").withDateActivated(now).withDose(1000.0).withAutoExpireDate(addDays(now, 5)).withConcept(new ConceptBuilder().withName("Ibeprofen").build()).build();
        DrugOrder paracetemol = new DrugOrderBuilder().withDrugName("Paracetemol").withDateActivated(now).withDose(200.0).withAutoExpireDate(addDays(now, 5)).withConcept(new ConceptBuilder().withName("Paracetemol").build()).build();
        drugOrders.add(ibeprofen);
        drugOrders.add(paracetemol);

        Regimen regimen = drugOrderToRegimenMapper.map(drugOrders);

        assertNotNull(regimen);
        assertEquals(2, regimen.getHeaders().size());
        Iterator<String> headerIterator = regimen.getHeaders().iterator();
        assertEquals("Paracetemol", headerIterator.next());
        assertEquals("Ibeprofen", headerIterator.next());
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
        DrugOrder ibeprofen = new DrugOrderBuilder().withDrugName("Ibeprofen").withDateActivated(now).withDose(1000.0).withAutoExpireDate(addDays(now, 2)).withConcept(new ConceptBuilder().withName("Ibeprofen").build()).build();
        DrugOrder paracetemol = new DrugOrderBuilder().withDrugName("Paracetemol").withDateActivated(addDays(now, 3)).withDose(200.0).withAutoExpireDate(addDays(now, 5)).withConcept(new ConceptBuilder().withName("Paracetemol").build()).build();
        drugOrders.add(ibeprofen);
        drugOrders.add(paracetemol);

        Regimen regimen = drugOrderToRegimenMapper.map(drugOrders);

        assertNotNull(regimen);
        assertEquals(2, regimen.getHeaders().size());
        Iterator<String> headerIterator = regimen.getHeaders().iterator();
        assertEquals("Paracetemol", headerIterator.next());
        assertEquals("Ibeprofen", headerIterator.next());
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
        DrugOrder ibeprofen = new DrugOrderBuilder().withDrugName("Ibeprofen").withDateActivated(now).withDose(1000.0).withAutoExpireDate(addDays(now, 3)).withConcept(new ConceptBuilder().withName("Ibeprofen").build()).build();
        DrugOrder paracetemol = new DrugOrderBuilder().withDrugName("Paracetemol").withDateActivated(addDays(now, 2)).withDose(200.0).withAutoExpireDate(addDays(now, 5)).withConcept(new ConceptBuilder().withName("Paracetemol").build()).build();
        drugOrders.add(ibeprofen);
        drugOrders.add(paracetemol);

        Regimen regimen = drugOrderToRegimenMapper.map(drugOrders);

        assertNotNull(regimen);
        assertEquals(2, regimen.getHeaders().size());
        Iterator<String> headerIterator = regimen.getHeaders().iterator();
        assertEquals("Paracetemol", headerIterator.next());
        assertEquals("Ibeprofen", headerIterator.next());
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
        DrugOrder ibeprofen = new DrugOrderBuilder().withDrugName("Ibeprofen").withDateActivated(now).withDose(1000.0).withAutoExpireDate(now).withConcept(new ConceptBuilder().withName("Ibeprofen").build()).build();
        drugOrders.add(ibeprofen);

        Regimen regimen = drugOrderToRegimenMapper.map(drugOrders);

        assertNotNull(regimen);
        assertEquals(1, regimen.getHeaders().size());
        Iterator<String> headerIterator = regimen.getHeaders().iterator();
        assertEquals("Ibeprofen", headerIterator.next());
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
    public void shouldMapRevisedDrugOrders() throws Exception {
        ArrayList<Order> drugOrders = new ArrayList<>();
        Date now = new Date();
        DrugOrder ibeprofen = new DrugOrderBuilder().withDrugName("Ibeprofen").withDateActivated(now).withDose(1000.0).withAutoExpireDate(addDays(now, 10)).withOrderAction(Order.Action.NEW).withConcept(new ConceptBuilder().withName("Ibeprofen").build()).build();
        DrugOrder ibeprofenRevised = new DrugOrderBuilder().withDrugName("Ibeprofen").withDateActivated(addDays(now, 5)).withDose(500.0).withAutoExpireDate(addDays(now, 10)).withOrderAction(Order.Action.REVISE).withConcept(new ConceptBuilder().withName("Ibeprofen").build()).build();
        drugOrders.add(ibeprofen);
        drugOrders.add(ibeprofenRevised);

        Regimen regimen = drugOrderToRegimenMapper.map(drugOrders);

        assertNotNull(regimen);
        assertEquals(1, regimen.getHeaders().size());
        Iterator<String> headerIterator = regimen.getHeaders().iterator();
        assertEquals("Ibeprofen", headerIterator.next());
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