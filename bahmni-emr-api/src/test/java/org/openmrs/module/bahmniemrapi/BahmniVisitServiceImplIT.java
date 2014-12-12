package org.openmrs.module.bahmniemrapi;

import org.joda.time.DateTime;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.Visit;
import org.openmrs.module.bahmniemrapi.visit.service.BahmniVisitService;
import org.openmrs.web.test.BaseModuleWebContextSensitiveTest;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Date;
import java.util.List;

@org.springframework.test.context.ContextConfiguration(locations = {"classpath:TestingApplicationContext.xml"}, inheritLocations = true)
public class BahmniVisitServiceImplIT extends BaseModuleWebContextSensitiveTest {
    @Before
    public void setUp() throws Exception {
        executeDataSet("visitData.xml");
    }

    @Autowired
    BahmniVisitService bahmniVisitService;

    @Test
    public void getVisitInAParticularDateRange() throws Exception {
        Date date = new DateTime(2014, 11, 16, 0, 0).toDate();
        List<Visit> visits = bahmniVisitService.getVisitOn(date);
        Assert.assertNotNull(visits);
        Assert.assertEquals(1, visits.size());
        Visit visit = visits.get(0);
        Assert.assertEquals("ad41fb41-a41a-4ad6-8835-2f59099acf5b", visit.getUuid());
    }


    @Test
    public void getVisitOnSameDateAsStart() throws Exception {
        Date date = new DateTime(2014, 11, 10, 0, 0).toDate();
        List<Visit> visits = bahmniVisitService.getVisitOn(date);
        Assert.assertNotNull(visits);
        Assert.assertEquals(1, visits.size());
        Visit visit = visits.get(0);
        Assert.assertEquals("ad41fb41-a41a-4ad6-8835-2f59099acf5b", visit.getUuid());
    }

    @Test
    public void getVisitOnSameDateAsEnd() throws Exception {
        Date date = new DateTime(2014, 11, 19, 0, 0).toDate();
        List<Visit> visits = bahmniVisitService.getVisitOn(date);
        Assert.assertNotNull(visits);
        Assert.assertEquals(1, visits.size());
        Visit visit = visits.get(0);
        Assert.assertEquals("ad41fb41-a41a-4ad6-8835-2f59099acf5b", visit.getUuid());
    }


    @Test
    public void getOverlappingVisitComplyingWithTheDate() throws Exception {
        Date date = new DateTime(2010, 11, 13, 0, 0).toDate();
        List<Visit> visits = bahmniVisitService.getVisitOn(date);
        Assert.assertNotNull(visits);
        Assert.assertEquals(2, visits.size());
    }

    @Test
    public void getNoVisitIfNothingMatchingFound() throws Exception {
        Date date = new DateTime(2011, 11, 13, 0, 0).toDate();
        List<Visit> visits = bahmniVisitService.getVisitOn(date);
        Assert.assertEquals(0, visits.size());
    }

}