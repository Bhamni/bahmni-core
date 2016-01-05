package org.bahmni.module.bahmnicore.web.v1_0.controller;


import org.apache.log4j.Logger;
import org.bahmni.module.bahmnicore.service.BahmniDrugOrderService;
import org.bahmni.module.bahmnicore.service.BahmniObsService;
import org.bahmni.module.bahmnicore.util.BahmniDateUtil;
import org.openmrs.Concept;
import org.openmrs.DrugOrder;
import org.openmrs.Order;
import org.openmrs.api.ConceptService;
import org.openmrs.module.bahmniemrapi.drugorder.contract.BahmniDrugOrder;
import org.openmrs.module.bahmniemrapi.drugorder.contract.BahmniOrderAttribute;
import org.openmrs.module.bahmniemrapi.drugorder.mapper.BahmniDrugOrderMapper;
import org.openmrs.module.bahmniemrapi.encountertransaction.contract.BahmniObservation;
import org.openmrs.module.webservices.rest.web.RestConstants;
import org.openmrs.module.webservices.rest.web.v1_0.controller.BaseRestController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.IOException;
import java.text.ParseException;
import java.util.*;

@Controller
public class BahmniDrugOrderController extends BaseRestController {

    private final String baseUrl = "/rest/" + RestConstants.VERSION_1 + "/bahmnicore/drugOrders";
    @Autowired
    private BahmniDrugOrderService drugOrderService;

    @Autowired
    private BahmniObsService bahmniObsService;

    @Autowired
    private ConceptService conceptService;

    private static Logger logger = Logger.getLogger(BahmniDrugOrderController.class);

    private BahmniDrugOrderMapper bahmniDrugOrderMapper;

    public BahmniDrugOrderController(BahmniDrugOrderService drugOrderService) {
        this.drugOrderService = drugOrderService;
        this.bahmniDrugOrderMapper = new BahmniDrugOrderMapper();
    }
    public BahmniDrugOrderController() {
        this.bahmniDrugOrderMapper = new BahmniDrugOrderMapper();
    }


    //TODO: Active orders are available in OMRS 1.10.x. Consider moving once we upgrade OpenMRS.
    @RequestMapping(value = baseUrl + "/active", method = RequestMethod.GET)
    @ResponseBody
    public List<BahmniDrugOrder> getActiveDrugOrders(@RequestParam(value = "patientUuid") String patientUuid) {
        logger.info("Retrieving active drug orders for patient with uuid " + patientUuid);
        return getActiveOrders(patientUuid);
    }

    @RequestMapping(value = baseUrl + "/prescribedAndActive", method = RequestMethod.GET)
    @ResponseBody
    public Map<String, Collection<BahmniDrugOrder>> getVisitWisePrescribedAndOtherActiveOrders(
                            @RequestParam(value = "patientUuid") String patientUuid,
                            @RequestParam(value = "numberOfVisits", required = false) Integer numberOfVisits,
                            @RequestParam(value = "getOtherActive", required = false) Boolean getOtherActive,
                            @RequestParam(value = "visitUuids", required = false) List visitUuids,
                            @RequestParam(value = "startDate", required = false) String startDateStr,
                            @RequestParam(value = "endDate", required = false) String endDateStr,
                            @RequestParam(value = "getEffectiveOrdersOnly", required = false) Boolean getEffectiveOrdersOnly) throws ParseException {

        Map<String, Collection<BahmniDrugOrder>> visitWiseOrders = new HashMap<>();
        Date startDate = BahmniDateUtil.convertToDate(startDateStr, BahmniDateUtil.DateFormatType.UTC);
        Date endDate = BahmniDateUtil.convertToDate(endDateStr, BahmniDateUtil.DateFormatType.UTC);

        List<BahmniDrugOrder> prescribedOrders = getPrescribedOrders(visitUuids, patientUuid, true, numberOfVisits, startDate, endDate, Boolean.TRUE.equals(getEffectiveOrdersOnly));
        visitWiseOrders.put("visitDrugOrders", prescribedOrders);

        if (Boolean.TRUE.equals(getOtherActive)) {
            List<BahmniDrugOrder> activeDrugOrders = getActiveOrders(patientUuid);
            activeDrugOrders.removeAll(prescribedOrders);
            visitWiseOrders.put("Other Active DrugOrders", activeDrugOrders);
        }

        return visitWiseOrders;
    }

    @RequestMapping(value = baseUrl, method = RequestMethod.GET)
    @ResponseBody
    public List<BahmniDrugOrder> getPrescribedDrugOrders(@RequestParam(value = "patientUuid") String patientUuid,
                                                         @RequestParam(value = "includeActiveVisit", required = false) Boolean includeActiveVisit,
                                                         @RequestParam(value = "numberOfVisits", required = false) Integer numberOfVisits) {
        return getPrescribedOrders(null, patientUuid, includeActiveVisit, numberOfVisits, null, null, false);
    }


