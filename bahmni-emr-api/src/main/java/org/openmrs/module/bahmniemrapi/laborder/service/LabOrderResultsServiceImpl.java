package org.openmrs.module.bahmniemrapi.laborder.service;

import org.openmrs.Encounter;
import org.openmrs.EncounterProvider;
import org.openmrs.Obs;
import org.openmrs.Order;
import org.openmrs.Patient;
import org.openmrs.Provider;
import org.openmrs.TestOrder;
import org.openmrs.Visit;
import org.openmrs.api.EncounterService;
import org.openmrs.api.db.hibernate.HibernateUtil;
import org.openmrs.module.bahmniemrapi.accessionnote.contract.AccessionNote;
import org.openmrs.module.bahmniemrapi.laborder.contract.LabOrderResult;
import org.openmrs.module.bahmniemrapi.laborder.contract.LabOrderResults;
import org.openmrs.module.emrapi.encounter.EncounterTransactionMapper;
import org.openmrs.module.emrapi.encounter.OrderMapper;
import org.openmrs.module.emrapi.encounter.domain.EncounterTransaction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;


@Service
public class LabOrderResultsServiceImpl implements LabOrderResultsService {
    public static final String LAB_ABNORMAL = "LAB_ABNORMAL";
    public static final String LAB_MINNORMAL = "LAB_MINNORMAL";
    public static final String LAB_MAXNORMAL = "LAB_MAXNORMAL";
    public static final String LAB_NOTES = "LAB_NOTES";
    private static final String REFERRED_OUT = "REFERRED_OUT";
    public static final String LAB_REPORT = "LAB_REPORT";
    private static final String VALIDATION_NOTES_ENCOUNTER_TYPE = "VALIDATION NOTES";
    public static final String LAB_ORDER_TYPE = "Lab Order";

    @Autowired
    private EncounterTransactionMapper encounterTransactionMapper;

    @Autowired
    private EncounterService encounterService;
    
    @Autowired
    private OrderMapper orderMapper;

    @Override
    @Transactional(readOnly = true)
    public LabOrderResults getAll(Patient patient, List<Visit> visits, int numberOfAccessions) {
        List<EncounterTransaction.Order> testOrders = new ArrayList<>();
        List<EncounterTransaction.Observation> observations = new ArrayList<>();
        Map<String, Encounter> encounterTestOrderUuidMap = new HashMap<>();
        Map<String, Encounter> encounterObservationMap = new HashMap<>();
        Map<String, List<AccessionNote>> encounterToAccessionNotesMap = new HashMap<>();

        List<Encounter> encounters = encounterService.getEncounters(patient, null, null, null, null, null, null, null, visits, false);

        int totalEncounters = encounters.size();
        int currentAccession = 0;
        for (int count = totalEncounters - 1; count >= 0; count--) {
            Encounter encounter = encounters.get(count);
            if (currentAccession >= numberOfAccessions) {
                break;
            }

            EncounterTransaction encounterTransaction = encounterTransactionMapper.map(encounter, false);
            List<EncounterTransaction.Order> existingTestOrders = filterTestOrders(encounterTransaction, encounter, encounterTestOrderUuidMap, null, null, null);
            testOrders.addAll(existingTestOrders);
            List<EncounterTransaction.Observation> nonVoidedObservations = filterObservations(encounterTransaction.getObservations(), null, null);
            observations.addAll(nonVoidedObservations);
            createAccessionNotesByEncounter(encounterToAccessionNotesMap, encounters, encounter);
            mapObservationsWithEncounter(nonVoidedObservations, encounter, encounterObservationMap);
            if (existingTestOrders.size() > 0) {
                currentAccession++;
            }
        }

        List<LabOrderResult> labOrderResults = mapOrdersWithObs(testOrders, observations, encounterTestOrderUuidMap, encounterObservationMap, encounterToAccessionNotesMap);

        return new LabOrderResults(filterLabOrderResults(labOrderResults));
    }

    private List<LabOrderResult> filterLabOrderResults(List<LabOrderResult> labOrderResults) {
        List<LabOrderResult> filteredResults = new ArrayList<>();
        for (LabOrderResult labOrderResult : labOrderResults) {
            if (labOrderResult.getResult() != null) {
                filteredResults.add(labOrderResult);
            } else if (labOrderResult.getAction().equals(Order.Action.NEW.toString())) {
                filteredResults.add(labOrderResult);
            }
        }
        return filteredResults;
    }

