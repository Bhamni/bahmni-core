package org.openmrs.module.bahmnicore.web.v1_0.resource;

import org.bahmni.module.bahmnicore.model.bahmniPatientProgram.ProgramAttributeType;
import org.bahmni.module.bahmnicore.service.BahmniProgramWorkflowService;
import org.openmrs.api.context.Context;
import org.openmrs.module.webservices.rest.web.RequestContext;
import org.openmrs.module.webservices.rest.web.RestConstants;
import org.openmrs.module.webservices.rest.web.annotation.Resource;
import org.openmrs.module.webservices.rest.web.resource.impl.NeedsPaging;
import org.openmrs.module.webservices.rest.web.response.ResponseException;
import org.openmrs.module.webservices.rest.web.v1_0.resource.openmrs1_9.BaseAttributeTypeCrudResource1_9;

@Resource(name = RestConstants.VERSION_1 + "/programattributetype", supportedClass = ProgramAttributeType.class, supportedOpenmrsVersions = {"1.12.*","2.*"})
public class ProgramAttributeTypeResource extends BaseAttributeTypeCrudResource1_9<ProgramAttributeType> {

    @Override
    public ProgramAttributeType getByUniqueId(String uuid) {
        return Context.getService(BahmniProgramWorkflowService.class).getProgramAttributeTypeByUuid(uuid);
    }

    @Override
    public ProgramAttributeType newDelegate() {
        return new ProgramAttributeType();
    }

    @Override
    public ProgramAttributeType save(ProgramAttributeType programAttributeType) {
        return Context.getService(BahmniProgramWorkflowService.class).saveProgramAttributeType(programAttributeType);
    }

    @Override
    public void purge(ProgramAttributeType programAttributeType, RequestContext requestContext) throws ResponseException {
        Context.getService(BahmniProgramWorkflowService.class).purgeProgramAttributeType(programAttributeType);
    }

    @Override
    protected NeedsPaging<ProgramAttributeType> doGetAll(RequestContext context) throws ResponseException {
        return new NeedsPaging<>(Context.getService(BahmniProgramWorkflowService.class).getAllProgramAttributeTypes(),
                context);
    }
}
