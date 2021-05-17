package org.openmrs.module.bahmniemrapi.visitlocation;


import org.openmrs.Location;
import org.openmrs.Visit;
import org.openmrs.api.LocationService;
import org.openmrs.module.emrapi.EmrApiConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
@Transactional
public class BahmniVisitLocationServiceImpl implements BahmniVisitLocationService {

    private LocationService locationService;

    @Autowired
    public BahmniVisitLocationServiceImpl(LocationService locationService) {
        this.locationService = locationService;
    }


    @Override
    public String getVisitLocationUuid(String loginLocationUuid) {
        Location location = getVisitLocation(loginLocationUuid);
        if (location != null) {
            return location.getUuid();
        }
        return null;
    }

    @Override
    public Location getVisitLocation(String loginLocationUuid) {
        return visitLocationFor(getLocationByUuid(loginLocationUuid));
    }

    private Location getLocationByUuid(String loginLocationUuid) {
        Location location = locationService.getLocationByUuid(loginLocationUuid);
        if (location == null) {
            throw new IllegalArgumentException("Location Uuid "+loginLocationUuid+" not found");
        }

        return location;
    }

    private Location visitLocationFor(Location location) {
        if (location == null) {
            throw new VisitLocationNotFoundException("No Location tagged to Visit Location Found");
        }

        return supportsVisits(location) ? location : visitLocationFor(location.getParentLocation());
    }

    private Boolean supportsVisits(Location location) {
        return location.hasTag(EmrApiConstants.LOCATION_TAG_SUPPORTS_VISITS);
    }

    @Override
    public Visit getMatchingVisitInLocation(List<Visit> visits, String locationUuid) {
        Location visitLocation;
        try {
            visitLocation = getVisitLocation(locationUuid);
        } catch (VisitLocationNotFoundException visitLocationNotFound) {
            return visits.get(0);//sensible default assuming there could be visits having location
                                    // that are not associated with visit locations
        }
        for (Visit visit : visits) {
            if (visit.getLocation().equals(visitLocation)) {
                return visit;
            }
        }
        return null;
    }

}
