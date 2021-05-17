package org.bahmni.module.bahmnicore.contract.patient.search;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.bahmni.module.bahmnicore.contract.patient.response.PatientResponse;
import org.bahmni.module.bahmnicore.model.bahmniPatientProgram.ProgramAttributeType;
import org.hibernate.SQLQuery;
import org.hibernate.SessionFactory;
import org.hibernate.transform.Transformers;
import org.hibernate.type.StandardBasicTypes;
import org.hibernate.type.Type;
import org.openmrs.Location;
import org.openmrs.PersonAttributeType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.apache.commons.lang3.StringUtils.isBlank;

public class PatientSearchQueryBuilder {

	private static final Logger log = Logger.getLogger(PatientSearchQueryBuilder.class);

	private String visitJoin = " left outer join visit v on v.patient_id = p.person_id and v.date_stopped is null ";
	private static String VISIT_JOIN = "_VISIT_JOIN_";
	public static final String SELECT_STATEMENT = "select " +
			"p.uuid as uuid, " +
			"p.person_id as personId, " +
			"pn.given_name as givenName, " +
			"pn.middle_name as middleName, " +
			"pn.family_name as familyName, " +
			"p.gender as gender, " +
			"p.birthdate as birthDate, " +
			"p.death_date as deathDate, " +
			"p.date_created as dateCreated, " +
			"v.uuid as activeVisitUuid, " +
			"primary_identifier.identifier as identifier, " +
			"extra_identifiers.identifiers as extraIdentifiers, " +
			"(CASE va.value_reference WHEN 'Admitted' THEN TRUE ELSE FALSE END) as hasBeenAdmitted ";
	public static final String WHERE_CLAUSE = " where p.voided = false and pn.voided = false and pn.preferred=true ";
	public static final String FROM_TABLE = " from person p ";
	public static final String JOIN_CLAUSE = " left join person_name pn on pn.person_id = p.person_id" +
			" left join person_address pa on p.person_id=pa.person_id and pa.voided = false" +
			" JOIN (SELECT identifier, patient_id" +
			"      FROM patient_identifier pi" +
			" JOIN patient_identifier_type pit ON pi.identifier_type = pit.patient_identifier_type_id AND pi.voided IS FALSE AND pit.retired IS FALSE" +
			" JOIN global_property gp ON gp.property = 'bahmni.primaryIdentifierType' AND gp.property_value = pit.uuid" +
			"      GROUP BY pi.patient_id) as primary_identifier ON p.person_id = primary_identifier.patient_id" +
			" LEFT JOIN (SELECT concat('{', group_concat((concat('\"', pit.name, '\":\"', pi.identifier, '\"')) SEPARATOR ','), '}') AS identifiers," +
			"        patient_id" +
			"      FROM patient_identifier pi" +
			"        JOIN patient_identifier_type pit ON pi.identifier_type = pit.patient_identifier_type_id AND pi.voided IS FALSE AND pit.retired IS FALSE "+
			" JOIN global_property gp ON gp.property = 'bahmni.primaryIdentifierType' AND gp.property_value != pit.uuid" +
			"  GROUP BY pi.patient_id) as extra_identifiers ON p.person_id = extra_identifiers.patient_id" +
			VISIT_JOIN +
			" left outer join visit_attribute va on va.visit_id = v.visit_id " +
			"   and va.attribute_type_id = (select visit_attribute_type_id from visit_attribute_type where name='Admission Status') " +
			"   and va.voided = 0";
	private static final String GROUP_BY_KEYWORD = " group by ";
	//public static final String ORDER_BY = " order by primary_identifier.identifier asc LIMIT :limit OFFSET :offset";
	public static final String ORDER_BY = " order by primary_identifier.identifier asc";
	public static final String LIMIT_OFFSET = " LIMIT :paramLimit OFFSET :paramOffset ";


	private String select;
	private String where;
	private String from;
	private String join;
	private String groupBy;
	private String orderBy;
	private SessionFactory sessionFactory;
	private Map<String,Type> types;

	private ArrayList<QueryParam> parameters = new ArrayList<>();
	private static final String BY_NAME_PARTS = " concat_ws(' ',coalesce(pn.given_name), coalesce(pn.middle_name), coalesce(pn.family_name)) like :%s";

	public PatientSearchQueryBuilder(SessionFactory sessionFactory){
		select = SELECT_STATEMENT;
		where = WHERE_CLAUSE;
		from  = FROM_TABLE;
		join = JOIN_CLAUSE;
		orderBy = ORDER_BY;
		groupBy = " p.person_id";
		this.sessionFactory = sessionFactory;
		types = new HashMap<>();

	}

