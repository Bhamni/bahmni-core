package org.bahmni.module.referencedata.labconcepts.mapper;

import org.openmrs.Concept;
import org.bahmni.module.referencedata.labconcepts.contract.Resource;

import java.util.HashMap;

public class AttributableResourceMapper extends ResourceMapper {

    public AttributableResourceMapper() {super(null);}

    protected AttributableResourceMapper(String parentConceptName) {
        super(parentConceptName);
    }

    @Override
    public Resource map(Concept concept) {
        Resource resource = new Resource();
        mapResource(resource, concept);
        HashMap<String, Object> properties = new HashMap<>();
        concept.getActiveAttributes().stream().forEach(a -> properties.put(a.getAttributeType().getName(), a.getValueReference()));
        if (!properties.isEmpty()) {
            resource.setProperties(properties);
        }
        return resource;
    }
}
