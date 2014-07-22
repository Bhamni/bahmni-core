package org.bahmni.module.bahmnicore.service.impl;

import org.apache.log4j.Logger;
import org.bahmni.module.bahmnicore.BahmniCoreApiProperties;
import org.bahmni.module.bahmnicore.contract.patient.response.PatientConfigResponse;
import org.bahmni.module.bahmnicore.contract.patient.response.PatientResponse;
import org.bahmni.module.bahmnicore.dao.BahmniPatientDao;
import org.bahmni.module.bahmnicore.contract.patient.PatientSearchParameters;
import org.bahmni.module.bahmnicore.datamigration.ExecutionMode;
import org.bahmni.module.bahmnicore.mapper.PatientMapper;
import org.bahmni.module.bahmnicore.model.BahmniPatient;
import org.bahmni.module.bahmnicore.service.BahmniPatientService;
import org.bahmni.module.bahmnicore.service.PatientImageService;
import org.openmrs.Concept;
import org.openmrs.Patient;
import org.openmrs.PersonAttributeType;
import org.openmrs.api.APIAuthenticationException;
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
    private BahmniPatientDao bahmniPatientDao;

    @Autowired
    public BahmniPatientServiceImpl(PatientImageService patientImageService,
                                    PatientService patientService, PersonService personService, ConceptService conceptService,
                                    BahmniCoreApiProperties bahmniCoreApiProperties, PatientMapper patientMapper, BahmniPatientDao bahmniPatientDao) {
        this.patientImageService = patientImageService;
        this.patientService = patientService;
        this.bahmniCoreApiProperties = bahmniCoreApiProperties;
        this.personService = personService;
        this.conceptService = conceptService;
        this.patientMapper = patientMapper;
        this.bahmniPatientDao = bahmniPatientDao;
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
    public Patient createPatient(BahmniPatient bahmniPatient) {
        Patient patient = null;
        ExecutionMode executionMode = bahmniCoreApiProperties.getExecutionMode();
        try {
            patient = savePatient(bahmniPatient, patient);
        } catch (APIAuthenticationException e) {
            throw e;
        } catch (RuntimeException e) {
            executionMode.handleSavePatientFailure(e, bahmniPatient);
        }
        return patient;
    }

    private Patient savePatient(BahmniPatient bahmniPatient, Patient patient) {
        patient = patientMapper.map(patient, bahmniPatient);
        Patient savedPatient = patientService.savePatient(patient);
        String patientIdentifier = savedPatient.getPatientIdentifier().toString();
        logger.debug(String.format("[%s] : Patient saved", patientIdentifier));
        patientImageService.saveImage(patientIdentifier, bahmniPatient.getImage());
        return savedPatient;
    }

    @Override
    public List<PatientResponse> search(PatientSearchParameters searchParameters) {
        return bahmniPatientDao.getPatients(searchParameters.getIdentifier(), searchParameters.getName(), searchParameters.getLocalName(), searchParameters.getCityVillage(), searchParameters.getLength(), searchParameters.getStart(), searchParameters.getPatientAttributes());
    }

    private Patient getPatientByUuid(String uuid) {
        return patientService.getPatientByUuid(uuid);
    }

    @Override
    public Patient getPatientByIdentifier(String patientIdentifier){
        return bahmniPatientDao.getPatient(patientIdentifier);

    }
}
