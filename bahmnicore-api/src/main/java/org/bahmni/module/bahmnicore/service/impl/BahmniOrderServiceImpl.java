package org.bahmni.module.bahmnicore.service.impl;

import org.apache.commons.collections.CollectionUtils;
import org.apache.log4j.Logger;
import org.bahmni.module.bahmnicore.service.BahmniObsService;
import org.bahmni.module.bahmnicore.service.BahmniOrderService;
import org.bahmni.module.bahmnicore.service.OrderService;
import org.openmrs.Concept;
import org.openmrs.Order;
import org.openmrs.module.bahmniemrapi.encountertransaction.contract.BahmniObservation;
import org.openmrs.module.bahmniemrapi.order.contract.BahmniOrder;
import org.openmrs.module.emrapi.encounter.ConceptMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;


@Service
public class BahmniOrderServiceImpl implements BahmniOrderService {

    private ConceptMapper conceptMapper;
    private OrderService orderService;
    private BahmniObsService bahmniObsService;
    private static final Logger log = Logger.getLogger(BahmniOrderServiceImpl.class);

    @Autowired
    public BahmniOrderServiceImpl(OrderService orderService, BahmniObsService bahmniObsService, ConceptMapper conceptMapper) {
        this.orderService = orderService;
        this.bahmniObsService = bahmniObsService;
        this.conceptMapper = conceptMapper;

    }

    @Override
    public List<BahmniOrder> ordersForOrderType(String patientUuid, List<Concept> concepts, Integer numberOfVisits, List<String> obsIgnoreList, String orderTypeUuid, Boolean includeObs) {
        List<BahmniOrder> bahmniOrders = new ArrayList<>();
        List<Order> orders;
        if(numberOfVisits == null || numberOfVisits ==0){
            orders = orderService.getAllOrders(patientUuid, orderTypeUuid, null, null);
        }else {
            orders = orderService.getAllOrdersForVisits(patientUuid, orderTypeUuid, numberOfVisits);
        }

        for (Order order : orders) {
            Collection<BahmniObservation> obs = bahmniObsService.observationsFor(patientUuid, concepts, null, obsIgnoreList, false, order, null, null);
            BahmniOrder bahmniOrder = createBahmniOrder(order, obs, includeObs);

            bahmniOrders.add(bahmniOrder);
        }
        return bahmniOrders;
    }

    @Override
    public List<BahmniOrder> ordersForOrderUuid(String patientUuid, List<Concept> concepts, List<String> obsIgnoreList, String orderUuid) {
        List<BahmniOrder> bahmniOrders = new ArrayList<>();
        Order order = orderService.getOrderByUuid(orderUuid);
        Collection<BahmniObservation> obs = bahmniObsService.observationsFor(patientUuid, concepts, null, obsIgnoreList, false, order, null, null);
        BahmniOrder bahmniOrder = createBahmniOrder(order, obs, true);
        bahmniOrders.add(bahmniOrder);
        return bahmniOrders;
    }

    @Override
    public List<BahmniOrder> ordersForVisit(String visitUuid, String orderTypeUuid, List<String> conceptNames, List<Concept> obsIgnoreList) {
        List<Order> orders = orderService.getAllOrdersForVisitUuid(visitUuid, orderTypeUuid);
        List<BahmniOrder> bahmniOrders = new ArrayList<>();
        for (Order order : orders) {
            Collection<BahmniObservation> observations = bahmniObsService.getObservationForVisit(visitUuid, conceptNames, obsIgnoreList, false, order);
            BahmniOrder bahmniOrder = createBahmniOrder(order, observations, true);
            bahmniOrders.add(bahmniOrder);
        }
        return bahmniOrders;
    }

    private BahmniOrder createBahmniOrder(Order order, Collection<BahmniObservation> bahmniObservations, boolean includeObs){
        BahmniOrder bahmniOrder = new BahmniOrder();

        bahmniOrder.setOrderDateTime(order.getDateActivated());
        bahmniOrder.setOrderNumber(order.getOrderNumber());
        bahmniOrder.setOrderTypeUuid(order.getOrderType().getUuid());
        bahmniOrder.setOrderUuid(order.getUuid());
        bahmniOrder.setProvider(order.getOrderer().getName());
        bahmniOrder.setConcept(conceptMapper.map(order.getConcept()));
        bahmniOrder.setHasObservations(CollectionUtils.isNotEmpty(bahmniObservations));
        bahmniOrder.setCommentToFulfiller(order.getCommentToFulfiller());
        if(includeObs) {
            bahmniOrder.setBahmniObservations(bahmniObservations);
        }
        return bahmniOrder;
    }
}
