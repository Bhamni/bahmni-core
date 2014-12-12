package org.openmrs.module.bahmniemrapi.encountertransaction.contract;


import org.openmrs.module.emrapi.encounter.domain.EncounterTransaction;

import java.util.ArrayList;
import java.util.List;

public class DiseaseTemplate {

    public static final String ALL_DISEASE_TEMPLATES = "All Disease Templates";
    private List<ObservationTemplate> observationTemplates = new ArrayList<>();
    private EncounterTransaction.Concept concept;

    public DiseaseTemplate() {
    }

    public DiseaseTemplate(EncounterTransaction.Concept concept) {
        this.concept = concept;
    }

    public List<ObservationTemplate> getObservationTemplates() {
        return observationTemplates;
    }

    public void setObservationTemplates(List<ObservationTemplate> observationTemplates) {
        this.observationTemplates = observationTemplates;
    }

    public void addObservationTemplate(ObservationTemplate observationTemplate) {
        this.observationTemplates.add(observationTemplate);
    }

    public void addObservationTemplates(List<ObservationTemplate> observationTemplates) {
        this.observationTemplates.addAll(observationTemplates);
    }

    public EncounterTransaction.Concept getConcept() {
        return concept;
    }

    public void setConcept(EncounterTransaction.Concept concept) {
        this.concept = concept;
    }
}
