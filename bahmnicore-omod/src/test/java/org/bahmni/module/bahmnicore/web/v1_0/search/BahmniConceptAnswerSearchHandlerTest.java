package org.bahmni.module.bahmnicore.web.v1_0.search;

import org.bahmni.module.bahmnicore.service.BahmniConceptService;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.openmrs.Concept;
import org.openmrs.module.emrapi.encounter.domain.EncounterTransaction;
import org.openmrs.module.emrapi.encounter.exception.ConceptNotFoundException;
import org.openmrs.module.webservices.rest.web.RequestContext;
import org.openmrs.module.webservices.rest.web.resource.api.PageableResult;
import org.openmrs.module.webservices.rest.web.resource.api.SearchConfig;
import org.openmrs.module.webservices.rest.web.resource.impl.AlreadyPaged;
import org.openmrs.module.webservices.rest.web.resource.impl.NeedsPaging;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

public class BahmniConceptAnswerSearchHandlerTest {

    public static final String QUESTION_CONCEPT = "questionConcept";
    public static final String QUERY = "query";
    @Mock
    private BahmniConceptService bahmniConceptService;
    private BahmniConceptAnswerSearchHandler bahmniConceptAnswerSearchHandler;
    @Mock
    RequestContext requestContext;

    @Before
    public void before() {
        initMocks(this);
        bahmniConceptAnswerSearchHandler = new BahmniConceptAnswerSearchHandler(bahmniConceptService);
    }

    @Test
    public void shouldSearchByQuestion() {
        SearchConfig searchConfig = bahmniConceptAnswerSearchHandler.getSearchConfig();
        assertThat(searchConfig.getId(), is(equalTo("byQuestion")));
    }

    @Test
    public void shouldSupportVersions1_10To1_12() {
        SearchConfig searchConfig = bahmniConceptAnswerSearchHandler.getSearchConfig();
        assertTrue(searchConfig.getSupportedOpenmrsVersions().contains("1.10.*"));
        assertTrue(searchConfig.getSupportedOpenmrsVersions().contains("1.11.*"));
        assertTrue(searchConfig.getSupportedOpenmrsVersions().contains("1.12.*"));
    }

    @Test
    public void shouldDelegateSearchOfConceptsToBahmniConceptService() {
        Collection<Concept> conceptServiceResult = new ArrayList<>();
        when(bahmniConceptService.searchByQuestion(QUESTION_CONCEPT, QUERY)).thenReturn(conceptServiceResult);
        when(requestContext.getParameter("question")).thenReturn(QUESTION_CONCEPT);
        when(requestContext.getParameter("q")).thenReturn(QUERY);

        NeedsPaging<Concept> searchResults = (NeedsPaging<Concept>) bahmniConceptAnswerSearchHandler.search(requestContext);

        assertThat(searchResults.getPageOfResults().size(), is(equalTo(0)));
    }
}