package org.bahmni.module.referencedata.labconcepts.mapper;

import org.apache.commons.lang3.StringUtils;
import org.bahmni.module.referencedata.labconcepts.contract.ConceptCommon;
import org.openmrs.ConceptClass;
import org.openmrs.api.ConceptNameType;

import static org.bahmni.module.referencedata.labconcepts.mapper.ConceptExtension.*;

public class ConceptCommonMapper {

    public org.openmrs.Concept map(ConceptCommon conceptCommon, ConceptClass conceptClass, org.openmrs.Concept existingConcept) {
        org.openmrs.Concept concept = new org.openmrs.Concept();
        if (existingConcept != null) {
            concept = existingConcept;
        }
        String displayName = conceptCommon.getDisplayName();
        concept = addConceptName(concept, getConceptName(conceptCommon.getUniqueName(), ConceptNameType.FULLY_SPECIFIED), conceptCommon.getLocale());
        if (displayName != null) {
            concept = addConceptName(concept, getConceptName(conceptCommon.getDisplayName(), ConceptNameType.SHORT), conceptCommon.getLocale());
        }

        if (!StringUtils.isBlank(conceptCommon.getDescription()) && concept.getDescription() != null) {
            concept.getDescription().setDescription(conceptCommon.getDescription());
        } else if (!StringUtils.isBlank(conceptCommon.getDescription())) {
            concept.addDescription(constructDescription(conceptCommon.getDescription()));
        }
        concept.setConceptClass(conceptClass);
        return concept;
    }


}