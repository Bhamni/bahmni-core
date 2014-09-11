package org.bahmni.module.referencedata.web.service.impl;

import org.bahmni.module.bahmnicore.mapper.builder.ConceptBuilder;
import org.bahmni.module.referencedata.web.contract.Concept;
import org.bahmni.module.referencedata.web.contract.mapper.ConceptMapper;
import org.bahmni.module.referencedata.web.service.ReferenceDataConceptService;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.openmrs.ConceptClass;
import org.openmrs.ConceptDatatype;
import org.openmrs.api.ConceptService;
import org.openmrs.api.context.Context;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

@PrepareForTest({ReferenceDataConceptServiceImpl.class, Context.class})
@RunWith(PowerMockRunner.class)
public class ReferenceDataConceptServiceImplTest {
    private ReferenceDataConceptService referenceDataConceptService;

    @Mock
    private ConceptService conceptService;

    @Mock
    private ConceptClass conceptClass;

    @Mock
    private ConceptDatatype conceptDatatype;

    @Mock
    private ConceptMapper conceptMapper;

    @Rule
    public ExpectedException exception = ExpectedException.none();
    private org.openmrs.Concept openmrsConcept;
    private Concept concept;


    @Before
    public void setUp() throws Exception {
        initMocks(this);

        openmrsConcept = new ConceptBuilder().build();
        concept = new Concept();
        concept.setUniqueName("unique-name");

        PowerMockito.when(this.conceptMapper.map(any(Concept.class), any(ConceptClass.class), any(ConceptDatatype.class))).thenReturn(openmrsConcept);
        PowerMockito.whenNew(ConceptMapper.class).withAnyArguments().thenReturn(conceptMapper);
        PowerMockito.mock(org.bahmni.module.bahmnicore.service.ConceptService.class);
        PowerMockito.mockStatic(Context.class);
        when(Context.getConceptService()).thenReturn(conceptService);

        referenceDataConceptService = new ReferenceDataConceptServiceImpl();
    }

    @Test
    public void shouldCreateConcept() throws Exception {

        when(conceptService.getConceptClassByName(anyString())).thenReturn(conceptClass);
        when(conceptService.getConceptDatatypeByName(anyString())).thenReturn(conceptDatatype);
        when(conceptService.saveConcept(openmrsConcept)).thenReturn(openmrsConcept);

        org.openmrs.Concept savedConcept = referenceDataConceptService.saveConcept(concept);

        verify(conceptMapper).map(concept, conceptClass, conceptDatatype);
        verify(conceptService).saveConcept(openmrsConcept);
        verify(conceptService).getConceptClassByName(concept.getClassName());
        verify(conceptService).getConceptDatatypeByName(concept.getDataType());
        assertEquals(savedConcept, openmrsConcept);
    }

    @Test
    public void shouldThrowExceptionIfConceptClassNotFound() {
        concept.setClassName("abc");

        when(conceptService.getConceptClassByName(concept.getClassName())).thenReturn(null);

        exception.expect(RuntimeException.class);
        exception.expectMessage("Concept Class abc not found");
        referenceDataConceptService.saveConcept(concept);
    }

    @Test
    public void shouldThrowExceptionIfConceptDatatypeNotFound() {
        concept.setDataType("xyz");

        when(conceptService.getConceptClassByName(concept.getClassName())).thenReturn(conceptClass);
        when(conceptService.getConceptDatatypeByName(concept.getDataType())).thenReturn(null);

        exception.expect(RuntimeException.class);
        exception.expectMessage("Concept Datatype xyz not found");
        referenceDataConceptService.saveConcept(concept);
    }

    @Test
    public void shouldThrowExceptionIfConceptUniqueNameIsNull() {
        concept.setUniqueName(null);
        concept.setDataType("xyz");

        when(conceptService.getConceptClassByName(concept.getClassName())).thenReturn(conceptClass);
        when(conceptService.getConceptDatatypeByName(concept.getDataType())).thenReturn(null);

        exception.expect(RuntimeException.class);
        exception.expectMessage("Concept unique name Cannot be empty");
        referenceDataConceptService.saveConcept(concept);
    }

    @Test
    public void shouldThrowExceptionIfConceptUniqueNameIsEmptyString() {
        concept.setUniqueName("");
        concept.setDataType("xyz");

        when(conceptService.getConceptClassByName(concept.getClassName())).thenReturn(conceptClass);
        when(conceptService.getConceptDatatypeByName(concept.getDataType())).thenReturn(null);

        exception.expect(RuntimeException.class);
        exception.expectMessage("Concept unique name Cannot be empty");
        referenceDataConceptService.saveConcept(concept);
    }
}