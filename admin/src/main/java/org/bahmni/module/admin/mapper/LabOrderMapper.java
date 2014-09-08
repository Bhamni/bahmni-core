package org.bahmni.module.admin.mapper;

import org.apache.commons.lang.StringUtils;
import org.bahmni.csv.KeyValue;
import org.bahmni.module.admin.csv.models.EncounterRow;
import org.openmrs.CareSetting;
import org.openmrs.Concept;
import org.openmrs.OrderType;
import org.openmrs.User;
import org.openmrs.api.ConceptService;
import org.openmrs.api.OrderService;
import org.openmrs.module.emrapi.encounter.domain.EncounterTransaction;
import org.openmrs.module.emrapi.encounter.exception.ConceptNotFoundException;


import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

public class LabOrderMapper {
    private HashMap<String, EncounterTransaction.Concept> cachedConcepts = new HashMap<>();

    private ConceptService conceptService;
    private OrderService orderService;
    private OrderType labOrderType;

    public LabOrderMapper(ConceptService conceptService, OrderService orderService) {
        this.conceptService = conceptService;
        this.orderService = orderService;
    }

    public List<EncounterTransaction.TestOrder> getLabOrders(EncounterRow encounterRow, User user, Set<EncounterTransaction.Provider> providers) throws ParseException {
        if (encounterRow.labResultRows == null || encounterRow.labResultRows.isEmpty())
            return new ArrayList<>();

        List<EncounterTransaction.TestOrder> testOrders = new ArrayList<>();
        Date encounterDate = encounterRow.getEncounterDate();
        for (KeyValue labResultRow : encounterRow.labResultRows) {
            if (labResultRow.getValue() != null && !StringUtils.isEmpty(labResultRow.getValue().trim())) {
                EncounterTransaction.TestOrder testOrder = new EncounterTransaction.TestOrder();
                testOrder.setConcept(getConcept(labResultRow.getKey()));
                testOrder.setOrderTypeUuid(getLabOrderType().getUuid());
                testOrder.setDateCreated(encounterDate);
                testOrder.setCareSetting(orderService.getCareSettingByName(CareSetting.CareSettingType.OUTPATIENT.toString()));
//                testOrder.setCreator(user);
//                testOrder.setOrderer(providers.iterator().next());

//                order.setAccessionNumber();
//                order.setPatient(patient);
//                order.setDateActivated();
//                order.setAutoExpireDate();

                testOrders.add(testOrder);
            }
        }
        return testOrders;

    }

    private OrderType getLabOrderType() {
        if (labOrderType == null)
            labOrderType = orderService.getOrderTypeByName("Lab Order");

        return labOrderType;
    }

    protected EncounterTransaction.Concept getConcept(String conceptName) {
        if (!cachedConcepts.containsKey(conceptName)) {
            cachedConcepts.put(conceptName, fetchConcept(conceptName));
        }
        return cachedConcepts.get(conceptName);
    }

    private EncounterTransaction.Concept fetchConcept(String conceptName) {
        Concept obsConcept = conceptService.getConceptByName(conceptName);
        if (obsConcept == null)
            throw new ConceptNotFoundException("Concept '"+ conceptName +"' not found");


        return new EncounterTransaction.Concept(obsConcept.getUuid(), obsConcept.getName().getName());
    }


}