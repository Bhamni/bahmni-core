package org.openmrs.module.bahmniemrapi.encountertransaction.impl;


import org.apache.commons.lang3.StringUtils;
import org.openmrs.Encounter;
import org.openmrs.EncounterType;
import org.openmrs.api.EncounterService;
import org.openmrs.module.bahmniemrapi.encountertransaction.command.EncounterDataPreSaveCommand;
import org.openmrs.module.bahmniemrapi.encountertransaction.command.EncounterDataPostSaveCommand;
import org.openmrs.module.bahmniemrapi.encountertransaction.contract.BahmniEncounterTransaction;
import org.openmrs.module.bahmniemrapi.encountertransaction.mapper.BahmniEncounterTransactionMapper;
import org.openmrs.module.bahmniemrapi.encountertransaction.mapper.LocationBasedEncounterTypeIdentifier;
import org.openmrs.module.bahmniemrapi.encountertransaction.service.BahmniEncounterTransactionService;
import org.openmrs.module.emrapi.encounter.EmrEncounterService;
import org.openmrs.module.emrapi.encounter.EncounterTransactionMapper;
import org.openmrs.module.emrapi.encounter.domain.EncounterTransaction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Transactional
public class BahmniEncounterTransactionServiceImpl implements BahmniEncounterTransactionService {

    private EncounterService encounterService;
    private EmrEncounterService emrEncounterService;
    private EncounterTransactionMapper encounterTransactionMapper;
    private LocationBasedEncounterTypeIdentifier locationBasedEncounterTypeIdentifier;
    private EncounterDataPreSaveCommand encounterDataPreSaveCommand;
    private List<EncounterDataPostSaveCommand> encounterDataPostSaveCommands;
    private BahmniEncounterTransactionMapper bahmniEncounterTransactionMapper;

    @Autowired
    public BahmniEncounterTransactionServiceImpl(EncounterService encounterService, EmrEncounterService emrEncounterService, EncounterTransactionMapper encounterTransactionMapper, LocationBasedEncounterTypeIdentifier locationBasedEncounterTypeIdentifier, EncounterDataPreSaveCommand encounterDataPreSaveCommand, List<EncounterDataPostSaveCommand> encounterDataPostSaveCommands, BahmniEncounterTransactionMapper bahmniEncounterTransactionMapper) {
        this.encounterService = encounterService;
        this.emrEncounterService = emrEncounterService;
        this.encounterTransactionMapper = encounterTransactionMapper;
        this.locationBasedEncounterTypeIdentifier = locationBasedEncounterTypeIdentifier;
        this.encounterDataPreSaveCommand = encounterDataPreSaveCommand;
        this.encounterDataPostSaveCommands = encounterDataPostSaveCommands;
        this.bahmniEncounterTransactionMapper = bahmniEncounterTransactionMapper;
    }

    @Override
    public BahmniEncounterTransaction save(BahmniEncounterTransaction bahmniEncounterTransaction) {
        // TODO : Mujir - map string VisitType to the uuids and set on bahmniEncounterTransaction object
        setEncounterType(bahmniEncounterTransaction);
        encounterDataPreSaveCommand.update(bahmniEncounterTransaction);
        EncounterTransaction encounterTransaction = emrEncounterService.save(bahmniEncounterTransaction.toEncounterTransaction());
        //Get the saved encounter transaction from emr-api
        String encounterUuid = encounterTransaction.getEncounterUuid();
        Encounter currentEncounter = encounterService.getEncounterByUuid(encounterUuid);

        EncounterTransaction updatedEncounterTransaction=encounterTransactionMapper.map(currentEncounter, true);
        for (EncounterDataPostSaveCommand saveCommand : encounterDataPostSaveCommands) {
            updatedEncounterTransaction = saveCommand.save(bahmniEncounterTransaction,currentEncounter, updatedEncounterTransaction);
        }
        return bahmniEncounterTransactionMapper.map(updatedEncounterTransaction);
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
