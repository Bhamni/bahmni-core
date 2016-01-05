package org.bahmni.module.referencedata.labconcepts.service.impl;

import org.bahmni.module.referencedata.BaseIntegrationTest;
import org.bahmni.module.referencedata.labconcepts.contract.ConceptReferenceTerm;
import org.bahmni.module.referencedata.labconcepts.contract.ConceptSet;
import org.bahmni.module.referencedata.labconcepts.service.ReferenceDataConceptService;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.openmrs.Concept;
import org.openmrs.ConceptAnswer;
import org.openmrs.ConceptDatatype;
import org.openmrs.ConceptMap;
import org.openmrs.ConceptNumeric;
import org.openmrs.api.APIException;
import org.openmrs.api.ConceptInUseException;
import org.openmrs.api.ConceptService;
import org.openmrs.api.context.Context;
import org.openmrs.util.LocaleUtility;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;
import static org.powermock.api.mockito.PowerMockito.when;

public class ReferenceDataConceptServiceImplIT extends BaseIntegrationTest {

    @Autowired
    private ReferenceDataConceptService referenceDataConceptService;

    @Autowired
    private ConceptService conceptService;

    @Rule
    public ExpectedException exception = ExpectedException.none();

    @Before
    public void setUp() throws Exception {
        executeDataSet("labDataSetup.xml");
    }

    @Test
    public void shouldSaveNewConceptSet() throws Exception {
        ConceptSet conceptSet = new ConceptSet();
        String uniqueName = "uniqueName";
        conceptSet.setUniqueName(uniqueName);
        String displayName = "displayName";
        conceptSet.setDisplayName(displayName);
        conceptSet.setClassName("Finding");
        String description = "Description";
        conceptSet.setDescription(description);
        ConceptReferenceTerm conceptReferenceTerm = new ConceptReferenceTerm();
        conceptReferenceTerm.setReferenceTermCode("New Code");
        conceptReferenceTerm.setReferenceTermRelationship("SAME-AS");
        conceptReferenceTerm.setReferenceTermSource("org.openmrs.module.emrapi");
//        List<ConceptReferenceTerm> conceptReferenceTerms = new ArrayList<>();
//        conceptReferenceTerms.add(conceptReferenceTerm);
        conceptSet.getConceptReferenceTermsList().add(conceptReferenceTerm);

        Concept concept = referenceDataConceptService.saveConcept(conceptSet);

        assertTrue(concept.isSet());
        assertEquals(uniqueName, concept.getFullySpecifiedName(Context.getLocale()).getName());
        assertEquals(displayName, concept.getShortNames().iterator().next().getName());
        assertEquals("Finding", concept.getConceptClass().getName());
        assertEquals(description, concept.getDescription(Context.getLocale()).getDescription());
        Collection<ConceptMap> conceptMappings = concept.getConceptMappings();
        ConceptMap conceptMap = conceptMappings.iterator().next();
        assertEquals("New Code", conceptMap.getConceptReferenceTerm().getCode());
        assertEquals("org.openmrs.module.emrapi",conceptMap.getConceptReferenceTerm().getConceptSource().getName());
        assertEquals("same-as",conceptMap.getConceptMapType().toString());
        assertEquals(ConceptDatatype.N_A_UUID, concept.getDatatype().getUuid());
    }

    @Test
    public void failIfConceptClassNotFound() throws Throwable {
        ConceptSet conceptSet = new ConceptSet();
        String uniqueName = "uniqueName";
        conceptSet.setUniqueName(uniqueName);
        String displayName = "displayName";
        conceptSet.setDisplayName(displayName);
        conceptSet.setClassName("Illegal");

        exception.expect(APIException.class);
        exception.expectMessage("Concept Class Illegal not found");

        referenceDataConceptService.saveConcept(conceptSet);
    }

