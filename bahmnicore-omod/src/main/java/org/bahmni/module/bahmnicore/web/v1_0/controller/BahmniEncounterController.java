package org.bahmni.module.bahmnicore.web.v1_0.controller;

import org.bahmni.module.bahmnicore.web.v1_0.VisitClosedException;
import org.openmrs.Encounter;
import org.openmrs.Visit;
import org.openmrs.api.EncounterService;
import org.openmrs.module.bahmniemrapi.encountertransaction.contract.BahmniEncounterSearchParameters;
import org.openmrs.module.bahmniemrapi.encountertransaction.contract.BahmniEncounterTransaction;
import org.openmrs.module.bahmniemrapi.encountertransaction.mapper.BahmniEncounterTransactionMapper;
import org.openmrs.module.bahmniemrapi.encountertransaction.service.BahmniEncounterTransactionService;
import org.openmrs.module.emrapi.encounter.EmrEncounterService;
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

import java.util.Date;

import static org.bahmni.module.bahmnicore.util.MiscUtils.setUuidsForObservations;
import static org.bahmni.module.bahmnicore.util.MiscUtils.validateFormFieldPath;

@Controller
@RequestMapping(value = "/rest/" + RestConstants.VERSION_1 + "/bahmnicore/bahmniencounter")
public class BahmniEncounterController extends BaseRestController {
    private EncounterService encounterService;
    private EmrEncounterService emrEncounterService;
    private EncounterTransactionMapper encounterTransactionMapper;
    private BahmniEncounterTransactionService bahmniEncounterTransactionService;
    private BahmniEncounterTransactionMapper bahmniEncounterTransactionMapper;

    public BahmniEncounterController() {
    }

    @Autowired
    public BahmniEncounterController(EncounterService encounterService,
                                     EmrEncounterService emrEncounterService, EncounterTransactionMapper encounterTransactionMapper,
                                     BahmniEncounterTransactionService bahmniEncounterTransactionService,
                                     BahmniEncounterTransactionMapper bahmniEncounterTransactionMapper) {
        this.encounterService = encounterService;
        this.emrEncounterService = emrEncounterService;
        this.encounterTransactionMapper = encounterTransactionMapper;
        this.bahmniEncounterTransactionService = bahmniEncounterTransactionService;
        this.bahmniEncounterTransactionMapper = bahmniEncounterTransactionMapper;
    }

    @RequestMapping(method = RequestMethod.GET, value = "/{uuid}")
    @ResponseBody
    public BahmniEncounterTransaction get(@PathVariable("uuid") String uuid, @RequestParam(value = "includeAll", required = false) Boolean includeAll) {
        EncounterTransaction encounterTransaction = emrEncounterService.getEncounterTransaction(uuid, includeAll);
        return bahmniEncounterTransactionMapper.map(encounterTransaction, includeAll);
    }

    @RequestMapping(method = RequestMethod.POST, value = "/find")
    @ResponseBody
    public BahmniEncounterTransaction find(@RequestBody BahmniEncounterSearchParameters encounterSearchParameters) {
        EncounterTransaction encounterTransaction = bahmniEncounterTransactionService.find(encounterSearchParameters);

        if (encounterTransaction != null) {
            return bahmniEncounterTransactionMapper.map(encounterTransaction, encounterSearchParameters.getIncludeAll());
        } else {
            return bahmniEncounterTransactionMapper.map(new EncounterTransaction(), false);
        }
    }

    @RequestMapping(method = RequestMethod.DELETE, value = "/{uuid}")
    @ResponseBody
    public void delete(@PathVariable("uuid") String uuid, @RequestParam(value = "reason", defaultValue = "web service call") String reason){
        String errorMessage = "Visit for this patient is closed. You cannot do an 'Undo Discharge' for the patient.";
        Visit visit = encounterService.getEncounterByUuid(uuid).getVisit();
        Date stopDate = visit.getStopDatetime();
        if(stopDate != null && stopDate.before(new Date())){
            throw new VisitClosedException(errorMessage);
        }
        else{
            BahmniEncounterTransaction bahmniEncounterTransaction = get(uuid,false);
            bahmniEncounterTransaction.setReason(reason);
            bahmniEncounterTransactionService.delete(bahmniEncounterTransaction);
        }
    }

    @RequestMapping(method = RequestMethod.POST)
    @ResponseBody
    @Transactional
    public BahmniEncounterTransaction update(@RequestBody BahmniEncounterTransaction bahmniEncounterTransaction) {
        setUuidsForObservations(bahmniEncounterTransaction.getObservations());
        validateFormFieldPath(bahmniEncounterTransaction.getObservations());
        return bahmniEncounterTransactionService.save(bahmniEncounterTransaction);
    }

    public BahmniEncounterTransaction get(String encounterUuid) {
        Encounter encounter = encounterService.getEncounterByUuid(encounterUuid);
        boolean includeAll = false;
        EncounterTransaction encounterTransaction = encounterTransactionMapper.map(encounter, includeAll);
        return bahmniEncounterTransactionMapper.map(encounterTransaction, includeAll);
    }

}
