package org.bahmni.module.bahmnicoreui.mapper;

import org.bahmni.module.bahmnicoreui.contract.ConceptValue;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openmrs.*;
import org.openmrs.module.bahmniemrapi.drugorder.dosinginstructions.FlexibleDosingInstructions;
import org.openmrs.module.bahmniemrapi.encountertransaction.contract.BahmniObservation;
import org.openmrs.module.bahmniemrapi.laborder.contract.LabOrderResult;
import org.openmrs.module.emrapi.encounter.domain.EncounterTransaction;
import org.openmrs.util.LocaleUtility;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.when;

@RunWith(PowerMockRunner.class)
@PrepareForTest(LocaleUtility.class)
public class DiseaseSummaryMapperTest {

    SimpleDateFormat simpleDateFormat = new SimpleDateFormat(DiseaseSummaryMapper.DATE_FORMAT);
    String date1;
    String date2;
    String date3;

    @Before
    public void setUp() throws Exception {
        date1 = "2014-09-12";
        date2 = "2014-09-13";
        date3 = "2014-09-14";
        mockStatic(LocaleUtility.class);
        when(LocaleUtility.getDefaultLocale()).thenReturn(Locale.ENGLISH);
    }

    @Test
    public void shouldMapObservationsToResponseFormat() throws ParseException {

        DiseaseSummaryMapper diseaseSummaryMapper = new DiseaseSummaryMapper();
        Map<String, Map<String, List<ConceptValue>>> obsTable = diseaseSummaryMapper.mapObservations(createBahmniObsList());
        assertNotNull(obsTable);
        assertEquals(3, obsTable.size());
        Map<String, List<ConceptValue>> firstDayValue = obsTable.get(date1);
        assertEquals(2, firstDayValue.size());
        assertEquals("101", firstDayValue.get("Temperature").get(0).getValue());
        assertEquals("90", firstDayValue.get("Pulse").get(0).getValue());

        Map<String, List<ConceptValue>> secondDayValue = obsTable.get(date2);
        assertEquals(1, secondDayValue.size());
        assertEquals("100", secondDayValue.get("Pulse").get(0).getValue());

        Map<String, List<ConceptValue>> thirdDayValue = obsTable.get(date3);
        assertEquals(1, thirdDayValue.size());
        assertEquals("120", thirdDayValue.get("bp").get(0).getValue());

    }

    @Test
    public void shouldMapCodedConceptValues() throws ParseException {
        DiseaseSummaryMapper diseaseSummaryMapper = new DiseaseSummaryMapper();
        List<BahmniObservation> bahmniObservations =  new ArrayList<>();

        Date visit1 = simpleDateFormat.parse(date1);
        bahmniObservations.add(createBahmniObservation(visit1,"Pulse",new EncounterTransaction.Concept("uuid-pulse","very high pulse")));

        Map<String, Map<String, List<ConceptValue>>> obsTable = diseaseSummaryMapper.mapObservations(bahmniObservations);

        Map<String, List<ConceptValue>> dayValue = obsTable.get(date1);
        assertEquals(1, dayValue.size());
        assertEquals("very high pulse", dayValue.get("Pulse").get(0).getValue());

    }

    @Test
    public void shouldMapDrugOrders() throws ParseException, IOException {
        DiseaseSummaryMapper diseaseSummaryMapper = new DiseaseSummaryMapper();
        Map<String, Map<String, List<ConceptValue>>> drugOrderData = diseaseSummaryMapper.mapDrugOrders(mockDrugOrders(new String[]{"paracetamol", "2014-08-15"}, new String[]{"penicillin", "2014-09-11"}));

        assertNotNull(drugOrderData);
        assertEquals(2, drugOrderData.size());

        Map<String, List<ConceptValue>> firstDayValue = drugOrderData.get("2014-08-15");
        assertEquals(1, firstDayValue.size());
        assertEquals("paracetamol-500mg,10.0 mg,daily,SOS", firstDayValue.get("paracetamol").get(0).getValue());

        Map<String, List<ConceptValue>> secondDayValue = drugOrderData.get("2014-09-11");
        assertEquals(1, secondDayValue.size());
        assertEquals("penicillin-500mg,10.0 mg,daily,SOS", secondDayValue.get("penicillin").get(0).getValue());
    }

