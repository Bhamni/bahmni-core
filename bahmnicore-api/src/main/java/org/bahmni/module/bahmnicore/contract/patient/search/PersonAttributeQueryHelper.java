package org.bahmni.module.bahmnicore.contract.patient.search;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.type.StandardBasicTypes;
import org.hibernate.type.Type;
import org.openmrs.PersonAttributeType;
import org.openmrs.api.context.Context;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class PersonAttributeQueryHelper {
    private String customAttribute;
    private List<PersonAttributeType> patientAttributes;
    private List<PersonAttributeType> patientSearchResultAttributes;

    public PersonAttributeQueryHelper(String customAttribute, List<PersonAttributeType> patientAttributes, List<PersonAttributeType> patientSearchResultAttributes) {
        this.customAttribute = customAttribute;
        this.patientAttributes = patientAttributes;
        this.patientSearchResultAttributes = patientSearchResultAttributes;
    }

    public String selectClause(String select){
        String selectClause = "''";
        if(patientSearchResultAttributes.size() > 0) {
            selectClause =
                    "concat('{',group_concat(DISTINCT (coalesce(concat('\"',attrt_results.name,'\":\"', REPLACE(REPLACE(coalesce(cn.name, def_loc_cn.name, pattr_results.value),'\\\\','\\\\\\\\'),'\"','\\\\\"'),'\"'))) SEPARATOR ','),'}')";
        }
        return String.format("%s,%s as customAttribute", select, selectClause);
    }

    public String appendToJoinClause(String join) {
        if (patientAttributes.size() > 0) {
            String attributeIds = patientAttributes.stream().map(pa -> pa.getPersonAttributeTypeId().toString()).collect(Collectors.joining(","));
            join += " LEFT OUTER JOIN person_attribute pattrln on pattrln.person_id = p.person_id and pattrln.person_attribute_type_id in (" + attributeIds + ") ";
        }

        if (patientSearchResultAttributes.size() > 0) {
            String searchAttributeIds = patientSearchResultAttributes.stream().map(psra -> psra.getPersonAttributeTypeId().toString()).collect(Collectors.joining(","));

            /**
             String conceptTypeAttributeTypeIds = patientSearchResultAttributes.stream()
                    .filter(psa -> psa.getFormat().equals("org.openmrs.Concept"))
                    .map(psra -> psra.getPersonAttributeTypeId().toString()).collect(Collectors.joining(","));
             */

            join += " LEFT OUTER JOIN person_attribute pattr_results on pattr_results.person_id = p.person_id and pattr_results.voided = 0"
                    + " and  pattr_results.person_attribute_type_id in (" + searchAttributeIds + ") "
                    + " LEFT OUTER JOIN person_attribute_type attrt_results on attrt_results.person_attribute_type_id = pattr_results.person_attribute_type_id "
                    + " LEFT OUTER JOIN concept_name cn on cn.concept_id = pattr_results.value and cn.concept_name_type = 'FULLY_SPECIFIED'"
                    + " and attrt_results.format = 'org.openmrs.Concept' and cn.locale = '" + Context.getLocale() + "'"
                    //+ " and pattr_results.person_attribute_type_id in (" + conceptTypeAttributeTypeIds +  ")"
                    + " LEFT OUTER JOIN concept_name def_loc_cn on def_loc_cn.concept_id = pattr_results.value and def_loc_cn.concept_name_type = 'FULLY_SPECIFIED'"
                    + " and attrt_results.format = 'org.openmrs.Concept' and def_loc_cn.locale = 'en' ";
                    //+ " and pattr_results.person_attribute_type_id in (" + conceptTypeAttributeTypeIds +  ")";
        }

        return join;
    }

    public String appendToWhereClauseWithParam(String where, Consumer<QueryParam> paramList){
        if (StringUtils.isEmpty(customAttribute) || patientAttributes.size() == 0) {
            return where;
        }
        String paramValue = "%".concat(customAttribute).concat("%");
        QueryParam param = new QueryParam("paramCustomPatientAttribute", paramValue);
        paramList.accept(param);
        return new StringBuilder(where).append(" and ").append(" pattrln.value like :paramCustomPatientAttribute").toString();
    }

    public Map<String, Type> addScalarQueryResult(){
        Map<String,Type> scalarQueryResult = new HashMap<>();
        scalarQueryResult.put("customAttribute", StandardBasicTypes.STRING);
        return scalarQueryResult;
    }
}
