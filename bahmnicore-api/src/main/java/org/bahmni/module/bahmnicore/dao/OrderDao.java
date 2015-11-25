package org.bahmni.module.bahmnicore.dao;

import org.openmrs.*;
import org.openmrs.module.emrapi.encounter.domain.EncounterTransaction;

import java.util.Collection;
import java.util.List;
import java.util.Set;

public interface OrderDao {
    List<Order> getCompletedOrdersFrom(List<Order> orders);

    List<DrugOrder> getPrescribedDrugOrders(Patient patient, Boolean includeActiveVisit, Integer numberOfVisits);

    List<Visit> getVisitsWithActiveOrders(Patient patient, String orderType, Boolean includeActiveVisit, Integer numberOfVisits);

    List<Visit> getVisitsWithAllOrders(Patient patient, String orderType, Boolean includeActiveVisit, Integer numberOfVisits);

    List<DrugOrder> getPrescribedDrugOrders(List<String> visitUuids);

    DrugOrder getDiscontinuedDrugOrder(DrugOrder drugOrder);

    List<DrugOrder> getPrescribedDrugOrdersForConcepts(Patient patient, Boolean includeActiveVisit, List<Visit> visits, List<Concept> conceptIds);

    Collection<EncounterTransaction.DrugOrder> getDrugOrderForRegimen(String regimenName);

    List<Visit> getVisitsForUUids(String[] visitUuids);

    List<Order> getAllOrders(Patient patient, List<OrderType> orderTypes, Integer offset, Integer limit);

    List<Order> getAllOrdersForVisits(OrderType orderType, List<Visit> visits);

    Order getOrderByUuid(String orderUuid);

    List<Order> getOrdersForVisitUuid(String visitUuid, String orderTypeUuid);

    List<Order> getAllOrders(Patient patientByUuid, OrderType drugOrderTypeUuid, Set<Concept> conceptsForDrugs);
}
