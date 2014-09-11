package org.bahmni.module.referencedata.web.service.impl;

import org.bahmni.module.referencedata.web.contract.RequestConcept;
import org.bahmni.module.referencedata.web.contract.mapper.ConceptMapper;
import org.bahmni.module.referencedata.web.service.ReferenceDataConceptService;
import org.openmrs.ConceptClass;
import org.openmrs.ConceptDatatype;
import org.openmrs.api.ConceptService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ReferenceDataConceptServiceImpl implements ReferenceDataConceptService {
    @Autowired
    private ConceptService conceptService;
    private ConceptMapper conceptMapper;

    public ReferenceDataConceptServiceImpl() {
    }

    public ReferenceDataConceptServiceImpl(ConceptService conceptService) {
        this.conceptService = conceptService;
        this.conceptMapper = new ConceptMapper();
    }

    @Override
    public org.openmrs.Concept saveConcept(RequestConcept requestConcept) {
        ConceptClass conceptClassName = conceptService.getConceptClassByName(requestConcept.getClassName());
        if(conceptClassName == null){
            throw new RuntimeException("Concept Class " + requestConcept.getClassName() + " not found");
        }
        ConceptDatatype conceptDatatype = conceptService.getConceptDatatypeByName(requestConcept.getDataType());
        if(conceptDatatype == null){
            throw new RuntimeException("Concept Datatype " + requestConcept.getDataType() + " not found");
        }
        org.openmrs.Concept mappedConcept = conceptMapper.map(requestConcept, conceptClassName, conceptDatatype);
        return conceptService.saveConcept(mappedConcept);
    }
}
