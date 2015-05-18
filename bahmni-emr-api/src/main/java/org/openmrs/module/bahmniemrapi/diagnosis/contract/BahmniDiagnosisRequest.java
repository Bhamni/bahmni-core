package org.openmrs.module.bahmniemrapi.diagnosis.contract;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.openmrs.PersonName;

@JsonIgnoreProperties(ignoreUnknown = true)
public class BahmniDiagnosisRequest extends BahmniDiagnosis {
    private String previousObs;
    private String encounterUuid;
    private String personName;

    public String getPreviousObs() {
        return previousObs;
    }

    public void setPreviousObs(String previousObs) {
        this.previousObs = previousObs;
    }

    public void setEncounterUuid(String encounterUuid) {
        this.encounterUuid = encounterUuid;
    }

    public String getEncounterUuid() {
        return encounterUuid;
    }

    public String getPersonName() {
        return this.personName;
    }

    public void setPersonName(String personName) {
        this.personName = personName;
    }
}
