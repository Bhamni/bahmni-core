package org.bahmni.module.referencedata.web.service.impl;

import org.bahmni.module.referencedata.web.contract.Concept;
import org.bahmni.module.referencedata.web.contract.mapper.ConceptMapper;
import org.bahmni.module.referencedata.web.service.ReferenceDataConceptService;
import org.openmrs.ConceptClass;
import org.openmrs.ConceptDatatype;
import org.openmrs.api.APIException;
import org.openmrs.api.ConceptService;
import org.openmrs.api.context.Context;
import org.springframework.stereotype.Service;

@Service
public class ReferenceDataConceptServiceImpl implements ReferenceDataConceptService {
    private ConceptMapper conceptMapper;
    private ConceptService conceptService;

    public ReferenceDataConceptServiceImpl() {
        this.conceptMapper = new ConceptMapper();
        conceptService = Context.getConceptService();
    }

    @Override
    public org.openmrs.Concept saveConcept(Concept conceptData) throws APIException {
        ConceptClass conceptClassName = conceptService.getConceptClassByName(conceptData.getClassName());
        ConceptDatatype conceptDatatype = conceptService.getConceptDatatypeByName(conceptData.getDataType());
        validate(conceptData, conceptClassName, conceptDatatype);
        org.openmrs.Concept mappedConcept = conceptMapper.map(conceptData, conceptClassName, conceptDatatype);
        return conceptService.saveConcept(mappedConcept);
    }

    private void validate(Concept conceptData, ConceptClass conceptClassName, ConceptDatatype conceptDatatype) {
        StringBuilder stringBuilder = new StringBuilder();
        if (conceptClassName == null) {
            stringBuilder.append("Concept Class " + conceptData.getClassName() + " not found\n");
        }
        if (conceptDatatype == null) {
            stringBuilder.append("Concept Datatype " + conceptData.getDataType() + " not found\n");
        }
        if(!stringBuilder.toString().isEmpty()){
            throw new APIException(stringBuilder.toString());
        }
    }
}
