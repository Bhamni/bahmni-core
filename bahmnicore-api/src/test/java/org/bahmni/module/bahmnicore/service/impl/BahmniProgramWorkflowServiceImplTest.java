package org.bahmni.module.bahmnicore.service.impl;

import org.bahmni.module.bahmnicore.dao.BahmniProgramWorkflowDAO;
import org.bahmni.module.bahmnicore.model.bahmniPatientProgram.BahmniPatientProgram;
import org.bahmni.module.bahmnicore.model.bahmniPatientProgram.ProgramAttributeType;
import org.bahmni.module.bahmnicore.service.BahmniProgramWorkflowService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.powermock.modules.junit4.PowerMockRunner;

import static org.mockito.Mockito.verify;

@RunWith(PowerMockRunner.class)
public class BahmniProgramWorkflowServiceImplTest {

    BahmniProgramWorkflowService bahmniProgramWorkflowService;

    @Mock
    BahmniProgramWorkflowDAO bahmniProgramWorkflowDAO;

    private Integer sampleId = 1234;
    private String sampleUuid = "a1b2c3";

    @Before
    public void before() {
        bahmniProgramWorkflowService = new BahmniProgramWorkflowServiceImpl(bahmniProgramWorkflowDAO);
    }

    @Test
    public void testGetAllProgramAttributeTypes() throws Exception {
        bahmniProgramWorkflowService.getAllProgramAttributeTypes();
        verify(bahmniProgramWorkflowDAO).getAllProgramAttributeTypes();
    }

    @Test
    public void testGetProgramAttributeType() throws Exception {
        bahmniProgramWorkflowService.getProgramAttributeType(sampleId);
        verify(bahmniProgramWorkflowDAO).getProgramAttributeType(sampleId);
    }

    @Test
    public void testGetProgramAttributeTypeByUuid() throws Exception {
        bahmniProgramWorkflowService.getProgramAttributeTypeByUuid(sampleUuid);
        verify(bahmniProgramWorkflowDAO).getProgramAttributeTypeByUuid(sampleUuid);
    }

    @Test
    public void testSaveProgramAttributeType() throws Exception {
        ProgramAttributeType programAttributeType = new ProgramAttributeType();
        bahmniProgramWorkflowService.saveProgramAttributeType(programAttributeType);
        verify(bahmniProgramWorkflowDAO).saveProgramAttributeType(programAttributeType);
    }

    @Test
    public void testPurgeProgramAttributeType() throws Exception {
        ProgramAttributeType programAttributeType = new ProgramAttributeType();
        bahmniProgramWorkflowService.purgeProgramAttributeType(programAttributeType);
        verify(bahmniProgramWorkflowDAO).purgeProgramAttributeType(programAttributeType);
    }

    @Test
    public void testGetPatientProgramAttributeByUuid() throws Exception {
        bahmniProgramWorkflowService.getPatientProgramAttributeByUuid(sampleUuid);
        verify(bahmniProgramWorkflowDAO).getPatientProgramAttributeByUuid(sampleUuid);
    }

    @Test
    public void testSaveBahmniPatientProgram() throws Exception {
        BahmniPatientProgram patientProgram = new BahmniPatientProgram();
        bahmniProgramWorkflowService.saveBahmniPatientProgram(patientProgram);
        verify(bahmniProgramWorkflowDAO).saveBahmniPatientProgram(patientProgram);
    }
}