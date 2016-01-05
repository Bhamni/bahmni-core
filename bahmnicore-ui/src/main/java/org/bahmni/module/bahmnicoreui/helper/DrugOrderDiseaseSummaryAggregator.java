package org.bahmni.module.bahmnicoreui.helper;

import org.apache.commons.lang3.StringUtils;
import org.bahmni.module.bahmnicore.dao.VisitDao;
import org.bahmni.module.bahmnicore.service.BahmniDrugOrderService;
import org.bahmni.module.bahmnicoreui.contract.DiseaseDataParams;
import org.bahmni.module.bahmnicoreui.contract.DiseaseSummaryData;
import org.bahmni.module.bahmnicoreui.mapper.DiseaseSummaryDrugOrderMapper;
import org.bahmni.module.referencedata.helper.ConceptHelper;
import org.openmrs.Concept;
import org.openmrs.DrugOrder;
import org.openmrs.Patient;
import org.openmrs.Visit;
import org.openmrs.api.VisitService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class DrugOrderDiseaseSummaryAggregator {

    private final Integer DEFAULT_VISIT_NUMBER = 50;
    private final VisitService visitService;
    private BahmniDrugOrderService drugOrderService;
    private ConceptHelper conceptHelper;
    private VisitDao visitDao;
    private final DiseaseSummaryDrugOrderMapper diseaseSummaryDrugOrderMapper = new DiseaseSummaryDrugOrderMapper();

    @Autowired
    public DrugOrderDiseaseSummaryAggregator(ConceptHelper conceptHelper, VisitService visitService, BahmniDrugOrderService drugOrderService, VisitDao visitDao) {
        this.visitService = visitService;
        this.drugOrderService = drugOrderService;
        this.conceptHelper = conceptHelper;
        this.visitDao = visitDao;
    }

    public DiseaseSummaryData aggregate(Patient patient, DiseaseDataParams diseaseDataParams) {
        DiseaseSummaryData diseaseSummaryData = new DiseaseSummaryData();
        List<Concept> concepts = conceptHelper.getConceptsForNames(diseaseDataParams.getDrugConcepts());
        if (!concepts.isEmpty()) {
            List<DrugOrder> drugOrders = drugOrderService.getPrescribedDrugOrdersForConcepts(patient, true, getVisits(patient, diseaseDataParams), concepts, diseaseDataParams.getStartDate(), diseaseDataParams.getEndDate() );
            diseaseSummaryData.addTabularData(diseaseSummaryDrugOrderMapper.map(drugOrders, diseaseDataParams.getGroupBy()));
            diseaseSummaryData.addConceptDetails(conceptHelper.getConceptDetails(concepts));
        }
        return diseaseSummaryData;
    }

    private List<Visit> getVisits(Patient patient, final DiseaseDataParams diseaseDataParams) {
        if (StringUtils.isBlank(diseaseDataParams.getVisitUuid())) {
            return visitDao.getVisitsByPatient(patient, getNumberOfVisits(diseaseDataParams.getNumberOfVisits()));
        }
        return new ArrayList() {{
            add(visitService.getVisitByUuid(diseaseDataParams.getVisitUuid()));
        }};
    }

    private int getNumberOfVisits(Integer numberOfVisit) {
        return null != numberOfVisit ? numberOfVisit : DEFAULT_VISIT_NUMBER;
    }
}
