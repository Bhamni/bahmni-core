package org.bahmni.module.bahmnicore.contract.patient.search;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class PatientNameQueryHelperTest {

	@Test
	public void shouldReturnWhereClauseWhenWildCardParameterWithNull() throws Exception {
		PatientNameQueryHelper patientNameQueryHelper = new PatientNameQueryHelper(null);
		String whereClause = patientNameQueryHelper.appendToWhereClause("where clause");
		assertEquals("where clause", whereClause);
	}

	@Test
	public void shouldReturnWhereClauseWhenWildCardParameterWithEmptyString() throws Exception {
		PatientNameQueryHelper patientNameQueryHelper = new PatientNameQueryHelper("");
		String whereClause = patientNameQueryHelper.appendToWhereClause("where clause");
		assertEquals("where clause", whereClause);
	}

	@Test
	public void shouldReturnWhereClauseWithNameSearchConditionWhenWildCardParameterWithAnyString() throws Exception {
		PatientNameQueryHelper patientNameQueryHelper = new PatientNameQueryHelper("James");
		String whereClause = patientNameQueryHelper.appendToWhereClause("where clause");
		assertEquals("where clause and ( concat_ws(' ',coalesce(pn.given_name), coalesce(pn.middle_name), coalesce(pn.family_name)) like  '%James%')", whereClause);
	}


	@Test
	public void shouldReturnWhereClauseWithNameSearchConditionWhenWildCardParameterWithMultipleStrings() throws Exception {
		PatientNameQueryHelper patientNameQueryHelper = new PatientNameQueryHelper("James Bond");
		String whereClause = patientNameQueryHelper.appendToWhereClause("where clause");
		assertEquals("where clause and ( concat_ws(' ',coalesce(pn.given_name), coalesce(pn.middle_name), coalesce(pn.family_name)) like  '%James%' and  concat_ws(' ',coalesce(pn.given_name), coalesce(pn.middle_name), coalesce(pn.family_name)) like  '%Bond%')", whereClause);
	}

	@Test
	public void shouldReturnWhereClauseWithNameSearchConditionWhenWildCardParameterWithSingleQuote() throws Exception {
		PatientNameQueryHelper patientNameQueryHelper = new PatientNameQueryHelper("James Bo'nd");
		String whereClause = patientNameQueryHelper.appendToWhereClause("where clause");
		assertEquals("where clause and ( concat_ws(' ',coalesce(pn.given_name), coalesce(pn.middle_name), coalesce(pn.family_name)) like  '%James%' and  concat_ws(' ',coalesce(pn.given_name), coalesce(pn.middle_name), coalesce(pn.family_name)) like  '%Bo\\'nd%')", whereClause);

	}

	@Test
	public void shouldReturnWhereClauseWithNameSearchConditionWhenNameContainsMultipleParts() throws Exception {
		PatientNameQueryHelper patientNameQueryHelper = new PatientNameQueryHelper("James Bond");
		String whereClause = patientNameQueryHelper.appendToWhereClause("where clause");
		assertEquals("where clause and ( concat_ws(' ',coalesce(pn.given_name), coalesce(pn.middle_name), coalesce(pn.family_name)) like  '%James%' and  concat_ws(' ',coalesce(pn.given_name), coalesce(pn.middle_name), coalesce(pn.family_name)) like  '%Bond%')", whereClause);

	}
}
