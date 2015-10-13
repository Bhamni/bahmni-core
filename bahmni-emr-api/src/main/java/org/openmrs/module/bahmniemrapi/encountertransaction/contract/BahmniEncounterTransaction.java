package org.openmrs.module.bahmniemrapi.encountertransaction.contract;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.openmrs.module.bahmniemrapi.accessionnote.contract.AccessionNote;
import org.openmrs.module.bahmniemrapi.diagnosis.contract.BahmniDiagnosis;
import org.openmrs.module.bahmniemrapi.diagnosis.contract.BahmniDiagnosisRequest;
import org.openmrs.module.emrapi.encounter.domain.EncounterTransaction;
import org.openmrs.module.emrapi.utils.CustomJsonDateSerializer;
import org.openmrs.module.bahmniemrapi.encountertransaction.utils.DateUtil;

import java.util.*;

@JsonIgnoreProperties(ignoreUnknown = true)
public class BahmniEncounterTransaction {

    private EncounterTransaction encounterTransaction = new EncounterTransaction();

    private List<BahmniDiagnosisRequest> bahmniDiagnoses = new ArrayList<>();
    private Collection<BahmniObservation> observations = new TreeSet<>();
    private List<AccessionNote> accessionNotes;
    private String encounterType;
    private String visitType;
    private String patientId;
    private String reason;

    public BahmniEncounterTransaction() {
        this(new EncounterTransaction());
    }

    public BahmniEncounterTransaction(EncounterTransaction encounterTransaction) {
        this.encounterTransaction = encounterTransaction;
    }

    public List<BahmniDiagnosisRequest> getBahmniDiagnoses() {
        return bahmniDiagnoses;
    }

    public void setBahmniDiagnoses(List<BahmniDiagnosisRequest> bahmniDiagnoses) {
        this.bahmniDiagnoses = bahmniDiagnoses;
        List<EncounterTransaction.Diagnosis> newDiagnoses = new ArrayList<>();
        for (BahmniDiagnosisRequest bahmniDiagnose : bahmniDiagnoses) {
            newDiagnoses.add(bahmniDiagnose);
        }
        encounterTransaction.setDiagnoses(newDiagnoses);
    }

    public String getVisitUuid() {
        return encounterTransaction.getVisitUuid();
    }

    public void setVisitUuid(String visitUuid) {
        encounterTransaction.setVisitUuid(visitUuid);
    }


    public String getEncounterUuid() {
        return encounterTransaction.getEncounterUuid();
    }


    public void setEncounterUuid(String encounterUuid) {
        encounterTransaction.setEncounterUuid(encounterUuid);
    }


    public void addObservation(BahmniObservation observation) {
        observation.setEncounterDateTime(getEncounterDateTime());
        observations.add(observation);
    }


    public void addOrder(EncounterTransaction.Order order) {
        encounterTransaction.addOrder(order);
    }


    public void addDrugOrder(EncounterTransaction.DrugOrder drugOrder) {
        encounterTransaction.addDrugOrder(drugOrder);
    }


    public void addBahmniDiagnosis(BahmniDiagnosisRequest diagnosis) {
        bahmniDiagnoses.add(diagnosis);
        encounterTransaction.addDiagnosis(diagnosis);
    }


    public Set<EncounterTransaction.Provider> getProviders() {
        return encounterTransaction.getProviders();
    }


    public void setProviders(Set<EncounterTransaction.Provider> providers) {
        encounterTransaction.setProviders(providers);
    }


    public EncounterTransaction.Disposition getDisposition() {
        return encounterTransaction.getDisposition();
    }


    public void setDisposition(EncounterTransaction.Disposition disposition) {
        encounterTransaction.setDisposition(disposition);
    }


    public String getPatientUuid() {
        return encounterTransaction.getPatientUuid();
    }


    public String getEncounterTypeUuid() {
        return encounterTransaction.getEncounterTypeUuid();
    }


    public String getVisitTypeUuid() {
        return encounterTransaction.getVisitTypeUuid();
    }


    public EncounterTransaction setPatientUuid(String patientUuid) {
        return encounterTransaction.setPatientUuid(patientUuid);
    }


    public EncounterTransaction setVisitTypeUuid(String visitTypeUuid) {
        return encounterTransaction.setVisitTypeUuid(visitTypeUuid);
    }


    public EncounterTransaction setEncounterTypeUuid(String encounterTypeUuid) {
        return encounterTransaction.setEncounterTypeUuid(encounterTypeUuid);
    }

