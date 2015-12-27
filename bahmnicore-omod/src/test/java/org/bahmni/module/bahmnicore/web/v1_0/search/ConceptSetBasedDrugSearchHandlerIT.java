package org.bahmni.module.bahmnicore.web.v1_0.search;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.module.webservices.rest.SimpleObject;
import org.openmrs.module.webservices.rest.web.RestConstants;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.List;

public class ConceptSetBasedDrugSearchHandlerIT extends MainResourceControllerTest{


    @Override
    public String getURI() {
        return "drug";
    }

    @Override
    public String getUuid() {
        return null;
    }

    @Override
    public long getAllCount() {
        return 0;
    }

    @Before
    public void setUp() throws Exception {
        executeDataSet("drugsWithConcepts.xml");
    }

    @Test
    public void shouldReturnDrugsThatAreChildrenOfGivenConceptName() throws Exception {
        MockHttpServletRequest req = request(RequestMethod.GET, getURI());
        req.addParameter("q", "All TB Drugs");
        req.addParameter("s", "byConceptSet");
        req.addParameter("v", RestConstants.REPRESENTATION_DEFAULT);

        SimpleObject result = deserialize(handle(req));
        List<Object> hits = (List<Object>) result.get("results");
        Assert.assertEquals(2, hits.size());
    }
}
