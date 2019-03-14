package org.bahmni.module.referencedata.labconcepts.model.event;

import org.openmrs.Concept;

import static org.bahmni.module.referencedata.labconcepts.contract.RadiologyTest.RADIOLOGY_TEST_CONCEPT_CLASS;
import static org.bahmni.module.referencedata.labconcepts.mapper.ConceptExtension.isOfConceptClass;
import static org.bahmni.module.referencedata.labconcepts.model.event.SellableTypeEvent.isConceptWithSellableAttribute;

public class RadiologyTestEvent extends ConceptOperationEvent {

    public RadiologyTestEvent(String url, String category, String title) {
        super(url, category, title);
    }


    @Override
    public boolean isResourceConcept(Concept concept) {
        return isOfConceptClass(concept, RADIOLOGY_TEST_CONCEPT_CLASS) && !isConceptWithSellableAttribute(concept);
    }


}
