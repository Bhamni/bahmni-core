package org.bahmni.module.bahmnicore.web.v1_0.controller;

import org.bahmni.module.bahmnicore.contract.diseasetemplate.DiseaseTemplate;
import org.bahmni.module.bahmnicore.contract.diseasetemplate.ObservationTemplate;
import org.bahmni.module.bahmnicore.web.v1_0.BaseIntegrationTest;
import org.codehaus.jackson.type.TypeReference;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.api.ObsService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class DiseaseTemplateControllerIT extends BaseIntegrationTest {

    @Autowired
    DiseaseTemplateController diseaseTemplateController;

    @Autowired
    private ObsService obsService;

    @Before
    public void setUp() throws Exception {
        executeDataSet("diagnosisMetadata.xml");
        executeDataSet("dispositionMetadata.xml");
        executeDataSet("obsTestData.xml");
    }

    @Test
    public void shouldReturnObsForAllDiseaseTemplatesWithIntakeAndProgressFromTheLatestVisit() throws Exception {
        String dataJson = "{\n" +
                "  \"diseaseTemplateConfigList\" : [{" +
                                                    "\"templateName\": \"Breast Cancer\"" + "}],\n" +
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
    public void get_shouldReturnEmptyObservationTemplatesForIncorrectTemplateName() throws Exception {
        String dataJson = "{\n" +
                "  \"diseaseTemplateConfigList\" : [{" +
                                                    "\"templateName\": \"Does not exist\"" + "}],\n" +
                "  \"patientUuid\": \"86526ed5-3c11-11de-a0ba-001e378eb67a\"\n" +
                "}";
        List<DiseaseTemplate> diseaseTemplates = deserialize(handle(newPostRequest("/rest/v1/bahmnicore/diseaseTemplates", dataJson)), new TypeReference<List<DiseaseTemplate>>() {});
        assertEquals(1, diseaseTemplates.size());
        assertEquals(0, diseaseTemplates.get(0).getObservationTemplates().size());
    }

    @Test
    public void getDiseaseTemplate_shouldReturnEmptyObservationTemplatesForIncorrectTemplateName() throws Exception {
        String dataJson = "{\n" +
                "  \"diseaseTemplateConfigList\" : [{" +
                "\"templateName\": \"Non Existing Concept\"" + "}],\n" +
                "  \"patientUuid\": \"86526ed5-3c11-11de-a0ba-001e378eb67a\"\n" +
                "}";
        DiseaseTemplate diseaseTemplate = deserialize(handle(newPostRequest("/rest/v1/bahmnicore/diseaseTemplate", dataJson)), new TypeReference<DiseaseTemplate>() {});
        assertEquals(0, diseaseTemplate.getObservationTemplates().size());
    }

    @Test
    public void getDiseaseTemplate_shouldReturnObsForADiseaseTemplateWithIntakeAndProgressAcrossAllVisits() throws Exception {
        String dataJson = "{\n" +
                "  \"diseaseTemplateConfigList\" : [{" +
                "\"templateName\": \"Breast Cancer\"" + "}],\n" +
                "  \"patientUuid\": \"86526ed5-3c11-11de-a0ba-001e378eb67a\"\n" +
                "}";
        DiseaseTemplate diseaseTemplates = deserialize(handle(newPostRequest("/rest/v1/bahmnicore/diseaseTemplate", dataJson)), new TypeReference<DiseaseTemplate>() {});
        assertNotNull(diseaseTemplates);
        assertEquals("Breast Cancer", diseaseTemplates.getConcept().getName());
        assertEquals(4, diseaseTemplates.getObservationTemplates().size());
    }
}