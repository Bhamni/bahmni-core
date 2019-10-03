package org.bahmni.module.bahmnicore.web.v1_0.controller.search;

import org.bahmni.module.bahmnicore.web.v1_0.BaseIntegrationTest;
import org.junit.Before;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class BahmniPatientSearchControllerIT extends BaseIntegrationTest {

    @Before
    public void setup() throws Exception {
        executeDataSet("apiTestData.xml");
        updateSearchIndex();
    }

    @Test
    public void shouldReturnPatientDetailsWhenUsingLuceneSearch() throws Exception {
        MockHttpServletRequest request = newGetRequest("/rest/v1/bahmnicore/search/patient/lucene");
        request.setParameter("loginLocationUuid", "c36006e5-9fbb-4f20-866b-0ece245615a1");
        request.setParameter("q", "GAN200001");

        MockHttpServletResponse response = handle(request);

        assertEquals(200, response.getStatus());
        assertTrue("Expected response to contain patient uuid", response.getContentAsString().contains("uuid"));
    }

    @Test
    public void shouldReturnPatientDetailsWhenUsingSimilarSearch() throws Exception {
        MockHttpServletRequest request = newGetRequest("/rest/v1/bahmnicore/search/patient/similar");
        request.setParameter("gender", "M");
        request.setParameter("birthdate", "1983-01-30 00:00:00");
        request.setParameter("loginLocationUuid", "c36006e5-9fbb-4f20-866b-0ece245615a1");
        request.setParameter("q", "Sinha");

        MockHttpServletResponse response = handle(request);

        assertEquals(200, response.getStatus());
        assertTrue("Expected response to contain list of results", response.getContentAsString().contains("pageOfResults"));
        assertTrue("Expected response to contain patient uuid", response.getContentAsString().contains("uuid"));
    }
}