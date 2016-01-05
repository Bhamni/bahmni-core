package org.bahmni.module.bahmnicore.web.v1_0.controller;

import org.bahmni.module.bahmnicore.service.BahmniDrugOrderService;
import org.bahmni.module.bahmnicore.service.BahmniObsService;
import org.bahmni.module.referencedata.labconcepts.contract.Concept;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.openmrs.api.ConceptService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Set;

import static org.junit.Assert.*;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

@RunWith(MockitoJUnitRunner.class)
public class BahmniDrugOrderControllerTest {

    @Mock
    ConceptService conceptService;
    @Mock
    BahmniDrugOrderService bahmniDrugOrderService;

    @InjectMocks
    BahmniDrugOrderController bahmniDrugOrderController;

    @Before
    public void setUp() throws Exception {
        initMocks(this);
    }

    @Test
    public void shouldReturnNullIfConceptNotFound() throws Exception {
        String drugConceptSetName = "All TB Drugs";
        when(conceptService.getConceptByName(drugConceptSetName)).thenReturn(null);
        Set<org.openmrs.Concept> drugConcepts = bahmniDrugOrderController.getDrugConcepts(drugConceptSetName);
        assertNull(drugConcepts);
    }

    @Test
    public void shouldReturnNullIfDrugConceptNameIsNull() {
        Set<org.openmrs.Concept> drugConcepts = bahmniDrugOrderController.getDrugConcepts(null);
        assertNull(drugConcepts);
    }
}