package org.bahmni.module.referencedata.labconcepts.service;

import org.bahmni.module.referencedata.labconcepts.contract.Concept;
import org.bahmni.module.referencedata.labconcepts.contract.ConceptCommon;
import org.bahmni.module.referencedata.labconcepts.model.ConceptMetaData;

public interface ConceptMetaDataService {
    public ConceptMetaData getConceptMetaData(ConceptCommon conceptCommon);
}
