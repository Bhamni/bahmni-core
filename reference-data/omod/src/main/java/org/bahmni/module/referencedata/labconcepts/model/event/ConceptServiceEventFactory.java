package org.bahmni.module.referencedata.labconcepts.model.event;

public class ConceptServiceEventFactory {
    public static final String CONCEPT_URL = "/openmrs/ws/rest/v1/reference-data/%s/%s";
    public static final String LAB = "lab";
    public static final String LAB_SAMPLE = "all-samples";
    public static final String SAMPLE = "sample";
    public static final String DEPARTMENT = "department";
    public static final String TEST = "test";
    public static final String PANEL = "panel";
    public static final String TESTS_AND_PANEL = "all-tests-and-panels";
    public static final String DRUG = "drug";
    public static final String RADIOLOGY = "radiology";
    public static final String SELLABLE = "sellable";

    public static ConceptServiceOperationEvent sampleEvent() {
        return new SampleEvent(CONCEPT_URL, LAB, SAMPLE);
    }

    public static ConceptServiceOperationEvent labConceptSetEvent() { return new AllLabSamplesEvent(CONCEPT_URL, LAB, LAB_SAMPLE); }

    public static ConceptServiceOperationEvent allTestsAndPanelsConceptSetEvent() { return new AllTestsPanelsConceptSetEvent(CONCEPT_URL, LAB, TESTS_AND_PANEL); }

    public static ConceptServiceOperationEvent departmentEvent() {
        return new DepartmentEvent(CONCEPT_URL, LAB, DEPARTMENT);
    }

    public static ConceptServiceOperationEvent panelEvent() {
        return new PanelEvent(CONCEPT_URL, LAB, PANEL);
    }

    public static ConceptServiceOperationEvent testEvent() {
        return new LabTestEvent(CONCEPT_URL, LAB, TEST);
    }

    public static ConceptServiceOperationEvent drugEvent() {
        return new DrugEvent(CONCEPT_URL, DRUG, DRUG);
    }
    public static ConceptServiceOperationEvent radiologyTestEvent() {
        return new RadiologyTestEvent(CONCEPT_URL, LAB, RADIOLOGY);
    }

    public static ConceptServiceOperationEvent sellableTypeEvent() {
        return new SellableTypeEvent(CONCEPT_URL, SELLABLE);
    }
}
