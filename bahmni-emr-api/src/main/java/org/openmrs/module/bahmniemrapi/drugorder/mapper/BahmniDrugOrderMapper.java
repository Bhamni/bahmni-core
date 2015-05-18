package org.openmrs.module.bahmniemrapi.drugorder.mapper;

import org.apache.commons.collections.CollectionUtils;
import org.openmrs.DrugOrder;
import org.openmrs.module.bahmniemrapi.drugorder.contract.BahmniDrugOrder;
import org.openmrs.module.bahmniemrapi.encountertransaction.contract.BahmniObservation;
import org.openmrs.module.emrapi.encounter.OrderMapper;
import org.openmrs.module.emrapi.encounter.mapper.OrderMapper1_10;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class BahmniDrugOrderMapper {

    private BahmniProviderMapper providerMapper;
    private OrderAttributesMapper orderAttributesMapper;

    public BahmniDrugOrderMapper(BahmniProviderMapper providerMapper, OrderAttributesMapper orderAttributesMapper) {
        this.providerMapper = providerMapper;
        this.orderAttributesMapper = orderAttributesMapper;
    }

    public List<BahmniDrugOrder> mapToResponse(List<DrugOrder> activeDrugOrders, Collection<BahmniObservation> orderAttributeObs) throws IOException {

        OrderMapper drugOrderMapper = new OrderMapper1_10();

        List<BahmniDrugOrder> bahmniDrugOrders = new ArrayList<>();

        for (DrugOrder openMRSDrugOrder : activeDrugOrders) {
            BahmniDrugOrder bahmniDrugOrder = new BahmniDrugOrder();
            bahmniDrugOrder.setDrugOrder(drugOrderMapper.mapDrugOrder(openMRSDrugOrder));
            bahmniDrugOrder.setVisit(openMRSDrugOrder.getEncounter().getVisit());
            bahmniDrugOrder.setProvider(providerMapper.map(openMRSDrugOrder.getOrderer()));
            bahmniDrugOrder.setPersonName(openMRSDrugOrder.getCreator().getPersonName().toString());
            bahmniDrugOrders.add(bahmniDrugOrder);
        }
        if(CollectionUtils.isNotEmpty(orderAttributeObs)){
           bahmniDrugOrders = orderAttributesMapper.map(bahmniDrugOrders,orderAttributeObs);
        }
        return bahmniDrugOrders;
    }
}