    @Test
    public void shouldSaveConceptSetWithChildMembers() throws Exception {
        ConceptSet conceptSet = new ConceptSet();
        String uniqueName = "uniqueName";
        conceptSet.setUniqueName(uniqueName);
        String displayName = "displayName";
        conceptSet.setDisplayName(displayName);
        conceptSet.setClassName("Finding");
        List<String> children = new ArrayList<>();
        children.add("Child1");
        children.add("Child2");
        conceptSet.setChildren(children);
        Concept concept = referenceDataConceptService.saveConcept(conceptSet);
        List<Concept> setMembers = concept.getSetMembers();
        assertEquals(2, setMembers.size());
        assertEquals("Child1", setMembers.get(0).getName(Context.getLocale()).getName());
        assertEquals("Child2", setMembers.get(1).getName(Context.getLocale()).getName());
        assertEquals(ConceptDatatype.N_A_UUID, concept.getDatatype().getUuid());
    }

    @Test
    public void throwExceptionifChildConceptDoesntExist() throws Exception {
        ConceptSet conceptSet = new ConceptSet();
        String uniqueName = "uniqueName";
        conceptSet.setUniqueName(uniqueName);
        String displayName = "displayName";
        conceptSet.setDisplayName(displayName);
        conceptSet.setClassName("Finding");
        List<String> children = new ArrayList<>();
        children.add("Child1");
        children.add("Child3");
        children.add("Child4");
        conceptSet.setChildren(children);

        exception.expect(APIException.class);
        exception.expectMessage("Child3 Concept/ConceptAnswer not found\nChild4 Concept/ConceptAnswer not found");

        referenceDataConceptService.saveConcept(conceptSet);
    }


    @Test
    public void updateExistingConceptSet() throws Exception {
        ConceptSet conceptSet = new ConceptSet();
        String uniqueName = "Existing Concept";
        conceptSet.setUniqueName(uniqueName);
        String displayName = "NewSName";
        conceptSet.setDisplayName(displayName);
        conceptSet.setClassName("Finding");
        List<String> children = new ArrayList<>();
        String description = "Description";
        conceptSet.setDescription(description);

        children.add("Child1");
        children.add("Child2");
        conceptSet.setChildren(children);
        Concept concept = referenceDataConceptService.saveConcept(conceptSet);

        assertTrue(concept.isSet());
        assertEquals(uniqueName, concept.getName(Context.getLocale()).getName());
        assertEquals(displayName, concept.getShortestName(Context.getLocale(), false).getName());
        assertEquals("Finding", concept.getConceptClass().getName());
        assertEquals(2, concept.getSetMembers().size());
        assertEquals("5d2d4cb7-mm3b-0037-70f7-0dmimmm22222", concept.getUuid());
        assertEquals(description, concept.getDescription(Context.getLocale()).getDescription());
        assertEquals(ConceptDatatype.N_A_UUID, concept.getDatatype().getUuid());
    }



    @Test
    public void updateExistingConceptSetWithUUID() throws Exception {
        ConceptSet conceptSet = new ConceptSet();
        conceptSet.setUuid("5d2d4cb7-mm3b-0037-70f7-0dmimmm22222");
        String uniqueName = "Existing Concept New";
        conceptSet.setUniqueName(uniqueName);
        String displayName = "NewSName";
        conceptSet.setDisplayName(displayName);
        conceptSet.setClassName("Finding");
        List<String> children = new ArrayList<>();
        String description = "Description";
        conceptSet.setDescription(description);

        children.add("Child1");
        children.add("Child2");
        conceptSet.setChildren(children);
        Concept concept = referenceDataConceptService.saveConcept(conceptSet);

        assertTrue(concept.isSet());
        assertEquals(uniqueName, concept.getName(Context.getLocale()).getName());
        assertEquals(displayName, concept.getShortestName(Context.getLocale(), false).getName());
        assertEquals("Finding", concept.getConceptClass().getName());
        assertEquals(2, concept.getSetMembers().size());
        assertEquals("5d2d4cb7-mm3b-0037-70f7-0dmimmm22222", concept.getUuid());
        assertEquals(description, concept.getDescription(Context.getLocale()).getDescription());
        assertEquals(ConceptDatatype.N_A_UUID, concept.getDatatype().getUuid());
    }

