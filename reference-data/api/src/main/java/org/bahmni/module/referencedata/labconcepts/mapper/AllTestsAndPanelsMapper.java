package org.bahmni.module.referencedata.labconcepts.mapper;

import org.bahmni.module.referencedata.labconcepts.contract.AllTestsAndPanels;
import org.openmrs.Concept;

public class AllTestsAndPanelsMapper extends ResourceMapper {
    public AllTestsAndPanelsMapper() {
        super(null);
    }

    @Override
    public AllTestsAndPanels map(Concept testsAndPanelsConcept) {
        AllTestsAndPanels allTestsAndPanels = new AllTestsAndPanels();
        allTestsAndPanels = mapResource(allTestsAndPanels, testsAndPanelsConcept, true);
        allTestsAndPanels.setDescription(ConceptExtension.getDescription(testsAndPanelsConcept));
        allTestsAndPanels.setTestsAndPanels(new TestAndPanelMapper().map(testsAndPanelsConcept));
        return allTestsAndPanels;
    }
}
