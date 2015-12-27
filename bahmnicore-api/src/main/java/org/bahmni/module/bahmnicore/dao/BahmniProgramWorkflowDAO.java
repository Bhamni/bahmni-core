package org.bahmni.module.bahmnicore.dao;

import org.bahmni.module.bahmnicore.model.bahmniPatientProgram.BahmniPatientProgram;
import org.bahmni.module.bahmnicore.model.bahmniPatientProgram.PatientProgramAttribute;
import org.bahmni.module.bahmnicore.model.bahmniPatientProgram.ProgramAttributeType;
import org.openmrs.api.db.ProgramWorkflowDAO;

import java.util.List;

public interface BahmniProgramWorkflowDAO extends ProgramWorkflowDAO {

    List getAllProgramAttributeTypes();

    ProgramAttributeType getProgramAttributeType(Integer var1);

    ProgramAttributeType getProgramAttributeTypeByUuid(String var1);

    ProgramAttributeType saveProgramAttributeType(ProgramAttributeType var1);

    PatientProgramAttribute getPatientProgramAttributeByUuid(String var1);

    void purgeProgramAttributeType(ProgramAttributeType var1);

    BahmniPatientProgram saveBahmniPatientProgram(BahmniPatientProgram bahmniPatientProgram);

    BahmniPatientProgram getBahmniPatientProgram(Integer id);

    BahmniPatientProgram getBahmniPatientProgramByUuid(String uuid);
}
