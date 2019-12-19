package org.bahmni.module.admin.observation;

import org.bahmni.csv.KeyValue;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.openmrs.Concept;
import org.openmrs.ConceptDatatype;
import org.openmrs.ConceptName;
import org.openmrs.ConceptNumeric;
import org.openmrs.api.APIException;
import org.openmrs.api.ConceptService;
import org.openmrs.module.emrapi.encounter.domain.EncounterTransaction;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.HashMap;
import java.util.LinkedHashMap;

import static java.util.Arrays.asList;
import static org.bahmni.module.admin.observation.CSVObservationHelper.getLastItem;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

public class CSVObservationHelperTest {

    @Rule
    public ExpectedException exception = ExpectedException.none();
    @Mock
    private ConceptService conceptService;
    private List<String> conceptNames;
    private List<EncounterTransaction.Observation> observations;
    private ConceptDatatype conceptDatatype;
    @Mock
    private Concept heightConcept;

    @Mock
    private ConceptName heightConceptName;

    @Before
    public void setUp() {
        initMocks(this);
        conceptNames = new ArrayList<>();
        observations = new ArrayList<>();

        when(conceptService.getConceptByName("Height")).thenReturn(heightConcept);

        when(heightConcept.getName()).thenReturn(heightConceptName);
        when(heightConceptName.getName()).thenReturn("Height");
        conceptDatatype = mock(ConceptDatatype.class);
        when(heightConcept.getDatatype()).thenReturn(conceptDatatype);
    }


    @Test
    public void shouldCreateHeightObservationForTheGivenObsRow() throws ParseException {
        KeyValue heightObsRow = new KeyValue("Height", "100");
        conceptNames.add("Height");

        CSVObservationHelper csvObservationHelper = new CSVObservationHelper(conceptService);
        Date encounterDate = new Date();
        csvObservationHelper.createObservations(observations, encounterDate, heightObsRow, conceptNames);

        assertEquals(1, observations.size());
        EncounterTransaction.Observation heightObservation = observations.get(0);
        assertEquals("Height", heightObservation.getConcept().getName());
        assertEquals("100", heightObservation.getValue());
        assertEquals(encounterDate, heightObservation.getObservationDateTime());

    }

    @Test
    public void shouldSetAbnormalInterpretationIfObsValueIsOutOfRange() throws ParseException {
        KeyValue heightObsRow = new KeyValue("Height", "100");
        conceptNames.add("Height");

        ConceptNumeric heightNumericConcept = mock(ConceptNumeric.class);

        when(heightNumericConcept.getDatatype()).thenReturn(conceptDatatype);
        when(heightNumericConcept.getName()).thenReturn(heightConceptName);
        when(conceptDatatype.isNumeric()).thenReturn(true);
        when(conceptService.getConceptByName("Height")).thenReturn(heightNumericConcept);
        when(heightNumericConcept.getHiNormal()).thenReturn(new Double(110));
        when(heightNumericConcept.getLowNormal()).thenReturn(new Double(105));

        CSVObservationHelper csvObservationHelper = new CSVObservationHelper(conceptService, administrationService);
        Date encounterDate = new Date();
        csvObservationHelper.createObservations(observations, encounterDate, heightObsRow, conceptNames);

        assertEquals(1, observations.size());
        EncounterTransaction.Observation heightObservation = observations.get(0);
        assertEquals("Height", heightObservation.getConcept().getName());
        assertEquals("100", heightObservation.getValue());
        assertEquals(encounterDate, heightObservation.getObservationDateTime());
        assertEquals("ABNORMAL", heightObservation.getInterpretation());
    }

    @Test
    public void shouldCreateHeightObservationAsGroupMemberOfBMIDataObservation() throws ParseException {
        KeyValue heightObsRow = new KeyValue("BMI Data.Height", "100");
        conceptNames.add("BMI Data");
        conceptNames.add("Height");

        Concept bmiDataConcept = mock(Concept.class);
        ConceptName bmiConceptName = mock(ConceptName.class);

        when(conceptService.getConceptByName("BMI Data")).thenReturn(bmiDataConcept);
        when(bmiDataConcept.getName()).thenReturn(bmiConceptName);
        when(bmiConceptName.getName()).thenReturn("BMI Data");

        CSVObservationHelper csvObservationHelper = new CSVObservationHelper(conceptService);
        Date encounterDate = new Date();
        csvObservationHelper.createObservations(observations, encounterDate, heightObsRow, conceptNames);

        assertEquals(1, observations.size());

        assertEquals("BMI Data", observations.get(0).getConcept().getName());
        assertEquals(1, observations.get(0).getGroupMembers().size());
        EncounterTransaction.Observation heightObservation = observations.get(0).getGroupMembers().get(0);
        assertEquals("Height", heightObservation.getConcept().getName());
        assertEquals("100", heightObservation.getValue());
        assertEquals(encounterDate, heightObservation.getObservationDateTime());

    }

