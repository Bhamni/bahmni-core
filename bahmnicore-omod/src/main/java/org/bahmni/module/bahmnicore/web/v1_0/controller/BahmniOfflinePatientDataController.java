package org.bahmni.module.bahmnicore.web.v1_0.controller;

import org.bahmni.module.bahmnicore.contract.patient.PatientSearchParameters;
import org.bahmni.module.bahmnicore.contract.patient.response.PatientResponse;
import org.bahmni.module.bahmnicore.service.BahmniPatientService;
import org.openmrs.Patient;
import org.openmrs.Person;
import org.openmrs.Relationship;
import org.openmrs.api.context.Context;
import org.openmrs.module.emrapi.patient.PatientProfile;
import org.openmrs.module.webservices.rest.web.ConversionUtil;
import org.openmrs.module.webservices.rest.web.RequestContext;
import org.openmrs.module.webservices.rest.web.RestConstants;
import org.openmrs.module.webservices.rest.web.RestUtil;
import org.openmrs.module.webservices.rest.web.representation.Representation;
import org.openmrs.module.webservices.rest.web.resource.impl.AlreadyPaged;
import org.openmrs.module.webservices.rest.web.response.ResponseException;
import org.openmrs.module.webservices.rest.web.v1_0.controller.BaseRestController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;

/**
 * Controller for REST web service access to
 * the Search resource.
 */
@Controller
@RequestMapping(value = "/rest/" + RestConstants.VERSION_1 + "/bahmnicore/patientData")
public class BahmniOfflinePatientDataController extends BaseRestController {

    private BahmniPatientService bahmniPatientService;


    @Autowired
    public BahmniOfflinePatientDataController(BahmniPatientService bahmniPatientService) {
        this.bahmniPatientService = bahmniPatientService;
    }

    @RequestMapping(method = RequestMethod.GET)
    @ResponseBody
    public AlreadyPaged<Object> getPatientData(HttpServletRequest request,
                                       HttpServletResponse response) throws ResponseException {
        RequestContext requestContext = RestUtil.getRequestContext(request, response);
        PatientSearchParameters searchParameters = new PatientSearchParameters(requestContext);
        List<PatientResponse> patients = bahmniPatientService.search(searchParameters);
        List<Object> returnValue = new ArrayList<>();
        for(PatientResponse bahmniPatient : patients){
            PatientProfile delegate = new PatientProfile();

            Patient patient = Context.getPatientService().getPatientByUuid(bahmniPatient.getUuid());
            delegate.setPatient(patient);

            Person person = Context.getPersonService().getPerson(bahmniPatient.getPersonId());
            List<Relationship> relationships = Context.getPersonService().getRelationshipsByPerson(person);
            delegate.setRelationships(relationships);
            returnValue.add(ConversionUtil.convertToRepresentation(delegate, Representation.FULL));
        }
        return new AlreadyPaged<>(requestContext, returnValue, false);
    }
}
