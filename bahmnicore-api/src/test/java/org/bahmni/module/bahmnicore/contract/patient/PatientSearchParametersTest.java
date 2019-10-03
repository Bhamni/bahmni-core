package org.bahmni.module.bahmnicore.contract.patient;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.openmrs.module.webservices.rest.web.RequestContext;

import javax.servlet.http.HttpServletRequest;
import java.sql.Timestamp;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;


public class PatientSearchParametersTest {

    @Mock
    RequestContext requestContext;

    @Mock
    HttpServletRequest request;

    @Before
    public void setup() {
        initMocks(this);
        when(requestContext.getRequest()).thenReturn(request);
    }

    @Test
    public void shouldIgnoreEmptyBirthdate () {
        when(requestContext.getParameter("birthdate")).thenReturn("");
        PatientSearchParameters patientSearchParameters = new PatientSearchParameters(requestContext);

        assertNull(patientSearchParameters.getBirthdate());
    }

    @Test
    public void shouldParseBirthdateFromStringAndSetToMidnight () {
        when(requestContext.getParameter("birthdate")).thenReturn("1983-01-30");
        PatientSearchParameters patientSearchParameters = new PatientSearchParameters(requestContext);

        assertEquals(Timestamp.valueOf("1983-01-30 00:00:00"), patientSearchParameters.getBirthdate());
    }
}