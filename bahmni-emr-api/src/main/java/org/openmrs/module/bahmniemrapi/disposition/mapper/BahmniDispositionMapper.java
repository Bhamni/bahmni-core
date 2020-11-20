package org.openmrs.module.bahmniemrapi.disposition.mapper;

import org.openmrs.Concept;
import org.openmrs.User;
import org.openmrs.api.ConceptService;
import org.openmrs.module.bahmniemrapi.disposition.contract.BahmniDisposition;
import org.openmrs.module.emrapi.encounter.domain.EncounterTransaction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.Locale;

@Component
public class BahmniDispositionMapper {
    @Autowired
    private ConceptService conceptService;
    public BahmniDisposition map(EncounterTransaction.Disposition disposition, Set<EncounterTransaction.Provider> providers, User user , Locale locale){
        BahmniDisposition bahmniDisposition = new BahmniDisposition();
        bahmniDisposition.setAdditionalObs(disposition.getAdditionalObs());
        bahmniDisposition.setCode(disposition.getCode());
        bahmniDisposition.setConceptName(disposition.getConceptName());
        bahmniDisposition.setDispositionDateTime(disposition.getDispositionDateTime());
        bahmniDisposition.setVoided(disposition.isVoided());
        bahmniDisposition.setExistingObs(disposition.getExistingObs());
        bahmniDisposition.setVoidReason(disposition.getVoidReason());
        bahmniDisposition.setProviders(providers);
        bahmniDisposition.setCreatorName(user.getPersonName().toString());
        Concept concept = conceptService.getConcept(disposition.getConceptName());
        bahmniDisposition.setPreferredName(concept.getPreferredName(locale).getName());
        return bahmniDisposition;
    }

}
