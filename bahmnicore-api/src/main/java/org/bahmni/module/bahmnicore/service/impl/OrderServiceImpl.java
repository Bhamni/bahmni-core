package org.bahmni.module.bahmnicore.service.impl;

import org.bahmni.module.bahmnicore.dao.OrderDao;
import org.bahmni.module.bahmnicore.dao.VisitDao;
import org.bahmni.module.bahmnicore.service.OrderService;
import org.openmrs.*;
import org.openmrs.api.PatientService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;

@Service
public class OrderServiceImpl implements OrderService {

    private org.openmrs.api.OrderService orderService;
    private PatientService patientService;
    private VisitDao visitDao;
    private OrderDao orderDao;

    @Autowired
    public OrderServiceImpl(org.openmrs.api.OrderService orderService, PatientService patientService, VisitDao visitDao, OrderDao orderDao) {
        this.orderService = orderService;
        this.patientService = patientService;
        this.visitDao = visitDao;
        this.orderDao = orderDao;
    }

    @Override
    public List<Order> getPendingOrders(String patientUuid, String orderTypeUuid) {
        Patient patient = patientService.getPatientByUuid(patientUuid);
        List<Order> allOrders = orderService.getAllOrdersByPatient(patient);
        List<Order> completedOrders = orderDao.getCompletedOrdersFrom(Collections.unmodifiableList(allOrders));
        allOrders.removeAll(completedOrders);
        return allOrders;
    }

    @Override
    public List<Order> getAllOrders(String patientUuid, String orderTypeUuid, Integer offset, Integer limit) {
        Patient patient = patientService.getPatientByUuid(patientUuid);
        OrderType orderType = orderService.getOrderTypeByUuid(orderTypeUuid);
        return orderDao.getAllOrders(patient, Arrays.asList(orderType), offset, limit);
    }

    @Override
    public List<Visit> getVisitsWithOrders(Patient patient, String orderType, Boolean includeActiveVisit, Integer numberOfVisits){
        return orderDao.getVisitsWithActiveOrders(patient, orderType, includeActiveVisit, numberOfVisits);
    }

    @Override
    public List<Order> getAllOrdersForVisits(String patientUuid, String orderTypeUuid, Integer numberOfVisits) {
        Patient patient = patientService.getPatientByUuid(patientUuid);
        OrderType orderType = orderService.getOrderTypeByUuid(orderTypeUuid);
        List<Visit> visits = visitDao.getVisitsByPatient(patient, numberOfVisits);
        return orderDao.getAllOrdersForVisits(orderType, visits);
    }
    @Override
    public Order getOrderByUuid(String orderUuid){
        return orderDao.getOrderByUuid(orderUuid);
    }

    @Override
    public List<Order> getAllOrdersForVisitUuid(String visitUuid, String orderTypeUuid) {
        return orderDao.getOrdersForVisitUuid(visitUuid, orderTypeUuid);
    }
}