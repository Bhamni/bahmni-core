package org.bahmni.module.bahmnicore.dao.impl;

import org.bahmni.module.bahmnicore.dao.BahmniProgramWorkflowDAO;
import org.bahmni.module.bahmnicore.model.bahmniPatientProgram.BahmniPatientProgram;
import org.bahmni.module.bahmnicore.model.bahmniPatientProgram.PatientProgramAttribute;
import org.bahmni.module.bahmnicore.model.bahmniPatientProgram.ProgramAttributeType;
import org.hibernate.Criteria;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Restrictions;
import org.openmrs.Patient;
import org.openmrs.PatientProgram;
import org.openmrs.Program;
import org.openmrs.api.db.DAOException;
import org.openmrs.api.db.hibernate.HibernateProgramWorkflowDAO;
import org.openmrs.customdatatype.CustomDatatypeUtil;

import java.util.Date;
import java.util.List;

public class BahmniHibernateProgramWorkflowDAOImpl extends HibernateProgramWorkflowDAO implements BahmniProgramWorkflowDAO {

    private SessionFactory sessionFactory;

    public void setSessionFactory(SessionFactory sessionFactory) {
        super.setSessionFactory(sessionFactory);
        this.sessionFactory = sessionFactory;
    }

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
        return (ProgramAttributeType) sessionFactory.getCurrentSession().createCriteria(ProgramAttributeType.class).add(
                Restrictions.eq("uuid", uuid)).uniqueResult();
    }

    @Override
    public ProgramAttributeType saveProgramAttributeType(ProgramAttributeType programAttributeType) {
        sessionFactory.getCurrentSession().saveOrUpdate(programAttributeType);
        return programAttributeType;
    }

    @Override
    public PatientProgramAttribute getPatientProgramAttributeByUuid(String uuid) {
        return (PatientProgramAttribute) sessionFactory.getCurrentSession().createCriteria(PatientProgramAttribute.class).add(Restrictions.eq("uuid", uuid)).uniqueResult();
    }

    @Override
    public void purgeProgramAttributeType(ProgramAttributeType type) {
        sessionFactory.getCurrentSession().delete(type);
    }

    @Override
    public PatientProgram getPatientProgramByUuid(String uuid) {
        return (BahmniPatientProgram) sessionFactory.getCurrentSession().createCriteria(BahmniPatientProgram.class).add(
                        Restrictions.eq("uuid", uuid)).uniqueResult();
    }

    @Override
    public PatientProgram getPatientProgram(Integer patientProgramId) throws DAOException {
        return (BahmniPatientProgram)sessionFactory.getCurrentSession().get(BahmniPatientProgram.class, patientProgramId);
    }

    @Override
    public PatientProgram savePatientProgram(PatientProgram patientProgram) throws DAOException {
        CustomDatatypeUtil.saveAttributesIfNecessary((BahmniPatientProgram)patientProgram);
        return super.savePatientProgram(patientProgram);
    }

    public List<PatientProgram> getPatientPrograms(Patient patient, Program program, Date minEnrollmentDate,
                                                   Date maxEnrollmentDate, Date minCompletionDate, Date maxCompletionDate, boolean includeVoided)
            throws DAOException {
        Criteria crit = sessionFactory.getCurrentSession().createCriteria(BahmniPatientProgram.class);
        if (patient != null) {
            crit.add(Restrictions.eq("patient", patient));
        }
        if (program != null) {
            crit.add(Restrictions.eq("program", program));
        }
        if (minEnrollmentDate != null) {
            crit.add(Restrictions.ge("dateEnrolled", minEnrollmentDate));
        }
        if (maxEnrollmentDate != null) {
            crit.add(Restrictions.le("dateEnrolled", maxEnrollmentDate));
        }
        if (minCompletionDate != null) {
            crit.add(Restrictions.or(Restrictions.isNull("dateCompleted"), Restrictions.ge("dateCompleted",
                    minCompletionDate)));
        }
        if (maxCompletionDate != null) {
            crit.add(Restrictions.le("dateCompleted", maxCompletionDate));
        }
        if (!includeVoided) {
            crit.add(Restrictions.eq("voided", false));
        }
        return crit.list();
    }
}
