package org.openmrs.module.bahmniemrapi.document.service.impl;

import org.openmrs.*;
import org.openmrs.api.AdministrationService;
import org.openmrs.api.ConceptService;
import org.openmrs.api.EncounterService;
import org.openmrs.api.VisitService;
import org.openmrs.api.context.Context;
import org.openmrs.module.bahmniemrapi.document.contract.Document;
import org.openmrs.module.bahmniemrapi.document.contract.VisitDocumentRequest;
import org.openmrs.module.bahmniemrapi.document.service.VisitDocumentService;
import org.openmrs.module.bahmniemrapi.encountertransaction.matcher.EncounterProviderMatcher;
import org.openmrs.module.emrapi.encounter.EncounterParameters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class VisitDocumentServiceImpl implements VisitDocumentService {
    
    public static final String DOCUMENT_OBS_GROUP_CONCEPT_NAME = "Document";

    private VisitService visitService;
    private ConceptService conceptService;
    private EncounterService encounterService;

    @Autowired
	public VisitDocumentServiceImpl(VisitService visitService, ConceptService conceptService, EncounterService encounterService,@Qualifier("adminService")AdministrationService administrationService) {
        this.visitService = visitService;
        this.conceptService = conceptService;
        this.encounterService = encounterService;
    }

    @Override
    public Visit upload(VisitDocumentRequest visitDocumentRequest) {
        Patient patient = Context.getPatientService().getPatientByUuid(visitDocumentRequest.getPatientUuid());

        Visit visit = findOrCreateVisit(visitDocumentRequest, patient);

        Date encounterDate = (visit.getStopDatetime() != null) ? visit.getStartDatetime() : new Date();

        Encounter encounter = findOrCreateEncounter(visit, visitDocumentRequest.getEncounterTypeUuid(), encounterDate, patient, visitDocumentRequest.getProviderUuid(), visitDocumentRequest.getLocationUuid());
        visit.addEncounter(encounter);

        updateEncounter(encounter, encounterDate, visitDocumentRequest.getDocuments());

        return Context.getVisitService().saveVisit(visit);
    }

    private void updateEncounter(Encounter encounter, Date encounterDateTime, List<Document> documents){
        LinkedHashSet<Obs> observations = new LinkedHashSet<>(encounter.getAllObs());
        Concept imageConcept = conceptService.getConceptByName(DOCUMENT_OBS_GROUP_CONCEPT_NAME);

        for (Document document : documents) {
            Concept testConcept = conceptService.getConceptByUuid(document.getTestUuid());

            Obs parentObservation = findOrCreateParentObs(encounter, encounterDateTime, testConcept, document.getObsUuid());

            if(!document.isVoided()){
                if(documentConceptChanged(parentObservation,document.getTestUuid())) {
                    parentObservation = voidExistingAndCreateNewObs(testConcept, parentObservation);
                }
                else{
                    parentObservation.setConcept(testConcept);
                }

                String url = document.getImage();
                parentObservation.addGroupMember(newObs(parentObservation.getObsDatetime(), encounter, imageConcept, url));
                observations.add(parentObservation);
            }
            else{
                voidDocumentObservationTree(parentObservation);
            }
        }
        encounter.setObs(observations);
    }

    private Obs voidExistingAndCreateNewObs(Concept testConcept, Obs parentObservation) {
        voidDocumentObservationTree(parentObservation);
        Obs newObs = new Obs(parentObservation.getPerson(),testConcept,parentObservation.getObsDatetime(),parentObservation.getLocation());
        newObs.setEncounter(parentObservation.getEncounter());
        return newObs;
    }

    private boolean documentConceptChanged(Obs parentObservation, String testUuid) {
        return !parentObservation.getConcept().getUuid().equals(testUuid);
    }

    private Obs findOrCreateParentObs(Encounter encounter, Date observationDateTime, Concept testConcept, String obsUuid) {
        Obs observation = findObservation(encounter.getAllObs(), obsUuid);
        return observation != null ? observation : newObs(observationDateTime, encounter, testConcept, null) ;
    }

    private void voidDocumentObservationTree(Obs obs) {
        obs.setVoided(true);
        Set<Obs> groupMembers = obs.getGroupMembers();
        if(groupMembers != null){
            for (Obs groupMember : groupMembers) {
                groupMember.setVoided(true);
            }
        }
    }

    private Obs findObservation(Set<Obs> allObs, String obsUuid) {
        for (Obs obs : allObs) {
            if (obs.getUuid().equals(obsUuid)) {
                return obs;
            }
        }
        return null;
    }

    private Obs newObs(Date obsDate, Encounter encounter, Concept concept, String value) {
        Obs observation = new Obs();
        observation.setPerson(encounter.getPatient());
        observation.setEncounter(encounter);
        observation.setConcept(concept);
        observation.setObsDatetime(obsDate);
        if (value != null) {
            observation.setValueText(value);
        }
        return observation;
    }

    private Encounter findOrCreateEncounter(Visit visit, String encounterTypeUUID, Date encounterDateTime, Patient patient, String providerUuid, String locationUuid) {
		EncounterType encounterType = encounterService.getEncounterTypeByUuid(encounterTypeUUID);
        Location location = Context.getLocationService().getLocationByUuid(locationUuid);
        Provider provider = Context.getProviderService().getProviderByUuid(providerUuid);

        EncounterParameters encounterParameters = EncounterParameters.instance();
        encounterParameters.setEncounterType(encounterType).setProviders(new HashSet<>(Arrays.asList(provider))).setLocation(location);

        Encounter existingEncounter = new EncounterProviderMatcher().findEncounter(visit, encounterParameters);
		if (existingEncounter != null) {
			return existingEncounter;
		}

		Encounter encounter = new Encounter();
		encounter.setPatient(patient);
		encounter.setEncounterType(encounterType);
		encounter.setEncounterDatetime(encounterDateTime);
        encounter.setLocation(location);
		EncounterRole encounterRoleByUuid = Context.getEncounterService().getEncounterRoleByUuid(EncounterRole.UNKNOWN_ENCOUNTER_ROLE_UUID);
        encounter.addProvider(encounterRoleByUuid, provider);
        return encounter;
    }

    private Visit createVisit(String visitTypeUUID, Date visitStartDate, Date visitEndDate, Patient patient) {
        VisitType visitType = Context.getVisitService().getVisitTypeByUuid(visitTypeUUID);
        Visit visit = new Visit();
        visit.setPatient(patient);
        visit.setVisitType(visitType);
        visit.setStartDatetime(visitStartDate);
        visit.setStopDatetime(visitEndDate);
        visit.setEncounters(new HashSet<Encounter>());
        return visit;
    }

    private Visit findOrCreateVisit(VisitDocumentRequest request, Patient patient) {
        if (request.getVisitUuid() != null) {
            return visitService.getVisitByUuid(request.getVisitUuid());
        }
        return createVisit(request.getVisitTypeUuid(), request.getVisitStartDate(), request.getVisitEndDate(), patient);
    }

}
