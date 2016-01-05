package org.bahmni.module.bahmnicore.web.v1_0.controller.display.controls;

import org.bahmni.module.bahmnicore.web.v1_0.BaseIntegrationTest;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.module.bahmniemrapi.drugogram.contract.TreatmentRegimen;
import org.openmrs.module.bahmniemrapi.drugogram.contract.RegimenRow;
import org.openmrs.module.emrapi.encounter.domain.EncounterTransaction;
import org.springframework.beans.factory.annotation.Autowired;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;
import java.util.Locale;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

public class DrugOGramControllerIT extends BaseIntegrationTest {
    @Autowired
    DrugOGramController drugOGramController;

    @Before
    public void setUp() throws Exception {
        executeDataSet("drugogram.xml");
        executeDataSet("revisedDrugsForDrugOGram.xml");
        executeDataSet("discontinueDrugsForDrugOGram.xml");
        executeDataSet("startAndStopOnSameDateDrugs.xml");
    }

    @Test
    public void shouldFetchDrugsInRegimenTableFormat() throws Exception {
        TreatmentRegimen treatmentRegimen = drugOGramController.getRegimen("1a246ed5-3c11-11de-a0ba-001ed98eb67a", null, null, null);

        assertNotNull(treatmentRegimen);
        assertEquals(2, treatmentRegimen.getHeaders().size());
        assertEquals(3, treatmentRegimen.getRows().size());
        Iterator<RegimenRow> rowIterator = treatmentRegimen.getRows().iterator();

        RegimenRow firstRow = rowIterator.next();
        assertEquals(getOnlyDate(stringToDate("2005-09-23 08:00:00")), firstRow.getDate());
        assertEquals("1000.0", firstRow.getDrugs().get("Ibuprofen"));
        assertEquals("450.0", firstRow.getDrugs().get("Crocin"));

        RegimenRow secondRow = rowIterator.next();
        assertEquals(getOnlyDate(stringToDate("2005-09-26 00:00:00.0")), secondRow.getDate());
        assertEquals("1000.0", secondRow.getDrugs().get("Ibuprofen"));
        assertEquals("Stop", secondRow.getDrugs().get("Crocin"));

        RegimenRow thirdRow = rowIterator.next();
        assertEquals(getOnlyDate(stringToDate("2005-09-30 00:00:00.0")), thirdRow.getDate());
        assertEquals("Stop", thirdRow.getDrugs().get("Ibuprofen"));
        assertEquals(null, thirdRow.getDrugs().get("Crocin"));
    }

    @Test
    public void shouldFetchSpecifiedDrugsInRegimenTableFormat() throws Exception {
        TreatmentRegimen treatmentRegimen = drugOGramController.getRegimen("1a246ed5-3c11-11de-a0ba-001ed98eb67a", Arrays.asList("Ibuprofen"), null, null);

        assertNotNull(treatmentRegimen);
        assertEquals(1, treatmentRegimen.getHeaders().size());
        assertEquals(2, treatmentRegimen.getRows().size());
        Iterator<RegimenRow> rowIterator = treatmentRegimen.getRows().iterator();

        RegimenRow firstRow = rowIterator.next();
        assertEquals(getOnlyDate(stringToDate("2005-09-23 08:00:00")), firstRow.getDate());
        assertEquals("1000.0", firstRow.getDrugs().get("Ibuprofen"));
        assertEquals(false, firstRow.getDrugs().keySet().contains("Crocin"));

        RegimenRow thirdRow = rowIterator.next();
        assertEquals(getOnlyDate(stringToDate("2005-09-30 00:00:00.0")), thirdRow.getDate());
        assertEquals("Stop", thirdRow.getDrugs().get("Ibuprofen"));
        assertEquals(false, firstRow.getDrugs().keySet().contains("Crocin"));
    }

