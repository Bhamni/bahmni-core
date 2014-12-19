package org.openmrs.module.bahmniemrapi.accessionnote.contract;

import java.util.Date;
import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.openmrs.module.emrapi.utils.CustomJsonDateSerializer;

public class AccessionNote {
    private String text;
    private String providerName;
    private String accessionUuid;
    private Date dateTime;

    public AccessionNote() {
    }

    public AccessionNote(String text, String providerName, String accessionUuid, Date dateTime) {
        this.text = text;
        this.providerName = providerName;
        this.accessionUuid = accessionUuid;
        this.dateTime = dateTime;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getProviderName() {
        return providerName;
    }

    public void setProviderName(String providerName) {
        this.providerName = providerName;
    }

    public String getAccessionUuid() {
        return accessionUuid;
    }

    public void setAccessionUuid(String accessionUuid) {
        this.accessionUuid = accessionUuid;
    }

    @JsonSerialize(using = CustomJsonDateSerializer.class)
    public Date getDateTime() {
        return dateTime;
    }

    public void setDateTime(Date dateTime){
        this.dateTime = dateTime;
    }
}
