package org.bahmni.module.referencedata.web.contract.mapper;

import org.bahmni.module.referencedata.web.contract.RequestConcept;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.ConceptClass;
import org.openmrs.ConceptDatatype;
import org.openmrs.api.context.Context;

import static org.junit.Assert.*;

public class RequestConceptMapperTest {
    private ConceptMapper conceptMapper;

    @Before
    public void setUp() throws Exception {
        conceptMapper = new ConceptMapper();
    }

    @Test
    public void shouldMapRequestConceptToOpenMRSConcept(){
        RequestConcept requestConcept = new RequestConcept();
        requestConcept.setUniqueName("uniqueName");
        requestConcept.setDisplayName("displayName");  //setShortName
        requestConcept.setDescription("description");  //setDescriptions
        requestConcept.setClassName("Finding");    //setConceptClass
        requestConcept.setDataType("N/A");     //setdatatype

        ConceptClass conceptClassName = new ConceptClass();
        conceptClassName.setName("Finding");
        ConceptDatatype conceptDatatype = new ConceptDatatype();
        conceptDatatype.setName("N/A");
        org.openmrs.Concept mappedConcept = conceptMapper.map(requestConcept, conceptClassName, conceptDatatype);

        assertEquals(requestConcept.getUniqueName(), mappedConcept.getFullySpecifiedName(Context.getLocale()).getName());
        assertEquals(requestConcept.getDisplayName(), mappedConcept.getShortNames().iterator().next().getName());
        assertEquals(requestConcept.getDescription(), mappedConcept.getDescription().getDescription());
        assertEquals(requestConcept.getClassName(), mappedConcept.getConceptClass().getName());
        assertEquals(requestConcept.getDataType(), mappedConcept.getDatatype().getName());
    }
}