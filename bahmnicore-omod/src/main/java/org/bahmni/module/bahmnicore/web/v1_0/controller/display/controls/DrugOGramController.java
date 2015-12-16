package org.bahmni.module.bahmnicore.web.v1_0.controller.display.controls;

import org.bahmni.module.bahmnicore.extensions.BahmniExtensions;
import org.bahmni.module.bahmnicore.service.BahmniDrugOrderService;
import org.bahmni.module.bahmnicore.web.v1_0.mapper.DrugOrderToTreatmentRegimenMapper;
import org.openmrs.Concept;
import org.openmrs.Order;
import org.openmrs.api.ConceptService;
import org.openmrs.module.bahmniemrapi.drugogram.contract.BaseTableExtension;
import org.openmrs.module.bahmniemrapi.drugogram.contract.TreatmentRegimen;
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
    private DrugOrderToTreatmentRegimenMapper drugOrderToTreatmentRegimenMapper;
    private ConceptService conceptService;
    private BahmniExtensions bahmniExtensions;

    @Autowired
    public DrugOGramController(BahmniDrugOrderService bahmniDrugOrderService, DrugOrderToTreatmentRegimenMapper drugOrderToTreatmentRegimenMapper, ConceptService conceptService, BahmniExtensions bahmniExtensions) {
        this.bahmniDrugOrderService = bahmniDrugOrderService;
        this.drugOrderToTreatmentRegimenMapper = drugOrderToTreatmentRegimenMapper;
        this.conceptService = conceptService;
        this.bahmniExtensions = bahmniExtensions;
    }

    @RequestMapping(method = RequestMethod.GET)
    @ResponseBody
    public TreatmentRegimen getRegimen(@RequestParam(value = "patientUuid", required = true) String patientUuid,
                                       @RequestParam(value = "drugs", required = false) List<String> drugs,
                                       @RequestParam(value = "startDate", required = false) String startDate,
                                       @RequestParam(value = "endDate", required = false) String endDate) throws ParseException {
        Set<Concept> conceptsForDrugs = getConceptsForDrugs(drugs);
        List<Order> allDrugOrders = bahmniDrugOrderService.getAllDrugOrders(patientUuid, conceptsForDrugs, startDate, endDate);
        TreatmentRegimen treatmentRegimen = drugOrderToTreatmentRegimenMapper.map(allDrugOrders, conceptsForDrugs);
        BaseTableExtension<TreatmentRegimen> extension = bahmniExtensions.getExtension("TreatmentRegimenExtension.groovy");
        extension.update(treatmentRegimen);
        return treatmentRegimen;
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
}
