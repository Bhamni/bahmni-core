package org.openmrs.module.bahmnicore.web.v1_0.resource;

import org.bahmni.module.bahmnicore.model.bahmniPatientProgram.BahmniPatientProgram;
import org.bahmni.module.bahmnicore.model.bahmniPatientProgram.PatientProgramAttribute;
import org.bahmni.module.bahmnicore.model.bahmniPatientProgram.ProgramAttributeType;
import org.bahmni.module.bahmnicore.service.BahmniProgramWorkflowService;
import org.openmrs.api.context.Context;
import org.openmrs.module.webservices.rest.web.RequestContext;
import org.openmrs.module.webservices.rest.web.annotation.PropertySetter;
import org.openmrs.module.webservices.rest.web.annotation.SubResource;
import org.openmrs.module.webservices.rest.web.resource.api.PageableResult;
import org.openmrs.module.webservices.rest.web.resource.impl.NeedsPaging;
import org.openmrs.module.webservices.rest.web.response.ResponseException;
import org.openmrs.module.webservices.rest.web.v1_0.resource.openmrs1_9.BaseAttributeCrudResource1_9;

import java.util.Collection;
import java.util.List;

@SubResource(parent = BahmniProgramEnrollmentResource.class, path = "attribute", supportedClass = PatientProgramAttribute.class, supportedOpenmrsVersions = {"1.12.*","2.*"})
public class PatientProgramAttributeResource  extends BaseAttributeCrudResource1_9<PatientProgramAttribute, BahmniPatientProgram, BahmniProgramEnrollmentResource> {


    @PropertySetter("attributeType")
    public static void setAttributeType(PatientProgramAttribute instance, ProgramAttributeType attr) {
        instance.setAttributeType(attr);
    }
    @Override
    public BahmniPatientProgram getParent(PatientProgramAttribute instance) {
        return instance.getPatientProgram();
    }

    @Override
    public void setParent(PatientProgramAttribute patientProgramAttribute, BahmniPatientProgram bahmniPatientProgram) {
        patientProgramAttribute.setPatientProgram(bahmniPatientProgram);

    }

    @Override
    public PageableResult doGetAll(BahmniPatientProgram parent, RequestContext context)
            throws ResponseException {
        return new NeedsPaging<>((List<PatientProgramAttribute>) parent.getActiveAttributes(), context);
    }

    @Override
    public PatientProgramAttribute getByUniqueId(String uniqueId) {
        return Context.getService(BahmniProgramWorkflowService.class).getPatientProgramAttributeByUuid(uniqueId);
    }

    @Override
    protected void delete(PatientProgramAttribute delegate, String reason, RequestContext context)
            throws ResponseException {
        delegate.setVoided(true);
        delegate.setVoidReason(reason);
        Context.getService(BahmniProgramWorkflowService.class).savePatientProgram(delegate.getPatientProgram());
    }

    @Override
    public PatientProgramAttribute newDelegate() {
        return new PatientProgramAttribute();
    }

    @Override
    public PatientProgramAttribute save(PatientProgramAttribute delegate) {
        boolean needToAdd = true;
        Collection<PatientProgramAttribute> activeAttributes = delegate.getPatientProgram().getActiveAttributes();
        for (PatientProgramAttribute pa : activeAttributes) {
            if (pa.equals(delegate)) {
                needToAdd = false;
                break;
            }
        }
        if (needToAdd) {
            delegate.getPatientProgram().addAttribute(delegate);
        }
        Context.getService(BahmniProgramWorkflowService.class).savePatientProgram(delegate.getPatientProgram());
        return delegate;
    }

    @Override
    public void purge(PatientProgramAttribute patientProgramAttribute, RequestContext requestContext)
            throws ResponseException {
        throw new UnsupportedOperationException("Cannot purge PatientProgramAttribute");
    }
}