    @RequestMapping(value = baseUrl + "/drugOrderDetails", method = RequestMethod.GET)
    @ResponseBody
    public List<BahmniDrugOrder> getDrugOrderDetails(@RequestParam(value = "patientUuid") String patientUuid,
                                                                      @RequestParam(value = "isActive", required = false) Boolean isActive,
                                                                      @RequestParam(value = "includeConceptSet", required = false) String drugConceptSetToBeFiltered,
                                                                      @RequestParam(value = "excludeConceptSet", required = false) String drugConceptSetToBeExcluded) throws ParseException {
        Set<Concept> drugConceptsToBeFiltered = getDrugConcepts(drugConceptSetToBeFiltered);
        Set<Concept> drugConceptsToBeExcluded = getDrugConcepts(drugConceptSetToBeExcluded);
        List<DrugOrder> drugOrders = new ArrayList<>();

        if (isActive == null) {
            List<Order> orders = drugOrderService.getAllDrugOrders(patientUuid, drugConceptsToBeFiltered, null, null, drugConceptsToBeExcluded);
            for (Order order : orders) {
                drugOrders.add((DrugOrder) order);
            }
        } else if (isActive) {
            drugOrders = drugOrderService.getActiveDrugOrders(patientUuid, drugConceptsToBeFiltered, drugConceptsToBeExcluded);
        } else {
            drugOrders = drugOrderService.getInactiveDrugOrders(patientUuid, drugConceptsToBeFiltered, drugConceptsToBeExcluded);
        }

        Map<String, DrugOrder> discontinuedDrugOrderMap = drugOrderService.getDiscontinuedDrugOrders(drugOrders);
        try {
            return bahmniDrugOrderMapper.mapToResponse(drugOrders, null, discontinuedDrugOrderMap);
        } catch (IOException e) {
            logger.error("Could not parse dosing instructions", e);
            throw new RuntimeException("Could not parse dosing instructions", e);

        }
    }

    Set<Concept> getDrugConcepts(String drugConceptSetName){
        if(drugConceptSetName == null) return  null;
        Set<Concept> drugConcepts = new HashSet<>();
        Concept concept = conceptService.getConceptByName(drugConceptSetName);
        if(concept == null) return  null;
        populateDrugConcepts(concept, drugConcepts);
        return drugConcepts;
    }

    private void populateDrugConcepts(Concept concept, Set<Concept> drugConcepts) {
        if (concept.isSet()) {
            for (Concept conceptSetMember : concept.getSetMembers()) {
                populateDrugConcepts(conceptSetMember, drugConcepts);
            }
        } else {
            drugConcepts.add(concept);
        }
    }

    private Collection<Concept> getOrdAttributeConcepts() {
        Concept orderAttribute = conceptService.getConceptByName(BahmniOrderAttribute.ORDER_ATTRIBUTES_CONCEPT_SET_NAME);
        return orderAttribute == null ? Collections.EMPTY_LIST : orderAttribute.getSetMembers();
    }

    private List<BahmniDrugOrder> getActiveOrders(String patientUuid) {
        List<DrugOrder> activeDrugOrders = drugOrderService.getActiveDrugOrders(patientUuid);
        logger.info(activeDrugOrders.size() + " active drug orders found");
        return getBahmniDrugOrders(patientUuid,activeDrugOrders);
    }

    private List<BahmniDrugOrder> getPrescribedOrders(List<String> visitUuids, String patientUuid, Boolean includeActiveVisit, Integer numberOfVisits, Date startDate, Date endDate, Boolean getEffectiveOrdersOnly) {
        List<DrugOrder> prescribedDrugOrders = drugOrderService.getPrescribedDrugOrders(visitUuids, patientUuid, includeActiveVisit, numberOfVisits, startDate, endDate, getEffectiveOrdersOnly);
        logger.info(prescribedDrugOrders.size() + " prescribed drug orders found");
        return getBahmniDrugOrders(patientUuid, prescribedDrugOrders);
    }

    private List<BahmniDrugOrder> getBahmniDrugOrders(String patientUuid, List<DrugOrder> drugOrders) {
        Map<String, DrugOrder> drugOrderMap = drugOrderService.getDiscontinuedDrugOrders(drugOrders);
        try {
            Collection<BahmniObservation> orderAttributeObs = bahmniObsService.observationsFor(patientUuid, getOrdAttributeConcepts(), null, null, false, null, null, null);
            return bahmniDrugOrderMapper.mapToResponse(drugOrders, orderAttributeObs, drugOrderMap);
        } catch (IOException e) {
            logger.error("Could not parse drug order", e);
            throw new RuntimeException("Could not parse drug order", e);
        }
    }

}
