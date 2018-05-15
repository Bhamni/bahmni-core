package org.bahmni.module.bahmnicore.web.v1_0.controller.search;

import org.bahmni.module.bahmnicore.contract.patient.PatientSearchParameters;
import org.bahmni.module.bahmnicore.contract.patient.mapper.PatientResponseMapper;
import org.bahmni.module.bahmnicore.contract.patient.response.PatientResponse;
import org.bahmni.module.bahmnicore.service.BahmniPatientService;
import org.openmrs.Patient;
import org.openmrs.PatientIdentifier;
import org.openmrs.Person;
import org.openmrs.api.context.Context;
import org.openmrs.module.bahmniemrapi.visitlocation.BahmniVisitLocationServiceImpl;
import org.openmrs.module.webservices.rest.web.RequestContext;
import org.openmrs.module.webservices.rest.web.RestConstants;
import org.openmrs.module.webservices.rest.web.RestUtil;
import org.openmrs.module.webservices.rest.web.resource.impl.AlreadyPaged;
import org.openmrs.module.webservices.rest.web.response.ResponseException;
import org.openmrs.module.webservices.rest.web.v1_0.controller.BaseRestController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * Controller for REST web service access to
 * the Search resource.
 */
@Controller
@RequestMapping(value = "/rest/" + RestConstants.VERSION_1 + "/bahmnicore/search/patient")
public class BahmniPatientSearchController extends BaseRestController {

    private BahmniPatientService bahmniPatientService;


    @Autowired
    public BahmniPatientSearchController(BahmniPatientService bahmniPatientService) {
        this.bahmniPatientService = bahmniPatientService;
    }

    @RequestMapping(method = RequestMethod.GET)
    @ResponseBody
    public ResponseEntity<AlreadyPaged<PatientResponse>> search(HttpServletRequest request,
                                                  HttpServletResponse response) throws ResponseException{
        RequestContext requestContext = RestUtil.getRequestContext(request, response);
        PatientSearchParameters searchParameters = new PatientSearchParameters(requestContext);
        try {
            List<PatientResponse> patients = bahmniPatientService.search(searchParameters);
            AlreadyPaged alreadyPaged = new AlreadyPaged(requestContext, patients, false);
            return new ResponseEntity(alreadyPaged,HttpStatus.OK);
        }catch (IllegalArgumentException e){
            return new ResponseEntity(RestUtil.wrapErrorResponse(e, e.getMessage()), HttpStatus.BAD_REQUEST);
        }
    }

    @RequestMapping(value="lucene", method = RequestMethod.GET)
    @ResponseBody
    public ResponseEntity<AlreadyPaged<PatientResponse>> luceneSearch(HttpServletRequest request,
                                                  HttpServletResponse response) throws ResponseException{
        RequestContext requestContext = RestUtil.getRequestContext(request, response);
        PatientSearchParameters searchParameters = new PatientSearchParameters(requestContext);
        try {
            List<PatientResponse> patients = bahmniPatientService.luceneSearch(searchParameters);
            AlreadyPaged alreadyPaged = new AlreadyPaged(requestContext, patients, false);
            return new ResponseEntity(alreadyPaged,HttpStatus.OK);
        }catch (IllegalArgumentException e){
            return new ResponseEntity(RestUtil.wrapErrorResponse(e, e.getMessage()), HttpStatus.BAD_REQUEST);
        }
    }

    @RequestMapping(value="similar", method = RequestMethod.GET)
    @ResponseBody
    public ResponseEntity<AlreadyPaged<PatientResponse>> searchSimilarPerson(HttpServletRequest request,
                                                    HttpServletResponse response) throws ResponseException{
        RequestContext requestContext = RestUtil.getRequestContext(request, response);
        PatientSearchParameters searchParameters = new PatientSearchParameters(requestContext);
        try {
            List<PatientResponse> patients = bahmniPatientService.searchSimilarPatients(searchParameters);
            AlreadyPaged alreadyPaged = new AlreadyPaged(requestContext, patients, false);
            return new ResponseEntity(alreadyPaged, HttpStatus.OK);
        }catch (IllegalArgumentException e){
            return new ResponseEntity(RestUtil.wrapErrorResponse(e, e.getMessage()), HttpStatus.BAD_REQUEST);
        }
    }
}
