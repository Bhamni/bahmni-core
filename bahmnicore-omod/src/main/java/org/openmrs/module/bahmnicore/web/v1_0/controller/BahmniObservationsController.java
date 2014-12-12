package org.openmrs.module.bahmnicore.web.v1_0.controller;

import org.apache.commons.lang3.ObjectUtils;
import org.openmrs.module.bahmniemrapi.encountertransaction.service.BahmniObsService;
import org.openmrs.Concept;
import org.openmrs.api.ConceptService;
import org.openmrs.module.bahmniemrapi.encountertransaction.contract.BahmniObservation;
import org.openmrs.module.webservices.rest.web.RestConstants;
import org.openmrs.module.webservices.rest.web.v1_0.controller.BaseRestController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping(value = "/rest/" + RestConstants.VERSION_1 + "/bahmnicore/observations")
public class BahmniObservationsController extends BaseRestController {

    private static final String LATEST = "latest";
    @Autowired
    private BahmniObsService bahmniObsService;

    @Autowired
    private ConceptService conceptService;

    public BahmniObservationsController() {
    }

    @RequestMapping(method = RequestMethod.GET)
    @ResponseBody
    public List<BahmniObservation> get(@RequestParam(value = "patientUuid", required = true) String patientUUID,
                                       @RequestParam(value = "concept", required = true) List<String> rootConceptNames,
                                       @RequestParam(value = "scope", required = false) String scope,
                                       @RequestParam(value = "numberOfVisits", required = false) Integer numberOfVisits) {

        List<Concept> rootConcepts = new ArrayList<>();
        for (String rootConceptName : rootConceptNames) {
            rootConcepts.add(conceptService.getConceptByName(rootConceptName));
        }

        List<BahmniObservation> observations;
        if (ObjectUtils.equals(scope, LATEST)) {
            observations = bahmniObsService.getLatest(patientUUID, rootConceptNames);
        } else {
            observations = bahmniObsService.observationsFor(patientUUID, rootConcepts, numberOfVisits);
        }

        return observations;
    }
}
