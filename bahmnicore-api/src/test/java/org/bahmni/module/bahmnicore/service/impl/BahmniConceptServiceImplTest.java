package org.bahmni.module.bahmnicore.service.impl;

import org.bahmni.module.bahmnicore.dao.BahmniConceptDao;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.openmrs.Concept;
import org.openmrs.Drug;
import org.openmrs.api.ConceptService;
import org.openmrs.module.emrapi.encounter.exception.ConceptNotFoundException;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

public class BahmniConceptServiceImplTest {

    public static final String QUESTION = "question";
    public static final String SEARCH_QUERY = "q";
    @Mock
    private BahmniConceptDao bahmniConceptDao;
    @Mock
    private ConceptService conceptService;
    private BahmniConceptServiceImpl bahmniConceptService;

    @Before
    public void setUp() {
        initMocks(this);
        bahmniConceptService = new BahmniConceptServiceImpl(conceptService, bahmniConceptDao);
    }

    @Test
    public void searchByQuestionShouldUseBahmniConceptDaoToSearchConcepts() {
        Concept questionConcept = new Concept();
        when(bahmniConceptDao.getConceptByFullySpecifiedName(QUESTION)).thenReturn(questionConcept);
        Concept resultConcept = new Concept();
        when(bahmniConceptDao.searchByQuestion(questionConcept, SEARCH_QUERY)).thenReturn(Arrays.asList(resultConcept));

        Collection<Concept> concepts = bahmniConceptService.searchByQuestion(QUESTION, SEARCH_QUERY);
        assertThat(concepts.size(), is(equalTo(1)));
        assertThat(concepts.iterator().next().getUuid(), is(equalTo(resultConcept.getUuid())));
    }

    @Test(expected = ConceptNotFoundException.class)
    public void searchByQuestionShouldThrowExceptionWhenQuestionConceptNotFound() throws Exception{
        bahmniConceptService.searchByQuestion("this concept doesn't exist", "headache");
    }

    @Test
    public void getDrugsByConceptSetNameShouldRetrieveAllDrugsForMembersOfAConceptSet() {
        Concept allTBDrugsConceptSet = new Concept();
        List<Concept> allTBDrugConcepts = Arrays.asList(new Concept(), new Concept());
        String conceptSetName = "All TB Drugs";
        List<Drug> allTBDrugs = Arrays.asList(new Drug(), new Drug());

        when(bahmniConceptDao.getConceptByFullySpecifiedName(conceptSetName)).thenReturn(allTBDrugsConceptSet);
        when(conceptService.getConceptsByConceptSet(allTBDrugsConceptSet)).thenReturn(allTBDrugConcepts);
        when(bahmniConceptDao.getDrugByListOfConcepts(allTBDrugConcepts)).thenReturn(allTBDrugs);

        Collection<Drug> drugs = bahmniConceptService.getDrugsByConceptSetName(conceptSetName);

        assertThat(drugs, containsInAnyOrder(allTBDrugs.toArray()));
    }
}