    @Override
    public List<LabOrderResult> getAllForConcepts(Patient patient, Collection<String> concepts, List<Visit> visits, Date startDate, Date endDate) {
        if (concepts != null && !concepts.isEmpty()) {

            List<EncounterTransaction.Order> testOrders = new ArrayList<>();
            List<EncounterTransaction.Observation> observations = new ArrayList<>();
            Map<String, Encounter> encounterTestOrderUuidMap = new HashMap<>();
            Map<String, Encounter> encounterObservationMap = new HashMap<>();
            Map<String, List<AccessionNote>> encounterToAccessionNotesMap = new HashMap<>();

            List<Encounter> encounters = encounterService.getEncounters(patient, null, null, null, null, null, null, null, visits, false);
            for (Encounter encounter : encounters) {
                EncounterTransaction encounterTransaction = encounterTransactionMapper.map(encounter, false);
                testOrders.addAll(filterTestOrders(encounterTransaction, encounter, encounterTestOrderUuidMap, concepts, startDate, endDate));
                List<EncounterTransaction.Observation> filteredObservations = filterObservations(encounterTransaction.getObservations(), startDate, endDate);
                observations.addAll(filteredObservations);
                createAccessionNotesByEncounter(encounterToAccessionNotesMap, encounters, encounter);
                mapObservationsWithEncounter(filteredObservations, encounter, encounterObservationMap);
            }
            return mapOrdersWithObs(testOrders, observations, encounterTestOrderUuidMap, encounterObservationMap, encounterToAccessionNotesMap);
        }
        return new ArrayList<>();
    }

    private void createAccessionNotesByEncounter(Map<String, List<AccessionNote>> encounterToAccessionNotesMap, List<Encounter> encounters, Encounter encounter) {
        List<AccessionNote> accessionNotes = getAccessionNotesFor(encounter, encounters);
        if (accessionNotes.size() != 0) {
            List<AccessionNote> existingAccessionNotes = encounterToAccessionNotesMap.get(encounter.getUuid());
            if (existingAccessionNotes != null) {
                accessionNotes.addAll(existingAccessionNotes);
            }
            encounterToAccessionNotesMap.put(encounter.getUuid(), accessionNotes);
        }
    }

    private List<AccessionNote> getAccessionNotesFor(Encounter orderEncounter, List<Encounter> encounters) {
        for (Encounter encounter : encounters) {
            if (VALIDATION_NOTES_ENCOUNTER_TYPE.equals(encounter.getEncounterType().getName()) && hasValidationNotesFor(orderEncounter.getUuid(), encounter)) {
                return createAccessionNotesFor(orderEncounter.getUuid(), encounter);
            }
        }
        return new ArrayList<>();
    }

    private List<AccessionNote> createAccessionNotesFor(String encounterUuid, Encounter accessionNotesEncounter) {
        List<AccessionNote> accessionNotes = new ArrayList<>();
        for (Obs observation : accessionNotesEncounter.getAllObs()) {
            if (!encounterUuid.equals(observation.getValueText())) {
                AccessionNote accessionNote = new AccessionNote();
                accessionNote.setAccessionUuid(encounterUuid);
                accessionNote.setDateTime(observation.getObsDatetime());
                accessionNote.setText(observation.getValueText());
                Collection<Set<Provider>> providersForRole = accessionNotesEncounter.getProvidersByRoles().values();
                if (providersForRole.size() > 0) {
                    Provider provider = providersForRole.iterator().next().iterator().next();
                    accessionNote.setProviderName(provider.getName());
                }
                accessionNotes.add(accessionNote);
            }
        }
        return accessionNotes;
    }

    private boolean hasValidationNotesFor(String encounterUuid, Encounter encounter) {
        Set<Obs> observations = encounter.getAllObs();
        for (Obs observation : observations) {
            if (encounterUuid.equals(observation.getValueText())) return true;
        }
        return false;
    }

