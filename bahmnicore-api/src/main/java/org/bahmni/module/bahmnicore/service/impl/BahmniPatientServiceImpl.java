package org.bahmni.module.bahmnicore.service.impl;

import org.apache.log4j.Logger;
import org.bahmni.module.bahmnicore.BahmniCoreApiProperties;
import org.bahmni.module.bahmnicore.contract.patient.response.PatientConfigResponse;
import org.bahmni.module.bahmnicore.contract.patient.response.PatientResponse;
import org.bahmni.module.bahmnicore.dao.PatientDao;
import org.bahmni.module.bahmnicore.contract.patient.PatientSearchParameters;
import org.bahmni.module.bahmnicore.mapper.PatientMapper;
import org.bahmni.module.bahmnicore.service.BahmniPatientService;
import org.bahmni.module.bahmnicore.service.PatientImageService;
import org.openmrs.Concept;
import org.openmrs.Patient;
import org.openmrs.PersonAttributeType;
import org.openmrs.RelationshipType;
import org.openmrs.api.ConceptService;
import org.openmrs.api.PatientService;
import org.openmrs.api.PersonService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Lazy //to get rid of cyclic dependencies
public class BahmniPatientServiceImpl implements BahmniPatientService {
    private PatientService patientService;
    private PatientImageService patientImageService;
    private BahmniCoreApiProperties bahmniCoreApiProperties;
    private PatientMapper patientMapper;
    private static Logger logger = Logger.getLogger(BahmniPatientServiceImpl.class);
    private PersonService personService;
    private ConceptService conceptService;
    private PatientDao patientDao;

    @Autowired
    public BahmniPatientServiceImpl(PatientImageService patientImageService,
                                    PatientService patientService, PersonService personService, ConceptService conceptService,
                                    BahmniCoreApiProperties bahmniCoreApiProperties, PatientMapper patientMapper, PatientDao patientDao) {
        this.patientImageService = patientImageService;
        this.patientService = patientService;
        this.bahmniCoreApiProperties = bahmniCoreApiProperties;
        this.personService = personService;
        this.conceptService = conceptService;
        this.patientMapper = patientMapper;
        this.patientDao = patientDao;
    }

    @Override
    public PatientConfigResponse getConfig() {
        List<PersonAttributeType> personAttributeTypes = personService.getAllPersonAttributeTypes();

        PatientConfigResponse patientConfigResponse = new PatientConfigResponse();
        for (PersonAttributeType personAttributeType : personAttributeTypes) {
            Concept attributeConcept = null;
            if (personAttributeType.getFormat().equals("org.openmrs.Concept")) {
                attributeConcept = conceptService.getConcept(personAttributeType.getForeignKey());
            }
            patientConfigResponse.addPersonAttribute(personAttributeType, attributeConcept);
        }
        return patientConfigResponse;
    }

    @Override
    public List<PatientResponse> search(PatientSearchParameters searchParameters) {
        return patientDao.getPatients(searchParameters.getIdentifier(), searchParameters.getName(), searchParameters.getCustomAttribute(), searchParameters.getAddressFieldName(), searchParameters.getAddressFieldValue(), searchParameters.getLength(), searchParameters.getStart(), searchParameters.getPatientAttributes());
    }

    @Override
    public List<Patient> get(String partialIdentifier, boolean shouldMatchExactPatientId) {
        return patientDao.getPatients(partialIdentifier, shouldMatchExactPatientId);
    }

    @Override
    public List<RelationshipType> getByAIsToB(String aIsToB) {
        return patientDao.getByAIsToB(aIsToB);
    }

}
