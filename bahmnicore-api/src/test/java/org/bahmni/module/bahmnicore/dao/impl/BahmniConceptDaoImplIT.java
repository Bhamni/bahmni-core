package org.bahmni.module.bahmnicore.dao.impl;

import org.bahmni.module.bahmnicore.BaseIntegrationTest;
import org.bahmni.module.bahmnicore.dao.BahmniConceptDao;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.Concept;
import org.openmrs.Drug;
import org.openmrs.api.ConceptService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Collection;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.junit.Assert.*;

public class BahmniConceptDaoImplIT extends BaseIntegrationTest{
    @Autowired
    private BahmniConceptDao bahmniConceptDao;

    @Autowired
    private ConceptService conceptService;
    private Concept questionConcept;

    @Test
    public void shouldReturnNonVoidedAnswersForAQuestion() throws Exception {
        executeDataSet("sampleCodedConcept.xml");
        questionConcept = conceptService.getConcept(90);
        Collection<Concept> result = bahmniConceptDao.searchByQuestion(questionConcept, "Aneurism");

        assertThat(result.size(), is(equalTo(1)));

        Concept resultConcept = result.iterator().next();
        assertTrue(resultConcept.getId().equals(902));
    }

    @Test
    public void shouldIgnoreCaseWhenSearching() throws Exception {
        executeDataSet("sampleCodedConcept.xml");
        questionConcept = conceptService.getConcept(90);
        Collection<Concept> result = bahmniConceptDao.searchByQuestion(questionConcept, "aNeUrIsM");

        assertThat(result.size(), is(equalTo(1)));

        Concept resultConcept = result.iterator().next();
        assertTrue(resultConcept.getId().equals(902));
    }

    @Test
    public void shouldNotReturnVoidedAnswers() throws Exception {
        executeDataSet("sampleCodedConcept.xml");
        questionConcept = conceptService.getConcept(90);
        Collection<Concept> result = bahmniConceptDao.searchByQuestion(questionConcept, "Porphyria");
        assertThat(result.size(), is(equalTo(0)));
    }

    @Test
    public void shouldSearchEachTermByQuestion() throws Exception {
        executeDataSet("sampleCodedConcept.xml");
        questionConcept = conceptService.getConcept(90);

        //Searching for "Abscess, Skin"
        Collection<Concept> result = bahmniConceptDao.searchByQuestion(questionConcept, " ab sk  ");
        assertThat(result.size(), is(equalTo(1)));
        Concept resultConcept = result.iterator().next();
        assertTrue(resultConcept.getId().equals(903));

        result = bahmniConceptDao.searchByQuestion(questionConcept, "in  ab");
        assertThat(result.size(), is(equalTo(2)));
        assertThat(result, containsInAnyOrder(conceptService.getConcept(902), conceptService.getConcept(903)));

        result = bahmniConceptDao.searchByQuestion(questionConcept, "in  and another term that is not present");
        assertThat(result.size(), is(equalTo(0)));
    }

    @Test
    public void shouldReturnMultipleResultsIfAvailable() throws Exception {
        executeDataSet("sampleCodedConcept.xml");
        questionConcept = conceptService.getConcept(90);

        Collection<Concept> result = bahmniConceptDao.searchByQuestion(questionConcept, "ab");

        assertThat(result.size(), is(equalTo(2)));

        assertThat(result, containsInAnyOrder(conceptService.getConcept(902), conceptService.getConcept(903)));
    }

    @Test
    public void getConceptByFullySpecifiedNameShouldGetConceptByItsFullySpecifiedName() throws Exception{
        executeDataSet("sampleCodedConcept.xml");
        questionConcept = conceptService.getConcept(90);

        Concept result = bahmniConceptDao.getConceptByFullySpecifiedName("Acne");

        assertNotNull(result);
        assertEquals("65230431-2fe5-49fc-b535-ae42bc90979d",result.getUuid());
    }

    @Test
    public void getConceptByFullySpecifiedNameShouldBeCaseInsensitive() throws Exception{
        executeDataSet("sampleCodedConcept.xml");
        questionConcept = conceptService.getConcept(90);

        Concept result = bahmniConceptDao.getConceptByFullySpecifiedName("ACNE");

        assertNotNull(result);
        assertEquals("65230431-2fe5-49fc-b535-ae42bc90979d",result.getUuid());
    }

    @Test
    public void searchByQuestionShouldGetAllConceptAnswersWhenQueryIsEmpty() throws Exception {
        executeDataSet("sampleCodedConcept.xml");
        questionConcept = conceptService.getConcept(90);

        Collection<Concept> result = bahmniConceptDao.searchByQuestion(questionConcept, null);

        assertEquals(4,result.size());
    }

    @Test
    public void getByConceptSetShouldRetrieveDrugsForSetMembersOfTheConceptSet() throws Exception {
        executeDataSet("drugsWithConcepts.xml");

        Collection<Drug> drugs = bahmniConceptDao.getDrugByListOfConcepts(
                conceptService.getConceptsByConceptSet(conceptService.getConcept(3010)));
        assertEquals(2, drugs.size());
        assertThat(drugs, containsInAnyOrder(conceptService.getDrug(2001), conceptService.getDrug(4001)));
    }
}