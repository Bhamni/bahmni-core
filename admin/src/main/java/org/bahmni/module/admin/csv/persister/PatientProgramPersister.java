package org.bahmni.module.admin.csv.persister;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.bahmni.csv.EntityPersister;
import org.bahmni.csv.Messages;
import org.bahmni.csv.RowResult;
import org.bahmni.module.admin.csv.models.PatientProgramRow;
import org.bahmni.module.admin.csv.service.PatientMatchService;
import org.openmrs.ConceptName;
import org.openmrs.Patient;
import org.openmrs.PatientProgram;
import org.openmrs.Program;
import org.openmrs.api.ProgramWorkflowService;
import org.openmrs.api.context.Context;
import org.openmrs.api.context.UserContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class PatientProgramPersister implements EntityPersister<PatientProgramRow> {
    @Autowired
    private PatientMatchService patientMatchService;
    @Autowired
    private ProgramWorkflowService programWorkflowService;

    private UserContext userContext;

    private static final Logger log = Logger.getLogger(PatientProgramPersister.class);
    private String patientMatchingAlgorithmClassName;

    public void init(UserContext userContext, String patientMatchingAlgorithmClassName) {
        this.userContext = userContext;
        this.patientMatchingAlgorithmClassName = patientMatchingAlgorithmClassName;
    }

    @Override
    public Messages validate(PatientProgramRow patientProgramRow) {
        return new Messages();
    }

    @Override
    public Messages persist(PatientProgramRow patientProgramRow) {
        // This validation is needed as patientservice get returns all patients for empty patient identifier
        if (StringUtils.isEmpty(patientProgramRow.patientIdentifier)) {
            return noMatchingPatients(patientProgramRow);
        }

        try {
            Context.openSession();
            Context.setUserContext(userContext);

            boolean shouldMatchExactPatientId = false; //Mujir - defaulting to false for now. Not sure if we have program data for migration that does not have exact patient identifiers.

            Patient patient = patientMatchService.getPatient(patientMatchingAlgorithmClassName, patientProgramRow.patientAttributes, patientProgramRow.patientIdentifier, shouldMatchExactPatientId);
            if (patient == null) {
                return noMatchingPatients(patientProgramRow);
            }

            Program program = getProgramByName(patientProgramRow.programName);
            List<PatientProgram> existingEnrolledPrograms = programWorkflowService.getPatientPrograms(patient, program, null, null, null, null, false);
            if (existingEnrolledPrograms != null && !existingEnrolledPrograms.isEmpty()) {
                return new Messages(getErrorMessage(existingEnrolledPrograms));
            }

            PatientProgram patientProgram = new PatientProgram();
            patientProgram.setPatient(patient);
            patientProgram.setProgram(program);
            patientProgram.setDateEnrolled(patientProgramRow.getEnrollmentDate());

            programWorkflowService.savePatientProgram(patientProgram);

        } catch (Exception e) {
            log.error(e.getMessage(), e);
            Context.clearSession();
            return new Messages(e);
        } finally {
            Context.flushSession();
            Context.closeSession();
        }
        return new Messages();
    }

    private String getErrorMessage(List<PatientProgram> patientPrograms) {
        PatientProgram existingProgram = patientPrograms.get(0);
        String errorMessage = "Patient already enrolled in " + existingProgram.getProgram().getName() + " from " + existingProgram.getDateEnrolled();
        errorMessage += existingProgram.getDateCompleted() == null ? "" : " to " + existingProgram.getDateCompleted();
        return errorMessage;
    }

    private Messages noMatchingPatients(PatientProgramRow patientProgramRow) {
        return new Messages("No matching patients found with ID:'" + patientProgramRow.patientIdentifier + "'");
    }

    private Program getProgramByName(String programName) {
        for (Program program : programWorkflowService.getAllPrograms()) {
            if (isNamed(program, programName)) {
                return program;
            }
        }
        throw new RuntimeException("No matching Program found with name: " + programName);
    }

    private boolean isNamed(Program program, String programName) {
        for (ConceptName conceptName : program.getConcept().getNames()) {
            if (programName.equalsIgnoreCase(conceptName.getName())) {
                return true;
            }
        }
        return false;
    }
}