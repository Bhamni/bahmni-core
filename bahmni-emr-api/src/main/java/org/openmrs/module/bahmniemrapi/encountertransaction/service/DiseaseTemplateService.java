package org.openmrs.module.bahmniemrapi.encountertransaction.service;

import org.openmrs.module.bahmniemrapi.encountertransaction.contract.DiseaseTemplate;
import org.bahmni.module.bahmnicore.contract.diseasetemplate.DiseaseTemplatesConfig;

import java.util.List;

public interface DiseaseTemplateService {

    public List<DiseaseTemplate> allDiseaseTemplatesFor(DiseaseTemplatesConfig diseaseTemplatesConfig);

    public DiseaseTemplate diseaseTemplateFor(String patientUUID, String diseaseName);
}