    @Test
    public void shouldFetchSpecifiedDrugsWhenWeSpecifyConceptSetNameInRegimenTableFormat() throws Exception {
        TreatmentRegimen treatmentRegimen = drugOGramController.getRegimen("1a246ed5-3c11-11de-a0ba-001ed98eb67a", Arrays.asList("TB Drugs"), null, null);

        assertNotNull(treatmentRegimen);
        assertEquals(1, treatmentRegimen.getHeaders().size());
        assertEquals(2, treatmentRegimen.getRows().size());
        Iterator<RegimenRow> rowIterator = treatmentRegimen.getRows().iterator();

        RegimenRow firstRow = rowIterator.next();
        assertEquals(getOnlyDate(stringToDate("2005-09-23 09:00:00")), firstRow.getDate());
        assertEquals("450.0", firstRow.getDrugs().get("Crocin"));

        RegimenRow thirdRow = rowIterator.next();
        assertEquals(getOnlyDate(stringToDate("2005-09-26 00:00:00.0")), thirdRow.getDate());
        assertEquals("Stop", thirdRow.getDrugs().get("Crocin"));
    }

    @Test
    public void shouldFetchRevisedDrugsInRegimenTableFormat() throws Exception {
        TreatmentRegimen treatmentRegimen = drugOGramController.getRegimen("1a246ed5-3c11-11de-a0ba-001edc8eb67a", null, null, null);

        assertNotNull(treatmentRegimen);
        assertEquals(2, treatmentRegimen.getHeaders().size());
        assertEquals(4, treatmentRegimen.getRows().size());
        Iterator<RegimenRow> rowIterator = treatmentRegimen.getRows().iterator();

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
        assertEquals("Stop", thirdRow.getDrugs().get("Crocin"));

        RegimenRow fourthRow = rowIterator.next();
        assertEquals(getOnlyDate(stringToDate("2005-10-02 00:00:00.0")), fourthRow.getDate());
        assertEquals("Stop", fourthRow.getDrugs().get("Ibuprofen"));
        assertEquals(null , fourthRow.getDrugs().get("Crocin"));
    }

    @Test
    public void shouldFetchDiscontinueDrugsInRegimenTableFormat() throws Exception {
        TreatmentRegimen treatmentRegimen = drugOGramController.getRegimen("1a246ed5-3c11-11de-a0ba-001edxseb67a", null, null, null);

        assertNotNull(treatmentRegimen);
        assertEquals(2, treatmentRegimen.getHeaders().size());
        assertEquals(4, treatmentRegimen.getRows().size());
        Iterator<RegimenRow> rowIterator = treatmentRegimen.getRows().iterator();

        RegimenRow firstRow = rowIterator.next();
        assertEquals(getOnlyDate(stringToDate("2005-09-23 08:00:00")), firstRow.getDate());
        assertEquals("1000.0", firstRow.getDrugs().get("Ibuprofen"));
        assertEquals(null, firstRow.getDrugs().get("Crocin"));

        RegimenRow secondRow = rowIterator.next();
        assertEquals(getOnlyDate(stringToDate("2005-09-23 08:00:00")), secondRow.getDate());
        assertEquals("Stop", secondRow.getDrugs().get("Ibuprofen"));
        assertEquals(null, secondRow.getDrugs().get("Crocin"));

        RegimenRow thirdRow = rowIterator.next();
        assertEquals(getOnlyDate(stringToDate("2005-09-25 00:00:00.0")), thirdRow.getDate());
        assertEquals(null, thirdRow.getDrugs().get("Ibuprofen"));
        assertEquals("450.0", thirdRow.getDrugs().get("Crocin"));

        RegimenRow fourthRow = rowIterator.next();
        assertEquals(getOnlyDate(stringToDate("2005-09-26 00:00:00.0")), fourthRow.getDate());
        assertEquals(null, fourthRow.getDrugs().get("Ibuprofen"));
        assertEquals("Stop", fourthRow.getDrugs().get("Crocin"));
    }

