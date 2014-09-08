package org.openmrs.module.bahmniemrapi.encountertransaction.matcher;

import org.apache.commons.lang3.time.DateUtils;
import org.openmrs.Encounter;
import org.openmrs.EncounterType;
import org.openmrs.Provider;
import org.openmrs.Visit;
import org.openmrs.api.AdministrationService;
import org.openmrs.module.emrapi.encounter.EncounterParameters;
import org.openmrs.module.emrapi.encounter.matcher.BaseEncounterMatcher;

import java.util.Calendar;
import java.util.Date;


public class EncounterSessionMatcher implements BaseEncounterMatcher {

    public static final int DEFAULT_SESSION_DURATION_IN_MINUTES = 60;
    private AdministrationService adminService;

    public EncounterSessionMatcher() {
    }

    public void setAdministrationService(AdministrationService administrationService) {
        this.adminService = administrationService;
    }

    @Override
    public Encounter findEncounter(Visit visit, EncounterParameters encounterParameters) {
        EncounterType encounterType = encounterParameters.getEncounterType();
        Provider provider = null;
        if(encounterParameters.getProviders() != null && encounterParameters.getProviders().iterator().hasNext())
            provider = encounterParameters.getProviders().iterator().next();

        if (encounterType == null){
            throw new IllegalArgumentException("Encounter Type not found");
        }

        if(visit.getEncounters()!=null){
            for (Encounter encounter : visit.getEncounters()) {
                if (encounterType.equals(encounter.getEncounterType())) {
                    Date encounterDateChanged = encounter.getDateChanged() == null ? encounter.getDateCreated() : encounter.getDateChanged();
                    if(!isCurrentSessionTimeExpired(encounterDateChanged) && isSameProvider(provider, encounter))
                        if (locationNotDefined(encounterParameters, encounter) || isSameLocation(encounterParameters, encounter))
                            return encounter;
                }
            }
        }
        return null;
    }

    private boolean isSameLocation(EncounterParameters encounterParameters, Encounter encounter) {
        return ((encounter.getLocation() != null && encounter.getLocation().equals(encounterParameters.getLocation())));
    }

    private boolean locationNotDefined(EncounterParameters encounterParameters, Encounter encounter) {
        return (encounterParameters.getLocation() == null && encounter.getLocation() == null);
    }

    private boolean isSameProvider(Provider provider, Encounter encounter) {
        if(provider == null || encounter.getProvider() == null){
            return false;
        }

        return encounter.getProvider().getId().equals(provider.getPerson().getId());
    }

    private boolean isCurrentSessionTimeExpired(Date encounterCreatedDate) {
        String configuredSessionDuration = adminService.getGlobalProperty("bahmni.encountersession.duration");
        int sessionDurationInMinutes = DEFAULT_SESSION_DURATION_IN_MINUTES;
        if(configuredSessionDuration != null)
            sessionDurationInMinutes = Integer.parseInt(configuredSessionDuration);
        Date allowedEncounterTIme = DateUtils.addMinutes(encounterCreatedDate, sessionDurationInMinutes);

        return DateUtils.truncatedCompareTo(allowedEncounterTIme, new Date(), Calendar.MILLISECOND) <= 0;
    }
}