    public Collection<BahmniObservation> getObservations() {
        return new TreeSet<>(observations);
    }

    public void setObservations(Collection<BahmniObservation> observations) {
        for (BahmniObservation observation : observations) {
            observation.setEncounterDateTime(getEncounterDateTime());
        }
        this.observations = observations;
    }


    public List<EncounterTransaction.Order> getOrders() {
        return encounterTransaction.getOrders();
    }


    public void setOrders(List<EncounterTransaction.Order> orders) {
        encounterTransaction.setOrders(orders);
    }


    public List<EncounterTransaction.DrugOrder> getDrugOrders() {
        return encounterTransaction.getDrugOrders();
    }


    public void setDrugOrders(List<EncounterTransaction.DrugOrder> drugOrders) {
        encounterTransaction.setDrugOrders(drugOrders);
    }


    @JsonSerialize(using = CustomJsonDateSerializer.class)
    public Date getEncounterDateTime() {
        return encounterTransaction.getEncounterDateTime();
    }


    public EncounterTransaction setEncounterDateTime(Date encounterDateTime) {
        return encounterTransaction.setEncounterDateTime(encounterDateTime);
    }


    public String getLocationUuid() {
        return encounterTransaction.getLocationUuid();
    }


    public EncounterTransaction setLocationUuid(String locationUuid) {
        return encounterTransaction.setLocationUuid(locationUuid);
    }

    public String getLocationName() {
        return encounterTransaction.getLocationName();
    }

    public EncounterTransaction setLocationName(String locationName) {
         return encounterTransaction.setLocationName(locationName);
    }

    public List<AccessionNote> getAccessionNotes() {
        return accessionNotes;
    }

    public void setAccessionNotes(List<AccessionNote> accessionNotes) {
        this.accessionNotes = accessionNotes;
    }

    public void setEncounterType(String encounterType) {
        this.encounterType = encounterType;
    }

    public void setVisitType(String visitType) {
        this.visitType = visitType;
    }

    public String getEncounterType() {
        return encounterType;
    }

    public String getVisitType() {
        return visitType;
    }

    public EncounterTransaction toEncounterTransaction() {
        encounterTransaction.setObservations(BahmniObservation.toETObsFromBahmniObs(this.observations));
        return encounterTransaction;
    }

    public Map<String, Object> getExtensions() {
        return encounterTransaction.getExtensions();
    }

    public EncounterTransaction setExtensions(Map<String, Object> extensions) {
        encounterTransaction.setExtensions(extensions);
        return encounterTransaction;
    }

    public String getPatientId() {
        return patientId;
    }

    public void setPatientId(String patientId) {
        this.patientId = patientId;
    }

    public BahmniEncounterTransaction updateForRetrospectiveEntry(Date encounterDate) {
        this.setEncounterDateTime(encounterDate);

        updateObservationDates(encounterDate);
        updateDiagnosisDates(encounterDate);
        updateDrugOrderDates(encounterDate);
        updateDisposition(encounterDate);
        return this;
    }

    public BahmniEncounterTransaction updateDisposition(Date encounterDate) {
        if (getDisposition() != null && getDisposition().getDispositionDateTime() == null) {
            getDisposition().setDispositionDateTime(encounterDate);
        }
        return this;
    }

    public BahmniEncounterTransaction updateDrugOrderDates(Date encounterDate) {
        for (EncounterTransaction.DrugOrder drugOrder : getDrugOrders()) {
            if (drugOrder.getDateActivated() == null)
                drugOrder.setDateActivated(encounterDate);
        }
        return this;
    }

    public BahmniEncounterTransaction updateDiagnosisDates(Date encounterDate) {
        for (BahmniDiagnosis diagnosis : getBahmniDiagnoses()) {
            if (diagnosis.getDiagnosisDateTime() == null)
                diagnosis.setDiagnosisDateTime(encounterDate);
        }
        return this;
    }

    public BahmniEncounterTransaction updateObservationDates(Date encounterDate) {
        for (BahmniObservation observation : getObservations()) {
            setObsDate(observation, encounterDate);
        }
        return this;
    }

    private void setObsDate(BahmniObservation observation, Date encounterDate) {
        if (observation.getObservationDateTime() == null)
            observation.setObservationDateTime(encounterDate);

        for (BahmniObservation childObservation : observation.getGroupMembers()) {
            setObsDate(childObservation, encounterDate);
        }
    }

    public static boolean isRetrospectiveEntry(Date encounterDateTime) {
        return encounterDateTime != null && DateUtil.isBefore(encounterDateTime, new Date());
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}

