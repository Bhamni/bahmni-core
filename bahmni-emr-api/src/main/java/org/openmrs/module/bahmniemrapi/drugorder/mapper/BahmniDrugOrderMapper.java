package org.openmrs.module.bahmniemrapi.drugorder.mapper;

import org.apache.commons.collections.CollectionUtils;
import org.openmrs.DrugOrder;
import org.openmrs.module.bahmniemrapi.drugorder.contract.BahmniDrugOrder;
import org.openmrs.module.bahmniemrapi.encountertransaction.contract.BahmniObservation;
import org.openmrs.module.emrapi.encounter.ConceptMapper;
import org.openmrs.module.emrapi.encounter.OrderMapper;
import org.openmrs.module.emrapi.encounter.mapper.OrderMapper1_12;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Locale;
public class BahmniDrugOrderMapper {

    private BahmniProviderMapper providerMapper;
    private OrderAttributesMapper orderAttributesMapper;
    private ConceptMapper conceptMapper;

    public BahmniDrugOrderMapper() {
        this.providerMapper = new BahmniProviderMapper();
        this.orderAttributesMapper = new OrderAttributesMapper();
        this.conceptMapper = new ConceptMapper();
    }

    public List<BahmniDrugOrder> mapToResponse(List<DrugOrder> activeDrugOrders,
                                               Collection<BahmniObservation> orderAttributeObs,
                                               Map<String, DrugOrder> discontinuedOrderMap,
                                               String locale) throws IOException {

        OrderMapper drugOrderMapper = new OrderMapper1_12();

        List<BahmniDrugOrder> bahmniDrugOrders = new ArrayList<>();

        for (DrugOrder openMRSDrugOrder : activeDrugOrders) {
            BahmniDrugOrder bahmniDrugOrder = new BahmniDrugOrder();
            bahmniDrugOrder.setDrugOrder(drugOrderMapper.mapDrugOrder(openMRSDrugOrder));
            if(locale != null) {
                bahmniDrugOrder.getDrugOrder().getDosingInstructions().setFrequency(openMRSDrugOrder.getFrequency().getConcept().getPreferredName(new Locale((locale))).getName());
            }
            bahmniDrugOrder.setVisit(openMRSDrugOrder.getEncounter().getVisit());
            bahmniDrugOrder.setProvider(providerMapper.map(openMRSDrugOrder.getOrderer()));
            if(openMRSDrugOrder.getDrug() != null){
                bahmniDrugOrder.setRetired(openMRSDrugOrder.getDrug().getRetired());
            }

            bahmniDrugOrder.setCreatorName(openMRSDrugOrder.getCreator().getPersonName().toString());
            if(discontinuedOrderMap.containsKey(openMRSDrugOrder.getOrderNumber())){
                bahmniDrugOrder.setOrderReasonText(discontinuedOrderMap.get(openMRSDrugOrder.getOrderNumber()).getOrderReasonNonCoded());
                bahmniDrugOrder.setOrderReasonConcept(conceptMapper.map(discontinuedOrderMap.get(openMRSDrugOrder.getOrderNumber()).getOrderReason()));
            }

            bahmniDrugOrders.add(bahmniDrugOrder);
        }
        if(CollectionUtils.isNotEmpty(orderAttributeObs)){
           bahmniDrugOrders = orderAttributesMapper.map(bahmniDrugOrders,orderAttributeObs);
        }
        return bahmniDrugOrders;
    }

    public void setMappers(BahmniProviderMapper bahmniProviderMapper, OrderAttributesMapper orderAttributesMapper, ConceptMapper conceptMapper){
        providerMapper = bahmniProviderMapper;
        this.orderAttributesMapper = orderAttributesMapper;
        this.conceptMapper = conceptMapper;
    }
}