    @Test
    public void create_concept_with_units() throws Exception {
        org.bahmni.module.referencedata.labconcepts.contract.Concept concept = new org.bahmni.module.referencedata.labconcepts.contract.Concept();
        concept.setUuid("5d2d4cb7-mm3b-0037-70k7-0dmimtm22222");
        String uniqueName = "Some Numeric Concept";
        concept.setUniqueName(uniqueName);
        String displayName = "NumericConcept";
        concept.setDisplayName(displayName);
        concept.setClassName("Finding");
        String description = "Description";
        concept.setDataType("Numeric");
        concept.setDescription(description);
        concept.setUnits("unit");
        Concept savedConcept = referenceDataConceptService.saveConcept(concept);

        assertEquals(uniqueName, savedConcept.getName(Context.getLocale()).getName());
        assertEquals(displayName, savedConcept.getShortestName(Context.getLocale(), false).getName());
        assertEquals("Finding", savedConcept.getConceptClass().getName());
        assertEquals(0, savedConcept.getSetMembers().size());
        assertEquals(description, savedConcept.getDescription(Context.getLocale()).getDescription());
        assertEquals(ConceptDatatype.NUMERIC_UUID, savedConcept.getDatatype().getUuid());
        ConceptNumeric conceptNumeric = conceptService.getConceptNumeric(savedConcept.getConceptId());
        assertTrue(savedConcept.isNumeric());
        assertEquals("unit", conceptNumeric.getUnits());
    }

    @Test
    public void create_concept_with_high_normal_and_low_normal() throws Exception {
        org.bahmni.module.referencedata.labconcepts.contract.Concept concept = new org.bahmni.module.referencedata.labconcepts.contract.Concept();
        concept.setUuid("5d2d4cb7-mm3b-0037-70k7-0dmimtm22222");
        String uniqueName = "Some Numeric Concept";
        concept.setUniqueName(uniqueName);
        String displayName = "NumericConcept";
        concept.setDisplayName(displayName);
        concept.setClassName("Finding");
        String description = "Description";
        concept.setDataType("Numeric");
        concept.setDescription(description);
        concept.setUnits("unit");
        concept.setHiNormal("99");
        concept.setLowNormal("10");
        Concept savedConcept = referenceDataConceptService.saveConcept(concept);

        assertEquals(uniqueName, savedConcept.getName(Context.getLocale()).getName());
        assertEquals(displayName, savedConcept.getShortestName(Context.getLocale(), false).getName());
        assertEquals("Finding", savedConcept.getConceptClass().getName());
        assertEquals(0, savedConcept.getSetMembers().size());
        assertEquals(description, savedConcept.getDescription(Context.getLocale()).getDescription());
        assertEquals(ConceptDatatype.NUMERIC_UUID, savedConcept.getDatatype().getUuid());
        ConceptNumeric conceptNumeric = conceptService.getConceptNumeric(savedConcept.getConceptId());
        assertTrue(savedConcept.isNumeric());
        assertEquals("unit", conceptNumeric.getUnits());
        assertTrue(conceptNumeric.getHiNormal().equals(99.0));
        assertTrue(conceptNumeric.getLowNormal().equals(10.0));
    }

    @Test
    public void update_existing_concept_shortname() throws Exception {
        org.bahmni.module.referencedata.labconcepts.contract.Concept concept = new org.bahmni.module.referencedata.labconcepts.contract.Concept();
        concept.setUuid("5d2d4cb7-feet-0037-70f7-0dmimmm22222");
        String uniqueName = "Existing Numeric Concept";
        concept.setUniqueName(uniqueName);
        String displayName = "NumericConcept";
        concept.setDisplayName(displayName);
        concept.setClassName("Finding");
        concept.setDataType("Numeric");
        concept.setUnits("unit");
        assertEquals(2, conceptService.getConceptByUuid("5d2d4cb7-mm3b-0037-70f7-0dmimmm22222").getNames().size());
        Concept savedConcept = referenceDataConceptService.saveConcept(concept);

        assertEquals(4, savedConcept.getNames().size());
        assertEquals(uniqueName, savedConcept.getName(Context.getLocale()).getName());
        assertEquals(displayName, savedConcept.getShortNames().iterator().next().getName());
        assertEquals("Finding", savedConcept.getConceptClass().getName());
        assertEquals(0, savedConcept.getSetMembers().size());
        assertEquals(ConceptDatatype.NUMERIC_UUID, savedConcept.getDatatype().getUuid());
        ConceptNumeric conceptNumeric = conceptService.getConceptNumeric(savedConcept.getConceptId());
        assertTrue(savedConcept.isNumeric());
        assertEquals("unit", conceptNumeric.getUnits());
    }

