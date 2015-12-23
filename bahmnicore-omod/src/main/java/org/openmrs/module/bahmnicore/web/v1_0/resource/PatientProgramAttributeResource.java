package org.openmrs.module.bahmnicore.web.v1_0.resource;


import org.bahmni.module.bahmnicore.model.bahmniPatientProgram.PatientProgramAttribute;
import org.bahmni.module.bahmnicore.model.bahmniPatientProgram.BahmniPatientProgram;
import org.bahmni.module.bahmnicore.model.bahmniPatientProgram.ProgramAttributeType;
import org.bahmni.module.bahmnicore.service.BahmniProgramWorkflowService;
import org.openmrs.module.webservices.rest.web.RequestContext;
import org.openmrs.module.webservices.rest.web.RestConstants;
import org.openmrs.module.webservices.rest.web.annotation.PropertySetter;
import org.openmrs.module.webservices.rest.web.annotation.Resource;
import org.openmrs.module.webservices.rest.web.resource.api.PageableResult;
import org.openmrs.module.webservices.rest.web.resource.impl.NeedsPaging;
import org.openmrs.module.webservices.rest.web.response.ResponseException;
import org.openmrs.module.webservices.rest.web.v1_0.resource.openmrs1_9.BaseAttributeCrudResource1_9;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

@Resource(name = RestConstants.VERSION_1 + "/patientprogramattribute", supportedClass = PatientProgramAttribute.class, supportedOpenmrsVersions = {"2.*"}, order = 0)
public class PatientProgramAttributeResource  extends BaseAttributeCrudResource1_9<PatientProgramAttribute, BahmniPatientProgram, BahmniProgramEnrollmentResource> {

    @Autowired
    BahmniProgramWorkflowService bahmniProgramWorkflowService;

    @PropertySetter("attributeType")
    public static void setAttributeType(PatientProgramAttribute instance, ProgramAttributeType attr) {
        instance.setAttributeType(attr);
    }

    @Override
    public PatientProgramAttribute getByUniqueId(String id) {
        return bahmniProgramWorkflowService.getPatientProgramAttributeByUuid(id);
    }

    @Override
    protected void delete(PatientProgramAttribute delegate, String reason, RequestContext context) throws ResponseException {
        delegate.setVoided(true);
        delegate.setVoidReason(reason);
        bahmniProgramWorkflowService.saveBahmniPatientProgram((BahmniPatientProgram) delegate.getPatientProgram());
    }

    @Override
    public PatientProgramAttribute newDelegate() {
        return new PatientProgramAttribute();
    }

    @Override
    public PatientProgramAttribute save(PatientProgramAttribute delegate) {
        boolean needToAdd = true;
        BahmniPatientProgram bahmniPatientProgram = (BahmniPatientProgram) delegate.getPatientProgram();
        for (PatientProgramAttribute pa : bahmniPatientProgram.getActiveAttributes()) {
            if (pa.equals(delegate)) {
                needToAdd = false;
                break;
            }
        }
        if (needToAdd) {
            bahmniPatientProgram.addAttribute(delegate);
        }
        bahmniProgramWorkflowService.saveBahmniPatientProgram(new BahmniPatientProgram(delegate.getPatientProgram()));
        return delegate;
    }

    @Override
    public void purge(PatientProgramAttribute patientProgramAttribute, RequestContext requestContext) throws ResponseException {
        throw new UnsupportedOperationException("Cannot purge PatientProgramAttribute");
    }
    @Override
    public BahmniPatientProgram getParent(PatientProgramAttribute patientProgramAttribute) {
        return new BahmniPatientProgram(patientProgramAttribute.getPatientProgram());
    }

    @Override
    public void setParent(PatientProgramAttribute patientProgramAttribute, BahmniPatientProgram bahmniPatientProgram) {
        patientProgramAttribute.setPatientProgram(bahmniPatientProgram);
    }

    @Override
    public PageableResult doGetAll(BahmniPatientProgram parent, RequestContext context) throws ResponseException {
        return new NeedsPaging<>((List<PatientProgramAttribute>) parent.getActiveAttributes(), context);
    }
}
