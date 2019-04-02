package org.bahmni.module.referencedata.labconcepts.mapper;

import org.bahmni.module.referencedata.labconcepts.contract.AllTestsAndPanels;
import org.bahmni.module.referencedata.labconcepts.contract.LabTest;
import org.openmrs.Concept;

public class LabTestMapper extends ResourceMapper {
    public LabTestMapper() {
        super(AllTestsAndPanels.ALL_TESTS_AND_PANELS);
    }

    @Override
    public LabTest map(Concept testConcept) {
        LabTest test = new LabTest();
        test = mapResource(test, testConcept, true);
        test.setDescription(ConceptExtension.getDescriptionOrName(testConcept));
        test.setResultType(testConcept.getDatatype().getName());
        test.setTestUnitOfMeasure(ConceptExtension.getUnits(testConcept));
        test.setSortOrder(getSortWeight(testConcept));
        test.setCodedTestAnswer(testConcept.getAnswers());
        return test;
    }


}