    @Test
    public void update_existing_concept_numeric_with_high_normal_and_low_normal() throws Exception {
        org.bahmni.module.referencedata.labconcepts.contract.Concept concept = new org.bahmni.module.referencedata.labconcepts.contract.Concept();
        concept.setUuid("5d2d4cb7-feet-0037-70f7-0dmimmm22222");
        String uniqueName = "Existing Numeric Concept";
        concept.setUniqueName(uniqueName);
        String displayName = "NumericConcept";
        concept.setDisplayName(displayName);
        concept.setClassName("Finding");
        concept.setDataType("Numeric");
        concept.setUnits("unit");
        concept.setHiNormal("99");
        concept.setLowNormal("10");
        Concept savedConcept = referenceDataConceptService.saveConcept(concept);

        assertEquals(uniqueName, savedConcept.getName(Context.getLocale()).getName());
        assertEquals(displayName, savedConcept.getShortestName(Context.getLocale(), false).getName());
        assertEquals("Finding", savedConcept.getConceptClass().getName());
        assertEquals(0, savedConcept.getSetMembers().size());
        assertEquals(ConceptDatatype.NUMERIC_UUID, savedConcept.getDatatype().getUuid());
        ConceptNumeric conceptNumeric = conceptService.getConceptNumeric(savedConcept.getConceptId());
        assertTrue(savedConcept.isNumeric());
        assertEquals("unit", conceptNumeric.getUnits());
        assertTrue(conceptNumeric.getHiNormal().equals(99.0));
        assertTrue(conceptNumeric.getLowNormal().equals(10.0));
    }

    @Test
    public void throwExceptionifConceptHasObs() throws Exception {
        org.bahmni.module.referencedata.labconcepts.contract.Concept concept = new org.bahmni.module.referencedata.labconcepts.contract.Concept();
        concept.setUuid("5d2d4cb7-t3tb-0037-70f7-0dmimmm22222");
        String uniqueName = "New Numeric Concept";
        concept.setUniqueName(uniqueName);
        concept.setClassName("Finding");
        concept.setDataType("N/A");
        concept.setUnits("unit");
        concept.setHiNormal("99");
        concept.setLowNormal("10");
        Concept existingConcept = conceptService.getConceptByUuid(concept.getUuid());
        assertNotEquals(ConceptDatatype.N_A_UUID, existingConcept.getDatatype().getUuid());

        exception.expect(ConceptInUseException.class);
        exception.expectMessage("The concepts datatype cannot be changed if it is already used/associated to an observation");
        referenceDataConceptService.saveConcept(concept);
    }

    @Test
    public void update_existing_concept_with_short_name() throws Exception {
        org.bahmni.module.referencedata.labconcepts.contract.Concept concept = new org.bahmni.module.referencedata.labconcepts.contract.Concept();
        String uniqueName = "Existing Concept with obs";
        concept.setUniqueName(uniqueName);
        String displayName = "NewShortName";
        concept.setDisplayName(displayName);
        concept.setClassName("Finding");
        concept.setDataType("Coded");
        Concept existingConcept = conceptService.getConceptByName("Existing Concept with obs");
        assertEquals(1, existingConcept.getNames().size());
        Concept savedConcept = referenceDataConceptService.saveConcept(concept);

        assertEquals(uniqueName, savedConcept.getName(Context.getLocale()).getName());
        assertEquals(displayName, savedConcept.getShortNameInLocale(Context.getLocale()).getName());
        assertEquals("Finding", savedConcept.getConceptClass().getName());
        assertEquals(3, savedConcept.getNames().size());
    }

