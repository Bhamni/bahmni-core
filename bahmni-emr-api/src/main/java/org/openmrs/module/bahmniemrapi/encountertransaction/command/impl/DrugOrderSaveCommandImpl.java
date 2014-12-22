package org.openmrs.module.bahmniemrapi.encountertransaction.command.impl;

import org.apache.commons.lang3.time.DateUtils;
import org.openmrs.Concept;
import org.openmrs.api.ConceptService;
import org.openmrs.module.bahmniemrapi.drugorder.DrugOrderUtil;
import org.openmrs.module.bahmniemrapi.encountertransaction.command.EncounterDataPreSaveCommand;
import org.openmrs.module.bahmniemrapi.encountertransaction.contract.BahmniEncounterTransaction;
import org.openmrs.module.emrapi.encounter.domain.EncounterTransaction;
import org.openmrs.module.emrapi.encounter.service.OrderMetadataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class DrugOrderSaveCommandImpl implements EncounterDataPreSaveCommand {

    private OrderMetadataService orderMetadataService;
    private ConceptService conceptService;

    Comparator<EncounterTransaction.DrugOrder> drugOrderStartDateComparator = new Comparator<EncounterTransaction.DrugOrder>() {
        @Override
        public int compare(EncounterTransaction.DrugOrder o1, EncounterTransaction.DrugOrder o2) {
            Date date1 = o1.getScheduledDate();
            Date date2 = o2.getScheduledDate();
            if(date1 == null){
                date1 = new Date();
            }
            if(date2 == null){
                date2 = new Date();
            }
            return date1.compareTo(date2);
        }
    };


    @Autowired
    public DrugOrderSaveCommandImpl(OrderMetadataService orderMetadataService, ConceptService conceptService) {
        this.orderMetadataService = orderMetadataService;
        this.conceptService = conceptService;
    }

    @Override
    public BahmniEncounterTransaction update(BahmniEncounterTransaction bahmniEncounterTransaction) {
        List<EncounterTransaction.DrugOrder> drugOrders = bahmniEncounterTransaction.getDrugOrders();
        Map<String,List<EncounterTransaction.DrugOrder>> sameDrugUuidOrderLists = new HashMap<>();
        for (EncounterTransaction.DrugOrder drugOrder : drugOrders) {
            String uuid = drugOrder.getDrug().getUuid();
            if(sameDrugUuidOrderLists.get(uuid) == null){
                sameDrugUuidOrderLists.put(uuid, new ArrayList<EncounterTransaction.DrugOrder>());
            }
            sameDrugUuidOrderLists.get(uuid).add(drugOrder);
        }

        for (List<EncounterTransaction.DrugOrder> orders : sameDrugUuidOrderLists.values()) {
            Collections.sort(orders, drugOrderStartDateComparator);
            checkAndFixOverlappingOrderWithCurrentDateOrder(orders);
        }

        return bahmniEncounterTransaction;
    }

    private void checkAndFixOverlappingOrderWithCurrentDateOrder(Collection<EncounterTransaction.DrugOrder> orders) {
//        Refactor using Lambda expressions after updating to Java 8
        EncounterTransaction.DrugOrder currentDateOrder = null;
        Date expectedStartDateForCurrentOrder = null;
        Date expectedStopDateForCurrentOrder = null;
        for (EncounterTransaction.DrugOrder order : orders) {
            if (order.getScheduledDate() == null && order.getAction() != "DISCONTINUE") { // To detect orders with dateActivated = current date
                currentDateOrder = order;
                Concept durationUnitConcept = conceptService.getConceptByName(order.getDurationUnits());
                if( order.getScheduledDate() == null){
                    expectedStartDateForCurrentOrder = new Date();
                }
                else{
                    expectedStartDateForCurrentOrder = order.getScheduledDate();
                }
                expectedStopDateForCurrentOrder = DrugOrderUtil.calculateAutoExpireDate(order.getDuration(), durationUnitConcept, null, expectedStartDateForCurrentOrder, orderMetadataService.getOrderFrequencyByName(order.getDosingInstructions().getFrequency(), false));
                break;
            }
        }
        if(currentDateOrder != null){
            for (EncounterTransaction.DrugOrder order : orders) {
                if(order!=currentDateOrder && order.getScheduledDate()!=null && order.getAction() != "DISCONTINUE" && DateUtils.isSameDay(order.getScheduledDate(), expectedStopDateForCurrentOrder)){
                    currentDateOrder.setScheduledDate(expectedStartDateForCurrentOrder);
                    currentDateOrder.setAutoExpireDate(expectedStopDateForCurrentOrder);
                    order.setScheduledDate(DrugOrderUtil.aMomentAfter(expectedStopDateForCurrentOrder));
                    Concept durationUnitConcept = conceptService.getConceptByName(order.getDurationUnits());
                    order.setAutoExpireDate(DrugOrderUtil.calculateAutoExpireDate(order.getDuration(), durationUnitConcept, null, order.getScheduledDate(), orderMetadataService.getOrderFrequencyByName(order.getDosingInstructions().getFrequency(), false)));
                }
            }
        }
    }

}
