package org.bahmni.module.bahmnicore.web.v1_0.controller.display.controls;

import org.bahmni.module.bahmnicore.service.BahmniDrugOrderService;
import org.bahmni.module.bahmnicore.web.v1_0.mapper.DrugOrderToRegimenMapper;
import org.openmrs.Order;
import org.openmrs.module.bahmniemrapi.drugogram.contract.Regimen;
import org.openmrs.module.webservices.rest.web.RestConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.text.ParseException;
import java.util.List;

@Controller
@RequestMapping(value = "/rest/" + RestConstants.VERSION_1 + "/bahmnicore/drugOGram/regimen")
public class DrugOGramController {

    private BahmniDrugOrderService bahmniDrugOrderService;
    private DrugOrderToRegimenMapper drugOrderToRegimenMapper;

    @Autowired
    public DrugOGramController(BahmniDrugOrderService bahmniDrugOrderService, DrugOrderToRegimenMapper drugOrderToRegimenMapper) {
        this.bahmniDrugOrderService = bahmniDrugOrderService;
        this.drugOrderToRegimenMapper = drugOrderToRegimenMapper;
    }

    @RequestMapping(method = RequestMethod.GET)
    @ResponseBody
    public Regimen getRegimen(@RequestParam(value = "patientUuid", required = true) String patientUuid,
                              @RequestParam(value = "drugs", required = false) List<String> drugs) throws ParseException {
        List<Order> allDrugOrders = bahmniDrugOrderService.getAllDrugOrders(patientUuid);
        return drugOrderToRegimenMapper.map(allDrugOrders);
    }
}
