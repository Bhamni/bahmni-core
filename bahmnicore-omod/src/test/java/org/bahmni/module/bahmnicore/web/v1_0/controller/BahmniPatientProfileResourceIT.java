package org.bahmni.module.bahmnicore.web.v1_0.controller;

import org.apache.commons.io.FileUtils;
import org.bahmni.module.bahmnicore.web.v1_0.BaseIntegrationTest;
import org.hibernate.exception.DataException;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.openmrs.Patient;
import org.openmrs.Relationship;
import org.openmrs.RelationshipType;
import org.openmrs.api.context.Context;
import org.openmrs.module.emrapi.patient.EmrPatientProfileService;
import org.openmrs.module.emrapi.patient.PatientProfile;
import org.openmrs.module.idgen.contract.IdentifierSource;
import org.openmrs.module.idgen.webservices.services.IdentifierSourceServiceWrapper;
import org.openmrs.module.webservices.rest.SimpleObject;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@PrepareForTest(Context.class)
public class BahmniPatientProfileResourceIT extends BaseIntegrationTest {

    @Autowired
    private EmrPatientProfileService emrPatientProfileService;

    private BahmniPatientProfileResource bahmniPatientProfileResource;
    private SimpleObject propertiesToCreate;
    private ClassLoader classLoader;

    @Mock
    private IdentifierSourceServiceWrapper identifierSourceServiceWrapper;

