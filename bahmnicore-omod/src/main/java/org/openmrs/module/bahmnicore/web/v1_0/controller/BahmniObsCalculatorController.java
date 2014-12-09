package org.openmrs.module.bahmnicore.web.v1_0.controller;

import org.bahmni.module.bahmnicore.service.BahmniObsCalculatorService;
import org.openmrs.module.bahmniemrapi.encountertransaction.contract.BahmniObservation;
import org.openmrs.module.webservices.rest.web.RestConstants;
import org.openmrs.module.webservices.rest.web.v1_0.controller.BaseRestController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

@Controller
@RequestMapping(value = "/rest/" + RestConstants.VERSION_1 + "/bahmnicore/bahmniobscalculator")
public class BahmniObsCalculatorController extends BaseRestController {

    @Autowired
    private BahmniObsCalculatorService bahmniObsCalculatorService;

    @Autowired
    public BahmniObsCalculatorController(BahmniObsCalculatorService bahmniObsCalculatorService) {
        this.bahmniObsCalculatorService = bahmniObsCalculatorService;
    }

    @RequestMapping(method = RequestMethod.POST)
    @ResponseBody
    public String get(@RequestBody List<BahmniObservation> bahmniObservations) throws Throwable {
        return bahmniObsCalculatorService.calculateObsFrom(bahmniObservations);
    }
}
