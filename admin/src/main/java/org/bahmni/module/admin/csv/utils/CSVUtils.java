package org.bahmni.module.admin.csv.utils;

import org.bahmni.csv.KeyValue;
import org.bahmni.csv.exception.MigrationException;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class CSVUtils {

    public static final String ENCOUNTER_DATE_PATTERN = "yyyy-M-d";

    public static final List<String> SUPPORTED_DATE_PATTERNS = Arrays.asList("yyyy-M-d", "yyyy/M/d");

    public static String[] getStringArray(List<KeyValue> keyValueList) {
        List<String> stringList = new ArrayList<>();
        for (KeyValue keyValue : keyValueList) {
            stringList.add(keyValue.getValue());
        }
        return stringList.toArray(new String[]{});
    }

    public static List<KeyValue> getKeyValueList(String key, List<String> stringList) {
        List<KeyValue> keyValueList = new ArrayList<>();
        for (String string : stringList) {
            keyValueList.add(new KeyValue(key, string));
        }
        return keyValueList;
    }

    public static Date getDateFromString(String dateString) throws ParseException {
            // Check all supported date formats
            for (String formatString : SUPPORTED_DATE_PATTERNS)
            {
                try
                {
                    SimpleDateFormat simpleDateFormat = new SimpleDateFormat(formatString);
                    simpleDateFormat.setLenient(false);
                    return simpleDateFormat.parse(dateString);
                }
                catch (ParseException e) {}
            }
            // if pattern is not supported throw the parse error
            throw new MigrationException("Unparseable date "+ dateString);
    }

    public static Date getTodayDate() throws ParseException {
        DateFormat dateFormat = new SimpleDateFormat(ENCOUNTER_DATE_PATTERN);
        Date date = new Date();
        String dateString = dateFormat.format(date);
        return getDateFromString(dateString);
    }

}
