package org.bahmni.module.bahmnicore.contract.patient.search;

import org.bahmni.module.bahmnicore.util.SqlQueryHelper;

import static org.apache.commons.lang.StringUtils.isEmpty;

public class PatientIdentifierQueryHelper {

	private String identifier;
	private final Boolean filterOnAllIdentifiers;

	public PatientIdentifierQueryHelper(String identifier, Boolean filterOnAllIdentifiers) {
		this.identifier = identifier;
		this.filterOnAllIdentifiers = filterOnAllIdentifiers;
	}

	public String appendToJoinClause(String join) {
		if (isEmpty(identifier)) {
			return join;
		}
		String extraIdentifierQuery = filterOnAllIdentifiers ? ", 'bahmni.extraPatientIdentifierTypes'":"";
		String query = " JOIN (" +
				"SELECT pi.patient_id " +
				"FROM patient_identifier pi " +
				" JOIN patient_identifier_type pit ON pi.identifier_type = pit.patient_identifier_type_id AND pi.voided IS FALSE " +
				" JOIN global_property gp ON gp.property IN ('bahmni.primaryIdentifierType' "+extraIdentifierQuery+")" +
				" AND gp.property_value LIKE concat('%', pit.uuid, '%')" +
				" AND pi.identifier LIKE '%" + SqlQueryHelper.escapeSQL(identifier, true, null) + "%' GROUP BY pi.patient_id) " +
				" AS matched_patient ON matched_patient.patient_id = p.person_id";
		return join + query;
	}
}