    @Test
    public void shouldFetchOrdersWhichAreStartedAndStoppedOnSameDate() throws Exception {
        TreatmentRegimen treatmentRegimen = drugOGramController.getRegimen("1a246ed5-3c11-11de-a0ba-001djxseb67a", null, null, null);

        assertNotNull(treatmentRegimen);
        assertEquals(3, treatmentRegimen.getHeaders().size());
        assertEquals(5, treatmentRegimen.getRows().size());
        Iterator<RegimenRow> rowIterator = treatmentRegimen.getRows().iterator();
        RegimenRow firstRow = rowIterator.next();
        assertEquals(getOnlyDate(stringToDate("2005-09-23 08:00:00")), firstRow.getDate());
        assertEquals("1000.0", firstRow.getDrugs().get("Ibuprofen"));
        assertEquals("450.0", firstRow.getDrugs().get("Crocin"));
        assertEquals(null, firstRow.getDrugs().get("Paracetamol"));

        RegimenRow secondRow = rowIterator.next();
        assertEquals(getOnlyDate(stringToDate("2005-09-23 08:00:00")), secondRow.getDate());
        assertEquals("Stop", secondRow.getDrugs().get("Ibuprofen"));
        assertEquals("450.0", secondRow.getDrugs().get("Crocin"));
        assertEquals(null, secondRow.getDrugs().get("Paracetamol"));

        RegimenRow thirdRow = rowIterator.next();
        assertEquals(getOnlyDate(stringToDate("2005-09-24 09:00:00")), thirdRow.getDate());
        assertEquals(null, thirdRow.getDrugs().get("Ibuprofen"));
        assertEquals("450.0", thirdRow.getDrugs().get("Crocin"));
        assertEquals("40.0", thirdRow.getDrugs().get("Paracetamol"));

        RegimenRow fourthRow = rowIterator.next();
        assertEquals(getOnlyDate(stringToDate("2005-09-25 00:00:00.0")), fourthRow.getDate());
        assertEquals(null, fourthRow.getDrugs().get("Ibuprofen"));
        assertEquals("Stop", fourthRow.getDrugs().get("Crocin"));
        assertEquals("40.0", fourthRow.getDrugs().get("Paracetamol"));

        RegimenRow fifthRow = rowIterator.next();
        assertEquals(getOnlyDate(stringToDate("2005-09-28 00:00:00.0")), fifthRow.getDate());
        assertEquals(null, fifthRow.getDrugs().get("Ibuprofen"));
        assertEquals(null, fifthRow.getDrugs().get("Crocin"));
        assertEquals("Stop", fifthRow.getDrugs().get("Paracetamol"));
    }

    @Test
    public void shouldRetrieveDrugsOrderedWithinProgramStartDateAndEndDate()  throws Exception {
        TreatmentRegimen treatmentRegimen = drugOGramController.getRegimen("1a246ed5-3c11-11de-a0ba-001edc8eb67a", null, "2005-09-22T14:29:38.000", "2005-09-24T14:29:38.000");

        assertNotNull(treatmentRegimen);
        assertEquals(2, treatmentRegimen.getHeaders().size());
        Iterator<RegimenRow> rowIterator = treatmentRegimen.getRows().iterator();
        RegimenRow firstRow = rowIterator.next();
        assertEquals(getOnlyDate(stringToDate("2005-09-23 08:00:00")), firstRow.getDate());
        assertEquals("1000.0", firstRow.getDrugs().get("Ibuprofen"));
        assertEquals("450.0", firstRow.getDrugs().get("Crocin"));

        RegimenRow secondRow = rowIterator.next();
        assertEquals(getOnlyDate(stringToDate("2005-09-25 08:00:00")), secondRow.getDate());
        assertEquals("Stop", secondRow.getDrugs().get("Ibuprofen"));
        assertEquals("450.0", secondRow.getDrugs().get("Crocin"));

        RegimenRow thirdRow = rowIterator.next();
        assertEquals(getOnlyDate(stringToDate("2005-09-26 00:00:00.0")), thirdRow.getDate());
        assertEquals("Stop", thirdRow.getDrugs().get("Crocin"));
    }

    public void shouldFetchSpecifiedDrugsWhenWeSpecifyConceptNamesInRegimenTableFormat() throws Exception {
        TreatmentRegimen treatmentRegimen = drugOGramController.getRegimen("1a246ed5-3c11-11de-a0ba-001ed98eb67a", Arrays.asList("Ibuprofen", "Crocin"), null, null);

        assertNotNull(treatmentRegimen);
        assertEquals(2, treatmentRegimen.getHeaders().size());
        Iterator<EncounterTransaction.Concept> headerIterator = treatmentRegimen.getHeaders().iterator();
        assertEquals("Ibuprofen", headerIterator.next().getName());
        assertEquals("Crocin", headerIterator.next().getName());
        assertEquals(false, headerIterator.hasNext());
        assertEquals(3, treatmentRegimen.getRows().size());
        Iterator<RegimenRow> rowIterator = treatmentRegimen.getRows().iterator();

        RegimenRow firstRow = rowIterator.next();
        assertEquals(getOnlyDate(stringToDate("2005-09-23 09:00:00")), firstRow.getDate());
        assertEquals("1000.0", firstRow.getDrugs().get("Ibuprofen"));
        assertEquals("450.0", firstRow.getDrugs().get("Crocin"));

        RegimenRow secondRow = rowIterator.next();
        assertEquals(getOnlyDate(stringToDate("2005-09-26 00:00:00.0")), secondRow.getDate());
        assertEquals("1000.0", secondRow.getDrugs().get("Ibuprofen"));
        assertEquals("Stop", secondRow.getDrugs().get("Crocin"));

        RegimenRow thirdRow = rowIterator.next();
        assertEquals(getOnlyDate(stringToDate("2005-09-30 00:00:00.0")), thirdRow.getDate());
        assertEquals("Stop", thirdRow.getDrugs().get("Ibuprofen"));
    }

