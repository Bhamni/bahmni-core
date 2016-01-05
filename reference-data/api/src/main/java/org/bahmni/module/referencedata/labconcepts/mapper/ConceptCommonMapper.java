package org.bahmni.module.referencedata.labconcepts.mapper;

import org.apache.commons.lang3.StringUtils;
import org.bahmni.module.referencedata.labconcepts.contract.ConceptCommon;
import org.bahmni.module.referencedata.labconcepts.model.ConceptMetaData;
import org.openmrs.Concept;
import org.openmrs.ConceptDescription;
import org.openmrs.api.ConceptNameType;
import org.openmrs.api.context.Context;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Locale;

import static org.bahmni.module.referencedata.labconcepts.mapper.ConceptExtension.*;

public class ConceptCommonMapper {

    public org.openmrs.Concept map(ConceptCommon conceptCommon, ConceptMetaData conceptMetaData) {
        org.openmrs.Concept concept = new org.openmrs.Concept();
        if (conceptMetaData.getExistingConcept() != null) {
            concept = conceptMetaData.getExistingConcept();
        }
        String displayName = conceptCommon.getDisplayName();
        concept = addConceptName(concept, getConceptName(conceptCommon.getUniqueName(), ConceptNameType.FULLY_SPECIFIED, conceptMetaData.getLocale()));
        if (displayName != null) {
            concept = addConceptName(concept, getConceptName(conceptCommon.getDisplayName(), ConceptNameType.SHORT, conceptMetaData.getLocale()));
        }

        if (!StringUtils.isBlank(conceptCommon.getDescription())) {
            setDescriptionWithLocale(conceptCommon.getDescription(), conceptMetaData.getLocale(), concept);
        }
        concept.setConceptClass(conceptMetaData.getConceptClass());
        return concept;
    }


    private void setDescriptionWithLocale(String description, Locale locale, Concept concept) {
        if (descriptionAlreadyExistsInLocale(concept, locale)) {
            concept.getDescription(locale).setDescription(description);
        } else {
            concept.addDescription(constructDescription(description, locale));
        }
    }

    private boolean descriptionAlreadyExistsInLocale(Concept concept, Locale locale) {
        locale = (locale != null ? locale: Context.getLocale());
        if (concept.getDescription(locale) == null)
            return false;
        return concept.getDescription(locale).getLocale().equals(locale);
    }
}