	public PatientSearchQueryBuilder withPatientName(String name) {
		if (isBlank(name)) {
			return this;
		}
		String[] nameParts = name.trim().split(" ");
		String query_by_name_parts = "";
		for (int i = 0; i < nameParts.length; i++) {
			String namePart = nameParts[i];
			//String paramValue = SqlQueryHelper.escapeSQL(namePart,true);
			String paramValue = "%".concat(namePart).concat("%");
			QueryParam queryParam = new QueryParam("paramName" + i, paramValue);
			parameters.add(queryParam);

			if (!"".equals(query_by_name_parts)) {
				query_by_name_parts += " and " + String.format(BY_NAME_PARTS, queryParam.getParamName());
			} else {
				query_by_name_parts += String.format(BY_NAME_PARTS, queryParam.getParamName());
			}

		}
		where = combine(where, "and", enclose(query_by_name_parts));
		return this;
	}

	private String combine(String query, String operator, String condition) {
		return String.format("%s %s %s", query, operator, condition);
	}
	private String enclose(String value) {
		return String.format("(%s)", value);
	}

	public PatientSearchQueryBuilder withPatientAddress(String addressFieldName, String addressFieldValue, String[] addressAttributes, List<String> addressFields){
		if (validAddressField(addressFieldName, addressFieldValue, addressFields)) {
			QueryParam param = new QueryParam("paramAddr", "%".concat(addressFieldValue.trim()).concat("%"));
			parameters.add(param);
			where = combine(where, "and", enclose(String.format(" pa.%s like :%s ", addressFieldName.trim(), param.getParamName())));
			groupBy = String.format("pa.%s, %s", addressFieldName.trim(), groupBy);
		}

		String addrSelection = ", ''  as addressFieldValue";
		if (addressAttributes != null) {
			List<String> columnValuePairs = new ArrayList<>();
			for (String field : addressAttributes) {
				if (!addressFields.contains(field.trim().toLowerCase())) {
					continue;
				}
				if (!"{}".equals(field)) {
					columnValuePairs.add(String.format("\"%s\" : ' , '\"' , IFNULL(pa.%s ,''), '\"'", field.trim(), field.trim()));
				}
			}
			if (columnValuePairs.size() > 0) {
				addrSelection = String.format(",CONCAT ('{ %s , '}') as addressFieldValue",
						StringUtils.join(columnValuePairs.toArray(new String[columnValuePairs.size()]), ", ',"));
			}
		}
		select += addrSelection;
		types.put("addressFieldValue", StandardBasicTypes.STRING);
		return this;
	}

	private boolean validAddressField(String addressFieldName, String addressFieldValue, List<String>  configuredAddressFields) {
		if (isBlank(addressFieldValue)) {
			return false;
		}
		if (isBlank(addressFieldName)) {
			return false;
		}
		if (!configuredAddressFields.contains(addressFieldName.trim().toLowerCase())) {
			log.error("Invalid address field specified in search parameter: "+ addressFieldName);
			throw new RuntimeException("Invalid search criteria");
		}
		return true;
	}

	public PatientSearchQueryBuilder withPatientIdentifier(String identifier, Boolean filterOnAllIdentifiers) {
		if (isBlank(identifier)) {
			return this;
		}
		List<String> identifierTypes = new ArrayList<>(Arrays.asList("bahmni.primaryIdentifierType"));
		if (filterOnAllIdentifiers) {
			identifierTypes.add("bahmni.extraPatientIdentifierTypes");
		}
		String identifierTypeList = identifierTypes.stream().map(it -> String.format("'%s'", it)).collect(Collectors.joining(", "));
		String query = " JOIN (" +
				"SELECT pi.patient_id " +
				"FROM patient_identifier pi " +
					" JOIN patient_identifier_type pit ON pi.identifier_type = pit.patient_identifier_type_id AND pi.voided IS FALSE " +
					" JOIN global_property gp ON gp.property IN ("+ identifierTypeList + ")" +
					" AND gp.property_value LIKE concat('%', pit.uuid, '%')" +
					" AND pi.identifier LIKE :paramPatientIdentifier GROUP BY pi.patient_id " +
				") AS matched_patient ON matched_patient.patient_id = p.person_id ";
		String paramValue = "%".concat(identifier).concat("%");
		parameters.add(new QueryParam("paramPatientIdentifier", paramValue));
		join += query;
		return this;
	}

