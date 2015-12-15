package org.bahmni.module.bahmnicore.dao.impl;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.apache.log4j.Logger;
import org.bahmni.module.bahmnicore.contract.orderTemplate.OrderTemplateJson;
import org.bahmni.module.bahmnicore.dao.OrderDao;
import org.codehaus.jackson.map.ObjectMapper;
import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.SessionFactory;
import org.hibernate.classic.Session;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.openmrs.Concept;
import org.openmrs.DrugOrder;
import org.openmrs.Obs;
import org.openmrs.Order;
import org.openmrs.OrderType;
import org.openmrs.Patient;
import org.openmrs.Visit;
import org.openmrs.module.emrapi.CareSettingType;
import org.openmrs.module.emrapi.encounter.domain.EncounterTransaction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.util.*;

@Component
public class OrderDaoImpl implements OrderDao {
    private static final String ORDER_TEMPLATES_DIRECTORY = "ordertemplates";
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final Logger log = Logger.getLogger(OrderDaoImpl.class);

    private SessionFactory sessionFactory;
    private ApplicationDataDirectory applicationDataDirectory;
    private String TEMPLATES_JSON_FILE = "templates.json";
    private String FILE_SEPARATOR = "/";


    @Autowired
    public OrderDaoImpl(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
        this.applicationDataDirectory = new ApplicationDataDirectoryImpl();
    }

    @Override
    public List<Order> getCompletedOrdersFrom(List<Order> allOrders) {
        Criteria criteria = getCurrentSession().createCriteria(Obs.class);
        criteria.setProjection(Projections.groupProperty("order"));

        criteria.add(Restrictions.in("order", allOrders));
        return criteria.list();
    }

    @Override
    public List<DrugOrder> getPrescribedDrugOrders(Patient patient, Boolean includeActiveVisit, Integer numberOfVisits, Date startDate, Date endDate) {
        Session currentSession = getCurrentSession();
        List<Integer> visitWithDrugOrderIds = getVisitIds(getVisitsWithActiveOrders(patient, "DrugOrder", includeActiveVisit, numberOfVisits));
        if (visitWithDrugOrderIds.isEmpty()) {
            return new ArrayList<>();
        }
        StringBuilder queryString = new StringBuilder("select d1 " +
            "from DrugOrder d1, Encounter e, Visit v " +
            "where d1.encounter = e and e.visit = v and v.visitId in (:visitIds) " +
            "and d1.voided = false and d1.action != :discontinued " +
            "and not exists " +
            "(select d2 from DrugOrder d2 where d2.voided = false and d2.action = :revised and d2.encounter = d1.encounter and d2.previousOrder = d1)");

        if (startDate != null) {
            queryString.append(" and d1.dateActivated >= :startDate ");
        }
        if (endDate != null) {
            queryString.append(" and d1.dateActivated <= :endDate ");
        }
        queryString.append(" order by d1.dateActivated desc");

        Query query = currentSession.createQuery(queryString.toString());
        query.setParameterList("visitIds", visitWithDrugOrderIds);
        query.setParameter("discontinued", Order.Action.DISCONTINUE);
        query.setParameter("revised", Order.Action.REVISE);
        if (startDate != null) query.setParameter("startDate", startDate);
        if (endDate != null) query.setParameter("endDate", endDate);
        return query.list();
    }

    @Override
    public List<DrugOrder> getPrescribedDrugOrders(List<String> visitUuids) {
        if (visitUuids != null && visitUuids.size() != 0) {
            Session currentSession = getCurrentSession();
            Query query = currentSession.createQuery("select d1 from DrugOrder d1, Encounter e, Visit v where d1.encounter = e and e.visit = v and v.uuid in (:visitUuids) " +
                "and d1.voided = false and d1.action != :discontinued and " +
                "not exists (select d2 from DrugOrder d2 where d2.voided = false and d2.action = :revised and d2.encounter = d1.encounter and d2.previousOrder = d1)" +
                "order by d1.dateActivated desc");
            query.setParameterList("visitUuids", visitUuids);
            query.setParameter("discontinued", Order.Action.DISCONTINUE);
            query.setParameter("revised", Order.Action.REVISE);
            return (List<DrugOrder>) query.list();
        }
        return new ArrayList<>();
    }

    @Override
    public List<DrugOrder> getPrescribedDrugOrdersForConcepts(Patient patient, Boolean includeActiveVisit, List<Visit> visits, List<Concept> concepts) {
        Session currentSession = getCurrentSession();
        List<Integer> visitWithDrugOrderIds = getVisitIds(visits);
        if (!visitWithDrugOrderIds.isEmpty()) {

            Query query = currentSession.createQuery("select d1 from DrugOrder d1, Encounter e, Visit v where d1.encounter = e and e.visit = v and v.visitId in (:visitIds) and d1.drug.concept in (:concepts)" +
                "and d1.voided = false and d1.action != :discontinued and " +
                "not exists (select d2 from DrugOrder d2 where d2.voided = false and d2.action = :revised and d2.encounter = d1.encounter and d2.previousOrder = d1)" +
                "order by d1.dateActivated desc");
            query.setParameterList("visitIds", visitWithDrugOrderIds);
            query.setParameterList("concepts", concepts);
            query.setParameter("discontinued", Order.Action.DISCONTINUE);
            query.setParameter("revised", Order.Action.REVISE);
            return (List<DrugOrder>) query.list();
        }
        return new ArrayList<>();
    }

