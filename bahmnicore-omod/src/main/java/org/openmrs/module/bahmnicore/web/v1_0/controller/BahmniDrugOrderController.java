package org.openmrs.module.bahmnicore.web.v1_0.controller;


import org.apache.log4j.Logger;
import org.bahmni.module.bahmnicore.service.BahmniDrugOrderService;
import org.joda.time.DateTime;
import org.joda.time.Days;
import org.openmrs.DrugOrder;
import org.openmrs.module.webservices.rest.web.RestConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;


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
    public List<Map> getActiveDrugOrders(@RequestParam(value = "patientUuid") String patientUuid){
        logger.info("Retrieving active drug orders for patient with uuid " + patientUuid);
        List<DrugOrder> activeDrugOrders = drugOrderService.getActiveDrugOrders(patientUuid);
        logger.info(activeDrugOrders.size() + " active drug orders found");

        return mapToResponse(activeDrugOrders);
    }


    @RequestMapping(value = baseUrl, method = RequestMethod.GET)
    @ResponseBody
    public List<Map> getPrescribedDrugOrders(@RequestParam(value = "patientUuid") String patientUuid,
                                             @RequestParam(value = "includeActiveVisit", required = false) Boolean includeActiveVisit,
                                             @RequestParam(value = "numberOfVisits", required = false) Integer numberOfVisits){
        List<DrugOrder> drugOrders = drugOrderService.getPrescribedDrugOrders(patientUuid, includeActiveVisit, numberOfVisits);
        return mapToResponse(drugOrders);
    }


    private ArrayList<Map> mapToResponse(List<DrugOrder> activeDrugOrders) {
        ArrayList<Map> response = new ArrayList<>();
        for (DrugOrder drugOrder : activeDrugOrders) {
            HashMap<String, Object> responseHashMap = new HashMap<>();
            responseHashMap.put("name", drugOrder.getDrug().getName());
            responseHashMap.put("orderDate", serializeDate(drugOrder.getDateActivated()));

            responseHashMap.put("dosingType", drugOrder.getDosingType().name());
            if (drugOrder.getDosingType() == DrugOrder.DosingType.FREE_TEXT) {
                populateFreeTextOrderDetails(drugOrder, responseHashMap);
            } else {
                populateSimpleOrderDetails(drugOrder, responseHashMap);
            }
            responseHashMap.put("dose", drugOrder.getDose());

            if (drugOrder.getAutoExpireDate() != null) {
                DateTime autoExpireDate = new DateTime(drugOrder.getAutoExpireDate());
                DateTime dateActivated = new DateTime(drugOrder.getDateActivated());
                responseHashMap.put("days", Days.daysBetween(dateActivated, autoExpireDate).getDays());
            }
            responseHashMap.put("expireDate", serializeDate(drugOrder.getAutoExpireDate()));
            responseHashMap.put("name", drugOrder.getDrug().getName());
            response.add(responseHashMap);
        }
        return response;
    }

    private void populateSimpleOrderDetails(DrugOrder drugOrder, HashMap<String, Object> responseHashMap) {
        responseHashMap.put("dose", drugOrder.getDose());
        responseHashMap.put("doseUnits", drugOrder.getDoseUnits());
        responseHashMap.put("route", drugOrder.getRoute().getDisplayString());
        responseHashMap.put("frequency", drugOrder.getFrequency());
    }

    private void populateFreeTextOrderDetails(DrugOrder drugOrder, HashMap<String, Object> responseHashMap) {
        responseHashMap.put("dosingInstructions", drugOrder.getDosingInstructions());
    }

    private String serializeDate(Date date) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        format.setTimeZone(TimeZone.getTimeZone("GMT"));
        return format.format(date);
    }

}
