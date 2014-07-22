package org.bahmni.module.openerpatomfeedclient.api.client.impl;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.log4j.Logger;
import org.bahmni.module.bahmnicore.service.BahmniDrugOrderService;
import org.bahmni.module.openerpatomfeedclient.api.OpenERPAtomFeedProperties;
import org.bahmni.module.openerpatomfeedclient.api.client.OpenERPSaleOrderFeedClient;
import org.bahmni.module.openerpatomfeedclient.api.worker.SaleOrderFeedEventWorker;
import org.ict4h.atomfeed.client.repository.AllFeeds;
import org.ict4h.atomfeed.client.repository.jdbc.AllFailedEventsJdbcImpl;
import org.ict4h.atomfeed.client.repository.jdbc.AllMarkersJdbcImpl;
import org.ict4h.atomfeed.client.service.AtomFeedClient;
import org.ict4h.atomfeed.client.service.FeedClient;
import org.joda.time.DateTime;
import org.openmrs.module.atomfeed.transaction.support.AtomFeedSpringTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;

public class OpenERPSaleOrderFeedClientImpl  {
    private static Logger logger = Logger.getLogger(OpenERPSaleOrderFeedClient.class);

    private FeedClient atomFeedClient;
    private OpenERPAtomFeedProperties properties;
    private PlatformTransactionManager transactionManager;
    private SaleOrderFeedEventWorker saleOrderFeedEventWorker;

    public OpenERPSaleOrderFeedClientImpl(
            OpenERPAtomFeedProperties properties,
            PlatformTransactionManager transactionManager, SaleOrderFeedEventWorker saleOrderFeedEventWorker) {
        this.properties = properties;
        this.transactionManager = transactionManager;
        this.saleOrderFeedEventWorker = saleOrderFeedEventWorker;
    }

    protected void initializeAtomFeedClient() {
        getAtomFeedClient();
    }

    private FeedClient getAtomFeedClient() {
        if(atomFeedClient == null) {
            String feedUri = properties.getFeedUri("sale.order.feed.uri");
            try {
                AtomFeedSpringTransactionManager txManager = new AtomFeedSpringTransactionManager(transactionManager);
                atomFeedClient = new AtomFeedClient(
                        new AllFeeds(properties, new HashMap<String, String>()),
                        new AllMarkersJdbcImpl(txManager),
                        new AllFailedEventsJdbcImpl(txManager),
                        properties,
                        txManager,
                        new URI(feedUri),
                        saleOrderFeedEventWorker);
            } catch (URISyntaxException e) {
                throw new RuntimeException(String.format("Is not a valid URI - %s", feedUri));
            }
        }
        return atomFeedClient;
    }

    protected void process(OpenERPSaleOrderProcessFeedClientImpl.FeedProcessor feedProcessor) {
        try {
            logger.info("openerpatomfeedclient:processing feed " + DateTime.now());
            feedProcessor.process(getAtomFeedClient());
        } catch (Exception e) {
            try {
                if (e != null && ExceptionUtils.getStackTrace(e).contains("HTTP response code: 401")) {
                    initializeAtomFeedClient();
                } else {
                    logger.error("Could not process Sale order feed", e);
                    initializeAtomFeedClient();
                }
            } catch (Exception ex){
                logger.error("openerpatomfeedclient:failed feed execution " + e, e);
                throw new RuntimeException(ex);
            }
        }
    }


}
