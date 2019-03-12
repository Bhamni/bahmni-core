package org.bahmni.module.referencedata.web.controller;

import org.bahmni.module.referencedata.labconcepts.mapper.AttributableResourceMapper;
import org.openmrs.Concept;
import org.bahmni.module.referencedata.labconcepts.contract.Resource;
import org.openmrs.api.ConceptService;
import org.openmrs.module.emrapi.encounter.exception.ConceptNotFoundException;
import org.openmrs.module.webservices.rest.web.v1_0.controller.BaseRestController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
@RequestMapping(value = "/rest/v1/reference-data/resources")
public class ResourcesController extends BaseRestController {
    public static final String UNIDENTIFIED_RESOURCE = "No resource was found for specified uuid";
    private ConceptService conceptService;
    AttributableResourceMapper attributeResourceMapper;

    @Autowired
    public ResourcesController(ConceptService conceptService) {
        this.conceptService = conceptService;
        this.attributeResourceMapper = new AttributableResourceMapper();
    }

    @RequestMapping(value = "/{uuid}", method = RequestMethod.GET)
    public Resource getResourceFromConcept(@PathVariable("uuid") String uuid) {
        final Concept concept = conceptService.getConceptByUuid(uuid);
        if (concept == null) {
            throw new ConceptNotFoundException(UNIDENTIFIED_RESOURCE + uuid);
        }
        return attributeResourceMapper.map(concept);
    }
}
