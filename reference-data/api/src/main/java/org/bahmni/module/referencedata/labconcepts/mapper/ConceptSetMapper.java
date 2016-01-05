package org.bahmni.module.referencedata.labconcepts.mapper;

import org.bahmni.module.referencedata.labconcepts.contract.*;
import org.bahmni.module.referencedata.labconcepts.contract.ConceptSet;
import org.bahmni.module.referencedata.labconcepts.model.ConceptMetaData;
import org.openmrs.*;
import org.openmrs.Concept;
import org.openmrs.ConceptReferenceTerm;
import org.openmrs.api.context.Context;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class ConceptSetMapper {

    private final ConceptMapper conceptMapper;
    private final ConceptCommonMapper conceptCommonMapper;
    private final SetMemberMapper setMemberMapper;

    public ConceptSetMapper() {
        conceptMapper = new ConceptMapper();
        conceptCommonMapper = new ConceptCommonMapper();
        setMemberMapper = new SetMemberMapper();
    }

    public Concept map(ConceptSet conceptSet, List<Concept> childConcepts, ConceptMetaData conceptMetaData) {
        Concept concept = conceptCommonMapper.map(conceptSet, conceptMetaData);
        concept.setSet(true);
        concept.setDatatype(conceptMetaData.getConceptDatatype());
        concept = setMemberMapper.map(concept, childConcepts);
        return concept;
    }


    public ConceptSet map(Concept concept) {
        String conceptDescription = null, conceptShortname = null;
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
        List<String> children = getSetMembers(concept);
        Collection<ConceptMap> conceptMappings = concept.getConceptMappings();


        List<org.bahmni.module.referencedata.labconcepts.contract.ConceptReferenceTerm> referenceTerms = new ArrayList<>();
        for (ConceptMap conceptMapping : conceptMappings) {
            org.openmrs.ConceptReferenceTerm term = conceptMapping.getConceptReferenceTerm();
            referenceTerms.add(new org.bahmni.module.referencedata.labconcepts.contract.ConceptReferenceTerm(term.getCode(), conceptMapping.getConceptMapType().getName(), term.getConceptSource().getName()));
        }

        String uuid = concept.getUuid();
        ConceptSet conceptSet = new ConceptSet(uuid, name, conceptDescription, conceptClass, conceptShortname, referenceTerms, children);
        return conceptSet;
    }

    private List<String> getSetMembers(Concept concept) {
        List<String> setMembers = new ArrayList<>();
        for (Concept setMember : concept.getSetMembers()) {
            setMembers.add(setMember.getName(Context.getLocale()).getName());
        }
        return setMembers;
    }

    public Concepts mapAll(Concept concept) {
        List<ConceptSet> conceptSetList = new ArrayList<>();
        List<org.bahmni.module.referencedata.labconcepts.contract.Concept> conceptList = new ArrayList<>();
        for (Concept setMember : concept.getSetMembers()) {
            if (setMember.isSet()) {
                Concepts concepts = mapAll(setMember);
                conceptSetList.addAll(concepts.getConceptSetList());
                conceptList.addAll(concepts.getConceptList());
            } else {
                conceptList.addAll(conceptMapper.mapAll(setMember));
            }
        }
        conceptSetList.add(map(concept));
        Concepts concepts = new Concepts();
        concepts.setConceptList(conceptList);
        concepts.setConceptSetList(conceptSetList);
        return concepts;
    }
}
