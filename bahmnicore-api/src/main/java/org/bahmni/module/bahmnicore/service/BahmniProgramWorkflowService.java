package org.bahmni.module.bahmnicore.service;

import org.bahmni.module.bahmnicore.model.bahmniPatientProgram.BahmniPatientProgram;
import org.bahmni.module.bahmnicore.model.bahmniPatientProgram.PatientProgramAttribute;
import org.bahmni.module.bahmnicore.model.bahmniPatientProgram.PatientProgramAttributeType;
import org.openmrs.annotation.Authorized;
import org.openmrs.api.ProgramWorkflowService;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

public interface BahmniProgramWorkflowService extends ProgramWorkflowService {

    @Transactional(readOnly = true)
    @Authorized({"View PatientProgram Attribute Types"})
    List<PatientProgramAttributeType> getAllPatientProgramAttributeTypes();

    @Transactional(readOnly = true)
    @Authorized({"View PatientProgram Attribute Types"})
    PatientProgramAttributeType getPatientProgramAttributeType(Integer var1);

    @Transactional(readOnly = true)
    @Authorized({"View PatientProgram Attribute Types"})
    PatientProgramAttributeType getPatientProgramAttributeTypeByUuid(String var1);

    @Authorized({"Manage PatientProgram Attribute Types"})
    PatientProgramAttributeType savePatientProgramAttributeType(PatientProgramAttributeType var1);

    @Authorized({"Manage PatientProgram Attribute Types"})
    PatientProgramAttributeType retirePatientProgramAttributeType(PatientProgramAttributeType var1, String var2);

    @Authorized({"Manage PatientProgram Attribute Types"})
    PatientProgramAttributeType unretirePatientProgramAttributeType(PatientProgramAttributeType var1);

    @Authorized({"Purge PatientProgram Attribute Types"})
    void purgePatientProgramAttributeType(PatientProgramAttributeType var1);

    @Transactional(readOnly = true)
    @Authorized({"View PatientPrograms"})
    PatientProgramAttribute getPatientProgramAttributeByUuid(String var1);

    List<PatientProgramAttribute> getAttributesByPatientProgramId(int id);

    BahmniPatientProgram saveBahmniPatientProgram(BahmniPatientProgram bahmniPatientProgram);

}
