package org.bahmni.module.bahmnicore.web.v1_0.controller;

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
import org.openmrs.Relationship;
import org.openmrs.RelationshipType;
import org.openmrs.api.APIAuthenticationException;
import org.openmrs.api.AdministrationService;
import org.openmrs.api.PatientService;
import org.openmrs.api.PersonService;
import org.openmrs.api.context.Context;
import org.openmrs.messagesource.MessageSourceService;
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

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.doReturn;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.spy;
import static org.powermock.api.mockito.PowerMockito.doThrow;

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
        PowerMockito.when(personResource1_8.getByUniqueId(anyString())).thenReturn(patient);
        PowerMockito.when(Context.getService(RestService.class).getResourceBySupportedClass(RelationshipType.class)).thenReturn(relationShipTypeResource1_8);
        PowerMockito.when(relationShipTypeResource1_8.getByUniqueId(anyString())).thenReturn(new RelationshipType());
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
        PatientProfile delegate = mock(PatientProfile.class);
        doReturn(delegate).when(spy, "mapForUpdatePatient", anyString(), any(SimpleObject.class));
        when(emrPatientProfileService.save(delegate)).thenReturn(delegate);
        doNothing().when(spy).setConvertedProperties(any(PatientProfile.class), any(SimpleObject.class), any(DelegatingResourceDescription.class), any(Boolean.class));
        Person person = new Person();
        person.setUuid("personUuid");
        when(personService.getPersonByUuid("patientUuid")).thenReturn(person);
        List<Relationship> relationships = Collections.emptyList();
        when(personService.getRelationshipsByPerson(person)).thenReturn(relationships);
        Patient patient = mock(Patient.class);
        when(patient.getUuid()).thenReturn("patientUuid");
        when(delegate.getPatient()).thenReturn(patient);

        ResponseEntity<Object> response = spy.update("someUuid", propertiesToCreate);

        Assert.assertEquals(200, response.getStatusCode().value());
        verify(personService, times(1)).getPersonByUuid("patientUuid");
        verify(delegate, times(2)).setRelationships(relationships);
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
