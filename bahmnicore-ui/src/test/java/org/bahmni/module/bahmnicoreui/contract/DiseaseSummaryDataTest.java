package org.bahmni.module.bahmnicoreui.contract;

import org.junit.Test;

import java.util.*;

import static java.util.AbstractMap.SimpleEntry;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class DiseaseSummaryDataTest {

    @Test
    public void shouldAddTabularDataToExistingTabularData() {
        DiseaseSummaryData diseaseSummaryData = new DiseaseSummaryData();
        Map<String, Map<String, List<ConceptValue>>> existingTabularData = new LinkedHashMap<>();
        existingTabularData.put("12-12-2012", createConceptValueMap(new SimpleEntry<>("Blood Pressure", "120/80"), new SimpleEntry<>("Temperature", "101")));
        existingTabularData.put("13-12-2012", createConceptValueMap(new SimpleEntry<>("pulse", "100"), new SimpleEntry<>("Temperature", "104")));
        diseaseSummaryData.addTabularData(existingTabularData);

        Map<String, Map<String, List<ConceptValue>>> newTabularData = new LinkedHashMap<>();
        newTabularData.put("11-12-2012", createConceptValueMap(new SimpleEntry<>("Paracetamol", "500mg"), new SimpleEntry<>("cetrizine", "200mg")));
        newTabularData.put("13-12-2012", createConceptValueMap(new SimpleEntry<>("White blood cells", "100000"), new SimpleEntry<>("serum creatinine", "5")));

        diseaseSummaryData.addTabularData(newTabularData);

        Map<String, Map<String, List<ConceptValue>>> tabularData = diseaseSummaryData.getTabularData();
        assertEquals(3, tabularData.size());
        assertEquals(4, tabularData.get("13-12-2012").size());

        assertEquals("500mg", tabularData.get("11-12-2012").get("Paracetamol").get(0).getValue());
        assertEquals("200mg", tabularData.get("11-12-2012").get("cetrizine").get(0).getValue());

        assertEquals("100000", tabularData.get("13-12-2012").get("White blood cells").get(0).getValue());
        assertEquals("5", tabularData.get("13-12-2012").get("serum creatinine").get(0).getValue());


    }

    @Test
    public void shouldAddConceptNamesToExistingSetOfConceptNames() {
        DiseaseSummaryData diseaseSummaryData = new DiseaseSummaryData();
        Set<String> existingConceptNames = new LinkedHashSet<>();
        existingConceptNames.add("blood");
        existingConceptNames.add("fluid");

        Set<String> newConceptNames = new LinkedHashSet<>();
        newConceptNames.add("temperature");
        diseaseSummaryData.addConceptNames(existingConceptNames);
        diseaseSummaryData.addConceptNames(newConceptNames);

        assertEquals(diseaseSummaryData.getConceptNames().size(), 3);
        assertTrue(diseaseSummaryData.getConceptNames().contains("temperature"));
    }

    private Map<String, List<ConceptValue>> createConceptValueMap(Map.Entry<String, String>... values) {
        Map<String, List<ConceptValue>> conceptValuesForDate = new LinkedHashMap<>();
        for (Map.Entry<String, String> concept : values) {
            List<ConceptValue> conceptValues = new ArrayList<>();
            ConceptValue value = new ConceptValue();
            value.setValue(concept.getValue());
            conceptValues.add(value);
            conceptValuesForDate.put(concept.getKey(), conceptValues);
        }
        return conceptValuesForDate;
    }
}