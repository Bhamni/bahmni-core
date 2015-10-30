package org.bahmni.module.bahmnicore.web.v1_0.controller.display.controls;

import org.bahmni.module.bahmnicore.web.v1_0.BaseIntegrationTest;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.module.bahmniemrapi.pivottable.contract.PivotRow;
import org.openmrs.module.bahmniemrapi.pivottable.contract.PivotTable;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Collections;
import java.util.List;

import static org.junit.Assert.*;

public class ObsToObsTabularFlowSheetControllerIT extends BaseIntegrationTest {

    @Before
    public void setUp() throws Exception {
        executeDataSet("obstoobsflowsheet/flowSheetTableDataSet.xml");
        executeDataSet("obstoobsflowsheet/flowSheetDataSetWithMultipleLevelConcepts.xml");
        executeDataSet("obstoobsflowsheet/flowSheetTableDataSetForConceptDetails.xml");
    }

    @Test
    public void shouldReturnAllTheMembersIfTheConceptNamesAreNotPassed() throws Exception {
        PivotTable pivotTable = deserialize(handle(newGetRequest("/rest/v1/bahmnicore/observations/flowSheet",
                new Parameter("patientUuid", "1a246ed5-3c11-11de-a0ba-001ed98eb67a"),
                new Parameter("numberOfVisits", "1"),
                new Parameter("conceptSet", "FOOD CONSTRUCT"),
                new Parameter("groupByConcept", "FOOD ASSISTANCE")
        )), PivotTable.class);

        List<PivotRow> rows = pivotTable.getRows();
        assertEquals(1, rows.size());
        assertEquals(rows.get(0).getValue("FAVORITE FOOD, NON-CODED").getValueAsString(), "Favorite");
        assertEquals(rows.get(0).getValue("FOOD ASSISTANCE").getValueAsString(), "Yes");
        assertEquals(rows.get(0).getValue("DATE OF FOOD ASSISTANCE").getValueAsString(), "2008-08-14 00:00:00");
        assertNotNull(pivotTable.getHeaders());
        assertNotEquals("Should not be empty list", Collections.EMPTY_LIST, pivotTable.getHeaders());
        assertArrayEquals(new String[]{"FAVORITE FOOD, NON-CODED", "FOOD ASSISTANCE", "DATE OF FOOD ASSISTANCE"}, pivotTable.getHeaders().toArray());
    }

    @Test
    public void shouldReturnOnlyConceptNamesWhichArePassed() throws Exception {
        PivotTable pivotTable = deserialize(handle(newGetRequest("/rest/v1/bahmnicore/observations/flowSheet",
                new Parameter("patientUuid", "1a246ed5-3c11-11de-a0ba-001ed98eb67a"),
                new Parameter("numberOfVisits", "1"),
                new Parameter("conceptSet", "FOOD CONSTRUCT"),
                new Parameter("groupByConcept", "FOOD ASSISTANCE"),
                new Parameter("conceptNames", "FOOD ASSISTANCE"),
                new Parameter("conceptNames", "DATE OF FOOD ASSISTANCE")
        )), PivotTable.class);

        List<PivotRow> rows = pivotTable.getRows();
        assertEquals(1, rows.size());
        assertEquals(rows.get(0).getValue("FOOD ASSISTANCE").getValueAsString(), "Yes");
        assertEquals(rows.get(0).getValue("DATE OF FOOD ASSISTANCE").getValueAsString(), "2008-08-14 00:00:00");
        assertNull("Should not return this concept", rows.get(0).getValue("FAVORITE FOOD, NON-CODED"));
        assertNotNull(pivotTable.getHeaders());
        assertNotEquals("Should not be empty list", Collections.EMPTY_LIST, pivotTable.getHeaders());
        assertArrayEquals(pivotTable.getHeaders().toArray(), new String[]{"FOOD ASSISTANCE", "DATE OF FOOD ASSISTANCE"});
    }

    @Test
    public void shouldGetAllMemberNamesAsHeadersWhenConceptNamesAreNotPassed() throws Exception {
        PivotTable pivotTable = deserialize(handle(newGetRequest("/rest/v1/bahmnicore/observations/flowSheet",
                new Parameter("patientUuid", "1a246ed5-3c11-11de-abaa-001ed98eb67a"),
                new Parameter("numberOfVisits", "-1"),
                new Parameter("conceptSet", "FOOD CONSTRUCT"),
                new Parameter("groupByConcept", "FOOD ASSISTANCE")
        )), PivotTable.class);

        List<PivotRow> rows = pivotTable.getRows();
        assertEquals(2, rows.size());
        assertNotNull(pivotTable.getHeaders());
        assertNotEquals("Should not be empty list", Collections.EMPTY_LIST, pivotTable.getHeaders());
        assertArrayEquals(new String[]{"FAVORITE FOOD, NON-CODED", "FOOD ASSISTANCE", "DATE OF FOOD ASSISTANCE"}, pivotTable.getHeaders().toArray());
    }

    @Test
    public void shouldGetAllChildMembersAsColumnsIfTheConceptIsSet() throws Exception {
        PivotTable pivotTable = deserialize(handle(newGetRequest("/rest/v1/bahmnicore/observations/flowSheet",
                new Parameter("patientUuid", "1a246ed5-3c11-11de-a0ba-001ed98eb67b"),
                new Parameter("conceptSet", "BACTERIOLOGY CONCEPT SET"),
                new Parameter("groupByConcept", "SPECIMEN COLLECTION DATE")
        )), PivotTable.class);

        List<PivotRow> rows = pivotTable.getRows();
        assertEquals(1, rows.size());
        assertEquals("2008-08-14 00:00:00", rows.get(0).getValue("SPECIMEN COLLECTION DATE").getValueAsString());
        assertEquals("56.0", rows.get(0).getValue("WEIGHT (KG)").getValueAsString());
        assertNull("Should not return this concept", rows.get(0).getValue("BACTERIOLOGY ADDITIONAL ATTRIBUTES"));
        assertNull("Should not return this concept", rows.get(0).getValue("BACTERIOLOGY RESULTS"));
        assertNotNull(pivotTable.getHeaders());
        assertNotEquals("Should not be empty list", Collections.EMPTY_LIST, pivotTable.getHeaders());
        assertArrayEquals(new String[]{"SPECIMEN COLLECTION DATE", "WEIGHT (KG)"}, pivotTable.getHeaders().toArray());
    }

    @Test
    public void shouldFetchConceptDetailsConcepts() throws Exception {
        PivotTable pivotTable = deserialize(handle(newGetRequest("/rest/v1/bahmnicore/observations/flowSheet",
                new Parameter("patientUuid", "1a246ed5-3c11-11de-a0ba-001edlaeb67a"),
                new Parameter("conceptSet", "Vitals"),
                new Parameter("groupByConcept", "Temperature Data"),
                new Parameter("conceptNames", "Temperature Data")
        )), PivotTable.class);

        List<PivotRow> rows = pivotTable.getRows();
        assertEquals(1, pivotTable.getHeaders().size());
        System.out.println(pivotTable.getHeaders());
        assertEquals(1, rows.size());
        assertEquals("98.0", rows.get(0).getValue("Temperature Data").getValueAsString());
        assertTrue(rows.get(0).getValue("Temperature Data").isAbnormal());
    }
}