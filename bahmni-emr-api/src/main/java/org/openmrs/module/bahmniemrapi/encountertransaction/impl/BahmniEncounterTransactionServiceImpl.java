package org.openmrs.module.bahmniemrapi.encountertransaction.impl;


import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.openmrs.*;
import org.openmrs.api.*;
import org.openmrs.module.bahmniemrapi.encountertransaction.command.EncounterDataPostSaveCommand;
import org.openmrs.module.bahmniemrapi.encountertransaction.command.EncounterDataPreSaveCommand;
import org.openmrs.module.bahmniemrapi.encountertransaction.contract.BahmniEncounterTransaction;
import org.openmrs.module.bahmniemrapi.encountertransaction.mapper.BahmniEncounterTransactionMapper;
import org.openmrs.module.bahmniemrapi.encountertransaction.mapper.EncounterTypeIdentifier;
import org.openmrs.module.bahmniemrapi.encountertransaction.matcher.EncounterSessionMatcher;
import org.openmrs.module.bahmniemrapi.encountertransaction.service.BahmniEncounterTransactionService;
import org.openmrs.module.bahmniemrapi.encountertransaction.service.RetrospectiveEncounterTransactionService;
import org.openmrs.module.bahmniemrapi.encountertransaction.service.VisitIdentificationHelper;
import org.openmrs.module.emrapi.encounter.*;
import org.openmrs.module.emrapi.encounter.domain.EncounterTransaction;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Transactional
public class BahmniEncounterTransactionServiceImpl implements BahmniEncounterTransactionService {
    private EncounterService encounterService;
    private EmrEncounterService emrEncounterService;
    private EncounterTransactionMapper encounterTransactionMapper;
    private EncounterTypeIdentifier encounterTypeIdentifier;
    private List<EncounterDataPreSaveCommand> encounterDataPreSaveCommand;
    private List<EncounterDataPostSaveCommand> encounterDataPostSaveCommands;
    private List<EncounterDataPostSaveCommand> encounterDataPostDeleteCommands;
    private BahmniEncounterTransactionMapper bahmniEncounterTransactionMapper;
    private VisitService visitService;
    private PatientService patientService;
    private LocationService locationService;
    private ProviderService providerService;
    private AdministrationService administrationService;
    private EncounterSessionMatcher encounterSessionMatcher;

    public BahmniEncounterTransactionServiceImpl(EncounterService encounterService, EmrEncounterService emrEncounterService, EncounterTransactionMapper encounterTransactionMapper,
                                                 EncounterTypeIdentifier encounterTypeIdentifier, List<EncounterDataPreSaveCommand> encounterDataPreSaveCommand, List<EncounterDataPostSaveCommand> encounterDataPostSaveCommands,
                                                 List<EncounterDataPostSaveCommand> encounterDataPostDeleteCommands,
                                                 BahmniEncounterTransactionMapper bahmniEncounterTransactionMapper, VisitService visitService, PatientService patientService, LocationService locationService, ProviderService providerService,
                                                 @Qualifier("adminService") AdministrationService administrationService, EncounterSessionMatcher encounterSessionMatcher) {

        this.encounterService = encounterService;
        this.emrEncounterService = emrEncounterService;
        this.encounterTransactionMapper = encounterTransactionMapper;
        this.encounterTypeIdentifier = encounterTypeIdentifier;
        this.encounterDataPreSaveCommand = encounterDataPreSaveCommand;
        this.encounterDataPostSaveCommands = encounterDataPostSaveCommands;
        this.encounterDataPostDeleteCommands = encounterDataPostDeleteCommands;
        this.bahmniEncounterTransactionMapper = bahmniEncounterTransactionMapper;
        this.visitService = visitService;
        this.patientService = patientService;
        this.locationService = locationService;
        this.providerService = providerService;
        this.administrationService = administrationService;
        this.encounterSessionMatcher = encounterSessionMatcher;
    }

