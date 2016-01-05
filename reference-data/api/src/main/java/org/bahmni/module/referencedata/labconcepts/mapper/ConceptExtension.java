package org.bahmni.module.referencedata.labconcepts.mapper;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.bahmni.module.referencedata.labconcepts.contract.ResourceReference;
import org.openmrs.Concept;
import org.openmrs.ConceptDescription;
import org.openmrs.ConceptName;
import org.openmrs.ConceptNumeric;
import org.openmrs.api.ConceptNameType;
import org.openmrs.api.context.Context;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ConceptExtension {
    public static String getDescription(Concept concept) {
        ConceptDescription description = concept.getDescription();
        if (description != null) {
            return description.getDescription();
        }
        return null;
    }

    public static String getDescriptionOrName(Concept concept) {
        ConceptDescription description = concept.getDescription();
        if (description != null) {
            return description.getDescription();
        }
        return concept.getName(Context.getLocale()).getName();
    }

    public static ConceptDescription constructDescription(String description, Locale locale) {
        if (StringUtils.isBlank(description)) return null;
        if (locale == null) {
            locale = Context.getLocale();
        }
        ConceptDescription conceptDescription = new ConceptDescription(description, locale);
        return conceptDescription;
    }


    public static ConceptName getConceptName(String name) {
        ConceptName conceptName = new ConceptName();
        conceptName.setName(name);
        conceptName.setLocale(Context.getLocale());
        return conceptName;
    }

    public static ConceptName getConceptName(String name, Locale locale) {
        ConceptName conceptName = new ConceptName();
        conceptName.setName(name);
        conceptName.setLocale(locale != null ? locale: Context.getLocale());
        return conceptName;
    }

    public static ConceptName getConceptName(String name, ConceptNameType conceptNameType) {
        ConceptName conceptName = getConceptName(name);
        conceptName.setConceptNameType(conceptNameType);
        return conceptName;
    }

    public static ConceptName getConceptName(String name, ConceptNameType conceptNameType, Locale locale) {
        ConceptName conceptName = getConceptName(name, conceptNameType);
        conceptName.setConceptNameType(conceptNameType);
        conceptName.setLocale(locale != null ? locale: Context.getLocale());
        return conceptName;
    }

    public static String getUnits(Concept concept) {
        ConceptNumeric conceptNumeric = Context.getConceptService().getConceptNumeric(concept.getConceptId());
        return conceptNumeric == null ? null : conceptNumeric.getUnits();
    }

    public static boolean isActive(Concept setMember) {
        return !setMember.isRetired();
    }

    public static org.openmrs.Concept addConceptName(org.openmrs.Concept concept, ConceptName conceptName) {
        if (conceptName.getName() == null) return concept;
        for (ConceptName name : concept.getNames()) {
            if (isFullySpecifiedName(conceptName) && isFullySpecifiedName(name) && !name.getName().equals(conceptName.getName()) && name.getLocale().equals(conceptName.getLocale())) {
                name.setName(conceptName.getName());
                return concept;
            } else if (isShortName(conceptName) && isShortName(name) && !name.getName().equals(conceptName.getName()) && name.getLocale().equals(conceptName.getLocale())) {
                name.setName(conceptName.getName());
                return concept;
            } else if (name.getName().equals(conceptName.getName()) && name.getLocale().equals(conceptName.getLocale())) {
                return concept;
            }
        }
        concept.addName(conceptName);
        return concept;
    }

    private static boolean isShortName(ConceptName conceptName) {
        return ObjectUtils.equals(conceptName.getConceptNameType(), ConceptNameType.SHORT);
    }

    private static boolean isFullySpecifiedName(ConceptName conceptName) {
        return ObjectUtils.equals(conceptName.getConceptNameType(), ConceptNameType.FULLY_SPECIFIED);
    }


    public static boolean isOfConceptClass(Concept concept, String conceptClassName) {
        return concept.getConceptClass() != null && concept.getConceptClass().getName() != null && concept.getConceptClass().getName().equals(conceptClassName);
    }

    public static boolean isOfConceptClassByUUID(Concept concept, String conceptClassUUID) {
        return concept.getConceptClass() != null && concept.getConceptClass().getUuid().equals(conceptClassUUID);
    }

    public static List<ResourceReference> getResourceReferencesOfConceptClass(List<Concept> setMembers, String conceptClass) {
        ResourceReferenceMapper resourceReferenceMapper = new ResourceReferenceMapper();
        List<ResourceReference> resourceReferences = new ArrayList<>();
        for (Concept setMember : setMembers) {
            if (isOfConceptClass(setMember, conceptClass)) {
                resourceReferences.add(resourceReferenceMapper.map(setMember));
            }
        }
        return resourceReferences;
    }

}
