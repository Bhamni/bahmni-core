package org.bahmni.module.referencedata.labconcepts.mapper;


import org.bahmni.module.referencedata.labconcepts.contract.Concept;
import org.bahmni.module.referencedata.labconcepts.contract.ConceptReferenceTerm;
import org.openmrs.*;
import org.openmrs.api.context.Context;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;

import static org.bahmni.module.referencedata.labconcepts.mapper.ConceptExtension.addConceptName;
import static org.bahmni.module.referencedata.labconcepts.mapper.ConceptExtension.getConceptName;

public class ConceptMapper {

    private final ConceptAnswerMapper conceptAnswerMapper;
    private final ConceptNumericMapper conceptNumericMapper;
    private final ConceptCommonMapper conceptCommonMapper;

    public ConceptMapper() {
        conceptAnswerMapper = new ConceptAnswerMapper();
        conceptNumericMapper = new ConceptNumericMapper();
        conceptCommonMapper = new ConceptCommonMapper();
    }

    public org.openmrs.Concept map(Concept conceptData, ConceptClass conceptClass, ConceptDatatype conceptDatatype, List<ConceptAnswer> answers, org.openmrs.Concept existingConcept) {
        org.openmrs.Concept concept = conceptCommonMapper.map(conceptData, conceptClass, existingConcept);
        for (String conceptName : conceptData.getSynonyms()) {
            concept = addConceptName(concept, getConceptName(conceptName), conceptData.getLocale());
        }
        if (conceptDatatype.isNumeric()) {
            concept = conceptNumericMapper.map(concept, conceptData, existingConcept);
        }
        concept.setDatatype(conceptDatatype);
        concept = conceptAnswerMapper.map(concept, answers);
        return concept;
    }

    public org.openmrs.Concept addConceptMap(org.openmrs.Concept mappedConcept, ConceptMap conceptMap) {
        if (conceptMap == null) return mappedConcept;
        for (ConceptMap existingMap : mappedConcept.getConceptMappings()) {
            if (existingMap.getConceptReferenceTerm().equals(conceptMap.getConceptReferenceTerm())) {
                return mappedConcept;
            }
        }
        mappedConcept.addConceptMapping(conceptMap);
        return mappedConcept;
    }

    public Concept map(org.openmrs.Concept concept) {
        String conceptDescription = null;
        String conceptShortname = null;
        String locale = Context.getLocale().toString();
        String name = concept.getName(Context.getLocale()).getName();
        ConceptDescription description = concept.getDescription(Context.getLocale());
        if (description != null) {
            conceptDescription = description.getDescription();
        }
        ConceptName shortName = concept.getShortNameInLocale(Context.getLocale());
        if (shortName != null) {
            conceptShortname = shortName.getName();
        }
        String conceptClass = concept.getConceptClass().getName();
        String conceptDatatype = concept.getDatatype().getName();
        List<String> conceptSynonyms = getSynonyms(concept);
        List<String> conceptAnswers = getAnswers(concept);
        Collection<ConceptMap> conceptMappings = concept.getConceptMappings();

        List<org.bahmni.module.referencedata.labconcepts.contract.ConceptReferenceTerm> referenceTerms = new ArrayList<>();
        for (ConceptMap conceptMapping : conceptMappings) {
            org.openmrs.ConceptReferenceTerm term = conceptMapping.getConceptReferenceTerm();
            referenceTerms.add(new ConceptReferenceTerm(term.getCode(), conceptMapping.getConceptMapType().getName(), term.getConceptSource().getName()));
        }

        String uuid = concept.getUuid();
        return new Concept(uuid, name, conceptDescription, conceptClass, conceptShortname, locale, referenceTerms, conceptSynonyms, conceptAnswers, conceptDatatype);
    }

    private List<String> getAnswers(org.openmrs.Concept concept) {
        List<String> answers = new ArrayList<>();
        for (ConceptAnswer conceptAnswer : concept.getAnswers()) {
            answers.add(conceptAnswer.getAnswerConcept().getName(Context.getLocale()).getName());
        }
        return answers;
    }

    private List<String> getSynonyms(org.openmrs.Concept concept) {
        List<String> synonyms = new ArrayList<>();
        for (ConceptName synonym : concept.getSynonyms()) {
            synonyms.add(synonym.getName());
        }
        return synonyms;
    }

    public List<Concept> mapAll(org.openmrs.Concept concept) {
        List<Concept> conceptList = new ArrayList<>();
        for (ConceptAnswer conceptAnswer : concept.getAnswers()) {
            conceptList.addAll(mapAll(conceptAnswer.getAnswerConcept()));
        }
        conceptList.add(map(concept));
        return conceptList;
    }

}