package org.bahmni.module.bahmnicore.util;

import org.junit.Before;
import org.junit.Test;
import org.openmrs.Patient;
import org.openmrs.Visit;
import org.openmrs.api.PatientService;
import org.openmrs.api.VisitService;
import org.openmrs.module.bahmniemrapi.encountertransaction.service.VisitIdentificationHelper;
import org.openmrs.web.test.BaseModuleWebContextSensitiveTest;
import org.springframework.beans.factory.annotation.Autowired;

import java.text.SimpleDateFormat;
import java.util.Date;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@org.springframework.test.context.ContextConfiguration(locations = {"classpath:TestingApplicationContext.xml"}, inheritLocations = true)
public class VisitIdentificationHelperIT extends BaseModuleWebContextSensitiveTest {
    public static final String TEST_VISIT_TYPE = "TEST VISIT TYPE";

    @Autowired
    VisitService visitService;
    @Autowired
    PatientService patientService;

    VisitIdentificationHelper visitIdentificationHelper;

    @Before
    public void setUp() {
        visitIdentificationHelper = new VisitIdentificationHelper(visitService);
    }

    @Test
    public void shouldFetchTheExistingVisit() throws Exception {
        executeDataSet("visitIdentificationHelper.xml");
        Patient patient = patientService.getPatient(1);
        Date accessionDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse("2014-02-11 01:00:00");

        Visit visit = visitIdentificationHelper.getVisitFor(patient, TEST_VISIT_TYPE, accessionDate);
        assertEquals(1, visit.getId().intValue());

        accessionDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse("2014-02-13 01:00:00");
        visit = visitIdentificationHelper.getVisitFor(patient, TEST_VISIT_TYPE, accessionDate);
        assertEquals(2, visit.getId().intValue());
    }

    @Test
    public void shouldInitializeNewVisitWhenNextVisitWithIn24Hours() throws Exception {
        executeDataSet("visitIdentificationHelper.xml");
        Patient patient = patientService.getPatient(1);
        Date accessionDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse("2014-02-13 03:00:00");

        Visit visit = visitIdentificationHelper.getVisitFor(patient, TEST_VISIT_TYPE, accessionDate);
        assertEquals(3, visit.getId().intValue());

        Date stopTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse("2014-02-14 05:00:00");
        assertEquals(accessionDate, visit.getStartDatetime());
        assertEquals(stopTime, visit.getStopDatetime());
    }

    @Test
    public void shouldInitializeNewVisitWhenNextVisitNotWithIn24Hours() throws Exception {
        executeDataSet("visitIdentificationHelper.xml");
        Patient patient = patientService.getPatient(1);
        Date accessionDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse("2014-02-18 03:00:00");
        Date stopTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse("2014-02-18 23:59:59");

        Visit visit = visitIdentificationHelper.getVisitFor(patient, TEST_VISIT_TYPE, accessionDate);

        assertTrue("Setup (visitIdentificationHelper.xml) creates visit ids 1-5. New visit id should be greater than 5", visit.getId() > 5);
        assertEquals(accessionDate, visit.getStartDatetime());
        assertEquals(stopTime, visit.getStopDatetime());
    }

    @Test
    public void shouldInitializeNewVisitWhenNextVisitDoesNotExist() throws Exception {
        executeDataSet("visitIdentificationHelper.xml");
        Patient patient = patientService.getPatient(1);
        Date accessionDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse("2014-02-21 03:00:00");
        Date stopTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse("2014-02-21 23:59:59");

        Visit visit = visitIdentificationHelper.getVisitFor(patient, TEST_VISIT_TYPE, accessionDate);

        assertTrue("Setup (visitIdentificationHelper.xml) creates visit ids 1-5. New visit id should be greater than 5", visit.getId() > 5);
        assertEquals(accessionDate, visit.getStartDatetime());
        assertEquals(stopTime, visit.getStopDatetime());
    }

    @Test
    public void stretch_earlier_visit_when_multiple_visits_for_a_date() throws Exception {
        executeDataSet("visitIdentificationHelper.xml");
        Patient patient = patientService.getPatient(1);
        Date accessionDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse("2014-05-20 00:00:00");

        Visit visit = visitIdentificationHelper.getVisitFor(patient, TEST_VISIT_TYPE, accessionDate);

        assertNotNull(visit);
        assertEquals(accessionDate, visit.getStartDatetime());

        Date stopTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse("2014-05-20 04:00:00");
        assertEquals(stopTime, visit.getStopDatetime());
    }
//        V1	10-Feb	10:00		12-Feb	6:00
//        V2	12-Feb	8:00		13-Feb	2:00
//        V3	13-Feb	6:00		14-Feb	5:00
//        v4  14th feb 6:00
//        v6  20th May 3:00       20th May 4:00
//        v7  20th May 6:00
}