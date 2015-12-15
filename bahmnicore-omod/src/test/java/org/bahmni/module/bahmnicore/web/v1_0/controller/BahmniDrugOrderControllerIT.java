package org.bahmni.module.bahmnicore.web.v1_0.controller;

import org.bahmni.module.bahmnicore.service.BahmniDrugOrderService;
import org.bahmni.module.bahmnicore.web.v1_0.BaseIntegrationTest;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.DrugOrder;
import org.openmrs.Order;
import org.openmrs.module.bahmniemrapi.drugorder.contract.BahmniDrugOrder;
import org.openmrs.module.emrapi.encounter.domain.EncounterTransaction;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

public class BahmniDrugOrderControllerIT extends BaseIntegrationTest {

    @Autowired
    private BahmniDrugOrderController bahmniDrugOrderController;

    @Autowired
    private BahmniDrugOrderService bahmniDrugOrderService;

    @Before
    public void setUp() throws Exception {
        executeDataSet("diagnosisMetadata.xml");
        executeDataSet("dispositionMetadata.xml");
        executeDataSet("drugOrdersForVisits.xml");
    }

    @Test
    public void shouldReturnDrugOrdersWithoutJavaAssistOrderProxy() throws Exception{
        executeDataSet("activeOrderTests.xml");
        List<DrugOrder> drugOrders = bahmniDrugOrderService.getActiveDrugOrders("c475abf0-59d7-4bfe-8d73-57604a17e519");
        for(Order order: drugOrders){
            //This issue happened in Possible when the Order that is returned is a javassist proxy instead of a DrugOrder and it returned a ClassCastException
            if(!(order instanceof DrugOrder)){
                fail("The Order ["+order+"] is not an instance of drugOrder");
            }
        }
        assertEquals(3,drugOrders.size());
    }

    @Test
    public void shouldReturnVisitWisePrescribedAndOtherActiveOrdersInOrderOfStartDate() throws Exception {
        executeDataSet("prescribedAndActiveDrugOrdersForVisits.xml");
        Map<String, Collection<BahmniDrugOrder>> drugOrders = bahmniDrugOrderController.getVisitWisePrescribedAndOtherActiveOrders("1a246ed5-3c11-11de-a0ba-001ed98eb67a", 1, true, new ArrayList(), null, null);
        assertEquals(2, drugOrders.keySet().size());

        assertEquals(1, drugOrders.get("visitDrugOrders").size());
        assertEquals("92c1bdef-72d4-49d9-8a1f-804892f44acf", drugOrders.get("visitDrugOrders").iterator().next().getUuid());

        assertEquals(1, drugOrders.get("Other Active DrugOrders").size());
        assertEquals("92c1bdef-72d4-77d9-8a1f-80411ac77abe", drugOrders.get("Other Active DrugOrders").iterator().next().getUuid());

    }

    @Test
    public void shouldReturnVisitWisePrescribedAndOtherActiveOrdersByVisitUuid() throws Exception {
        executeDataSet("prescribedAndActiveDrugOrdersForVisits.xml");
        Map<String, Collection<BahmniDrugOrder>> drugOrders = bahmniDrugOrderController.getVisitWisePrescribedAndOtherActiveOrders("1a246ed5-3c11-11de-a0ba-001ed98eb67a", 1, true, Arrays.asList("c809162f-dc55-4814-be3f-33d23c8abc1d"),null ,null );
        assertEquals(2, drugOrders.keySet().size());

        assertEquals(1, drugOrders.get("visitDrugOrders").size());
        assertEquals("92c1bdef-72d4-49d9-8a1f-804892f44acf", drugOrders.get("visitDrugOrders").iterator().next().getUuid());

        assertEquals(1, drugOrders.get("Other Active DrugOrders").size());
        assertEquals("92c1bdef-72d4-77d9-8a1f-80411ac77abe", drugOrders.get("Other Active DrugOrders").iterator().next().getUuid());
    }

