package org.bahmni.module.bahmnicore.service.impl;

import org.bahmni.module.bahmnicore.BaseIntegrationTest;
import org.bahmni.module.bahmnicore.model.bahmniPatientProgram.BahmniPatientProgram;
import org.bahmni.module.bahmnicore.model.bahmniPatientProgram.PatientProgramAttribute;
import org.bahmni.module.bahmnicore.model.bahmniPatientProgram.ProgramAttributeType;
import org.bahmni.module.bahmnicore.service.BahmniProgramWorkflowService;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.PatientProgram;
import org.openmrs.api.AdministrationService;
import org.openmrs.api.ConceptService;
import org.openmrs.api.EncounterService;
import org.openmrs.api.ProgramWorkflowService;
import org.openmrs.api.context.Context;
import org.springframework.test.context.ContextConfiguration;

import java.util.Date;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@ContextConfiguration(locations = {"classpath:moduleApplicationContext.xml"},inheritLocations = true)
public class BahmniProgramWorkflowServiceImplIT extends BaseIntegrationTest {

	protected static final String CREATE_PATIENT_PROGRAMS_XML = "org/openmrs/api/include/ProgramWorkflowServiceTest-createPatientProgram.xml";

	protected BahmniProgramWorkflowService pws = null;

	protected AdministrationService adminService = null;

	protected EncounterService encounterService = null;

	protected ConceptService cs = null;

	@Before
	public void runBeforeEachTest() throws Exception {
		executeDataSet(CREATE_PATIENT_PROGRAMS_XML);
		executeDataSet("programAttributesDataSet.xml");

		if (pws == null) {
			pws = Context.getService(BahmniProgramWorkflowService.class);
			adminService = Context.getAdministrationService();
			encounterService = Context.getEncounterService();
			cs = Context.getConceptService();
		}
	}


	//Test from OpenMRS to ensure that the patientprogram functionality works
	@Test
	public void savePatientProgram_shouldUpdatePatientProgram() throws Exception {

		Date today = new Date();

		PatientProgram patientProgram = pws.getPatientProgram(1);

		patientProgram.setDateCompleted(today);
		patientProgram.setChangedBy(Context.getAuthenticatedUser());
		patientProgram.setDateChanged(today);
		pws.savePatientProgram(patientProgram);

		PatientProgram ptProg = pws.getPatientProgram(1);
		assertNotNull(ptProg.getDateCompleted());
		assertEquals(today, ptProg.getDateCompleted());

	}

	@Test
	public void getAllProgramAttributeTypes_shouldRetreiveAllProgramAttributeTypes(){
		List<ProgramAttributeType> programAttributeTypes = pws.getAllProgramAttributeTypes();
		assertEquals(1, programAttributeTypes.size());
		assertEquals("d7477c21-bfc3-4922-9591-e89d8b9c8efb", programAttributeTypes.get(0).getUuid());
		assertEquals(new Integer(1), programAttributeTypes.get(0).getId());
		assertEquals("ProgramId", programAttributeTypes.get(0).getName());
	}

	@Test
	public void saveProgramAttributeType_shouldSaveProgramAttributeType(){
		ProgramAttributeType programFacilityAttributeType = new ProgramAttributeType();
		programFacilityAttributeType.setName("Facility Phone");
		programFacilityAttributeType.setMinOccurs(0);
		programFacilityAttributeType.setMaxOccurs(1);
		programFacilityAttributeType.setDatatypeClassname("org.openmrs.customdatatype.datatype.FreeTextDatatype");

		pws.saveProgramAttributeType(programFacilityAttributeType);
		List<ProgramAttributeType> programAttributeTypes = pws.getAllProgramAttributeTypes();
		assertEquals(2, programAttributeTypes.size());

		assertEquals("d7477c21-bfc3-4922-9591-e89d8b9c8efb", programAttributeTypes.get(0).getUuid());
		assertEquals(new Integer(1), programAttributeTypes.get(0).getId());
		assertEquals("ProgramId", programAttributeTypes.get(0).getName());

		assertEquals("Facility Phone", programAttributeTypes.get(1).getName());
		assertEquals(new Integer(0), programAttributeTypes.get(1).getMinOccurs());
		assertEquals(new Integer(1), programAttributeTypes.get(1).getMaxOccurs());
		assertEquals("org.openmrs.customdatatype.datatype.FreeTextDatatype", programAttributeTypes.get(1).getDatatypeClassname());
	}

