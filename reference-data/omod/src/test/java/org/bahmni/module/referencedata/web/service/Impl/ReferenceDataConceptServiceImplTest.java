package org.bahmni.module.referencedata.web.service.impl;

import org.bahmni.module.bahmnicore.mapper.builder.ConceptBuilder;
import org.bahmni.module.referencedata.web.contract.RequestConcept;
import org.bahmni.module.referencedata.web.contract.mapper.ConceptMapper;
import org.bahmni.module.referencedata.web.service.ReferenceDataConceptService;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Mock;
import org.openmrs.ConceptClass;
import org.openmrs.ConceptDatatype;
import org.openmrs.api.ConceptService;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

public class ReferenceDataConceptServiceImplTest {
    private ReferenceDataConceptService referenceDataConceptService;

    @Mock
    private ConceptService conceptService;

    @Mock
    private ConceptMapper conceptMapper;

    @Mock
    private ConceptClass conceptClass;

    @Mock
    private ConceptDatatype conceptDatatype;

    @Rule
    public ExpectedException exception = ExpectedException.none();

    @Before
    public void setUp() throws Exception {
        initMocks(this);
        referenceDataConceptService = new ReferenceDataConceptServiceImpl(conceptService);
    }

    @Test
    public void shouldCreateConcept(){
        RequestConcept requestConcept = new RequestConcept();

        org.openmrs.Concept openmrsConcept = new ConceptBuilder().build();

        when(conceptService.getConceptClassByName(requestConcept.getClassName())).thenReturn(conceptClass);
        when(conceptService.getConceptDatatypeByName(requestConcept.getDataType())).thenReturn(conceptDatatype);
        when(conceptMapper.map(requestConcept, conceptClass, conceptDatatype)).thenReturn(openmrsConcept);
        when(conceptService.saveConcept(openmrsConcept)).thenReturn(openmrsConcept);

        org.openmrs.Concept savedConcept = referenceDataConceptService.saveConcept(requestConcept);

        verify(conceptMapper).map(requestConcept, conceptClass, conceptDatatype);
        verify(conceptService).saveConcept(openmrsConcept);
        verify(conceptService).getConceptClassByName(requestConcept.getClassName());
        verify(conceptService).getConceptDatatypeByName(requestConcept.getDataType());
        assertEquals(savedConcept, openmrsConcept);
    }

    @Test
    public void shouldThrowExceptionIfConceptClassNotFound(){
        RequestConcept requestConcept = new RequestConcept();
        requestConcept.setClassName("abc");

        when(conceptService.getConceptClassByName(requestConcept.getClassName())).thenReturn(null);

        exception.expect(RuntimeException.class);
        exception.expectMessage("Concept Class abc not found");
        referenceDataConceptService.saveConcept(requestConcept);
    }

    @Test
    public void shouldThrowExceptionIfConceptDatatypeNotFound(){
        RequestConcept requestConcept = new RequestConcept();
        requestConcept.setDataType("xyz");

        when(conceptService.getConceptClassByName(requestConcept.getClassName())).thenReturn(conceptClass);
        when(conceptService.getConceptDatatypeByName(requestConcept.getDataType())).thenReturn(null);

        exception.expect(RuntimeException.class);
        exception.expectMessage("Concept Datatype xyz not found");
        referenceDataConceptService.saveConcept(requestConcept);
    }
}