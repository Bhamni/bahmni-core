package org.openmrs.module.bahmniemrapi.drugorder;

import org.openmrs.Concept;
import org.openmrs.Duration;
import org.openmrs.OrderFrequency;

import java.util.Date;

import static org.apache.commons.lang3.time.DateUtils.addMilliseconds;

public class DrugOrderUtil {
    public static Date calculateAutoExpireDate(Integer orderDuration, Concept durationUnits, Integer numRefills, Date effectiveStartDate, OrderFrequency frequency) {
        if (orderDuration == null || durationUnits == null) {
            return null;
        }
        if (numRefills != null && numRefills > 0) {
            return null;
        }
        String durationCode = Duration.getCode(durationUnits);
        if (durationCode == null) {
            return null;
        }
        Duration duration = new Duration(orderDuration, durationCode);
        return aMomentBefore(duration.addToDate(effectiveStartDate, frequency));
    }

    public static Date aMomentBefore(Date date) {
        return addMilliseconds(date, -1);
    }
    public static Date aMomentAfter(Date date) {
        return addMilliseconds(date, 1);
    }

}
