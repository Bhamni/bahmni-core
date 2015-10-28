package org.bahmni.module.bahmnicore.web.v1_0.controller.display.controls;

import org.bahmni.module.bahmnicore.web.v1_0.BaseIntegrationTest;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.module.bahmniemrapi.drugogram.contract.Regimen;
import org.openmrs.module.bahmniemrapi.drugogram.contract.RegimenRow;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.Locale;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class DrugOGramControllerIT extends BaseIntegrationTest {

    @Before
    public void setUp() throws Exception {
        executeDataSet("drugogram/drugOGram.xml");
        executeDataSet("drugogram/revisedDrugsForDrugOGram.xml");
        executeDataSet("drugogram/discontinueDrugsForDrugOGram.xml");
    }

    @Test
    public void shouldFetchDrugsInRegimenTableFormat() throws Exception {
        Regimen regimen = deserialize(handle(newGetRequest("/rest/v1/bahmnicore/drugOGram/regimen",
                new Parameter("patientUuid", "1a246ed5-3c11-11de-a0ba-001ed98eb67a")
        )), Regimen.class);

        assertNotNull(regimen);
        assertEquals(2, regimen.getHeaders().size());
        assertEquals(3, regimen.getRows().size());
        Iterator<RegimenRow> rowIterator = regimen.getRows().iterator();

        RegimenRow firstRow = rowIterator.next();
        assertEquals(getOnlyDate(stringToDate("2005-09-23 08:00:00")), firstRow.getDate());
        assertEquals("1000.0", firstRow.getDrugs().get("Ibuprofen"));
        assertEquals("450.0", firstRow.getDrugs().get("Crocin"));

        RegimenRow secondRow = rowIterator.next();
        assertEquals(getOnlyDate(stringToDate("2005-09-26 00:00:00.0")), secondRow.getDate());
        assertEquals("1000.0", secondRow.getDrugs().get("Ibuprofen"));
        assertEquals("STOP", secondRow.getDrugs().get("Crocin"));

        RegimenRow thirdRow = rowIterator.next();
        assertEquals(getOnlyDate(stringToDate("2005-09-30 00:00:00.0")), thirdRow.getDate());
        assertEquals("STOP", thirdRow.getDrugs().get("Ibuprofen"));
        assertEquals(null, thirdRow.getDrugs().get("Crocin"));
    }

    @Test
    public void shouldFetchRevisedDrugsInRegimenTableFormat() throws Exception {
        Regimen regimen = deserialize(handle(newGetRequest("/rest/v1/bahmnicore/drugOGram/regimen",
                new Parameter("patientUuid", "1a246ed5-3c11-11de-a0ba-001edc8eb67a")
        )), Regimen.class);

        assertNotNull(regimen);
        assertEquals(2, regimen.getHeaders().size());
        assertEquals(4, regimen.getRows().size());
        Iterator<RegimenRow> rowIterator = regimen.getRows().iterator();

        RegimenRow firstRow = rowIterator.next();
        assertEquals(getOnlyDate(stringToDate("2005-09-23 08:00:00")), firstRow.getDate());
        assertEquals("1000.0", firstRow.getDrugs().get("Ibuprofen"));
        assertEquals("450.0", firstRow.getDrugs().get("Crocin"));

        RegimenRow secondRow = rowIterator.next();
        assertEquals(getOnlyDate(stringToDate("2005-09-25 08:00:00")), secondRow.getDate());
        assertEquals("500.0", secondRow.getDrugs().get("Ibuprofen"));
        assertEquals("450.0", secondRow.getDrugs().get("Crocin"));

        RegimenRow thirdRow = rowIterator.next();
        assertEquals(getOnlyDate(stringToDate("2005-09-26 00:00:00.0")), thirdRow.getDate());
        assertEquals("500.0", thirdRow.getDrugs().get("Ibuprofen"));
        assertEquals("STOP", thirdRow.getDrugs().get("Crocin"));

        RegimenRow fourthRow = rowIterator.next();
        assertEquals(getOnlyDate(stringToDate("2005-10-02 00:00:00.0")), fourthRow.getDate());
        assertEquals("STOP", fourthRow.getDrugs().get("Ibuprofen"));
        assertEquals(null , fourthRow.getDrugs().get("Crocin"));
    }

    @Test
    public void shouldFetchDiscontinueDrugsInRegimenTableFormat() throws Exception {
        Regimen regimen = deserialize(handle(newGetRequest("/rest/v1/bahmnicore/drugOGram/regimen",
                new Parameter("patientUuid", "1a246ed5-3c11-11de-a0ba-001edxseb67a")
        )), Regimen.class);

        assertNotNull(regimen);
        assertEquals(2, regimen.getHeaders().size());
        assertEquals(3, regimen.getRows().size());
        Iterator<RegimenRow> rowIterator = regimen.getRows().iterator();

        RegimenRow firstRow = rowIterator.next();
        assertEquals(getOnlyDate(stringToDate("2005-09-23 08:00:00")), firstRow.getDate());
        assertEquals("1000.0", firstRow.getDrugs().get("Ibuprofen"));
        assertEquals("450.0", firstRow.getDrugs().get("Crocin"));

        RegimenRow secondRow = rowIterator.next();
        assertEquals(getOnlyDate(stringToDate("2005-09-25 08:00:00")), secondRow.getDate());
        assertEquals("STOP", secondRow.getDrugs().get("Ibuprofen"));
        assertEquals("450.0", secondRow.getDrugs().get("Crocin"));

        RegimenRow thirdRow = rowIterator.next();
        assertEquals(getOnlyDate(stringToDate("2005-09-26 00:00:00.0")), thirdRow.getDate());
        assertEquals(null, thirdRow.getDrugs().get("Ibuprofen"));
        assertEquals("STOP", thirdRow.getDrugs().get("Crocin"));
    }

    public Date getOnlyDate(Date date) throws ParseException {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        return sdf.parse(sdf.format(date));
    }

    public Date stringToDate(String dateString) throws ParseException {
        DateFormat format = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
        return format.parse(dateString);
    }

}