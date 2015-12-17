package org.openmrs.module.bahmnicore.web.v1_0.resource;

import org.bahmni.module.bahmnicore.model.bahmniPatientProgram.PatientProgramAttributeType;
import org.bahmni.module.bahmnicore.service.BahmniProgramWorkflowService;
import org.openmrs.module.webservices.rest.web.RequestContext;
import org.openmrs.module.webservices.rest.web.RestConstants;
import org.openmrs.module.webservices.rest.web.annotation.Resource;
import org.openmrs.module.webservices.rest.web.response.ResponseException;
import org.openmrs.module.webservices.rest.web.v1_0.resource.openmrs1_9.BaseAttributeTypeCrudResource1_9;
import org.springframework.beans.factory.annotation.Autowired;

@Resource(name = RestConstants.VERSION_1 + "/patientprogramattributeType", supportedClass = PatientProgramAttributeType.class, supportedOpenmrsVersions = {"2.0.*"})
public class PatientProgramAttributeTypeResource extends BaseAttributeTypeCrudResource1_9<PatientProgramAttributeType> {

    @Autowired
    BahmniProgramWorkflowService bahmniProgramWorkflowService;

    @Override
    public PatientProgramAttributeType getByUniqueId(String uuid) {
        return bahmniProgramWorkflowService.getPatientProgramAttributeTypeByUuid(uuid);
    }

    @Override
    public PatientProgramAttributeType newDelegate() {
        return new PatientProgramAttributeType();
    }

    @Override
    public PatientProgramAttributeType save(PatientProgramAttributeType patientProgramAttributeType) {
        return bahmniProgramWorkflowService.savePatientProgramAttributeType(patientProgramAttributeType);
    }

    @Override
    public void purge(PatientProgramAttributeType patientProgramAttributeType, RequestContext requestContext) throws ResponseException {
        bahmniProgramWorkflowService.purgePatientProgramAttributeType(patientProgramAttributeType);
    }
}
