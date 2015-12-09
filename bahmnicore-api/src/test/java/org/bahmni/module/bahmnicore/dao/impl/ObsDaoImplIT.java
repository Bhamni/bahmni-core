package org.bahmni.module.bahmnicore.dao.impl;

import org.bahmni.module.bahmnicore.BaseIntegrationTest;
import org.bahmni.module.bahmnicore.dao.ObsDao;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.Obs;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

}