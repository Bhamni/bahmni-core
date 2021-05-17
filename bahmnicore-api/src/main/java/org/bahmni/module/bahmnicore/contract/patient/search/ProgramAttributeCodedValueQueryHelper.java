package org.bahmni.module.bahmnicore.contract.patient.search;

import org.apache.commons.lang3.StringUtils;
import org.bahmni.module.bahmnicore.util.SqlQueryHelper;

public class ProgramAttributeCodedValueQueryHelper extends PatientProgramAttributeQueryHelper  {

	public ProgramAttributeCodedValueQueryHelper(String patientProgramAttributeValue, Integer programAttributeTypeId) {
		super(patientProgramAttributeValue, programAttributeTypeId);
	}

	public String selectClause(String select){
			return select + ", " +
					"concat('{',group_concat(DISTINCT (coalesce(concat('\"',ppt.name,'\":\"', cn.name,'\"'))) SEPARATOR ','),'}') AS patientProgramAttributeValue";
	}

	public String appendToJoinClause(String join){

			return join + " left outer join patient_program pp on p.person_id = pp.patient_id and pp.voided=0"
					+ " left outer join patient_program_attribute ppa on pp.patient_program_id = ppa.patient_program_id and ppa.voided=0"
					+ " left outer join program_attribute_type ppt on ppa.attribute_type_id = ppt.program_attribute_type_id and ppa.attribute_type_id =" + programAttributeTypeId.intValue()
					+ " LEFT OUTER JOIN concept_name cn on ppa.value_reference = cn.concept_id and cn.voided=0";

	}

	public String appendToWhereClause(String where){
		if(StringUtils.isEmpty(patientProgramAttributeValue)){
			return where;
		}

		return combine(where, "and",
				enclose(" cn.name like '%"
						+ SqlQueryHelper.escapeSQL(patientProgramAttributeValue, true, null)
						+ "%' and ppa.attribute_type_id =" + programAttributeTypeId.intValue()));

	}

}
