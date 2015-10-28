package org.bahmni.module.bahmnicore.web.v1_0.controller.display.controls;

import org.bahmni.module.bahmnicore.service.BahmniDrugOrderService;
import org.bahmni.module.bahmnicore.web.v1_0.mapper.DrugOrderToRegimenMapper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.openmrs.DrugOrder;
import org.openmrs.Order;
import org.openmrs.module.bahmniemrapi.drugogram.contract.Regimen;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.ArrayList;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

@RunWith(PowerMockRunner.class)
public class DrugOGramControllerTest {
    @Mock
    private BahmniDrugOrderService bahmniDrugOrderService;
    @Mock
    private DrugOrderToRegimenMapper drugOrderToRegimenMapper;

    private DrugOGramController drugOGramController;

    @Before
    public void setUp() throws Exception {
        drugOGramController = new DrugOGramController(bahmniDrugOrderService, drugOrderToRegimenMapper);
    }

    @Test
    public void shouldFetchDrugsAsRegimen() throws Exception {
        ArrayList<Order> drugOrders = new ArrayList<>();
        when(bahmniDrugOrderService.getAllDrugOrders("patientUuid")).thenReturn(drugOrders);
        Regimen expected = new Regimen();
        when(drugOrderToRegimenMapper.map(drugOrders)).thenReturn(expected);

        Regimen actual = drugOGramController.getRegimen("patientUuid");

        verify(bahmniDrugOrderService, times(1)).getAllDrugOrders("patientUuid");
        verify(drugOrderToRegimenMapper, times(1)).map(drugOrders);
        assertEquals(expected, actual);
        assertEquals(0, expected.getHeaders().size());
    }
}