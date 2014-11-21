package org.openmrs.module.bahmnicore.web.v1_0.controller;


import org.apache.log4j.Logger;
import org.bahmni.module.bahmnicore.contract.drugorder.*;
import org.openmrs.module.bahmniemrapi.drugorder.contract.BahmniDrugOrder;
import org.bahmni.module.bahmnicore.service.BahmniDrugOrderService;
import org.openmrs.DrugOrder;
import org.openmrs.module.bahmniemrapi.drugorder.mapper.BahmniDrugOrderMapper;
import org.openmrs.module.bahmniemrapi.drugorder.mapper.BahmniProviderMapper;
import org.openmrs.module.webservices.rest.web.RestConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

@Controller
public class BahmniDrugOrderController {

    private final String baseUrl = "/rest/" + RestConstants.VERSION_1 + "/bahmnicore/drugOrders";
    @Autowired
    private BahmniDrugOrderService drugOrderService;
    private static Logger logger = Logger.getLogger(BahmniDrugOrderController.class);

    public BahmniDrugOrderController(BahmniDrugOrderService drugOrderService) {
        this.drugOrderService = drugOrderService;
    }

    public BahmniDrugOrderController() {
    }

    //TODO: Active orders are available in OMRS 1.10.x. Consider moving once we upgrade OpenMRS.
    @RequestMapping(value = baseUrl + "/active", method = RequestMethod.GET)
    @ResponseBody
    public List<BahmniDrugOrder> getActiveDrugOrders(@RequestParam(value = "patientUuid") String patientUuid){
        logger.info("Retrieving active drug orders for patient with uuid " + patientUuid);
        List<DrugOrder> activeDrugOrders = drugOrderService.getActiveDrugOrders(patientUuid);
        logger.info(activeDrugOrders.size() + " active drug orders found");

        try {
            Collections.sort(activeDrugOrders, new DrugIdComparator());
            return new BahmniDrugOrderMapper(new BahmniProviderMapper()).mapToResponse(activeDrugOrders);
        } catch (IOException e) {
            logger.error("Could not parse dosing instructions",e);
            throw new RuntimeException("Could not parse dosing instructions",e);
        }
    }


    @RequestMapping(value = baseUrl, method = RequestMethod.GET)
    @ResponseBody
    public List<BahmniDrugOrder> getPrescribedDrugOrders(@RequestParam(value = "patientUuid") String patientUuid,
                                                         @RequestParam(value = "includeActiveVisit", required = false) Boolean includeActiveVisit,
                                                         @RequestParam(value = "numberOfVisits", required = false) Integer numberOfVisits){
        List<DrugOrder> drugOrders = drugOrderService.getPrescribedDrugOrders(patientUuid, includeActiveVisit, numberOfVisits);
        try {
            Collections.sort(drugOrders, new DrugIdComparator());
            return new BahmniDrugOrderMapper(new BahmniProviderMapper()).mapToResponse(drugOrders);
        } catch (IOException e) {
            logger.error("Could not parse drug order",e);
             throw new RuntimeException("Could not parse drug order",e);
        }
    }

    @RequestMapping(method = RequestMethod.GET, value = baseUrl + "/config")
    @ResponseBody
    public DrugOrderConfigResponse getConfig() {
        return drugOrderService.getConfig();
    }

    class DrugIdComparator implements Comparator<DrugOrder> {
        @Override
        public int compare(DrugOrder drugOrder1, DrugOrder drugOrder2) {
            return drugOrder1.getOrderId().compareTo(drugOrder2.getOrderId());
        }
    }
}
