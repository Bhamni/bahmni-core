package org.bahmni.module.bahmnicore.web.v1_0.search;

import org.bahmni.module.referencedata.labconcepts.contract.Concept;
import org.openmrs.Drug;
import org.openmrs.api.context.Context;
import org.openmrs.module.webservices.rest.web.RequestContext;
import org.openmrs.module.webservices.rest.web.RestConstants;
import org.openmrs.module.webservices.rest.web.resource.api.PageableResult;
import org.openmrs.module.webservices.rest.web.resource.api.SearchConfig;
import org.openmrs.module.webservices.rest.web.resource.api.SearchHandler;
import org.openmrs.module.webservices.rest.web.resource.api.SearchQuery;
import org.openmrs.module.webservices.rest.web.resource.impl.NeedsPaging;
import org.openmrs.module.webservices.rest.web.response.ResponseException;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;

@Component
public class BahmniDrugSearchHandler implements SearchHandler {

    @Override
    public SearchConfig getSearchConfig() {
        SearchQuery searchQuery = new SearchQuery.Builder("Allows you to search for drugs").withRequiredParameters("q").build();
        return new SearchConfig("ordered", RestConstants.VERSION_1 + "/drug", Arrays.asList("1.10.*", "1.11.*", "1.12.*"), searchQuery);
    }

    @Override
    public PageableResult search(RequestContext ctx) throws ResponseException {
        boolean includeRetired = ctx.getIncludeAll();
            String searchPhrase = ctx.getParameter("q");
            LinkedHashSet<Drug> drugs = new LinkedHashSet<>(findDrugsStartingWith(searchPhrase, includeRetired, ctx));
            LinkedHashSet<Drug> drugsHavingConcept = new LinkedHashSet<>();


        drugs.addAll(findDrugsContaining(searchPhrase, includeRetired, ctx));

            if(ctx.getParameter("conceptUuid") != null) {
                String conceptUuid = ctx.getParameter("conceptUuid");
                for(Drug drug : drugs){
                    if(drug.getConcept().getUuid().equals(conceptUuid)){
                        drugsHavingConcept.add(drug);
                    }
                }
                return new NeedsPaging<>(new ArrayList<>(drugsHavingConcept), ctx);
            }

            return new NeedsPaging<>(new ArrayList<>(drugs), ctx);
    }

    private List<Drug> findDrugsStartingWith(String searchPhrase, boolean includeRetired, RequestContext ctx) {
        return Context.getConceptService().getDrugs(searchPhrase, null, false, true, includeRetired, ctx.getStartIndex(), ctx.getLimit());
    }

    private List<Drug> findDrugsContaining(String searchPhrase, boolean includeRetired, RequestContext ctx) {
        return Context.getConceptService().getDrugs(searchPhrase, null, true, true, includeRetired, ctx.getStartIndex(), ctx.getLimit());
    }
}
