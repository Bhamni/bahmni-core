package org.bahmni.module.bahmnicoreui.service.impl;

import org.bahmni.module.bahmnicoreui.contract.DiseaseDataParams;
import org.bahmni.module.bahmnicoreui.contract.DiseaseSummaryData;
import org.bahmni.module.bahmnicoreui.helper.DrugOrderDiseaseSummaryAggregator;
import org.bahmni.module.bahmnicoreui.helper.LabDiseaseSummaryAggregator;
import org.bahmni.module.bahmnicoreui.helper.ObsDiseaseSummaryAggregator;
import org.bahmni.module.bahmnicoreui.service.BahmniDiseaseSummaryService;
import org.openmrs.Patient;
import org.openmrs.api.PatientService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
public class BahmniDiseaseSummaryServiceImpl implements BahmniDiseaseSummaryService {

    private PatientService patientService;
    private LabDiseaseSummaryAggregator labDiseaseSummaryAggregator;
    private DrugOrderDiseaseSummaryAggregator drugOrderDiseaseSummaryAggregator;
    private ObsDiseaseSummaryAggregator obsDiseaseSummaryAggregator;

    @Autowired
    public BahmniDiseaseSummaryServiceImpl(PatientService patientService, LabDiseaseSummaryAggregator labDiseaseSummaryAggregator, DrugOrderDiseaseSummaryAggregator drugOrderDiseaseSummaryAggregator, ObsDiseaseSummaryAggregator obsDiseaseSummaryAggregator){
        this.patientService = patientService;
        this.labDiseaseSummaryAggregator = labDiseaseSummaryAggregator;
        this.drugOrderDiseaseSummaryAggregator = drugOrderDiseaseSummaryAggregator;
        this.obsDiseaseSummaryAggregator = obsDiseaseSummaryAggregator;
    }

    @Override
    @Transactional(readOnly = true)
    public DiseaseSummaryData getDiseaseSummary(String patientUuid, DiseaseDataParams queryParams) {
        DiseaseSummaryData diseaseSummaryData = new DiseaseSummaryData();

        Patient patient = patientService.getPatientByUuid(patientUuid);

        diseaseSummaryData.concat(obsDiseaseSummaryAggregator.aggregate(patient, queryParams.getObsConcepts(), queryParams.getNumberOfVisits()));
        diseaseSummaryData.concat(labDiseaseSummaryAggregator.aggregate(patient, queryParams.getLabConcepts(), queryParams.getNumberOfVisits()));
        diseaseSummaryData.concat(drugOrderDiseaseSummaryAggregator.aggregate(patient, queryParams.getDrugConcepts(), queryParams.getNumberOfVisits()));
        return diseaseSummaryData;
    }





}