    @Mock
    private IdentifierSource identifierSource;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        executeDataSet("createPatientMetadata.xml");
        bahmniPatientProfileResource = new BahmniPatientProfileResource(emrPatientProfileService, identifierSourceServiceWrapper);
        when(identifierSourceServiceWrapper.getSequenceValueUsingIdentifierSourceUuid("dead-cafe")).thenReturn("300010");
        when(identifierSourceServiceWrapper.generateIdentifierUsingIdentifierSourceUuid("dead-cafe", "")).thenReturn("BAH300010");
        classLoader = getClass().getClassLoader();
        File file = new File(classLoader.getResource("patient.json").getFile());
        String jsonString = FileUtils.readFileToString(file);
        propertiesToCreate = SimpleObject.parseJson(jsonString);
    }

    @Test
    public void shouldReturnHttpPreconditionFailedStatusAndJumpSizeIfIdentifierIsPassedInTheRequestAndTheirIsAJump() throws Exception {
        LinkedHashMap identifierProperties = (LinkedHashMap) ((ArrayList) ((LinkedHashMap) propertiesToCreate.get("patient")).get("identifiers")).get(0);
        String identifier = String.valueOf(identifierProperties.get("identifierPrefix")).concat("300020");
        identifierProperties.put("identifier", identifier);
        ResponseEntity<Object> response = bahmniPatientProfileResource.create(false, propertiesToCreate);
        assertEquals(412, response.getStatusCode().value());
        assertEquals("[{\"sizeOfJump\":10,\"identifierType\":\"81433852-3f10-11e4-adec-0800271c1b75\"}]", response.getBody().toString());
        verify(identifierSourceServiceWrapper,never()).saveSequenceValueUsingIdentifierSourceUuid(anyLong(), anyString());
    }

    @Test
    public void shouldCreatePatientWhenUserAcceptsTheJump() throws Exception {
        LinkedHashMap identifierProperties = (LinkedHashMap) ((ArrayList) ((LinkedHashMap) propertiesToCreate.get("patient")).get("identifiers")).get(0);
        String identifier = String.valueOf(identifierProperties.get("identifierPrefix")).concat("300020");
        identifierProperties.put("identifier", identifier);
        ResponseEntity<Object> response = bahmniPatientProfileResource.create(true, propertiesToCreate);
        assertEquals(200, response.getStatusCode().value());
    }

    @Test
    public void shouldCreatePatientWhenIdentifierIsPassedAndJumpIsZero() throws Exception {
        LinkedHashMap identifierProperties = (LinkedHashMap) ((ArrayList) ((LinkedHashMap) propertiesToCreate.get("patient")).get("identifiers")).get(0);
        String identifier = String.valueOf(identifierProperties.get("identifierPrefix")).concat("300010");
        identifierProperties.put("identifier", identifier);
        ResponseEntity<Object> response = bahmniPatientProfileResource.create(false, propertiesToCreate);
        assertEquals(200, response.getStatusCode().value());
    }

    @Test
    public void shouldCreatePatient() throws Exception {
        ResponseEntity<Object> response = bahmniPatientProfileResource.create(false, propertiesToCreate);
        assertEquals(200, response.getStatusCode().value());
    }
    
    @Test
    public void shouldCreatePatientRelationshipsFromBothTypesOfJsons() throws Exception {
        ResponseEntity<Object> response = bahmniPatientProfileResource.create(false, propertiesToCreate);
        assertEquals((((PatientProfile)response.getBody()).getRelationships()).size(),2);
        assertEquals(200, response.getStatusCode().value());
    }

    @Test
    public void shouldCreatePatientWhenIdentifierPrefixIsNotPresentAndIdentifierIsManuallyEntered() throws Exception {
        HashMap<String, Object> patient = propertiesToCreate.get("patient");
        List<HashMap<String, Object>> identifiers = (List<HashMap<String, Object>>) patient.get("identifiers");
        identifiers.get(0).put("identifier", "identifier");
        identifiers.get(0).put("identifierPrefix", "");

        ResponseEntity<Object> response = bahmniPatientProfileResource.create(false, propertiesToCreate);

        assertEquals(200, response.getStatusCode().value());
    }

    @Test
    public void shouldCreatePatientWhenIdentifierPrefixIsBlankAndNoIdentifierIsEntered() throws Exception {
        when(identifierSourceServiceWrapper.getAllIdentifierSources()).thenReturn(Arrays.asList(identifierSource));
        when(identifierSource.getName()).thenReturn("identifierName");
        when(identifierSourceServiceWrapper.generateIdentifier("identifierName", "")).thenReturn("300010");
        HashMap<String, Object> patient = propertiesToCreate.get("patient");
        List<HashMap<String, Object>> identifiers = (ArrayList<HashMap<String, Object>>) patient.get("identifiers");
        identifiers.get(0).put("identifierPrefix", "");


        ResponseEntity<Object> response = bahmniPatientProfileResource.create(false, propertiesToCreate);

        assertEquals(200, response.getStatusCode().value());
    }
    
    @Test
    public void shouldReturnBadRequestForInvalidJson() throws Exception {
        LinkedHashMap person = ((LinkedHashMap)((LinkedHashMap)propertiesToCreate.get("patient")).get("person"));
        person.remove("names");
        ResponseEntity<Object> response = bahmniPatientProfileResource.create(false, propertiesToCreate);
        assertEquals(400, response.getStatusCode().value());
    }

    @Test
    public void shouldUpdatePatient() throws Exception {
        File file = new File(classLoader.getResource("updatePatient.json").getFile());
        String jsonString = FileUtils.readFileToString(file);
        propertiesToCreate = SimpleObject.parseJson(jsonString);
        String uuid = "592b29e1-b3f5-423e-83cb-0d2c9b80867f";
        ResponseEntity<Object> response = bahmniPatientProfileResource.update(uuid, propertiesToCreate);
        assertEquals(200, response.getStatusCode().value());
        final Patient patient = ((PatientProfile) response.getBody()).getPatient();
        SimpleDateFormat formatter = new SimpleDateFormat("EEE MMM dd hh:mm:ss yyyy");
        assertEquals("Wed Mar 07 12:00:00 1984", formatter.format(patient.getBirthdate()));
        assertEquals(2, patient.getIdentifiers().size());
        assertEquals("ABC123DEF", patient.getActiveIdentifiers().get(1).getIdentifier());
    }

    @Test
    public void shouldReturnBadRequestForLongPatientName() throws Exception {
        File file = new File(classLoader.getResource("updatePatient.json").getFile());
        String jsonString = FileUtils.readFileToString(file);
        propertiesToCreate = SimpleObject.parseJson(jsonString);
        LinkedHashMap name = (LinkedHashMap) ((ArrayList) ((LinkedHashMap) ((LinkedHashMap) propertiesToCreate.get("patient")).get("person")).get("names")).get(0);
        name.put("givenName", "LongStringLongStringLongStringLongStringLongStringLongString");
        String uuid = "592b29e1-b3f5-423e-83cb-0d2c9b80867f";
        ResponseEntity<Object> response = bahmniPatientProfileResource.update(uuid, propertiesToCreate);
        assertEquals(400, response.getStatusCode().value());
    }

    @Test
    public void shouldReturnBadRequestForLongNumericPatientIdentifier() throws Exception {
        LinkedHashMap identifier = (LinkedHashMap) ((ArrayList) ((LinkedHashMap) propertiesToCreate.get("patient")).get("identifiers")).get(0);
        identifier.put("identifier", "BAH12345678912345678");
        when(identifierSourceServiceWrapper.saveSequenceValueUsingIdentifierSourceUuid(12345678912345679L, "dead-cafe")).thenThrow(DataException.class);
        ResponseEntity<Object> response = bahmniPatientProfileResource.create(true, propertiesToCreate);
        assertThat(response.getStatusCode().value(), is(400));
        assertThat(response.getBody().toString(), is("{\"error\":{\"message\":\"Entered numeric patient identifier is too large\"}}"));
    }

    @Test
    public void shouldCreatePatientWithMultipleIdentifiers() throws Exception {
        ResponseEntity<Object> response = bahmniPatientProfileResource.create(false, propertiesToCreate);
        assertEquals(200, response.getStatusCode().value());
        assertEquals(2, ((PatientProfile)response.getBody()).getPatient().getIdentifiers().size());
    }
}