    @Test
    public void shouldMapDrugOrdersWithFlexibleDosing() throws ParseException, IOException {
        DiseaseSummaryMapper diseaseSummaryMapper = new DiseaseSummaryMapper();
        Map<String, Map<String, List<ConceptValue>>> drugOrderData = diseaseSummaryMapper.mapDrugOrders(mockDrugOrdersWithFlexibleDosing(new String[]{"paracetamol", "2014-08-15"}, new String[]{"penicillin", "2014-09-11"}));

        assertNotNull(drugOrderData);
        assertEquals(2, drugOrderData.size());

        Map<String, List<ConceptValue>> firstDayValue = drugOrderData.get("2014-08-15");
        assertEquals(1, firstDayValue.size());
        assertEquals("paracetamol-500mg,10.0 mg,1-0-1,SOS", firstDayValue.get("paracetamol").get(0).getValue());

        Map<String, List<ConceptValue>> secondDayValue = drugOrderData.get("2014-09-11");
        assertEquals(1, secondDayValue.size());
        assertEquals("penicillin-500mg,10.0 mg,1-0-1,SOS", secondDayValue.get("penicillin").get(0).getValue());
    }

    @Test
    public void shouldMapDrugOrdersWithoutAnyExceptionsWhenThereIsNoData() throws ParseException, IOException {
        try{
            DiseaseSummaryMapper diseaseSummaryMapper = new DiseaseSummaryMapper();
            Map<String, Map<String, List<ConceptValue>>> drugOrderData = diseaseSummaryMapper.mapDrugOrders(mockDrugOrdersWithoutAnyData(new String[]{"paracetamol", "2014-08-15"}, new String[]{"penicillin", "2014-09-11"}));

            assertNotNull(drugOrderData);
            assertEquals(2, drugOrderData.size());

            Map<String, List<ConceptValue>> firstDayValue = drugOrderData.get("2014-08-15");
            assertEquals(1, firstDayValue.size());
            assertEquals("", firstDayValue.get("paracetamol").get(0).getValue());

            Map<String, List<ConceptValue>> secondDayValue = drugOrderData.get("2014-09-11");
            assertEquals(1, secondDayValue.size());
            assertEquals("", secondDayValue.get("penicillin").get(0).getValue());
        }catch (Exception e){
            throw new RuntimeException("Should not throw any exception when drug orders dont have strength,dosage and freequency",e);
        }

    }

    private List<DrugOrder> mockDrugOrdersWithFlexibleDosing(String[]... drugInfoList) throws ParseException {
        List<DrugOrder> drugOrders = new ArrayList<>();
        for (String[] drugInfo : drugInfoList) {
            DrugOrder drugOrder = new DrugOrder();
            drugOrder.setConcept(createMRSConcept(drugInfo[0]));
            drugOrder.setEncounter(createEncounterWithVisitDateInfo(getDateFromString(drugInfo[1])));
            drugOrder.setDrug(createDrugWithNameAndStrength(drugInfo[0], drugInfo[0] + "-500mg"));
            drugOrder.setDose(10.0);
            Concept doseUnits = new Concept();
            doseUnits.setFullySpecifiedName(new ConceptName("mg",Locale.getDefault()));
            drugOrder.setDoseUnits(doseUnits);
            drugOrder.setAsNeeded(true);
            drugOrder.setDosingInstructions("{\"instructions\":\"Before meals\",\"morningDose\":1,\"afternoonDose\":0,\"eveningDose\":1}");
            drugOrder.setDosingType(FlexibleDosingInstructions.class);
            drugOrders.add(drugOrder);
        }
        return drugOrders;
    }

    private List<DrugOrder> mockDrugOrdersWithoutAnyData(String[]... drugInfoList) throws ParseException {
        List<DrugOrder> drugOrders = new ArrayList<>();
        for (String[] drugInfo : drugInfoList) {
            DrugOrder drugOrder = new DrugOrder();
            drugOrder.setConcept(createMRSConcept(drugInfo[0]));
            drugOrder.setEncounter(createEncounterWithVisitDateInfo(getDateFromString(drugInfo[1])));
            drugOrder.setDrug(createDrugWithNameAndStrength(drugInfo[0], ""));
            drugOrders.add(drugOrder);
        }
        return drugOrders;
    }

