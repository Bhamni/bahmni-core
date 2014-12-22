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
            checkAndFixChainOverlapsWithCurrentDateOrder(orders);
        }

        return bahmniEncounterTransaction;
    }

    private void checkAndFixChainOverlapsWithCurrentDateOrder(Collection<EncounterTransaction.DrugOrder> orders) {
//        Refactor using Lambda expressions after updating to Java 8
        EncounterTransaction.DrugOrder currentDateOrder = getCurrentOrderFromOrderList(orders);
        Date expectedStartDateForCurrentOrder = setExpectedStartDateForOrder(currentDateOrder);
        Date expectedStopDateForCurrentOrder = setExpectedStopDateForOrder(currentDateOrder, expectedStartDateForCurrentOrder);

        if(currentDateOrder != null){
            for (EncounterTransaction.DrugOrder order : orders) {
                if(order!=currentDateOrder && order.getScheduledDate()!=null && order.getAction() != "DISCONTINUE" && DateUtils.isSameDay(order.getScheduledDate(), expectedStopDateForCurrentOrder)){
                    currentDateOrder.setScheduledDate(expectedStartDateForCurrentOrder);
                    currentDateOrder.setAutoExpireDate(expectedStopDateForCurrentOrder);

                    order.setScheduledDate(DrugOrderUtil.aMomentAfter(expectedStopDateForCurrentOrder));

                    currentDateOrder = order;
                    expectedStartDateForCurrentOrder = setExpectedStartDateForOrder(order);
                    expectedStopDateForCurrentOrder = setExpectedStopDateForOrder(currentDateOrder, expectedStartDateForCurrentOrder);
                }
            }
        }
    }

    private Date setExpectedStopDateForOrder(EncounterTransaction.DrugOrder order, Date expectedStartDateForCurrentOrder) {
        if(order == null){
            return null;
        }
        Concept durationUnitConcept = conceptService.getConceptByName(order.getDurationUnits());
        return DrugOrderUtil.calculateAutoExpireDate(order.getDuration(), durationUnitConcept, null, expectedStartDateForCurrentOrder, orderMetadataService.getOrderFrequencyByName(order.getDosingInstructions().getFrequency(), false));
    }

    private Date setExpectedStartDateForOrder(EncounterTransaction.DrugOrder order) {
        if(order == null){
            return null;
        }

        if( order.getScheduledDate() == null){
            return new Date();
        }
        return order.getScheduledDate();
    }

    private EncounterTransaction.DrugOrder getCurrentOrderFromOrderList(Collection<EncounterTransaction.DrugOrder> orders) {
        for (EncounterTransaction.DrugOrder order : orders) {
            if (order.getScheduledDate() == null && order.getAction() != "DISCONTINUE") { // To detect orders with dateActivated = current date
                return order;
            }
        }
        return null;
    }

}
