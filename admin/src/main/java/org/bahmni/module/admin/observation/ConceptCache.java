package org.bahmni.module.admin.observation;

import java.util.HashMap;
import java.util.Map;

import org.openmrs.Concept;
import org.openmrs.api.ConceptService;
import org.openmrs.module.emrapi.encounter.exception.ConceptNotFoundException;

public class ConceptCache {
    private Map<String, Concept> cachedConcepts = new HashMap<>();
    private ConceptService conceptService;

    public ConceptCache(ConceptService conceptService) {
        this.conceptService = conceptService;
    }

    public Concept getConcept(String conceptName) {
        if (!cachedConcepts.containsKey(conceptName)) {
            cachedConcepts.put(conceptName, fetchConcept(conceptName));
        }
        return cachedConcepts.get(conceptName);
    }

    private Concept fetchConcept(String conceptName) {
        Concept obsConcept = conceptService.getConceptByName(conceptName);
        if (obsConcept == null)
            throw new ConceptNotFoundException("Concept '" + conceptName + "' not found");

        return obsConcept;
    }
}
