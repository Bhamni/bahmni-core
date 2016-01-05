package org.bahmni.module.referencedata.labconcepts.service.impl;

import org.apache.commons.lang3.StringUtils;
import org.bahmni.module.referencedata.labconcepts.contract.ConceptCommon;
import org.bahmni.module.referencedata.labconcepts.model.ConceptMetaData;
import org.bahmni.module.referencedata.labconcepts.service.ConceptMetaDataService;
import org.openmrs.Concept;
import org.openmrs.ConceptClass;
import org.openmrs.ConceptDatatype;
import org.openmrs.ConceptSearchResult;
import org.openmrs.api.AdministrationService;
import org.openmrs.api.ConceptService;
import org.openmrs.api.context.Context;
import org.openmrs.util.LocaleUtility;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Locale;

@Service
public class ConceptMetaDataServiceImpl implements ConceptMetaDataService {

    @Autowired
    private ConceptService conceptService;


    private AdministrationService administrationService;


    @Override
    public ConceptMetaData getConceptMetaData(ConceptCommon conceptCommon) {
        ConceptClass conceptClass = conceptService.getConceptClassByName(conceptCommon.getClassName());
        org.openmrs.Concept existingConcept = getExistingConcept(conceptCommon.getUniqueName(), conceptCommon.getUuid());
        ConceptDatatype conceptDatatype = conceptService.getConceptDatatypeByName(conceptCommon.getDataType());
        return new ConceptMetaData(existingConcept, conceptDatatype, conceptClass, getLocale(conceptCommon.getLocale()));
    }

    private org.openmrs.Concept getExistingConcept(String uniqueName, String uuid) {
        if (uuid != null) {
            return conceptService.getConceptByUuid(uuid);
        }

        Concept conceptByName = conceptService.getConceptByName(uniqueName);
        if (conceptByName != null) {
            return conceptByName;
        }

        administrationService = Context.getAdministrationService();
        List<Locale> locales = administrationService.getAllowedLocales();
        List<ConceptSearchResult> conceptSearchResults = conceptService.getConcepts(uniqueName, locales, false, null, null, null, null, null, null, null);
        if (conceptSearchResults.isEmpty())
            return null;
        return conceptSearchResults.get(0).getConcept();

    }


    private Locale getLocale(String locale) {
        if (StringUtils.isEmpty(locale)) {
            return Context.getLocale();
        }

        Locale locale1 = LocaleUtility.fromSpecification(locale);
        if (!LocaleUtility.isValid(locale1)) {
            throw new IllegalArgumentException("The locale " + locale + " is not valid");
        }
        return locale1;
    }

}
