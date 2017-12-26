package org.bahmni.module.admin.csv.persister;

import org.bahmni.csv.KeyValue;
import org.bahmni.csv.Messages;
import org.bahmni.module.admin.BaseIntegrationTest;
import org.bahmni.module.admin.csv.models.ConceptReferenceTermRow;
import org.bahmni.module.admin.csv.models.ConceptRow;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.openmrs.Concept;
import org.openmrs.ConceptAnswer;
import org.openmrs.ConceptDatatype;
import org.openmrs.ConceptMap;
import org.openmrs.ConceptName;
import org.openmrs.ConceptNumeric;
import org.openmrs.api.ConceptService;
import org.openmrs.api.context.Context;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class ConceptPersisterIT extends BaseIntegrationTest {

    public static final String SAME_AS = "SAME-AS";

    @Autowired
    private ConceptPersister conceptPersister;

    @Autowired
    private ConceptService conceptService;

    @Before
    public void setUp() throws Exception {
        Context.authenticate("admin", "test");
        executeDataSet("conceptSetup.xml");
    }

    @Test
    public void shouldFailValidationForNoConceptName() throws Exception {
        ConceptRow conceptRow = new ConceptRow();
        Messages errorMessages = conceptPersister.validate(conceptRow);
        assertFalse(errorMessages.isEmpty());
    }

    @Test
    public void shouldFailValidationForNoConceptClass() throws Exception {
        ConceptRow conceptRow = new ConceptRow();
        conceptRow.name = "Concept Name";
        Messages errorMessages = conceptPersister.validate(conceptRow);
        assertFalse(errorMessages.isEmpty());
    }


    @Test
    public void shouldPassValidationIfConceptNameAndConceptClassArePresent() throws Exception {
        ConceptRow conceptRow = new ConceptRow();
        conceptRow.name = "concept Name";
        conceptRow.conceptClass = "concept Class";
        Messages errorMessages = conceptPersister.validate(conceptRow);
        assertTrue(errorMessages.isEmpty());
    }

    @Test
    public void shouldPersistNewConceptWithNameClassAndDescriptionInputOnly() throws Exception {
        ConceptRow conceptRow = new ConceptRow();
        conceptRow.name = "New concept";
        conceptRow.conceptClass = "New Class";
        conceptRow.description = "New concept description";
        Messages errorMessages = conceptPersister.persist(conceptRow);
        assertTrue(errorMessages.isEmpty());
        Context.openSession();
        Context.authenticate("admin", "test");
        Concept persistedConcept = conceptService.getConceptByName(conceptRow.name);
        assertNotNull(persistedConcept);
        assertEquals(conceptRow.name, persistedConcept.getName(Context.getLocale()).getName());
        assertEquals(conceptRow.conceptClass, persistedConcept.getConceptClass().getName());
        assertEquals("New concept description", persistedConcept.getDescription().getDescription());
        assertEquals(0, persistedConcept.getSynonyms().size());
        Context.flushSession();
        Context.closeSession();
    }

    @Test
    public void shouldPersistNewConceptWithNameAndClassAndDatatypeDescriptionShortnameSynonymsInputOnly() throws Exception {
        ConceptRow conceptRow = new ConceptRow();
        conceptRow.name = "New Concept";
        conceptRow.description = "New Description";
        conceptRow.conceptClass = "New Class";
        conceptRow.dataType = "Numeric";
        conceptRow.shortName = "NConcept";
        List<KeyValue> synonyms = new ArrayList<>();
        synonyms.add(new KeyValue("1", "Synonym1"));
        synonyms.add(new KeyValue("2", "Synonym2"));
        conceptRow.synonyms = synonyms;
        Messages errorMessages = conceptPersister.persist(conceptRow);
        assertTrue(errorMessages.isEmpty());
        Context.openSession();
        Context.authenticate("admin", "test");
        Concept persistedConcept = conceptService.getConceptByName(conceptRow.name);
        assertNotNull(persistedConcept);
        assertEquals(conceptRow.name, persistedConcept.getName(Context.getLocale()).getName());
        assertEquals(conceptRow.description, persistedConcept.getDescription(Context.getLocale()).getDescription());
        assertEquals(conceptRow.conceptClass, persistedConcept.getConceptClass().getName());
        assertEquals(ConceptDatatype.NUMERIC_UUID, persistedConcept.getDatatype().getUuid());
        assertEquals(conceptRow.shortName, persistedConcept.getShortestName(Context.getLocale(), false).getName());
        assertEquals(2, persistedConcept.getSynonyms().size());
        for (ConceptName conceptName : persistedConcept.getSynonyms(Context.getLocale())) {
            assertTrue(conceptName.getName().equals("Synonym1") || conceptName.getName().equals("Synonym2"));
        }
        Context.flushSession();
        Context.closeSession();
    }

    @Test
    public void shouldPersistNewConceptWithAnswers() throws Exception {
        ConceptRow conceptRow = new ConceptRow();
        conceptRow.name = "New Concept";
        conceptRow.description = "New Description";
        conceptRow.conceptClass = "New Class";
        conceptRow.dataType = "Coded";
        conceptRow.shortName = "NConcept";
        List<KeyValue> synonyms = new ArrayList<>();
        synonyms.add(new KeyValue("1", "Synonym1"));
        synonyms.add(new KeyValue("2", "Synonym2"));
        conceptRow.synonyms = synonyms;
        List<KeyValue> answers = new ArrayList<>();
        answers.add(new KeyValue("1", "Answer1"));
        answers.add(new KeyValue("2", "Answer2"));
        conceptRow.answers = answers;
        Messages errorMessages = conceptPersister.persist(conceptRow);
        assertTrue(errorMessages.isEmpty());
        Context.openSession();
        Context.authenticate("admin", "test");
        Concept persistedConcept = conceptService.getConceptByName(conceptRow.name);
        assertNotNull(persistedConcept);
        assertEquals(conceptRow.name, persistedConcept.getName(Context.getLocale()).getName());
        assertEquals(conceptRow.description, persistedConcept.getDescription(Context.getLocale()).getDescription());
        assertEquals(conceptRow.conceptClass, persistedConcept.getConceptClass().getName());
        assertEquals(ConceptDatatype.CODED_UUID, persistedConcept.getDatatype().getUuid());
        assertEquals(conceptRow.shortName, persistedConcept.getShortestName(Context.getLocale(), false).getName());
        assertEquals(2, persistedConcept.getSynonyms().size());
        assertEquals(2, persistedConcept.getAnswers().size());
        for (ConceptName conceptName : persistedConcept.getSynonyms(Context.getLocale())) {
            assertTrue(conceptName.getName().equals("Synonym1") || conceptName.getName().equals("Synonym2"));
        }
        for (ConceptAnswer conceptAnswer : persistedConcept.getAnswers()) {
            assertTrue(conceptAnswer.getAnswerConcept().getName(Context.getLocale()).getName().equals("Answer1")
                    || conceptAnswer.getAnswerConcept().getName(Context.getLocale()).getName().equals("Answer2"));
        }
        Context.flushSession();
        Context.closeSession();
    }

    @Test
    public void shouldSetConceptReferenceTerms() throws Exception {
        ConceptRow conceptRow = new ConceptRow();
        conceptRow.name = "New Concept";
        conceptRow.description = "New Description";
        conceptRow.conceptClass = "New Class";
        conceptRow.dataType = "Coded";
        conceptRow.shortName = "NConcept";
        ConceptReferenceTermRow conceptReferenceTermRow = new ConceptReferenceTermRow("org.openmrs.module.emrapi", "New Code", SAME_AS);
        List<ConceptReferenceTermRow> conceptReferenceTermsList = new ArrayList<>(Arrays.asList(conceptReferenceTermRow));
        conceptRow.referenceTerms = conceptReferenceTermsList;

        List<KeyValue> synonyms = new ArrayList<>();
        synonyms.add(new KeyValue("1", "Synonym1"));
        synonyms.add(new KeyValue("2", "Synonym2"));
        conceptRow.synonyms = synonyms;
        List<KeyValue> answers = new ArrayList<>();
        answers.add(new KeyValue("1", "Answer1"));
        answers.add(new KeyValue("2", "Answer2"));
        conceptRow.answers = answers;
        Messages errorMessages = conceptPersister.persist(conceptRow);
        assertTrue(errorMessages.isEmpty());
        Context.openSession();
        Context.authenticate("admin", "test");
        Concept persistedConcept = conceptService.getConceptByName(conceptRow.name);
        assertNotNull(persistedConcept);
        assertEquals(conceptRow.name, persistedConcept.getName(Context.getLocale()).getName());
        assertEquals(conceptRow.description, persistedConcept.getDescription(Context.getLocale()).getDescription());
        assertEquals(conceptRow.conceptClass, persistedConcept.getConceptClass().getName());
        assertEquals(ConceptDatatype.CODED_UUID, persistedConcept.getDatatype().getUuid());
        assertEquals(conceptRow.shortName, persistedConcept.getShortestName(Context.getLocale(), false).getName());
        assertEquals(2, persistedConcept.getSynonyms().size());
        assertEquals(2, persistedConcept.getAnswers().size());
        for (ConceptName conceptName : persistedConcept.getSynonyms(Context.getLocale())) {
            assertTrue(conceptName.getName().equals("Synonym1") || conceptName.getName().equals("Synonym2"));
        }
        for (ConceptAnswer conceptAnswer : persistedConcept.getAnswers()) {
            assertTrue(conceptAnswer.getAnswerConcept().getName(Context.getLocale()).getName().equals("Answer1")
                    || conceptAnswer.getAnswerConcept().getName(Context.getLocale()).getName().equals("Answer2"));
        }
        ArrayList<ConceptMap> conceptMaps = new ArrayList<>(persistedConcept.getConceptMappings());
        ConceptMap conceptMap = conceptMaps.get(0);
        assertEquals(persistedConcept, conceptMap.getConcept());
        assertEquals(conceptReferenceTermRow.getReferenceTermCode(), conceptMap.getConceptReferenceTerm().getCode());
        assertEquals(conceptReferenceTermRow.getReferenceTermRelationship().toLowerCase(), conceptMap.getConceptMapType().toString());
        assertEquals(conceptReferenceTermRow.getReferenceTermSource(), conceptMap.getConceptReferenceTerm().getConceptSource().getName());
        Context.flushSession();
        Context.closeSession();

    }

    @Test
    @Ignore
    public void shouldUpdateDetailsOnExistingConcepts() throws Exception {
        ConceptRow conceptRow = new ConceptRow();
        conceptRow.name = "Existing Concept";
        conceptRow.conceptClass = "New Class";
        conceptRow.description = "Some Description";
        List<KeyValue> synonyms = new ArrayList<>();
        synonyms.add(new KeyValue("1", "Synonym1"));
        synonyms.add(new KeyValue("2", "Synonym2"));
        conceptRow.synonyms = synonyms;
        conceptRow.shortName = "NConcept";
        Messages errorMessages = conceptPersister.persist(conceptRow);
        assertTrue(errorMessages.isEmpty());
        Context.openSession();
        Context.authenticate("admin", "test");
        Concept persistedConcept = conceptService.getConceptByName(conceptRow.name);
        assertNotNull(persistedConcept);
        assertEquals(conceptRow.description, persistedConcept.getDescription(Context.getLocale()).getDescription());
        assertEquals(conceptRow.conceptClass, persistedConcept.getConceptClass().getName());
        assertEquals(conceptRow.shortName, persistedConcept.getShortestName(Context.getLocale(), false).getName());
        assertEquals(2, persistedConcept.getSynonyms().size());
        assertEquals(0, persistedConcept.getAnswers().size());
        for (ConceptName conceptName : persistedConcept.getSynonyms(Context.getLocale())) {
            assertTrue(conceptName.getName().equals("Synonym1") || conceptName.getName().equals("Synonym2"));
        }
        Context.flushSession();
        Context.closeSession();
    }

    @Test
    @Ignore
    public void shouldCreateNewMappingForExistingConcept() throws Exception {
        ConceptRow conceptRow = new ConceptRow();
        conceptRow.name = "Existing Concept";
        conceptRow.conceptClass = "New Class";
        conceptRow.description = "Some Description";
        ConceptReferenceTermRow conceptReferenceTermRow = new ConceptReferenceTermRow("org.openmrs.module.emrapi", "New Code", SAME_AS);
        List<ConceptReferenceTermRow> conceptReferenceTermsList = new ArrayList<>(Arrays.asList(conceptReferenceTermRow));
        conceptRow.referenceTerms = conceptReferenceTermsList;

        List<KeyValue> synonyms = new ArrayList<>();
        synonyms.add(new KeyValue("1", "Synonym1"));
        synonyms.add(new KeyValue("2", "Synonym2"));
        conceptRow.synonyms = synonyms;
        conceptRow.shortName = "NConcept";
        Messages errorMessages = conceptPersister.persist(conceptRow);
        assertTrue(errorMessages.isEmpty());
        Context.openSession();
        Context.authenticate("admin", "test");
        Concept persistedConcept = conceptService.getConceptByName(conceptRow.name);
        assertNotNull(persistedConcept);
        assertEquals(conceptRow.description, persistedConcept.getDescription(Context.getLocale()).getDescription());
        assertEquals(conceptRow.conceptClass, persistedConcept.getConceptClass().getName());
        assertEquals(conceptRow.shortName, persistedConcept.getShortestName(Context.getLocale(), false).getName());
        assertEquals(2, persistedConcept.getSynonyms().size());
        assertEquals(0, persistedConcept.getAnswers().size());
        ArrayList<ConceptMap> conceptMaps = new ArrayList<>(persistedConcept.getConceptMappings());
        ConceptMap conceptMap = conceptMaps.get(0);
        assertEquals(persistedConcept, conceptMap.getConcept());
        assertEquals(conceptReferenceTermRow.getReferenceTermCode(), conceptMap.getConceptReferenceTerm().getCode());
        assertEquals(conceptReferenceTermRow.getReferenceTermRelationship().toLowerCase(), conceptMap.getConceptMapType().toString());
        assertEquals(conceptReferenceTermRow.getReferenceTermSource(), conceptMap.getConceptReferenceTerm().getConceptSource().getName());

        for (ConceptName conceptName : persistedConcept.getSynonyms(Context.getLocale())) {
            assertTrue(conceptName.getName().equals("Synonym1") || conceptName.getName().equals("Synonym2"));
        }
        Context.flushSession();
        Context.closeSession();
    }

    @Test
    @Ignore
    public void shouldCreateNewMappingsForExistingConcept() throws Exception {
        ConceptRow conceptRow = new ConceptRow();
        conceptRow.name = "Existing Concept";
        conceptRow.conceptClass = "New Class";
        conceptRow.description = "Some Description";
        ConceptReferenceTermRow conceptReferenceTermRow = new ConceptReferenceTermRow("org.openmrs.module.emrapi", "New Code", SAME_AS);
        List<ConceptReferenceTermRow> conceptReferenceTermsList = new ArrayList<>(Arrays.asList(conceptReferenceTermRow));
        conceptRow.referenceTerms = conceptReferenceTermsList;

        List<KeyValue> synonyms = new ArrayList<>();
        synonyms.add(new KeyValue("1", "Synonym1"));
        synonyms.add(new KeyValue("2", "Synonym2"));
        conceptRow.synonyms = synonyms;
        conceptRow.shortName = "NConcept";
        Messages messages = conceptPersister.persist(conceptRow);
        assertEquals(0, messages.size());
        Context.openSession();
        Context.authenticate("admin", "test");
        Concept persistedConcept = conceptService.getConceptByName(conceptRow.name);
        assertNotNull(persistedConcept);
        assertEquals(conceptRow.description, persistedConcept.getDescription(Context.getLocale()).getDescription());
        assertEquals(conceptRow.conceptClass, persistedConcept.getConceptClass().getName());
        assertEquals(conceptRow.shortName, persistedConcept.getShortestName(Context.getLocale(), false).getName());
        assertEquals(2, persistedConcept.getSynonyms().size());
        assertEquals(0, persistedConcept.getAnswers().size());
        ArrayList<ConceptMap> conceptMaps = new ArrayList<>(persistedConcept.getConceptMappings());
        ConceptMap conceptMap = conceptMaps.get(0);
        assertEquals(persistedConcept, conceptMap.getConcept());
        assertEquals(conceptReferenceTermRow.getReferenceTermCode(), conceptMap.getConceptReferenceTerm().getCode());
        assertEquals(conceptReferenceTermRow.getReferenceTermRelationship().toLowerCase(), conceptMap.getConceptMapType().toString());
        assertEquals(conceptReferenceTermRow.getReferenceTermSource(), conceptMap.getConceptReferenceTerm().getConceptSource().getName());

        for (ConceptName conceptName : persistedConcept.getSynonyms(Context.getLocale())) {
            assertTrue(conceptName.getName().equals("Synonym1") || conceptName.getName().equals("Synonym2"));
        }
        Context.flushSession();
        Context.closeSession();
    }

    @Test
    public void createNewConceptOfTypeNumericWithUnitsAndHinormalLownormal() throws Exception {
        ConceptRow conceptRow = new ConceptRow();
        conceptRow.name = "New Concept";
        conceptRow.conceptClass = "New Class";
        conceptRow.description = "Some Description";
        ConceptReferenceTermRow conceptReferenceTermRow = new ConceptReferenceTermRow("org.openmrs.module.emrapi", "New Code", SAME_AS);
        List<ConceptReferenceTermRow> conceptReferenceTermsList = new ArrayList<>(Arrays.asList(conceptReferenceTermRow));
        conceptRow.referenceTerms = conceptReferenceTermsList;
        conceptRow.dataType = "Numeric";
        conceptRow.units = "unit";
        conceptRow.hiNormal = "99";
        conceptRow.lowNormal = "10";

        List<KeyValue> synonyms = new ArrayList<>();
        synonyms.add(new KeyValue("1", "Synonym1"));
        synonyms.add(new KeyValue("2", "Synonym2"));
        conceptRow.synonyms = synonyms;
        conceptRow.shortName = "NConcept";
        Messages errorMessages = conceptPersister.persist(conceptRow);
        assertTrue(errorMessages.isEmpty());
        Context.openSession();
        Context.authenticate("admin", "test");
        Concept persistedConcept = conceptService.getConceptByName(conceptRow.name);
        assertNotNull(persistedConcept);
        assertEquals(conceptRow.description, persistedConcept.getDescription(Context.getLocale()).getDescription());
        assertEquals(conceptRow.conceptClass, persistedConcept.getConceptClass().getName());
        assertEquals(conceptRow.shortName, persistedConcept.getShortestName(Context.getLocale(), false).getName());
        assertEquals(2, persistedConcept.getSynonyms().size());
        assertEquals(0, persistedConcept.getAnswers().size());
        ArrayList<ConceptMap> conceptMaps = new ArrayList<>(persistedConcept.getConceptMappings());
        ConceptMap conceptMap = conceptMaps.get(0);
        assertEquals(persistedConcept, conceptMap.getConcept());
        assertEquals(conceptReferenceTermRow.getReferenceTermCode(), conceptMap.getConceptReferenceTerm().getCode());
        assertEquals(conceptReferenceTermRow.getReferenceTermRelationship().toLowerCase(), conceptMap.getConceptMapType().toString());
        assertEquals(conceptReferenceTermRow.getReferenceTermSource(), conceptMap.getConceptReferenceTerm().getConceptSource().getName());
        ConceptNumeric conceptNumeric = conceptService.getConceptNumeric(persistedConcept.getConceptId());
        assertTrue(conceptNumeric.getUnits().equals(conceptRow.units));
        assertTrue(conceptNumeric.getHiNormal().equals(99.0));
        assertTrue(conceptNumeric.getLowNormal().equals(10.0));

        for (ConceptName conceptName : persistedConcept.getSynonyms(Context.getLocale())) {
            assertTrue(conceptName.getName().equals("Synonym1") || conceptName.getName().equals("Synonym2"));
        }
        Context.flushSession();
        Context.closeSession();
    }

    @Test
    public void shouldCreateNewConceptWithGivenUUID() throws Exception {
        String uuid = UUID.randomUUID().toString();
        ConceptRow conceptRow = new ConceptRow();
        conceptRow.name = "New concept";
        conceptRow.conceptClass = "New Class";
        conceptRow.description = "New concept description";
        conceptRow.uuid = uuid;

        Messages errorMessages = conceptPersister.persist(conceptRow);

        assertTrue(errorMessages.isEmpty());
        Context.openSession();
        Context.authenticate("admin", "test");
        Concept persistedConcept = conceptService.getConceptByName(conceptRow.name);
        assertNotNull(persistedConcept);
        assertEquals(uuid, persistedConcept.getUuid());
        assertEquals(conceptRow.name, persistedConcept.getName(Context.getLocale()).getName());
        assertEquals(conceptRow.conceptClass, persistedConcept.getConceptClass().getName());
        assertEquals("New concept description", persistedConcept.getDescription().getDescription());
        assertEquals(0, persistedConcept.getSynonyms().size());
        Context.flushSession();
        Context.closeSession();

    }
}
