package org.bahmni.module.bahmnicoreui.helper;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Predicate;
import org.apache.commons.lang3.StringUtils;
import org.bahmni.module.bahmnicore.service.BahmniObsService;
import org.bahmni.module.bahmnicoreui.contract.DiseaseDataParams;
import org.bahmni.module.bahmnicoreui.contract.DiseaseSummaryData;
import org.bahmni.module.bahmnicoreui.mapper.DiseaseSummaryObsMapper;
import org.bahmni.module.referencedata.helper.ConceptHelper;
import org.openmrs.Concept;
import org.openmrs.Patient;
import org.openmrs.module.bahmniemrapi.encountertransaction.contract.BahmniObservation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

@Component
public class ObsDiseaseSummaryAggregator {

    private final ConceptHelper conceptHelper;
    private BahmniObsService bahmniObsService;
    private final DiseaseSummaryObsMapper diseaseSummaryObsMapper = new DiseaseSummaryObsMapper();

    @Autowired
    public ObsDiseaseSummaryAggregator(ConceptHelper conceptHelper, BahmniObsService bahmniObsService) {
        this.bahmniObsService = bahmniObsService;
        this.conceptHelper = conceptHelper;
    }

    public DiseaseSummaryData aggregate(Patient patient, DiseaseDataParams queryParams) {
        DiseaseSummaryData diseaseSummaryData = new DiseaseSummaryData();
        List<Concept> concepts = conceptHelper.getConceptsForNames(queryParams.getObsConcepts());
        Collection<BahmniObservation> bahmniObservations = fetchBahmniObservations(patient, queryParams, concepts);
        constructDiseaseSummaryData(bahmniObservations, concepts, queryParams.getGroupBy(), diseaseSummaryData);
        return diseaseSummaryData;
    }

    private Collection<BahmniObservation> fetchBahmniObservations(Patient patient, DiseaseDataParams queryParams, List<Concept> concepts) {
        if (StringUtils.isBlank(queryParams.getVisitUuid())) {
            if (!concepts.isEmpty()) {
                return bahmniObsService.observationsFor(patient.getUuid(), concepts, queryParams.getNumberOfVisits(), null, false, null, queryParams.getStartDate(), queryParams.getEndDate());
            }
            return Collections.EMPTY_LIST;
        }
        return filterObservationsLinkedWithOrders(bahmniObsService.getObservationForVisit(queryParams.getVisitUuid(), queryParams.getObsConcepts(), null,  false, null));
    }

    private Collection<BahmniObservation> filterObservationsLinkedWithOrders(Collection<BahmniObservation> bahmniObservations) {
        CollectionUtils.filter(bahmniObservations,new Predicate() {
            @Override
            public boolean evaluate(Object bahmniObservation) {
                return ((BahmniObservation)bahmniObservation).getOrderUuid() == null;
            }
        });
        return bahmniObservations;
    }

    private void constructDiseaseSummaryData(Collection<BahmniObservation> bahmniObservations, List<Concept> concepts, String groupBy, DiseaseSummaryData diseaseSummaryData) {
        diseaseSummaryData.setTabularData(diseaseSummaryObsMapper.map(bahmniObservations, groupBy));
        diseaseSummaryData.addConceptDetails(conceptHelper.getLeafConceptDetails(concepts, false));
    }
}
