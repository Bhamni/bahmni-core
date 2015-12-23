package org.bahmni.module.bahmnicore.dao.impl;

import org.bahmni.module.bahmnicore.dao.BahmniProgramWorkflowDAO;
import org.bahmni.module.bahmnicore.model.bahmniPatientProgram.BahmniPatientProgram;
import org.bahmni.module.bahmnicore.model.bahmniPatientProgram.PatientProgramAttribute;
import org.bahmni.module.bahmnicore.model.bahmniPatientProgram.ProgramAttributeType;
import org.hibernate.Criteria;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Restrictions;
import org.openmrs.api.db.hibernate.HibernateProgramWorkflowDAO;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

public class BahmniHibernateProgramWorkflowDAOImpl extends HibernateProgramWorkflowDAO implements BahmniProgramWorkflowDAO {
    @Autowired
    private SessionFactory sessionFactory;

    @SuppressWarnings("unchecked")
    @Override
    public List<ProgramAttributeType> getAllProgramAttributeTypes() {
        return sessionFactory.getCurrentSession().createCriteria(ProgramAttributeType.class).list();
    }

    @Override
    public ProgramAttributeType getProgramAttributeType(Integer id) {
        return (ProgramAttributeType) sessionFactory.getCurrentSession().get(ProgramAttributeType.class, id);
    }

    @Override
    public ProgramAttributeType getProgramAttributeTypeByUuid(String uuid) {
        return (ProgramAttributeType) sessionFactory.getCurrentSession().createCriteria(ProgramAttributeType.class).add(Restrictions.eq("uuid", uuid)).uniqueResult();
    }

    @Override
    public ProgramAttributeType saveProgramAttributeType(ProgramAttributeType programAttributeType) {
        sessionFactory.getCurrentSession().saveOrUpdate(programAttributeType);
        return programAttributeType;
    }

    @Override
    public PatientProgramAttribute getPatientProgramAttributeByUuid(String uuid) {
        return (PatientProgramAttribute) sessionFactory.getCurrentSession().createCriteria(PatientProgramAttribute.class).add(Restrictions.eq("uuid", uuid))
                .uniqueResult();
    }

    @Override
    public void purgeProgramAttributeType(ProgramAttributeType type) {
        sessionFactory.getCurrentSession().delete(type);
    }

    @Override
    public List<PatientProgramAttribute> getAttributesPatientProgramById(int id) {
        Criteria criteria = sessionFactory.getCurrentSession().createCriteria(PatientProgramAttribute.class).add(
                Restrictions.eq("patientProgram", id));
        List<PatientProgramAttribute> patientProgramAttributes = criteria.list();
        if (null == patientProgramAttributes || patientProgramAttributes.isEmpty()) {
            return null;
        }
        return patientProgramAttributes;
    }

    @Override
    public BahmniPatientProgram saveBahmniPatientProgram(BahmniPatientProgram bahmniPatientProgram) {
        if(bahmniPatientProgram.getPatientProgramId() == null) {
            this.sessionFactory.getCurrentSession().save(bahmniPatientProgram);
        } else {
            this.sessionFactory.getCurrentSession().merge(bahmniPatientProgram);
        }

        return bahmniPatientProgram;
    }
}
