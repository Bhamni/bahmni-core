package org.bahmni.module.bahmnicore.service;

import org.bahmni.module.bahmnicore.contract.diseasetemplate.DiseaseTemplate;
import org.bahmni.module.bahmnicore.contract.diseasetemplate.DiseaseTemplatesConfig;

import java.util.List;

public interface DiseaseTemplateService {

    List<DiseaseTemplate> allDiseaseTemplatesFor(DiseaseTemplatesConfig diseaseTemplatesConfig);

    DiseaseTemplate diseaseTemplateFor(DiseaseTemplatesConfig diseaseTemplatesConfig);
}
