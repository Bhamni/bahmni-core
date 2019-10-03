package org.bahmni.module.bahmnicore.service.impl;

import org.bahmni.module.bahmnicore.contract.patient.PatientSearchParameters;
import org.bahmni.module.bahmnicore.contract.patient.response.PatientConfigResponse;
import org.bahmni.module.bahmnicore.dao.PatientDao;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.openmrs.Concept;
import org.openmrs.PersonAttributeType;
import org.openmrs.api.ConceptService;
import org.openmrs.api.PersonService;
import org.openmrs.module.webservices.rest.web.RequestContext;

import javax.servlet.http.HttpServletRequest;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static junit.framework.Assert.assertEquals;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

public class BahmniPatientServiceImplTest {
    @Mock
    private PersonService personService;
    @Mock
    private ConceptService conceptService;
    @Mock
    RequestContext requestContext;
    @Mock
    private PatientDao patientDao;

    private BahmniPatientServiceImpl bahmniPatientService;

    @Before
    public void setup() {
        initMocks(this);
        bahmniPatientService = new BahmniPatientServiceImpl(personService, conceptService, patientDao);
    }

    @Test
    public void shouldGetPatientConfig() throws Exception {
        List<PersonAttributeType> personAttributeTypes = new ArrayList<>();
        personAttributeTypes.add(new PersonAttributeType() {{
            this.setName("class");
            this.setDescription("Class");
            this.setFormat("org.openmrs.Concept");
            this.setSortWeight(10.0);
            this.setForeignKey(10);
        }});
        personAttributeTypes.add(new PersonAttributeType() {{
            this.setName("primaryContact");
            this.setDescription("Primary Contact");
            this.setFormat("java.lang.String");
            this.setSortWeight(10.0);
        }});

        when(personService.getAllPersonAttributeTypes()).thenReturn(personAttributeTypes);
        when(conceptService.getConcept(anyInt())).thenReturn(new Concept());

        PatientConfigResponse config = bahmniPatientService.getConfig();
        assertEquals(2, config.getPersonAttributeTypes().size());
        assertEquals("class", config.getPersonAttributeTypes().get(0).getName());
        assertEquals("primaryContact", config.getPersonAttributeTypes().get(1).getName());
    }

    @Test
    public void shouldGetPatientByPartialIdentifier() throws Exception {
        boolean shouldMatchExactPatientId = false;
        bahmniPatientService.get("partial_identifier", shouldMatchExactPatientId);
        verify(patientDao).getPatients("partial_identifier", shouldMatchExactPatientId);
    }

    @Test
    public void shouldCallGetSimilarPatientsUsingLuceneSearch() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(requestContext.getRequest()).thenReturn(request);
        when(request.getParameterMap()).thenReturn(new HashMap<>());
        PatientSearchParameters patientSearchParameters = new PatientSearchParameters(requestContext);
        patientSearchParameters.setName("John");
        patientSearchParameters.setGender("M");
        patientSearchParameters.setBirthdate("1983-01-30 00:00:00");
        patientSearchParameters.setLoginLocationUuid("someUUid");

        bahmniPatientService.searchSimilarPatients(patientSearchParameters);
        verify(patientDao).getSimilarPatientsUsingLuceneSearch("John", "M", Timestamp.valueOf("1983-01-30 00:00:00"), "someUUid", 5);
    }
}