    List<EncounterTransaction.Order> filterTestOrders(EncounterTransaction encounterTransaction, Encounter encounter, Map<String, Encounter> encounterTestOrderUuidMap, Collection<String> concepts, Date startDate, Date endDate) {
        List<EncounterTransaction.Order> orders = new ArrayList<>();
        for (EncounterTransaction.Order order : encounterTransaction.getOrders()) {
            boolean conceptFilter = (concepts == null) || concepts.contains(order.getConcept().getName());
            if ((conceptFilter && LAB_ORDER_TYPE.equals(order.getOrderType())) && !((startDate != null && order.getDateCreated().before(startDate))
                    || (endDate != null && order.getDateCreated().after(endDate)))) {
                encounterTestOrderUuidMap.put(order.getUuid(), encounter);
                orders.add(order);
            }
        }
        // Hack to hundle OpenMRS Test Orders from the TestOrder domain as Orders since the emrapi's OrderMapper does not hundle them
//        if (orders.isEmpty()) {
//            for (Order order : encounter.getOrders()) {
//                order = HibernateUtil.getRealObjectFromProxy(order);
//                boolean conceptFilter = (concepts == null) || concepts.contains(order.getConcept().getName().getName());
//                if (TestOrder.class.equals(order.getClass()) && ((conceptFilter && LAB_ORDER_TYPE.equals(order.getOrderType().getName())) && !((startDate != null && order.getDateCreated().before(startDate))
//                        || (endDate != null && order.getDateCreated().after(endDate))))) {
//                	encounterTestOrderUuidMap.put(order.getUuid(), encounter);
//                	orders.add(orderMapper.mapOrder(order));
//                }
//            }
//        }
        return orders;
    }

    private List<EncounterTransaction.Observation> filterObservations(List<EncounterTransaction.Observation> observations, Date startDate, Date endDate) {
        List<EncounterTransaction.Observation> filteredObservations = new ArrayList<>();
        for (EncounterTransaction.Observation observation : observations) {
            if (!observation.getVoided() && !((startDate != null && observation.getObservationDateTime().before(startDate))
                    || (endDate != null && observation.getObservationDateTime().after(endDate)))) {
                filteredObservations.add(observation);
            }
        }
        return filteredObservations;
    }

    private void mapObservationsWithEncounter(List<EncounterTransaction.Observation> observations, Encounter encounter, Map<String, Encounter> encounterObservationMap) {
        for (EncounterTransaction.Observation observation : observations) {
            encounterObservationMap.put(observation.getUuid(), encounter);
            if (observation.getGroupMembers().size() > 0) {
                mapObservationsWithEncounter(observation.getGroupMembers(), encounter, encounterObservationMap);
            }
        }
    }

    List<LabOrderResult> mapOrdersWithObs(List<EncounterTransaction.Order> testOrders, List<EncounterTransaction.Observation> observations, Map<String, Encounter> encounterTestOrderMap, Map<String, Encounter> encounterObservationMap, Map<String, List<AccessionNote>> encounterToAccessionNotesMap) {
        List<LabOrderResult> labOrderResults = new ArrayList<>();
        for (EncounterTransaction.Order testOrder : testOrders) {
            List<EncounterTransaction.Observation> obsGroups = findObsGroup(observations, testOrder);
            if (!obsGroups.isEmpty()) {
                for (EncounterTransaction.Observation obsGroup : obsGroups) {
                    labOrderResults.addAll(mapObs(obsGroup, testOrder, encounterTestOrderMap, encounterObservationMap, encounterToAccessionNotesMap));
                }
            } else if (testOrder.getDateStopped() == null) {
                EncounterTransaction.Concept orderConcept = testOrder.getConcept();
                Encounter orderEncounter = encounterTestOrderMap.get(testOrder.getUuid());
                LabOrderResult labOrderResult = new LabOrderResult(testOrder.getUuid(), testOrder.getAction(), orderEncounter.getUuid(), orderEncounter.getEncounterDatetime(), orderConcept.getName(), orderConcept.getUnits(), null, null, null, null, false, null, null);
                if(testOrder.getConcept().getShortName() != null) {
                    labOrderResult.setPreferredTestName(testOrder.getConcept().getShortName());
                }
                labOrderResult.setVisitStartTime(orderEncounter.getVisit().getStartDatetime());
                labOrderResults.add(labOrderResult);
            }
        }
        return labOrderResults;
    }

    private List<LabOrderResult> mapObs(EncounterTransaction.Observation obsGroup, EncounterTransaction.Order testOrder, Map<String, Encounter> encounterTestOrderMap, Map<String, Encounter> encounterObservationMap, Map<String, List<AccessionNote>> encounterToAccessionNotesMap) {
        List<LabOrderResult> labOrderResults = new ArrayList<>();
        if (isPanel(obsGroup)) {
            for (EncounterTransaction.Observation observation : obsGroup.getGroupMembers()) {
                LabOrderResult order = createLabOrderResult(observation, testOrder, encounterTestOrderMap, encounterObservationMap, encounterToAccessionNotesMap);
                order.setPanelUuid(obsGroup.getConceptUuid());
                if(obsGroup.getConcept().getShortName() != null) {
                    order.setPanelName(obsGroup.getConcept().getShortName());
                }else{
                    order.setPanelName(obsGroup.getConcept().getName());
                }
                labOrderResults.add(order);
            }
        } else {
            labOrderResults.add(createLabOrderResult(obsGroup, testOrder, encounterTestOrderMap, encounterObservationMap, encounterToAccessionNotesMap));
        }
        return labOrderResults;
    }