    @Override
    public Collection<EncounterTransaction.DrugOrder> getDrugOrderForRegimen(String regimenName) {
        File file = getTemplates();
        OrderTemplateJson orderTemplates = null;
        try {
            orderTemplates = OBJECT_MAPPER.readValue(file, OrderTemplateJson.class);
            setDefaultFields(orderTemplates);
        } catch (IOException e) {
            log.error("Could not deserialize file " + file.getAbsolutePath());
            throw new RuntimeException(e);
        }
        for (OrderTemplateJson.OrderTemplate orderTemplate : orderTemplates.getOrderTemplates()) {
            if (orderTemplate.getName().equals(regimenName)) {
                return orderTemplate.getDrugOrders();
            }
        }
        return new ArrayList<>();
    }

    private void setDefaultFields(OrderTemplateJson orderTemplateJson) {
        for (OrderTemplateJson.OrderTemplate orderTemplate : orderTemplateJson.getOrderTemplates()) {
            for (EncounterTransaction.DrugOrder drugOrder : orderTemplate.getDrugOrders()) {
                drugOrder.setCareSetting(CareSettingType.OUTPATIENT);
                drugOrder.setOrderType("Drug Order");
                drugOrder.setDosingInstructionType("org.openmrs.module.bahmniemrapi.drugorder.dosinginstructions.FlexibleDosingInstructions");
                drugOrder.getDosingInstructions().setAsNeeded(false);
            }
        }
    }

    private File getTemplates() {
        return applicationDataDirectory.getFile(ORDER_TEMPLATES_DIRECTORY + FILE_SEPARATOR + TEMPLATES_JSON_FILE);
    }

    public List<Visit> getVisitsWithActiveOrders(Patient patient, String orderType, Boolean includeActiveVisit, Integer numberOfVisits) {
        Session currentSession = getCurrentSession();
        String includevisit = includeActiveVisit == null || includeActiveVisit == false ? "and v.stopDatetime is not null and v.stopDatetime < :now" : "";
        Query queryVisitsWithDrugOrders = currentSession.createQuery("select v from " + orderType + " o, Encounter e, Visit v where o.encounter = e.encounterId and e.visit = v.visitId and v.patient = (:patientId) " +
            "and o.voided = false and o.dateStopped = null and o.action != :discontinued " + includevisit + " group by v.visitId order by v.startDatetime desc");
        queryVisitsWithDrugOrders.setParameter("patientId", patient);
        queryVisitsWithDrugOrders.setParameter("discontinued", Order.Action.DISCONTINUE);
        if (includeActiveVisit == null || includeActiveVisit == false) {
            queryVisitsWithDrugOrders.setParameter("now", new Date());
        }
        if (numberOfVisits != null) {
            queryVisitsWithDrugOrders.setMaxResults(numberOfVisits);
        }
        return (List<Visit>) queryVisitsWithDrugOrders.list();
    }

    public List<Visit> getVisitsWithAllOrders(Patient patient, String orderType, Boolean includeActiveVisit, Integer numberOfVisits) {
        Session currentSession = getCurrentSession();
        String includevisit = includeActiveVisit == null || includeActiveVisit == false ? "and v.stopDatetime is not null and v.stopDatetime < :now" : "";
        Query queryVisitsWithDrugOrders = currentSession.createQuery("select v from " + orderType + " o, Encounter e, Visit v where o.encounter = e.encounterId and e.visit = v.visitId and v.patient = (:patientId) " +
            "and o.voided = false and o.dateStopped = null " + includevisit + " group by v.visitId order by v.startDatetime desc");
        queryVisitsWithDrugOrders.setParameter("patientId", patient);
        if (includeActiveVisit == null || includeActiveVisit == false) {
            queryVisitsWithDrugOrders.setParameter("now", new Date());
        }
        if (numberOfVisits != null) {
            queryVisitsWithDrugOrders.setMaxResults(numberOfVisits);
        }
        return (List<Visit>) queryVisitsWithDrugOrders.list();
    }

    void setApplicationDataDirectory(ApplicationDataDirectory applicationDataDirectory) {
        this.applicationDataDirectory = applicationDataDirectory;
    }

    @Override
    public List<Visit> getVisitsForUUids(String[] visitUuids) {
        return getCurrentSession()
            .createQuery("from Visit v where v.uuid in (:visitUuids)")
            .setParameterList("visitUuids", visitUuids)
            .list();
    }

    private Session getCurrentSession() {
        return sessionFactory.getCurrentSession();
    }

    private List<Integer> getVisitIds(List<Visit> visits) {
        List<Integer> visitIds = new ArrayList<>();
        for (Visit visit : visits) {
            visitIds.add(visit.getId());
        }
        return visitIds;
    }

