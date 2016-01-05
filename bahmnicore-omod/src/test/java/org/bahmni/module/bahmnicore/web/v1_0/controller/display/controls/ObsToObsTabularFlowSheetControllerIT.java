package org.bahmni.module.bahmnicore.web.v1_0.controller.display.controls;

import org.bahmni.module.bahmnicore.web.v1_0.BaseIntegrationTest;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.module.bahmniemrapi.pivottable.contract.PivotRow;
import org.openmrs.module.bahmniemrapi.pivottable.contract.PivotTable;
import org.openmrs.module.emrapi.encounter.domain.EncounterTransaction;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.*;

public class ObsToObsTabularFlowSheetControllerIT extends BaseIntegrationTest {
    @Autowired
    private ObsToObsTabularFlowSheetController obsToObsPivotTableController;

    @Before
    public void setUp() throws Exception {
        executeDataSet("flowSheetTableDataSet.xml");
        executeDataSet("flowSheetDataSetWithMultipleLevelConcepts.xml");
        executeDataSet("flowSheetTableDataSetForConceptDetails.xml");
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
        assertEquals(rows.get(0).getValue("FAVORITE FOOD, NON-CODED").get(0).getValueAsString(), "Favorite");
        assertEquals(rows.get(0).getValue("FOOD ASSISTANCE").get(0).getValueAsString(), "Yes");
        assertEquals(rows.get(0).getValue("DATE OF FOOD ASSISTANCE").get(0).getValueAsString(), "2008-08-14 00:00:00");
        assertNotNull(pivotTable.getHeaders());
        assertNotEquals("Should not be empty list", Collections.EMPTY_LIST, pivotTable.getHeaders());
    }

    @Test
    public void shouldReturnOnlyConceptNamesWhichArePassed() throws Exception {
        PivotTable pivotTable = deserialize(handle(newGetRequest("/rest/v1/bahmnicore/observations/flowSheet",
                new Parameter("patientUuid", "1a246ed5-3c11-11de-a0ba-001ed98eb67a"),
                new Parameter("numberOfVisits", "1"),
                new Parameter("conceptSet", "FOOD CONSTRUCT"),
                new Parameter("groupByConcept", "FOOD ASSISTANCE"),
                new Parameter("conceptNames", "FOOD ASSISTANCE"),
                new Parameter("conceptNames", "DATE OF FOOD ASSISTANCE"),
                new Parameter("name", null)
        )), PivotTable.class);

        List<PivotRow> rows = pivotTable.getRows();
        assertEquals(1, rows.size());
        assertEquals(rows.get(0).getValue("FOOD ASSISTANCE").get(0).getValueAsString(), "Yes");
        assertEquals(rows.get(0).getValue("DATE OF FOOD ASSISTANCE").get(0).getValueAsString(), "2008-08-14 00:00:00");
        assertNull("Should not return this concept", rows.get(0).getValue("FAVORITE FOOD, NON-CODED"));
        assertNotNull(pivotTable.getHeaders());
        assertNotEquals("Should not be empty list", Collections.EMPTY_LIST, pivotTable.getHeaders());
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
        assertEquals("2008-08-14 00:00:00", rows.get(0).getValue("SPECIMEN COLLECTION DATE").get(0).getValueAsString());
        assertEquals("56.0", rows.get(0).getValue("WEIGHT (KG)").get(0).getValueAsString());
        assertNull("Should not return this concept", rows.get(0).getValue("BACTERIOLOGY ADDITIONAL ATTRIBUTES"));
        assertNull("Should not return this concept", rows.get(0).getValue("BACTERIOLOGY RESULTS"));
        assertNotNull(pivotTable.getHeaders());
        assertNotEquals("Should not be empty list", Collections.EMPTY_LIST, pivotTable.getHeaders());
    }

    @Test
    public void shouldGetZeroRowsIfGroupByConceptSameAsConceptName() throws Exception {
        executeDataSet("flowSheetTableDataSetForInitialAndLatestCount.xml");
        PivotTable pivotTable = deserialize(handle(newGetRequest("/rest/v1/bahmnicore/observations/flowSheet",
                new Parameter("patientUuid", "1a246ed5-3c11-11de-a0ba-001ed2aeb66u"),
                new Parameter("conceptSet", "Vitals"),
                new Parameter("groupByConcept", "Temperature Data"),
                new Parameter("conceptNames", "Temperature Data"),
                new Parameter("initialCount", "2"),
                new Parameter("latestCount","0"),
                new Parameter("numberOfVisits","1")
        )), PivotTable.class);

        List<PivotRow> rows = pivotTable.getRows();
        assertEquals(1, pivotTable.getHeaders().size());
        assertEquals(0, rows.size());
    }

    @Test
    public void shouldGetHeadersWithNormalRangeWhenHeaderHasANumericConcept() throws Exception {
        executeDataSet("flowSheetTableDataSetForInitialAndLatestCount.xml");
        PivotTable pivotTable = deserialize(handle(newGetRequest("/rest/v1/bahmnicore/observations/flowSheet",
            new Parameter("patientUuid", "1a246ed5-3c11-11de-a0ba-001ed2aeb66u"),
            new Parameter("conceptSet", "Vitals"),
            new Parameter("groupByConcept", "Temperature Data"),
            new Parameter("conceptNames", "Temperature Data"),
            new Parameter("initialCount", "2"),
            new Parameter("latestCount","0"),
            new Parameter("numberOfVisits","1")
        )), PivotTable.class);

        Set<EncounterTransaction.Concept> headers = pivotTable.getHeaders();
        assertEquals(1, headers.size());

        EncounterTransaction.Concept temperatureConcept = (EncounterTransaction.Concept) headers.toArray()[0];
        assertEquals(108, temperatureConcept.getHiNormal(),0);
        assertEquals(98, temperatureConcept.getLowNormal(),0);
    }
}

