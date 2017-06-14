package org.bahmni.module.bahmnicore.dao.impl;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.bahmni.module.bahmnicore.dao.ObsDao;
import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Restrictions;
import org.openmrs.Concept;
import org.openmrs.ConceptDatatype;
import org.openmrs.Encounter;
import org.openmrs.Obs;
import org.openmrs.Order;
import org.openmrs.Person;
import org.openmrs.api.ConceptNameType;
import org.openmrs.api.context.Context;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class ObsDaoImpl implements ObsDao {

    public static final String COMMA = ",";
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
                " and cn.locale = :locale " +
                " and cn.conceptNameType = :conceptNameType " +
                " and cn.voided = false and obs.voided = false ");

        if (CollectionUtils.isNotEmpty(listOfVisitIds)) {
            query.append(" and obs.encounter.visit.visitId in (:listOfVisitIds) ");
        }
        if (startDate != null) {
            query.append(" and obs.obsDatetime >= :startDate ");
        }
        if (endDate != null) {
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
        queryToGetObservations.setString("locale", Context.getLocale().getLanguage());
        if (null != obsIgnoreList && obsIgnoreList.size() > 0) {
            queryToGetObservations.setParameterList("obsIgnoreList", obsIgnoreList);
        }
        if (null != listOfVisitIds && listOfVisitIds.size() > 0) {
            queryToGetObservations.setParameterList("listOfVisitIds", listOfVisitIds);
        }
        if (null != order) {
            queryToGetObservations.setParameter("order", order);
        }
        if (startDate != null) {
            queryToGetObservations.setParameter("startDate", startDate);
        }
        if (endDate != null) {
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
                        " and cn.locale = :locale " +
                        " and cn.conceptNameType = :conceptNameType " +
                        " and cn.voided = false " +
                        " and obs.voided = false" +
                        " order by obs.obsDatetime desc ");

        queryToGetObservations.setMaxResults(limit);
        queryToGetObservations.setString("patientUuid", patientUuid);
        queryToGetObservations.setParameter("conceptName", conceptName);
        queryToGetObservations.setParameter("conceptNameType", ConceptNameType.FULLY_SPECIFIED);
        queryToGetObservations.setString("locale", Context.getLocale().getLanguage());
        
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
                        "     where cs.conceptSet.conceptId = cn.concept.conceptId and cn.conceptNameType='FULLY_SPECIFIED' " +
                        "	  and cn.locale = :locale and cn.name=:conceptName)\n" +
                        "   and obs.person.uuid=:patientUuid and v.visitId =:visitId order by enc.encounterId desc";
        Query queryToGetObs = sessionFactory.getCurrentSession().createQuery(queryString);
        queryToGetObs.setString("conceptName", conceptName);
        queryToGetObs.setString("patientUuid", patientUuid);
        queryToGetObs.setInteger("visitId", visitId);
        queryToGetObs.setString("locale", Context.getLocale().getLanguage());
        
        return queryToGetObs.list();
    }

    @Override
    public List<Obs> getObsForConceptsByEncounter(String encounterUuid, List<String> conceptNames) {
        if (encounterUuid == null) return new ArrayList<>();
        
        String queryString =
                "select obs\n" +
                        "from Obs obs, ConceptName cn \n" +
                        "where obs.voided = false and obs.encounter.uuid =:encounterUuid " +
                        "and obs.concept.conceptId = cn.concept.conceptId  " +
                        "and cn.name in (:conceptNames) " +
                        "and cn.locale = :locale " +
                        "and cn.conceptNameType='FULLY_SPECIFIED'";
        Query queryToGetObs = sessionFactory.getCurrentSession().createQuery(queryString);
        queryToGetObs.setParameterList("conceptNames", conceptNames);
        queryToGetObs.setString("encounterUuid", encounterUuid);
        queryToGetObs.setString("locale", Context.getLocale().getLanguage());

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
        if (CollectionUtils.isEmpty(encounters)) {
            return new ArrayList<>();
        } else {
            criteria.add(Restrictions.in("encounter", encounters));
        }
        if (CollectionUtils.isNotEmpty(persons)) {
            criteria.add(Restrictions.in("person", persons));
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
        criteria.add(Restrictions.eq("voided", Boolean.FALSE));

        criteria.addOrder(org.hibernate.criterion.Order.desc("obsDatetime"));

        return criteria.list();
    }

    @Override
    public List<Obs> getObsFor(String patientUuid, Concept rootConcept, Concept childConcept, List<Integer> listOfVisitIds, Collection<Encounter> encounters, Date startDate, Date endDate) {
        if (listOfVisitIds == null || listOfVisitIds.isEmpty())
            return new ArrayList<>();

        String encounterFilter = "";
        if (encounters != null && encounters.size() > 0) {
            encounterFilter = "AND encounter.encounter_id in (" + commaSeparatedEncounterIds(encounters) + ")";
        }
        StringBuilder queryString = new StringBuilder("SELECT rootObs.* " +
                "FROM obs rootObs " +
                "JOIN concept_name rootConceptName " +
                "ON rootObs.concept_id = rootConceptName.concept_id AND rootConceptName.name = :rootConceptName AND " +
                "rootConceptName.concept_name_type = 'FULLY_SPECIFIED' " +
                "JOIN person ON person.person_id = rootObs.person_id AND person.uuid = :patientUuid AND " +
                "rootObs.voided = 0 AND person.voided = 0 " +
                "JOIN encounter ON encounter.encounter_id = rootObs.encounter_id AND encounter.voided = 0 " +
                encounterFilter +
                "JOIN visit ON visit.visit_id = encounter.visit_id AND visit.visit_id IN :visitIds " +
                "JOIN obs groupByObs ON groupByObs.obs_group_id = rootObs.obs_id AND groupByObs.voided = 0 " +
                "JOIN concept_name groupByConceptName " +
                "ON groupByConceptName.concept_id = groupByObs.concept_id AND groupByConceptName.name = :childConceptName AND " +
                "groupByConceptName.concept_name_type = 'FULLY_SPECIFIED' ");

        if (startDate != null) queryString.append("where groupByObs.obs_datetime >= :startDate ");
        if (startDate != null && endDate != null) queryString.append("and groupByObs.obs_datetime <= :endDate ");
        queryString.append("group by groupByObs.obs_group_id order by obs_datetime asc ");

        Query queryToGetObs = sessionFactory.getCurrentSession()
                .createSQLQuery(queryString.toString()).addEntity(Obs.class);
        queryToGetObs.setParameter("rootConceptName", rootConcept.getName().getName());
        queryToGetObs.setParameter("patientUuid", patientUuid);
        queryToGetObs.setParameterList("visitIds", listOfVisitIds);
        queryToGetObs.setParameter("childConceptName", childConcept.getName().getName());
        if (startDate != null)
            queryToGetObs.setParameter("startDate", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(startDate));
        if (endDate != null)
            queryToGetObs.setParameter("endDate", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(endDate));


        return queryToGetObs.list();
    }

    private String commaSeparatedEncounterIds(Collection<Encounter> encounters) {
        ArrayList<String> encounterIds = new ArrayList<>();
        for (Encounter encounter : encounters) {
            encounterIds.add(encounter.getEncounterId().toString());
        }
        return StringUtils.join(encounterIds, COMMA);
    }

    @Override
    public Obs getChildObsFromParent(String parentObsUuid, Concept childConcept) {
        String queryString = "from Obs obs where obs.obsGroup.uuid = :parentObsUuid and obs.concept = :concept  and obs.voided = false order by obs.obsDatetime desc";
        Query queryToGetObs = sessionFactory.getCurrentSession().createQuery(queryString);
        queryToGetObs.setParameter("parentObsUuid", parentObsUuid);
        queryToGetObs.setParameter("concept", childConcept);
        List<Obs> obsList = queryToGetObs.list();
        if (obsList.size() > 0) {
            return (Obs) queryToGetObs.list().get(0);
        }
        return null;

    }

    @Override
    public List<Obs> getObsByPatientProgramUuidAndConceptNames(String patientProgramUuid, List<String> conceptNames, Integer limit, OrderBy sortOrder, Date startDate, Date endDate) {
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        StringBuilder queryString = new StringBuilder("SELECT o.* " +
                "FROM patient_program pp " +
                "INNER JOIN episode_patient_program epp " +
                "ON pp.patient_program_id = epp.patient_program_id\n " +
                "INNER JOIN episode_encounter ee " +
                "ON epp.episode_id = ee.episode_id\n " +
                "INNER JOIN obs o " +
                "ON o.encounter_id = ee.encounter_id\n " +
                "INNER JOIN concept_name cn on o.concept_id = cn.concept_id\n " +
                "WHERE pp.uuid = (:patientProgramUuid) " +
                "AND o.voided = false " +
                "AND cn.concept_name_type='FULLY_SPECIFIED' " +
                "AND cn.name IN (:conceptNames) " +
                "AND cn.locale = :locale");
        if(null != startDate) {
            queryString.append(" AND o.obs_datetime >= STR_TO_DATE(:startDate, '%Y-%m-%d')");
        }
        if(null != endDate) {
            queryString.append(" AND o.obs_datetime <= STR_TO_DATE(:endDate, '%Y-%m-%d')");
        }
        if (sortOrder == OrderBy.ASC) {
            queryString.append(" ORDER by o.obs_datetime asc");
        } else {
            queryString.append(" ORDER by o.obs_datetime desc");
        }
        if (limit != null) {
            queryString.append(" limit " + limit);
        }
        Query queryToGetObs = sessionFactory.getCurrentSession().createSQLQuery(queryString.toString()).addEntity(Obs.class);
        queryToGetObs.setParameterList("conceptNames", conceptNames);
        queryToGetObs.setString("patientProgramUuid", patientProgramUuid);
        queryToGetObs.setString("locale", Context.getLocale().getLanguage());
        if(null != startDate) {
            queryToGetObs.setString("startDate", dateFormat.format(startDate));
        }
        if(null != endDate) {
            queryToGetObs.setString("endDate", dateFormat.format(endDate));
        }

        return queryToGetObs.list();
    }
}
