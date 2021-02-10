package org.bahmni.module.admin.observation.handler;

import org.bahmni.csv.KeyValue;
import org.bahmni.module.admin.csv.models.EncounterRow;
import org.bahmni.module.admin.observation.CSVObservationHelper;
import org.bahmni.form2.service.FormFieldPathService;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.openmrs.api.APIException;
import org.openmrs.module.emrapi.encounter.domain.EncounterTransaction;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static java.lang.String.format;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyListOf;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.never;
import static org.mockito.MockitoAnnotations.initMocks;

public class Form2CSVObsHandlerTest {

    @Rule
    public ExpectedException expectedException = ExpectedException.none();
    private Form2CSVObsHandler form2CSVObsHandler;
    private CSVObservationHelper csvObservationHelper;
    private FormFieldPathService formFieldPathService;

    @Before
    public void setUp() {
        initMocks(this);
        csvObservationHelper = mock(CSVObservationHelper.class);
        formFieldPathService = mock(FormFieldPathService.class);
    }

    @Test
    public void shouldFilterForm2CSVObs() {
        final KeyValue form1CSVObservation = new KeyValue("Vitals.Height", "100");
        final KeyValue form2CSVObservation = new KeyValue("form2.Vitals.Height", "100");

        final EncounterRow encounterRow = new EncounterRow();
        encounterRow.obsRows = asList(form1CSVObservation, form2CSVObservation);

        when(csvObservationHelper.isForm2Type(form1CSVObservation)).thenReturn(false);
        when(csvObservationHelper.isForm2Type(form2CSVObservation)).thenReturn(true);

        form2CSVObsHandler = new Form2CSVObsHandler(csvObservationHelper, null);

        final List<KeyValue> form2CSVObs = form2CSVObsHandler.getRelatedCSVObs(encounterRow);

        assertEquals(1, form2CSVObs.size());
        assertEquals(form2CSVObservation, form2CSVObs.get(0));
        verify(csvObservationHelper).isForm2Type(form1CSVObservation);
        verify(csvObservationHelper).isForm2Type(form2CSVObservation);
    }

    @Test
    public void shouldVerifyCreateObservationsIsCalled() throws ParseException {
        final KeyValue form1CSVObservation = new KeyValue("Vitals.Height", "100");
        final KeyValue form2CSVObservation = new KeyValue("form2.Vitals.Height", "100");

        final EncounterRow encounterRow = new EncounterRow();
        encounterRow.obsRows = asList(form1CSVObservation, form2CSVObservation);
        encounterRow.encounterDateTime = "2019-11-11";

        when(csvObservationHelper.isForm2Type(form1CSVObservation)).thenReturn(false);
        when(csvObservationHelper.isForm2Type(form2CSVObservation)).thenReturn(true);

        final List<String> headerParts = new ArrayList<>(Arrays.asList("form2", "Vitals", "Height"));
        when(csvObservationHelper.getCSVHeaderParts(form2CSVObservation)).thenReturn(headerParts);
        doNothing().when(csvObservationHelper).verifyNumericConceptValue(form2CSVObservation, headerParts);
        doNothing().when(csvObservationHelper).createObservations(anyListOf(EncounterTransaction.Observation.class),
                any(Date.class), any(KeyValue.class), anyListOf(String.class));
        when(formFieldPathService.getFormFieldPath(asList("Vitals", "Height"))).thenReturn("Vitals.1/1-0");

        form2CSVObsHandler = new Form2CSVObsHandler(csvObservationHelper, formFieldPathService);

        form2CSVObsHandler.handle(encounterRow);

        verify(csvObservationHelper).isForm2Type(form1CSVObservation);
        verify(csvObservationHelper).isForm2Type(form2CSVObservation);
        verify(csvObservationHelper).getCSVHeaderParts(form2CSVObservation);
        verify(csvObservationHelper).verifyNumericConceptValue(form2CSVObservation, headerParts);
        verify(csvObservationHelper).createObservations(anyListOf(EncounterTransaction.Observation.class),
                any(Date.class), any(KeyValue.class), anyListOf(String.class));
    }

