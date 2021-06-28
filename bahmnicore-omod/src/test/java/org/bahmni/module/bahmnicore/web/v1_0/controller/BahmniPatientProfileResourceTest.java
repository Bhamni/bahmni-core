package org.bahmni.module.bahmnicore.web.v1_0.controller;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.doReturn;
import static org.powermock.api.mockito.PowerMockito.doThrow;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.spy;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.openmrs.Patient;
import org.openmrs.PatientIdentifier;
import org.openmrs.Person;
import org.openmrs.PersonName;
import org.openmrs.Relationship;
import org.openmrs.RelationshipType;
import org.openmrs.api.APIAuthenticationException;
import org.openmrs.api.AdministrationService;
import org.openmrs.api.PatientService;
import org.openmrs.api.PersonService;
import org.openmrs.api.context.Context;
import org.openmrs.messagesource.MessageSourceService;
import org.openmrs.module.emrapi.encounter.DateMapper;
import org.openmrs.module.emrapi.patient.EmrPatientProfileService;
import org.openmrs.module.emrapi.patient.PatientProfile;
import org.openmrs.module.idgen.webservices.services.IdentifierSourceServiceWrapper;
import org.openmrs.module.webservices.rest.SimpleObject;
import org.openmrs.module.webservices.rest.web.api.RestService;
import org.openmrs.module.webservices.rest.web.resource.impl.DelegatingResourceDescription;
import org.openmrs.module.webservices.rest.web.v1_0.resource.openmrs1_8.PatientResource1_8;
import org.openmrs.module.webservices.rest.web.v1_0.resource.openmrs1_8.PersonResource1_8;
import org.openmrs.module.webservices.rest.web.v1_0.resource.openmrs1_8.RelationShipTypeResource1_8;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.springframework.http.ResponseEntity;

// TODO: 13/09/16 This is wrong way of writing test. We should mock the external dependency in resource but we ended up mocking all internal dependencies. For eg: MessageSourceService
@PrepareForTest({Context.class, BahmniPatientProfileResource.class})
@RunWith(PowerMockRunner.class)
public class BahmniPatientProfileResourceTest {

	@Mock
	private EmrPatientProfileService emrPatientProfileService;

	@Mock
	private RestService restService;

	@Mock
	PatientResource1_8 patientResource1_8;

	@Mock
	PersonResource1_8 personResource1_8;

	@Mock
	RelationShipTypeResource1_8 relationShipTypeResource1_8;

	@Mock
	private AdministrationService administrationService;

	@Mock
	private PatientService patientService;

	@Mock
	private PersonService personService;

	@Mock
	private MessageSourceService messageSourceService;

	@Mock
	private IdentifierSourceServiceWrapper identifierSourceServiceWrapper;

	private BahmniPatientProfileResource bahmniPatientProfileResource;
	private SimpleObject propertiesToCreate;

	@Before
	public void setUp() throws IOException {
		MockitoAnnotations.initMocks(this);
		ClassLoader classLoader = getClass().getClassLoader();
		File file = new File(classLoader.getResource("patient.json").getFile());
		String jsonString = FileUtils.readFileToString(file);
		propertiesToCreate = new SimpleObject().parseJson(jsonString);
		Patient patient = new Patient();
		patient.setGender("M");
		mockStatic(Context.class);
		PowerMockito.when(Context.getService(RestService.class)).thenReturn(restService);
		PowerMockito.when(Context.getPersonService()).thenReturn(personService);
		PowerMockito.when(Context.getMessageSourceService()).thenReturn(messageSourceService);
		PowerMockito.when(restService.getResourceBySupportedClass(Patient.class)).thenReturn(patientResource1_8);
		PowerMockito.when(patientResource1_8.getPatient(any(SimpleObject.class))).thenReturn(patient);
		PowerMockito.when(patientResource1_8.getPatientForUpdate(anyString(), any(SimpleObject.class))).thenReturn(patient);
		PowerMockito.when(Context.getService(RestService.class).getResourceBySupportedClass(Person.class)).thenReturn(personResource1_8);
		PowerMockito.when(personResource1_8.getByUniqueId("patientUuid")).thenReturn(patient);
		PowerMockito.when(Context.getService(RestService.class).getResourceBySupportedClass(RelationshipType.class)).thenReturn(relationShipTypeResource1_8);

		RelationshipType rt = new RelationshipType();
		rt.setUuid("2a5f4ff4-a179-4b8a-aa4c-40f71956eabc");
		PowerMockito.when(relationShipTypeResource1_8.getByUniqueId(anyString())).thenReturn(rt);
	}

