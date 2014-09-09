package org.openmrs.module.bahmniemrapi.encountertransaction.contract;

import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.openmrs.module.bahmniemrapi.accessionnote.contract.AccessionNote;
import org.openmrs.module.bahmniemrapi.diagnosis.contract.BahmniDiagnosisRequest;
import org.openmrs.module.bahmniemrapi.document.contract.Document;
import org.openmrs.module.emrapi.encounter.domain.EncounterTransaction;
import org.openmrs.module.emrapi.utils.CustomJsonDateSerializer;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

@JsonIgnoreProperties(ignoreUnknown = true)
public class BahmniEncounterTransaction extends EncounterTransaction {
    
    private List<BahmniDiagnosisRequest> bahmniDiagnoses = new ArrayList<>();
    private List<AccessionNote> accessionNotes;
    private List<Document> documents = new ArrayList<>();

    private EncounterTransaction encounterTransaction = new EncounterTransaction();
    private String encounterType;
    private String visitType;

    public BahmniEncounterTransaction() {
    }

    public BahmniEncounterTransaction(EncounterTransaction encounterTransaction) {
        this.encounterTransaction = encounterTransaction;
    }

    public List<BahmniDiagnosisRequest> getBahmniDiagnoses() {
        return bahmniDiagnoses;
    }

    public void setBahmniDiagnoses(List<BahmniDiagnosisRequest> bahmniDiagnoses) {
        this.bahmniDiagnoses = bahmniDiagnoses;
    }

    @Override
    @JsonIgnore
    public List<Diagnosis> getDiagnoses() {
        return encounterTransaction.getDiagnoses();
    }

    @Override
    @JsonIgnore
    public void setDiagnoses(List<Diagnosis> diagnoses) {
        encounterTransaction.setDiagnoses(diagnoses);
    }

    @Override
    public String getVisitUuid() {
        return encounterTransaction.getVisitUuid();
    }

    @Override
    public void setVisitUuid(String visitUuid) {
        encounterTransaction.setVisitUuid(visitUuid);
    }

    @Override
    public String getEncounterUuid() {
        return encounterTransaction.getEncounterUuid();
    }

    @Override
    public void setEncounterUuid(String encounterUuid) {
        encounterTransaction.setEncounterUuid(encounterUuid);
    }

    @Override
    public void addObservation(Observation observation) {
        encounterTransaction.addObservation(observation);
    }

    @Override
    public void addTestOrder(TestOrder testOrder) {
        encounterTransaction.addTestOrder(testOrder);
    }

    @Override
    public void addDrugOrder(DrugOrder drugOrder) {
        encounterTransaction.addDrugOrder(drugOrder);
    }

    @Override
    public void addDiagnosis(Diagnosis diagnosis) {
        encounterTransaction.addDiagnosis(diagnosis);
    }

    @Override
    public Set<Provider> getProviders() {
        return encounterTransaction.getProviders();
    }

    @Override
    public void setProviders(Set<Provider> providers) {
        encounterTransaction.setProviders(providers);
    }

    @Override
    public Disposition getDisposition() {
        return encounterTransaction.getDisposition();
    }

    @Override
    public void setDisposition(Disposition disposition) {
        encounterTransaction.setDisposition(disposition);
    }

    @Override
    public String getPatientUuid() {
        return encounterTransaction.getPatientUuid();
    }

    @Override
    public String getEncounterTypeUuid() {
        return encounterTransaction.getEncounterTypeUuid();
    }

    @Override
    public String getVisitTypeUuid() {
        return encounterTransaction.getVisitTypeUuid();
    }

    @Override
    public EncounterTransaction setPatientUuid(String patientUuid) {
        return encounterTransaction.setPatientUuid(patientUuid);
    }

    @Override
    public EncounterTransaction setVisitTypeUuid(String visitTypeUuid) {
        return encounterTransaction.setVisitTypeUuid(visitTypeUuid);
    }

    @Override
    public EncounterTransaction setEncounterTypeUuid(String encounterTypeUuid) {
        return encounterTransaction.setEncounterTypeUuid(encounterTypeUuid);
    }

    @Override
    public EncounterTransaction setObservations(List<Observation> observations) {
        return encounterTransaction.setObservations(observations);
    }

    @Override
    public List<Observation> getObservations() {
        return encounterTransaction.getObservations();
    }

    @Override
    public List<TestOrder> getTestOrders() {
        return encounterTransaction.getTestOrders();
    }

    @Override
    public void setTestOrders(List<TestOrder> testOrders) {
        encounterTransaction.setTestOrders(testOrders);
    }

    @Override
    public List<DrugOrder> getDrugOrders() {
        return encounterTransaction.getDrugOrders();
    }

    @Override
    public void setDrugOrders(List<DrugOrder> drugOrders) {
        encounterTransaction.setDrugOrders(drugOrders);
    }

    @Override
    @JsonSerialize(using = CustomJsonDateSerializer.class)
    public Date getEncounterDateTime() {
        return encounterTransaction.getEncounterDateTime();
    }

    @Override
    public EncounterTransaction setEncounterDateTime(Date encounterDateTime) {
        return encounterTransaction.setEncounterDateTime(encounterDateTime);
    }

    @Override
    public String getLocationUuid() {
        return encounterTransaction.getLocationUuid();
    }

    @Override
    public EncounterTransaction setLocationUuid(String locationUuid) {
        return encounterTransaction.setLocationUuid(locationUuid);
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

    public List<Document> getDocuments() {
        return documents;
    }

    public void setDocuments(List<Document> documents) {
        this.documents = documents;
    }
}

