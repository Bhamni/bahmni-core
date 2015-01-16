package org.bahmni.module.bahmnicore.service.impl;

import org.bahmni.module.bahmnicore.dao.ObsDao;
import org.bahmni.module.bahmnicore.service.BahmniObsService;
import org.bahmni.test.builder.ConceptBuilder;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.Concept;
import org.openmrs.api.ConceptService;
import org.openmrs.module.bahmniemrapi.encountertransaction.contract.BahmniObservation;
import org.openmrs.web.test.BaseModuleWebContextSensitiveTest;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import static org.junit.Assert.assertEquals;
@org.springframework.test.context.ContextConfiguration(locations = {"classpath:TestingApplicationContext.xml"}, inheritLocations = true)
public class BahmniObsServiceImplIT extends BaseModuleWebContextSensitiveTest {

    @Autowired
    BahmniObsService personObsService;
    @Autowired
    private ConceptService conceptService;

    @Autowired
    ObsDao obsDao;

    @Before
    public void setUp() throws Exception {
        executeDataSet("diagnosisMetadata.xml");
        executeDataSet("dispositionMetadata.xml");
        executeDataSet("observationsTestData.xml");
    }

    @Test
    public void shouldReturnLatestObsForEachConcept() {
        Concept vitalsConcept = conceptService.getConceptByName("Vitals");
        Collection<BahmniObservation> bahmniObservations = personObsService.getLatest("86526ed5-3c11-11de-a0ba-001e378eb67a", Arrays.asList(vitalsConcept), true);
        BahmniObservation vitalObservation = bahmniObservations.iterator().next();
        Collection<BahmniObservation> vitalsGroupMembers = vitalObservation.getGroupMembers();
        assertEquals(2, vitalsGroupMembers.size());
        Iterator<BahmniObservation> observationIterator = vitalsGroupMembers.iterator();

        BahmniObservation weight = observationIterator.next();
        BahmniObservation pulse = observationIterator.next();
        assertEquals("Pulse", pulse.getConcept().getName());
        assertEquals("Weight", weight.getConcept().getName());
    }

    @Test
    public void return_orphaned_obs_for_patient() throws Exception {
        Concept bloodPressureConcept = new ConceptBuilder().withName("Blood Pressure").build();
        Collection<BahmniObservation> obsForConceptSet = personObsService.observationsFor("86526ed5-3c11-11de-a0ba-001e378eb67a", Arrays.asList(bloodPressureConcept), null);
        assertEquals(1, obsForConceptSet.size());
        Collection<BahmniObservation> bloodPressureMembers = obsForConceptSet.iterator().next().getGroupMembers();
        Iterator<BahmniObservation> bloodPressureMembersIterator = bloodPressureMembers.iterator();
        assertEquals(2, bloodPressureMembers.size());
        Collection<BahmniObservation> systolicMembers = bloodPressureMembersIterator.next().getGroupMembers();
        Collection<BahmniObservation> diastolicMembers = bloodPressureMembersIterator.next().getGroupMembers();
        assertEquals(2, systolicMembers.size());
        assertEquals(2, diastolicMembers.size());
    }

    @Test
    public void shouldReturnObsForAllConceptForGivenVisit() {
        List<BahmniObservation> bahmniObservations = (List<BahmniObservation>) personObsService.getObservationForVisit("ad41fb41-a41a-4ad6-8835-2f59099acf5b", null);
        assertEquals(2, bahmniObservations.size());
        assertEquals(1, bahmniObservations.get(0).getGroupMembers().size());
        assertEquals(2, bahmniObservations.get(1).getGroupMembers().size());
    }

    @Test
    public void shouldReturnObsForGivenConceptForGivenVisit() {
        Collection<BahmniObservation> bahmniObservations = personObsService.getObservationForVisit("ad41fb41-a41a-4ad6-8835-2f59099acf5b", Arrays.asList("Systolic", "Diastolic"));
        assertEquals(2, bahmniObservations.size());
    }
}
