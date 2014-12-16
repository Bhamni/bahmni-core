package org.openmrs.module.bahmnicore.web.v1_0.controller;

import org.apache.commons.lang.StringUtils;
import org.bahmni.module.bahmnicore.contract.encounter.data.ConceptData;
import org.bahmni.module.bahmnicore.contract.encounter.response.EncounterConfigResponse;
import org.openmrs.Concept;
import org.openmrs.Encounter;
import org.openmrs.EncounterType;
import org.openmrs.OrderType;
import org.openmrs.VisitType;
import org.openmrs.api.ConceptService;
import org.openmrs.api.EncounterService;
import org.openmrs.api.OrderService;
import org.openmrs.api.VisitService;
import org.openmrs.module.bahmnicore.web.v1_0.InvalidInputException;
import org.openmrs.module.bahmniemrapi.encountertransaction.contract.BahmniEncounterTransaction;
import org.openmrs.module.bahmniemrapi.encountertransaction.contract.BahmniObservation;
import org.openmrs.module.bahmniemrapi.encountertransaction.mapper.BahmniEncounterTransactionMapper;
import org.openmrs.module.bahmniemrapi.encountertransaction.service.BahmniEncounterTransactionService;
import org.openmrs.module.emrapi.encounter.EmrEncounterService;
import org.openmrs.module.emrapi.encounter.EncounterSearchParameters;
import org.openmrs.module.emrapi.encounter.EncounterTransactionMapper;
import org.openmrs.module.emrapi.encounter.domain.EncounterTransaction;
import org.openmrs.module.webservices.rest.web.RestConstants;
import org.openmrs.module.webservices.rest.web.v1_0.controller.BaseRestController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Controller
@RequestMapping(value = "/rest/" + RestConstants.VERSION_1 + "/bahmnicore/bahmniencounter")
public class BahmniEncounterController extends BaseRestController {
    @Autowired
    private VisitService visitService;
    @Autowired
    private ConceptService conceptService;
    @Autowired
    private EncounterService encounterService;
    @Autowired
    private OrderService orderService;
    @Autowired
    private EmrEncounterService emrEncounterService;
    @Autowired
    private EncounterTransactionMapper encounterTransactionMapper;
    @Autowired
    private BahmniEncounterTransactionService bahmniEncounterTransactionService;
    @Autowired
    private BahmniEncounterTransactionMapper bahmniEncounterTransactionMapper;

    @RequestMapping(method = RequestMethod.GET, value = "config", params = "callerContext")
    @ResponseBody
    public EncounterConfigResponse getConfig(@RequestParam(value = "callerContext") String callerContext) {
        EncounterConfigResponse encounterConfigResponse = new EncounterConfigResponse();
        List<VisitType> visitTypes = visitService.getAllVisitTypes();
        for (VisitType visitType : visitTypes) {
            if (!visitType.isRetired()) {
                encounterConfigResponse.addVisitType(visitType.getName(), visitType.getUuid());
            }
        }
        List<EncounterType> allEncounterTypes = encounterService.getAllEncounterTypes(false);
        for (EncounterType encounterType : allEncounterTypes) {
            encounterConfigResponse.addEncounterType(encounterType.getName(), encounterType.getUuid());
        }
        Concept conceptSetConcept = conceptService.getConcept(callerContext);
        if (conceptSetConcept != null) {
            List<Concept> conceptsByConceptSet = conceptService.getConceptsByConceptSet(conceptSetConcept);
            for (Concept concept : conceptsByConceptSet) {
                ConceptData conceptData = new ConceptData(concept.getUuid(), concept.getName().getName());
                encounterConfigResponse.addConcept(concept.getName().getName(), conceptData);
            }
        }
        List<OrderType> orderTypes = orderService.getOrderTypes(true);
        for (OrderType orderType : orderTypes) {
            encounterConfigResponse.addOrderType(orderType.getName(), orderType.getUuid());
        }
        return encounterConfigResponse;
    }

    @RequestMapping(method = RequestMethod.GET, value = "/{uuid}")
    @ResponseBody
    public BahmniEncounterTransaction get(@PathVariable("uuid") String uuid, Boolean includeAll) {
        EncounterTransaction encounterTransaction = emrEncounterService.getEncounterTransaction(uuid, includeAll);
        return bahmniEncounterTransactionMapper.map(encounterTransaction);
    }

    @RequestMapping(method = RequestMethod.GET)
    @ResponseBody
    public List<BahmniEncounterTransaction> find(@RequestParam(value = "visitUuids", required = true) String[] visitUuids,
                                                 @RequestParam(value = "includeAll", required = false) boolean includeAll,
                                                 @RequestParam(value = "encounterDate", required = false) String encounterDate) {
        List<BahmniEncounterTransaction> bahmniEncounterTransactions = new ArrayList<>();

        for (String visitUuid : visitUuids ) {
            EncounterSearchParameters encounterSearchParameters = new EncounterSearchParameters();
            encounterSearchParameters.setVisitUuid(visitUuid);
            encounterSearchParameters.setEncounterDate(encounterDate);
            encounterSearchParameters.setIncludeAll(includeAll);

            checkForValidInput(encounterSearchParameters);
            List<EncounterTransaction> encounterTransactions = emrEncounterService.find(encounterSearchParameters);
            for (EncounterTransaction encounterTransaction : encounterTransactions) {
                bahmniEncounterTransactions.add(bahmniEncounterTransactionMapper.map(encounterTransaction));
            }
        }


        return bahmniEncounterTransactions;
    }

    private void checkForValidInput(EncounterSearchParameters encounterSearchParameters) {
        String visitUuid = encounterSearchParameters.getVisitUuid();
        if (StringUtils.isBlank(visitUuid))
            throw new InvalidInputException("Visit UUID cannot be empty.");

        String encounterDate = encounterSearchParameters.getEncounterDate();
        if (StringUtils.isNotBlank(encounterDate)){
            try {
                new SimpleDateFormat("yyyy-MM-dd").parse(encounterDate);
            } catch (ParseException e) {
                throw new InvalidInputException("Date format needs to be 'yyyy-MM-dd'. Incorrect Date:" + encounterDate + ".", e);
            }
        }
    }

    @RequestMapping(method = RequestMethod.POST)
    @ResponseBody
    @Transactional
    public BahmniEncounterTransaction update(@RequestBody BahmniEncounterTransaction bahmniEncounterTransaction) {
        setUuidsForObservations(bahmniEncounterTransaction.getObservations());
        return bahmniEncounterTransactionService.save(bahmniEncounterTransaction);
    }

    public BahmniEncounterTransaction get(String encounterUuid) {
        Encounter encounter = encounterService.getEncounterByUuid(encounterUuid);
        EncounterTransaction encounterTransaction = encounterTransactionMapper.map(encounter, true);
        return bahmniEncounterTransactionMapper.map(encounterTransaction);
    }

    private void setUuidsForObservations(List<BahmniObservation> bahmniObservations) {
        for (BahmniObservation bahmniObservation : bahmniObservations) {
            if (org.apache.commons.lang3.StringUtils.isBlank(bahmniObservation.getUuid())){
                bahmniObservation.setUuid(UUID.randomUUID().toString());
            }
        }
    }
}
