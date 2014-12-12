package org.bahmni.module.bahmnicore.dao.impl;

import org.junit.Before;
import org.junit.Test;
import org.openmrs.Visit;
import org.openmrs.module.bahmniemrapi.visit.dao.VisitDao;
import org.openmrs.test.BaseContextSensitiveTest;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.Assert.assertEquals;

@org.springframework.test.context.ContextConfiguration(locations = {"classpath:TestingApplicationContext.xml", "classpath:webModuleApplicationContext.xml"}, inheritLocations = true)
public class VisitDaoImplIT extends BaseContextSensitiveTest {

    @Autowired
    VisitDao visitDao;

    @Before
    public void setUp() throws Exception {
        executeDataSet("visitTestData.xml");
    }

    @Test
    public void shouldGetLatestObsForConceptSetByVisit() {
        Visit latestVisit = visitDao.getLatestVisit("86526ed5-3c11-11de-a0ba-001e378eb67a", "Weight");
        assertEquals(902, latestVisit.getVisitId().intValue());
    }

}