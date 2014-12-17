package org.openmrs.module.bahmniemrapi.encountertransaction.service;

import org.joda.time.DateTime;
import org.openmrs.Patient;
import org.openmrs.Visit;
import org.openmrs.module.bahmniemrapi.diagnosis.contract.BahmniDiagnosis;
import org.openmrs.module.bahmniemrapi.encountertransaction.contract.BahmniEncounterTransaction;
import org.openmrs.module.bahmniemrapi.encountertransaction.contract.BahmniObservation;
import org.openmrs.module.emrapi.encounter.domain.EncounterTransaction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Date;

import static org.openmrs.module.bahmniemrapi.encountertransaction.service.DateUtils.isBefore;

@Component
public class RetrospectiveEncounterTransactionService {
    protected final VisitIdentificationHelper visitIdentificationHelper;

    @Autowired
    public RetrospectiveEncounterTransactionService(VisitIdentificationHelper visitIdentificationHelper) {
        this.visitIdentificationHelper = visitIdentificationHelper;
    }

    public BahmniEncounterTransaction updatePastEncounters(BahmniEncounterTransaction bahmniEncounterTransaction, Patient patient, Date visitStartDate, Date visitEndDate) {
        if (bahmniEncounterTransaction.getEncounterDateTime() == null || !isBefore(bahmniEncounterTransaction.getEncounterDateTime(), new Date())) {
            return bahmniEncounterTransaction;
        }

        Visit matchingVisit = visitIdentificationHelper.getVisitFor(patient, bahmniEncounterTransaction.getVisitType(),
                bahmniEncounterTransaction.getEncounterDateTime(), visitStartDate, visitEndDate);
        bahmniEncounterTransaction.setVisitUuid(matchingVisit.getUuid());

        // TODO : Mujir - this should not happen here. Just set the visitType. BahmniEncounterTransaction should handle string visitTypes.
        bahmniEncounterTransaction.setVisitTypeUuid(matchingVisit.getVisitType().getUuid());

        bahmniEncounterTransaction = updateObservationDates(bahmniEncounterTransaction);
        bahmniEncounterTransaction = updateDiagnosisDates(bahmniEncounterTransaction);
        bahmniEncounterTransaction = updateDrugOrderDates(bahmniEncounterTransaction);
        bahmniEncounterTransaction = updateDisposition(bahmniEncounterTransaction);
        return bahmniEncounterTransaction;
    }

    private BahmniEncounterTransaction updateDisposition(BahmniEncounterTransaction bahmniEncounterTransaction) {
        if (bahmniEncounterTransaction.getDisposition() != null && bahmniEncounterTransaction.getDisposition().getDispositionDateTime() == null) {
            bahmniEncounterTransaction.getDisposition().setDispositionDateTime(bahmniEncounterTransaction.getEncounterDateTime());
        }
        return bahmniEncounterTransaction;
    }

    private BahmniEncounterTransaction updateDrugOrderDates(BahmniEncounterTransaction bahmniEncounterTransaction) {
        for (EncounterTransaction.DrugOrder drugOrder : bahmniEncounterTransaction.getDrugOrders()) {
            if (drugOrder.getDateActivated() == null)
                drugOrder.setDateActivated(bahmniEncounterTransaction.getEncounterDateTime());
        }
        return bahmniEncounterTransaction;
    }

    private BahmniEncounterTransaction updateDiagnosisDates(BahmniEncounterTransaction bahmniEncounterTransaction) {
        for (BahmniDiagnosis diagnosis : bahmniEncounterTransaction.getBahmniDiagnoses()) {
            if (diagnosis.getDiagnosisDateTime() == null)
                diagnosis.setDiagnosisDateTime(bahmniEncounterTransaction.getEncounterDateTime());
        }
        return bahmniEncounterTransaction;
    }

    private BahmniEncounterTransaction updateObservationDates(BahmniEncounterTransaction bahmniEncounterTransaction) {
        for (BahmniObservation observation : bahmniEncounterTransaction.getObservations()) {
            setObsDate(observation, bahmniEncounterTransaction.getEncounterDateTime());
        }
        return bahmniEncounterTransaction;
    }

    private void setObsDate(BahmniObservation observation, Date encounterDateTime) {
        if (observation.getObservationDateTime() == null)
            observation.setObservationDateTime(encounterDateTime);

        for (BahmniObservation childObservation : observation.getGroupMembers()) {
            setObsDate(childObservation, encounterDateTime);
        }
    }
}

class DateUtils {

    public static Boolean isBefore(Date date1, Date date2) {
        return new DateTime(date1).toDateMidnight().isBefore(new DateTime(date2).toDateMidnight());
    }

}
