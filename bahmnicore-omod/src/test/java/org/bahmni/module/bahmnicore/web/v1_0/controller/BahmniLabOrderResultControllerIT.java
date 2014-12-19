package org.bahmni.module.bahmnicore.web.v1_0.controller;

import org.bahmni.test.web.controller.BaseWebControllerTest;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.Patient;
import org.openmrs.api.PatientService;
import org.openmrs.module.bahmnicore.web.v1_0.controller.BahmniLabOrderResultController;
import org.openmrs.module.bahmniemrapi.document.contract.VisitDocumentResponse;
import org.openmrs.module.bahmniemrapi.laborder.contract.LabOrderResults;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

public class BahmniLabOrderResultControllerIT extends BaseWebControllerTest {

    @Autowired
    private BahmniLabOrderResultController labResultController;

    @Autowired
    private PatientService patientService;
    private String LAB_ORDER_URL = "/rest/v1/bahmnicore/labOrderResults";
    private String PERSON_UUID = "75e04d42-3ca8-11e3-bf2b-0800271c1b75";

    @Before
    public void setUp() throws Exception {
        executeDataSet("diagnosisMetaData.xml");
        executeDataSet("dispositionMetaData.xml");
        executeDataSet("labOrderTestData.xml");
    }

    @Test
    //todo: add more good assert statements
    public void shouldFindLabOrderResultsForMultipleVisitUuids() throws Exception {
        MockHttpServletRequest mockHttpServletRequest = newGetRequest(LAB_ORDER_URL,
                new Parameter("visitUuids", "ad41ef34-a41a-4ad6-8835-2f59099acf5a"),
                new Parameter("visitUuids", "9d705396-0c0c-11e4-bb80-f18addb6f9bb"));
        MockHttpServletResponse response = handle(mockHttpServletRequest);
        LabOrderResults labOrderResults = deserialize(response, LabOrderResults.class);
    }

    @Test
    //todo: add more good assert statements
    public void shouldReturnSpecifiedVisitsWhenQueried() throws Exception {
        MockHttpServletRequest mockHttpServletRequest = newGetRequest(LAB_ORDER_URL,
                new Parameter("patientUuid", PERSON_UUID),
                new Parameter("numberOfVisits", "1"));
        MockHttpServletResponse response = handle(mockHttpServletRequest);
        LabOrderResults labOrderResults = deserialize(response, LabOrderResults.class);
        assertNotNull(labOrderResults);
        assertEquals(labOrderResults.getResults().size(), 1);
        assertEquals(labOrderResults.getResults().get(0).getTestName(),"PS for Malaria");
    }
}