    private boolean isPanel(EncounterTransaction.Observation obsGroup) {
        return obsGroup.getConcept().isSet();
    }

    private LabOrderResult createLabOrderResult(EncounterTransaction.Observation observation, EncounterTransaction.Order testOrder, Map<String, Encounter> encounterTestOrderMap, Map<String, Encounter> encounterObservationMap, Map<String, List<AccessionNote>> encounterToAccessionNotesMap) {
        LabOrderResult labOrderResult = new LabOrderResult();
        Encounter orderEncounter = encounterTestOrderMap.get(observation.getOrderUuid());
        Object resultValue = getValue(observation, observation.getConcept().getName());
        String notes = (String) getValue(observation, LAB_NOTES);
        String uploadedFileName = (String) getValue(observation, LAB_REPORT);
        labOrderResult.setAccessionUuid(orderEncounter.getUuid());
        labOrderResult.setAccessionDateTime(orderEncounter.getEncounterDatetime());
        labOrderResult.setProvider(getProviderName(observation, encounterObservationMap));
        labOrderResult.setResultDateTime(observation.getObservationDateTime());
        labOrderResult.setTestUuid(observation.getConceptUuid());
        labOrderResult.setTestName(observation.getConcept().getName());
        labOrderResult.setResult(resultValue != null ? resultValue.toString() : null);
        labOrderResult.setAbnormal((Boolean) getValue(observation, LAB_ABNORMAL));
        labOrderResult.setMinNormal((Double) getValue(observation, LAB_MINNORMAL));
        labOrderResult.setMaxNormal((Double) getValue(observation, LAB_MAXNORMAL));
        labOrderResult.setNotes(notes != null && notes.trim().length() > 1 ? notes.trim() : null);
        labOrderResult.setReferredOut(getLeafObservation(observation, REFERRED_OUT) != null);
        labOrderResult.setTestUnitOfMeasurement(observation.getConcept().getUnits());
        labOrderResult.setUploadedFileName(uploadedFileName != null && uploadedFileName.trim().length() > 0 ? uploadedFileName.trim() : null);
        labOrderResult.setVisitStartTime(orderEncounter.getVisit().getStartDatetime());
        labOrderResult.setAccessionNotes(encounterToAccessionNotesMap.get(orderEncounter.getUuid()));
        labOrderResult.setAction(testOrder.getAction());
        labOrderResult.setOrderUuid(testOrder.getUuid());
        return labOrderResult;
    }

    private String getProviderName(EncounterTransaction.Observation observation, Map<String, Encounter> encounterObservationMap) {
        Encounter obsEncounter = encounterObservationMap.get(observation.getUuid());
        ArrayList<EncounterProvider> encounterProviders = new ArrayList<>(obsEncounter.getEncounterProviders());
        return encounterProviders.size() > 0 ? encounterProviders.get(0).getProvider().getName() : null;
    }

    private Object getValue(EncounterTransaction.Observation observation, String conceptName) {
        EncounterTransaction.Observation leafObservation = getLeafObservation(observation, conceptName);
        if (leafObservation != null) {
            Object value = leafObservation.getValue();
            return (value instanceof EncounterTransaction.Concept) ? ((EncounterTransaction.Concept) value).getName() : value;
        }
        return null;
    }

    private EncounterTransaction.Observation getLeafObservation(EncounterTransaction.Observation observation, String conceptName) {
        for (EncounterTransaction.Observation childObs : observation.getGroupMembers()) {
            if (!childObs.getGroupMembers().isEmpty()) {
                return getLeafObservation(childObs, conceptName);
            }
            if (childObs.getConcept().getName().equalsIgnoreCase(conceptName)) {
                return childObs;
            }
        }
        return null;
    }

    private List<EncounterTransaction.Observation> findObsGroup(List<EncounterTransaction.Observation> observations, EncounterTransaction.Order testOrder) {
        List<EncounterTransaction.Observation> obsGroups = new ArrayList<>();
        for (EncounterTransaction.Observation observation : observations) {
            if (observation.getOrderUuid() != null && observation.getOrderUuid().equals(testOrder.getUuid())) {
                obsGroups.add(observation);
            }
        }
        return obsGroups;
    }
}
