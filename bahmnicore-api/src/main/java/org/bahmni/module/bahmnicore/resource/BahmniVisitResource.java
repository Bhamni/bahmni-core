package org.bahmni.module.bahmnicore.resource;

import org.openmrs.Patient;
import org.openmrs.Visit;
import org.openmrs.VisitAttribute;
import org.openmrs.api.context.Context;
import org.openmrs.module.webservices.rest.SimpleObject;
import org.openmrs.module.webservices.rest.web.RequestContext;
import org.openmrs.module.webservices.rest.web.RestConstants;
import org.openmrs.module.webservices.rest.web.annotation.PropertySetter;
import org.openmrs.module.webservices.rest.web.api.RestService;
import org.openmrs.module.webservices.rest.web.representation.DefaultRepresentation;
import org.openmrs.module.webservices.rest.web.representation.FullRepresentation;
import org.openmrs.module.webservices.rest.web.representation.Representation;
import org.openmrs.module.webservices.rest.web.resource.impl.DataDelegatingCrudResource;
import org.openmrs.module.webservices.rest.web.resource.impl.DelegatingCrudResource;
import org.openmrs.module.webservices.rest.web.resource.impl.DelegatingResourceDescription;
import org.openmrs.module.webservices.rest.web.resource.impl.NeedsPaging;
import org.openmrs.module.webservices.rest.web.response.ObjectNotFoundException;
import org.openmrs.module.webservices.rest.web.response.ResourceDoesNotSupportOperationException;
import org.openmrs.module.webservices.rest.web.response.ResponseException;
import org.openmrs.module.webservices.rest.web.v1_0.resource.openmrs1_8.PatientResource1_8;

import java.util.Set;

//Copied over from VisitResource since OpenMRS does not publish Rest 1.9 extensions anymore
public class BahmniVisitResource extends DataDelegatingCrudResource<Visit> {

    @Override
    public DelegatingResourceDescription getRepresentationDescription(Representation rep) {
        if (rep instanceof DefaultRepresentation) {
            DelegatingResourceDescription description = new DelegatingResourceDescription();
            description.addProperty("uuid");
            description.addProperty("display", findMethod("getDisplayString"));
            description.addProperty("patient", Representation.REF);
            description.addProperty("visitType", Representation.REF);
            description.addProperty("indication", Representation.REF);
            description.addProperty("location", Representation.REF);
            description.addProperty("startDatetime");
            description.addProperty("stopDatetime");
            description.addProperty("encounters", Representation.REF);
            description.addProperty("attributes", "activeAttributes", Representation.REF);
            description.addProperty("voided");
            description.addSelfLink();
            description.addLink("full", ".?v=" + RestConstants.REPRESENTATION_FULL);
            return description;
        } else if (rep instanceof FullRepresentation) {
            DelegatingResourceDescription description = new DelegatingResourceDescription();
            description.addProperty("uuid");
            description.addProperty("display", findMethod("getDisplayString"));
            description.addProperty("patient", Representation.REF);
            description.addProperty("visitType", Representation.REF);
            description.addProperty("indication", Representation.REF);
            description.addProperty("location", Representation.REF);
            description.addProperty("startDatetime");
            description.addProperty("stopDatetime");
            description.addProperty("encounters", Representation.DEFAULT);
            description.addProperty("attributes", "activeAttributes", Representation.DEFAULT);
            description.addProperty("voided");
            description.addProperty("auditInfo", findMethod("getAuditInfo"));
            description.addSelfLink();
            return description;
        }
        return null;
    }

    public String getDisplayString(Visit visit) {
        String ret = visit.getVisitType().getName();
        ret += " ";
        ret += visit.getLocation() == null ? "?" : "@ " + visit.getLocation().getName();
        ret += " - ";
        ret += Context.getDateTimeFormat().format(visit.getStartDatetime());
        return ret;
    }

    @Override
    public DelegatingResourceDescription getCreatableProperties() {
        DelegatingResourceDescription description = new DelegatingResourceDescription();
        description.addRequiredProperty("patient");
        description.addRequiredProperty("visitType");
        description.addRequiredProperty("startDatetime");

        description.addProperty("location");
        description.addProperty("indication");
        description.addProperty("stopDatetime");
        description.addProperty("encounters");
        description.addProperty("attributes");

        return description;
    }

    @Override
    public DelegatingResourceDescription getUpdatableProperties() throws ResourceDoesNotSupportOperationException {
        DelegatingResourceDescription description = super.getUpdatableProperties();
        description.removeProperty("patient");
        return description;
    }

    @Override
    public Visit newDelegate() {
        return new Visit();
    }

    @Override
    public Visit save(Visit visit) {
        return Context.getVisitService().saveVisit(visit);
    }

    @Override
    public Visit getByUniqueId(String uuid) {
        return Context.getVisitService().getVisitByUuid(uuid);
    }

    @Override
    public void delete(Visit visit, String reason, RequestContext context) throws ResponseException {
        if (visit.isVoided()) {
            // Makes no sense, so we return success here
            return;
        }
        Context.getVisitService().voidVisit(visit, reason);
    }

    @Override
    public void purge(Visit visit, RequestContext context) throws ResponseException {
        if (visit == null)
            return;
        Context.getVisitService().purgeVisit(visit);
    }

    public SimpleObject getVisitsByPatient(String patientUniqueId, RequestContext context) throws ResponseException {
        DelegatingCrudResource<Patient> resource = (DelegatingCrudResource<Patient>) Context.getService(RestService.class).getResourceBySupportedClass(PatientResource1_8.class);
        Patient patient = resource.getByUniqueId(patientUniqueId);
        if (patient == null)
            throw new ObjectNotFoundException();
        return new NeedsPaging<Visit>(Context.getVisitService().getVisitsByPatient(patient, true, false), context)
                .toSimpleObject(this);
    }

    @Override
    public String getResourceVersion() {
        return "1.9";
    }

    @PropertySetter("attributes")
    public static void setAttributes(Visit visit, Set<VisitAttribute> attributes) {
        for (VisitAttribute attribute : attributes) {
            attribute.setOwner(visit);
        }
        visit.setAttributes(attributes);
    }
}