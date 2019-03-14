package org.bahmni.module.referencedata.labconcepts.model.event;

import org.openmrs.Concept;

import static org.bahmni.module.referencedata.labconcepts.contract.Department.DEPARTMENT_CONCEPT_CLASS;
import static org.bahmni.module.referencedata.labconcepts.mapper.ConceptExtension.isOfConceptClass;
import static org.bahmni.module.referencedata.labconcepts.model.event.SellableTypeEvent.isConceptWithSellableAttribute;

public class DepartmentEvent extends ConceptOperationEvent {

    public DepartmentEvent(String url, String category, String title) {
        super(url, category, title);
    }

    @Override
    public boolean isResourceConcept(Concept concept) {
        return isOfConceptClass(concept, DEPARTMENT_CONCEPT_CLASS) && !isConceptWithSellableAttribute(concept);
    }

}
