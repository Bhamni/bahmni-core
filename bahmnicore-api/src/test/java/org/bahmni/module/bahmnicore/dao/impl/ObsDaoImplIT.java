package org.bahmni.module.bahmnicore.dao.impl;

import org.bahmni.module.bahmnicore.BaseIntegrationTest;
import org.bahmni.module.bahmnicore.dao.ObsDao;
import org.bahmni.module.bahmnicore.util.BahmniDateUtil;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.Concept;
import org.openmrs.Obs;
import org.openmrs.api.context.Context;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;

import static org.junit.Assert.assertEquals;

public class ObsDaoImplIT extends BaseIntegrationTest {
    
    @Autowired
    ObsDao obsDao;

    Map<Integer, Integer> conceptToObsMap = new HashMap<>();

    @Before
    public void setUp() throws Exception {
        executeDataSet("obsTestData.xml");
        conceptToObsMap.put(9012, 5);
        conceptToObsMap.put(9011, 4);
    }

    @Test
    public void shouldGetLatestObsForConceptSetByVisit() {
        List<Obs> obsList = obsDao.getLatestObsForConceptSetByVisit("86526ed5-3c11-11de-a0ba-001e378eb67a", "Breast Cancer Intake", 901);
        assertEquals(2, obsList.size());
        for (Obs obs : obsList) {
            assertEquals("for concept : " + obs.getConcept().getName().getName(), conceptToObsMap.get(obs.getConcept().getId()), obs.getId());
        }
    }

    @Test
    public void shouldNotRetrieveIfObservationMadeInADifferentTemplate() {
        List<Obs> obsList = obsDao.getLatestObsForConceptSetByVisit("86526ed5-3c11-11de-a0ba-001e378eb67a", "Breast Cancer Progress", 901);
        assertEquals(2, obsList.size());
    }

    @Test
    public void shouldRetrieveObservationsForAnOrder() throws Exception {
        assertEquals(1, obsDao.getObsForOrder("5145792e-feb5-11e4-ae7f-080027b662ec").size());

        List<Obs> obsForOrder = obsDao.getObsForOrder("129de0a3-05c4-444a-be03-e01b4c4b2419");
        assertEquals(5, obsForOrder.size());
        assertEquals((Integer)12, obsForOrder.get(0).getId());
        assertEquals((Integer)11, obsForOrder.get(1).getId());
        assertEquals((Integer)10, obsForOrder.get(2).getId());
        assertEquals((Integer)8, obsForOrder.get(3).getId());
        assertEquals((Integer)7, obsForOrder.get(4).getId());

        assertEquals(0, obsDao.getObsForOrder("some-random-uuid").size());
    }

    @Test
    public void shouldRetrieveObservationWithinProgramsDateRange() throws Exception {
        String rootConceptName = "Breast Cancer Intake";
        String childConceptName = "Histopathology";
        String patientUUid = "86526ed5-3c11-11de-a0ba-001e378eb67a";
        Date startDate = BahmniDateUtil.convertToDate("2008-08-18T15:00:01.000", BahmniDateUtil.DateFormatType.UTC);
        Concept rootConcept = Context.getConceptService().getConceptByName(rootConceptName);
        Concept childConcept = Context.getConceptService().getConceptByName(childConceptName);
        List<Integer> listOfVisitIds = new ArrayList<Integer>();
        listOfVisitIds.add(902);
        rootConcept.getName().getName();

        List<Obs> bahmniObservations = obsDao.getObsFor(patientUUid, rootConcept, childConcept,listOfVisitIds, startDate, null);

        assertEquals(1, bahmniObservations.size());
        assertEquals(rootConceptName, bahmniObservations.get(0).getConcept().getName().getName());
        assertEquals(3, bahmniObservations.get(0).getGroupMembers(true).size());
    }

}