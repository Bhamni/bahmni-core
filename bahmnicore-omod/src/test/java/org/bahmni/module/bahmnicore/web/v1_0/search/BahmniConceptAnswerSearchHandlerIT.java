package org.bahmni.module.bahmnicore.web.v1_0.search;


import org.junit.Assert;
import org.junit.Test;
import org.openmrs.module.webservices.rest.SimpleObject;
import org.openmrs.module.webservices.rest.web.RestConstants;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.List;

public class BahmniConceptAnswerSearchHandlerIT extends MainResourceControllerTest {
    @Override
    public String getURI() {
        return "concept";
    }

    @Override
    public String getUuid() {
        return null;
    }

    @Override
    public long getAllCount() {
        return 0;
    }


    @Test
    public void shouldSearchAllConceptAnswersOfQuery() throws Exception {
        MockHttpServletRequest req = request(RequestMethod.GET, getURI());
        req.addParameter("q", "SIN");
        req.addParameter("s", "byQuestion");
        req.addParameter("question", "CIVIL STATUS");
        req.addParameter("v", RestConstants.REPRESENTATION_DEFAULT);

        SimpleObject result = deserialize(handle(req));
        List<Object> hits = (List<Object>) result.get("results");
        Assert.assertEquals(1, hits.size());
    }

    @Test
    public void shouldPerformACaseInsensitiveSearch() throws Exception {
        MockHttpServletRequest req = request(RequestMethod.GET, getURI());
        req.addParameter("q", "sIn");
        req.addParameter("s", "byQuestion");
        req.addParameter("question", "CIVIL STATUS");
        req.addParameter("v", RestConstants.REPRESENTATION_DEFAULT);

        SimpleObject result = deserialize(handle(req));
        List<Object> hits = (List<Object>) result.get("results");
        Assert.assertEquals(1, hits.size());
    }
}
