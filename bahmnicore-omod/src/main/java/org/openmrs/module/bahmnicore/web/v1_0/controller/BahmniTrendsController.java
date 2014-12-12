package org.openmrs.module.bahmnicore.web.v1_0.controller;

import org.bahmni.module.bahmnicore.contract.encounter.data.ConceptData;
import org.bahmni.module.bahmnicore.contract.encounter.data.PersonObservationData;
import org.openmrs.module.bahmniemrapi.encountertransaction.service.BahmniObsService;
import org.openmrs.Concept;
import org.openmrs.ConceptNumeric;
import org.openmrs.Obs;
import org.openmrs.module.webservices.rest.web.RestConstants;
import org.openmrs.module.webservices.rest.web.v1_0.controller.BaseRestController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping(value = "/rest/" + RestConstants.VERSION_1 + "/bahmnicore/bahmniobs")
public class BahmniTrendsController extends BaseRestController {
    @Autowired
    private BahmniObsService personObsService;

    @Autowired

    public BahmniTrendsController(BahmniObsService personObsService) {
        this.personObsService = personObsService;
    }

    public BahmniTrendsController() {
    }

    @RequestMapping(method = RequestMethod.GET)
    @ResponseBody
    public List<PersonObservationData> get(String patientUUID) {
        List<Obs> obsForPerson = personObsService.getObsForPerson(patientUUID);
        List<PersonObservationData> observationDataList = new ArrayList<>();
        for (Obs obs : obsForPerson) {
            Concept concept = obs.getConcept();
            String units = null;
            if(concept.isNumeric()){
                units = ((ConceptNumeric)concept).getUnits();
            }
            observationDataList.add(new PersonObservationData(concept.getName().getName(), obs.getValueNumeric(), obs.getDateCreated(),concept.isNumeric(),units));
        }
        return observationDataList;
    }

    @RequestMapping(method = RequestMethod.GET, value = "concepts")
    @ResponseBody
    public List<ConceptData> getConceptsfor(String patientUUID) {
        List<Concept> numericConcepts = personObsService.getNumericConceptsForPerson(patientUUID);
        List<ConceptData> conceptDataList = new ArrayList<>();
        for (Concept concept : numericConcepts){
            conceptDataList.add(new ConceptData(concept.getUuid(), concept.getName().getName()));
        }
        return conceptDataList;
    }

}
