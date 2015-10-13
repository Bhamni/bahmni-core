package org.bahmni.module.referencedata.labconcepts.mapper;

import org.bahmni.module.referencedata.labconcepts.contract.Drug;
import org.bahmni.module.referencedata.labconcepts.model.DrugMetaData;
import org.openmrs.Concept;
import org.openmrs.api.ConceptNameType;
import org.openmrs.api.context.Context;
import org.springframework.util.Assert;

import static org.bahmni.module.referencedata.labconcepts.mapper.ConceptExtension.getConceptName;

public class DrugMapper {

    private final DrugMetaDataMapper drugMetaDataMapper;

    public DrugMapper() {
        drugMetaDataMapper = new DrugMetaDataMapper();
    }

    public org.openmrs.Drug map(Drug drug, DrugMetaData drugMetaData) {
        Assert.notNull(drugMetaData.getDosageForm(),"The dosage form should not be null");
        Assert.notNull(drugMetaData.getDrugConcept(),"The drug concept should not be null");

        org.openmrs.Drug conceptDrug = drugMetaDataMapper.map(drugMetaData);
        conceptDrug.setName(drug.getName());
        ConceptExtension.addConceptName(conceptDrug.getConcept(), getConceptName(drug.getGenericName(), ConceptNameType.FULLY_SPECIFIED), conceptDrug.getConcept().getName().getLocale());
        conceptDrug.setCombination(drug.isCombination());
        conceptDrug.setStrength(drug.getStrength());
        conceptDrug.setMaximumDailyDose(drug.doubleMaximumDose());
        conceptDrug.setMinimumDailyDose(drug.doubleMinimumDose());
        return conceptDrug;
    }


    public Drug map(org.openmrs.Drug conceptDrug) {
        Drug drug = new Drug();
        drug.setName(conceptDrug.getName());
        drug.setShortName(getShortNameFrom(conceptDrug.getConcept()));
        drug.setGenericName(getNameFrom(conceptDrug.getConcept()));
        drug.setDosageForm(getNameFrom(conceptDrug.getDosageForm()));
        drug.setStrength(conceptDrug.getStrength());
        drug.setUuid(conceptDrug.getUuid());
        drug.setCombination(conceptDrug.getCombination());
        drug.setMaximumDose(conceptDrug.getMaximumDailyDose()==null?"":conceptDrug.getMaximumDailyDose().toString());
        drug.setMinimumDose(conceptDrug.getMinimumDailyDose()==null?"":conceptDrug.getMinimumDailyDose().toString());
        return drug;
    }

    private String getShortNameFrom(Concept concept) {
        if (concept != null && concept.getShortNameInLocale(Context.getLocale()) != null) {
            return concept.getShortNameInLocale(Context.getLocale()).getName();
        }
        return null;
    }

    private String getNameFrom(Concept concept) {
        if (concept != null && concept.getName(Context.getLocale()) != null) {
            return concept.getName(Context.getLocale()).getName();
        }
        return null;
    }
}