package org.bahmni.module.bahmnicore.dao.impl;

import org.bahmni.module.bahmnicore.dao.BahmniProgramWorkflowDao;
import org.bahmni.module.bahmnicore.model.bahmniPatientProgram.BahmniPatientProgram;
import org.bahmni.module.bahmnicore.model.bahmniPatientProgram.PatientProgramAttribute;
import org.bahmni.module.bahmnicore.model.bahmniPatientProgram.PatientProgramAttributeType;
import org.hibernate.Query;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Restrictions;
import org.openmrs.api.db.hibernate.HibernateProgramWorkflowDAO;

import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class BahmniHibernateProgramWorkflowDaoImpl extends HibernateProgramWorkflowDAO implements BahmniProgramWorkflowDao {
    private SessionFactory sessionFactory;

    public void setSessionFactory(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<PatientProgramAttributeType> getAllPatientProgramAttributeTypes() {
        return sessionFactory.getCurrentSession().createCriteria(PatientProgramAttributeType.class).list();
    }

    @Override
    public PatientProgramAttributeType getPatientProgramAttributeType(Integer var1) {
        return (PatientProgramAttributeType) sessionFactory.getCurrentSession().get(PatientProgramAttributeType.class, var1);
    }

    @Override
    public PatientProgramAttributeType getPatientProgramAttributeTypeByUuid(String var1) {
        return (PatientProgramAttributeType) sessionFactory.getCurrentSession().createCriteria(PatientProgramAttributeType.class).add(Restrictions.eq("uuid", var1)).uniqueResult();
    }

    @Override
    public PatientProgramAttributeType savePatientProgramAttributeType(PatientProgramAttributeType patientProgramAttributeType) {
        sessionFactory.getCurrentSession().saveOrUpdate(patientProgramAttributeType);
        return patientProgramAttributeType;
    }

    @Override
    public PatientProgramAttributeType retirePatientProgramAttributeType(PatientProgramAttributeType var1, String var2) {
        return null;
    }

    @Override
    public PatientProgramAttributeType unretirePatientProgramAttributeType(PatientProgramAttributeType var1) {
        return null;
    }

    @Override
    public PatientProgramAttribute getPatientProgramAttributeByUuid(String var1) {
        return null;
    }

    @Override
    public void purgePatientProgramAttributeType(PatientProgramAttributeType var1) {

    }

    @Override
    public List<PatientProgramAttribute> getAttributesPatientProgramById(int id) {
        String queryString = "select * " +
                "from patient_program_attribute" +
                "where patient_program_id = " + id;
        Query query = sessionFactory.getCurrentSession().createQuery(queryString);
        List<PatientProgramAttribute> patientProgramAttributes = query.list();
        return patientProgramAttributes.size() > 0 ? patientProgramAttributes : null;
    }

    @Override
    public BahmniPatientProgram saveBahmniPatientProgram(BahmniPatientProgram bahmniPatientProgram) {
        savePatientProgram(bahmniPatientProgram.getPatientProgram());
        savePatientProgramAttributes(bahmniPatientProgram.getAttributes());
        return bahmniPatientProgram;
    }

    private void savePatientProgramAttributes(Set<PatientProgramAttribute> attributes) {
        Iterator<PatientProgramAttribute> it = attributes.iterator();
        while (it.hasNext()) {
            sessionFactory.getCurrentSession().saveOrUpdate(it.next());
        }
    }
}
