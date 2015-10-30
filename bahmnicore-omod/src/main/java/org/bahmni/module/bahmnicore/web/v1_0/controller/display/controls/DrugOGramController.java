package org.bahmni.module.bahmnicore.web.v1_0.controller.display.controls;

import org.bahmni.module.bahmnicore.service.BahmniDrugOrderService;
import org.bahmni.module.bahmnicore.web.v1_0.mapper.DrugOrderToRegimenMapper;
import org.openmrs.Concept;
import org.openmrs.Order;
import org.openmrs.api.ConceptService;
import org.openmrs.module.bahmniemrapi.drugogram.contract.Regimen;
import org.openmrs.module.webservices.rest.web.RestConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.text.ParseException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Controller
@RequestMapping(value = "/rest/" + RestConstants.VERSION_1 + "/bahmnicore/drugOGram/regimen")
public class DrugOGramController {

    private BahmniDrugOrderService bahmniDrugOrderService;
    private DrugOrderToRegimenMapper drugOrderToRegimenMapper;
    private ConceptService conceptService;

    @Autowired
    public DrugOGramController(BahmniDrugOrderService bahmniDrugOrderService, DrugOrderToRegimenMapper drugOrderToRegimenMapper, ConceptService conceptService) {
        this.bahmniDrugOrderService = bahmniDrugOrderService;
        this.drugOrderToRegimenMapper = drugOrderToRegimenMapper;
        this.conceptService = conceptService;
    }

    @RequestMapping(method = RequestMethod.GET)
    @ResponseBody
    public Regimen getRegimen(@RequestParam(value = "patientUuid", required = true) String patientUuid,
                              @RequestParam(value = "drugs", required = false) List<String> drugs) throws ParseException {
        Set<Concept> conceptsForDrugs = getConceptsForDrugs(drugs);
        List<Order> allDrugOrders = bahmniDrugOrderService.getAllDrugOrders(patientUuid, conceptsForDrugs);
        return drugOrderToRegimenMapper.map(allDrugOrders, conceptsForDrugs);
    }

    private Set<Concept> getConceptsForDrugs(List<String> drugs) {
        if (drugs == null) return null;
        Set<Concept> drugConcepts = new HashSet<>();
        for (String drug : drugs) {
            Concept concept = conceptService.getConceptByName(drug);
            if (concept != null) {
                drugConcepts.add(concept);
            }
        }
        return drugConcepts;
    }
}
