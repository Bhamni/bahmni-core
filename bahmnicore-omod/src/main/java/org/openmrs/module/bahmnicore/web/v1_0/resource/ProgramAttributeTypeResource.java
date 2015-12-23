package org.openmrs.module.bahmnicore.web.v1_0.resource;

import org.bahmni.module.bahmnicore.model.bahmniPatientProgram.ProgramAttributeType;
import org.bahmni.module.bahmnicore.service.BahmniProgramWorkflowService;
import org.openmrs.module.webservices.rest.web.RequestContext;
import org.openmrs.module.webservices.rest.web.RestConstants;
import org.openmrs.module.webservices.rest.web.annotation.Resource;
import org.openmrs.module.webservices.rest.web.response.ResponseException;
import org.openmrs.module.webservices.rest.web.v1_0.resource.openmrs1_9.BaseAttributeTypeCrudResource1_9;
import org.springframework.beans.factory.annotation.Autowired;

@Resource(name = RestConstants.VERSION_1 + "/programattributeType", supportedClass = ProgramAttributeType.class, supportedOpenmrsVersions = {"2.0.*"})
public class ProgramAttributeTypeResource extends BaseAttributeTypeCrudResource1_9<ProgramAttributeType> {

    @Autowired
    BahmniProgramWorkflowService bahmniProgramWorkflowService;

    @Override
    public ProgramAttributeType getByUniqueId(String uuid) {
        return bahmniProgramWorkflowService.getProgramAttributeTypeByUuid(uuid);
    }

    @Override
    public ProgramAttributeType newDelegate() {
        return new ProgramAttributeType();
    }

    @Override
    public ProgramAttributeType save(ProgramAttributeType programAttributeType) {
        return bahmniProgramWorkflowService.saveProgramAttributeType(programAttributeType);
    }

    @Override
    public void purge(ProgramAttributeType programAttributeType, RequestContext requestContext) throws ResponseException {
        bahmniProgramWorkflowService.purgeProgramAttributeType(programAttributeType);
    }
}
