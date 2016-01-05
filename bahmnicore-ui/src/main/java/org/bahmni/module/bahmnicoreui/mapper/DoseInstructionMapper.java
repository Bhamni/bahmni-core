package org.bahmni.module.bahmnicoreui.mapper;

import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;
import org.openmrs.DrugOrder;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class DoseInstructionMapper {

    public static String getFrequency(DrugOrder drugOrder) throws IOException {
        if (drugOrder.getFrequency() == null) {
            String dosingInstructions = drugOrder.getDosingInstructions();
            Map<String, Object> instructions = hashMapForJson(dosingInstructions);
            return concat("-", getEmptyIfNull(instructions.get("morningDose")),
                    getEmptyIfNull(instructions.get("afternoonDose")),
                    getEmptyIfNull(instructions.get("eveningDose")));
        }
        return drugOrder.getFrequency().getName();
    }

    private static String getEmptyIfNull(Object text) {
        return text == null ? "" : text.toString();
    }

    public static Map<String, Object> hashMapForJson(String dosingInstructions) throws IOException {
        if (dosingInstructions == null || dosingInstructions.isEmpty()) {
            return Collections.EMPTY_MAP;
        }
        ObjectMapper objectMapper = new ObjectMapper(new JsonFactory());
        TypeReference<HashMap<String, Object>> typeRef
                = new TypeReference<HashMap<String, Object>>() {
        };
        return objectMapper.readValue(dosingInstructions, typeRef);
    }

    private static String concat(String separator, String... values) {
        StringBuilder stringBuilder = new StringBuilder();
        for (String value : values) {
            if (value != null && !value.isEmpty()) {
                stringBuilder.append(separator).append(value);
            }
        }
        return stringBuilder.length() > 1 ? stringBuilder.substring(1) : "";
    }
}