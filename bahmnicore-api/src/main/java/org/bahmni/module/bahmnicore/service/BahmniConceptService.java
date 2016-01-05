package org.bahmni.module.bahmnicore.service;

import org.openmrs.Concept;
import org.openmrs.Drug;
import org.openmrs.module.emrapi.encounter.domain.EncounterTransaction;

import java.util.Collection;
import java.util.List;

public interface BahmniConceptService {
    EncounterTransaction.Concept getConceptByName(String conceptName);

    Collection<Concept> searchByQuestion(String questionConcept, String query);
    Collection<Drug> getDrugsByConceptSetName(String conceptSetName);
}