	@Test
	public void createPatient() throws Exception {
		bahmniPatientProfileResource = new BahmniPatientProfileResource(emrPatientProfileService, identifierSourceServiceWrapper);
		BahmniPatientProfileResource bahmniPatientProfileResourceSpy = spy(this.bahmniPatientProfileResource);
		PatientProfile delegate = mock(PatientProfile.class);
		when(identifierSourceServiceWrapper.generateIdentifierUsingIdentifierSourceUuid("dead-cafe", "")).thenReturn("BAH300010");
		doReturn(delegate).when(bahmniPatientProfileResourceSpy, "mapForCreatePatient", propertiesToCreate);
		when(emrPatientProfileService.save(delegate)).thenReturn(delegate);
		when(Context.getAdministrationService()).thenReturn(administrationService);
		when(Context.getPatientService()).thenReturn(patientService);
		Patient patient = mock(Patient.class);
		when(patient.getUuid()).thenReturn("patientUuid");
		when(delegate.getPatient()).thenReturn(patient);
		PatientIdentifier patientIdentifier = mock(PatientIdentifier.class);
		Set<PatientIdentifier> patientIdentifiers = new HashSet<>();
		patientIdentifiers.add(patientIdentifier);
		when(patient.getIdentifiers()).thenReturn(patientIdentifiers);
		doNothing().when(bahmniPatientProfileResourceSpy).setConvertedProperties(any(PatientProfile.class), any(SimpleObject.class), any(DelegatingResourceDescription.class), any(Boolean.class));
		Person person = new Person();
		person.setUuid("personUuid");
		when(personService.getPersonByUuid("patientUuid")).thenReturn(person);
		List<Relationship> relationships = Arrays.asList();
		when(personService.getRelationshipsByPerson(person)).thenReturn(relationships);

		ResponseEntity<Object> response = bahmniPatientProfileResourceSpy.create(false, propertiesToCreate);

		Assert.assertEquals(200, response.getStatusCode().value());
		verify(identifierSourceServiceWrapper, times(1)).generateIdentifierUsingIdentifierSourceUuid("dead-cafe", "");
		verify(personService, times(1)).getPersonByUuid("patientUuid");
		verify(delegate, times(1)).setRelationships(relationships);
	}

	@Test
	public void updatePatient() throws Exception {
		bahmniPatientProfileResource = new BahmniPatientProfileResource(emrPatientProfileService, identifierSourceServiceWrapper);
		BahmniPatientProfileResource spy = spy(bahmniPatientProfileResource);
		PatientProfile mockPatientProfile = mock(PatientProfile.class);
		when(emrPatientProfileService.save(mockPatientProfile)).thenReturn(mockPatientProfile);
		doNothing().when(spy).setConvertedProperties(any(PatientProfile.class), any(SimpleObject.class), any(DelegatingResourceDescription.class), any(Boolean.class));

		Person person = new Person();
		when(personService.getPersonByUuid("patientUuid")).thenReturn(person);

		// Create PersonB
		Person personB = new Person();
		// "592b29e1-b3f5-423e-83cb-0d2c9b80867f" is the UUID of PersonB as specified in the patient.json payload
		PowerMockito.when(personResource1_8.getByUniqueId("592b29e1-b3f5-423e-83cb-0d2c9b80867f")).thenReturn(personB);

		Patient patient = new Patient();
		patient.setUuid("patientUuid");
		when(mockPatientProfile.getPatient()).thenReturn(patient);

		doReturn(mockPatientProfile).when(spy, "mapForUpdatePatient", anyString(), any(SimpleObject.class));
			
		// Create the Relationship that corresponds to the one specified in patient.json payload
		Relationship relationship = new Relationship();
		relationship.setEndDate(new DateMapper().convertUTCToDate(	"2020-04-06T21:00:00.000Z"));
		relationship.setPersonA(patient);
		relationship.setPersonB(personB);
		relationship.setUuid("1234-5678");
		relationship.setRelationshipType(new RelationshipType());
		// Return it when calling "createRelationship" method.
		doReturn(relationship).when(spy, "createRelationship", any(), any());
		
		// Create the list of relationships
		List<Relationship> relationships = new ArrayList<Relationship>();
		relationships.add(relationship);
		// and return it when personService.getRelationshipsByPerson() is called
		when(personService.getRelationshipsByPerson(person)).thenReturn(relationships);

		// Replay
		ResponseEntity<Object> response = spy.update("patientUuid", propertiesToCreate);

		Assert.assertEquals(200, response.getStatusCode().value());
		verify(personService, times(1)).getPersonByUuid("patientUuid");
		verify(mockPatientProfile, times(2)).setRelationships(relationships);
	}

	@Test
	public void shouldThrowExceptionWhenPatientIsNotHavingProperPrivilege() throws Exception {
		bahmniPatientProfileResource = new BahmniPatientProfileResource(emrPatientProfileService, identifierSourceServiceWrapper);
		BahmniPatientProfileResource spy = spy(bahmniPatientProfileResource);
		doThrow(new APIAuthenticationException()).when(spy, "mapForUpdatePatient", anyString(), any(SimpleObject.class));

		ResponseEntity<Object> response = spy.update("someUuid", propertiesToCreate);
		Assert.assertEquals(403,response.getStatusCode().value());
	}
}
