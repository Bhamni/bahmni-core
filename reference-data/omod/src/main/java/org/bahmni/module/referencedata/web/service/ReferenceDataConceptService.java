package org.bahmni.module.referencedata.web.service;

import org.bahmni.module.referencedata.web.contract.Concept;

public interface ReferenceDataConceptService {
    public org.openmrs.Concept saveConcept(Concept concept) throws Throwable;
}
