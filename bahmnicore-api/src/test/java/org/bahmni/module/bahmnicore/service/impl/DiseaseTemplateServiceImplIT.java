package org.bahmni.module.bahmnicore.service.impl;

import org.bahmni.module.bahmnicore.BaseIntegrationTest;
import org.bahmni.module.bahmnicore.contract.diseasetemplate.DiseaseTemplate;
import org.bahmni.module.bahmnicore.contract.diseasetemplate.DiseaseTemplateConfig;
import org.bahmni.module.bahmnicore.contract.diseasetemplate.DiseaseTemplatesConfig;
import org.bahmni.module.bahmnicore.contract.diseasetemplate.ObservationTemplate;
import org.bahmni.module.bahmnicore.service.DiseaseTemplateService;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.module.bahmniemrapi.encountertransaction.contract.BahmniObservation;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class DiseaseTemplateServiceImplIT extends BaseIntegrationTest {
    @Before
    public void setUp() throws Exception {
        executeDataSet("dispositionMetadata.xml");
        executeDataSet("obsTestData.xml");
        executeDataSet("diagnosisMetadata.xml");
        executeDataSet("diseaseTemplate.xml");
    }

    @Autowired
    private DiseaseTemplateService diseaseTemplateService;

    @Test
    public void get_disease_template_for_observation_template_concept() throws Exception {
        DiseaseTemplatesConfig diseaseTemplatesConfig = new DiseaseTemplatesConfig();
        List<DiseaseTemplateConfig> diseaseTemplateConfigList = new ArrayList<>();
        DiseaseTemplateConfig diseaseTemplateConfig = new DiseaseTemplateConfig();
        diseaseTemplateConfig.setTemplateName("Blood Pressure");
        diseaseTemplateConfigList.add(diseaseTemplateConfig);
        diseaseTemplatesConfig.setPatientUuid("b2a59310-58e8-11e4-8ed6-0800200c9a66");
        diseaseTemplatesConfig.setStartDate(null);
        diseaseTemplatesConfig.setEndDate(null);
        diseaseTemplatesConfig.setDiseaseTemplateConfigList(diseaseTemplateConfigList);

        DiseaseTemplate diseaseTemplate = diseaseTemplateService.diseaseTemplateFor(diseaseTemplatesConfig);
        assertEquals(1, diseaseTemplate.getObservationTemplates().size());
        ObservationTemplate observationTemplate = diseaseTemplate.getObservationTemplates().get(0);
        assertEquals(1, observationTemplate.getBahmniObservations().size());
        BahmniObservation obs = observationTemplate.getBahmniObservations().iterator().next();
        assertTrue(obs.getValue().equals(100.0));
    }

    @Test
    public void get_disease_template_ignores_invalid_template_name() throws Exception {
        DiseaseTemplatesConfig diseaseTemplatesConfig = new DiseaseTemplatesConfig();
        List<DiseaseTemplateConfig> diseaseTemplateConfigList = new ArrayList<>();
        DiseaseTemplateConfig diseaseTemplateConfig = new DiseaseTemplateConfig();
        diseaseTemplateConfig.setTemplateName("Non existing Concept");
        diseaseTemplateConfigList.add(diseaseTemplateConfig);
        diseaseTemplatesConfig.setPatientUuid("b2a59310-58e8-11e4-8ed6-0800200c9a66");
        diseaseTemplatesConfig.setStartDate(null);
        diseaseTemplatesConfig.setEndDate(null);
        diseaseTemplatesConfig.setDiseaseTemplateConfigList(diseaseTemplateConfigList);

        DiseaseTemplate diseaseTemplate = diseaseTemplateService.diseaseTemplateFor(diseaseTemplatesConfig);
        assertEquals("Non existing Concept", diseaseTemplate.getConcept().getName());
        assertEquals(0, diseaseTemplate.getObservationTemplates().size());
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
        assertEquals("Receptor Status", diseaseTemplates.get(0).getObservationTemplates().get(0).getBahmniObservations().iterator().next().getConcept().getName());
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
        assertEquals("Histopathology", diseaseTemplates.get(0).getObservationTemplates().get(0).getBahmniObservations().iterator().next().getConcept().getName());

        assertEquals("Breast Cancer Progress", diseaseTemplates.get(0).getObservationTemplates().get(1).getConcept().getName());
        assertEquals(1, diseaseTemplates.get(0).getObservationTemplates().get(1).getBahmniObservations().size());
        assertEquals("Histopathology", diseaseTemplates.get(0).getObservationTemplates().get(1).getBahmniObservations().iterator().next().getConcept().getName());
    }

    @Test
    public void get_all_disease_template_should_get_latest_across_all_visits_for_class_case_intake() throws Exception {
        executeDataSet("diseaseTemplateScopeLatest.xml");
        ArrayList<String> showOnly = new ArrayList<>();

        DiseaseTemplateConfig diseaseTemplateConfig = new DiseaseTemplateConfig();
        diseaseTemplateConfig.setTemplateName("Anaemia");
        diseaseTemplateConfig.setShowOnly(showOnly);

        ArrayList<DiseaseTemplateConfig> diseaseTemplateConfigList = new ArrayList<>();
        diseaseTemplateConfigList.add(diseaseTemplateConfig);

        DiseaseTemplatesConfig diseaseTemplatesConfig = new DiseaseTemplatesConfig();
        diseaseTemplatesConfig.setPatientUuid("86526ed5-3c11-11de-a0ba-001e378eb67a");
        diseaseTemplatesConfig.setDiseaseTemplateConfigList(diseaseTemplateConfigList);

        List<DiseaseTemplate> diseaseTemplates = diseaseTemplateService.allDiseaseTemplatesFor(diseaseTemplatesConfig);
        assertEquals(1, diseaseTemplates.size());
        assertEquals("Anaemia", diseaseTemplates.get(0).getConcept().getName());

        assertEquals("Anaemia Intake", diseaseTemplates.get(0).getObservationTemplates().get(0).getConcept().getName());
        assertEquals(1, diseaseTemplates.get(0).getObservationTemplates().get(0).getBahmniObservations().size());
        BahmniObservation bahmniObservations = diseaseTemplates.get(0).getObservationTemplates().get(0).getBahmniObservations().iterator().next();
        assertEquals("Anaemia Intake", bahmniObservations.getConcept().getName());

        assertEquals(2, bahmniObservations.getGroupMembers().size());
        Iterator<BahmniObservation> groupMembersIterator = bahmniObservations.getGroupMembers().iterator();
        BahmniObservation diastolicConceptSet = groupMembersIterator.next();
        assertEquals("Diastolic", diastolicConceptSet.getConcept().getName());

        assertEquals(1, diastolicConceptSet.getGroupMembers().size());
        assertEquals("Diastolic value", diastolicConceptSet.getGroupMembers().iterator().next().getConcept().getName());

        assertEquals("Anaemia value", groupMembersIterator.next().getConcept().getName());
    }

    @Test
    public void get_all_disease_template_should_not_fail_when_invalid_showonly_provided() throws Exception {
        ArrayList<String> showOnly = new ArrayList<>();
        showOnly.add("Breast Cancer Intake");
        showOnly.add("Non existing concept");

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
}