    @Test
    public void shouldVerifyCreateObservationIsNotCalledWhenAnEmptyValueIsGiven() throws ParseException {
        final KeyValue form1CSVObservation = new KeyValue("Vitals.Height", "100");
        final KeyValue form2CSVObservation = new KeyValue("form2.Vitals.Height", "");

        final EncounterRow encounterRow = new EncounterRow();
        encounterRow.obsRows = asList(form1CSVObservation, form2CSVObservation);
        encounterRow.encounterDateTime = "2019-11-11";

        when(csvObservationHelper.isForm2Type(form1CSVObservation)).thenReturn(false);
        when(csvObservationHelper.isForm2Type(form2CSVObservation)).thenReturn(true);

        final List<String> headerParts = new ArrayList<>(Arrays.asList("form2", "Vitals"));
        when(csvObservationHelper.getCSVHeaderParts(form2CSVObservation)).thenReturn(headerParts);

        form2CSVObsHandler = new Form2CSVObsHandler(csvObservationHelper, formFieldPathService);

        form2CSVObsHandler.handle(encounterRow);

        verify(csvObservationHelper).isForm2Type(form1CSVObservation);
        verify(csvObservationHelper).isForm2Type(form2CSVObservation);
        verify(csvObservationHelper, never()).createObservations(anyListOf(EncounterTransaction.Observation.class),
                any(Date.class), any(KeyValue.class), anyListOf(String.class));
    }

    @Test
    public void shouldThrowAPIExceptionIfNoConceptProvidedWithCSVHeader() throws ParseException {
        final KeyValue form2CSVObservation = new KeyValue("form2.Vitals", "100");
        final EncounterRow encounterRow = new EncounterRow();
        encounterRow.obsRows = singletonList(form2CSVObservation);
        encounterRow.encounterDateTime = "2019-11-11";

        when(csvObservationHelper.isForm2Type(form2CSVObservation)).thenReturn(true);
        final List<String> headerParts = new ArrayList<>(Arrays.asList("form2", "Vitals"));
        when(csvObservationHelper.getCSVHeaderParts(form2CSVObservation)).thenReturn(headerParts);

        form2CSVObsHandler = new Form2CSVObsHandler(csvObservationHelper, formFieldPathService);

        expectedException.expect(APIException.class);
        expectedException.expectMessage(format("No concepts found in %s", form2CSVObservation.getKey()));

        form2CSVObsHandler.handle(encounterRow);
    }

    @Test
    public void shouldCreateObsForMultiSelectConcept() throws ParseException {
        final KeyValue form2CSVObservation = new KeyValue("form2.HIV_History.PresentConditions", "Asymptomatic|Herpes Zoster");

        final EncounterRow encounterRow = new EncounterRow();
        encounterRow.obsRows = asList(form2CSVObservation);
        encounterRow.encounterDateTime = "2019-11-11";

        when(csvObservationHelper.isForm2Type(form2CSVObservation)).thenReturn(true);

        final List<String> headerParts = new ArrayList<>(Arrays.asList("form2", "HIV_History", "PresentConditions"));
        final List<String> obsValues = new ArrayList<>(Arrays.asList("Asymptomatic", "Herpes Zoster"));
        when(csvObservationHelper.getCSVHeaderParts(form2CSVObservation)).thenReturn(headerParts);
        when(csvObservationHelper.getMultiSelectObs(form2CSVObservation)).thenReturn(obsValues);
        doNothing().when(csvObservationHelper).verifyNumericConceptValue(form2CSVObservation, headerParts);
        doNothing().when(csvObservationHelper).createObservations(anyListOf(EncounterTransaction.Observation.class),
                any(Date.class), anyListOf(KeyValue.class), anyListOf(String.class));
        when(formFieldPathService.isMultiSelectObs(asList("HIV_History", "PresentConditions"))).thenReturn(true);

        form2CSVObsHandler = new Form2CSVObsHandler(csvObservationHelper, formFieldPathService);

        form2CSVObsHandler.handle(encounterRow);
        verify(csvObservationHelper).isForm2Type(form2CSVObservation);
        verify(csvObservationHelper).getCSVHeaderParts(form2CSVObservation);
        verify(csvObservationHelper).verifyNumericConceptValue(form2CSVObservation, headerParts);
        verify(csvObservationHelper).createObservations(anyListOf(EncounterTransaction.Observation.class),
                any(Date.class), anyListOf(KeyValue.class), anyListOf(String.class));
    }
}
