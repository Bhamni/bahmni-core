package org.bahmni.module.referencedata.web.service.impl;

import org.apache.commons.lang.StringUtils;
import org.bahmni.module.referencedata.web.contract.Concept;
import org.bahmni.module.referencedata.web.contract.mapper.ConceptMapper;
import org.bahmni.module.referencedata.web.service.ReferenceDataConceptService;
import org.openmrs.ConceptClass;
import org.openmrs.ConceptDatatype;
import org.openmrs.api.APIException;
import org.openmrs.api.ConceptService;
import org.openmrs.api.context.Context;
import org.springframework.beans.factory.annotation.Autowired;
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
    public org.openmrs.Concept saveConcept(Concept concept) {
        if(StringUtils.isBlank(concept.getUniqueName())){
            throw new APIException("Concept unique name Cannot be empty");
        }

        ConceptClass conceptClassName = conceptService.getConceptClassByName(concept.getClassName());
        if (conceptClassName == null) {
            throw new APIException("Concept Class " + concept.getClassName() + " not found");
        }

        ConceptDatatype conceptDatatype = conceptService.getConceptDatatypeByName(concept.getDataType());
        if (conceptDatatype == null) {
            throw new APIException("Concept Datatype " + concept.getDataType() + " not found");
        }

        org.openmrs.Concept mappedConcept = conceptMapper.map(concept, conceptClassName, conceptDatatype);
        return conceptService.saveConcept(mappedConcept);
    }
}
