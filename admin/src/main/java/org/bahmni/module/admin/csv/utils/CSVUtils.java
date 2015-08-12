package org.bahmni.module.admin.csv.utils;

import org.bahmni.csv.KeyValue;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class CSVUtils {

    public static final String ENCOUNTER_DATE_PATTERN = "yyyy-M-d";

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
        // All csv imports use the same date format
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(ENCOUNTER_DATE_PATTERN);
        simpleDateFormat.setLenient(false);
        return simpleDateFormat.parse(dateString);
    }

    public static Date getTodayDate() throws ParseException {
        DateFormat dateFormat = new SimpleDateFormat(ENCOUNTER_DATE_PATTERN);
        Date date = new Date();
        String dateString = dateFormat.format(date);
        return getDateFromString(dateString);
    }

}
