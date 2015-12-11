package org.bahmni.module.bahmnicore.model.bahmniPatientProgram;


import org.openmrs.*;
import org.openmrs.customdatatype.Customizable;

import java.util.Date;
import java.util.List;
import java.util.Set;


public class BahmniPatientProgram extends BaseCustomizableData<BahmniPatientProgramAttribute> implements Auditable, Customizable<BahmniPatientProgramAttribute> {

    private final PatientProgram patientProgram;

    public BahmniPatientProgram() {
        this.patientProgram = new PatientProgram();
    }

    public BahmniPatientProgram(Integer patientProgramId) {
        this();
        patientProgram.setPatientProgramId(patientProgramId);
    }

    public boolean getActive(Date onDate) {
        return patientProgram.getActive(onDate);
    }

    public boolean getActive() {
        return patientProgram.getActive(null);
    }

    public PatientState getPatientState(Integer patientStateId) {
        return patientProgram.getPatientState(patientStateId);
    }

    public void transitionToState(ProgramWorkflowState programWorkflowState, Date onDate) {
        patientProgram.transitionToState(programWorkflowState, onDate);
    }

    public void voidLastState(ProgramWorkflow workflow, User voidBy, Date voidDate, String voidReason) {
        patientProgram.voidLastState(workflow, voidBy, voidDate, voidReason);
    }

    public PatientState getCurrentState(ProgramWorkflow programWorkflow) {
        return patientProgram.getCurrentState(programWorkflow);
    }

    public Set<PatientState> getCurrentStates() {
        return patientProgram.getCurrentStates();
    }

    public List<PatientState> statesInWorkflow(ProgramWorkflow programWorkflow, boolean includeVoided) {
        return patientProgram.statesInWorkflow(programWorkflow, includeVoided);
    }

    public String toString() {
        return patientProgram.toString();
    }

    public Concept getOutcome() {
        return patientProgram.getOutcome();
    }

    public void setOutcome(Concept concept) {
        patientProgram.setOutcome(concept);
    }

    public Date getDateCompleted() {
        return patientProgram.getDateCompleted();
    }

    public void setDateCompleted(Date dateCompleted) {
        patientProgram.setDateCompleted(dateCompleted);
    }

    public Date getDateEnrolled() {
        return patientProgram.getDateChanged();
    }

    public void setDateEnrolled(Date dateEnrolled) {
        patientProgram.setDateEnrolled(dateEnrolled);
    }

    public Patient getPatient() {
        return patientProgram.getPatient();
    }

    public void setPatient(Patient patient) {
        patientProgram.setPatient(patient);
    }

    public Integer getPatientProgramId() {
        return patientProgram.getPatientProgramId();
    }

    public void setPatientProgramId(Integer patientProgramId) {
        patientProgram.setPatientProgramId(patientProgramId);
    }

    public Program getProgram() {
        return patientProgram.getProgram();
    }

    public void setProgram(Program program) {
        patientProgram.setProgram(program);
    }

    public Set<PatientState> getStates() {
        return patientProgram.getStates();
    }

    public void setStates(Set<PatientState> states) {
        patientProgram.setStates(states);
    }

    public Integer getId() {
        return patientProgram.getId();
    }

    public void setId(Integer id) {
        patientProgram.setId(id);
    }

    public Location getLocation() {
        return patientProgram.getLocation();
    }

    public void setLocation(Location location) {
        patientProgram.setLocation(location);
    }
}
