package org.openmrs.module.bahmniemrapi.encountertransaction.service;

import org.bahmni.module.bahmnicore.dao.ObsDao;
import org.bahmni.test.builder.ConceptBuilder;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.Concept;
import org.openmrs.module.bahmniemrapi.encountertransaction.contract.BahmniObservation;
import org.openmrs.module.bahmniemrapi.encountertransaction.service.impl.BahmniObsServiceImpl;
import org.openmrs.web.test.BaseModuleWebContextSensitiveTest;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
@org.springframework.test.context.ContextConfiguration(locations = {"classpath:TestingApplicationContext.xml"}, inheritLocations = true)
public class BahmniObsServiceImplIT extends BaseModuleWebContextSensitiveTest {

    BahmniObsService personObsService;
    
    @Autowired
    ObsDao obsDao;

    @Before
    public void setUp() throws Exception {
        personObsService = new BahmniObsServiceImpl(obsDao);
        executeDataSet("observationsTestData.xml");
    }

    @Test
    public void shouldReturnLatestObsForEachConcept() {
        List<BahmniObservation> bahmniObservations = personObsService.getLatest("86526ed5-3c11-11de-a0ba-001e378eb67a", Arrays.asList("Vitals"));
        BahmniObservation vitalObservation = bahmniObservations.get(0);
        List<BahmniObservation> vitalsGroupMembers = vitalObservation.getGroupMembers();
        assertEquals(2, vitalsGroupMembers.size());

        BahmniObservation weight = vitalsGroupMembers.get(0);
        BahmniObservation pulse = vitalsGroupMembers.get(1);
        assertEquals("Weight", weight.getConcept().getName());
        assertEquals("Pulse", pulse.getConcept().getName());
    }

    @Test
    public void return_orphaned_obs_for_patient() throws Exception {
        Concept bloodPressureConcept = new ConceptBuilder().withName("Blood Pressure").build();
        List<BahmniObservation> obsForConceptSet = personObsService.observationsFor("86526ed5-3c11-11de-a0ba-001e378eb67a", Arrays.asList(bloodPressureConcept), null);
        assertEquals(1, obsForConceptSet.size());
        List<BahmniObservation> bloodPressureMembers = obsForConceptSet.get(0).getGroupMembers();
        assertEquals(2, bloodPressureMembers.size());
        List<BahmniObservation> systolicMembers = bloodPressureMembers.get(0).getGroupMembers();
        List<BahmniObservation> diastolicMembers = bloodPressureMembers.get(1).getGroupMembers();
        assertEquals(2, systolicMembers.size());
        assertEquals(2, diastolicMembers.size());
    }
}
