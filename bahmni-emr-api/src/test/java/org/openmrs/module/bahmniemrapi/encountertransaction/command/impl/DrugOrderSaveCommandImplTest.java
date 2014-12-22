package org.openmrs.module.bahmniemrapi.encountertransaction.command.impl;

import org.apache.commons.lang3.time.DateUtils;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.openmrs.Concept;
import org.openmrs.ConceptMap;
import org.openmrs.ConceptMapType;
import org.openmrs.ConceptReferenceTerm;
import org.openmrs.ConceptSource;
import org.openmrs.Duration;
import org.openmrs.OrderFrequency;
import org.openmrs.api.ConceptService;
import org.openmrs.module.bahmniemrapi.drugorder.DrugOrderUtil;
import org.openmrs.module.bahmniemrapi.encountertransaction.contract.BahmniEncounterTransaction;
import org.openmrs.module.emrapi.encounter.builder.DrugOrderBuilder;
import org.openmrs.module.emrapi.encounter.domain.EncounterTransaction;
import org.openmrs.module.emrapi.encounter.service.OrderMetadataService;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

public class DrugOrderSaveCommandImplTest {
    @Mock
    private OrderMetadataService orderMetadataService;

    @Mock
    private ConceptService conceptService;

    public static final String DAY_DURATION_UNIT = "Day";
    public static final String ONCE_A_DAY_CONCEPT_NAME = "Once A Day";
    public static final String SNOMED_CT_DAYS_CODE = "258703001";


    DrugOrderSaveCommandImpl drugOrderSaveCommand;

    @Before
    public void setUp() throws Exception {
        initMocks(this);
        drugOrderSaveCommand = new DrugOrderSaveCommandImpl(orderMetadataService, conceptService);
    }

    private static ConceptMap getConceptMap(String sourceHl7Code, String code, String mapTypeUuid) {
        ConceptMap conceptMap = new ConceptMap();
        ConceptReferenceTerm conceptReferenceTerm = new ConceptReferenceTerm();
        ConceptSource conceptSource = new ConceptSource();
        conceptSource.setHl7Code(sourceHl7Code);
        conceptReferenceTerm.setConceptSource(conceptSource);
        conceptReferenceTerm.setCode(code);
        conceptMap.setConceptReferenceTerm(conceptReferenceTerm);
        ConceptMapType conceptMapType = new ConceptMapType();
        if (mapTypeUuid != null) {
            conceptMapType.setUuid(mapTypeUuid);
        } else {
            conceptMapType.setUuid(ConceptMapType.SAME_AS_MAP_TYPE_UUID);
        }
        conceptMap.setConceptMapType(conceptMapType);
        return conceptMap;
    }

    @Test
    public void shouldSetDatesForDrugOrdersConflictingWithCurrentDateOrders() {
        BahmniEncounterTransaction bahmniEncounterTransaction = new BahmniEncounterTransaction();
        Concept dayConcept = new Concept();
        dayConcept.addConceptMapping(getConceptMap(Duration.SNOMED_CT_CONCEPT_SOURCE_HL7_CODE, Duration.SNOMED_CT_DAYS_CODE,"35543629-7d8c-11e1-909d-c80aa9edcf4e"));

        when(conceptService.getConceptByName(DAY_DURATION_UNIT)).thenReturn(dayConcept);
        OrderFrequency orderFrequency = new OrderFrequency();
        when(orderMetadataService.getDurationUnitsConceptByName(DAY_DURATION_UNIT)).thenReturn(dayConcept);
        when(orderMetadataService.getOrderFrequencyByName("day", false)).thenReturn(orderFrequency);


        List<EncounterTransaction.DrugOrder> drugOrders = new ArrayList<>();
        Date today = new Date();
        EncounterTransaction.DrugOrder drugOrder1 = new DrugOrderBuilder().withDrugUuid("drug-uuid1").withScheduledDate(null).withFrequency(DAY_DURATION_UNIT).build();

        drugOrders.add(drugOrder1);
        EncounterTransaction.DrugOrder drugOrder2 = new DrugOrderBuilder().withDrugUuid("drug-uuid1").withScheduledDate(DateUtils.addDays(today, 10)).withFrequency(DAY_DURATION_UNIT).build();
        drugOrders.add(drugOrder2);
        EncounterTransaction.DrugOrder drugOrder3 = new DrugOrderBuilder().withDrugUuid("drug-uuid1").withScheduledDate(DateUtils.addDays(today, 2)).withFrequency(DAY_DURATION_UNIT).build();
        drugOrders.add(drugOrder3);
        bahmniEncounterTransaction.setDrugOrders(drugOrders);
        BahmniEncounterTransaction updatedEncounterTransaction = drugOrderSaveCommand.update(bahmniEncounterTransaction);
        assertEquals(updatedEncounterTransaction.getDrugOrders().size(),3);


        EncounterTransaction.DrugOrder currentDrugOrder = updatedEncounterTransaction.getDrugOrders().get(0);
        EncounterTransaction.DrugOrder overlappingOrderWithCurrentDateOrder = updatedEncounterTransaction.getDrugOrders().get(2);

        Date expectedStopDateForCurrentOrder = DrugOrderUtil.calculateAutoExpireDate(currentDrugOrder.getDuration(), dayConcept, null, currentDrugOrder.getScheduledDate(), orderMetadataService.getOrderFrequencyByName(currentDrugOrder.getDosingInstructions().getFrequency(), false));
        Date expectedStopDateForOverlappingOrder = DrugOrderUtil.calculateAutoExpireDate(overlappingOrderWithCurrentDateOrder.getDuration(), dayConcept, null, overlappingOrderWithCurrentDateOrder.getScheduledDate(), orderMetadataService.getOrderFrequencyByName(currentDrugOrder.getDosingInstructions().getFrequency(), false));
        assertEquals(currentDrugOrder.getAutoExpireDate(), expectedStopDateForCurrentOrder);
        assertEquals(overlappingOrderWithCurrentDateOrder.getAutoExpireDate(),expectedStopDateForOverlappingOrder);
        assertTrue(currentDrugOrder.getAutoExpireDate().before(overlappingOrderWithCurrentDateOrder.getScheduledDate()));

    }

}
