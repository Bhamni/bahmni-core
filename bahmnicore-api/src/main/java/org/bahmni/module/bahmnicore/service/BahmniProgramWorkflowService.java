package org.bahmni.module.bahmnicore.service;

import org.bahmni.module.bahmnicore.model.bahmniPatientProgram.BahmniPatientProgram;
import org.bahmni.module.bahmnicore.model.bahmniPatientProgram.PatientProgramAttribute;
import org.bahmni.module.bahmnicore.model.bahmniPatientProgram.ProgramAttributeType;
import org.openmrs.annotation.Authorized;
import org.openmrs.api.ProgramWorkflowService;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface BahmniProgramWorkflowService extends ProgramWorkflowService {

    @Transactional(readOnly = true)
    @Authorized({"View PatientProgram Attribute Types"})
    List<ProgramAttributeType> getAllProgramAttributeTypes();

    @Transactional(readOnly = true)
    @Authorized({"View PatientProgram Attribute Types"})
    ProgramAttributeType getProgramAttributeType(Integer var1);

    @Transactional(readOnly = true)
    @Authorized({"View PatientProgram Attribute Types"})
    ProgramAttributeType getProgramAttributeTypeByUuid(String var1);

    @Authorized({"Manage PatientProgram Attribute Types"})
    ProgramAttributeType saveProgramAttributeType(ProgramAttributeType var1);

    @Authorized({"Purge PatientProgram Attribute Types"})
    void purgeProgramAttributeType(ProgramAttributeType var1);

    @Transactional(readOnly = true)
    @Authorized({"View PatientPrograms"})
    PatientProgramAttribute getPatientProgramAttributeByUuid(String var1);

    BahmniPatientProgram saveBahmniPatientProgram(BahmniPatientProgram bahmniPatientProgram);

    BahmniPatientProgram getBahmniPatientProgramByUuid(String uuid);
}
