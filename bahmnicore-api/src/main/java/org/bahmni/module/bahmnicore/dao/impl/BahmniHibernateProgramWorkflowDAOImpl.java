package org.bahmni.module.bahmnicore.dao.impl;

import org.bahmni.module.bahmnicore.dao.BahmniProgramWorkflowDAO;
import org.bahmni.module.bahmnicore.model.bahmniPatientProgram.BahmniPatientProgram;
import org.bahmni.module.bahmnicore.model.bahmniPatientProgram.PatientProgramAttribute;
import org.bahmni.module.bahmnicore.model.bahmniPatientProgram.ProgramAttributeType;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Restrictions;
import org.openmrs.api.db.DAOException;
import org.openmrs.api.db.hibernate.HibernateProgramWorkflowDAO;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

public class BahmniHibernateProgramWorkflowDAOImpl extends HibernateProgramWorkflowDAO implements BahmniProgramWorkflowDAO {
    @Autowired
    private SessionFactory sessionFactory;

    public void setSessionFactory(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
        super.setSessionFactory(sessionFactory);
    }

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
    public BahmniPatientProgram saveBahmniPatientProgram(BahmniPatientProgram bahmniPatientProgram) {
        if(bahmniPatientProgram.getPatientProgramId() == null) {
            this.sessionFactory.getCurrentSession().save(bahmniPatientProgram);
        } else {
            this.sessionFactory.getCurrentSession().merge(bahmniPatientProgram);
        }
        return bahmniPatientProgram;
    }

    @Override
    public BahmniPatientProgram getBahmniPatientProgram(Integer id) throws DAOException {
        return (BahmniPatientProgram)this.sessionFactory.getCurrentSession().get(BahmniPatientProgram.class, id);
    }

    @Override
    public BahmniPatientProgram getBahmniPatientProgramByUuid(String uuid) {
//        return (BahmniPatientProgram) sessionFactory.getCurrentSession().createQuery("from PatientProgram pp where pp.uuid = :uuid").setString("uuid", uuid).uniqueResult();
        return (BahmniPatientProgram) sessionFactory.getCurrentSession().createCriteria(BahmniPatientProgram.class).add(Restrictions.eq("uuid", uuid)).uniqueResult();
    }
}
