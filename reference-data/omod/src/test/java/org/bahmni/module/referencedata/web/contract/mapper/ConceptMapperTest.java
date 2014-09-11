package org.bahmni.module.referencedata.web.contract.mapper;

import org.bahmni.module.referencedata.web.contract.Concept;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.ConceptClass;
import org.openmrs.ConceptDatatype;
import org.openmrs.api.context.Context;

import static org.junit.Assert.*;

public class ConceptMapperTest {
    private ConceptMapper conceptMapper;

    @Before
    public void setUp() throws Exception {
        conceptMapper = new ConceptMapper();
    }

    @Test
    public void shouldMapRequestConceptToOpenMRSConcept(){
        Concept concept = new Concept();
        concept.setUniqueName("uniqueName");
        concept.setDisplayName("displayName");
        concept.setDescription("description");
        concept.setClassName("Finding");
        concept.setDataType("N/A");

        ConceptClass conceptClassName = new ConceptClass();
        conceptClassName.setName("Finding");
        ConceptDatatype conceptDatatype = new ConceptDatatype();
        conceptDatatype.setName("N/A");
        org.openmrs.Concept mappedConcept = conceptMapper.map(concept, conceptClassName, conceptDatatype);

        assertEquals(concept.getUniqueName(), mappedConcept.getFullySpecifiedName(Context.getLocale()).getName());
        assertEquals(concept.getDisplayName(), mappedConcept.getShortNames().iterator().next().getName());
        assertEquals(concept.getDescription(), mappedConcept.getDescription().getDescription());
        assertEquals(concept.getClassName(), mappedConcept.getConceptClass().getName());
        assertEquals(concept.getDataType(), mappedConcept.getDatatype().getName());
    }

    @Test
    public void shouldMapConceptIfDescriptionIsNull(){
        Concept concept = new Concept();
        concept.setUniqueName("uniqueName");
        concept.setDisplayName("displayName");
        concept.setClassName("Finding");
        concept.setDataType("N/A");

        ConceptClass conceptClassName = new ConceptClass();
        conceptClassName.setName("Finding");
        ConceptDatatype conceptDatatype = new ConceptDatatype();
        conceptDatatype.setName("N/A");
        org.openmrs.Concept mappedConcept = conceptMapper.map(concept, conceptClassName, conceptDatatype);

        assertEquals(concept.getUniqueName(), mappedConcept.getFullySpecifiedName(Context.getLocale()).getName());
        assertEquals(concept.getDisplayName(), mappedConcept.getShortNames().iterator().next().getName());
        assertNull(mappedConcept.getDescriptions());
        assertEquals(concept.getClassName(), mappedConcept.getConceptClass().getName());
        assertEquals(concept.getDataType(), mappedConcept.getDatatype().getName());
    }

    @Test
    public void shouldMapConceptIfDisplayNameAndDescriptionIsNull(){
        Concept concept = new Concept();
        concept.setUniqueName("uniqueName");
        concept.setClassName("Finding");
        concept.setDataType("N/A");

        ConceptClass conceptClassName = new ConceptClass();
        conceptClassName.setName("Finding");
        ConceptDatatype conceptDatatype = new ConceptDatatype();
        conceptDatatype.setName("N/A");
        org.openmrs.Concept mappedConcept = conceptMapper.map(concept, conceptClassName, conceptDatatype);

        assertEquals(concept.getUniqueName(), mappedConcept.getFullySpecifiedName(Context.getLocale()).getName());
        assertEquals(0, mappedConcept.getShortNames().size());
        assertNull(mappedConcept.getDescriptions());
        assertEquals(concept.getClassName(), mappedConcept.getConceptClass().getName());
        assertEquals(concept.getDataType(), mappedConcept.getDatatype().getName());
    }
}