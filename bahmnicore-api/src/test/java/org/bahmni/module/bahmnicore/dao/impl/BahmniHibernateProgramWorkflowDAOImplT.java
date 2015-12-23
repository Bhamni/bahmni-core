package org.bahmni.module.bahmnicore.dao.impl;

import org.bahmni.module.bahmnicore.BaseIntegrationTest;
import org.bahmni.module.bahmnicore.dao.BahmniProgramWorkflowDAO;
import org.bahmni.module.bahmnicore.model.bahmniPatientProgram.BahmniPatientProgram;
import org.bahmni.module.bahmnicore.model.bahmniPatientProgram.PatientProgramAttribute;
import org.bahmni.module.bahmnicore.model.bahmniPatientProgram.ProgramAttributeType;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.Program;
import org.openmrs.User;
import org.openmrs.api.PatientService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Date;
import java.util.List;

public class BahmniHibernateProgramWorkflowDAOImplT extends BaseIntegrationTest {

    @Autowired
    BahmniProgramWorkflowDAO bahmniHibernateProgramWorkflowDAO;

    @Autowired
    PatientService patientService;

    @Before
    public void setUp() throws Exception {
        executeDataSet("ProgramWorkFlowDAOTestData.xml");
    }

    private BahmniPatientProgram buildPatientProgramObject() {
        BahmniPatientProgram patientProgram = new BahmniPatientProgram();
        patientProgram.setProgram(bahmniHibernateProgramWorkflowDAO.getProgram(5));
        patientProgram.setDateCreated(new Date());
        patientProgram.setCreator(new User(1));
        patientProgram.setVoided(false);
        patientProgram.setPatient(patientService.getPatient(2));
        return patientProgram;
    }

    private PatientProgramAttribute buildProgramAttributeObject(String value, int id) {
        PatientProgramAttribute attribute = new PatientProgramAttribute();
        attribute.setAttributeType(bahmniHibernateProgramWorkflowDAO.getProgramAttributeTypeByUuid("d7477c21-bfc3-4922-9591-e89d8b9c8efb"));
        BahmniPatientProgram patientProgram = new BahmniPatientProgram();
        Program program = new Program();
        program.setId(id);
        patientProgram.setProgram(program);
        attribute.setPatientProgram(patientProgram);
        attribute.setVoided(false);
        attribute.setDateCreated(new Date());
        attribute.setCreator(new User(1));
        attribute.setValue(value);
        return attribute;
    }

    @Test
    public void testSaveBahmniPatientProgram() throws Exception {
        BahmniPatientProgram bahmniPatientProgram = buildPatientProgramObject();
        bahmniPatientProgram.addAttribute(buildProgramAttributeObject("Attr1", 5));
        bahmniPatientProgram = bahmniHibernateProgramWorkflowDAO.saveBahmniPatientProgram(bahmniPatientProgram);
        BahmniPatientProgram patientProgram = (BahmniPatientProgram) bahmniHibernateProgramWorkflowDAO.getPatientProgram(bahmniPatientProgram.getId());

        Assert.assertTrue(patientProgram.getId() != null);
        Assert.assertTrue(patientProgram.getProgram().getId() == 5);
        Assert.assertTrue(patientProgram.getAttributes().size() == 1);
    }

    @Test
    public void testGetAllProgramAttributeTypes() throws Exception {
        List<ProgramAttributeType> list = bahmniHibernateProgramWorkflowDAO.getAllProgramAttributeTypes();

        Assert.assertTrue(list.size() == 1);
        Assert.assertTrue(list.get(0).getId() == 1);
        Assert.assertTrue(list.get(0).getName().equals("stage"));
    }

    @Test
    public void testGetProgramAttributeTypeById() throws Exception {
        ProgramAttributeType programAttributeType = bahmniHibernateProgramWorkflowDAO.getProgramAttributeType(1);

        Assert.assertTrue(programAttributeType.getName().equals("stage"));
    }

    @Test
    public void testGetProgramAttributeTypeByUuid() throws Exception {
        ProgramAttributeType programAttributeType = bahmniHibernateProgramWorkflowDAO.getProgramAttributeTypeByUuid("d7477c21-bfc3-4922-9591-e89d8b9c8efb");

        Assert.assertTrue(programAttributeType.getName().equals("stage"));
    }

    @Test
    public void testSaveProgramAttributeType() throws Exception {
        ProgramAttributeType programAttributeType = buildProgramAttributeType("Type1");
        bahmniHibernateProgramWorkflowDAO.saveProgramAttributeType(programAttributeType);

        Assert.assertTrue(programAttributeType.getName().equals("Type1"));
        Assert.assertTrue(programAttributeType.getUuid().isEmpty() == false);
        Assert.assertTrue(programAttributeType.getRetired() == false);
    }

    private ProgramAttributeType buildProgramAttributeType(String name) {
        ProgramAttributeType programAttributeType = new ProgramAttributeType();
        programAttributeType.setName(name);
        programAttributeType.setRetired(false);
        programAttributeType.setDateCreated(new Date());
        programAttributeType.setCreator(new User(1));
        return programAttributeType;
    }

    @Test
    public void testGetPatientProgramAttributeByUuid() throws Exception {
        PatientProgramAttribute programAttribute = bahmniHibernateProgramWorkflowDAO.getPatientProgramAttributeByUuid("3a2bdb18-6faa-11e0-8414-001e378eb67e");

        Assert.assertTrue(programAttribute.getValue().equals("Stage1"));
        Assert.assertTrue(programAttribute.getAttributeType().getName().equals("stage"));
    }

    @Test
    public void testPurgeProgramAttributeType() throws Exception {
        ProgramAttributeType programAttributeType = new ProgramAttributeType();
        programAttributeType.setId(1);
        bahmniHibernateProgramWorkflowDAO.purgeProgramAttributeType(programAttributeType);
        Assert.assertNull(bahmniHibernateProgramWorkflowDAO.getProgramAttributeType(1));
    }

    @Test
    public void testGetAttributesPatientProgramById() throws Exception {
        List<PatientProgramAttribute> patientProgramAttributes = bahmniHibernateProgramWorkflowDAO.getAttributesPatientProgramById(1);
        Assert.assertTrue(patientProgramAttributes.size() == 1);
        Assert.assertTrue(patientProgramAttributes.get(0).getId() == 1);
        Assert.assertTrue(patientProgramAttributes.get(0).getValue().equals("Stage1"));
    }
}