    @Test
    public void update_existing_concept_set_with_child_members() throws Exception {
        ConceptSet conceptSet = new ConceptSet();
        String uniqueName = "Existing Concept With Children";
        conceptSet.setUniqueName(uniqueName);
        String displayName = "NewSName";
        conceptSet.setDisplayName(displayName);
        conceptSet.setClassName("Finding");
        List<String> children = new ArrayList<>();

        children.add("Child1");
        children.add("Child2");
        conceptSet.setChildren(children);
        Concept existingConceptSet = conceptService.getConceptByName("Existing Concept With Children");
        assertEquals(1, existingConceptSet.getSetMembers().size());
        Concept concept = referenceDataConceptService.saveConcept(conceptSet);

        assertTrue(concept.isSet());
        assertEquals(uniqueName, concept.getName(Context.getLocale()).getName());
        assertEquals(displayName, concept.getShortestName(Context.getLocale(), false).getName());
        assertEquals("Finding", concept.getConceptClass().getName());
        assertEquals(2, concept.getSetMembers().size());
        assertEquals("kf2d4cb7-t3tb-0037-70f7-0dmimmm22222", concept.getUuid());
        assertEquals(ConceptDatatype.N_A_UUID, concept.getDatatype().getUuid());
    }


    @Test
    public void update_existing_concept_with_answers() throws Exception {
        org.bahmni.module.referencedata.labconcepts.contract.Concept concept = new org.bahmni.module.referencedata.labconcepts.contract.Concept();
        String uniqueName = "Existing Concept With Answer";
        concept.setUniqueName(uniqueName);
        String displayName = "NewSName";
        concept.setDisplayName(displayName);
        concept.setClassName("Finding");
        concept.setDataType("Coded");
        List<String> answers = new ArrayList<>();

        answers.add("Answer1");
        answers.add("Answer2");
        concept.setAnswers(answers);
        Concept existingConcept = conceptService.getConceptByName("Existing Concept With Answer");
        assertEquals(1, existingConcept.getAnswers().size());
        Concept savedConcept = referenceDataConceptService.saveConcept(concept);

        assertEquals(2, savedConcept.getAnswers().size());
        ArrayList<ConceptAnswer> conceptAnswers = new ArrayList<>(savedConcept.getAnswers());
        ConceptAnswer answer1 = conceptAnswers.get(0);
        ConceptAnswer answer2 = conceptAnswers.get(1);
        assertEquals("Answer1", answer1.getAnswerConcept().getName(Context.getLocale()).getName());
        assertEquals("Answer2", answer2.getAnswerConcept().getName(Context.getLocale()).getName());
    }

    @Test
    public void migrate_concept_datatype_to_numeric() throws Exception {
        org.bahmni.module.referencedata.labconcepts.contract.Concept concept = new org.bahmni.module.referencedata.labconcepts.contract.Concept();
        concept.setUuid("kf2d4cb7-t3tb-oo37-70f7-0dmimmm22222");
        concept.setClassName("Finding");
        concept.setDataType("Numeric");
        concept.setUnits("unit");
        concept.setHiNormal("99");
        concept.setLowNormal("10");
        Concept existingConcept = conceptService.getConceptByUuid("kf2d4cb7-t3tb-oo37-70f7-0dmimmm22222");
        assertNotEquals(ConceptDatatype.NUMERIC_UUID, existingConcept.getDatatype().getUuid());
        Concept savedConcept = referenceDataConceptService.saveConcept(concept);

        assertEquals("First Child", savedConcept.getName(Context.getLocale()).getName());
        assertEquals("Finding", savedConcept.getConceptClass().getName());
        assertEquals(0, savedConcept.getSetMembers().size());
        assertEquals(ConceptDatatype.NUMERIC_UUID, savedConcept.getDatatype().getUuid());
        ConceptNumeric conceptNumeric = conceptService.getConceptNumeric(savedConcept.getConceptId());
        assertTrue(savedConcept.isNumeric());
        assertEquals("unit", conceptNumeric.getUnits());
        assertTrue(conceptNumeric.getHiNormal().equals(99.0));
        assertTrue(conceptNumeric.getLowNormal().equals(10.0));
    }
}
