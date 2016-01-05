package org.bahmni.module.referencedata.labconcepts.service.impl;

import org.bahmni.module.referencedata.labconcepts.contract.ConceptCommon;
import org.bahmni.module.referencedata.labconcepts.model.ConceptMetaData;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.openmrs.Concept;
import org.openmrs.ConceptSearchResult;
import org.openmrs.api.AdministrationService;
import org.openmrs.api.ConceptService;
import org.openmrs.api.context.Context;
import org.openmrs.api.context.ServiceContext;
import org.openmrs.api.impl.AdministrationServiceImpl;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static org.mockito.Matchers.anyList;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

@RunWith(PowerMockRunner.class)
@PrepareForTest({org.openmrs.util.LocaleUtility.class, Context.class})
public class ConceptMetaDataServiceImplTest {

    @Mock
    ConceptService conceptService;

    @Mock
    org.openmrs.ConceptClass conceptClass;

    @Mock
    org.openmrs.ConceptDatatype conceptDataType;

    @Mock
    AdministrationService administrationService;

    @Mock
    Concept concept;

    @InjectMocks
    ConceptMetaDataServiceImpl conceptMetaDataService;

    @Before
    public void setup(){
        initMocks(this);
        PowerMockito.mockStatic(org.openmrs.util.LocaleUtility.class);
        PowerMockito.mockStatic(Context.class);
    }

    @Test
    public void testGetConceptMetaDataWhenUuidIsPassed() throws Exception {
        ConceptCommon conceptCommon = new ConceptCommon();
        conceptCommon.setClassName("ConceptClass");
        conceptCommon.setDataType("N/A");
        conceptCommon.setLocale("en");
        conceptCommon.setUniqueName("ConceptA");
        conceptCommon.setUuid("123");

        when(conceptService.getConceptClassByName("ConceptClass")).thenReturn(conceptClass);
        when(conceptService.getConceptDatatypeByName("N/A")).thenReturn(conceptDataType);
        when(conceptService.getConceptByUuid("123")).thenReturn(concept);
        Locale locale = new Locale("en");
        when(org.openmrs.util.LocaleUtility.fromSpecification("en")).thenReturn(locale);
        when(org.openmrs.util.LocaleUtility.isValid(locale)).thenReturn(true);

        ConceptMetaData conceptMetadata = conceptMetaDataService.getConceptMetaData(conceptCommon);

        Assert.assertEquals(concept, conceptMetadata.getExistingConcept());
        Assert.assertEquals(conceptClass, conceptMetadata.getConceptClass());
        Assert.assertEquals(conceptDataType, conceptMetadata.getConceptDatatype());

    }

    @Test
    public void testGetConceptMetaDataWhenLocaleIsPassedAndThereAreNoResults() throws Exception {
        ConceptCommon conceptCommon = new ConceptCommon();

        conceptCommon.setClassName("ConceptClass");
        conceptCommon.setDataType("N/A");
        conceptCommon.setUniqueName("ConceptA");
        conceptCommon.setLocale("en");

        List<Locale> locales =  new ArrayList<>();
        List<ConceptSearchResult> conceptSearchResults = new ArrayList<>();

        when(Context.getAdministrationService()).thenReturn(administrationService);
        when(conceptService.getConceptClassByName("ConceptClass")).thenReturn(conceptClass);
        when(conceptService.getConceptDatatypeByName("N/A")).thenReturn(conceptDataType);
        when(administrationService.getAllowedLocales()).thenReturn(locales);
        Locale locale = new Locale("en");
        when(org.openmrs.util.LocaleUtility.fromSpecification("en")).thenReturn(locale);
        when(org.openmrs.util.LocaleUtility.isValid(locale)).thenReturn(true);
        List<Concept> conceptList = new ArrayList<>();



        ConceptMetaData conceptMetadata = conceptMetaDataService.getConceptMetaData(conceptCommon);

        Assert.assertNull(conceptMetadata.getExistingConcept());
        Assert.assertEquals(conceptClass, conceptMetadata.getConceptClass());
        Assert.assertEquals(conceptDataType, conceptMetadata.getConceptDatatype());
    }

