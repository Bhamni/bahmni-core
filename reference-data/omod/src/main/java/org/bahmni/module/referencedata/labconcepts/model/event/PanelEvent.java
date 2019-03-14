package org.bahmni.module.referencedata.labconcepts.model.event;

import org.openmrs.Concept;
import org.openmrs.ConceptClass;

import static org.bahmni.module.referencedata.labconcepts.mapper.ConceptExtension.isOfConceptClassByUUID;
import static org.bahmni.module.referencedata.labconcepts.model.event.SellableTypeEvent.isConceptWithSellableAttribute;

public class PanelEvent extends ConceptOperationEvent {

    public PanelEvent(String url, String category, String title) {
        super(url, category, title);
    }

    @Override
    public boolean isResourceConcept(Concept concept) {
        return isOfConceptClassByUUID(concept, ConceptClass.LABSET_UUID) && !isConceptWithSellableAttribute(concept);
    }

}
