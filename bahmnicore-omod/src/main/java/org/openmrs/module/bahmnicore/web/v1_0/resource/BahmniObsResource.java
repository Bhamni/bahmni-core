package org.openmrs.module.bahmnicore.web.v1_0.resource;


import org.openmrs.Encounter;
import org.openmrs.Obs;
import org.openmrs.Visit;
import org.openmrs.module.webservices.rest.web.RestConstants;
import org.openmrs.module.webservices.rest.web.annotation.PropertyGetter;
import org.openmrs.module.webservices.rest.web.representation.NamedRepresentation;
import org.openmrs.module.webservices.rest.web.representation.Representation;
import org.openmrs.module.webservices.rest.web.resource.impl.DelegatingResourceDescription;
import org.openmrs.module.webservices.rest.web.v1_0.resource.openmrs1_11.ObsResource1_11;

import java.util.Date;

@org.openmrs.module.webservices.rest.web.annotation.Resource(name = RestConstants.VERSION_1 + "/obs", supportedClass = Obs.class, supportedOpenmrsVersions = {"1.10.*", "1.11.*", "1.12.*"}, order = 0)
public class BahmniObsResource extends ObsResource1_11 {

    @Override
    public DelegatingResourceDescription getRepresentationDescription(Representation rep) {

        DelegatingResourceDescription representationDescription = super.getRepresentationDescription(rep);
        if (representationDescription == null) {
            if (rep instanceof NamedRepresentation && rep.getRepresentation().equals("visitFormDetails")) {
                DelegatingResourceDescription description = new DelegatingResourceDescription();
                description.addProperty("uuid");
                description.addProperty("concept",new NamedRepresentation("bahmniAnswer"));
                description.addProperty("display");
                description.addProperty("obsDatetime");
                description.addProperty("visitUuid");
                description.addProperty("encounterUuid");
                description.addProperty("visitStartDateTime");
                return description;
            }
        }
        return representationDescription;
    }

    @PropertyGetter("visitUuid")
    public String getvisitUuid(Obs obs) {
        Encounter encounter = obs.getEncounter();
        Visit visit = encounter.getVisit();
        String visitUuid = visit.getUuid();
        return visitUuid;
    }

    @PropertyGetter("visitStartDateTime")
    public Date getVisitDateTime(Obs obs) {
        Encounter encounter = obs.getEncounter();
        Visit visit = encounter.getVisit();
        Date visitStartDatetime = visit.getStartDatetime();
        return visitStartDatetime;
    }

    @PropertyGetter("encounterUuid")
    public String getEncounterUuid(Obs obs) {
        Encounter encounter = obs.getEncounter();
        return encounter.getUuid();
    }
}

