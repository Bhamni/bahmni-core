package org.bahmni.module.bahmnicore.service;

import org.openmrs.Obs;
import org.openmrs.module.bahmniemrapi.diagnosis.contract.BahmniDiagnosisRequest;
import org.openmrs.module.emrapi.diagnosis.Diagnosis;

import java.text.*;
import java.util.Date;
import java.util.List;

public interface BahmniDiagnosisService {
    void delete(String diagnosisObservationUuid);
    List<BahmniDiagnosisRequest> getBahmniDiagnosisByPatientAndVisit(String patientUuid,String visitUuid);
    List<BahmniDiagnosisRequest> getBahmniDiagnosisByPatientAndDate(String patientUuid, Date startDate, Date endDate) throws ParseException;
}
