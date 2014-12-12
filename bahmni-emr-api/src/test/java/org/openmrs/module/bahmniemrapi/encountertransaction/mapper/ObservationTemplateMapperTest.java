package org.openmrs.module.bahmniemrapi.encountertransaction.mapper;

import org.bahmni.test.builder.ConceptBuilder;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.openmrs.Concept;
import org.openmrs.module.bahmniemrapi.encountertransaction.contract.BahmniObservation;
import org.openmrs.module.bahmniemrapi.encountertransaction.contract.ObservationTemplate;
import org.openmrs.module.emrapi.encounter.ConceptMapper;
import org.openmrs.module.emrapi.encounter.domain.EncounterTransaction;
import org.openmrs.test.TestUtil;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

public class ObservationTemplateMapperTest {

    private ObservationTemplateMapper observationTemplateMapper;
    private BahmniObservation bahmniObservation1;
    private BahmniObservation bahmniObservation2;
    private BahmniObservation bahmniObservation3;
    private BahmniObservation bahmniObservation4;
    private BahmniObservation bahmniObservation5;
    private Concept observationTemplateConcept;
    @Mock
    private ConceptMapper conceptMapper;
    private EncounterTransaction.Concept conceptData;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        bahmniObservation1 = new BahmniObservation();
        bahmniObservation2 = new BahmniObservation();
        bahmniObservation3 = new BahmniObservation();
        bahmniObservation4 = new BahmniObservation();
        bahmniObservation5 = new BahmniObservation();
        observationTemplateConcept = new ConceptBuilder().withUUID("otUUID").build();
        conceptData = new EncounterTransaction.Concept();
        conceptData.setName("Observation Template");
        conceptData.setConceptClass("otClass");
        conceptData.setDataType("N/A");
        conceptData.setUuid("otUUID");
        when(conceptMapper.map(observationTemplateConcept)).thenReturn(conceptData);
        observationTemplateMapper = new ObservationTemplateMapper(conceptMapper);
    }

    @Test
    public void map_obs_to_observation_templates_group_by_visit_date() throws Exception {
        bahmniObservation1.setVisitStartDateTime(TestUtil.createDateTime("2012-01-01"));
        bahmniObservation2.setVisitStartDateTime(TestUtil.createDateTime("2012-01-01"));
        bahmniObservation3.setVisitStartDateTime(TestUtil.createDateTime("2012-03-01"));
        bahmniObservation4.setVisitStartDateTime(TestUtil.createDateTime("2012-03-01"));
        bahmniObservation5.setVisitStartDateTime(TestUtil.createDateTime("2012-05-01"));
        List<BahmniObservation> bahmniObservations = new ArrayList<>();
        bahmniObservations.add(bahmniObservation1);
        bahmniObservations.add(bahmniObservation2);
        bahmniObservations.add(bahmniObservation3);
        bahmniObservations.add(bahmniObservation4);
        bahmniObservations.add(bahmniObservation5);
        List<ObservationTemplate> observationTemplates = observationTemplateMapper.map(bahmniObservations, observationTemplateConcept);
        assertEquals(3, observationTemplates.size());
        ObservationTemplate observationTemplate1 = observationTemplates.get(0);
        ObservationTemplate observationTemplate2 = observationTemplates.get(1);
        ObservationTemplate observationTemplate3 = observationTemplates.get(2);
        assertEquals("Observation Template", observationTemplate1.getConcept().getName());
        assertEquals(2, observationTemplate1.getBahmniObservations().size());
        assertEquals(TestUtil.createDateTime("2012-01-01"), observationTemplate1.getVisitStartDate());
        assertEquals(observationTemplate1.getVisitStartDate(), observationTemplate1.getBahmniObservations().get(0).getVisitStartDateTime());
        assertEquals(observationTemplate1.getVisitStartDate(), observationTemplate1.getBahmniObservations().get(1).getVisitStartDateTime());
        assertEquals(TestUtil.createDateTime("2012-03-01"), observationTemplate2.getVisitStartDate());
        assertEquals(observationTemplate2.getVisitStartDate(), observationTemplate2.getBahmniObservations().get(0).getVisitStartDateTime());
        assertEquals(observationTemplate2.getVisitStartDate(), observationTemplate2.getBahmniObservations().get(1).getVisitStartDateTime());
        assertEquals(TestUtil.createDateTime("2012-05-01"), observationTemplate3.getVisitStartDate());
        assertEquals(observationTemplate3.getVisitStartDate(), observationTemplate3.getBahmniObservations().get(0).getVisitStartDateTime());
        assertEquals("Observation Template", observationTemplate1.getConcept().getName());
        assertEquals("Observation Template", observationTemplate2.getConcept().getName());
        assertEquals("Observation Template", observationTemplate3.getConcept().getName());
        assertEquals(2, observationTemplate2.getBahmniObservations().size());
        assertEquals(1, observationTemplate3.getBahmniObservations().size());
    }

}