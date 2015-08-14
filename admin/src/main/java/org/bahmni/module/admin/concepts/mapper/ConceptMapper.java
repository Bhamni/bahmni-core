package org.bahmni.module.admin.concepts.mapper;

import org.apache.commons.lang3.StringUtils;
import org.bahmni.csv.KeyValue;
import org.bahmni.module.admin.csv.models.ConceptReferenceTermRow;
import org.bahmni.module.admin.csv.models.ConceptRow;
import org.bahmni.module.referencedata.labconcepts.contract.Concept;
import org.bahmni.module.referencedata.labconcepts.contract.ConceptReferenceTerm;
import org.openmrs.ConceptAnswer;
import org.openmrs.ConceptDescription;
import org.openmrs.ConceptMap;
import org.openmrs.ConceptName;
import org.openmrs.api.context.Context;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static org.bahmni.module.admin.csv.utils.CSVUtils.getKeyValueList;

public class ConceptMapper {


    public ConceptMapper() {
    }

    public Concept map(ConceptRow conceptRow) {
        Concept concept = new Concept();
        concept.setUuid(conceptRow.getUuid());
        concept.setClassName(conceptRow.conceptClass);
        concept.setDataType(conceptRow.getDataType());
        concept.setDescription(conceptRow.getDescription());
        concept.setUniqueName(conceptRow.getName());
        concept.setDisplayName(conceptRow.getShortName());
        concept.setUnits(conceptRow.getUnits());
        concept.setHiNormal(conceptRow.getHiNormal());
        concept.setLowNormal(conceptRow.getLowNormal());
        addSynonyms(conceptRow, concept);
        addAnswers(conceptRow, concept);
        addConceptReferenceTerms(conceptRow, concept);

        return concept;
    }

    private void addSynonyms(ConceptRow conceptRow, Concept concept) {
        List<String> synonyms = new ArrayList<>();
        for (KeyValue synonym : conceptRow.getSynonyms()) {
            if (!StringUtils.isEmpty(synonym.getValue())) {
                synonyms.add(synonym.getValue());
            }
        }
        concept.setSynonyms(synonyms);
    }

    private void addAnswers(ConceptRow conceptRow, Concept concept) {
        List<String> answers = new ArrayList<>();
        for (KeyValue answer : conceptRow.getAnswers()) {
            if (!StringUtils.isEmpty(answer.getValue())) {
                answers.add(answer.getValue());
            }
        }
        concept.setAnswers(answers);
    }

    private void addConceptReferenceTerms(ConceptRow conceptRow, Concept concept) {
        ConceptReferenceTerm conceptReferenceTerm;
        for (ConceptReferenceTermRow referenceTerm : conceptRow.getReferenceTerms()) {
            conceptReferenceTerm = new ConceptReferenceTerm(referenceTerm.getReferenceTermCode(), referenceTerm.getReferenceTermRelationship(), referenceTerm.getReferenceTermSource());
            concept.getConceptReferenceTermsList().add(conceptReferenceTerm);
        }
    }

    //TODO need to change
    public ConceptRow map(Concept concept) {
        String name = concept.getUniqueName();
        String description = concept.getDescription();
        String shortName = concept.getDisplayName();
        String conceptClass = concept.getClassName();
        String conceptDatatype = concept.getDataType();
        List<KeyValue> conceptSynonyms = getKeyValueList("synonym", concept.getSynonyms());
        List<KeyValue> conceptAnswers = getKeyValueList("answer", concept.getAnswers());

        List<ConceptReferenceTermRow> referenceTermRows = new ArrayList<>();
        for (ConceptReferenceTerm term : concept.getConceptReferenceTermsList()) {
            referenceTermRows.add(new ConceptReferenceTermRow(term.getReferenceTermSource(), term.getReferenceTermCode(), term.getReferenceTermRelationship()));
        }
        String uuid = concept.getUuid();
        String units = concept.getUnits();
        String hiNormal = concept.getHiNormal();
        String lowNormal = concept.getLowNormal();
        return new ConceptRow(uuid, name, description, conceptClass, shortName, conceptDatatype, units, hiNormal, lowNormal, referenceTermRows, conceptSynonyms, conceptAnswers);
    }
}
