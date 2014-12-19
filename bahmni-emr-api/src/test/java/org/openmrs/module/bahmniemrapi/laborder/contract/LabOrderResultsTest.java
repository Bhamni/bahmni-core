package org.openmrs.module.bahmniemrapi.laborder.contract;

import org.joda.time.DateTime;
import org.joda.time.DateTimeUtils;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class LabOrderResultsTest {
    @Test
    public void shouldCreateSparseMatrixForLabOrderResultAndDates() throws Exception {
        List<LabOrderResult> results = Arrays.asList(
                new LabOrderResult("uuid1", new DateTime(2014, 2, 10, 0, 0).toDate(), "Haemoglobin", "ppm", 15.0, 20.0, "17.0", false, false, "uploadedFile", null),
                new LabOrderResult("uuid1", new DateTime(2014, 2, 12, 0, 0).toDate(), "Haemoglobin", "ppm", 15.0, 20.0, "19.0", false, false, null, null),
                new LabOrderResult("uuid1", new DateTime(2014, 1, 14, 0, 0, 1, 0).toDate(), "Haemoglobin", "ppm", 15.0, 20.0, "9.0", true, false, null, null),
                new LabOrderResult("uuid1", new DateTime(2014, 1, 14, 1, 0, 0, 0).toDate(), "Haemoglobin", "ppm", 15.0, 20.0, "9.2", true, false, null, null),
                new LabOrderResult("uuid2", new DateTime(2014, 5, 15, 0, 0).toDate(), "ESR", "gm/L", 100.0, 200.0, "50.0", false, false, null, null),
                new LabOrderResult("uuid2", new DateTime(2014, 5, 16, 0, 0).toDate(), "ESR", "gm/L", 100.0, 200.0, "51.0", false, false, null, null),
                new LabOrderResult("uuid3", new DateTime(2014, 5, 17, 0, 0).toDate(), "ESR", null, null, null, null, null, false, null, null),
                new LabOrderResult("uuid3", new DateTime(2014, 5, 18, 0, 0).toDate(), "ESR", null, null, null, null, null, true, null, null)
        );

        LabOrderResults labOrderResults = new LabOrderResults(results);
        TabularLabOrderResults table = labOrderResults.getTabularResult();

        assertEquals(7, table.getDates().size());
        assertEquals(2, table.getOrders().size());
        assertEquals(7, table.getValues().size());
        assertEquals("uploadedFile", table.getValues().get(0).getUploadedFileName());
    }
}
