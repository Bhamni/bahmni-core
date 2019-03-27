package org.bahmni.module.referencedata.labconcepts.mapper;

import org.apache.commons.collections.MapUtils;
import org.bahmni.module.referencedata.labconcepts.contract.Resource;
import org.openmrs.Concept;
import org.openmrs.ConceptSet;
import org.openmrs.api.context.Context;

import java.util.HashMap;
import java.util.List;

public abstract class ResourceMapper {
    public static final double DEFAULT_SORT_ORDER = 999.0;
    String parentConceptName;

    protected ResourceMapper(String parentConceptName) {
        this.parentConceptName = parentConceptName;
    }

    public abstract <T extends Resource> T map(Concept concept);


    <R extends Resource> R mapResource(R resource, Concept concept) {
        resource.setName(concept.getName(Context.getLocale()).getName());
        resource.setIsActive(!concept.isRetired());
        resource.setId(concept.getUuid());
        resource.setDateCreated(concept.getDateCreated());
        resource.setLastUpdated(concept.getDateChanged());
        HashMap<String, Object> properties = new HashMap<>();
        concept.getActiveAttributes().stream().forEach(a -> properties.put(a.getAttributeType().getName(), a.getValueReference()));
        if (!MapUtils.isEmpty(properties)) {
            resource.setProperties(properties);
        }
        return (R) resource;
    }

    Double getSortWeight(Concept concept) {
        List<ConceptSet> conceptSets = Context.getConceptService().getSetsContainingConcept(concept);
        if (conceptSets == null) return DEFAULT_SORT_ORDER;
        for (ConceptSet conceptSet : conceptSets) {
            if (conceptSet.getConceptSet().getName(Context.getLocale()).getName().equals(parentConceptName)) {
                return conceptSet.getSortWeight() != null ? conceptSet.getSortWeight() : DEFAULT_SORT_ORDER;
            }
        }
        return DEFAULT_SORT_ORDER;
    }

}
