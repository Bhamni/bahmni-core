package org.openmrs.module.bahmnicore.web.v1_0.resource;


import org.bahmni.module.bahmnicore.model.bahmniPatientProgram.BahmniPatientProgram;
import org.bahmni.module.bahmnicore.model.bahmniPatientProgram.PatientProgramAttribute;
import org.bahmni.module.bahmnicore.service.BahmniProgramWorkflowService;
import org.openmrs.Location;
import org.openmrs.LocationAttribute;
import org.openmrs.Patient;
import org.openmrs.PatientProgram;
import org.openmrs.PatientState;
import org.openmrs.Program;
import org.openmrs.api.PatientService;
import org.openmrs.api.context.Context;
import org.openmrs.module.webservices.rest.web.RequestContext;
import org.openmrs.module.webservices.rest.web.RestConstants;
import org.openmrs.module.webservices.rest.web.annotation.PropertySetter;
import org.openmrs.module.webservices.rest.web.annotation.Resource;
import org.openmrs.module.webservices.rest.web.representation.DefaultRepresentation;
import org.openmrs.module.webservices.rest.web.representation.FullRepresentation;
import org.openmrs.module.webservices.rest.web.representation.Representation;
import org.openmrs.module.webservices.rest.web.resource.api.PageableResult;
import org.openmrs.module.webservices.rest.web.resource.impl.DelegatingCrudResource;
import org.openmrs.module.webservices.rest.web.resource.impl.DelegatingResourceDescription;
import org.openmrs.module.webservices.rest.web.resource.impl.EmptySearchResult;
import org.openmrs.module.webservices.rest.web.resource.impl.NeedsPaging;
import org.openmrs.module.webservices.rest.web.response.ResponseException;
import org.openmrs.module.webservices.rest.web.v1_0.resource.openmrs1_10.ProgramEnrollmentResource1_10;
import org.openmrs.util.OpenmrsUtil;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Set;

@Resource(name = RestConstants.VERSION_1 + "/programenrollment", supportedClass = BahmniPatientProgram.class, supportedOpenmrsVersions = {"1.12.*,2.*"}, order = 0)
public class BahmniProgramEnrollmentResource extends ProgramEnrollmentResource1_10 {

    @PropertySetter("attributes")
    public static void setAttributes(BahmniPatientProgram instance, List<PatientProgramAttribute> attrs) {
        for (PatientProgramAttribute attr : attrs) {
            instance.addAttribute(attr);
        }
    }

    @Override
    public PatientProgram newDelegate() {
        return new BahmniPatientProgram();
    }

    @Override
    public DelegatingResourceDescription getRepresentationDescription(Representation rep) {
        DelegatingResourceDescription parentRep = super.getRepresentationDescription(rep);
        if (rep instanceof DefaultRepresentation) {
            parentRep.addProperty("attributes", Representation.REF);
            return parentRep;
        } else if (rep instanceof FullRepresentation) {
            parentRep.addProperty("states", Representation.REF);
            parentRep.addProperty("attributes", Representation.DEFAULT);
            return parentRep;
        } else {
            return null;
        }
    }

    @Override
    public DelegatingResourceDescription getCreatableProperties() {
        DelegatingResourceDescription delegatingResourceDescription = super.getCreatableProperties();
        delegatingResourceDescription.addProperty("attributes");
        return delegatingResourceDescription;
    }

    @Override
    public DelegatingResourceDescription getUpdatableProperties() {
        DelegatingResourceDescription delegatingResourceDescription = super.getUpdatableProperties();
        delegatingResourceDescription.addProperty("attributes");
        return delegatingResourceDescription;
    }

    public PatientProgram getByUniqueId(String uniqueId) {
        return Context.getService(BahmniProgramWorkflowService.class).getPatientProgramByUuid(uniqueId);
    }

    protected void delete(PatientProgram delegate, String reason, RequestContext context) throws ResponseException {
        if(!delegate.isVoided().booleanValue()) {
            Context.getService(BahmniProgramWorkflowService.class).voidPatientProgram(delegate, reason);
        }
    }

    public void purge(PatientProgram delegate, RequestContext context) throws ResponseException {
        Context.getService(BahmniProgramWorkflowService.class).purgePatientProgram(delegate);
    }

    @Override
    public List<String> getPropertiesToExposeAsSubResources() {
        return Arrays.asList("attributes");
    }

    public PatientProgram save(PatientProgram delegate) {
        return Context.getService(BahmniProgramWorkflowService.class).savePatientProgram(delegate);
    }

    protected PageableResult doSearch(RequestContext context) {
        String patientUuid = context.getRequest().getParameter("patient");
        if(patientUuid != null) {
            PatientService patientService = Context.getPatientService();
            Patient patient = patientService.getPatientByUuid(patientUuid);
            if(patient == null) {
                return new EmptySearchResult();
            } else {
                List patientPrograms = Context.getService(BahmniProgramWorkflowService.class).getPatientPrograms(patient, (Program)null, (Date)null, (Date)null, (Date)null, (Date)null, true);
                return new NeedsPaging(patientPrograms, context);
            }
        } else {
            return super.doSearch(context);
        }
    }
}