    @Test
    public void shouldCreateHeightObservationAndWeightObservationAsGroupMembersOfBMIDataObservation() throws ParseException {
        KeyValue heightObsRow = new KeyValue("BMI Data.Height", "100");
        KeyValue weightObsRow = new KeyValue("BMI Data.Weight", "150");
        String bmiData = "BMI Data";
        conceptNames.add(bmiData);
        conceptNames.add("Height");

        List<String> bmiAndWeightConcepts = new ArrayList<>();
        bmiAndWeightConcepts.add(bmiData);
        bmiAndWeightConcepts.add("Weight");

        Concept bmiDataConcept = mock(Concept.class);
        ConceptName bmiConceptName = mock(ConceptName.class);

        when(conceptService.getConceptByName(bmiData)).thenReturn(bmiDataConcept);
        when(bmiDataConcept.getName()).thenReturn(bmiConceptName);
        when(bmiConceptName.getName()).thenReturn(bmiData);


        Concept weightConcept = mock(Concept.class);
        ConceptName weightConceptName = mock(ConceptName.class);

        when(conceptService.getConceptByName("Weight")).thenReturn(weightConcept);

        when(weightConcept.getName()).thenReturn(weightConceptName);
        when(weightConceptName.getName()).thenReturn("Weight");
        ConceptDatatype conceptDatatype = mock(ConceptDatatype.class);
        when(weightConcept.getDatatype()).thenReturn(conceptDatatype);

        CSVObservationHelper csvObservationHelper = new CSVObservationHelper(conceptService);
        Date encounterDate = new Date();
        csvObservationHelper.createObservations(observations, encounterDate, heightObsRow, conceptNames);
        csvObservationHelper.createObservations(observations, encounterDate, weightObsRow, bmiAndWeightConcepts);


        assertEquals(1, observations.size());
        assertEquals(bmiData, observations.get(0).getConcept().getName());
        assertEquals(2, observations.get(0).getGroupMembers().size());
        EncounterTransaction.Observation heightObservation = observations.get(0).getGroupMembers().get(0);
        assertEquals("Height", heightObservation.getConcept().getName());
        assertEquals("100", heightObservation.getValue());
        assertEquals(encounterDate, heightObservation.getObservationDateTime());
        EncounterTransaction.Observation weightObservation = observations.get(0).getGroupMembers().get(1);
        assertEquals("Weight", weightObservation.getConcept().getName());
        assertEquals("150", weightObservation.getValue());
        assertEquals(encounterDate, weightObservation.getObservationDateTime());

    }

    @Test
    public void shouldCreateTwoHeightObsInBMIData() throws ParseException {
        KeyValue heightObsRow = new KeyValue("BMI Data.Height", "100");
        KeyValue secondHeightObsRow = new KeyValue("BMI Data.Height", "200");
        String bmiData = "BMI Data";
        conceptNames.add(bmiData);
        String height = "Height";
        conceptNames.add(height);

        List<String> heightConcepts = new ArrayList<>();
        heightConcepts.add(bmiData);
        heightConcepts.add(height);

        Concept bmiDataConcept = mock(Concept.class);
        ConceptName bmiConceptName = mock(ConceptName.class);

        when(conceptService.getConceptByName(bmiData)).thenReturn(bmiDataConcept);
        when(bmiDataConcept.getName()).thenReturn(bmiConceptName);
        when(bmiConceptName.getName()).thenReturn(bmiData);

        CSVObservationHelper csvObservationHelper = new CSVObservationHelper(conceptService);
        Date encounterDate = new Date();
        csvObservationHelper.createObservations(observations, encounterDate, heightObsRow, conceptNames);
        csvObservationHelper.createObservations(observations, encounterDate, secondHeightObsRow, heightConcepts);

        assertEquals(1, observations.size());
        assertEquals(2, observations.get(0).getGroupMembers().size());
        EncounterTransaction.Observation heightObservation = observations.get(0).getGroupMembers().get(0);
        assertEquals(height, heightObservation.getConcept().getName());
        EncounterTransaction.Observation secondHeightObservation = observations.get(0).getGroupMembers().get(1);
        assertEquals(height, secondHeightObservation.getConcept().getName());
        assertEquals("100", heightObservation.getValue());
        assertEquals("200", secondHeightObservation.getValue());

    }

