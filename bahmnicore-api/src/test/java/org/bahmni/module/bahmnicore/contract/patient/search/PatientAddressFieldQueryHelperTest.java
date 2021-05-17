package org.bahmni.module.bahmnicore.contract.patient.search;

import org.hibernate.type.StandardBasicTypes;
import org.hibernate.type.Type;
import org.junit.Test;

import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class PatientAddressFieldQueryHelperTest {

	@Test
	public void shouldReturnWhereClauseWhenAddressFieldValueIsAvailable(){
		PatientAddressFieldQueryHelper patientAddressFieldQueryHelper = new PatientAddressFieldQueryHelper("city_village", "Bilaspur",null);
		String whereClause = patientAddressFieldQueryHelper.appendToWhereClause("where test='1234'");
		assertEquals("where test='1234' and ( pa.city_village like '%Bilaspur%')", whereClause);
	}

	@Test
	public void shouldReturnWhereClauseWhenAddressFieldValueIsNotAvailable(){
		PatientAddressFieldQueryHelper patientAddressFieldQueryHelper = new PatientAddressFieldQueryHelper("city_village", "",null);
		String whereClause = patientAddressFieldQueryHelper.appendToWhereClause("where test='1234'");
		assertEquals("where test='1234'", whereClause);
	}

	@Test
	public void ensureThatScalarQueryResultIsConfigured(){
		PatientAddressFieldQueryHelper patientAddressFieldQueryHelper = new PatientAddressFieldQueryHelper("city_village", "Bilaspur",null);
		Map<String,Type> map = patientAddressFieldQueryHelper.addScalarQueryResult();
		assertTrue(map.containsKey("addressFieldValue"));
		assertEquals(StandardBasicTypes.STRING,map.get("addressFieldValue"));
	}

	@Test
	public void ensureThatGroupByClauseIsConfiguredAndIsNotEmpty(){
		PatientAddressFieldQueryHelper patientAddressFieldQueryHelper = new PatientAddressFieldQueryHelper("city_village", "Bilaspur",null);
		String groupBy = patientAddressFieldQueryHelper.appendToGroupByClause("something");
		assertEquals("city_village, something",groupBy);
	}

	@Test
	public void ensureThatGroupByClauseIsConfiguredAndIsEmpty(){
		PatientAddressFieldQueryHelper patientAddressFieldQueryHelper = new PatientAddressFieldQueryHelper("city_village", "Bilaspur",null);
		String groupBy = patientAddressFieldQueryHelper.appendToGroupByClause("");
		assertEquals(" pa.city_village",groupBy);
	}

	@Test
	public void shouldReturnSelectClauseWithAddressFieldValue(){
		String[] addressSearchResultFields = {"address3", "address1", "address2"};
		PatientAddressFieldQueryHelper patientAddressFieldQueryHelper = new PatientAddressFieldQueryHelper("address1","123",addressSearchResultFields );
		String selectClause = patientAddressFieldQueryHelper.selectClause("select someFields");
		assertEquals("select someFields,CONCAT ('{ \"address3\" : ' , '\"' , IFNULL(pa.address3 ,''), '\"', ',\"address1\" : ' , '\"' , IFNULL(pa.address1 ,''), '\"', ',\"address2\" : ' , '\"' , IFNULL(pa.address2 ,''), '\"' , '}') as addressFieldValue", selectClause);

	}

}
