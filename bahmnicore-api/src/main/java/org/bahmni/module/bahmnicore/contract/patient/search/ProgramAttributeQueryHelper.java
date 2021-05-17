package org.bahmni.module.bahmnicore.contract.patient.search;

import org.apache.commons.lang3.StringUtils;
import org.bahmni.module.bahmnicore.customdatatype.datatype.CodedConceptDatatype;
import org.bahmni.module.bahmnicore.model.bahmniPatientProgram.ProgramAttributeType;
import org.hibernate.type.StandardBasicTypes;
import org.hibernate.type.Type;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public class ProgramAttributeQueryHelper {
    private final String programAttributeValue;
    private ProgramAttributeType programAttributeType;
    private QueryParam param;

    public ProgramAttributeQueryHelper(String programAttributeValue, ProgramAttributeType programAttributeType) {
        this.programAttributeValue = programAttributeValue;
        this.programAttributeType = programAttributeType;
    }

    public String selectClause(String select) {
        String selectField = isAttributeValueCodedConcept() ? "cn.name" : "ppa.value_reference";
        return select.concat(String.format(", concat('{',group_concat(DISTINCT (coalesce(concat('\"',ppt.name,'\":\"',%s,'\"'))) " +
                "SEPARATOR ','),'}') AS patientProgramAttributeValue", selectField));
    }

    public String appendToJoinClause(String join) {
        StringBuffer stringBuffer = new StringBuffer(join);
        stringBuffer.append(" left outer join patient_program pp on p.person_id = pp.patient_id and pp.voided=0");
        stringBuffer.append(" left outer join patient_program_attribute ppa on pp.patient_program_id = ppa.patient_program_id and ppa.voided=0");
        stringBuffer.append(" left outer join program_attribute_type ppt on ppa.attribute_type_id = ppt.program_attribute_type_id");
        stringBuffer.append(" and ppa.attribute_type_id = " + getProgramAttributeTypeId());
        if (isAttributeValueCodedConcept()) {
            stringBuffer.append(" LEFT OUTER JOIN concept_name cn on ppa.value_reference = cn.concept_id and cn.voided=0");
        }
        return stringBuffer.toString();
    }

    public String appendToWhereClause(String where, Consumer<QueryParam> paramList) {
        if (StringUtils.isBlank(programAttributeValue))  {
            return where;
        }
        String paramValue = "%".concat(programAttributeValue).concat("%");
        QueryParam param = new QueryParam("paramProgramAttributeValue", paramValue);
        paramList.accept(param);
        return where.concat(" AND ppa.value_reference like :paramProgramAttributeValue and ppa.attribute_type_id = " + getProgramAttributeTypeId());
    }

    private boolean isAttributeValueCodedConcept() {
        return  programAttributeType.getDatatypeClassname().equals(CodedConceptDatatype.class.getCanonicalName());
    }

    private int getProgramAttributeTypeId() {
        return programAttributeType.getProgramAttributeTypeId().intValue();
    }

    public Map<String,Type> addScalarQueryResult(){
        Map<String,Type> scalarQueryResult = new HashMap<>();
        scalarQueryResult.put("patientProgramAttributeValue", StandardBasicTypes.STRING);
        return scalarQueryResult;
    }
}
