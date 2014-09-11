package org.bahmni.module.referencedata.web.service;

import org.bahmni.module.referencedata.web.contract.RequestConcept;

public interface ReferenceDataConceptService {
    public org.openmrs.Concept saveConcept(RequestConcept requestConcept);
}
