package org.bahmni.module.bahmnicore.contract.patient.search;

import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;


public class PatientAttributeQueryHelperTest {

    @Test
    public void codedPersonAttributeShouldReturnConceptName() {
        PatientAttributeQueryHelper patientAttributeQueryHelper = new PatientAttributeQueryHelper("", Arrays.asList(new Integer(1), 2), Arrays.asList(1));
        String updatedSelectClause = patientAttributeQueryHelper.appendToJoinClause("''");
    }

    @Test
    public void ensureSelectClauseReturnsProperSqlForAttributes() {
        PatientAttributeQueryHelper PatientAttributeQueryHelper = new PatientAttributeQueryHelper("", Arrays.asList(new Integer(1), 2), Arrays.asList(1));

        String updatedSelectClause = PatientAttributeQueryHelper.selectClause("a");
        assertNotNull(updatedSelectClause);
        assertEquals("a,concat('{',group_concat(DISTINCT (coalesce(concat('\"',attrt_results.name,'\":\"', REPLACE(REPLACE(coalesce(cn.name, def_loc_cn.name, pattr_results.value),'\\\\','\\\\\\\\'),'\"','\\\\\"'),'\"'))) SEPARATOR ','),'}') as customAttribute", updatedSelectClause);
    }

    @Test
    public void ensureAppendJoinClauseReturnsProperSqlForPersonAttributeConceptTypes() {
        PatientAttributeQueryHelper PatientAttributeQueryHelper = new PatientAttributeQueryHelper("", Arrays.asList(new Integer(1), 2),  Arrays.asList(1));

        String updatedSelectClause =  PatientAttributeQueryHelper.appendToJoinClause("a");
        assertNotNull(updatedSelectClause);
        assertEquals("a LEFT OUTER JOIN person_attribute pattrln on pattrln.person_id = p.person_id and pattrln.person_attribute_type_id in (1,2)  LEFT OUTER JOIN person_attribute pattr_results on pattr_results.person_id = p.person_id and pattr_results.person_attribute_type_id in (1)  LEFT OUTER JOIN person_attribute_type attrt_results on attrt_results.person_attribute_type_id = pattr_results.person_attribute_type_id and pattr_results.voided = 0  LEFT OUTER JOIN concept_name cn on cn.concept_id = pattr_results.value and cn.concept_name_type = 'FULLY_SPECIFIED' and attrt_results.format = 'org.openmrs.Concept' and cn.locale = 'en_GB' LEFT OUTER JOIN concept_name def_loc_cn on def_loc_cn.concept_id = pattr_results.value and def_loc_cn.concept_name_type = 'FULLY_SPECIFIED' and attrt_results.format = 'org.openmrs.Concept' and def_loc_cn.locale = 'en' ", updatedSelectClause);
    }
}