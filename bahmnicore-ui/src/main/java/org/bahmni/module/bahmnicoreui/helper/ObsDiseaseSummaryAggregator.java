package org.bahmni.module.bahmnicoreui.helper;

import org.bahmni.module.bahmnicoreui.contract.DiseaseSummaryData;
import org.bahmni.module.bahmnicoreui.mapper.DiseaseSummaryMapper;
import org.openmrs.Concept;
import org.openmrs.Patient;
import org.openmrs.api.ConceptService;
import org.openmrs.module.bahmniemrapi.encountertransaction.contract.BahmniObservation;
import org.openmrs.module.bahmniemrapi.encountertransaction.service.BahmniObsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ObsDiseaseSummaryAggregator {

    private final ConceptHelper conceptHelper;
    private BahmniObsService bahmniObsService;
    private final DiseaseSummaryMapper diseaseSummaryMapper = new DiseaseSummaryMapper();

    @Autowired
    public ObsDiseaseSummaryAggregator(ConceptService conceptService, BahmniObsService bahmniObsService) {
        this.bahmniObsService = bahmniObsService;
        this.conceptHelper = new ConceptHelper(conceptService);
    }

    public DiseaseSummaryData aggregate(Patient patient, List<String> conceptNames, Integer numberOfVisits) {
        DiseaseSummaryData diseaseSummaryData =  new DiseaseSummaryData();
        List<Concept> concepts = conceptHelper.getConceptsForNames(conceptNames);
        if(!concepts.isEmpty()){
            List<BahmniObservation> bahmniObservations = bahmniObsService.observationsFor(patient.getUuid(), concepts, numberOfVisits);
            diseaseSummaryData.setTabularData(diseaseSummaryMapper.mapObservations(bahmniObservations));
            diseaseSummaryData.addConceptNames(conceptHelper.getLeafConceptNames(conceptNames));
        }
        return diseaseSummaryData;
    }


}
