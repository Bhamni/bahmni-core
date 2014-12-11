package org.bahmni.module.bahmnicore.dao.impl;

import org.bahmni.module.bahmnicore.dao.OrderDao;
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
import org.openmrs.Patient;
import org.openmrs.Visit;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class OrderDaoImpl implements OrderDao {
    @Autowired
    private SessionFactory sessionFactory;

    @Override
    public List<Order> getCompletedOrdersFrom(List<Order> allOrders) {
        Criteria criteria = sessionFactory.getCurrentSession().createCriteria(Obs.class);
        criteria.setProjection(Projections.groupProperty("order"));

        criteria.add(Restrictions.in("order", allOrders));
        return criteria.list();
    }

    @Override
    public List<DrugOrder> getPrescribedDrugOrders(Patient patient, Boolean includeActiveVisit, Integer numberOfVisits) {
        Session currentSession = sessionFactory.getCurrentSession();
        List<Integer> visitWithDrugOrderIds = getVisitIds(getVisitsWithOrders(patient, "DrugOrder", includeActiveVisit, numberOfVisits));
        if(!visitWithDrugOrderIds.isEmpty()) {
            Query query = currentSession.createQuery("select d1 from DrugOrder d1, Encounter e, Visit v where d1.encounter = e and e.visit = v and v.visitId in (:visitIds) " +
                    "and d1.voided = false and d1.action != :discontinued and " +
                    "not exists (select d2 from DrugOrder d2 where d2.voided = false and d2.action = :revised and d2.encounter = d1.encounter and d2.previousOrder = d1)" +
                    "order by d1.dateActivated desc");
            query.setParameterList("visitIds", visitWithDrugOrderIds);
            query.setParameter("discontinued", Order.Action.DISCONTINUE);
            query.setParameter("revised", Order.Action.REVISE);
            return (List<DrugOrder>) query.list();
        }
        return new ArrayList<>();
    }

    public List<DrugOrder> getPrescribedDrugOrdersForConcepts(Patient patient, Boolean includeActiveVisit, Integer numberOfVisits, List<Concept> concepts){
        Session currentSession = sessionFactory.getCurrentSession();
        List<Integer> visitWithDrugOrderIds = getVisitIds(getVisitsWithOrders(patient, "DrugOrder", includeActiveVisit, numberOfVisits));
        if(!visitWithDrugOrderIds.isEmpty()) {

            Query query = currentSession.createQuery("select d1 from DrugOrder d1, Encounter e, Visit v where d1.encounter = e and e.visit = v and v.visitId in (:visitIds) and d1.concept in (:concepts)" +
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

    public List<Visit> getVisitsWithOrders(Patient patient, String orderType, Boolean includeActiveVisit, Integer numberOfVisits) {
        Session currentSession = sessionFactory.getCurrentSession();
        String includevisit = includeActiveVisit == null || includeActiveVisit == false ? "and v.stopDatetime is not null and v.stopDatetime < :now" : "";
        Query queryVisitsWithDrugOrders = currentSession.createQuery("select v from "  + orderType + " o, Encounter e, Visit v where o.encounter = e.encounterId and e.visit = v.visitId and v.patient = (:patientId) " +
                "and o.voided = false and o.action != :discontinued " +  includevisit + " group by v.visitId order by v.startDatetime desc");
        queryVisitsWithDrugOrders.setParameter("patientId", patient);
        queryVisitsWithDrugOrders.setParameter("discontinued", Order.Action.DISCONTINUE);
        if(includeActiveVisit == null || includeActiveVisit == false) {
            queryVisitsWithDrugOrders.setParameter("now", new Date());
        }
        if(numberOfVisits != null ) {
            queryVisitsWithDrugOrders.setMaxResults(numberOfVisits);
        }
        return (List<Visit>) queryVisitsWithDrugOrders.list();
    }

    @Override
    public List<Visit> getVisitsWithOrdersForConcepts(Patient patient, String orderType, Boolean includeActiveVisit, Integer numberOfVisits,List<Concept> concepts) {
        Session currentSession = sessionFactory.getCurrentSession();
        String includevisit = includeActiveVisit == null || includeActiveVisit == false ? "and v.stopDatetime is not null and v.stopDatetime < :now" : "";
        Query queryVisitsWithOrders = currentSession.createQuery("select v from "  + orderType + " o, Encounter e, Visit v where o.encounter = e.encounterId and e.visit = v.visitId and v.patient = (:patientId) " +
                "and o.voided = false and o.concept in (:concepts) and o.action != :discontinued " +  includevisit + " group by v.visitId order by v.startDatetime desc");
        queryVisitsWithOrders.setParameter("patientId", patient);
        queryVisitsWithOrders.setParameter("discontinued", Order.Action.DISCONTINUE);
        queryVisitsWithOrders.setParameterList("concepts", concepts);
        if(includeActiveVisit == null || includeActiveVisit == false) {
            queryVisitsWithOrders.setParameter("now", new Date());
        }
        if(numberOfVisits != null ) {
            queryVisitsWithOrders.setMaxResults(numberOfVisits);
        }
        return (List<Visit>) queryVisitsWithOrders.list();
    }
    private List<Integer> getVisitIds(List<Visit> visits) {
        List<Integer> visitIds = new ArrayList<>();
        for (Visit visit : visits) {
            visitIds.add(visit.getId());
        }
        return visitIds;
    }
}
