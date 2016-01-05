package org.bahmni.module.bahmnicore.web.v1_0.mapper;

import org.junit.Test;
import org.omg.CORBA.BAD_CONTEXT;
import org.openmrs.module.bahmniemrapi.builder.BahmniObservationBuilder;
import org.openmrs.module.bahmniemrapi.builder.ETConceptBuilder;
import org.openmrs.module.bahmniemrapi.encountertransaction.contract.BahmniObservation;
import org.openmrs.module.bahmniemrapi.pivottable.contract.PivotTable;
import org.openmrs.module.emrapi.encounter.domain.EncounterTransaction;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class BahmniObservationsToTabularViewMapperTest {

    private BahmniObservationsToTabularViewMapper bahmniObservationsToTabularViewMapper = new BahmniObservationsToTabularViewMapper();
    private String groupByConcept = null;

    @Test
    public void shouldReturnAllObservationsInTabularFormatIfTheConceptNamesAreNotPassed() throws Exception {
        EncounterTransaction.Concept heightConcept = new ETConceptBuilder().withName("HEIGHT").withUuid("height uuid").withSet(false).withClass("Misc").build();
        EncounterTransaction.Concept weightConcept = new ETConceptBuilder().withName("WEIGHT").withUuid("weight uuid").withSet(false).withClass("Misc").build();
        EncounterTransaction.Concept vitalsConcept = new ETConceptBuilder().withName("Vitals").withUuid("vitals uuid").withSet(true).withClass("Misc").build();
        BahmniObservation height = new BahmniObservationBuilder().withConcept(heightConcept).withValue(170).build();
        BahmniObservation weight = new BahmniObservationBuilder().withConcept(weightConcept).withValue(80).build();
        BahmniObservation vitals = new BahmniObservationBuilder().withConcept(vitalsConcept).withGroupMember(height).withGroupMember(weight).build();
        ArrayList<BahmniObservation> bahmniObservations = new ArrayList<>();
        bahmniObservations.add(vitals);

        Set<EncounterTransaction.Concept> conceptNames = new HashSet<>();
        conceptNames.add(heightConcept);
        conceptNames.add(weightConcept);
        PivotTable pivotTable = bahmniObservationsToTabularViewMapper.constructTable(conceptNames, bahmniObservations, groupByConcept);

        assertNotNull(pivotTable);
        assertEquals(1, pivotTable.getRows().size());
        assertEquals(conceptNames, pivotTable.getHeaders());
        assertEquals(170, pivotTable.getRows().get(0).getValue("HEIGHT").get(0).getValue());
        assertEquals(80, pivotTable.getRows().get(0).getValue("WEIGHT").get(0).getValue());
    }

    @Test
    public void shouldReturnObservationsInTabularFormatForOnlyTheConceptNamesArePassed() throws Exception {
        EncounterTransaction.Concept heightConcept = new ETConceptBuilder().withName("HEIGHT").withUuid("height uuid").withSet(false).withClass("Misc").build();
        EncounterTransaction.Concept weightConcept = new ETConceptBuilder().withName("WEIGHT").withUuid("weight uuid").withSet(false).withClass("Misc").build();
        EncounterTransaction.Concept vitalsConcept = new ETConceptBuilder().withName("Vitals").withUuid("vitals uuid").withSet(true).withClass("Misc").build();
        BahmniObservation height = new BahmniObservationBuilder().withConcept(heightConcept).withValue(170).build();
        BahmniObservation weight = new BahmniObservationBuilder().withConcept(weightConcept).withValue(80).build();
        BahmniObservation vitals = new BahmniObservationBuilder().withConcept(vitalsConcept).withGroupMember(height).withGroupMember(weight).build();
        ArrayList<BahmniObservation> bahmniObservations = new ArrayList<>();
        bahmniObservations.add(vitals);

        Set<EncounterTransaction.Concept> conceptNames = new HashSet<>();
        conceptNames.add(heightConcept);
        PivotTable pivotTable = bahmniObservationsToTabularViewMapper.constructTable(conceptNames, bahmniObservations, groupByConcept);

        assertNotNull(pivotTable);
        assertEquals(1, pivotTable.getRows().size());
        assertEquals(conceptNames, pivotTable.getHeaders());
        assertEquals(170, pivotTable.getRows().get(0).getValue("HEIGHT").get(0).getValue());
    }

    @Test
    public void shouldReturnOnlyLeafObservationsInTabularFormat() throws Exception {
        EncounterTransaction.Concept heightConcept = new ETConceptBuilder().withName("HEIGHT").withUuid("height uuid").withSet(false).withClass("Misc").build();
        EncounterTransaction.Concept weightConcept = new ETConceptBuilder().withName("WEIGHT").withUuid("weight uuid").withSet(false).withClass("Misc").build();
        EncounterTransaction.Concept vitalsConcept = new ETConceptBuilder().withName("Vitals").withUuid("vitals uuid").withSet(true).withClass("Misc").build();
        EncounterTransaction.Concept systolicConcept = new ETConceptBuilder().withName("Systolic").withUuid("Systolic uuid").withSet(false).withClass("Misc").build();
        EncounterTransaction.Concept diastolicConcept = new ETConceptBuilder().withName("Diastolic").withUuid("Diastolic uuid").withSet(false).withClass("Misc").build();
        EncounterTransaction.Concept bpConcept = new ETConceptBuilder().withName("BP").withUuid("BP uuid").withSet(true).withClass("Misc").build();

        BahmniObservation systolic = new BahmniObservationBuilder().withConcept(systolicConcept).withValue(120).build();
        BahmniObservation diastolic = new BahmniObservationBuilder().withConcept(diastolicConcept).withValue(90).build();
        BahmniObservation bp = new BahmniObservationBuilder().withConcept(bpConcept).withGroupMember(systolic).withGroupMember(diastolic).build();
        BahmniObservation height = new BahmniObservationBuilder().withConcept(heightConcept).withValue(170).build();
        BahmniObservation weight = new BahmniObservationBuilder().withConcept(weightConcept).withValue(80).build();
        BahmniObservation vitals = new BahmniObservationBuilder().withConcept(vitalsConcept).withGroupMember(height).withGroupMember(weight).withGroupMember(bp).build();
        ArrayList<BahmniObservation> bahmniObservations = new ArrayList<>();
        bahmniObservations.add(vitals);

        Set<EncounterTransaction.Concept> conceptNames = new HashSet<>();
        conceptNames.add(heightConcept);
        conceptNames.add(weightConcept);
        conceptNames.add(systolicConcept);
        conceptNames.add(diastolicConcept);

        PivotTable pivotTable = bahmniObservationsToTabularViewMapper.constructTable(conceptNames, bahmniObservations, groupByConcept);

        assertNotNull(pivotTable);
        assertEquals(1, pivotTable.getRows().size());
        assertEquals(conceptNames, pivotTable.getHeaders());
        assertEquals(170, pivotTable.getRows().get(0).getValue("HEIGHT").get(0).getValue());
        assertEquals(80, pivotTable.getRows().get(0).getValue("WEIGHT").get(0).getValue());
        assertEquals(120, pivotTable.getRows().get(0).getValue("Systolic").get(0).getValue());
        assertEquals(90, pivotTable.getRows().get(0).getValue("Diastolic").get(0).getValue());
    }

    @Test
    public void shouldReturnMultipleRowsIfThereAreMultipleRootObservations() throws Exception {
        EncounterTransaction.Concept heightConcept = new ETConceptBuilder().withName("HEIGHT").withUuid("height uuid").withSet(false).withClass("Misc").build();
        EncounterTransaction.Concept weightConcept = new ETConceptBuilder().withName("WEIGHT").withUuid("weight uuid").withSet(false).withClass("Misc").build();
        EncounterTransaction.Concept vitalsConcept = new ETConceptBuilder().withName("Vitals").withUuid("vitals uuid").withSet(true).withClass("Misc").build();
        BahmniObservation firstHeight = new BahmniObservationBuilder().withConcept(heightConcept).withValue(170).build();
        BahmniObservation firstWeight = new BahmniObservationBuilder().withConcept(weightConcept).withValue(80).build();
        BahmniObservation firstVitals = new BahmniObservationBuilder().withConcept(vitalsConcept).withGroupMember(firstHeight).withGroupMember(firstWeight).build();


        BahmniObservation secondHeight = new BahmniObservationBuilder().withConcept(heightConcept).withValue(180).build();
        BahmniObservation secondWeight = new BahmniObservationBuilder().withConcept(weightConcept).withValue(90).build();
        BahmniObservation secondVitals = new BahmniObservationBuilder().withConcept(vitalsConcept).withGroupMember(secondHeight).withGroupMember(secondWeight).build();
        ArrayList<BahmniObservation> bahmniObservations = new ArrayList<>();
        bahmniObservations.add(firstVitals);
        bahmniObservations.add(secondVitals);

        HashSet<EncounterTransaction.Concept> conceptNames = new HashSet<>();
        conceptNames.add(heightConcept);
        conceptNames.add(weightConcept);

        PivotTable pivotTable = bahmniObservationsToTabularViewMapper.constructTable(conceptNames, bahmniObservations, groupByConcept);

        assertNotNull(pivotTable);
        assertEquals(2, pivotTable.getRows().size());
        assertEquals(conceptNames, pivotTable.getHeaders());
        assertEquals(170, pivotTable.getRows().get(0).getValue("HEIGHT").get(0).getValue());
        assertEquals(80, pivotTable.getRows().get(0).getValue("WEIGHT").get(0).getValue());
        assertEquals(180, pivotTable.getRows().get(1).getValue("HEIGHT").get(0).getValue());
        assertEquals(90, pivotTable.getRows().get(1).getValue("WEIGHT").get(0).getValue());
    }

    @Test
    public void shouldRetrunEmptyTableIfThereAreNoObservations() throws Exception {
        PivotTable pivotTable = bahmniObservationsToTabularViewMapper.constructTable(null, null, groupByConcept);

        assertNotNull(pivotTable);
        assertEquals(0, pivotTable.getRows().size());
        assertEquals(0, pivotTable.getHeaders().size());
    }

    @Test
    public void shouldRetrunEmptyTableIfAllTheObservationValuesAreNull() throws Exception {
        EncounterTransaction.Concept heightConcept = new ETConceptBuilder().withName("HEIGHT").withUuid("height uuid").withSet(false).withClass("Misc").build();
        EncounterTransaction.Concept weightConcept = new ETConceptBuilder().withName("WEIGHT").withUuid("weight uuid").withSet(false).withClass("Misc").build();

        BahmniObservation height = new BahmniObservationBuilder().withConcept(heightConcept).withValue(null).build();
        BahmniObservation weight = new BahmniObservationBuilder().withConcept(weightConcept).withValue(null).build();
        ArrayList<BahmniObservation> bahmniObservations = new ArrayList<>();
        bahmniObservations.add(height);
        bahmniObservations.add(weight);

        Set<EncounterTransaction.Concept> conceptNames = new HashSet<>();
        conceptNames.add(heightConcept);
        conceptNames.add(weightConcept);
        PivotTable pivotTable = bahmniObservationsToTabularViewMapper.constructTable(conceptNames, bahmniObservations, "test concept");

        assertNotNull(pivotTable);
        assertEquals(0, pivotTable.getRows().size());
        assertEquals(2, pivotTable.getHeaders().size());
    }
}