package org.openmrs.module.bahmniemrapi.document.contract;

import java.util.Date;

public class Document {

    private String image;
    private String format;
    private String testUuid;
    private String obsUuid;
    private Date obsDateTime;
    private boolean voided;

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getFormat() {
        return format;
    }

    public void setFormat(String format) {
        this.format = format;
    }

    public String getTestUuid() {
        return testUuid;
    }

    public void setTestUuid(String testUuid) {
        this.testUuid = testUuid;
    }

    public String getObsUuid() {
        return obsUuid;
    }

    public void setObsUuid(String obsUuid) {
        this.obsUuid = obsUuid;
    }

    public Date getObsDateTime() {
        return obsDateTime;
    }

    public void setObsDateTime(Date obsDateTime) {
        this.obsDateTime = obsDateTime;
    }

    public boolean isVoided() {
        return voided;
    }

    public void setVoided(boolean voided) {
        this.voided = voided;
    }
}
