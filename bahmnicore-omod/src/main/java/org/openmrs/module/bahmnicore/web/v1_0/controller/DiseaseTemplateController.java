package org.openmrs.module.bahmnicore.web.v1_0.controller;

import org.openmrs.module.bahmniemrapi.encountertransaction.contract.DiseaseTemplate;
import org.bahmni.module.bahmnicore.contract.diseasetemplate.DiseaseTemplatesConfig;
import org.openmrs.module.bahmniemrapi.encountertransaction.service.DiseaseTemplateService;
import org.openmrs.module.webservices.rest.web.v1_0.controller.BaseRestController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

@Controller
public class DiseaseTemplateController extends BaseRestController {

    private final String baseUrl = "/rest/v1/bahmnicore/";

    @Autowired
    private DiseaseTemplateService diseaseTemplateService;

    @RequestMapping(method = RequestMethod.POST, value = baseUrl + "diseaseTemplates")
    @ResponseBody
    public List<DiseaseTemplate> get(@RequestBody DiseaseTemplatesConfig diseaseTemplatesConfig) {
        return diseaseTemplateService.allDiseaseTemplatesFor(diseaseTemplatesConfig);
    }

    @RequestMapping(method = RequestMethod.GET, value = baseUrl + "diseaseTemplate")
    @ResponseBody
    public DiseaseTemplate getDiseaseTemplate(@RequestParam(value = "patientUuid", required = true) String patientUUID,
                                              @RequestParam(value = "diseaseName", required = true) String diseaseName) {
        return diseaseTemplateService.diseaseTemplateFor(patientUUID, diseaseName);
    }
}
