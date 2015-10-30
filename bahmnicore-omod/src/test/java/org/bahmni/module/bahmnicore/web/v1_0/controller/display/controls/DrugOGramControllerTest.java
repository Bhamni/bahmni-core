package org.bahmni.module.bahmnicore.web.v1_0.controller.display.controls;

import org.bahmni.module.bahmnicore.service.BahmniDrugOrderService;
import org.bahmni.module.bahmnicore.web.v1_0.mapper.DrugOrderToRegimenMapper;
import org.bahmni.test.builder.ConceptBuilder;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.openmrs.Concept;
import org.openmrs.Order;
import org.openmrs.api.ConceptService;
import org.openmrs.module.bahmniemrapi.drugogram.contract.Regimen;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

@RunWith(PowerMockRunner.class)
public class DrugOGramControllerTest {
    @Mock
    private BahmniDrugOrderService bahmniDrugOrderService;
    @Mock
    private DrugOrderToRegimenMapper drugOrderToRegimenMapper;
    @Mock
    private ConceptService conceptService;

    private DrugOGramController drugOGramController;

    @Before
    public void setUp() throws Exception {
        drugOGramController = new DrugOGramController(bahmniDrugOrderService, drugOrderToRegimenMapper, conceptService);
    }

    @Test
    public void shouldFetchDrugsAsRegimen() throws Exception {
        ArrayList<Order> drugOrders = new ArrayList<>();
        when(bahmniDrugOrderService.getAllDrugOrders("patientUuid", null)).thenReturn(drugOrders);
        Regimen expected = new Regimen();
        when(drugOrderToRegimenMapper.map(drugOrders, null)).thenReturn(expected);

        Regimen actual = drugOGramController.getRegimen("patientUuid", null);

        verify(bahmniDrugOrderService, times(1)).getAllDrugOrders("patientUuid", null);
        verify(drugOrderToRegimenMapper, times(1)).map(drugOrders, null);
        assertEquals(expected, actual);
        assertEquals(0, expected.getHeaders().size());
    }

    @Test
    public void shouldFetchSpecifiedDrugsAsRegimen() throws Exception {
        Concept paracetamol = new ConceptBuilder().withName("Paracetamol").build();
        when(conceptService.getConceptByName("Paracetamol")).thenReturn(paracetamol);

        ArrayList<Order> drugOrders = new ArrayList<>();
        HashSet<Concept> concepts = new HashSet<>();
        concepts.add(paracetamol);
        when(bahmniDrugOrderService.getAllDrugOrders("patientUuid", concepts)).thenReturn(drugOrders);
        Regimen expected = new Regimen();
        when(drugOrderToRegimenMapper.map(drugOrders, concepts)).thenReturn(expected);

        Regimen actual = drugOGramController.getRegimen("patientUuid", Arrays.asList("Paracetamol"));

        verify(bahmniDrugOrderService, times(1)).getAllDrugOrders("patientUuid", concepts);
        verify(drugOrderToRegimenMapper, times(1)).map(drugOrders, concepts);
        assertEquals(expected, actual);
        assertEquals(0, expected.getHeaders().size());
    }
}