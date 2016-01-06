package org.bahmni.module.bahmnicore.service.impl;

import org.bahmni.module.bahmnicore.dao.BahmniProgramWorkflowDAO;
import org.bahmni.module.bahmnicore.model.bahmniPatientProgram.PatientProgramAttribute;
import org.bahmni.module.bahmnicore.model.bahmniPatientProgram.ProgramAttributeType;
import org.bahmni.module.bahmnicore.service.BahmniProgramWorkflowService;
import org.openmrs.api.impl.ProgramWorkflowServiceImpl;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Transactional
public class BahmniProgramWorkflowServiceImpl extends ProgramWorkflowServiceImpl implements BahmniProgramWorkflowService {

    @Override
    public List<ProgramAttributeType> getAllProgramAttributeTypes() {
        return ((BahmniProgramWorkflowDAO)dao).getAllProgramAttributeTypes();
    }

    @Override
    public ProgramAttributeType getProgramAttributeType(Integer id) {
        return ((BahmniProgramWorkflowDAO)dao).getProgramAttributeType(id);
    }

    @Override
    public ProgramAttributeType getProgramAttributeTypeByUuid(String uuid) {
        return ((BahmniProgramWorkflowDAO)dao).getProgramAttributeTypeByUuid(uuid);
    }

    @Override
    public ProgramAttributeType saveProgramAttributeType(ProgramAttributeType type) {
        return ((BahmniProgramWorkflowDAO)dao).saveProgramAttributeType(type);
    }

    @Override
    public void purgeProgramAttributeType(ProgramAttributeType type) {
        ((BahmniProgramWorkflowDAO)dao).purgeProgramAttributeType(type);
    }

    @Override
    public PatientProgramAttribute getPatientProgramAttributeByUuid(String uuid) {
        return ((BahmniProgramWorkflowDAO)dao).getPatientProgramAttributeByUuid(uuid);
    }

}
