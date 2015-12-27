package org.bahmni.module.bahmnicore.web.v1_0.search;

import org.bahmni.module.bahmnicore.service.BahmniConceptService;
import org.openmrs.Drug;
import org.openmrs.api.ConceptService;
import org.openmrs.module.webservices.rest.web.RequestContext;
import org.openmrs.module.webservices.rest.web.RestConstants;
import org.openmrs.module.webservices.rest.web.resource.api.PageableResult;
import org.openmrs.module.webservices.rest.web.resource.api.SearchConfig;
import org.openmrs.module.webservices.rest.web.resource.api.SearchHandler;
import org.openmrs.module.webservices.rest.web.resource.api.SearchQuery;
import org.openmrs.module.webservices.rest.web.resource.impl.AlreadyPaged;
import org.openmrs.module.webservices.rest.web.response.ResponseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

@Component
public class ConceptSetBasedDrugSearchHandler implements SearchHandler{

    @Autowired
    private BahmniConceptService bahmniConceptService;

    @Override
    public SearchConfig getSearchConfig() {
        SearchQuery searchQuery = new SearchQuery.Builder("Allows you to search for Drugs based on concept set").withRequiredParameters("q").build();
        return new SearchConfig("byConceptSet", RestConstants.VERSION_1 + "/drug", Arrays.asList("1.9.*", "1.10.*", "1.11.*", "1.12.*"), searchQuery);
    }

    @Override
    public PageableResult search(RequestContext requestContext) throws ResponseException {
        String conceptSetName = requestContext.getParameter("q");
        Collection<Drug> drugs = bahmniConceptService.getDrugsByConceptSetName(conceptSetName);
        List<Drug> drugResponse = new ArrayList<>(drugs);
        return new AlreadyPaged<>(requestContext, drugResponse, false);
    }
}
