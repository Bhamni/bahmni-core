package org.bahmni.module.admin.csv.persister;

import org.bahmni.csv.Messages;
import org.bahmni.module.admin.BaseIntegrationTest;
import org.bahmni.module.admin.csv.models.LabResultRow;
import org.bahmni.module.admin.csv.models.LabResultsRow;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.openmrs.CareSetting;
import org.openmrs.Encounter;
import org.openmrs.EncounterProvider;
import org.openmrs.Order;
import org.openmrs.Patient;
import org.openmrs.Visit;
import org.openmrs.api.PatientService;
import org.openmrs.api.VisitService;
import org.openmrs.api.context.Context;
import org.openmrs.api.context.UserContext;
import org.openmrs.module.bahmniemrapi.laborder.contract.LabOrderResult;
import org.openmrs.module.bahmniemrapi.laborder.service.LabOrderResultsService;
import org.openmrs.test.TestUtil;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class LabResultPersisterIT extends BaseIntegrationTest {
    
    @Autowired
    private VisitService visitService;

    @Autowired
    private PatientService patientService;

    @Autowired
    private LabResultPersister labResultPersister;

    @Autowired
    private LabOrderResultsService labOrderResultsService;
    private UserContext userContext;

    @Before
    public void setUp() throws Exception {
        executeDataSet("baseMetaData.xml");
        executeDataSet("diagnosisMetaData.xml");
        executeDataSet("dispositionMetaData.xml");
        executeDataSet("labResultMetaData.xml");
        executeDataSet("labResult.xml");
        executeDataSet("visitAttributeDataSet.xml");
        Context.authenticate("admin", "test");
        userContext = Context.getUserContext();
        labResultPersister.init(userContext, null, true, "be69741b-29e9-49a1-adc9-2a726e6610e4");
    }

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Test
    public void shouldThrowExceptionIfLabResultsHaveDecimalInANonAllowDecimalConcept() throws Exception {
        String visitType = "LAB RESULT IMPORT VISIT";
        LabResultsRow labResultsRow = new LabResultsRow();
        labResultsRow.setPatientIdentifier("GAN200001").setTestDateString("2014-10-11").setVisitType(visitType);
        labResultsRow.setTestResults(Arrays.asList(new LabResultRow().setTest("Eosinophil (Blood)").setResult("10.6")));

        expectedException.expect(org.openmrs.api.APIException.class);
        expectedException.expectMessage("Decimal is not allowed for Eosinophil (Blood) concept");

        labResultPersister.persist(labResultsRow);
    }

    @Test
    public void shouldSetCodedValueOfATest() throws Exception {
        String visitType = "LAB RESULT IMPORT VISIT";
        LabResultsRow labResultsRow = new LabResultsRow();
        labResultsRow.setPatientIdentifier("GAN200001").setTestDateString("2014-10-11").setVisitType(visitType);
        labResultsRow.setTestResults(Collections.singletonList(new LabResultRow().setTest("Proteins").setResult("+")));

        Messages errorMessages = labResultPersister.persist(labResultsRow);

        Patient patient = patientService.getPatientByUuid("75e04d42-3ca8-11e3-bf2b-ab87271c1b75");
        List<Visit> visits = visitService.getVisitsByPatient(patient);
        assertTrue(errorMessages.isEmpty());
        // Assert visit data
        assertEquals(1, visits.size());
        Visit visit = visits.get(0);
        assertEquals(visitType, visit.getVisitType().getName());
        assertEquals(1, visit.getEncounters().size());
        assertEquals(TestUtil.createDateTime("2014-10-11"), visit.getStartDatetime());
        assertEquals(TestUtil.createDateTime("2014-10-11 23:59:59"), visit.getStopDatetime());
        // Assert encounter data
        Encounter encounter = visit.getEncounters().iterator().next();
        assertEquals(1, encounter.getEncounterProviders().size());
        assertEquals(LabResultPersister.LAB_RESULT_ENCOUNTER_TYPE, encounter.getEncounterType().getName());
        assertEquals(TestUtil.createDateTime("2014-10-11"), encounter.getEncounterDatetime());
        final EncounterProvider provider = encounter.getEncounterProviders().iterator().next();
        assertEquals(userContext.getAuthenticatedUser().getId(), provider.getProvider().getPerson().getId());
        // Assert tests orders data
        assertEquals(1, encounter.getOrders().size());
        Order order = encounter.getOrders().iterator().next();
        assertEquals("Proteins", order.getConcept().getName().getName());
        assertEquals(userContext.getAuthenticatedUser().getId(), order.getOrderer().getId());
        assertEquals(TestUtil.createDateTime("2014-10-11"), order.getDateActivated());
        assertEquals(TestUtil.createDateTime("2014-10-11 23:59:59"), order.getAutoExpireDate());
        assertEquals(LabResultPersister.LAB_ORDER_TYPE, order.getOrderType().getName());
        assertEquals(CareSetting.CareSettingType.OUTPATIENT.name(), order.getCareSetting().getName());
        // Assert results data
        List<LabOrderResult> labOrderResults = labOrderResultsService.getAll(patient, visits, Integer.MAX_VALUE).getResults();
        assertEquals(1, labOrderResults.size());
        LabOrderResult labOrderResult = labOrderResults.get(0);
        assertEquals("Proteins", labOrderResult.getTestName());
        assertEquals("+", labOrderResult.getResult());
        assertEquals(TestUtil.createDateTime("2014-10-11"), labOrderResult.getResultDateTime());
        ExpectedException.none();
    }

    @Test
    public void testPersist() throws Exception {
        String visitType = "LAB RESULT IMPORT VISIT";
        LabResultsRow labResultsRow = new LabResultsRow();
        labResultsRow.setPatientIdentifier("GAN200001").setTestDateString("2014-10-11").setVisitType(visitType);
        labResultsRow.setTestResults(Arrays.asList(new LabResultRow().setTest("Urea Nitorgen").setResult("10")));

        Messages errorMessages = labResultPersister.persist(labResultsRow);

        Patient patient = patientService.getPatientByUuid("75e04d42-3ca8-11e3-bf2b-ab87271c1b75");
        List<Visit> visits = visitService.getVisitsByPatient(patient);
        assertTrue(errorMessages.isEmpty());
        // Assert visit data
        assertEquals(1, visits.size());
        Visit visit = visits.get(0);
        assertEquals(visitType, visit.getVisitType().getName());
        assertEquals(1, visit.getEncounters().size());
        assertEquals(TestUtil.createDateTime("2014-10-11"), visit.getStartDatetime());
        assertEquals(TestUtil.createDateTime("2014-10-11 23:59:59"), visit.getStopDatetime());
        // Assert encounter data
        Encounter encounter = visit.getEncounters().iterator().next();
        assertEquals(1, encounter.getEncounterProviders().size());
        assertEquals(LabResultPersister.LAB_RESULT_ENCOUNTER_TYPE, encounter.getEncounterType().getName());
        assertEquals(TestUtil.createDateTime("2014-10-11"), encounter.getEncounterDatetime());
        final EncounterProvider provider = encounter.getEncounterProviders().iterator().next();
        assertEquals(userContext.getAuthenticatedUser().getId(), provider.getProvider().getPerson().getId());
        // Assert tests orders data
        assertEquals(1, encounter.getOrders().size());
        Order order = encounter.getOrders().iterator().next();
        assertEquals("Urea Nitorgen", order.getConcept().getName().getName());
        assertEquals(userContext.getAuthenticatedUser().getId(), order.getOrderer().getId());
        assertEquals(TestUtil.createDateTime("2014-10-11"), order.getDateActivated());
        assertEquals(TestUtil.createDateTime("2014-10-11 23:59:59"), order.getAutoExpireDate());
        assertEquals(LabResultPersister.LAB_ORDER_TYPE, order.getOrderType().getName());
        assertEquals(CareSetting.CareSettingType.OUTPATIENT.name(), order.getCareSetting().getName());
        // Assert results data
        List<LabOrderResult> labOrderResults = labOrderResultsService.getAll(patient, visits, Integer.MAX_VALUE).getResults();
        assertEquals(1, labOrderResults.size());
        LabOrderResult labOrderResult = labOrderResults.get(0);
        assertEquals("Urea Nitorgen", labOrderResult.getTestName());
        assertEquals("10.0", labOrderResult.getResult());
        assertEquals(TestUtil.createDateTime("2014-10-11"), labOrderResult.getResultDateTime());
    }
}