    @Test
    public void testGetConceptMetaDataWhenLocaleIsNotPassed() throws Exception {
        ConceptCommon conceptCommon = new ConceptCommon();
        conceptCommon.setClassName("ConceptClass");
        conceptCommon.setDataType("N/A");
        conceptCommon.setUniqueName("ConceptA");

        List<Locale> locales =  new ArrayList<>();
        List<ConceptSearchResult> conceptSearchResults = new ArrayList<>();
        ConceptSearchResult conceptSearchResult = new ConceptSearchResult();
        conceptSearchResult.setConcept(concept);
        conceptSearchResults.add(conceptSearchResult);

        when(Context.getAdministrationService()).thenReturn(administrationService);
        when(conceptService.getConceptClassByName("ConceptClass")).thenReturn(conceptClass);
        when(conceptService.getConceptDatatypeByName("N/A")).thenReturn(conceptDataType);
        when(administrationService.getAllowedLocales()).thenReturn(locales);
        when(conceptService.getConcepts("ConceptA", locales, false, null, null, null, null, null, null, null)).thenReturn(conceptSearchResults);


        ConceptMetaData conceptMetadata = conceptMetaDataService.getConceptMetaData(conceptCommon);

        Assert.assertEquals(concept, conceptMetadata.getExistingConcept());
        Assert.assertEquals(conceptClass, conceptMetadata.getConceptClass());
        Assert.assertEquals(conceptDataType, conceptMetadata.getConceptDatatype());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetConceptMetaDataWhenLocaleIsInvalid() {
        ConceptCommon conceptCommon = new ConceptCommon();
        conceptCommon.setUniqueName("ConceptA");
        conceptCommon.setLocale("en");

        List<Locale> locales =  new ArrayList<>();
        List<ConceptSearchResult> conceptSearchResults = new ArrayList<>();
        ConceptSearchResult conceptSearchResult = new ConceptSearchResult();
        conceptSearchResult.setConcept(concept);
        conceptSearchResults.add(conceptSearchResult);

        Locale locale = new Locale("en");
        when(Context.getAdministrationService()).thenReturn(administrationService);
        when(org.openmrs.util.LocaleUtility.fromSpecification("en")).thenReturn(locale);
        when(org.openmrs.util.LocaleUtility.isValid(locale)).thenReturn(false);
        when(administrationService.getAllowedLocales()).thenReturn(locales);
        when(conceptService.getConcepts("ConceptA", locales, false, null, null, null, null, null, null, null)).thenReturn(conceptSearchResults);

        conceptMetaDataService.getConceptMetaData(conceptCommon);
    }

    @Test
    public void testGetConceptMetaDataWhenLocaleIsPassed() throws Exception {
        ConceptCommon conceptCommon = new ConceptCommon();
        conceptCommon.setUniqueName("ConceptA");
        conceptCommon.setLocale("en");

        List<ConceptSearchResult> conceptSearchResults = new ArrayList<>();
        ConceptSearchResult conceptSearchResult = new ConceptSearchResult();
        conceptSearchResult.setConcept(concept);
        conceptSearchResults.add(conceptSearchResult);


        List<Locale> locales =  new ArrayList<>();
        Locale locale = new Locale("en");

        when(Context.getAdministrationService()).thenReturn(administrationService);
        when(org.openmrs.util.LocaleUtility.fromSpecification("en")).thenReturn(locale);
        when(org.openmrs.util.LocaleUtility.isValid(locale)).thenReturn(true);
        when(administrationService.getAllowedLocales()).thenReturn(locales);
        when(conceptService.getConcepts("ConceptA", locales, false, null, null, null, null, null, null, null)).thenReturn(conceptSearchResults);


        ConceptMetaData conceptMetadata = conceptMetaDataService.getConceptMetaData(conceptCommon);

        Assert.assertEquals(concept, conceptMetadata.getExistingConcept());
    }
}