	public PatientSearchQueryBuilder withPatientAttributes(String customAttribute,
														   List<PersonAttributeType> patientAttributes,
														   List<PersonAttributeType> patientSearchResultAttributes){
		//TODO check old implementaation
		if (patientAttributes.isEmpty() && patientSearchResultAttributes.isEmpty()) {
			return this;
		}
		PersonAttributeQueryHelper patientAttributeQueryHelper = new PersonAttributeQueryHelper(customAttribute, patientAttributes, patientSearchResultAttributes);
		select = patientAttributeQueryHelper.selectClause(select);
		join = patientAttributeQueryHelper.appendToJoinClause(join);
		where = patientAttributeQueryHelper.appendToWhereClauseWithParam(where, (QueryParam param) -> parameters.add(param));
		types.putAll(patientAttributeQueryHelper.addScalarQueryResult());
		return this;
	}

	public PatientSearchQueryBuilder withProgramAttributes(String programAttribute, ProgramAttributeType programAttributeType){
		if (programAttributeType == null) {
			return this;
		}
		ProgramAttributeQueryHelper attributeQueryHelper = new ProgramAttributeQueryHelper(programAttribute, programAttributeType);
		select = attributeQueryHelper.selectClause(select);
		join = attributeQueryHelper.appendToJoinClause(join);

		where = attributeQueryHelper.appendToWhereClause(where, (QueryParam param) -> parameters.add(param));
		types.putAll(attributeQueryHelper.addScalarQueryResult());
		return this;
	}

	public SQLQuery buildSqlQuery(Integer limit, Integer offset){
		String joinWithVisit = join.replace(VISIT_JOIN, visitJoin);
		String query = new StringBuffer(select)
				.append(from)
				.append(joinWithVisit)
				.append(where)
				.append(GROUP_BY_KEYWORD)
				.append(groupBy)
				.append(orderBy)
				.append(LIMIT_OFFSET)
				.toString();

		SQLQuery sqlQuery = sessionFactory.getCurrentSession()
				.createSQLQuery(query)
				.addScalar("uuid", StandardBasicTypes.STRING)
				.addScalar("identifier", StandardBasicTypes.STRING)
				.addScalar("givenName", StandardBasicTypes.STRING)
				.addScalar("personId", StandardBasicTypes.INTEGER)
				.addScalar("middleName", StandardBasicTypes.STRING)
				.addScalar("familyName", StandardBasicTypes.STRING)
				.addScalar("gender", StandardBasicTypes.STRING)
				.addScalar("birthDate", StandardBasicTypes.DATE)
				.addScalar("deathDate", StandardBasicTypes.DATE)
				.addScalar("dateCreated", StandardBasicTypes.TIMESTAMP)
				.addScalar("activeVisitUuid", StandardBasicTypes.STRING)
				.addScalar("hasBeenAdmitted", StandardBasicTypes.BOOLEAN)
				.addScalar("extraIdentifiers", StandardBasicTypes.STRING);

		log.debug("Running patient search query : " + sqlQuery.getQueryString());

		Iterator<Map.Entry<String,Type>> iterator = types.entrySet().iterator();
		log.info("Executing Patient Search Query:" + sqlQuery.getQueryString());

		parameters.add(new QueryParam("paramLimit",limit));
		parameters.add(new QueryParam("paramOffset",offset));
		parameters.forEach(param -> sqlQuery.setParameter(param.getParamName(),param.getParamValue()));
		parameters.forEach(param -> log.debug(String.format("Patient Search Parameter %s = %s", param.getParamName(), param.getParamValue().toString())));

		while(iterator.hasNext()){
			Map.Entry<String,Type> entry = iterator.next();
			sqlQuery.addScalar(entry.getKey(),entry.getValue());
		}

		sqlQuery.setResultTransformer(Transformers.aliasToBean(PatientResponse.class));
		return sqlQuery;
	}

	public PatientSearchQueryBuilder withLocation(Location visitLocation, Boolean filterPatientsByLocation) {
		if (visitLocation == null) {
			return this;
		}

		visitJoin += " and v.location_id = :paramVisitLocationId ";
		if (filterPatientsByLocation) {
			where += " and v.location_id = :paramVisitLocationId ";
		}
		QueryParam paramVisitLocationId = new QueryParam("paramVisitLocationId", visitLocation.getLocationId());
		parameters.add(paramVisitLocationId);
		return this;
	}
}
