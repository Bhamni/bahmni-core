package org.bahmni.module.bahmnicore.service.impl;

import org.bahmni.module.bahmnicore.dao.BahmniProgramWorkflowDAO;
import org.bahmni.module.bahmnicore.model.bahmniPatientProgram.BahmniPatientProgram;
import org.bahmni.module.bahmnicore.model.bahmniPatientProgram.PatientProgramAttribute;
import org.bahmni.module.bahmnicore.model.bahmniPatientProgram.PatientProgramAttributeType;
import org.bahmni.module.bahmnicore.service.BahmniProgramWorkflowService;
import org.openmrs.api.impl.ProgramWorkflowServiceImpl;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
@Transactional
public class BahmniProgramWorkflowServiceImpl extends ProgramWorkflowServiceImpl implements BahmniProgramWorkflowService {

    protected BahmniProgramWorkflowDAO bahmniProgramWorkflowDao;

    @Override
    public List<PatientProgramAttributeType> getAllPatientProgramAttributeTypes() {
        return bahmniProgramWorkflowDao.getAllPatientProgramAttributeTypes();
    }

    @Override
    public PatientProgramAttributeType getPatientProgramAttributeType(Integer var1) {
        return bahmniProgramWorkflowDao.getPatientProgramAttributeType(var1);
    }

    @Override
    public PatientProgramAttributeType getPatientProgramAttributeTypeByUuid(String var1) {
        return bahmniProgramWorkflowDao.getPatientProgramAttributeTypeByUuid(var1);
    }

    @Override
    public PatientProgramAttributeType savePatientProgramAttributeType(PatientProgramAttributeType var1) {
        return bahmniProgramWorkflowDao.savePatientProgramAttributeType(var1);
    }

    @Override
    public void purgePatientProgramAttributeType(PatientProgramAttributeType var1) {
        bahmniProgramWorkflowDao.purgePatientProgramAttributeType(var1);
    }

    @Override
    public PatientProgramAttribute getPatientProgramAttributeByUuid(String var1) {
        return bahmniProgramWorkflowDao.getPatientProgramAttributeByUuid(var1);
    }

    @Override
    public List<PatientProgramAttribute> getAttributesByPatientProgramId(int id) {
        return bahmniProgramWorkflowDao.getAttributesPatientProgramById(id);
    }

    @Override
    public BahmniPatientProgram saveBahmniPatientProgram(BahmniPatientProgram bahmniPatientProgram) {
        return bahmniProgramWorkflowDao.saveBahmniPatientProgram(bahmniPatientProgram);
    }
}
