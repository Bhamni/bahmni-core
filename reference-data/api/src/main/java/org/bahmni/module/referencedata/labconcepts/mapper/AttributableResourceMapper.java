package org.bahmni.module.referencedata.labconcepts.mapper;

import org.openmrs.Concept;
import org.bahmni.module.referencedata.labconcepts.contract.Resource;

public class AttributableResourceMapper extends ResourceMapper {

    public AttributableResourceMapper() {super(null);}

    protected AttributableResourceMapper(String parentConceptName) {
        super(parentConceptName);
    }

    @Override
    public Resource map(Concept concept) {
        Resource resource = new Resource();
        mapResource(resource, concept);
        return resource;
    }
}
