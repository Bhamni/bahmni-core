package org.bahmni.module.bahmnicore.web.v1_0.controller;

import org.bahmni.module.bahmnicore.contract.diagnosis.DiagnosisSearchConfig;
import org.bahmni.module.bahmnicore.service.BahmniDiagnosisService;
import org.openmrs.module.bahmniemrapi.diagnosis.contract.BahmniDiagnosisRequest;
import org.openmrs.module.webservices.rest.web.RestConstants;
import org.openmrs.module.webservices.rest.web.v1_0.controller.BaseRestController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping(value = "/rest/" + RestConstants.VERSION_1 + "/bahmnicore/diagnosis")
public class BahmniDiagnosisController extends BaseRestController {

    @Autowired
    private BahmniDiagnosisService bahmniDiagnosisService;

    @RequestMapping(method = RequestMethod.POST, value = "search")
    @ResponseBody
    public List<BahmniDiagnosisRequest> search(@RequestBody DiagnosisSearchConfig diagnosisSearchConfig) throws Exception {
        if (diagnosisSearchConfig.getVisitUuid() != null) {
            return bahmniDiagnosisService.getBahmniDiagnosisByPatientAndVisit(diagnosisSearchConfig.getPatientUuid(), diagnosisSearchConfig.getVisitUuid());
        } else {
            return bahmniDiagnosisService.getBahmniDiagnosisByPatientAndDate(diagnosisSearchConfig.getPatientUuid(), diagnosisSearchConfig.getStartDate(), diagnosisSearchConfig.getEndDate());
        }
    }

    @RequestMapping(method = RequestMethod.GET, value = "delete")
    @ResponseBody
    public boolean delete(@RequestParam(value = "obsUuid", required = true) String obsUuid) throws Exception {
        bahmniDiagnosisService.delete(obsUuid);
        return true;
    }
}