    @Test
    public void shouldCreateCodedHeightObservationForTheGivenObsRow() throws ParseException {
        KeyValue heightObsRow = new KeyValue("Height", "tall");
        conceptNames.add("Height");
        Concept valueConcept = mock(Concept.class);

        when(conceptDatatype.isCoded()).thenReturn(true);
        when(conceptService.getConceptsByName("tall")).thenReturn(asList(valueConcept));

        ConceptName valueConceptName = mock(ConceptName.class);
        when(valueConcept.getFullySpecifiedName(Matchers.any())).thenReturn(valueConceptName);
        when(valueConcept.getName()).thenReturn(valueConceptName);
        when(valueConceptName.getName()).thenReturn("tall");
        when(valueConcept.getUuid()).thenReturn("108abe5c-555e-40d2-ba16-5645a7ad237b");
        when(valueConcept.getName().getName()).thenReturn("tall");
        when(valueConcept.getName().getUuid()).thenReturn("108abe5c-555e-40d2-ba16-5645a7ad45237");


        CSVObservationHelper csvObservationHelper = new CSVObservationHelper(conceptService);
        Date encounterDate = new Date();
        csvObservationHelper.createObservations(observations, encounterDate, heightObsRow, conceptNames);

        assertEquals(1, observations.size());
        EncounterTransaction.Observation heightObservation = observations.get(0);
        assertEquals("Height", heightObservation.getConcept().getName());
        assertEquals("108abe5c-555e-40d2-ba16-5645a7ad237b", (String) ((LinkedHashMap<String, Object>) heightObservation.getValue()).get("uuid"));
        assertEquals("108abe5c-555e-40d2-ba16-5645a7ad45237", (String) ((HashMap<String, Object>) ((LinkedHashMap<String, Object>) heightObservation.getValue()).get("name")).get("uuid"));
        assertEquals("tall", (String) ((HashMap<String, Object>) ((LinkedHashMap<String, Object>) heightObservation.getValue()).get("name")).get("name"));
        assertEquals(encounterDate, heightObservation.getObservationDateTime());

    }

    @Test
    public void shouldThrowConceptNotFoundExceptionForInvalidCodedAnswer() throws ParseException {
        KeyValue heightObsRow = new KeyValue("Height", "invalid-concept");
        conceptNames.add("Height");

        when(conceptDatatype.isCoded()).thenReturn(true);

        CSVObservationHelper csvObservationHelper = new CSVObservationHelper(conceptService);
        Date encounterDate = new Date();

        exception.expect(Exception.class);
        exception.expectMessage("invalid-concept not found");

        csvObservationHelper.createObservations(observations, encounterDate, heightObsRow, conceptNames);

    }

    @Test
    public void shouldThrowExceptionIfDecimalValueisGivenForNumericConcept() {
        ConceptNumeric bmiConcept = mock(ConceptNumeric.class);
        when(bmiConcept.isNumeric()).thenReturn(true);
        String bmi = "BMI";
        conceptNames.add(bmi);

        when(conceptService.getConceptByName(bmi)).thenReturn(bmiConcept);
        ConceptName conceptName = new ConceptName();
        conceptName.setName(bmi);
        when(bmiConcept.getName()).thenReturn(conceptName);
        KeyValue csvHeightObs = new KeyValue(bmi, "1.34");

        exception.expect(APIException.class);
        exception.expectMessage("Decimal is not allowed for BMI concept");

        CSVObservationHelper csvObservationHelper = new CSVObservationHelper(conceptService);
        csvObservationHelper.verifyNumericConceptValue(csvHeightObs, conceptNames);

    }

    @Test
    public void shouldReturnCSVHeaderPartsFromGivenObsRow() {
        KeyValue csvObservation = new KeyValue();
        csvObservation.setKey("BMI Data.Height");

        CSVObservationHelper csvObservationHelper = new CSVObservationHelper(conceptService);
        List<String> csvHeaderParts = csvObservationHelper.getCSVHeaderParts(csvObservation);

        assertEquals(csvHeaderParts.get(0), "BMI Data");
        assertEquals(csvHeaderParts.get(1), "Height");
    }

    @Test
    public void shouldReturnEmptyListIfKeyIsEmpty() {
        KeyValue csvObservation = new KeyValue();

        CSVObservationHelper csvObservationHelper = new CSVObservationHelper(conceptService);
        List<String> csvHeaderParts = csvObservationHelper.getCSVHeaderParts(csvObservation);

        assertTrue(csvHeaderParts.isEmpty());
    }

    @Test
    public void shouldReturnTrueIfCSVObsIsOfForm1Type() {
        KeyValue csvObservation = new KeyValue();
        csvObservation.setKey("BMI Data.Height");

        CSVObservationHelper csvObservationHelper = new CSVObservationHelper(conceptService);

        assertTrue(csvObservationHelper.isForm1Type(csvObservation));
    }

    @Test
    public void shouldReturnTrueIfCSVObsIsOfForm2Type() {
        KeyValue csvObservation = new KeyValue();
        csvObservation.setKey("Form2.BMI Data.Height");

        CSVObservationHelper csvObservationHelper = new CSVObservationHelper(conceptService);

        assertTrue(csvObservationHelper.isForm2Type(csvObservation));
    }

    @Test
    public void shouldReturnLastItem() {
        final List<Object> emptyList = Arrays.asList("Vitals", "Height");

        assertEquals("Height", getLastItem(emptyList));
    }

    @Test
    public void shouldThrowExceptionWhenEmptyItemsAreSent() {
        exception.expectMessage("Empty items");

        getLastItem(new ArrayList<>());

    }
}
