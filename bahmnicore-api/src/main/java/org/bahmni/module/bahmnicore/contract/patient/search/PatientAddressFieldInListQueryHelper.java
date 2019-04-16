package org.bahmni.module.bahmnicore.contract.patient.search;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.collections.CollectionUtils;

import java.util.List;
import java.util.stream.Collectors;

public class PatientAddressFieldInListQueryHelper extends PatientAddressFieldQueryHelper{
    final protected static String EMPTY_LIST = "''";

    private List<String> addressFieldValues;

    public PatientAddressFieldInListQueryHelper(String addressFieldName, List<String> addressFieldValues, String[] addressResultFields) {
        super(addressFieldName, null, addressResultFields);
        this.addressFieldValues = addressFieldValues;
    }

    @Override
    public String appendToWhereClause(String  where) {
        String inList = EMPTY_LIST;
        if (!CollectionUtils.isEmpty(addressFieldValues)) {
            inList = addressFieldValues.stream()
                    .map((s) -> "'" + StringEscapeUtils.escapeSql(s) + "'")
                    .collect(Collectors.joining(","));
        }
        return combine(where, "and", enclose(" " + addressFieldName + " in (" + inList + ")"));
    }
}
