package org.bahmni.module.bahmnicore.contract.patient.search;


import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.hibernate.type.StandardBasicTypes;
import org.hibernate.type.Type;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.apache.commons.lang3.StringUtils.isEmpty;

public class PatientAddressFieldQueryHelper {
	private String addressFieldValue;
	protected String addressFieldName;
	protected String[] addressSearchResultFields;

	public PatientAddressFieldQueryHelper(String addressFieldName,String addressFieldValue, String[] addressResultFields){
		this.addressFieldName = addressFieldName;
		this.addressFieldValue = addressFieldValue;
		this.addressSearchResultFields = addressResultFields;
	}

	public String selectClause(String select){
		String selectClause = ", ''  as addressFieldValue";
		List<String> columnValuePairs = new ArrayList<>();

		if (addressSearchResultFields != null) {
			for (String field : addressSearchResultFields)
				if (!"{}".equals(field)) columnValuePairs.add(String.format("\"%s\" : ' , '\"' , IFNULL(pa.%s ,''), '\"'", field, field));

			if(columnValuePairs.size() > 0)
				selectClause = String.format(",CONCAT ('{ %s , '}') as addressFieldValue",
					StringUtils.join(columnValuePairs.toArray(new String[columnValuePairs.size()]), ", ',"));
		}

		return select + selectClause;
	}

	public String appendToWhereClause(String where){
		if (isEmpty(addressFieldValue)) {
			return where;
		}
		return combine(where, "and", enclose(" " +addressFieldName+ " like '%" + StringEscapeUtils.escapeSql(addressFieldValue) + "%'"));

	}

	protected String combine(String query, String operator, String condition) {
		return String.format("%s %s %s", query, operator, condition);
	}

	protected String enclose(String value) {
		return String.format("(%s)", value);
	}

	public Map<String,Type> addScalarQueryResult(){
		Map<String,Type> scalarQueryResult = new HashMap<>();
		scalarQueryResult.put("addressFieldValue", StandardBasicTypes.STRING);
		return scalarQueryResult;
	}

	public String appendToGroupByClause(String fieldName) {
		if(isEmpty(fieldName)) return  addressFieldName;
		return addressFieldName + ", " + fieldName;
	}
}
