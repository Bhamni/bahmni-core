package org.bahmni.module.admin.concepts.mapper;

import org.bahmni.csv.KeyValue;
import org.bahmni.module.admin.csv.models.ConceptReferenceTermRow;
import org.bahmni.module.admin.csv.models.ConceptRow;
import org.bahmni.module.referencedata.labconcepts.contract.Concept;
import org.bahmni.module.referencedata.labconcepts.contract.ConceptReferenceTerm;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class ConceptMapperTest {

    private ConceptMapper conceptMapper;

    @Before
    public void setUp() throws Exception {
        conceptMapper = new ConceptMapper();
    }

    @Test
    public void map_concept_row_to_concept_dto() throws Exception {
        ConceptRow conceptRow = new ConceptRow();
        conceptRow.name = "UniqueName";
        conceptRow.uuid = UUID.randomUUID().toString();
        conceptRow.shortName = "UName";
        conceptRow.conceptClass = "Finding";
        Concept mappedConcept = conceptMapper.map(conceptRow);
        assertEquals(conceptRow.name, mappedConcept.getUniqueName());
        assertEquals(conceptRow.shortName, mappedConcept.getDisplayName());
        assertEquals(conceptRow.conceptClass, mappedConcept.getClassName());
        assertEquals(conceptRow.getDataType(), mappedConcept.getDataType());
        assertEquals(conceptRow.getUuid(), mappedConcept.getUuid());
    }

    @Test
    public void set_default_datatype_to_NA() throws Exception {
        Concept mappedConcept = conceptMapper.map(new ConceptRow());
        assertEquals("N/A", mappedConcept.getDataType());
    }

    @Test
    public void get_empty_list_for_no_answers() throws Exception {
        Concept mappedConcept = conceptMapper.map(new ConceptRow());
        assertEquals(0, mappedConcept.getAnswers().size());
    }

    @Test
    public void get_empty_list_for_no_synonyms() throws Exception {
        Concept mappedConcept = conceptMapper.map(new ConceptRow());
        assertEquals(0, mappedConcept.getSynonyms().size());
    }

    @Test
    public void map_concept_reference_term() throws Exception {
        ConceptRow conceptRow = new ConceptRow();
        ConceptReferenceTermRow referenceTermRowOne = new ConceptReferenceTermRow("source", "codeOne", "SAME-AS");
        ConceptReferenceTermRow referenceTermRowTwo = new ConceptReferenceTermRow("source", "codeTwo", "SAME-AS");
        conceptRow.setReferenceTerms(Arrays.asList(referenceTermRowOne, referenceTermRowTwo));
        Concept mappedConcept = conceptMapper.map(conceptRow);

        assertEquals(2, mappedConcept.getConceptReferenceTermsList().size());
        assertEquals("codeOne", mappedConcept.getConceptReferenceTermsList().get(0).getReferenceTermCode());
        assertEquals("codeTwo", mappedConcept.getConceptReferenceTermsList().get(1).getReferenceTermCode());
    }

    @Test
    public void should_not_map_empty_synonyms() throws Exception {
        List<KeyValue> synonyms = new ArrayList<>();
        synonyms.add(new KeyValue("Synonym.1", ""));
        synonyms.add(new KeyValue("Synonym.2", "Synonym"));
        ConceptRow conceptRow = new ConceptRow();
        conceptRow.synonyms = synonyms;
        Concept mappedConcept = conceptMapper.map(conceptRow);
        assertEquals(1, mappedConcept.getSynonyms().size());
        assertEquals("Synonym", mappedConcept.getSynonyms().get(0));
    }

    @Test
    public void should_not_map_empty_answers() throws Exception {
        List<KeyValue> answers = new ArrayList<>();
        answers.add(new KeyValue("1", ""));
        answers.add(new KeyValue("2", "Answer"));
        ConceptRow conceptRow = new ConceptRow();
        conceptRow.answers = answers;
        Concept mappedConcept = conceptMapper.map(conceptRow);
        assertEquals(1, mappedConcept.getAnswers().size());
        assertEquals("Answer", mappedConcept.getAnswers().get(0));
    }

    @Test
    public void map_description_null_if_empty() throws Exception {
        ConceptRow conceptRow = new ConceptRow();
        conceptRow.description = "";
        Concept mappedConcept = conceptMapper.map(conceptRow);
        assertNull(mappedConcept.getDescription());
    }

    @Test
    public void uuid_null_if_not_specified() throws Exception {
        ConceptRow conceptRow = new ConceptRow();
        conceptRow.uuid = null;
        Concept map = conceptMapper.map(conceptRow);
        assertNull(map.getUuid());
    }

    @Test
    public void uuid_null_if_not_valid() throws Exception {
        ConceptRow conceptRow = new ConceptRow();
        conceptRow.uuid = "invalid UUID";
        Concept map = conceptMapper.map(conceptRow);
        assertNull(map.getUuid());
    }
}