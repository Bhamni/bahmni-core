package org.bahmni.module.referencedata.labconcepts.service.impl;

import org.bahmni.module.referencedata.labconcepts.contract.Concept;
import org.bahmni.module.referencedata.labconcepts.contract.ConceptCommon;
import org.bahmni.module.referencedata.labconcepts.contract.ConceptReferenceTerm;
import org.bahmni.module.referencedata.labconcepts.contract.ConceptSet;
import org.bahmni.module.referencedata.labconcepts.contract.Concepts;
import org.bahmni.module.referencedata.labconcepts.mapper.ConceptMapper;
import org.bahmni.module.referencedata.labconcepts.mapper.ConceptSetMapper;
import org.bahmni.module.referencedata.labconcepts.model.ConceptMetaData;
import org.bahmni.module.referencedata.labconcepts.service.ConceptMetaDataService;
import org.bahmni.module.referencedata.labconcepts.service.ReferenceDataConceptReferenceTermService;
import org.bahmni.module.referencedata.labconcepts.service.ReferenceDataConceptService;
import org.bahmni.module.referencedata.labconcepts.validator.ConceptValidator;
import org.openmrs.ConceptAnswer;
import org.openmrs.ConceptMap;
import org.openmrs.api.ConceptService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Service
public class ReferenceDataConceptServiceImpl implements ReferenceDataConceptService {
    private final ConceptMetaDataService conceptMetaDataService;
    private ConceptService conceptService;
    private ReferenceDataConceptReferenceTermService referenceDataConceptReferenceTermService;
    private ConceptMapper conceptMapper;
    private ConceptSetMapper conceptSetMapper;
    private final ConceptValidator conceptValidator;
    private List<String> notFound;

    @Autowired
    public ReferenceDataConceptServiceImpl(ConceptService conceptService, ReferenceDataConceptReferenceTermService referenceDataConceptReferenceTermService, ConceptMetaDataService conceptMetaDataService) {
        this.conceptMapper = new ConceptMapper();
        this.conceptSetMapper = new ConceptSetMapper();
        this.conceptService = conceptService;
        this.referenceDataConceptReferenceTermService = referenceDataConceptReferenceTermService;
        this.conceptMetaDataService = conceptMetaDataService;
        conceptValidator = new ConceptValidator();

    }

    @Override
    public org.openmrs.Concept saveConcept(Concept conceptData) {
        ConceptMetaData conceptMetaData = conceptMetaDataService.getConceptMetaData(conceptData);
        org.openmrs.Concept mappedConcept = getConcept(conceptData, conceptMetaData);
        return conceptService.saveConcept(mappedConcept);
    }


    @Override
    public org.openmrs.Concept saveConcept(ConceptSet conceptSet) {
        ConceptMetaData conceptMetaData = conceptMetaDataService.getConceptMetaData(conceptSet);
        org.openmrs.Concept mappedConceptSet = getConceptSet(conceptSet, conceptMetaData);
        return conceptService.saveConcept(mappedConceptSet);
    }


    @Override
    public Concepts getConcept(String conceptName) {
        org.openmrs.Concept concept = conceptService.getConceptByName(conceptName);
        if (concept == null) {
            return null;
        }
        Concepts concepts = conceptSetMapper.mapAll(concept);
        return concepts;
    }

    private org.openmrs.Concept getConceptSet(ConceptSet conceptSet, ConceptMetaData conceptMetaData) {
        List<org.openmrs.Concept> setMembers = getSetMembers(conceptSet.getChildren());
        conceptValidator.validate(conceptSet, conceptMetaData.getConceptClass(), conceptMetaData.getConceptDatatype(), notFound);
        org.openmrs.Concept mappedConceptSet = conceptSetMapper.map(conceptSet, setMembers,conceptMetaData);
        clearAndAddConceptMappings(conceptSet, mappedConceptSet);
        return mappedConceptSet;
    }

    private org.openmrs.Concept getConcept(Concept conceptData, ConceptMetaData conceptMetaData) {
        List<ConceptAnswer> conceptAnswers = getConceptAnswers(conceptData.getAnswers());
        conceptValidator.validate(conceptData, conceptMetaData.getConceptClass(), conceptMetaData.getConceptDatatype(), notFound);
        org.openmrs.Concept mappedConcept = conceptMapper.map(conceptData, conceptMetaData, conceptAnswers);
        clearAndAddConceptMappings(conceptData, mappedConcept);
        return mappedConcept;
    }

    private void clearAndAddConceptMappings(ConceptCommon conceptData, org.openmrs.Concept mappedConcept) {
        Collection<ConceptMap> conceptMappings = mappedConcept.getConceptMappings();
        conceptMappings.clear();
        mappedConcept.setConceptMappings(conceptMappings);

        for(ConceptReferenceTerm conceptReferenceTerm : conceptData.getConceptReferenceTermsList()) {
            conceptMapper.addConceptMap(mappedConcept, referenceDataConceptReferenceTermService.getConceptMap(conceptReferenceTerm));
        }
    }

    private List<ConceptAnswer> getConceptAnswers(List<String> answers) {
        List<ConceptAnswer> conceptAnswers = new ArrayList<>();
        notFound = new ArrayList<>();
        if (answers == null) return conceptAnswers;
        for (String answer : answers) {
            org.openmrs.Concept answerConcept = conceptService.getConceptByName(answer);
            if (answerConcept == null) {
                notFound.add(answer);
            }
            conceptAnswers.add(constructConceptAnswer(answerConcept));
        }
        return conceptAnswers;
    }

    private List<org.openmrs.Concept> getSetMembers(List<String> children) {
        List<org.openmrs.Concept> setMembers = new ArrayList<>();
        notFound = new ArrayList<>();
        if (children == null) return setMembers;
        for (String child : children) {
            org.openmrs.Concept childConcept = conceptService.getConceptByName(child);
            if (childConcept == null) {
                notFound.add(child);
            }
            setMembers.add(childConcept);
        }
        return setMembers;
    }

    private ConceptAnswer constructConceptAnswer(org.openmrs.Concept answerConcept) {
        ConceptAnswer conceptAnswer = new ConceptAnswer(answerConcept);
        return conceptAnswer;
    }
}
