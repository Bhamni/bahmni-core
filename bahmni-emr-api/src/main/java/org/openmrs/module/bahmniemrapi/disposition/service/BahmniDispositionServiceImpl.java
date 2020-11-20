package org.openmrs.module.bahmniemrapi.disposition.service;

import org.openmrs.Encounter;
import org.openmrs.Obs;
import org.openmrs.Visit;
import org.openmrs.api.VisitService;
import org.openmrs.module.bahmniemrapi.disposition.contract.BahmniDisposition;
import org.openmrs.module.bahmniemrapi.disposition.mapper.BahmniDispositionMapper;
import org.openmrs.module.emrapi.encounter.DispositionMapper;
import org.openmrs.module.emrapi.encounter.EncounterProviderMapper;
import org.openmrs.module.emrapi.encounter.domain.EncounterTransaction;
import org.openmrs.module.emrapi.encounter.matcher.ObservationTypeMatcher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;

@Service
public class BahmniDispositionServiceImpl implements BahmniDispositionService {

    private VisitService visitService;

    private DispositionMapper dispositionMapper;

    private ObservationTypeMatcher observationTypeMatcher;

    private EncounterProviderMapper encounterProviderMapper;

    private BahmniDispositionMapper bahmniDispositionMapper;

    @Autowired
    public BahmniDispositionServiceImpl(VisitService visitService, DispositionMapper dispositionMapper,
                                        ObservationTypeMatcher observationTypeMatcher, EncounterProviderMapper encounterProviderMapper,
                                        BahmniDispositionMapper bahmniDispositionMapper){
        this.visitService = visitService;
        this.dispositionMapper = dispositionMapper;
        this.observationTypeMatcher = observationTypeMatcher;
        this.encounterProviderMapper = encounterProviderMapper;
        this.bahmniDispositionMapper = bahmniDispositionMapper;
    }

    @Override
    public List<BahmniDisposition> getDispositionByVisitUuid(String visitUuid , Locale locale) {
        Assert.notNull(visitUuid);

        Visit visit = visitService.getVisitByUuid(visitUuid);

        if(visit == null){
            return new ArrayList<>();
        }

        return getDispositionByVisit(visit, locale);
    }

    public List<BahmniDisposition> getDispositionByVisits(List<Visit> visits , Locale locale){
        List<BahmniDisposition> dispositions = new ArrayList<>();

        for(Visit visit: visits){
            dispositions.addAll(getDispositionByVisit(visit , locale));
        }

        return  dispositions;
    }

    private List<BahmniDisposition> getDispositionByVisit(Visit visit , Locale locale) {
        List<BahmniDisposition> dispositions = new ArrayList<>();
        for (Encounter encounter : visit.getEncounters()) {
            Set<Obs> observations = encounter.getObsAtTopLevel(false);
            Set<EncounterTransaction.Provider> eTProvider = encounterProviderMapper.convert(encounter.getEncounterProviders());

            for (Obs observation : observations) {
                if(ObservationTypeMatcher.ObservationType.DISPOSITION.equals(observationTypeMatcher.getObservationType(observation))){
                    addBahmniDisposition(dispositions, eTProvider, observation , locale);
                }
            }
        }
        return dispositions;
    }

    private void addBahmniDisposition(List<BahmniDisposition> dispositions, Set<EncounterTransaction.Provider> eTProvider, Obs observation , Locale locale) {
        EncounterTransaction.Disposition eTDisposition = dispositionMapper.getDisposition(observation);
        if(eTDisposition!=null){
            dispositions.add(bahmniDispositionMapper.map(eTDisposition, eTProvider, observation.getCreator(),locale));
        }
    }
}
