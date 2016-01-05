package org.bahmni.module.bahmnicore.dao.impl;

import org.apache.commons.collections.CollectionUtils;
import org.bahmni.module.bahmnicore.dao.ObsDao;
import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Restrictions;
import org.openmrs.*;
import org.openmrs.api.ConceptNameType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.text.SimpleDateFormat;
import java.util.*;

@Repository
public class ObsDaoImpl implements ObsDao {

    @Autowired
    private SessionFactory sessionFactory;

    public enum OrderBy {ASC, DESC}

    @Override
    public List<Obs> getNumericObsByPerson(String personUUID) {
        Query query = sessionFactory.getCurrentSession().createQuery(
                "select obs from Obs as obs inner join fetch " +
                        "obs.concept as concept inner join fetch " +
                        "concept.datatype as datatype inner join " +
                        "obs.person as person " +
                        "where datatype.hl7Abbreviation = :hl7abrv  " +
                        "and person.uuid= :personUUID  " +
                        "and obs.voided = false ");
        query.setString("hl7abrv", ConceptDatatype.NUMERIC);
        query.setString("personUUID", personUUID);
        return query.list();

    }

    @Override
    public List<Concept> getNumericConceptsForPerson(String personUUID) {
        Query query = sessionFactory.getCurrentSession().createQuery(
                "select concept " +
                        "from Obs as obs " +
                        "inner join obs.concept as concept " +
                        "inner join concept.datatype as datatype " +
                        "inner join obs.person as person " +
                        "where datatype.hl7Abbreviation = :hl7abrv " +
                        "and person.uuid = :personUUID " +
                        "and obs.voided = false ");
        query.setString("hl7abrv", ConceptDatatype.NUMERIC);
        query.setString("personUUID", personUUID);
        return query.list();

    }

    public List<Obs> getObsByPatientAndVisit(String patientUuid, List<String> conceptNames, List<Integer> listOfVisitIds,
                     Integer limit, OrderBy sortOrder, List<String> obsIgnoreList, Boolean filterOutOrderObs, Order order, Date startDate, Date endDate) {

        StringBuilder query = new StringBuilder("select obs from Obs as obs, ConceptName as cn " +
                " where obs.person.uuid = :patientUuid " +
                " and cn.concept = obs.concept.conceptId " +
                " and cn.name in (:conceptNames) " +
                " and cn.conceptNameType = :conceptNameType " +
                " and cn.voided = false and obs.voided = false ");

        if(CollectionUtils.isNotEmpty(listOfVisitIds)){
            query.append(" and obs.encounter.visit.visitId in (:listOfVisitIds) ");
        }
        if(startDate != null){
            query.append(" and obs.obsDatetime >= :startDate ");
        }
        if(endDate != null){
            query.append(" and obs.obsDatetime <= :endDate ");
        }

        if (CollectionUtils.isNotEmpty(obsIgnoreList)) {
            query.append(" and cn.name not in (:obsIgnoreList) ");
        }
        if (filterOutOrderObs) {
            query.append(" and obs.order.orderId is null ");
        }
        if (null != order) {
            query.append(" and obs.order = (:order) ");
        }
        if (sortOrder == OrderBy.ASC) {
            query.append(" order by obs.obsDatetime asc ");
        } else {
            query.append(" order by obs.obsDatetime desc ");
        }


        Query queryToGetObservations = sessionFactory.getCurrentSession().createQuery(query.toString());
        queryToGetObservations.setMaxResults(limit);
        queryToGetObservations.setString("patientUuid", patientUuid);
        queryToGetObservations.setParameterList("conceptNames", conceptNames);
        queryToGetObservations.setParameter("conceptNameType", ConceptNameType.FULLY_SPECIFIED);
        if (null != obsIgnoreList && obsIgnoreList.size() > 0) {
            queryToGetObservations.setParameterList("obsIgnoreList", obsIgnoreList);
        }
        if (null != listOfVisitIds && listOfVisitIds.size() > 0 ) {
            queryToGetObservations.setParameterList("listOfVisitIds", listOfVisitIds);
        }
        if (null != order) {
            queryToGetObservations.setParameter("order", order);
        }
        if(startDate != null){
            queryToGetObservations.setParameter("startDate", startDate);
        }
        if (endDate != null){
            queryToGetObservations.setParameter("endDate", endDate);
        }
        return queryToGetObservations.list();
    }


    @Override
    public List<Obs> getLatestObsFor(String patientUuid, String conceptName, Integer limit) {
        Query queryToGetObservations = sessionFactory.getCurrentSession().createQuery(
                "select obs " +
                        " from Obs as obs, ConceptName as cn " +
                        " where obs.person.uuid = :patientUuid " +
                        " and cn.concept = obs.concept.conceptId " +
                        " and cn.name = (:conceptName) " +
                        " and cn.conceptNameType = :conceptNameType " +
                        " and cn.voided = false " +
                        " and obs.voided = false" +
                        " order by obs.obsDatetime desc ");

        queryToGetObservations.setMaxResults(limit);
        queryToGetObservations.setString("patientUuid", patientUuid);
        queryToGetObservations.setParameter("conceptName", conceptName);
        queryToGetObservations.setParameter("conceptNameType", ConceptNameType.FULLY_SPECIFIED);
        return queryToGetObservations.list();
    }

