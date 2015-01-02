package org.bahmni.module.bahmnicoreui.contract;

import java.util.*;

public class DiseaseSummaryData {

    private Map<String,Map<String, List<ConceptValue>>> tabularData = new LinkedHashMap<>();
    private Set<String> conceptNames = new LinkedHashSet<>();

    public Map<String, Map<String, List<ConceptValue>>> getTabularData() {
        return tabularData;
    }

    public void setTabularData(Map<String, Map<String, List<ConceptValue>>> tabularData) {
        this.tabularData = tabularData;
    }

    public void addTabularData(Map<String, Map<String, List<ConceptValue>>> newTable){
        for (String visitDate : newTable.keySet()) {
            Map<String, List<ConceptValue>> valuesForVisit = getValuesForVisit(visitDate);//tabularData.get(visitDate);
            valuesForVisit.putAll(newTable.get(visitDate));
        }
    }

    private Map<String, List<ConceptValue>> getValuesForVisit(String visitDate) {
        Map<String, List<ConceptValue>> valuesForVisit = tabularData.get(visitDate);
        if( valuesForVisit == null){
            valuesForVisit = new LinkedHashMap<>();
            tabularData.put(visitDate,valuesForVisit);
        }
        return valuesForVisit;
    }

    public void setConceptNames(Set<String> conceptNames) {
        this.conceptNames = conceptNames;
    }

    public Set<String> getConceptNames() {
        return conceptNames;
    }

    public void addConceptNames(Set<String> conceptNames) {
        this.conceptNames.addAll(conceptNames);
    }

    public void concat(DiseaseSummaryData diseaseSummaryData){
        addTabularData(diseaseSummaryData.getTabularData());
        addConceptNames(diseaseSummaryData.getConceptNames());
    }
}
