package org.bahmni.module.bahmnicore.util;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.bahmni.module.bahmnicore.model.searchParams.AdditionalSearchParam;
import org.codehaus.jackson.map.ObjectMapper;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SqlQueryHelper {
    private final Pattern paramPlaceHolderPattern;
    private static final String PARAM_PLACE_HOLDER_REGEX = "\\$\\{[^{]*\\}";
    private static final Logger log = Logger.getLogger(SqlQueryHelper.class);

    public SqlQueryHelper() {
        this.paramPlaceHolderPattern = Pattern.compile(PARAM_PLACE_HOLDER_REGEX);
    }

    List<String> getParamNamesFromPlaceHolders(String query){
        List<String> params  = new ArrayList<>();
        Matcher matcher = paramPlaceHolderPattern.matcher(query);
        while(matcher.find()){
            params.add(stripDelimiters(matcher.group()));
        }
        return params;
    }

    private String stripDelimiters(String text) {
        return text.replaceAll("[${}]", "");
    }

    public String transformIntoPreparedStatementFormat(String queryString){
        return  queryString.replaceAll(PARAM_PLACE_HOLDER_REGEX,"?");
    }

    public PreparedStatement constructPreparedStatement(String queryString, Map<String, String[]> params, Connection conn) throws SQLException {
       String finalQueryString = queryString;
        if (params.get("additionalParams") != null && params.get("additionalParams") != null) {
            finalQueryString = parseAdditionalParams(params.get("additionalParams")[0], queryString);
        }

        List<String> paramNamesFromPlaceHolders = getParamNamesFromPlaceHolders(finalQueryString);
        String statement = transformIntoPreparedStatementFormat(finalQueryString);
        PreparedStatement preparedStatement = conn.prepareStatement(statement);
        if(params != null ){
            int i=1;
            for (String paramName : paramNamesFromPlaceHolders) {
                String paramValue = params.get(paramName)[0];
                preparedStatement.setObject(i++,paramValue);
            }
        }
        return preparedStatement;
    }

    String parseAdditionalParams(String additionalParams, String queryString) {
        String queryWithAdditionalParams = queryString;
        try {
            AdditionalSearchParam additionalSearchParams = new ObjectMapper().readValue(additionalParams, AdditionalSearchParam.class);
            String test = additionalSearchParams.getTests();
            queryWithAdditionalParams = queryString.replaceAll("\\$\\{testName\\}", test);
        } catch (IOException e) {
            log.error("Failed to parse Additional Search Parameters.");
            e.printStackTrace();
        }
        return queryWithAdditionalParams;
    }

    public static String escapeSQL(String str, boolean escapeDoubleQuotes, Character escapeChar) {
        if (StringUtils.isBlank(str)) {
            return str;
        }
        char escChar = '\\';
        if (escapeChar != null) {
            escChar = escapeChar.charValue();
        }
        String strToCheck = str.trim().replace("0x", "0X").replace("/*", "\\/*");
        StringBuilder sBuilder = new StringBuilder();
        int stringLength = strToCheck.length();
        for (int i = 0; i < stringLength; ++i) {
            char c = strToCheck.charAt(i);
            switch (c) {
                case 0:
                    sBuilder.append(escChar);
                    sBuilder.append('0');
                    break;
                case ';':
                    sBuilder.append(escChar);
                    sBuilder.append(';');
                    break;
                case '\n': /* Must be escaped for logs */
                    sBuilder.append(escChar);
                    sBuilder.append('n');
                    break;
                case '\r':
                    sBuilder.append(escChar);
                    sBuilder.append('r');
                    break;
                case '\\':
                    sBuilder.append(escChar);
                    sBuilder.append('\\');
                    break;
                case '\'':
                    sBuilder.append(escChar);
                    sBuilder.append('\'');
                    break;
                case '"':
                    if (escapeDoubleQuotes) {
                        sBuilder.append('\\');
                    }
                    sBuilder.append('"');
                    break;
                case '\032':
                    sBuilder.append(escChar);
                    sBuilder.append('Z');
                    break;
                default:
                    sBuilder.append(c);
            }
        }
        return sBuilder.toString();
    }

}
