package org.bahmni.module.referencedata.web.contract;

import lombok.Data;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class RequestConcept {
    private String uuid;
    private String uniqueName;
    private String displayName;
    private String description;
    private String className;
    private String dataType;

    public RequestConcept() {
    }
}
