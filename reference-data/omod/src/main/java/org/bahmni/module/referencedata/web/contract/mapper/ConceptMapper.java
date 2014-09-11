package org.bahmni.module.referencedata.web.contract.mapper;


import org.bahmni.module.referencedata.web.contract.RequestConcept;
import org.openmrs.ConceptClass;
import org.openmrs.ConceptDatatype;
import org.openmrs.ConceptDescription;
import org.openmrs.ConceptName;
import org.openmrs.api.context.Context;
import org.springframework.stereotype.Component;

import java.util.HashSet;
public class ConceptMapper {
    public org.openmrs.Concept map(RequestConcept requestConcept, ConceptClass conceptClass, ConceptDatatype conceptDatatype) {

        org.openmrs.Concept openmrsConcept = new org.openmrs.Concept();
        ConceptName conceptName = new ConceptName(requestConcept.getUniqueName(), Context.getLocale());
        openmrsConcept.setFullySpecifiedName(conceptName);

        ConceptName shortName = new ConceptName(requestConcept.getDisplayName(), Context.getLocale());
        openmrsConcept.setShortName(shortName);

        ConceptDescription conceptDescription = new ConceptDescription(requestConcept.getDescription(), Context.getLocale());

        HashSet<ConceptDescription> descriptions = new HashSet<>();
        descriptions.add(conceptDescription);
        openmrsConcept.setDescriptions(descriptions);

        openmrsConcept.setConceptClass(conceptClass);
        openmrsConcept.setDatatype(conceptDatatype);

        return openmrsConcept;
    }
}
