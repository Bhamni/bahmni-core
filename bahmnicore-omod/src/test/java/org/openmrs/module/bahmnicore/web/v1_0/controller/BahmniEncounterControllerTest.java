package org.openmrs.module.bahmnicore.web.v1_0.controller;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.openmrs.module.bahmniemrapi.encountertransaction.contract.BahmniEncounterTransaction;
import org.openmrs.module.bahmniemrapi.encountertransaction.mapper.BahmniEncounterTransactionMapper;
import org.openmrs.module.emrapi.encounter.EmrEncounterService;
import org.openmrs.module.emrapi.encounter.EncounterSearchParameters;
import org.openmrs.module.emrapi.encounter.domain.EncounterTransaction;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

public class BahmniEncounterControllerTest {
    @Mock
    private EmrEncounterService emrEncounterService;
    @Mock
    private BahmniEncounterTransactionMapper bahmniEncounterTransactionMapper;

    private BahmniEncounterController bahmniEncounterController;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void returns_multiple_encounterTransactions_if_exists() throws Exception {
        ArrayList<EncounterTransaction> encounterTransactions = new ArrayList<>();
        EncounterTransaction et1 = new EncounterTransaction();
        et1.setEncounterUuid("et1");
        EncounterTransaction et2 = new EncounterTransaction();
        et2.setEncounterUuid("et2");
        encounterTransactions.add(et1);
        encounterTransactions.add(et2);
        when(emrEncounterService.find(null)).thenReturn(encounterTransactions);
        when(bahmniEncounterTransactionMapper.map(et1)).thenReturn(new BahmniEncounterTransaction(et1));
        when(bahmniEncounterTransactionMapper.map(et2)).thenReturn(new BahmniEncounterTransaction(et2));

        bahmniEncounterController = new BahmniEncounterController(null, null, null, null, emrEncounterService, null, null, bahmniEncounterTransactionMapper);
        List<BahmniEncounterTransaction> bahmniEncounterTransactions = bahmniEncounterController.find(null);

        assertEquals(2, bahmniEncounterTransactions.size());
        assertEquals(et1.getEncounterUuid(), bahmniEncounterTransactions.get(0).getEncounterUuid());
        assertEquals(et2.getEncounterUuid(), bahmniEncounterTransactions.get(1).getEncounterUuid());
    }

    @Test
    public void should_return_empty_encounter_transaction_if_there_are_no_encounters_exists() throws Exception {
        ArrayList<EncounterTransaction> encounterTransactions = new ArrayList<>();
        when(emrEncounterService.find(null)).thenReturn(null);
        when(bahmniEncounterTransactionMapper.map(any(EncounterTransaction.class))).thenReturn(new BahmniEncounterTransaction(new EncounterTransaction()));

        bahmniEncounterController = new BahmniEncounterController(null, null, null, null, emrEncounterService, null, null, bahmniEncounterTransactionMapper);
        List<BahmniEncounterTransaction> bahmniEncounterTransactions = bahmniEncounterController.find(null);

        assertEquals(1, bahmniEncounterTransactions.size());
        assertNotNull(bahmniEncounterTransactions.get(0));
        assertNull(bahmniEncounterTransactions.get(0).getEncounterUuid());
    }
}