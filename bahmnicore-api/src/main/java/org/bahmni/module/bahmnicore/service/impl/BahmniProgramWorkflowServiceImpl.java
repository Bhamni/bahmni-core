package org.bahmni.module.bahmnicore.service.impl;

import org.bahmni.module.bahmnicore.dao.BahmniProgramWorkflowDAO;
import org.bahmni.module.bahmnicore.model.bahmniPatientProgram.PatientProgramAttribute;
import org.bahmni.module.bahmnicore.model.bahmniPatientProgram.BahmniPatientProgram;
import org.bahmni.module.bahmnicore.model.bahmniPatientProgram.ProgramAttributeType;
import org.bahmni.module.bahmnicore.service.BahmniProgramWorkflowService;
import org.openmrs.api.impl.ProgramWorkflowServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
@Transactional
public class BahmniProgramWorkflowServiceImpl extends ProgramWorkflowServiceImpl implements BahmniProgramWorkflowService {

    BahmniProgramWorkflowDAO bahmniProgramWorkflowDao;

    @Autowired
    public BahmniProgramWorkflowServiceImpl(BahmniProgramWorkflowDAO bahmniProgramWorkflowDao) {
        this.bahmniProgramWorkflowDao = bahmniProgramWorkflowDao;
    }

    @Override
    public List<ProgramAttributeType> getAllProgramAttributeTypes() {
        return bahmniProgramWorkflowDao.getAllProgramAttributeTypes();
    }

    @Override
    public ProgramAttributeType getProgramAttributeType(Integer id) {
        return bahmniProgramWorkflowDao.getProgramAttributeType(id);
    }

    @Override
    public ProgramAttributeType getProgramAttributeTypeByUuid(String uuid) {
        return bahmniProgramWorkflowDao.getProgramAttributeTypeByUuid(uuid);
    }

    @Override
    public ProgramAttributeType saveProgramAttributeType(ProgramAttributeType type) {
        return bahmniProgramWorkflowDao.saveProgramAttributeType(type);
    }

    @Override
    public void purgeProgramAttributeType(ProgramAttributeType type) {
        bahmniProgramWorkflowDao.purgeProgramAttributeType(type);
    }

    @Override
    public PatientProgramAttribute getPatientProgramAttributeByUuid(String uuid) {
        return bahmniProgramWorkflowDao.getPatientProgramAttributeByUuid(uuid);
    }

    @Override
    public BahmniPatientProgram saveBahmniPatientProgram(BahmniPatientProgram bahmniPatientProgram) {
        return bahmniProgramWorkflowDao.saveBahmniPatientProgram(bahmniPatientProgram);
    }
}
