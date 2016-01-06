package org.openmrs.module.bahmnicore.web.v1_0.resource;

import org.bahmni.module.bahmnicore.model.bahmniPatientProgram.ProgramAttributeType;
import org.bahmni.module.bahmnicore.service.BahmniProgramWorkflowService;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.api.context.Context;
import org.openmrs.module.webservices.rest.web.RequestContext;
import org.openmrs.module.webservices.rest.web.resource.impl.BaseDelegatingResourceTest;
import org.openmrs.module.webservices.rest.web.resource.impl.NeedsPaging;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.Assert.assertEquals;

@org.springframework.test.context.ContextConfiguration(locations = {"classpath:TestingApplicationContext.xml"}, inheritLocations = true)
public class ProgramAttributeTypeResourceTest extends BaseDelegatingResourceTest<ProgramAttributeTypeResource, ProgramAttributeType> {

    @Before
    public void before() throws Exception {
        executeDataSet("programEnrollmentDataSet.xml");
    }

    @Override
    public ProgramAttributeType newObject() {
        return Context.getService(BahmniProgramWorkflowService.class).getProgramAttributeTypeByUuid(getUuidProperty());
    }

    @Override
    public void validateDefaultRepresentation() throws Exception {
        super.validateDefaultRepresentation();
        assertPropEquals("name", getObject().getName());
        assertPropEquals("description", getObject().getDescription());
        assertPropEquals("datatypeClassname", getObject().getDatatypeClassname());
        assertPropEquals("preferredHandlerClassname", getObject().getPreferredHandlerClassname());
        assertPropEquals("retired", getObject().getRetired());
    }

    @Override
    public void validateFullRepresentation() throws Exception {
        super.validateFullRepresentation();
        assertPropEquals("name", getObject().getName());
        assertPropEquals("description", getObject().getDescription());
        assertPropEquals("minOccurs", getObject().getMinOccurs());
        assertPropEquals("maxOccurs", getObject().getMaxOccurs());
        assertPropEquals("datatypeClassname", getObject().getDatatypeClassname());
        assertPropEquals("datatypeConfig", getObject().getDatatypeConfig());
        assertPropEquals("preferredHandlerClassname", getObject().getPreferredHandlerClassname());
        assertPropEquals("handlerConfig", getObject().getHandlerConfig());
        assertPropEquals("retired", getObject().getRetired());
        assertPropPresent("auditInfo");
    }

    @Test
    public void ensureGetAllReturnsAllTheAttributes(){
        RequestContext context = new RequestContext();
        context.setLimit(100);
        context.setStartIndex(0);
        NeedsPaging<ProgramAttributeType> programAttributeTypes =  getResource().doGetAll(context);
        assertEquals(2, programAttributeTypes.getPageOfResults().size());
        assertEquals("d7477c21-bfc3-4922-9591-e89d8b9c8efb", programAttributeTypes.getPageOfResults().get(0).getUuid());
        assertEquals("d7477c21-bfc3-4922-9591-e89d8b9c8efe", programAttributeTypes.getPageOfResults().get(1).getUuid());
    }

    @Override
    public String getDisplayProperty() {
        return "stage";
    }

    @Override
    public String getUuidProperty() {
        return RestConstants.PROGRAM_ATTRIBUTE_TYPE_UUID;
    }
}
