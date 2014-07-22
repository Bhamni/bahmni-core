package org.bahmni.module.openerpatomfeedclient.api.worker;

import org.apache.log4j.Logger;
import org.bahmni.module.bahmnicore.model.BahmniDrugOrder;
import org.bahmni.module.bahmnicore.service.BahmniDrugOrderService;
import org.bahmni.module.openerpatomfeedclient.api.OpenERPAtomFeedProperties;
import org.bahmni.module.openerpatomfeedclient.api.domain.AdjustDrugOrderStartDateFilter;
import org.bahmni.module.openerpatomfeedclient.api.domain.SaleOrder;
import org.bahmni.module.openerpatomfeedclient.api.exception.OpenERPFeedException;
import org.bahmni.module.openerpatomfeedclient.api.util.ObjectMapperRepository;
import org.ict4h.atomfeed.client.domain.Event;
import org.ict4h.atomfeed.client.service.EventWorker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class SaleOrderFeedEventWorker implements EventWorker{
    private static Logger logger = Logger.getLogger(SaleOrderFeedEventWorker.class);
    private BahmniDrugOrderService bahmniDrugOrderService;
    private OpenERPAtomFeedProperties properties;
    private AdjustDrugOrderStartDateFilter adjustDrugOrderStartDateFilter;

    @Autowired
    public SaleOrderFeedEventWorker(BahmniDrugOrderService bahmniDrugOrderService, OpenERPAtomFeedProperties properties, AdjustDrugOrderStartDateFilter adjustDrugOrderStartDateFilter) {
        this.bahmniDrugOrderService = bahmniDrugOrderService;
        this.properties = properties;
        this.adjustDrugOrderStartDateFilter = adjustDrugOrderStartDateFilter;
    }

    @Override
    public void process(Event event) {
        String saleOrderContent = event.getContent();
        logger.info("openERPatomfeedclient:Processing : " + saleOrderContent);
        try {
            SaleOrder saleOrder = ObjectMapperRepository.objectMapper.readValue(saleOrderContent, SaleOrder.class);
            String patientIdentifier = saleOrder.getCustomerId();
            if (saleOrder.getExternalId() == null || saleOrder.getExternalId().trim().length() == 0) {

                List<BahmniDrugOrder> bahmniDrugOrders = saleOrder.getSaleOrderItems();
                for (BahmniDrugOrder bahmniDrugOrder : bahmniDrugOrders) {
                    bahmniDrugOrder.setStartDate(saleOrder.getOrderDate());
                }

                List<BahmniDrugOrder> adjustedDrugOrders = adjustDrugOrderStartDateFilter.execute(bahmniDrugOrders, patientIdentifier);
                bahmniDrugOrderService.add(patientIdentifier, saleOrder.getOrderDate(), adjustedDrugOrders, properties.getSystemUserName());
            }
        } catch (Exception e) {
            logger.error("openERPatomfeedclient:error processing : " + saleOrderContent + e.getMessage(), e);
            throw new OpenERPFeedException("Could not sync Sale Order data", e);
        }
    }

    @Override
    public void cleanUp(Event event) {
    }
}
