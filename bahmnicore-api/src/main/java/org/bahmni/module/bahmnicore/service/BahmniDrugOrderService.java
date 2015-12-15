package org.bahmni.module.bahmnicore.service;

import org.bahmni.module.bahmnicore.contract.drugorder.*;
import org.bahmni.module.bahmnicore.model.BahmniFeedDrugOrder;
import org.openmrs.*;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

public interface BahmniDrugOrderService {
    void add(String patientId, Date orderDate, List<BahmniFeedDrugOrder> bahmniDrugOrders, String systemUserName, String visitTypeName);
    List getActiveDrugOrders(String patientUuid);

    List<DrugOrder> getPrescribedDrugOrders(List<String> visitUuids, String patientUuid, Boolean includeActiveVisit, Integer numberOfVisit, Date startDate, Date endDate);

    List<DrugOrder> getPrescribedDrugOrdersForConcepts(Patient patient, Boolean includeActiveVisit, List<Visit> visits, List<Concept> concepts);

    DrugOrderConfigResponse getConfig();

    List<Order> getAllDrugOrders(String patientUuid, Set<Concept> conceptsForDrugs);

    Map<String,DrugOrder> getDiscontinuedDrugOrders(List<DrugOrder> drugOrders);
}
