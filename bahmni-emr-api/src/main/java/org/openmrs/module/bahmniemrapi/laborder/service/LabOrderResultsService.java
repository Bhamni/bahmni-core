package org.openmrs.module.bahmniemrapi.laborder.service;

import org.openmrs.Patient;
import org.openmrs.Visit;
import org.openmrs.module.bahmniemrapi.laborder.contract.LabOrderResult;
import org.openmrs.module.bahmniemrapi.laborder.contract.LabOrderResults;

import java.util.Collection;
import java.util.Date;
import java.util.List;

public interface LabOrderResultsService {
    LabOrderResults getAll(Patient patient, List<Visit> visits, int numberOfAccessions);

    List<LabOrderResult> getAllForConcepts(Patient patient, Collection<String> concepts, List<Visit> visits, Date startDate, Date endDate);
}