    @Test
    public void shouldReturnVisitWisePrescribedWithoutOtherActiveOrdersInOrderOfStartDate() throws Exception {
        executeDataSet("prescribedAndActiveDrugOrdersForVisits.xml");
        Map<String, Collection<BahmniDrugOrder>> drugOrders = bahmniDrugOrderController.getVisitWisePrescribedAndOtherActiveOrders("1a246ed5-3c11-11de-a0ba-001ed98eb67a", 2, true, new ArrayList(), null, null);
        assertEquals(2, drugOrders.keySet().size());

        assertEquals(5, drugOrders.get("visitDrugOrders").size());
        Iterator<BahmniDrugOrder> drugOrderIterator = drugOrders.get("visitDrugOrders").iterator();
        assertEquals("92c1bdef-72d4-77d9-8a1f-80411ac77abe", drugOrderIterator.next().getUuid());
        assertEquals("92c1bdef-72d4-77d9-8a1f-80411ac66abe", drugOrderIterator.next().getUuid());
        assertEquals("92c1bdef-72d4-88d9-8a1f-804892f66abf", drugOrderIterator.next().getUuid());
        assertEquals("92c1bdef-72d4-49d9-8a1f-804892f44acf", drugOrderIterator.next().getUuid());
        assertEquals("92c1bdef-72d4-77d9-8a1f-804892f66aba", drugOrderIterator.next().getUuid());

        assertEquals(0, drugOrders.get("Other Active DrugOrders").size());

        drugOrders = bahmniDrugOrderController.getVisitWisePrescribedAndOtherActiveOrders("1a246ed5-3c11-11de-a0ba-001ed98eb67a", 2, false, new ArrayList(), null, null);
        assertEquals(1, drugOrders.keySet().size());
        assertNull(drugOrders.get("Other Active DrugOrders"));

    }

