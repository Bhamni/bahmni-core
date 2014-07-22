package org.bahmni.module.openerpatomfeedclient.api.client.impl;

import org.bahmni.module.openerpatomfeedclient.api.OpenERPAtomFeedProperties;
import org.bahmni.module.openerpatomfeedclient.api.client.OpenERPSaleOrderFailedFeedClient;
import org.bahmni.module.openerpatomfeedclient.api.worker.SaleOrderFeedEventWorker;
import org.ict4h.atomfeed.client.service.FeedClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;

@Component("openERPSaleOrderProcessFailedFeedClient")
public class OpenERPSaleOrderFailedFeedClientImpl extends OpenERPSaleOrderFeedClientImpl implements OpenERPSaleOrderFailedFeedClient{

    @Autowired
    public OpenERPSaleOrderFailedFeedClientImpl(OpenERPAtomFeedProperties properties, PlatformTransactionManager transactionManager, SaleOrderFeedEventWorker saleOrderFeedEventWorker) {
        super(properties, transactionManager, saleOrderFeedEventWorker);
    }

    @Override
    public void processFailedFeed() {
        process(new ProcessFailedFeed());
    }

    private static class ProcessFailedFeed implements OpenERPSaleOrderProcessFeedClientImpl.FeedProcessor {
        public void process(FeedClient atomFeedClient){
            atomFeedClient.processFailedEvents();
        }
    }
}
