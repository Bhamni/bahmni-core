package org.bahmni.module.bahmnicore.web.v1_0.controller;


import org.apache.commons.collections.CollectionUtils;
import org.apache.log4j.Logger;
import org.bahmni.module.bahmnicore.service.BahmniDrugOrderService;
import org.bahmni.module.bahmnicore.service.BahmniObsService;
import org.openmrs.Concept;
import org.openmrs.DrugOrder;
import org.openmrs.Order;
import org.openmrs.api.ConceptService;
import org.openmrs.module.bahmniemrapi.drugorder.contract.BahmniDrugOrder;
import org.openmrs.module.bahmniemrapi.drugorder.contract.BahmniOrderAttribute;
import org.openmrs.module.bahmniemrapi.drugorder.mapper.BahmniDrugOrderMapper;
import org.openmrs.module.bahmniemrapi.drugorder.mapper.BahmniProviderMapper;
import org.openmrs.module.bahmniemrapi.drugorder.mapper.OrderAttributesMapper;
import org.openmrs.module.bahmniemrapi.encountertransaction.contract.BahmniObservation;
import org.openmrs.module.emrapi.encounter.ConceptMapper;
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
import java.text.SimpleDateFormat;
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

    private OrderAttributesMapper orderAttributesMapper;

    private ConceptMapper conceptMapper;

    public BahmniDrugOrderController(BahmniDrugOrderService drugOrderService) {
        this.drugOrderService = drugOrderService;
        this.conceptMapper = new ConceptMapper();
    }

    public BahmniDrugOrderController() {
        this.conceptMapper = new ConceptMapper();
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
                            @RequestParam(value = "startDate", required = false) String startDate,
                            @RequestParam(value = "endDate", required = false) String endDate) throws ParseException {

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");
        Date end_date=null;
        Date start_date=null;

        if(startDate!=null) start_date = simpleDateFormat.parse(startDate);
        if(endDate!=null) end_date = simpleDateFormat.parse(endDate);

        Map<String, Collection<BahmniDrugOrder>> visitWiseOrders = new HashMap<>();

        List<BahmniDrugOrder> prescribedOrders = getPrescribedOrders(visitUuids, patientUuid, true, numberOfVisits, start_date, end_date);
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
        return getPrescribedOrders(null, patientUuid, includeActiveVisit, numberOfVisits, null, null);
    }

    @RequestMapping(value = baseUrl + "/drugOrderDetails", method = RequestMethod.GET)
    @ResponseBody
    public List<BahmniDrugOrder> getDrugOrderDetails(@RequestParam(value = "patientUuid") String patientUuid,
                                                         @RequestParam(value = "drugNames", required = false) List<String> drugNames) throws ParseException {
        Set<Concept> conceptsForDrugs = getConceptsForDrugs(drugNames);
        List<DrugOrder> drugOrders = new ArrayList<>();

        if (CollectionUtils.isEmpty(conceptsForDrugs)) {
            return new ArrayList<>();
        }

        List<Order> allDrugOrders = drugOrderService.getAllDrugOrders(patientUuid, conceptsForDrugs, null, null);
        for (Order allDrugOrder : allDrugOrders) {
            drugOrders.add((DrugOrder) allDrugOrder);
        }

        Map<String, DrugOrder> drugOrderMap = drugOrderService.getDiscontinuedDrugOrders(drugOrders);
        try {
            return new BahmniDrugOrderMapper(new BahmniProviderMapper(), getOrderAttributesMapper(), conceptMapper).mapToResponse(drugOrders, null, drugOrderMap);
        } catch (IOException e) {
            logger.error("Could not parse dosing instructions", e);
            throw new RuntimeException("Could not parse dosing instructions", e);

        }
    }

    private Set<Concept> getConceptsForDrugs(List<String> drugs) {
        if (drugs == null) return null;
        Set<Concept> drugConcepts = new HashSet<>();
        for (String drug : drugs) {
            Concept concept = conceptService.getConceptByName(drug);
            getDrugs(concept, drugConcepts);
        }
        return drugConcepts;
    }

    private void getDrugs(Concept concept, Set<Concept> drugConcepts) {
        if (concept.isSet()) {
            for (Concept drugConcept : concept.getSetMembers()) {
                getDrugs(drugConcept, drugConcepts);
            }
        } else {
            drugConcepts.add(concept);
        }
    }

    private Collection<Concept> getOrdAttributeConcepts() {
        Concept orderAttribute = conceptService.getConceptByName(BahmniOrderAttribute.ORDER_ATTRIBUTES_CONCEPT_SET_NAME);
        return orderAttribute == null ? Collections.EMPTY_LIST : orderAttribute.getSetMembers();
    }

    private OrderAttributesMapper getOrderAttributesMapper() {
        if (orderAttributesMapper == null) {
            orderAttributesMapper = new OrderAttributesMapper();
        }
        return orderAttributesMapper;
    }

    private List<BahmniDrugOrder> getActiveOrders(String patientUuid) {
        List<DrugOrder> activeDrugOrders = drugOrderService.getActiveDrugOrders(patientUuid);
        logger.info(activeDrugOrders.size() + " active drug orders found");
        return getBahmniDrugOrders(patientUuid,activeDrugOrders);
    }

    private List<BahmniDrugOrder> getPrescribedOrders(List<String> visitUuids, String patientUuid, Boolean includeActiveVisit, Integer numberOfVisits, Date startDate, Date endDate) {
        List<DrugOrder> prescribedDrugOrders = drugOrderService.getPrescribedDrugOrders(visitUuids, patientUuid, includeActiveVisit, numberOfVisits, startDate, endDate);
        logger.info(prescribedDrugOrders.size() + " prescribed drug orders found");
        return getBahmniDrugOrders(patientUuid, prescribedDrugOrders);
    }

    private List<BahmniDrugOrder> getBahmniDrugOrders(String patientUuid, List<DrugOrder> drugOrders) {
        Map<String, DrugOrder> drugOrderMap = drugOrderService.getDiscontinuedDrugOrders(drugOrders);
        try {
            Collection<BahmniObservation> orderAttributeObs = bahmniObsService.observationsFor(patientUuid, getOrdAttributeConcepts(), null, null, false, null, null, null);
            return new BahmniDrugOrderMapper(new BahmniProviderMapper(), getOrderAttributesMapper(),conceptMapper).mapToResponse(drugOrders, orderAttributeObs, drugOrderMap);
        } catch (IOException e) {
            logger.error("Could not parse drug order", e);
            throw new RuntimeException("Could not parse drug order", e);
        }
    }

}