    @Override
    public BahmniEncounterTransaction save(BahmniEncounterTransaction bahmniEncounterTransaction, Patient patient, Date visitStartDate, Date visitEndDate) {
        // TODO : Mujir - map string VisitType to the uuids and set on bahmniEncounterTransaction object
        if(!StringUtils.isBlank(bahmniEncounterTransaction.getEncounterUuid())){
            Encounter encounterByUuid = encounterService.getEncounterByUuid(bahmniEncounterTransaction.getEncounterUuid());
            if(encounterByUuid != null){
                bahmniEncounterTransaction.setEncounterTypeUuid(encounterByUuid.getEncounterType().getUuid());
            }
        }

        if (StringUtils.isBlank(bahmniEncounterTransaction.getEncounterTypeUuid())) {
            setEncounterType(bahmniEncounterTransaction);
        }

        if(bahmniEncounterTransaction.getEncounterDateTime() == null){
            bahmniEncounterTransaction.setEncounterDateTime(new Date());
        }
        for (EncounterDataPreSaveCommand saveCommand : encounterDataPreSaveCommand) {
            saveCommand.update(bahmniEncounterTransaction);
        }
        VisitIdentificationHelper visitIdentificationHelper = new VisitIdentificationHelper(visitService);
        bahmniEncounterTransaction = new RetrospectiveEncounterTransactionService(visitIdentificationHelper).updatePastEncounters(bahmniEncounterTransaction, patient, visitStartDate, visitEndDate);
        if (!StringUtils.isBlank(bahmniEncounterTransaction.getVisitType())) {
            setVisitTypeUuid(visitIdentificationHelper, bahmniEncounterTransaction);
        }

        EncounterTransaction encounterTransaction = emrEncounterService.save(bahmniEncounterTransaction.toEncounterTransaction());
        //Get the saved encounter transaction from emr-api
        String encounterUuid = encounterTransaction.getEncounterUuid();
        Encounter currentEncounter = encounterService.getEncounterByUuid(encounterUuid);

        boolean includeAll = false;
        EncounterTransaction updatedEncounterTransaction = encounterTransactionMapper.map(currentEncounter, includeAll);
        for (EncounterDataPostSaveCommand saveCommand : encounterDataPostSaveCommands) {
            updatedEncounterTransaction = saveCommand.save(bahmniEncounterTransaction,currentEncounter, updatedEncounterTransaction);
        }
        return bahmniEncounterTransactionMapper.map(updatedEncounterTransaction, includeAll);
    }

    private void setVisitTypeUuid(VisitIdentificationHelper visitIdentificationHelper, BahmniEncounterTransaction bahmniEncounterTransaction) {
        VisitType visitType = visitIdentificationHelper.getVisitTypeByName(bahmniEncounterTransaction.getVisitType());
        if (visitType != null) {
            bahmniEncounterTransaction.setVisitTypeUuid(visitType.getUuid());
        }
    }

    @Override
    public BahmniEncounterTransaction save(BahmniEncounterTransaction bahmniEncounterTransaction) {
        Patient patientByUuid = patientService.getPatientByUuid(bahmniEncounterTransaction.getPatientUuid());
        return save(bahmniEncounterTransaction, patientByUuid, null, null);
    }

    @Override
    public EncounterTransaction find(EncounterSearchParameters encounterSearchParameters) {
        EncounterSearchParametersBuilder searchParameters = new EncounterSearchParametersBuilder(encounterSearchParameters,
                this.patientService, this.encounterService, this.locationService, this.providerService, this.visitService);

        Visit visit = null;
        if(! BahmniEncounterTransaction.isRetrospectiveEntry(searchParameters.getEndDate())){
            List<Visit> visits = this.visitService.getActiveVisitsByPatient(searchParameters.getPatient());
            if(CollectionUtils.isNotEmpty(visits)){
                visit = visits.get(0);
            }
        }

        Encounter encounter = encounterSessionMatcher.findEncounter(visit, mapEncounterParameters(searchParameters));
        if(encounter != null){
            return encounterTransactionMapper.map(encounter, encounterSearchParameters.getIncludeAll());
        }
        return null;
    }

    private EncounterParameters mapEncounterParameters(EncounterSearchParametersBuilder encounterSearchParameters) {
        EncounterParameters encounterParameters = EncounterParameters.instance();
        encounterParameters.setPatient(encounterSearchParameters.getPatient());
        if(encounterSearchParameters.getEncounterTypes().size() > 0){
            encounterParameters.setEncounterType(encounterSearchParameters.getEncounterTypes().iterator().next());
        }
        encounterParameters.setProviders(new HashSet<Provider>(encounterSearchParameters.getProviders()));
        encounterParameters.setEncounterDateTime(encounterSearchParameters.getEndDate());
        return encounterParameters;
    }

    @Override
    public void delete(BahmniEncounterTransaction bahmniEncounterTransaction) {
        Encounter encounter = encounterService.getEncounterByUuid(bahmniEncounterTransaction.getEncounterUuid());
        encounterService.voidEncounter(encounter, bahmniEncounterTransaction.getReason());
        for (EncounterDataPostSaveCommand saveCommand : encounterDataPostDeleteCommands) {
            saveCommand.save(bahmniEncounterTransaction,encounter, null);
        }
    }

    private void setEncounterType(BahmniEncounterTransaction bahmniEncounterTransaction) {
        EncounterType encounterType = encounterTypeIdentifier.getEncounterTypeFor(bahmniEncounterTransaction.getEncounterType(), bahmniEncounterTransaction.getLocationUuid());
        if (encounterType == null) {
            throw new RuntimeException("Encounter type not found.");
        }
        bahmniEncounterTransaction.setEncounterTypeUuid(encounterType.getUuid());
    }

}
