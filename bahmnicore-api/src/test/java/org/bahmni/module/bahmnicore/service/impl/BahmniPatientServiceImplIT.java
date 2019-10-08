package org.bahmni.module.bahmnicore.service.impl;

import org.bahmni.module.bahmnicore.BaseIntegrationTest;
import org.bahmni.module.bahmnicore.contract.patient.PatientSearchParameters;
import org.bahmni.module.bahmnicore.contract.patient.response.PatientResponse;
import org.bahmni.module.bahmnicore.service.BahmniPatientService;
import org.hibernate.SessionFactory;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.openmrs.api.AdministrationService;
import org.openmrs.api.LocationService;
import org.openmrs.api.db.hibernate.PersonAttributeHelper;
import org.openmrs.module.webservices.rest.web.RequestContext;
import org.openmrs.module.webservices.rest.web.RestUtil;
import org.openmrs.util.GlobalPropertiesTestHelper;
import org.openmrs.util.OpenmrsConstants;
import org.springframework.beans.factory.annotation.Autowired;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

public class BahmniPatientServiceImplIT extends BaseIntegrationTest {

    @Autowired
    private BahmniPatientService bahmniPatientService;

    @Autowired
    private LocationService locationService;

    @Autowired
    private AdministrationService adminService;

    private GlobalPropertiesTestHelper globalPropertiesTestHelper;

    private PersonAttributeHelper personAttributeHelper;

    private RequestContext requestContext;

    @Autowired
    private SessionFactory sessionFactory;

    @Before
    public void setUp() throws Exception {
        HttpServletRequest mockedRequest = Mockito.mock(HttpServletRequest.class);
        HttpServletResponse mockedRsponse = Mockito.mock(HttpServletResponse.class);
        executeDataSet("apiTestData.xml");
        updateSearchIndex();

        requestContext = RestUtil.getRequestContext(mockedRequest, mockedRsponse);
        personAttributeHelper = new PersonAttributeHelper(sessionFactory);
        globalPropertiesTestHelper = new GlobalPropertiesTestHelper(adminService);

        globalPropertiesTestHelper.setGlobalProperty(OpenmrsConstants.GLOBAL_PROPERTY_PATIENT_SEARCH_MATCH_MODE,
                OpenmrsConstants.GLOBAL_PROPERTY_PATIENT_SEARCH_MATCH_ANYWHERE);
        globalPropertiesTestHelper.setGlobalProperty(OpenmrsConstants.GLOBAL_PROPERTY_PERSON_ATTRIBUTE_SEARCH_MATCH_MODE,
                OpenmrsConstants.GLOBAL_PROPERTY_PATIENT_SEARCH_MATCH_ANYWHERE);
        globalPropertiesTestHelper.setGlobalProperty(OpenmrsConstants.GLOBAL_PROPERTY_PATIENT_IDENTIFIER_SEARCH_MATCH_MODE,
                OpenmrsConstants.GLOBAL_PROPERTY_PATIENT_SEARCH_MATCH_ANYWHERE);

    }

    @Test
    public void shouldIntersectBetweenLuceneAndHibernateWithIdentifier(){
        PatientSearchParameters searchParameters = new PatientSearchParameters(requestContext);
        searchParameters.setIdentifier("GAN");
        searchParameters.setLoginLocationUuid("c36006e5-9fbb-4f20-866b-0ece245615a1");
        searchParameters.setLength(100);
        searchParameters.setStart(0);
        List<PatientResponse> patientResponses = bahmniPatientService.luceneHibernateSearch(searchParameters);
        Assert.assertEquals(5, patientResponses.size());
    }

    @Test
    public void shouldIntersectBetweenLuceneAndHibernateWithName(){
        PatientSearchParameters searchParameters = new PatientSearchParameters(requestContext);
        searchParameters.setName("Sin");
        searchParameters.setLoginLocationUuid("c36006e5-9fbb-4f20-866b-0ece245615a1");
        searchParameters.setLength(100);
        searchParameters.setStart(0);
        List<PatientResponse> patientResponses = bahmniPatientService.luceneHibernateSearch(searchParameters);
        Assert.assertEquals(2, patientResponses.size());
    }

    @Test
    public void shouldIntersectBetweenLuceneAndHibernateWithAttribute(){
        PatientSearchParameters searchParameters = new PatientSearchParameters(requestContext);
        searchParameters.setLoginLocationUuid("c36006e5-9fbb-4f20-866b-0ece245615a1");
        searchParameters.setCustomAttribute("testCaste1");
        searchParameters.setLength(100);
        searchParameters.setStart(0);
        List<PatientResponse> patientResponses = bahmniPatientService.luceneHibernateSearch(searchParameters);
        Assert.assertEquals(1, patientResponses.size());
    }


    @Test
    public void shouldFetchPatientsByPatientAddress(){
        PatientSearchParameters searchParameters = new PatientSearchParameters(requestContext);
        searchParameters.setLoginLocationUuid("c36006e5-9fbb-4f20-866b-0ece245615a1");
        String[] addressResultFields = {"city_village"};
        String[] patientResultFields = {"caste"};
        searchParameters.setAddressSearchResultFields(addressResultFields);
        searchParameters.setAddressFieldValue("Bilaspur");
        List<PatientResponse> patientResponses = bahmniPatientService.luceneHibernateSearch(searchParameters);
        Assert.assertEquals(2, patientResponses.size());

    }

    @Test
    public void shouldIntersectBetweenLuceneAndHibernateWithIdentifierAndPatientAddress(){
        PatientSearchParameters searchParameters = new PatientSearchParameters(requestContext);
        searchParameters.setLoginLocationUuid("c36006e5-9fbb-4f20-866b-0ece245615a1");
        String[] addressResultFields = {"city_village"};
        searchParameters.setAddressSearchResultFields(addressResultFields);
        searchParameters.setIdentifier("GAN200002");
        searchParameters.setAddressFieldValue("Bilaspur");

        List<PatientResponse> patientResponses = bahmniPatientService.luceneHibernateSearch(searchParameters);
        PatientResponse response = patientResponses.get(0);
        Assert.assertEquals(1, patientResponses.size());
        Assert.assertEquals("GAN200002",response.getIdentifier());
        Assert.assertEquals(1026,response.getPersonId());
        Assert.assertEquals("John",response.getGivenName());
        Assert.assertEquals("Peeter",response.getMiddleName());
        Assert.assertEquals("Sinha",response.getFamilyName());
    }
}
