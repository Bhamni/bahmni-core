package org.bahmni.module.bahmnicore.dao;

import org.bahmni.module.bahmnicore.dao.impl.BahmniHibernateProgramWorkflowDAOImpl;
import org.bahmni.module.bahmnicore.model.bahmniPatientProgram.BahmniPatientProgram;
import org.bahmni.module.bahmnicore.model.bahmniPatientProgram.PatientProgramAttribute;
import org.bahmni.module.bahmnicore.model.bahmniPatientProgram.ProgramAttributeType;
import org.hibernate.Criteria;
import org.hibernate.SessionFactory;
import org.hibernate.classic.Session;
import org.hibernate.criterion.Restrictions;
import org.hibernate.criterion.SimpleExpression;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(PowerMockRunner.class)
@PrepareForTest({Restrictions.class})
public class BahmniHibernateProgramWorkflowDAOImplTest {

    BahmniHibernateProgramWorkflowDAOImpl bahmniProgramWorkflowDAO;

    @Mock
    SessionFactory sessionFactory;

    @Mock
    Session session;

    @Mock
    Criteria criteria;

    @Mock
    SimpleExpression simpleExpression;

    private Integer sampleId = 1234;
    private String sampleUuid = "a1b2c3";

    @Before
    public void setUp() throws Exception {
        bahmniProgramWorkflowDAO = new BahmniHibernateProgramWorkflowDAOImpl();
        bahmniProgramWorkflowDAO.setSessionFactory(sessionFactory);
        when(sessionFactory.getCurrentSession()).thenReturn(session);
    }

    @Test
    public void testGetAllProgramAttributeTypes() throws Exception {
        when(session.createCriteria(ProgramAttributeType.class)).thenReturn(criteria);

        bahmniProgramWorkflowDAO.getAllProgramAttributeTypes();

        verify(sessionFactory).getCurrentSession();
        verify(session).createCriteria(ProgramAttributeType.class);
        verify(criteria).list();
    }

    @Test
    public void testGetProgramAttributeType() throws Exception {
        bahmniProgramWorkflowDAO.getProgramAttributeType(sampleId);
        verify(session).get(ProgramAttributeType.class, sampleId);
    }

    @Test
    public void testGetProgramAttributeTypeByUuid() throws Exception {
        PowerMockito.mockStatic(Restrictions.class);
        when(session.createCriteria(ProgramAttributeType.class)).thenReturn(criteria);
        when(Restrictions.eq("uuid", sampleUuid)).thenReturn(simpleExpression);
        when(criteria.add(simpleExpression)).thenReturn(criteria);

        bahmniProgramWorkflowDAO.getProgramAttributeTypeByUuid(sampleUuid);
        verify(session).createCriteria(ProgramAttributeType.class);
        verify(criteria).uniqueResult();
    }

    @Test
    public void testSaveProgramAttributeType() throws Exception {
        ProgramAttributeType programAttributeType = new ProgramAttributeType();

        bahmniProgramWorkflowDAO.saveProgramAttributeType(programAttributeType);
        verify(session).saveOrUpdate(programAttributeType);
    }

    @Test
    public void testGetPatientProgramAttributeByUuid() throws Exception {
        PowerMockito.mockStatic(Restrictions.class);
        when(session.createCriteria(PatientProgramAttribute.class)).thenReturn(criteria);
        when(Restrictions.eq("uuid", sampleUuid)).thenReturn(simpleExpression);
        when(criteria.add(simpleExpression)).thenReturn(criteria);

        bahmniProgramWorkflowDAO.getPatientProgramAttributeByUuid(sampleUuid);
        verify(session).createCriteria(PatientProgramAttribute.class);
        verify(criteria).uniqueResult();
    }

    @Test
    public void testPurgeProgramAttributeType() throws Exception {
        ProgramAttributeType programAttributeType = new ProgramAttributeType();
        bahmniProgramWorkflowDAO.purgeProgramAttributeType(programAttributeType);
        verify(session).delete(programAttributeType);
    }

    @Test
    public void testSaveBahmniPatientProgram() throws Exception {
        BahmniPatientProgram bahmniPatientProgram = new BahmniPatientProgram();
        bahmniProgramWorkflowDAO.saveBahmniPatientProgram(bahmniPatientProgram);
        verify(session).save(bahmniPatientProgram);
        bahmniPatientProgram.setPatientProgramId(sampleId);
        bahmniProgramWorkflowDAO.saveBahmniPatientProgram(bahmniPatientProgram);
        verify(session).merge(bahmniPatientProgram);
    }

    @Test
    public void testGetBahmniPatientProgram() throws Exception {
        bahmniProgramWorkflowDAO.getBahmniPatientProgram(sampleId);
        verify(session).get(BahmniPatientProgram.class,sampleId);
    }
}