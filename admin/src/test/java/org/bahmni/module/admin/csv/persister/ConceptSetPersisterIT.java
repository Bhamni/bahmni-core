package org.bahmni.module.admin.csv.persister;

import org.bahmni.csv.KeyValue;
import org.bahmni.csv.Messages;
import org.bahmni.module.admin.BaseIntegrationTest;
import org.bahmni.module.admin.csv.models.ConceptSetRow;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.Concept;
import org.openmrs.api.ConceptService;
import org.openmrs.api.context.Context;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class ConceptSetPersisterIT extends BaseIntegrationTest {
    
    @Autowired
    private ConceptSetPersister conceptSetPersister;

    @Autowired
    private ConceptService conceptService;

    @Before
    public void setUp() throws Exception {
        Context.authenticate("admin", "test");
        executeDataSet("conceptSetup.xml");
    }

    @Test
    public void shouldFailValidationForNoConceptName() throws Exception {
        ConceptSetRow conceptRow = new ConceptSetRow();
        Messages persistErrorMessages = conceptSetPersister.validate(conceptRow);
        assertFalse(persistErrorMessages.isEmpty());
    }

    @Test
    public void shouldFailValidationForNoConceptClass() throws Exception {
        ConceptSetRow conceptRow = new ConceptSetRow();
        conceptRow.name = "Concept Name";
        Messages persistErrorMessages = conceptSetPersister.validate(conceptRow);
        assertFalse(persistErrorMessages.isEmpty());
    }


    @Test
    public void shouldPassValidationIfConceptNameAndConceptClassArePresent() throws Exception {
        ConceptSetRow conceptRow = new ConceptSetRow();
        conceptRow.name = "concept Name";
        conceptRow.conceptClass = "concept Class";
        Messages persistErrorMessages = conceptSetPersister.validate(conceptRow);
        assertTrue(persistErrorMessages.isEmpty());
    }

    @Test
    public void shouldPersistNewConceptSetWithNameClassDescriptionInputOnly() throws Exception {
        ConceptSetRow conceptRow = new ConceptSetRow();
        conceptRow.name = "New concept";
        conceptRow.conceptClass = "New Class";
        conceptRow.description = "some description";
        Messages persistErrorMessages = conceptSetPersister.persist(conceptRow);
        assertTrue(persistErrorMessages.isEmpty());
        Context.openSession();
        Context.authenticate("admin", "test");
        Concept persistedConcept = conceptService.getConceptByName(conceptRow.name);
        assertNotNull(persistedConcept);
        assertEquals(conceptRow.name, persistedConcept.getName(Context.getLocale()).getName());
        assertEquals(conceptRow.conceptClass, persistedConcept.getConceptClass().getName());
        assertEquals("some description", persistedConcept.getDescription().getDescription());
        assertEquals(0, persistedConcept.getSynonyms().size());
        assertTrue(persistedConcept.isSet());
        Context.flushSession();
        Context.closeSession();
    }


    @Test
    public void addSetMembers() throws Exception {
        ConceptSetRow conceptRow = new ConceptSetRow();
        conceptRow.name = "New concept";
        conceptRow.conceptClass = "New Class";
        conceptRow.description = "some description";

        List<KeyValue> children = new ArrayList<>();
        children.add(new KeyValue("1", "Child1"));
        children.add(new KeyValue("2", "Child2"));
        conceptRow.children = children;
        Messages persistErrorMessages = conceptSetPersister.persist(conceptRow);
        assertTrue(persistErrorMessages.isEmpty());
        Context.openSession();
        Context.authenticate("admin", "test");
        Concept persistedConcept = conceptService.getConceptByName(conceptRow.name);
        assertNotNull(persistedConcept);
        assertEquals(conceptRow.name, persistedConcept.getName(Context.getLocale()).getName());
        assertEquals(conceptRow.conceptClass, persistedConcept.getConceptClass().getName());
        assertEquals("some description", persistedConcept.getDescription().getDescription());

        assertEquals(2, persistedConcept.getSetMembers().size());
        assertEquals("some description", persistedConcept.getDescription().getDescription());
        assertEquals(0, persistedConcept.getSynonyms().size());
        assertTrue(persistedConcept.isSet());
        Context.flushSession();
        Context.closeSession();
    }

    @Test
    public void should_fail_validation_for_conceptset_with_cycle () throws Exception{
        ConceptSetRow conceptSetRow = new ConceptSetRow();
        conceptSetRow.name = "Cycle concept Name";
        conceptSetRow.conceptClass = "Cycle concept Class";

        List<KeyValue> children = new ArrayList<>();
        children.add(new KeyValue("1", "Child1"));
        children.add(new KeyValue("2", "Cycle concept Name"));
        conceptSetRow.children = children;
        Messages persistErrorMessages = conceptSetPersister.validate(conceptSetRow);
        assertFalse("Validation did not catch cycle", persistErrorMessages.isEmpty());
    }

    @Test
    public void should_fail_to_persist_if_conceptSetRow_introduces_cycle() throws Exception {
        ConceptSetRow row1 = new ConceptSetRow();
        row1.name = "ConceptA";
        row1.conceptClass = "New Class";
        row1.description = "some description";
        List<KeyValue> children = new ArrayList<>();
        children.add(new KeyValue("1", "Child1"));
        children.add(new KeyValue("2", "Child2"));
        row1.children = children;

        Messages persistErrorMessages = conceptSetPersister.persist(row1);
        assertTrue(persistErrorMessages.isEmpty());
        Context.openSession();
        Context.authenticate("admin", "test");
        Concept persistedConcept = conceptService.getConceptByName(row1.name);
        assertNotNull(persistedConcept);

        ConceptSetRow row2 = new ConceptSetRow();
        row2.name = "Child2";
        row2.conceptClass = "New Class";
        row2.description = "some description";
        List<KeyValue> children1 = new ArrayList<>();
        children1.add(new KeyValue("1", "ConceptA"));
        children1.add(new KeyValue("2", "Child3"));
        row2.children = children1;

        Messages persistErrorMessages1 = conceptSetPersister.persist(row2);
        assertFalse(persistErrorMessages1.isEmpty());

        Context.flushSession();
        Context.closeSession();
    }

    @Test
    public void shouldCreateNewConceptSetWithGivenUUID() throws Exception {
        String uuid = UUID.randomUUID().toString();
        ConceptSetRow conceptRow = new ConceptSetRow();
        conceptRow.name = "New concept";
        conceptRow.conceptClass = "New Class";
        conceptRow.description = "some description";
        conceptRow.uuid = uuid;

        Messages persistErrorMessages = conceptSetPersister.persist(conceptRow);

        assertTrue(persistErrorMessages.isEmpty());
        Context.openSession();
        Context.authenticate("admin", "test");
        Concept persistedConcept = conceptService.getConceptByName(conceptRow.name);
        assertNotNull(persistedConcept);
        assertEquals(conceptRow.name, persistedConcept.getName(Context.getLocale()).getName());
        assertEquals(uuid, persistedConcept.getUuid());
        Context.flushSession();
        Context.closeSession();
    }




}
