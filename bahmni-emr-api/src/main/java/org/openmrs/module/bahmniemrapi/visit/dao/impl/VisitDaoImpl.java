package org.openmrs.module.bahmniemrapi.visit.dao.impl;

import org.hibernate.Query;
import org.hibernate.SessionFactory;
import org.openmrs.Visit;
import org.openmrs.module.bahmniemrapi.visit.dao.VisitDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

@Repository
public class VisitDaoImpl implements VisitDao {

    @Autowired
    private SessionFactory sessionFactory;

    @Override
    public Visit getLatestVisit(String patientUuid, String conceptName) {
        String queryString = "select v\n" +
                "from Obs obs join obs.encounter enc join enc.visit v, ConceptName cn \n" +
                "where cn.concept.conceptId = obs.concept.conceptId and cn.name=:conceptName and cn.conceptNameType='FULLY_SPECIFIED' and obs.person.uuid=:patientUuid\n" +
                "order by v.visitId desc";
        Query queryToGetVisitId = sessionFactory.getCurrentSession().createQuery(queryString);
        queryToGetVisitId.setString("conceptName", conceptName);
        queryToGetVisitId.setString("patientUuid", patientUuid);
        queryToGetVisitId.setMaxResults(1);

        return (Visit) queryToGetVisitId.uniqueResult();
    }

    @Override
    public List<Visit> getVisitOn(Date visitDate) {
        String queryString = "select v " +
                "from Visit v where " +
                " year(v.startDatetime)<=year(:visitDate) and month(v.startDatetime)<=month(:visitDate) and day(v.startDatetime) <= day(:visitDate) and " +
                " v.stopDatetime is not null and " +
                " year(v.stopDatetime)>=year(:visitDate) and month(v.stopDatetime)>=month(:visitDate) and day(v.stopDatetime) >= day(:visitDate) ";
        Query query = sessionFactory.getCurrentSession().createQuery(queryString);
        query.setDate("visitDate", visitDate);
        return query.list();
    }
}
