package org.openmrs.module.bahmniemrapi.encountertransaction.impl;


import org.apache.commons.lang3.StringUtils;
import org.openmrs.Encounter;
import org.openmrs.EncounterType;
import org.openmrs.Visit;
import org.openmrs.api.EncounterService;
import org.openmrs.api.PatientService;
import org.openmrs.api.VisitService;
import org.openmrs.module.bahmniemrapi.encountertransaction.command.EncounterDataSaveCommand;
import org.openmrs.module.bahmniemrapi.encountertransaction.contract.BahmniEncounterTransaction;
import org.openmrs.module.bahmniemrapi.encountertransaction.mapper.BahmniEncounterTransactionMapper;
import org.openmrs.module.bahmniemrapi.encountertransaction.mapper.LocationBasedEncounterTypeIdentifier;
import org.openmrs.module.bahmniemrapi.encountertransaction.service.BahmniEncounterTransactionService;
import org.openmrs.module.bahmniemrapi.utils.DateUtils;
import org.openmrs.module.bahmniemrapi.visit.service.BahmniVisitService;
import org.openmrs.module.emrapi.encounter.EmrEncounterService;
import org.openmrs.module.emrapi.encounter.EncounterTransactionMapper;
import org.openmrs.module.emrapi.encounter.domain.EncounterTransaction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;

import static org.openmrs.module.bahmniemrapi.utils.DateUtils.isAfter;

@Transactional
public class BahmniEncounterTransactionServiceImpl implements BahmniEncounterTransactionService {

    private VisitService visitService;
    private EncounterService encounterService;
    private EmrEncounterService emrEncounterService;
    private EncounterTransactionMapper encounterTransactionMapper;
    private LocationBasedEncounterTypeIdentifier locationBasedEncounterTypeIdentifier;
    private List<EncounterDataSaveCommand> encounterDataSaveCommands;
    private BahmniEncounterTransactionMapper bahmniEncounterTransactionMapper;
    private BahmniVisitService bahmniVisitService;
    private PatientService patientService;

    @Autowired
    public BahmniEncounterTransactionServiceImpl(EncounterService encounterService, EmrEncounterService emrEncounterService, EncounterTransactionMapper encounterTransactionMapper, LocationBasedEncounterTypeIdentifier locationBasedEncounterTypeIdentifier, List<EncounterDataSaveCommand> encounterDataSaveCommands, BahmniEncounterTransactionMapper bahmniEncounterTransactionMapper, VisitService visitService, BahmniVisitService bahmniVisitService, PatientService patientService) {
        this.encounterService = encounterService;
        this.emrEncounterService = emrEncounterService;
        this.encounterTransactionMapper = encounterTransactionMapper;
        this.locationBasedEncounterTypeIdentifier = locationBasedEncounterTypeIdentifier;
        this.encounterDataSaveCommands = encounterDataSaveCommands;
        this.bahmniEncounterTransactionMapper = bahmniEncounterTransactionMapper;
        this.visitService = visitService;
        this.bahmniVisitService = bahmniVisitService;
        this.patientService = patientService;
    }

    @Override
    public BahmniEncounterTransaction save(BahmniEncounterTransaction bahmniEncounterTransaction) {
        // TODO : Mujir - map string VisitType to the uuids and set on bahmniEncounterTransaction object
        setEncounterType(bahmniEncounterTransaction);
        handleRetrospective(bahmniEncounterTransaction);
        EncounterTransaction encounterTransaction = emrEncounterService.save(bahmniEncounterTransaction.toEncounterTransaction());
        //Get the saved encounter transaction from emr-api
        String encounterUuid = encounterTransaction.getEncounterUuid();
        Encounter currentEncounter = encounterService.getEncounterByUuid(encounterUuid);

        EncounterTransaction updatedEncounterTransaction = encounterTransactionMapper.map(currentEncounter, true);
        for (EncounterDataSaveCommand saveCommand : encounterDataSaveCommands) {
            updatedEncounterTransaction = saveCommand.save(bahmniEncounterTransaction, currentEncounter, updatedEncounterTransaction);
        }
        return bahmniEncounterTransactionMapper.map(updatedEncounterTransaction);
    }

    private void handleRetrospective(BahmniEncounterTransaction bahmniEncounterTransaction) {
        if (bahmniEncounterTransaction.getEncounterDateTime() != null && isAfter(new Date(), bahmniEncounterTransaction.getEncounterDateTime())){
            List<Visit> visits = bahmniVisitService.getVisitOn(bahmniEncounterTransaction.getEncounterDateTime());
            if (visits != null && !visits.isEmpty()) {
                bahmniEncounterTransaction.setVisitUuid(visits.get(0).getUuid());
            } else {
                String visitUuid = UUID.randomUUID().toString();
                Visit visit = new Visit();
                visit.setStartDatetime(bahmniEncounterTransaction.getEncounterDateTime());
                visit.setStopDatetime(DateUtils.eod(bahmniEncounterTransaction.getEncounterDateTime()));
                visit.setPatient(patientService.getPatientByUuid(bahmniEncounterTransaction.getPatientUuid()));
                visit.setVisitType(visitService.getVisitTypeByUuid(bahmniEncounterTransaction.getVisitTypeUuid()));
                visit.setEncounters(new HashSet<Encounter>());
                visit.setUuid(visitUuid);
                visitService.saveVisit(visit);
                bahmniEncounterTransaction.setVisitUuid(visitUuid);
            }
        }
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
