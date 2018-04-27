package org.bahmni.module.bahmnicore.contract.patient.search;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.Collections;

import org.junit.Test;

public class PatientAddressFieldInListQueryHelperTest {

	@Test
	public void shouldReturnWhereClauseWithInStatement() {
		
		PatientAddressFieldQueryHelper helper = new PatientAddressFieldInListQueryHelper("city_village", Arrays.asList("address.bilaspur", "address.jaipur"), null);
		String whereClause = helper.appendToWhereClause("where test='1234'");
		
		assertEquals("where test='1234' and ( city_village in ('address.bilaspur','address.jaipur'))", whereClause);
	}
	
	@Test
	public void shouldReturnEmptyInStatement_whenNullList() {
		
		PatientAddressFieldQueryHelper helper = new PatientAddressFieldInListQueryHelper("city_village", null, null);
		String whereClause = helper.appendToWhereClause("where test='1234'");
		
		assertEquals("where test='1234' and ( city_village in (" + PatientAddressFieldInListQueryHelper.EMPTY_LIST + "))", whereClause);
	}
	
	@Test
	public void shouldReturnEmptyInStatement_whenEmptyList() {
		
		PatientAddressFieldQueryHelper helper = new PatientAddressFieldInListQueryHelper("city_village", Collections.emptyList(), null);
		String whereClause = helper.appendToWhereClause("where test='1234'");
		
		assertEquals("where test='1234' and ( city_village in (" + PatientAddressFieldInListQueryHelper.EMPTY_LIST + "))", whereClause);
	}
}