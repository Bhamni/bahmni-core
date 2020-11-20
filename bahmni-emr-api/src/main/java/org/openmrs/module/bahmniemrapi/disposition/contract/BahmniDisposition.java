package org.openmrs.module.bahmniemrapi.disposition.contract;

import org.openmrs.ConceptName;
import org.openmrs.module.emrapi.encounter.domain.EncounterTransaction;

import java.util.*;

public class BahmniDisposition {
    private String code;
    private String conceptName;
    private String existingObs;
    private boolean voided;
    private String voidReason;
    private String creatorName;
    private List<EncounterTransaction.Observation> additionalObs;
    private Date dispositionDateTime;
    private Set<EncounterTransaction.Provider> providers = new HashSet<>();

    public String getPreferredName() {
        return preferredName;
    }

    public void setPreferredName(String preferredName) {
        this.preferredName = preferredName;
    }

    private String preferredName;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getConceptName() {
        return conceptName;
    }

    public void setConceptName(String conceptName) {
        this.conceptName = conceptName;
    }

    public String getExistingObs() {
        return existingObs;
    }

    public void setExistingObs(String existingObs) {
        this.existingObs = existingObs;
    }

    public boolean isVoided() {
        return voided;
    }

    public void setVoided(boolean voided) {
        this.voided = voided;
    }

    public String getVoidReason() {
        return voidReason;
    }

    public void setVoidReason(String voidReason) {
        this.voidReason = voidReason;
    }

    public List<EncounterTransaction.Observation> getAdditionalObs() {
        return additionalObs;
    }

    public void setAdditionalObs(List<EncounterTransaction.Observation> additionalObs) {
        this.additionalObs = additionalObs;
    }

    public Date getDispositionDateTime() {
        return dispositionDateTime;
    }

    public void setDispositionDateTime(Date dispositionDateTime) {
        this.dispositionDateTime = dispositionDateTime;
    }

    public Set<EncounterTransaction.Provider> getProviders() {
        return providers;
    }

    public void setProviders(Set<EncounterTransaction.Provider> providers) {
        this.providers = providers;
    }

    public String getCreatorName() {
        return creatorName;
    }

    public void setCreatorName(String creatorName) {
        this.creatorName = creatorName;
    }
}
