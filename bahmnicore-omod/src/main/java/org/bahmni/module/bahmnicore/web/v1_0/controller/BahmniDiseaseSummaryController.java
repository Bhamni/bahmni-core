package org.bahmni.module.bahmnicore.web.v1_0.controller;

import org.bahmni.module.bahmnicore.util.BahmniDateUtil;
import org.bahmni.module.bahmnicoreui.constant.DiseaseSummaryConstants;
import org.bahmni.module.bahmnicoreui.contract.DiseaseDataParams;
import org.bahmni.module.bahmnicoreui.contract.DiseaseSummaryData;
import org.bahmni.module.bahmnicoreui.service.BahmniDiseaseSummaryService;
import org.openmrs.module.webservices.rest.web.RestConstants;
import org.openmrs.module.webservices.rest.web.v1_0.controller.BaseRestController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.text.ParseException;
import java.util.Date;
import java.util.List;

@Controller
@RequestMapping(value = "/rest/" + RestConstants.VERSION_1 + "/bahmnicore/diseaseSummaryData")
public class BahmniDiseaseSummaryController extends BaseRestController {

    @Autowired
    private BahmniDiseaseSummaryService bahmniDiseaseSummaryService;

    @RequestMapping(method = RequestMethod.GET)
    @ResponseBody
    public DiseaseSummaryData getDiseaseSummaryData(@RequestParam(value = "patientUuid") String patientUuid,
                                                    @RequestParam(value = "visit",required = false) String visitUuid,
                                                    @RequestParam(value = "numberOfVisits",required = false) Integer numberOfVisits,
                                                    @RequestParam(value = "initialCount",required = false) Integer initialCount,
                                                    @RequestParam(value = "latestCount",required = false) Integer latestCount,
                                                    @RequestParam(value = "obsConcepts",required = false) List<String> obsConcepts,
                                                    @RequestParam(value = "drugConcepts",required = false) List<String> drugConcepts,
                                                    @RequestParam(value = "labConcepts",required = false) List<String> labConcepts,
                                                    @RequestParam(value = "groupBy", defaultValue = DiseaseSummaryConstants.RESULT_TABLE_GROUP_BY_VISITS, required = false) String groupBy,
                                                    @RequestParam(value = "startDate",required = false) String startDateStr,
                                                    @RequestParam(value = "endDate",required = false) String endDateStr) throws ParseException {

        Date startDate = BahmniDateUtil.convertToDate(startDateStr, BahmniDateUtil.DateFormatType.UTC);
        Date endDate = BahmniDateUtil.convertToDate(endDateStr, BahmniDateUtil.DateFormatType.UTC);

        DiseaseDataParams diseaseDataParams = new DiseaseDataParams();
        diseaseDataParams.setNumberOfVisits(numberOfVisits);
        diseaseDataParams.setInitialCount(initialCount);
        diseaseDataParams.setLatestCount(latestCount);
        diseaseDataParams.setVisitUuid(visitUuid);
        diseaseDataParams.setObsConcepts(obsConcepts);
        diseaseDataParams.setLabConcepts(labConcepts);
        diseaseDataParams.setDrugConcepts(drugConcepts);
        diseaseDataParams.setGroupBy(groupBy);
        diseaseDataParams.setStartDate(startDate);
        diseaseDataParams.setEndDate(endDate);
        return bahmniDiseaseSummaryService.getDiseaseSummary(patientUuid,diseaseDataParams);
    }

}
