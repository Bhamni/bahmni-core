package org.bahmni.module.referencedata.labconcepts.mapper;

import org.apache.commons.lang3.StringUtils;
import org.bahmni.module.referencedata.labconcepts.contract.ConceptCommon;
import org.bahmni.module.referencedata.labconcepts.model.ConceptMetaData;
import org.openmrs.Concept;
import org.openmrs.api.ConceptNameType;
import org.openmrs.api.context.Context;

import java.util.Locale;

import static org.bahmni.module.referencedata.labconcepts.mapper.ConceptExtension.addConceptName;
import static org.bahmni.module.referencedata.labconcepts.mapper.ConceptExtension.constructDescription;
import static org.bahmni.module.referencedata.labconcepts.mapper.ConceptExtension.getConceptName;

public class ConceptCommonMapper {

    public org.openmrs.Concept map(ConceptCommon conceptCommon, ConceptMetaData conceptMetaData) {
        org.openmrs.Concept openmrsConcept = new org.openmrs.Concept();
        if (conceptMetaData.getExistingConcept() != null) {
            openmrsConcept = conceptMetaData.getExistingConcept();
        } else if (StringUtils.isNotBlank(conceptCommon.getUuid())){
            openmrsConcept.setUuid(conceptCommon.getUuid());
        }
        String displayName = conceptCommon.getDisplayName();
        openmrsConcept = addConceptName(openmrsConcept, getConceptName(conceptCommon.getUniqueName(), ConceptNameType.FULLY_SPECIFIED, conceptMetaData.getLocale()));
        if (displayName != null) {
            openmrsConcept = addConceptName(openmrsConcept, getConceptName(conceptCommon.getDisplayName(), ConceptNameType.SHORT, conceptMetaData.getLocale()));
        }

        if (!StringUtils.isBlank(conceptCommon.getDescription())) {
            setDescriptionWithLocale(conceptCommon.getDescription(), conceptMetaData.getLocale(), openmrsConcept);
        }
        openmrsConcept.setConceptClass(conceptMetaData.getConceptClass());
        return openmrsConcept;
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
