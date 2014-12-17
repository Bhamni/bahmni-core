package org.openmrs.module.bahmniemrapi.encountertransaction.service;

import org.apache.commons.lang.time.DateUtils;
import org.joda.time.DateTime;
import org.openmrs.Encounter;
import org.openmrs.Patient;
import org.openmrs.Visit;
import org.openmrs.VisitType;
import org.openmrs.api.VisitService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;

@Component
public class VisitIdentificationHelper {
    private VisitService visitService;

    @Autowired
    public VisitIdentificationHelper(VisitService visitService) {
        this.visitService = visitService;
    }

    public Visit getVisitFor(Patient patient, String visitTypeForNewVisit, Date orderDate, Date visitStartDate, Date visitEndDate) {
        Date nextDate = getNextDate(orderDate);
        List<Visit> visits = visitService.getVisits(null, Arrays.asList(patient), null, null, null, nextDate, orderDate, null, null, true, false);
        if (matchingVisitsFound(visits)) {
            Visit matchingVisit = getVisit(orderDate, visits);
            return stretchVisits(orderDate, matchingVisit);
        }
        return createNewVisit(patient, orderDate, visitTypeForNewVisit, visitStartDate, visitEndDate);
    }

    public Visit getVisitFor(Patient patient, String visitTypeForNewVisit, Date orderDate) {
        return getVisitFor(patient, visitTypeForNewVisit, orderDate, null, null);
    }

    private boolean matchingVisitsFound(List<Visit> visits) {
        return visits != null && !visits.isEmpty();
    }

    private Visit stretchVisits(Date orderDate, Visit matchingVisit) {
        if (matchingVisit.getStartDatetime().after(orderDate)) {
            matchingVisit.setStartDatetime(orderDate);
        }
        if (matchingVisit.getStopDatetime() != null && matchingVisit.getStopDatetime().before(orderDate)) {
            matchingVisit.setStopDatetime(orderDate);
        }
        return matchingVisit;
    }

    private Visit getVisit(Date orderDate, List<Visit> visits) {
        if (visits.size() > 1) {
            return getVisitMatchingOrderDate(orderDate, visits);
        } else {
            return visits.get(0);
        }
    }

    private Visit getVisitMatchingOrderDate(Date orderDate, List<Visit> visits) {
        for (Visit visit : visits) {
            Date visitStartDatetime = visit.getStartDatetime();
            Date visitStopDatetime = visit.getStopDatetime();
            if(visitStopDatetime!=null) {
                if ((orderDate.equals(visitStartDatetime) || visitStartDatetime.before(orderDate)) &&
                        (orderDate.equals(visitStopDatetime) || visitStopDatetime.after(orderDate)))
                    return visit;
            }
            else {
                if(orderDate.equals(visitStartDatetime) || visitStartDatetime.before(orderDate))
                    return visit;
            }
        }
        return visits.get(visits.size() - 1);
    }

    private Visit createNewVisit(Patient patient, Date date, String visitTypeForNewVisit, Date visitStartDate, Date visitEndDate) {
        VisitType visitTypeByName = getVisitTypeByName(visitTypeForNewVisit);
        if (visitTypeByName == null) {
            throw new RuntimeException("Visit type:'" + visitTypeForNewVisit + "' not found.");
        }

        Visit visit = new Visit();
        visit.setPatient(patient);
        visit.setVisitType(visitTypeByName);
        visit.setStartDatetime(visitStartDate == null ? date : visitStartDate);
        if (!DateUtils.isSameDay(date, new Date())) {
            if (visitEndDate == null)
                visit.setStopDatetime(new DateTime(date).toDateMidnight().toDateTime().plusDays(1).minusSeconds(1).toDate());
            else
                visit.setStopDatetime(new DateTime(visitEndDate).toDateMidnight().toDateTime().plusDays(1).minusSeconds(1).toDate());
        }
        visit.setEncounters(new HashSet<Encounter>());
        return visitService.saveVisit(visit);
    }

    private VisitType getVisitTypeByName(String visitTypeName) {
        List<VisitType> visitTypes = visitService.getVisitTypes(visitTypeName);
        return visitTypes.isEmpty() ? null : visitTypes.get(0);
    }

    private static Date getNextDate(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        cal.add(Calendar.DAY_OF_YEAR, 1);
        return cal.getTime();
    }
}