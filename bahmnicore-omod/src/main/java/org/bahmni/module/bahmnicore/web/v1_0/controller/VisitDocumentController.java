package org.bahmni.module.bahmnicore.web.v1_0.controller;

import org.apache.commons.lang.StringUtils;
import org.bahmni.module.bahmnicore.model.Document;
import org.bahmni.module.bahmnicore.service.PatientDocumentService;
import org.openmrs.Patient;
import org.openmrs.Visit;
import org.openmrs.api.AdministrationService;
import org.openmrs.api.context.Context;
import org.openmrs.module.bahmniemrapi.document.contract.VisitDocumentRequest;
import org.openmrs.module.bahmniemrapi.document.contract.VisitDocumentResponse;
import org.openmrs.module.bahmniemrapi.document.service.VisitDocumentService;
import org.openmrs.module.bahmniemrapi.visitlocation.BahmniVisitLocationService;
import org.openmrs.module.webservices.rest.web.RestConstants;
import org.openmrs.module.webservices.rest.web.annotation.WSDoc;
import org.openmrs.module.webservices.rest.web.v1_0.controller.BaseRestController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;

@Controller
public class VisitDocumentController extends BaseRestController {
    private final String baseVisitDocumentUrl = "/rest/" + RestConstants.VERSION_1 + "/bahmnicore/visitDocument";
    @Autowired
    private VisitDocumentService visitDocumentService;

    @Autowired
    private PatientDocumentService patientDocumentService;

    @Autowired
    private BahmniVisitLocationService bahmniVisitLocationService;

    @Autowired
    @Qualifier("adminService")
    private AdministrationService administrationService;

    @RequestMapping(method = RequestMethod.POST, value = baseVisitDocumentUrl)
    @WSDoc("Save Patient Document")
    @ResponseBody
    public VisitDocumentResponse save(@RequestBody VisitDocumentRequest visitDocumentUpload) {
        String visitLocation = bahmniVisitLocationService.getVisitLocationUuid(visitDocumentUpload.getLocationUuid());
        visitDocumentUpload.setVisitLocationUuid(visitLocation);
        final Visit visit = visitDocumentService.upload(visitDocumentUpload);
        return new VisitDocumentResponse(visit.getUuid());
    }

    @RequestMapping(method = RequestMethod.POST, value = baseVisitDocumentUrl + "/uploadDocument")
    @ResponseBody
    public HashMap<String, String> saveDocument(@RequestBody Document document) {
        Patient patient = Context.getPatientService().getPatientByUuid(document.getPatientUuid());
        String encounterTypeName = document.getEncounterTypeName();
        if (StringUtils.isEmpty(encounterTypeName)) {
            encounterTypeName = administrationService.getGlobalProperty("bahmni.encounterType.default");
        }
        HashMap<String, String> savedDocument = new HashMap<>();
        String url = patientDocumentService.saveDocument(patient.getId(), encounterTypeName, document.getContent(),
                document.getFormat(), document.getFileType());
        savedDocument.put("url", url);
        return savedDocument;
    }

    @RequestMapping(method = RequestMethod.DELETE, value = baseVisitDocumentUrl)
    @ResponseBody
    public void deleteDocument(@RequestParam(value = "filename") String fileName) {
        if (Context.getUserContext().isAuthenticated())
            patientDocumentService.delete(fileName);
    }
}