    @Override
    public List<Obs> getLatestObsForConceptSetByVisit(String patientUuid, String conceptName, Integer visitId) {
        if (visitId == null) return new ArrayList<>();

        String queryString =
                "select obs\n" +
                        "from Obs obs join obs.encounter enc join enc.visit v \n" +
                        "where obs.voided = false and obs.concept.conceptId in " +
                        "   ( select cs.concept.conceptId\n" +
                        "     from ConceptName cn, ConceptSet cs\n" +
                        "     where cs.conceptSet.conceptId = cn.concept.conceptId and cn.conceptNameType='FULLY_SPECIFIED' and cn.name=:conceptName)\n" +
                        "   and obs.person.uuid=:patientUuid and v.visitId =:visitId order by enc.encounterId desc";
        Query queryToGetObs = sessionFactory.getCurrentSession().createQuery(queryString);
        queryToGetObs.setString("conceptName", conceptName);
        queryToGetObs.setString("patientUuid", patientUuid);
        queryToGetObs.setInteger("visitId", visitId);

        return queryToGetObs.list();
    }

    @Override
    public List<Obs> getObsForOrder(String orderUuid) {
        String queryString = "from Obs obs where obs.voided = false and obs.order.uuid = :orderUuid order by obs.obsDatetime desc";
        Query queryToGetObs = sessionFactory.getCurrentSession().createQuery(queryString);
        queryToGetObs.setString("orderUuid", orderUuid);

        return queryToGetObs.list();
    }

    @Override
    public List<Obs> getObsForVisits(List<Person> persons, ArrayList<Encounter> encounters, List<Concept> conceptsForNames, Collection<Concept> obsIgnoreList, Boolean filterOutOrders, Order order) {
        Criteria criteria = sessionFactory.getCurrentSession().createCriteria(Obs.class, "obs");
        if (CollectionUtils.isNotEmpty(persons)) {
            criteria.add(Restrictions.in("person", persons));
        }

        if (CollectionUtils.isNotEmpty(encounters)) {
            criteria.add(Restrictions.in("encounter", encounters));
        }
        if (CollectionUtils.isNotEmpty(conceptsForNames)) {
            criteria.add(Restrictions.in("concept", conceptsForNames));
        }
        if (CollectionUtils.isNotEmpty(obsIgnoreList)) {
            criteria.add(Restrictions.not(Restrictions.in("concept", obsIgnoreList)));
        }
        if (filterOutOrders) {
            criteria.add(Restrictions.isNull("order"));
        }
        if (order != null) {
            criteria.add(Restrictions.eq("order", order));
        }
        criteria.add(Restrictions.eq("voided", Boolean.valueOf(false)));

        criteria.addOrder(org.hibernate.criterion.Order.desc("obsDatetime"));

        return criteria.list();
    }

    @Override
    public List<Obs> getObsFor(String patientUuid, Concept rootConcept, Concept childConcept, List<Integer> listOfVisitIds, Date startDate, Date endDate) {
        if (listOfVisitIds == null || listOfVisitIds.isEmpty())
            return new ArrayList<>();

        StringBuilder queryString = new StringBuilder("SELECT rootObs.* " +
                "FROM obs rootObs " +
                "JOIN concept_name rootConceptName " +
                "ON rootObs.concept_id = rootConceptName.concept_id AND rootConceptName.name = :rootConceptName AND " +
                "rootConceptName.concept_name_type = 'FULLY_SPECIFIED' " +
                "JOIN person ON person.person_id = rootObs.person_id AND person.uuid = :patientUuid AND " +
                "rootObs.voided = 0 AND person.voided = 0 " +
                "JOIN encounter ON encounter.encounter_id = rootObs.encounter_id AND encounter.voided = 0 " +
                "JOIN visit ON visit.visit_id = encounter.visit_id AND visit.visit_id IN :visitIds " +
                "JOIN obs groupByObs ON groupByObs.obs_group_id = rootObs.obs_id AND groupByObs.voided = 0 " +
                "JOIN concept_name groupByConceptName " +
                "ON groupByConceptName.concept_id = groupByObs.concept_id AND groupByConceptName.name = :childConceptName AND " +
                "groupByConceptName.concept_name_type = 'FULLY_SPECIFIED' ");

        if(startDate != null) queryString.append("where groupByObs.obs_datetime >= :startDate ");
        if(startDate != null && endDate != null) queryString.append("and groupByObs.obs_datetime <= :endDate ");
        queryString.append("group by groupByObs.obs_group_id order by obs_datetime asc ");

        Query queryToGetObs = sessionFactory.getCurrentSession()
                .createSQLQuery(queryString.toString()).addEntity(Obs.class);
        queryToGetObs.setParameter("rootConceptName", rootConcept.getName().getName());
        queryToGetObs.setParameter("patientUuid", patientUuid);
        queryToGetObs.setParameterList("visitIds", listOfVisitIds);
        queryToGetObs.setParameter("childConceptName", childConcept.getName().getName());
        if(startDate != null) queryToGetObs.setParameter("startDate", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(startDate));
        if(endDate != null) queryToGetObs.setParameter("endDate", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(endDate));
        return queryToGetObs.list();
    }
}