    @Override
    public List<Order> getAllOrders(Patient patient, List<OrderType> orderTypes, Integer offset, Integer limit) {
        Criteria criteria = sessionFactory.getCurrentSession().createCriteria(Order.class);
        criteria.add(Restrictions.eq("patient", patient));

        if (orderTypes != null && orderTypes.size() > 0) {
            criteria.add(Restrictions.in("orderType", orderTypes));
        }

        criteria.add(Restrictions.eq("voided", false));
        criteria.add(Restrictions.isNull("dateStopped"));
        criteria.add(Restrictions.ne("action", Order.Action.DISCONTINUE));
        criteria.addOrder(org.hibernate.criterion.Order.desc("dateCreated"));
        if (offset != null) {
            criteria.setFirstResult(offset);
        }

        if (limit != null && limit > 0) {
            criteria.setMaxResults(limit);
        }

        return criteria.list();
    }

    @Override
    public List<Order> getAllOrdersForVisits(OrderType orderType, List<Visit> visits) {
        if (visits == null || visits.isEmpty()) {
            return new ArrayList<>();
        }
        Session currentSession = getCurrentSession();
        Query queryVisitsWithDrugOrders = currentSession.createQuery(" select o from Order o where o.encounter.encounterId in\n" +
            "(select e.encounterId from Encounter e where e.visit in (:visits) group by e.visit.visitId )\n" +
            "and o.dateStopped = null and o.voided = false and o.orderType = (:orderTypeId) " +
            "and o.action != :discontinued order by o.dateActivated desc");
        queryVisitsWithDrugOrders.setParameter("discontinued", Order.Action.DISCONTINUE);
        queryVisitsWithDrugOrders.setParameter("orderTypeId", orderType);
        queryVisitsWithDrugOrders.setParameterList("visits", visits);
        return (List<Order>) queryVisitsWithDrugOrders.list();
    }

    @Override
    public Order getOrderByUuid(String uuid) {
        String queryString = "select o from Order o where o.uuid = (:uuid)";
        Query queryToGetVisitId = sessionFactory.getCurrentSession().createQuery(queryString);
        queryToGetVisitId.setString("uuid", uuid);
        queryToGetVisitId.setMaxResults(1);
        return (Order) queryToGetVisitId.uniqueResult();
    }

    @Override
    public List<Order> getOrdersForVisitUuid(String visitUuid, String orderTypeUuid) {
        Session currentSession = getCurrentSession();
        Query queryVisitsWithDrugOrders = currentSession.createQuery(" select o from Order o where o.encounter.encounterId in\n" +
            "(select e.encounterId from Encounter e where e.visit.uuid =:visitUuid)\n" +
            "and o.voided = false and o.dateStopped = null and o.orderType.uuid = (:orderTypeUuid) and  o.action != :discontinued order by o.dateActivated desc");
        queryVisitsWithDrugOrders.setParameter("orderTypeUuid", orderTypeUuid);
        queryVisitsWithDrugOrders.setParameter("discontinued", Order.Action.DISCONTINUE);
        queryVisitsWithDrugOrders.setParameter("visitUuid", visitUuid);
        return (List<Order>) queryVisitsWithDrugOrders.list();
    }

    @Override
    public List<Order> getAllOrders(Patient patientByUuid, OrderType drugOrderType, Set<Concept> conceptsForDrugs) {
        Criteria criteria = sessionFactory.getCurrentSession().createCriteria(Order.class);
        criteria.add(Restrictions.eq("patient", patientByUuid));
        if (CollectionUtils.isNotEmpty(conceptsForDrugs)) {
            criteria.add(Restrictions.in("concept", conceptsForDrugs));
        }
        criteria.add(Restrictions.eq("orderType", drugOrderType));
        criteria.add(Restrictions.eq("voided", false));
        criteria.add(Restrictions.ne("action", Order.Action.DISCONTINUE));
        criteria.addOrder(org.hibernate.criterion.Order.asc("orderId"));

        return criteria.list();

    }

    @Override
    public Map<String, DrugOrder> getDiscontinuedDrugOrders(List<DrugOrder> drugOrders) {

        if (drugOrders == null || drugOrders.size() == 0)
            return new HashMap<>();

        Session currentSession = getCurrentSession();

        Query query = currentSession.createQuery("select d1 from DrugOrder d1 where d1.action = :discontinued and  d1.previousOrder in :drugOrderList");
        query.setParameter("discontinued", Order.Action.DISCONTINUE);
        query.setParameterList("drugOrderList", drugOrders);
        List<DrugOrder> discontinuedDrugOrders = query.list();

        Map<String, DrugOrder> discontinuedDrugOrderMap = new HashMap<>();
        for (DrugOrder discontinuedDrugOrder : discontinuedDrugOrders) {
            discontinuedDrugOrderMap.put(discontinuedDrugOrder.getPreviousOrder().getOrderNumber(), discontinuedDrugOrder);
        }

        return discontinuedDrugOrderMap;
    }
}
