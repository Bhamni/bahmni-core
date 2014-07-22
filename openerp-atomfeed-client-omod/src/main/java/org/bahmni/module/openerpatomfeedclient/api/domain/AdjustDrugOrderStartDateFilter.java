package org.bahmni.module.openerpatomfeedclient.api.domain;

import org.apache.commons.lang.time.DateUtils;
import org.bahmni.module.bahmnicore.model.BahmniDrugOrder;
import org.bahmni.module.bahmnicore.service.BahmniDrugOrderService;
import org.bahmni.module.bahmnicore.service.BahmniPatientService;
import org.openmrs.Drug;
import org.openmrs.DrugOrder;
import org.openmrs.Patient;
import org.openmrs.api.ConceptService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class AdjustDrugOrderStartDateFilter {
    private BahmniDrugOrderService bahmniDrugOrderService;
    private ConceptService conceptService;
    private BahmniPatientService bahmniPatientService;

    @Autowired
    public AdjustDrugOrderStartDateFilter(BahmniDrugOrderService bahmniDrugOrderService, ConceptService conceptService, BahmniPatientService bahmniPatientService) {
        this.bahmniDrugOrderService = bahmniDrugOrderService;
        this.conceptService = conceptService;
        this.bahmniPatientService = bahmniPatientService;
    }

    public List<BahmniDrugOrder> execute(List<BahmniDrugOrder> newDrugOrders, String patientIdentifier) {
        Patient patient = bahmniPatientService.getPatientByIdentifier(patientIdentifier);
        List<DrugOrder> activeOrders = bahmniDrugOrderService.getActiveDrugOrders(patient.getUuid());

        for(BahmniDrugOrder newDrugOrder : newDrugOrders){
            DrugOrder activeDrugOrder = getActiveDrugOrder(activeOrders, newDrugOrder);
            if(activeDrugOrder != null) {
                newDrugOrder.setStartDate(DateUtils.addDays(activeDrugOrder.getAutoExpireDate(), 1));
            }
        }

        return newDrugOrders;
    }

    private DrugOrder getActiveDrugOrder(List<DrugOrder> activeOrders, BahmniDrugOrder newDrugOrder) {
        for (DrugOrder activeOrder : activeOrders) {
            Drug newDrug = conceptService.getDrugByUuid(newDrugOrder.getProductUuid());
            if (activeOrder.getConcept().equals(newDrug.getConcept())) {
                return activeOrder;
            }
        }
        return null;
    }
}
