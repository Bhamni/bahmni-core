package org.bahmni.module.bahmnicore.web.v1_0.mapper;

import org.junit.Test;
import org.openmrs.module.bahmniemrapi.builder.BahmniObservationBuilder;
import org.openmrs.module.bahmniemrapi.encountertransaction.contract.BahmniObservation;
import org.openmrs.module.bahmniemrapi.pivottable.contract.PivotTable;

import java.util.*;

import static org.junit.Assert.*;

public class BahmniObservationsToTabularViewMapperTest {

    private BahmniObservationsToTabularViewMapper bahmniObservationsToTabularViewMapper = new BahmniObservationsToTabularViewMapper();

    @Test
    public void shouldReturnAllObservationsInTabularFormatIfTheConceptNamesAreNotPassed() throws Exception {
        BahmniObservation height = new BahmniObservationBuilder().withConcept("HEIGHT", false).withValue(170).build();
        BahmniObservation weight = new BahmniObservationBuilder().withConcept("WEIGHT", false).withValue(80).build();
        BahmniObservation vitals = new BahmniObservationBuilder().withConcept("Vitals", true).withGroupMember(height).withGroupMember(weight).build();
        ArrayList<BahmniObservation> bahmniObservations = new ArrayList<>();
        bahmniObservations.add(vitals);

        HashSet<String> conceptNames = new HashSet<>();
        conceptNames.add("HEIGHT");
        conceptNames.add("WEIGHT");
        PivotTable pivotTable = bahmniObservationsToTabularViewMapper.constructTable("WEIGHT", conceptNames, bahmniObservations);

        assertNotNull(pivotTable);
        assertEquals(1, pivotTable.getRows().size());
        assertArrayEquals(new String[]{"WEIGHT", "HEIGHT"}, pivotTable.getHeaders().toArray());
        assertEquals(170, pivotTable.getRows().get(0).getValue("HEIGHT").getValue());
        assertEquals(80, pivotTable.getRows().get(0).getValue("WEIGHT").getValue());
    }

    @Test
    public void shouldReturnObservationsInTabularFormatForOnlyTheConceptNamesArePassed() throws Exception {
        BahmniObservation height = new BahmniObservationBuilder().withConcept("HEIGHT", false).withValue(170).build();
        BahmniObservation weight = new BahmniObservationBuilder().withConcept("WEIGHT", false).withValue(80).build();
        BahmniObservation vitals = new BahmniObservationBuilder().withConcept("Vitals", true).withGroupMember(height).withGroupMember(weight).build();
        ArrayList<BahmniObservation> bahmniObservations = new ArrayList<>();
        bahmniObservations.add(vitals);

        Set<String> conceptNames = new HashSet<>();
        conceptNames.add("HEIGHT");
        PivotTable pivotTable = bahmniObservationsToTabularViewMapper.constructTable("WEIGHT", conceptNames, bahmniObservations);

        assertNotNull(pivotTable);
        assertEquals(1, pivotTable.getRows().size());
        assertArrayEquals(new String[]{"HEIGHT"}, pivotTable.getHeaders().toArray());
        assertEquals(170, pivotTable.getRows().get(0).getValue("HEIGHT").getValue());
    }

    @Test
    public void shouldReturnOnlyLeafObservationsInTabularFormat() throws Exception {
        BahmniObservation height = new BahmniObservationBuilder().withConcept("HEIGHT", false).withValue(170).build();
        BahmniObservation weight = new BahmniObservationBuilder().withConcept("WEIGHT", false).withValue(80).build();
        BahmniObservation systolic = new BahmniObservationBuilder().withConcept("Systolic", false).withValue(120).build();
        BahmniObservation diastolic = new BahmniObservationBuilder().withConcept("Diastolic", false).withValue(90).build();
        BahmniObservation bp = new BahmniObservationBuilder().withConcept("BP", true).withGroupMember(systolic).withGroupMember(diastolic).build();
        BahmniObservation vitals = new BahmniObservationBuilder().withConcept("Vitals", true).withGroupMember(height).withGroupMember(weight).withGroupMember(bp).build();
        ArrayList<BahmniObservation> bahmniObservations = new ArrayList<>();
        bahmniObservations.add(vitals);

        HashSet<String> conceptNames = new HashSet<>();
        conceptNames.add("HEIGHT");
        conceptNames.add("WEIGHT");
        conceptNames.add("Systolic");
        conceptNames.add("Diastolic");

        PivotTable pivotTable = bahmniObservationsToTabularViewMapper.constructTable("WEIGHT", conceptNames, bahmniObservations);

        assertNotNull(pivotTable);
        assertEquals(1, pivotTable.getRows().size());
        assertArrayEquals(new String[]{"WEIGHT", "Systolic", "HEIGHT", "Diastolic"}, pivotTable.getHeaders().toArray());
        assertEquals(170, pivotTable.getRows().get(0).getValue("HEIGHT").getValue());
        assertEquals(80, pivotTable.getRows().get(0).getValue("WEIGHT").getValue());
        assertEquals(120, pivotTable.getRows().get(0).getValue("Systolic").getValue());
        assertEquals(90, pivotTable.getRows().get(0).getValue("Diastolic").getValue());
    }

    @Test
    public void shouldReturnMultipleRowsIfThereAreMultipleRootObservations() throws Exception {
        BahmniObservation height = new BahmniObservationBuilder().withConcept("HEIGHT", false).withValue(170).build();
        BahmniObservation weight = new BahmniObservationBuilder().withConcept("WEIGHT", false).withValue(80).build();
        BahmniObservation vitals = new BahmniObservationBuilder().withConcept("Vitals", true).withGroupMember(height).withGroupMember(weight).build();

        BahmniObservation secondHeight = new BahmniObservationBuilder().withConcept("HEIGHT", false).withValue(180).build();
        BahmniObservation secondWeight = new BahmniObservationBuilder().withConcept("WEIGHT", false).withValue(90).build();
        BahmniObservation secondVitals = new BahmniObservationBuilder().withConcept("Vitals", true).withGroupMember(secondHeight).withGroupMember(secondWeight).build();
        ArrayList<BahmniObservation> bahmniObservations = new ArrayList<>();
        bahmniObservations.add(vitals);
        bahmniObservations.add(secondVitals);

        HashSet<String> conceptNames = new HashSet<>();
        conceptNames.add("HEIGHT");
        conceptNames.add("WEIGHT");

        PivotTable pivotTable = bahmniObservationsToTabularViewMapper.constructTable("WEIGHT", conceptNames, bahmniObservations);

        assertNotNull(pivotTable);
        assertEquals(2, pivotTable.getRows().size());
        assertArrayEquals(new String[]{"WEIGHT", "HEIGHT"}, pivotTable.getHeaders().toArray());
        assertEquals(170, pivotTable.getRows().get(0).getValue("HEIGHT").getValue());
        assertEquals(80, pivotTable.getRows().get(0).getValue("WEIGHT").getValue());
        assertEquals(180, pivotTable.getRows().get(1).getValue("HEIGHT").getValue());
        assertEquals(90, pivotTable.getRows().get(1).getValue("WEIGHT").getValue());
    }

    @Test
    public void shouldRetrunEmptyTableIfThereAreNoObservations() throws Exception {
        PivotTable pivotTable = bahmniObservationsToTabularViewMapper.constructTable("WEIGHT", null, null);

        assertNotNull(pivotTable);
        assertEquals(0, pivotTable.getRows().size());
        assertEquals(0, pivotTable.getHeaders().size());
    }
}