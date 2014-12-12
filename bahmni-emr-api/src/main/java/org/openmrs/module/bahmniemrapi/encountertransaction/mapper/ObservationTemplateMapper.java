package org.openmrs.module.bahmniemrapi.encountertransaction.mapper;

import org.apache.commons.lang3.ObjectUtils;
import org.openmrs.Concept;
import org.openmrs.module.bahmniemrapi.encountertransaction.contract.BahmniObservation;
import org.openmrs.module.bahmniemrapi.encountertransaction.contract.ObservationTemplate;
import org.openmrs.module.emrapi.encounter.ConceptMapper;
import org.openmrs.module.emrapi.encounter.domain.EncounterTransaction;

import java.util.ArrayList;
import java.util.List;

public class ObservationTemplateMapper {

    private final ConceptMapper conceptMapper;

    public ObservationTemplateMapper(ConceptMapper conceptMapper) {
        this.conceptMapper = conceptMapper;
    }

    public List<ObservationTemplate> map(List<BahmniObservation> observations, Concept concept) {
        List<ObservationTemplate> observationTemplates = new ArrayList<>();
        for (BahmniObservation observation : observations) {
            ObservationTemplate matchingObservationTemplate = getMatchingObservationTemplate(observation, observationTemplates);
            if(matchingObservationTemplate == null){
                observationTemplates.add(createObservationTemplate(observation, concept));
            } else {
                matchingObservationTemplate.addBahmniObservation(observation);
            }
        }
        return observationTemplates;
    }

    private ObservationTemplate createObservationTemplate(BahmniObservation observation, Concept concept) {
        ObservationTemplate observationTemplate = new ObservationTemplate();
        observationTemplate.addBahmniObservation(observation);
        EncounterTransaction.Concept conceptData = conceptMapper.map(concept);
        observationTemplate.setConcept(conceptData);
        observationTemplate.setVisitStartDate(observation.getVisitStartDateTime());
        return observationTemplate;
    }

    private ObservationTemplate getMatchingObservationTemplate(BahmniObservation observation, List<ObservationTemplate> observationTemplates) {
        for (ObservationTemplate observationTemplate : observationTemplates) {
            if(ObjectUtils.equals(observationTemplate.getVisitStartDate(), observation.getVisitStartDateTime())){
                return observationTemplate;
            }
        }
        return null;
    }
}
