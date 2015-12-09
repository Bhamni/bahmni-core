package org.bahmni.module.bahmnicore.web.v1_0.controller.display.controls;

import org.apache.commons.lang3.ObjectUtils;
import org.bahmni.module.bahmnicore.service.BahmniObsService;
import org.bahmni.module.bahmnicore.util.MiscUtils;
import org.openmrs.Concept;
import org.openmrs.Visit;
import org.openmrs.api.ConceptService;
import org.openmrs.api.VisitService;
import org.openmrs.module.bahmniemrapi.encountertransaction.contract.BahmniObservation;
import org.openmrs.module.webservices.rest.web.RestConstants;
import org.openmrs.module.webservices.rest.web.v1_0.controller.BaseRestController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.List;

@Controller
@RequestMapping(value = "/rest/" + RestConstants.VERSION_1 + "/bahmnicore/observations")
public class BahmniObservationsController extends BaseRestController {

    private static final String LATEST = "latest";
    private static final String INITIAL = "initial";
    private BahmniObsService bahmniObsService;
    private ConceptService conceptService;
    private VisitService visitService;

    @Autowired
    public BahmniObservationsController(BahmniObsService bahmniObsService, ConceptService conceptService, VisitService visitService) {
        this.bahmniObsService = bahmniObsService;
        this.conceptService = conceptService;
        this.visitService = visitService;
    }

    @RequestMapping(method = RequestMethod.GET)
    @ResponseBody
    public Collection<BahmniObservation> get(@RequestParam(value = "patientUuid", required = true) String patientUUID,
                                             @RequestParam(value = "concept", required = true) List<String> rootConceptNames,
                                             @RequestParam(value = "scope", required = false) String scope,
                                             @RequestParam(value = "numberOfVisits", required = false) Integer numberOfVisits,
                                             @RequestParam(value = "obsIgnoreList", required = false) List<String> obsIgnoreList,
                                             @RequestParam(value = "filterObsWithOrders", required = false, defaultValue = "true") Boolean filterObsWithOrders,
                                             @RequestParam(value = "startDate", required = false) String startDate,
                                             @RequestParam(value = "endDate", required = false) String endDate) throws ParseException {

        List<Concept> rootConcepts = MiscUtils.getConceptsForNames(rootConceptNames, conceptService);
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");
        Date start_date = null;
        Date end_date = null;
        if (startDate != null) {
            start_date = simpleDateFormat.parse(startDate);
        }
        if (endDate != null) {
            end_date = simpleDateFormat.parse(endDate);
        }
        if (ObjectUtils.equals(scope, LATEST)) {
            return bahmniObsService.getLatest(patientUUID, rootConcepts, numberOfVisits, obsIgnoreList, filterObsWithOrders,
                    null, start_date, end_date);
        } else if (ObjectUtils.equals(scope, INITIAL)) {
            return bahmniObsService.getInitial(patientUUID, rootConcepts, numberOfVisits, obsIgnoreList, filterObsWithOrders, null, start_date, end_date);
        } else {
            return bahmniObsService.observationsFor(patientUUID, rootConcepts, numberOfVisits, obsIgnoreList, filterObsWithOrders, null, start_date, end_date);
        }

    }

    @RequestMapping(method = RequestMethod.GET, params = {"visitUuid"})
    @ResponseBody
    public Collection<BahmniObservation> get(@RequestParam(value = "visitUuid", required = true) String visitUuid,
                                             @RequestParam(value = "scope", required = false) String scope,
                                             @RequestParam(value = "concept", required = false) List<String> conceptNames,
                                             @RequestParam(value = "obsIgnoreList", required = false) List<String> obsIgnoreList,
                                             @RequestParam(value = "filterObsWithOrders", required = false, defaultValue = "true") Boolean filterObsWithOrders) {

        Visit visit = visitService.getVisitByUuid(visitUuid);
        if (ObjectUtils.equals(scope, INITIAL)) {
            return bahmniObsService.getInitialObsByVisit(visit, MiscUtils.getConceptsForNames(conceptNames, conceptService), obsIgnoreList, filterObsWithOrders);
        } else if (ObjectUtils.equals(scope, LATEST)) {
            return bahmniObsService.getLatestObsByVisit(visit, MiscUtils.getConceptsForNames(conceptNames, conceptService), obsIgnoreList, filterObsWithOrders);
        } else {
            // Sending conceptName and obsIgnorelist, kinda contradicts, since we filter directly on concept names (not on root concept)
            return bahmniObsService.getObservationForVisit(visitUuid, conceptNames, MiscUtils.getConceptsForNames(obsIgnoreList, conceptService), filterObsWithOrders, null);
        }
    }
}
