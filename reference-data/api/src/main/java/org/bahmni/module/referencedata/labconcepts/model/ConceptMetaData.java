package org.bahmni.module.referencedata.labconcepts.model;

import org.openmrs.Concept;
import org.openmrs.ConceptClass;
import org.openmrs.ConceptDatatype;

import java.util.Locale;

public class ConceptMetaData {
    private Concept existingConcept;
    private ConceptDatatype conceptDatatype;
    private ConceptClass conceptClass;
    private Locale locale;

    public ConceptMetaData(Concept existingConcept, ConceptDatatype conceptDatatype, ConceptClass conceptClass, Locale locale) {
        this.existingConcept = existingConcept;
        this.conceptDatatype = conceptDatatype;
        this.conceptClass = conceptClass;
        this.locale = locale;
    }

    public Concept getExistingConcept() {
        return existingConcept;
    }

    public void setExistingConcept(Concept existingConcept) {
        this.existingConcept = existingConcept;
    }

    public ConceptDatatype getConceptDatatype() {
        return conceptDatatype;
    }

    public void setConceptDatatype(ConceptDatatype conceptDatatype) {
        this.conceptDatatype = conceptDatatype;
    }

    public ConceptClass getConceptClass() {
        return conceptClass;
    }

    public void setConceptClass(ConceptClass conceptClass) {
        this.conceptClass = conceptClass;
    }

    public Locale getLocale() {
        return locale;
    }

    public void setLocale(Locale locale) {
        this.locale = locale;
    }

}
