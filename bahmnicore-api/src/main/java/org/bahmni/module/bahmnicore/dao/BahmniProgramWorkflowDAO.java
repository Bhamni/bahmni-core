package org.bahmni.module.bahmnicore.dao;

import org.bahmni.module.bahmnicore.model.bahmniPatientProgram.BahmniPatientProgram;
import org.bahmni.module.bahmnicore.model.bahmniPatientProgram.PatientProgramAttribute;
import org.bahmni.module.bahmnicore.model.bahmniPatientProgram.PatientProgramAttributeType;
import org.openmrs.api.db.ProgramWorkflowDAO;

import java.util.List;

public interface BahmniProgramWorkflowDAO extends ProgramWorkflowDAO {

    List getAllPatientProgramAttributeTypes();

    PatientProgramAttributeType getPatientProgramAttributeType(Integer var1);

    PatientProgramAttributeType getPatientProgramAttributeTypeByUuid(String var1);

    PatientProgramAttributeType savePatientProgramAttributeType(PatientProgramAttributeType var1);

    PatientProgramAttribute getPatientProgramAttributeByUuid(String var1);

    void purgePatientProgramAttributeType(PatientProgramAttributeType var1);

    List<PatientProgramAttribute> getAttributesPatientProgramById(int uuid);

    BahmniPatientProgram saveBahmniPatientProgram(BahmniPatientProgram bahmniPatientProgram);
}
