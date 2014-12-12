package org.openmrs.module.bahmniemrapi.encountertransaction.service;

import org.bahmni.module.bahmnicore.contract.diseasetemplate.DiseaseTemplateConfig;
import org.bahmni.module.bahmnicore.contract.diseasetemplate.DiseaseTemplatesConfig;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.module.bahmniemrapi.encountertransaction.contract.BahmniObservation;
import org.openmrs.module.bahmniemrapi.encountertransaction.contract.DiseaseTemplate;
import org.openmrs.module.bahmniemrapi.encountertransaction.contract.ObservationTemplate;
import org.openmrs.web.test.BaseModuleWebContextSensitiveTest;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@org.springframework.test.context.ContextConfiguration(locations = {"classpath:TestingApplicationContext.xml"}, inheritLocations = true)
public class DiseaseTemplateServiceImplIT extends BaseModuleWebContextSensitiveTest {
    @Before
    public void setUp() throws Exception {
        executeDataSet("obsTestData.xml");
        executeDataSet("diseaseTemplate.xml");
    }

    @Autowired
    private DiseaseTemplateService diseaseTemplateService;

    @Test
    public void get_disease_template_for_observation_template_concept() throws Exception {
        DiseaseTemplate diseaseTemplate = diseaseTemplateService.diseaseTemplateFor("b2a59310-58e8-11e4-8ed6-0800200c9a66", "Blood Pressure");
        assertEquals(1, diseaseTemplate.getObservationTemplates().size());
        ObservationTemplate observationTemplate = diseaseTemplate.getObservationTemplates().get(0);
        assertEquals(1, observationTemplate.getBahmniObservations().size());
        BahmniObservation obs = observationTemplate.getBahmniObservations().get(0);
        assertTrue(obs.getValue().equals(100.0));
    }

    @Test
    public void get_all_disease_template_for_specified_observation_template_for_disease() throws Exception {
        ArrayList<String> showOnly = new ArrayList<>();
        showOnly.add("Breast Cancer Intake");

        DiseaseTemplateConfig diseaseTemplateConfig = new DiseaseTemplateConfig();
        diseaseTemplateConfig.setTemplateName("Breast Cancer");
        diseaseTemplateConfig.setShowOnly(showOnly);

        ArrayList<DiseaseTemplateConfig> diseaseTemplateConfigList = new ArrayList<>();
        diseaseTemplateConfigList.add(diseaseTemplateConfig);

        DiseaseTemplatesConfig diseaseTemplatesConfig = new DiseaseTemplatesConfig();
        diseaseTemplatesConfig.setPatientUuid("86526ed5-3c11-11de-a0ba-001e378eb67a");
        diseaseTemplatesConfig.setDiseaseTemplateConfigList(diseaseTemplateConfigList);

        List<DiseaseTemplate> diseaseTemplates = diseaseTemplateService.allDiseaseTemplatesFor(diseaseTemplatesConfig);
        assertEquals(1, diseaseTemplates.size());
        assertEquals(1, diseaseTemplates.get(0).getObservationTemplates().size());
        assertEquals("Breast Cancer Intake", diseaseTemplates.get(0).getObservationTemplates().get(0).getConcept().getName());
    }

    @Test
    public void get_all_disease_template_for_specified_concept_for_disease() throws Exception {
        ArrayList<String> showOnly = new ArrayList<>();
        showOnly.add("Receptor Status");

        DiseaseTemplateConfig diseaseTemplateConfig = new DiseaseTemplateConfig();
        diseaseTemplateConfig.setTemplateName("Breast Cancer");
        diseaseTemplateConfig.setShowOnly(showOnly);

        ArrayList<DiseaseTemplateConfig> diseaseTemplateConfigList = new ArrayList<>();
        diseaseTemplateConfigList.add(diseaseTemplateConfig);

        DiseaseTemplatesConfig diseaseTemplatesConfig = new DiseaseTemplatesConfig();
        diseaseTemplatesConfig.setPatientUuid("86526ed5-3c11-11de-a0ba-001e378eb67a");
        diseaseTemplatesConfig.setDiseaseTemplateConfigList(diseaseTemplateConfigList);

        List<DiseaseTemplate> diseaseTemplates = diseaseTemplateService.allDiseaseTemplatesFor(diseaseTemplatesConfig);
        assertEquals(1, diseaseTemplates.size());
        assertEquals(1, diseaseTemplates.get(0).getObservationTemplates().size());

        assertEquals("Breast Cancer Intake", diseaseTemplates.get(0).getObservationTemplates().get(0).getConcept().getName());
        assertEquals(1, diseaseTemplates.get(0).getObservationTemplates().get(0).getBahmniObservations().size());
        assertEquals("Receptor Status", diseaseTemplates.get(0).getObservationTemplates().get(0).getBahmniObservations().get(0).getConcept().getName());
    }

    @Test
    public void get_all_disease_template_for_specified_concept_for_disease_exists_in_both_intake_and_progress() throws Exception {
        ArrayList<String> showOnly = new ArrayList<>();
        showOnly.add("Histopathology");

        DiseaseTemplateConfig diseaseTemplateConfig = new DiseaseTemplateConfig();
        diseaseTemplateConfig.setTemplateName("Breast Cancer");
        diseaseTemplateConfig.setShowOnly(showOnly);

        ArrayList<DiseaseTemplateConfig> diseaseTemplateConfigList = new ArrayList<>();
        diseaseTemplateConfigList.add(diseaseTemplateConfig);

        DiseaseTemplatesConfig diseaseTemplatesConfig = new DiseaseTemplatesConfig();
        diseaseTemplatesConfig.setPatientUuid("86526ed5-3c11-11de-a0ba-001e378eb67a");
        diseaseTemplatesConfig.setDiseaseTemplateConfigList(diseaseTemplateConfigList);

        List<DiseaseTemplate> diseaseTemplates = diseaseTemplateService.allDiseaseTemplatesFor(diseaseTemplatesConfig);
        assertEquals(1, diseaseTemplates.size());
        assertEquals(2, diseaseTemplates.get(0).getObservationTemplates().size());

        assertEquals("Breast Cancer Intake", diseaseTemplates.get(0).getObservationTemplates().get(0).getConcept().getName());
        assertEquals(1, diseaseTemplates.get(0).getObservationTemplates().get(0).getBahmniObservations().size());
        assertEquals("Histopathology", diseaseTemplates.get(0).getObservationTemplates().get(0).getBahmniObservations().get(0).getConcept().getName());

        assertEquals("Breast Cancer Progress", diseaseTemplates.get(0).getObservationTemplates().get(1).getConcept().getName());
        assertEquals(1, diseaseTemplates.get(0).getObservationTemplates().get(1).getBahmniObservations().size());
        assertEquals("Histopathology", diseaseTemplates.get(0).getObservationTemplates().get(1).getBahmniObservations().get(0).getConcept().getName());
    }
}