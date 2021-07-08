package org.bahmni.module.bahmnicore.util;

import org.apache.commons.collections.CollectionUtils;
import org.openmrs.Concept;
import org.openmrs.api.ConceptService;
import org.openmrs.module.bahmniemrapi.encountertransaction.contract.BahmniObservation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

public class MiscUtils {
    public static List<Concept> getConceptsForNames(List<String> conceptNames, ConceptService conceptService) {
        //Returning null for the sake of UTs
        if (CollectionUtils.isNotEmpty(conceptNames)) {
            List<Concept> rootConcepts = new ArrayList<>();
            for (String rootConceptName : conceptNames) {
                Concept concept = conceptService.getConceptByName(rootConceptName);
                if (concept != null) {
                    rootConcepts.add(concept);
                }
            }
            return rootConcepts;
        }
        return new ArrayList<>();
    }

    public static void setUuidsForObservations(Collection<BahmniObservation> bahmniObservations) {
        for (BahmniObservation bahmniObservation : bahmniObservations) {
            if (org.apache.commons.lang3.StringUtils.isBlank(bahmniObservation.getUuid())) {
                bahmniObservation.setUuid(UUID.randomUUID().toString());
            }
        }
    }

    public static void validateFormFieldPath(Collection<BahmniObservation> bahmniObservations) {
        bahmniObservations.forEach(observation -> {
            if ( observation.getFormFieldPath() != null && !isValidFormFieldPath(observation.getFormFieldPath())) {
                throw new IllegalArgumentException("Not a valid formFieldPath"+ observation.getFormFieldPath());
            }
        });
    }

    public static boolean isValidFormFieldPath(String formFieldPath) {
        return formFieldPath.matches("[\\s\\w]*.[\\d]+(/[\\d]+-[\\d]+)*");
    }
}