    @Test
    public void shouldMapLabOrders() throws ParseException {
        DiseaseSummaryMapper diseaseSummaryMapper = new DiseaseSummaryMapper();
        Map<String, Map<String, List<ConceptValue>>> labOrderData = diseaseSummaryMapper.mapLabResults(mockLabOrders());

        assertNotNull(labOrderData);
        assertEquals(2, labOrderData.size());

        Map<String, List<ConceptValue>> firstDayValue = labOrderData.get("2014-07-22");
        assertEquals(1, firstDayValue.size());
        assertEquals("120", firstDayValue.get("Blood glucose").get(0).getValue());

        Map<String, List<ConceptValue>> secondDayValue = labOrderData.get("2014-07-23");
        assertEquals(2, secondDayValue.size());
        assertEquals("140", secondDayValue.get("Blood glucose").get(0).getValue());
        assertEquals("3.0", secondDayValue.get("serum creatinine").get(0).getValue());

    }

    private List<LabOrderResult> mockLabOrders() throws ParseException {
        List<LabOrderResult> labOrderResults = new ArrayList<>();
        labOrderResults.add(createLabOrder("2014-07-22","Blood glucose","120"));
        labOrderResults.add(createLabOrder("2014-07-23","Blood glucose","140"));
        labOrderResults.add(createLabOrder("2014-07-23","serum creatinine","3.0"));
        return labOrderResults;
    }

    private LabOrderResult createLabOrder(String visitDate, String conceptName, String value) throws ParseException {

        LabOrderResult labOrderResult = new LabOrderResult();
        Date date = simpleDateFormat.parse(visitDate);
        labOrderResult.setVisitStartTime(date);
        labOrderResult.setResult(value);
        labOrderResult.setTestName(conceptName);
        return labOrderResult;
    }


    private List<DrugOrder> mockDrugOrders(String[]... drugInfoList) throws ParseException {
        List<DrugOrder> drugOrders = new ArrayList<>();
        for (String[] drugInfo : drugInfoList) {
            DrugOrder drugOrder = new DrugOrder();
            drugOrder.setConcept(createMRSConcept(drugInfo[0]));
            drugOrder.setEncounter(createEncounterWithVisitDateInfo(getDateFromString(drugInfo[1])));
            drugOrder.setDrug(createDrugWithNameAndStrength(drugInfo[0], drugInfo[0] + "-500mg"));
            drugOrder.setDose(10.0);
            Concept doseUnits = new Concept();
            doseUnits.setFullySpecifiedName(new ConceptName("mg",Locale.getDefault()));
            drugOrder.setDoseUnits(doseUnits);
            drugOrder.setAsNeeded(true);
            OrderFrequency frequency = new OrderFrequency();
            frequency.setConcept(createMRSConcept("daily"));
            drugOrder.setFrequency(frequency);
            drugOrders.add(drugOrder);
        }
        return drugOrders;
    }

    private Drug createDrugWithNameAndStrength(String drugName, String strength) {
        Drug drug = new Drug();
        drug.setName(drugName);
        drug.setStrength(strength);
        return drug;
    }

    private Encounter createEncounterWithVisitDateInfo(Date date) {
        Encounter encounter = new Encounter();
        Visit visit = new Visit();
        visit.setStartDatetime(date);
        encounter.setVisit(visit);
        return encounter;
    }

    private Date getDateFromString(String dateString) throws ParseException {
        return simpleDateFormat.parse(dateString);
    }

    private Concept createMRSConcept(String drugName) {
        Concept concept =  new Concept();
        concept.setFullySpecifiedName(new ConceptName(drugName, Locale.getDefault()));
        return concept;
    }


    private List<BahmniObservation> createBahmniObsList() throws ParseException {
        List<BahmniObservation> bahmniObservations =  new ArrayList<>();
        Date visit1 = simpleDateFormat.parse(date1);
        Date visit2 = simpleDateFormat.parse(date2);
        Date visit3 = simpleDateFormat.parse(date3);

        bahmniObservations.add(createBahmniObservation(visit1,"Temperature","101"));
        bahmniObservations.add(createBahmniObservation(visit1,"Pulse","90"));
        bahmniObservations.add(createBahmniObservation(visit2,"Pulse","100"));
        bahmniObservations.add(createBahmniObservation(visit3,"bp","120"));
        return bahmniObservations;
    }

    private BahmniObservation createBahmniObservation(Date visitStartTime, String conceptName, Object value) {
        BahmniObservation bahmniObservation = new BahmniObservation();
        bahmniObservation.setVisitStartDateTime(visitStartTime);
        bahmniObservation.setConcept(new EncounterTransaction.Concept("uuid-"+conceptName,conceptName));
        bahmniObservation.setValue(value);
        return bahmniObservation;
    }
}