	@Test
	public void getProgramAttributeType_getProgramAttributeTypeById(){
		ProgramAttributeType programAttributeType = pws.getProgramAttributeType(1);
		assertEquals(new Integer(1), programAttributeType.getId());
		assertEquals("d7477c21-bfc3-4922-9591-e89d8b9c8efb", programAttributeType.getUuid());
		assertEquals("ProgramId", programAttributeType.getName());
	}

	@Test
	public void getProgramAttributeTypeByUuid_shouldReturnAttributeTypeByUuid(){
		ProgramAttributeType programAttributeType = pws.getProgramAttributeTypeByUuid("d7477c21-bfc3-4922-9591-e89d8b9c8efb");
		assertEquals(new Integer(1), programAttributeType.getId());
		assertEquals("d7477c21-bfc3-4922-9591-e89d8b9c8efb", programAttributeType.getUuid());
		assertEquals("ProgramId", programAttributeType.getName());
	}

	@Test
	public void purgeProgramAttributeType_shouldDeleteProgramAttributeType(){
		ProgramAttributeType programAttributeType = pws.getProgramAttributeTypeByUuid("d7477c21-bfc3-4922-9591-e89d8b9c8efb");
		pws.purgeProgramAttributeType(programAttributeType);
		List<ProgramAttributeType> programAttributeTypes = pws.getAllProgramAttributeTypes();
		assertEquals(0,programAttributeTypes.size());
	}

	@Test
	public void savePatientProgram_addAttributesToExistingPatientProgram(){
		PatientProgram patientProgram = pws.getPatientProgram(1);

		PatientProgramAttribute attribute = new PatientProgramAttribute();
		attribute.setAttributeType(pws.getProgramAttributeType(1));
		attribute.setValue("0123456789");

		((BahmniPatientProgram) patientProgram).addAttribute(attribute);
		pws.savePatientProgram(patientProgram);

		patientProgram = pws.getPatientProgram(1);
		Set<PatientProgramAttribute> attributes = ((BahmniPatientProgram)patientProgram).getAttributes();
		assertEquals(1,attributes.size());

		attribute = attributes.iterator().next();
		assertEquals(new Integer(1),attribute.getAttributeType().getId());
		assertEquals("0123456789", attribute.getValue());
	}

	@Test
	public void getPatientProgramByUuid_shouldFindObjectGivenValidUuid() throws Exception {
		String uuid = "2edf272c-bf05-4208-9f93-2fa213ed0415";
		PatientProgram patientProgram = Context.getService(BahmniProgramWorkflowService.class).getPatientProgramByUuid(uuid);
		Assert.assertEquals(2, (int) patientProgram.getPatientProgramId());

		PatientProgramAttribute attribute = new PatientProgramAttribute();
		attribute.setAttributeType(Context.getService(BahmniProgramWorkflowService.class).getProgramAttributeType(1));
		attribute.setValue("0123456789");

		((BahmniPatientProgram)patientProgram).addAttribute(attribute);
		Context.getService(BahmniProgramWorkflowService.class).savePatientProgram(patientProgram);

		patientProgram = Context.getService(BahmniProgramWorkflowService.class).getPatientProgram(2);
		Set<PatientProgramAttribute> attributes = ((BahmniPatientProgram)patientProgram).getAttributes();
		assertEquals(1, attributes.size());

		attribute = attributes.iterator().next();
		assertEquals(new Integer(1),attribute.getAttributeType().getId());
		assertEquals("0123456789", attribute.getValue());
	}


}
