package org.openmrs.module.bahmnicore.web.v1_0.resource;


import org.bahmni.module.bahmnicore.model.bahmniPatientProgram.BahmniPatientProgram;
import org.bahmni.module.bahmnicore.service.BahmniProgramWorkflowService;
import org.openmrs.PatientProgram;
import org.openmrs.module.webservices.rest.web.RequestContext;
import org.openmrs.module.webservices.rest.web.RestConstants;
import org.openmrs.module.webservices.rest.web.annotation.Resource;
import org.openmrs.module.webservices.rest.web.representation.DefaultRepresentation;
import org.openmrs.module.webservices.rest.web.representation.FullRepresentation;
import org.openmrs.module.webservices.rest.web.representation.Representation;
import org.openmrs.module.webservices.rest.web.resource.impl.DelegatingCrudResource;
import org.openmrs.module.webservices.rest.web.resource.impl.DelegatingResourceDescription;
import org.openmrs.module.webservices.rest.web.response.ResponseException;
import org.springframework.beans.factory.annotation.Autowired;

@Resource(name = RestConstants.VERSION_1 + "/bahmniprogramenrollment", supportedClass = BahmniPatientProgram.class, supportedOpenmrsVersions = {"2.*"}, order = 0)
public class BahmniProgramEnrollmentResource extends DelegatingCrudResource<BahmniPatientProgram> {

    @Autowired
    BahmniProgramWorkflowService bahmniProgramWorkflowService;

    @SuppressWarnings("unchecked")
    @Override
    public BahmniPatientProgram getByUniqueId(String uuid) {
        PatientProgram patientProgram = bahmniProgramWorkflowService.getPatientProgramByUuid(uuid);
        BahmniPatientProgram bahmniPatientProgram = (BahmniPatientProgram) patientProgram;
//        List<PatientProgramAttribute> patientProgramAttributes = bahmniProgramWorkflowService.getAttributesByPatientProgramId(patientProgram.getPatientProgramId());
//        bahmniPatientProgram.setAttributes((Set<PatientProgramAttribute>) patientProgramAttributes);
        return bahmniPatientProgram;
    }

    @Override
    protected void delete(BahmniPatientProgram delegate, String reason, RequestContext requestContext) throws ResponseException {
        if (delegate.isVoided()) {
            // DELETE is idempotent, so we return success here
            return;
        }
        bahmniProgramWorkflowService.voidPatientProgram(delegate, reason);
    }

    @Override
    public BahmniPatientProgram newDelegate() {
        return new BahmniPatientProgram();
    }

    @Override
    public BahmniPatientProgram save(BahmniPatientProgram bahmniPatientProgram) {
        return bahmniProgramWorkflowService.saveBahmniPatientProgram(bahmniPatientProgram);
    }

    @Override
    public void purge(BahmniPatientProgram bahmniPatientProgram, RequestContext requestContext) throws ResponseException {
        bahmniProgramWorkflowService.purgePatientProgram(bahmniPatientProgram);
    }

    @Override
    public DelegatingResourceDescription getRepresentationDescription(Representation rep) {
        if (rep instanceof DefaultRepresentation) {
            DelegatingResourceDescription description = new DelegatingResourceDescription();
            description.addProperty("patientProgram", Representation.DEFAULT);
            description.addProperty("attributes", Representation.DEFAULT);
            description.addSelfLink();
            description.addLink("full", ".?v=" + RestConstants.REPRESENTATION_FULL);
            return description;
        } else if (rep instanceof FullRepresentation) {
            DelegatingResourceDescription description = new DelegatingResourceDescription();
            description.addProperty("patientProgram", Representation.FULL);
            description.addProperty("attributes", Representation.FULL);
            return description;
        } else {
            return null;
        }
    }
}
