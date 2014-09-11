package org.bahmni.module.referencedata.web.controller;

import org.bahmni.module.referencedata.web.contract.RequestConcept;
import org.bahmni.module.referencedata.web.service.ReferenceDataConceptService;
import org.openmrs.module.webservices.rest.web.v1_0.controller.BaseRestController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class ConceptController extends BaseRestController{
    @Autowired
    private ReferenceDataConceptService referenceDataConceptService;

    public ConceptController() {
    }

    @RequestMapping(value = "/rest/v1/reference-data/concept", method = RequestMethod.POST)
    @ResponseBody
    public org.openmrs.Concept create(@RequestBody RequestConcept requestConcept) {
        return referenceDataConceptService.saveConcept(requestConcept);
    }
}
