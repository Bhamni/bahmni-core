package org.openmrs.module.bahmniemrapi.laborder.contract;

import java.util.List;
import lombok.Data;

import java.util.Date;
import org.openmrs.module.bahmniemrapi.accessionnote.contract.AccessionNote;

@Data
public class LabOrderResult {
    private String accessionUuid;
    private Date accessionDateTime;
    private Date visitStartTime;
    private List<AccessionNote> accessionNotes;
    private String testName;
    private String testUnitOfMeasurement;
    private String testUuid;
    private String panelUuid;
    private String panelName;
    private Double minNormal;
    private Double maxNormal;
    private String result;
    private String notes;
    private Boolean abnormal;
    private String provider;
    private Boolean referredOut;
    private Date resultDateTime;
    private String uploadedFileName;

    public LabOrderResult() {
    }

    public LabOrderResult(String accessionUuid, Date accessionDateTime, String testName, String testUnitOfMeasurement, Double minNormal, Double maxNormal, String result, Boolean abnormal, Boolean referredOut, String uploadedFileName, List<AccessionNote> accessionNotes) {
        this.accessionUuid = accessionUuid;
        this.testName = testName;
        this.testUnitOfMeasurement = testUnitOfMeasurement;
        this.minNormal = minNormal;
        this.maxNormal = maxNormal;
        this.accessionDateTime = accessionDateTime;
        this.result = result;
        this.abnormal = abnormal;
        this.referredOut = referredOut;
        this.uploadedFileName = uploadedFileName;
        this.accessionNotes = accessionNotes;
    }
}
