package org.bahmni.module.referencedata.web.controller;

import org.bahmni.module.referencedata.web.contract.Concept;
import org.bahmni.module.referencedata.web.service.ReferenceDataConceptService;
import org.openmrs.api.APIException;
import org.openmrs.module.webservices.rest.web.v1_0.controller.BaseRestController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
public class ConceptController extends BaseRestController {
    @Autowired
    private ReferenceDataConceptService referenceDataConceptService;

    public ConceptController() {
    }

    @RequestMapping(value = "/rest/v1/reference-data/concept", method = RequestMethod.POST)
    @ResponseBody
    public ResponseEntity<String> create(@RequestBody Concept concept) {
        try{
            org.openmrs.Concept savedConcept = referenceDataConceptService.saveConcept(concept);
            return new ResponseEntity<>(String.valueOf(savedConcept.getId()), HttpStatus.CREATED);
        }
        catch (Throwable error){
            return new ResponseEntity<>(error.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }
}
