package org.bahmni.module.bahmnicore.dao;


import org.openmrs.Concept;
import org.openmrs.Drug;

import java.util.Collection;
import java.util.List;

public interface BahmniConceptDao {
    Collection<Concept> searchByQuestion(Concept questionConcept, String searchQuery);
    Concept getConceptByFullySpecifiedName(String fullySpecifiedConceptName);
    Collection<Drug> getDrugByListOfConcepts(Collection<Concept> conceptSet);
}
