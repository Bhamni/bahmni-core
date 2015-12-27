package org.bahmni.module.bahmnicore.service.impl;

import org.bahmni.module.bahmnicore.dao.BahmniConceptDao;
import org.bahmni.module.bahmnicore.service.BahmniConceptService;
import org.openmrs.Concept;
import org.openmrs.Drug;
import org.openmrs.api.ConceptService;
import org.openmrs.module.emrapi.encounter.ConceptMapper;
import org.openmrs.module.emrapi.encounter.domain.EncounterTransaction;
import org.openmrs.module.emrapi.encounter.exception.ConceptNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;

@Component
@Transactional
public class BahmniConceptServiceImpl implements BahmniConceptService{

    private ConceptService conceptService;
    private ConceptMapper conceptMapper;
    private BahmniConceptDao bahmniConceptDao;

    @Autowired
    public BahmniConceptServiceImpl(ConceptService conceptService, BahmniConceptDao bahmniConceptDao) {
        this.conceptService = conceptService;
        this.bahmniConceptDao = bahmniConceptDao;
        this.conceptMapper = new ConceptMapper();
    }

    @Override
    @Transactional(readOnly = true)
    public EncounterTransaction.Concept getConceptByName(String conceptName) {
        Concept concept = conceptByName(conceptName);
        if (concept == null) {
            return new EncounterTransaction.Concept(null, conceptName, false, null, null, null, null,null);
        }
        return convertToContract(concept);
    }

    @Override
    @Transactional(readOnly = true)
    public Collection<Concept> searchByQuestion(String questionConceptName, String query) {
        Concept questionConcept = bahmniConceptDao.getConceptByFullySpecifiedName(questionConceptName);
        if(questionConcept==null){
            throw new ConceptNotFoundException("Concept '" + questionConceptName + "' not found");
        }
        return bahmniConceptDao.searchByQuestion(questionConcept, query);
    }

    @Override
    @Transactional(readOnly = true)
    public Collection<Drug> getDrugsByConceptSetName(String conceptSetName) {
        Concept conceptSet = bahmniConceptDao.getConceptByFullySpecifiedName(conceptSetName);
        List<Concept> setMembers = conceptService.getConceptsByConceptSet(conceptSet);
        return bahmniConceptDao.getDrugByListOfConcepts(setMembers);
    }

    private EncounterTransaction.Concept convertToContract(Concept concept) {
        return conceptMapper.map(concept);
    }

    private Concept conceptByName(String conceptName) {
        return conceptService.getConceptByName(conceptName);
    }
}
