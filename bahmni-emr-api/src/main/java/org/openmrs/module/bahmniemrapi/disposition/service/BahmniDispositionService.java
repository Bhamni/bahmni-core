package org.openmrs.module.bahmniemrapi.disposition.service;

import org.openmrs.Visit;
import org.openmrs.module.bahmniemrapi.disposition.contract.BahmniDisposition;

import java.util.List;
import java.util.Locale;

public interface BahmniDispositionService {
    List<BahmniDisposition> getDispositionByVisitUuid(String visitUuid , Locale locale);
   // List<BahmniDisposition> getDispositionByVisits(List<Visit> visits);
    List<BahmniDisposition> getDispositionByVisits(List<Visit> visits , Locale locale);

}
