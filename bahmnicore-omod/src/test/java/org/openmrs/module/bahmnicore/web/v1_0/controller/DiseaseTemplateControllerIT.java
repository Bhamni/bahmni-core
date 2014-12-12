package org.openmrs.module.bahmnicore.web.v1_0.controller;

import org.openmrs.module.bahmniemrapi.encountertransaction.contract.DiseaseTemplate;
import org.openmrs.module.bahmniemrapi.encountertransaction.contract.ObservationTemplate;
import org.bahmni.test.web.controller.BaseWebControllerTest;
import org.codehaus.jackson.type.TypeReference;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.api.ObsService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@org.springframework.test.context.ContextConfiguration(locations = {"classpath:TestingApplicationContext.xml"}, inheritLocations = true)
public class DiseaseTemplateControllerIT extends BaseWebControllerTest {

    @Autowired
    DiseaseTemplateController diseaseTemplateController;

    @Autowired
    private ObsService obsService;

    @Before
    public void setUp() throws Exception {
        executeDataSet("obsTestData.xml");
    }

    @Test
    public void shouldReturnObsForAllDiseaseTemplatesWithIntakeAndProgressFromTheLatestVisit() throws Exception {
        String dataJson = "{\n" +
                "  \"diseaseTemplateConfigList\" : [],\n" +
                "  \"patientUuid\": \"86526ed5-3c11-11de-a0ba-001e378eb67a\"\n" +
                "}";
        List<DiseaseTemplate> diseaseTemplates = deserialize(handle(newPostRequest("/rest/v1/bahmnicore/diseaseTemplates", dataJson)), new TypeReference<List<DiseaseTemplate>>() {});
        assertNotNull(diseaseTemplates);
        assertEquals(1, diseaseTemplates.size());
        DiseaseTemplate breastCancer = diseaseTemplates.get(0);
        assertEquals(2, breastCancer.getObservationTemplates().size());
        ObservationTemplate breastCancerIntake = breastCancer.getObservationTemplates().get(0);
        assertEquals(2, breastCancerIntake.getBahmniObservations().size());
        assertEquals("Breast Cancer Intake", breastCancerIntake.getConcept().getName());
        assertEquals("BC_intake_concept_uuid", breastCancerIntake.getConcept().getUuid());
    }

    @Test
    public void shouldReturnObsForADiseaseTemplateWithIntakeAndProgressAcrossAllVisits() throws Exception {
        DiseaseTemplate diseaseTemplates = deserialize(handle(newGetRequest("/rest/v1/bahmnicore/diseaseTemplate", new Parameter("patientUuid", "86526ed5-3c11-11de-a0ba-001e378eb67a"), new Parameter("diseaseName", "Breast Cancer"))), new TypeReference<DiseaseTemplate>() {});
        assertNotNull(diseaseTemplates);
        assertEquals("Breast Cancer", diseaseTemplates.getConcept().getName());
        assertEquals(4, diseaseTemplates.getObservationTemplates().size());
    }
}