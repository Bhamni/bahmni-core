package org.openmrs.module.bahmniemrapi.encountertransaction.impl;


import org.apache.commons.lang3.StringUtils;
import org.openmrs.Encounter;
import org.openmrs.EncounterType;
import org.openmrs.Patient;
import org.openmrs.api.EncounterService;
import org.openmrs.api.PatientService;
import org.openmrs.api.VisitService;
import org.openmrs.module.bahmniemrapi.encountertransaction.command.EncounterDataSaveCommand;
import org.openmrs.module.bahmniemrapi.encountertransaction.contract.BahmniEncounterTransaction;
import org.openmrs.module.bahmniemrapi.encountertransaction.mapper.BahmniEncounterTransactionMapper;
import org.openmrs.module.bahmniemrapi.encountertransaction.mapper.LocationBasedEncounterTypeIdentifier;
import org.openmrs.module.bahmniemrapi.encountertransaction.service.BahmniEncounterTransactionService;
import org.openmrs.module.bahmniemrapi.encountertransaction.service.RetrospectiveEncounterTransactionService;
import org.openmrs.module.bahmniemrapi.encountertransaction.service.VisitIdentificationHelper;
import org.openmrs.module.emrapi.encounter.EmrEncounterService;
import org.openmrs.module.emrapi.encounter.EncounterTransactionMapper;
import org.openmrs.module.emrapi.encounter.domain.EncounterTransaction;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

@Transactional
public class BahmniEncounterTransactionServiceImpl implements BahmniEncounterTransactionService {
    private EncounterService encounterService;
    private EmrEncounterService emrEncounterService;
    private EncounterTransactionMapper encounterTransactionMapper;
    private LocationBasedEncounterTypeIdentifier locationBasedEncounterTypeIdentifier;
    private List<EncounterDataSaveCommand> encounterDataSaveCommands;
    private BahmniEncounterTransactionMapper bahmniEncounterTransactionMapper;
    private VisitService visitService;
    private PatientService patientService;

    public BahmniEncounterTransactionServiceImpl(EncounterService encounterService, EmrEncounterService emrEncounterService, EncounterTransactionMapper encounterTransactionMapper,
                                                 LocationBasedEncounterTypeIdentifier locationBasedEncounterTypeIdentifier, List<EncounterDataSaveCommand> encounterDataSaveCommands,
                                                 BahmniEncounterTransactionMapper bahmniEncounterTransactionMapper, VisitService visitService, PatientService patientService) {
        this.encounterService = encounterService;
        this.emrEncounterService = emrEncounterService;
        this.encounterTransactionMapper = encounterTransactionMapper;
        this.locationBasedEncounterTypeIdentifier = locationBasedEncounterTypeIdentifier;
        this.encounterDataSaveCommands = encounterDataSaveCommands;
        this.bahmniEncounterTransactionMapper = bahmniEncounterTransactionMapper;
        this.visitService = visitService;
        this.patientService = patientService;
    }

    @Override
    public BahmniEncounterTransaction save(BahmniEncounterTransaction bahmniEncounterTransaction, Patient patient, Date visitStartDate, Date visitEndDate) {
        // TODO : Mujir - map string VisitType to the uuids and set on bahmniEncounterTransaction object
        setEncounterType(bahmniEncounterTransaction);

        VisitIdentificationHelper visitIdentificationHelper = new VisitIdentificationHelper(visitService);
        bahmniEncounterTransaction = new RetrospectiveEncounterTransactionService(visitIdentificationHelper).updatePastEncounters(bahmniEncounterTransaction, patient, visitStartDate, visitEndDate);

        EncounterTransaction encounterTransaction = emrEncounterService.save(bahmniEncounterTransaction.toEncounterTransaction());
        //Get the saved encounter transaction from emr-api
        String encounterUuid = encounterTransaction.getEncounterUuid();
        Encounter currentEncounter = encounterService.getEncounterByUuid(encounterUuid);

        EncounterTransaction updatedEncounterTransaction = encounterTransactionMapper.map(currentEncounter, true);
        for (EncounterDataSaveCommand saveCommand : encounterDataSaveCommands) {
            updatedEncounterTransaction = saveCommand.save(bahmniEncounterTransaction,currentEncounter, updatedEncounterTransaction);
        }
        return bahmniEncounterTransactionMapper.map(updatedEncounterTransaction);
    }

    @Override
    public BahmniEncounterTransaction save(BahmniEncounterTransaction bahmniEncounterTransaction) {
        Patient patientByUuid = patientService.getPatientByUuid(bahmniEncounterTransaction.getPatientUuid());
        return save(bahmniEncounterTransaction, patientByUuid, null, null);
    }

    private void setEncounterType(BahmniEncounterTransaction bahmniEncounterTransaction) {
        String encounterTypeString = bahmniEncounterTransaction.getEncounterType();
        locationBasedEncounterTypeIdentifier.populateEncounterType(bahmniEncounterTransaction);
        if (bahmniEncounterTransaction.getEncounterTypeUuid() == null && StringUtils.isNotEmpty(encounterTypeString)) {
            EncounterType encounterType = encounterService.getEncounterType(encounterTypeString);
            if (encounterType == null) {
                throw new RuntimeException("Encounter type:'" + encounterTypeString + "' not found.");
            }
            bahmniEncounterTransaction.setEncounterTypeUuid(encounterType.getUuid());
        }
    }


}
