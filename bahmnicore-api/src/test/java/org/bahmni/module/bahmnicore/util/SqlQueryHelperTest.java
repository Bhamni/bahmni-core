package org.bahmni.module.bahmnicore.util;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.openmrs.api.AdministrationService;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

public class SqlQueryHelperTest {

    @Mock
    private AdministrationService administrationService;
    private SqlQueryHelper sqlQueryHelper;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        sqlQueryHelper = new SqlQueryHelper();
    }

    @Test
    public void shouldReturnQueryParamsInOrder(){
        String queryString ="select * from encounter where date_started=${en_date_started} AND visit_id=${en_visit_id} and patient_id=${en_patient_id}";
        List<String> paramNamesFromPlaceHolders = sqlQueryHelper.getParamNamesFromPlaceHolders(queryString);
        assertEquals("en_date_started",paramNamesFromPlaceHolders.get(0));
        assertEquals("en_visit_id",paramNamesFromPlaceHolders.get(1));
        assertEquals("en_patient_id",paramNamesFromPlaceHolders.get(2));
    }

    @Test
    public void shouldTransformQueryIntoPreparedStatementFormat(){
        String queryString ="select * from encounter where date_started=${en_date_started} AND visit_id=${en_visit_id} and patient_id=${en_patient_id}";
        String expectQueryString = "select * from encounter where date_started=? AND visit_id=? and patient_id=?";
        String result = sqlQueryHelper.transformIntoPreparedStatementFormat(queryString);
        assertEquals(expectQueryString,result);
    }

    @Test
    public void shouldParseAdditionalParams(){
        String queryString ="SELECT *\n" +
                "FROM person p\n" +
                "  INNER JOIN person_name pn ON pn.person_id = p.person_id\n" +
                "  INNER join (SELECT * FROM obs\n" +
                "              WHERE concept_id IN\n" +
                "                                   (SELECT concept_id\n" +
                "                                    FROM concept_name cn cn.name in (${testName}))  as tests on tests.person_id = p.person_id";
        String additionalParams = "{\"tests\": \"'HIV (Blood)','Gram Stain (Sputum)'\"}";

        when(administrationService.getGlobalProperty("emrapi.sqlSearch.additionalSearchHandler")).thenReturn(" cn.name = '${testName}'");

        String expectedQueryString ="SELECT *\n" +
                "FROM person p\n" +
                "  INNER JOIN person_name pn ON pn.person_id = p.person_id\n" +
                "  INNER join (SELECT * FROM obs\n" +
                "              WHERE concept_id IN\n" +
                "                                   (SELECT concept_id\n" +
                "                                    FROM concept_name cn cn.name in ('HIV (Blood)','Gram Stain (Sputum)'))  as tests on tests.person_id = p.person_id";
        String result = sqlQueryHelper.parseAdditionalParams(additionalParams, queryString);

        assertEquals(expectedQueryString,result);
    }

    @Test
    public void shouldEscapeSQLInjection() {
        assertEquals("0X3", SqlQueryHelper.escapeSQL("0x3", true, null));
        assertEquals("DROP sampletable\\;--", SqlQueryHelper.escapeSQL("DROP sampletable;--", true, null));
        assertEquals("admin\\'--", SqlQueryHelper.escapeSQL("admin'--", true, null));
        assertEquals("admin\\'\\\\/*", SqlQueryHelper.escapeSQL("admin'/*", true, null));
    }
}