    @Test
    public void shouldFetchSpecifiedDrugsWhenWeSpecifyConceptNamesInRegimenTableFormatCrocinShouldComeFirst() throws Exception {
        TreatmentRegimen treatmentRegimen = drugOGramController.getRegimen("1a246ed5-3c11-11de-a0ba-001ed98eb67a", Arrays.asList("Crocin", "Ibuprofen"), null, null);

        assertNotNull(treatmentRegimen);
        assertEquals(2, treatmentRegimen.getHeaders().size());
        Iterator<EncounterTransaction.Concept> headerIterator = treatmentRegimen.getHeaders().iterator();
        assertEquals("Crocin", headerIterator.next().getName());
        assertEquals("Ibuprofen", headerIterator.next().getName());
        assertEquals(false, headerIterator.hasNext());
        assertEquals(3, treatmentRegimen.getRows().size());
        Iterator<RegimenRow> rowIterator = treatmentRegimen.getRows().iterator();

        RegimenRow firstRow = rowIterator.next();
        assertEquals(getOnlyDate(stringToDate("2005-09-23 09:00:00")), firstRow.getDate());
        assertEquals("1000.0", firstRow.getDrugs().get("Ibuprofen"));
        assertEquals("450.0", firstRow.getDrugs().get("Crocin"));

        RegimenRow secondRow = rowIterator.next();
        assertEquals(getOnlyDate(stringToDate("2005-09-26 00:00:00.0")), secondRow.getDate());
        assertEquals("1000.0", secondRow.getDrugs().get("Ibuprofen"));
        assertEquals("Stop", secondRow.getDrugs().get("Crocin"));

        RegimenRow thirdRow = rowIterator.next();
        assertEquals(getOnlyDate(stringToDate("2005-09-30 00:00:00.0")), thirdRow.getDate());
        assertEquals("Stop", thirdRow.getDrugs().get("Ibuprofen"));
    }

    @Test
    public void shouldNotFetchParacetamolAsItWasNotPrescribedToPatientButSpecifiedInConceptNames() throws Exception {
        TreatmentRegimen treatmentRegimen = drugOGramController.getRegimen("1a246ed5-3c11-11de-a0ba-001ed98eb67a", Arrays.asList("Ibuprofen", "Crocin", "Paracetamol"), null, null);

        assertNotNull(treatmentRegimen);
        assertEquals(2, treatmentRegimen.getHeaders().size());
        assertEquals(3, treatmentRegimen.getRows().size());
        Iterator<RegimenRow> rowIterator = treatmentRegimen.getRows().iterator();

        RegimenRow firstRow = rowIterator.next();
        assertEquals(getOnlyDate(stringToDate("2005-09-23 09:00:00")), firstRow.getDate());
        assertEquals("1000.0", firstRow.getDrugs().get("Ibuprofen"));
        assertEquals("450.0", firstRow.getDrugs().get("Crocin"));

        RegimenRow secondRow = rowIterator.next();
        assertEquals(getOnlyDate(stringToDate("2005-09-26 00:00:00.0")), secondRow.getDate());
        assertEquals("1000.0", secondRow.getDrugs().get("Ibuprofen"));
        assertEquals("Stop", secondRow.getDrugs().get("Crocin"));

        RegimenRow thirdRow = rowIterator.next();
        assertEquals(getOnlyDate(stringToDate("2005-09-30 00:00:00.0")), thirdRow.getDate());
        assertEquals("Stop", thirdRow.getDrugs().get("Ibuprofen"));
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
