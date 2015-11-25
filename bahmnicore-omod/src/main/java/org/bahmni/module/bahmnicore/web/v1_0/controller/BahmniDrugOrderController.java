package org.bahmni.module.bahmnicore.web.v1_0.controller;


import org.apache.log4j.Logger;
import org.bahmni.module.bahmnicore.service.BahmniDrugOrderService;
import org.bahmni.module.bahmnicore.service.BahmniObsService;
import org.openmrs.Concept;
import org.openmrs.DrugOrder;
import org.openmrs.api.ConceptService;
import org.openmrs.module.bahmniemrapi.drugorder.contract.BahmniDrugOrder;
import org.openmrs.module.bahmniemrapi.drugorder.contract.BahmniOrderAttribute;
import org.openmrs.module.bahmniemrapi.drugorder.mapper.BahmniDrugOrderMapper;
import org.openmrs.module.bahmniemrapi.drugorder.mapper.BahmniProviderMapper;
import org.openmrs.module.bahmniemrapi.drugorder.mapper.OrderAttributesMapper;
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

    public BahmniDrugOrderController(BahmniDrugOrderService drugOrderService) {
        this.drugOrderService = drugOrderService;
    }

    public BahmniDrugOrderController() {
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
    public Map<String, Collection<BahmniDrugOrder>> getVisitWisePrescribedAndOtherActiveOrders(@RequestParam(value = "patientUuid") String patientUuid,
                                                                                               @RequestParam(value = "numberOfVisits", required = false) Integer numberOfVisits,
                                                                                               @RequestParam(value = "getOtherActive", required = false) Boolean getOtherActive,
                                                                                               @RequestParam(value = "visitUuids", required = false) List visitUuids) {

        Map<String, Collection<BahmniDrugOrder>> visitWiseOrders = new HashMap<>();


        List<BahmniDrugOrder> prescribedOrders;
        if (visitUuids != null && visitUuids.size() != 0) {
            prescribedOrders = getPrescribedDrugOrders(patientUuid, visitUuids);
        } else {
            prescribedOrders = getPrescribedOrders(patientUuid, true, numberOfVisits);
        }
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
        return getPrescribedOrders(patientUuid, includeActiveVisit, numberOfVisits);
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
        Map<DrugOrder,DrugOrder> drugOrderMap = drugOrderService.getDiscontinuedDrugOrdersForDrugOrders(activeDrugOrders);
        logger.info(activeDrugOrders.size() + " active drug orders found");
        try {
            Collection<BahmniObservation> orderAttributeObs = bahmniObsService.observationsFor(patientUuid, getOrdAttributeConcepts(), null, null, false, null);
            return new BahmniDrugOrderMapper(new BahmniProviderMapper(), getOrderAttributesMapper()).mapToResponse(activeDrugOrders, orderAttributeObs, drugOrderMap);
        } catch (IOException e) {
            logger.error("Could not parse dosing instructions", e);
            throw new RuntimeException("Could not parse dosing instructions", e);
        }
    }

    private List<BahmniDrugOrder> getPrescribedOrders(String patientUuid, Boolean includeActiveVisit, Integer numberOfVisits) {
        List<DrugOrder> drugOrders = drugOrderService.getPrescribedDrugOrders(patientUuid, includeActiveVisit, numberOfVisits);
        Map<DrugOrder,DrugOrder> drugOrderMap = drugOrderService.getDiscontinuedDrugOrdersForDrugOrders(drugOrders);
        logger.info(drugOrders.size() + " prescribed drug orders found");

        try {
            Collection<BahmniObservation> orderAttributeObs = bahmniObsService.observationsFor(patientUuid, getOrdAttributeConcepts(), null, null, false, null);
            return new BahmniDrugOrderMapper(new BahmniProviderMapper(), getOrderAttributesMapper()).mapToResponse(drugOrders, orderAttributeObs, drugOrderMap);
        } catch (IOException e) {
            logger.error("Could not parse drug order", e);
            throw new RuntimeException("Could not parse drug order", e);
        }
    }

    private List<BahmniDrugOrder> getPrescribedDrugOrders(String patientUuid, List visitUuids) {
        List<DrugOrder> drugOrders = drugOrderService.getPrescribedDrugOrders(visitUuids);
        Map<DrugOrder,DrugOrder> drugOrderMap = drugOrderService.getDiscontinuedDrugOrdersForDrugOrders(drugOrders);
        logger.info(drugOrders.size() + " prescribed drug orders found");

        try {
            Collection<BahmniObservation> orderAttributeObs = bahmniObsService.observationsFor(patientUuid, getOrdAttributeConcepts(), null, null, false, null);
            return new BahmniDrugOrderMapper(new BahmniProviderMapper(), getOrderAttributesMapper()).mapToResponse(drugOrders, orderAttributeObs, drugOrderMap);
        } catch (IOException e) {
            logger.error("Could not parse drug order", e);
            throw new RuntimeException("Could not parse drug order", e);
        }
    }

}
