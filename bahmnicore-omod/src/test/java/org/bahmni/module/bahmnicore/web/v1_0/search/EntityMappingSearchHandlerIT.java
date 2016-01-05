package org.bahmni.module.bahmnicore.web.v1_0.search;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.module.webservices.rest.SimpleObject;
import org.openmrs.module.webservices.rest.web.RestConstants;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.List;

public class EntityMappingSearchHandlerIT extends MainResourceControllerTest {

    private static final String ENTITY_MAPPING_DATA_SET_XML = "entityMappingDataSet.xml";

    @Before
    public void init() throws Exception {
        executeDataSet(ENTITY_MAPPING_DATA_SET_XML);
    }

    /**
     * @see org.openmrs.module.webservices.rest.web.v1_0.controller.MainResourceControllerTest#getURI()
     */
    @Override
    public String getURI() {
        return "entitymapping";
    }

    /**
     * @see org.openmrs.module.webservices.rest.web.v1_0.controller.MainResourceControllerTest#getAllCount()
     */
    @Override
    public long getAllCount() {
        return 0l;
    }

    /**
     * @see org.openmrs.module.webservices.rest.web.v1_0.controller.MainResourceControllerTest#getUuid()
     */
    @Override
    public String getUuid() {
        return null;
    }

    /**
     * @verifies return location by tag uuid
     */
    @Test
    public void shouldRetrieveProgramEntityMapping() throws Exception {
        MockHttpServletRequest req = request(RequestMethod.GET, getURI());
        req.addParameter("entityUuid", "e45931c5-cc97-48bd-b686-e64a28ab2bde");
        req.addParameter("mappingType", "program_obstemplate");
        req.addParameter("s", "byEntityAndMappingType");
        req.addParameter("v", RestConstants.REPRESENTATION_DEFAULT);

        SimpleObject result = deserialize(handle(req));
        List<Object> hits = (List<Object>) result.get("results");
        Assert.assertEquals(1, hits.size());
    }
}