    @Test
    public void shouldReturnDrugOrdersForSpecifiedNumberOfVisits() throws Exception {
        executeDataSet("drugOrdersForVisits.xml");
        List<BahmniDrugOrder> prescribedDrugOrders = bahmniDrugOrderController.getPrescribedDrugOrders("86526ed5-3c11-11de-a0ba-001ed98eb67a", true, 2);
        assertEquals(5, prescribedDrugOrders.size());

        BahmniDrugOrder drugOrder1 = prescribedDrugOrders.get(0);
        assertEquals("d798916f-210d-4c4e-8978-467d1a969f31", drugOrder1.getVisit().getUuid());
        EncounterTransaction.DosingInstructions dosingInstructions1 = drugOrder1.getDosingInstructions();
        assertEquals("{\"dose\": \"1.5\", \"doseUnits\": \"Tablet\"}", dosingInstructions1.getAdministrationInstructions());
        assertEquals(15, drugOrder1.getDuration(), 0);
        assertEquals("Triomune-30", drugOrder1.getDrug().getName());
        assertEquals("2011-10-24 00:00:00.0", drugOrder1.getEffectiveStartDate().toString());
        assertEquals("2011-11-08 00:00:00.0", drugOrder1.getEffectiveStopDate().toString());

        BahmniDrugOrder drugOrder2 = prescribedDrugOrders.get(1);
        assertEquals("d798916f-210d-4c4e-8978-467d1a969f31", drugOrder2.getVisit().getUuid());
        EncounterTransaction.DosingInstructions dosingInstructions2 = drugOrder2.getDosingInstructions();
        assertEquals(4.5, dosingInstructions2.getDose(), 0);
        assertEquals("Before meals", drugOrder2.getInstructions());
        assertEquals("Take while sleeping", drugOrder2.getCommentToFulfiller());
        assertEquals("1/day x 7 days/week", dosingInstructions2.getFrequency());
        assertEquals("UNKNOWN", dosingInstructions2.getRoute());
        assertEquals(6, drugOrder2.getDuration(), 0);
        assertEquals("Paracetamol 250 mg", drugOrder2.getDrug().getName());
        assertEquals("2011-10-22 00:00:00.0", drugOrder2.getEffectiveStartDate().toString());
        assertEquals("2011-10-30 00:00:00.0", drugOrder2.getEffectiveStopDate().toString());

        BahmniDrugOrder drugOrder3 = prescribedDrugOrders.get(2);
        assertEquals("adf4fb41-a41a-4ad6-8835-2f59889acf5a", drugOrder3.getVisit().getUuid());
        EncounterTransaction.DosingInstructions dosingInstructions3 = drugOrder3.getDosingInstructions();
        assertEquals("{\"dose\": \"5.0\", \"doseUnits\": \"Tablet\"}", dosingInstructions3.getAdministrationInstructions());
        assertEquals("tab (s)", drugOrder3.getDrug().getForm());
        assertEquals(6, drugOrder3.getDuration(), 0);
        assertEquals("Triomune-30", drugOrder3.getDrug().getName());
        assertEquals("2005-09-23 08:00:00.0", drugOrder3.getEffectiveStartDate().toString());
        assertEquals("2005-09-30 00:00:00.0", drugOrder3.getEffectiveStopDate().toString());
        assertEquals(1, drugOrder3.getOrderAttributes().size());
        assertEquals("dispensed", drugOrder3.getOrderAttributes().get(0).getName());
        assertEquals("true", drugOrder3.getOrderAttributes().get(0).getValue());
        assertEquals("be48cdcb-6666-47e3-9f2e-2635032f3a9a", drugOrder3.getOrderAttributes().get(0).getObsUuid());
        assertNotNull(drugOrder3.getOrderAttributes().get(0).getEncounterUuid());

        BahmniDrugOrder drugOrder4 = prescribedDrugOrders.get(3);
        assertEquals("adf4fb41-a41a-4ad6-8835-2f59889acf5a", drugOrder4.getVisit().getUuid());
        EncounterTransaction.DosingInstructions dosingInstructions4 = drugOrder4.getDosingInstructions();
        assertEquals("{\"dose\": \"2.5\", \"doseUnits\": \"Tablet\"}", dosingInstructions4.getAdministrationInstructions());
        assertEquals(6, drugOrder4.getDuration(), 0);
        assertEquals("Triomune-40", drugOrder4.getDrug().getName());
        assertEquals("2005-09-23 00:00:00.0", drugOrder4.getEffectiveStartDate().toString());
        assertEquals("2005-09-29 00:00:00.0", drugOrder4.getEffectiveStopDate().toString());

        BahmniDrugOrder drugOrder5 = prescribedDrugOrders.get(4);
        assertEquals("d798916f-210d-4c4e-8978-467d1a969f31", drugOrder5.getVisit().getUuid());
        assertEquals("FreeTextDrug", drugOrder5.getDrugNonCoded());
        EncounterTransaction.DosingInstructions dosingInstructions5 = drugOrder5.getDosingInstructions();
        assertEquals("{\"dose\": \"1.5\", \"doseUnits\": \"Tablet\"}", dosingInstructions5.getAdministrationInstructions());
        assertEquals(15, drugOrder5.getDuration(), 0);
        assertEquals(null, drugOrder5.getDrug());
        assertEquals("2005-09-23 00:00:00.0", drugOrder5.getEffectiveStartDate().toString());
        assertEquals("2005-09-23 00:00:00.0", drugOrder5.getEffectiveStopDate().toString());
    }


    @Test
    public void shouldNotReturnDrugOrdersIfDrugNamesAreNotPresent() throws Exception {
        executeDataSet("allDrugOrdersForConcepts.xml");
        List<BahmniDrugOrder> prescribedDrugOrders = bahmniDrugOrderController.getDrugOrderDetails("1a246ed5-3c11-3456-wenh-001ed98eb612", null);
        assertEquals(prescribedDrugOrders.size(), 0);
    }

    @Test
    public void shouldReturnAllDrugOrdersForGivenDrugNames() throws Exception {
        executeDataSet("allDrugOrdersForConcepts.xml");
        List<String> drugNames = new ArrayList<>();
        drugNames.add("Acidul");
        drugNames.add("Mepron");
        List<BahmniDrugOrder> prescribedDrugOrders = bahmniDrugOrderController.getDrugOrderDetails("1a246ed5-3c11-3456-wenh-001ed98eb612", drugNames);
        assertEquals(prescribedDrugOrders.size(), 4);
    }

}
