package org.bahmni.module.bahmnicore.service.impl;

import org.apache.commons.lang3.StringUtils;
import org.bahmni.module.bahmnicore.contract.patient.PatientSearchParameters;
import org.bahmni.module.bahmnicore.contract.patient.response.PatientConfigResponse;
import org.bahmni.module.bahmnicore.contract.patient.response.PatientResponse;
import org.bahmni.module.bahmnicore.dao.PatientDao;
import org.bahmni.module.bahmnicore.service.BahmniPatientService;
import org.openmrs.Concept;
import org.openmrs.Patient;
import org.openmrs.PersonAttributeType;
import org.openmrs.RelationshipType;
import org.openmrs.api.ConceptService;
import org.openmrs.api.PatientService;
import org.openmrs.api.PersonService;
import org.openmrs.api.context.Context;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@Lazy //to toString rid of cyclic dependencies
public class BahmniPatientServiceImpl implements BahmniPatientService {
    private PersonService personService;
    private ConceptService conceptService;
    private PatientDao patientDao;
    private PatientService patientService;

    @Autowired
    public BahmniPatientServiceImpl(PersonService personService, ConceptService conceptService,
                                    PatientDao patientDao, PatientService patientService) {
        this.personService = personService;
        this.conceptService = conceptService;
        this.patientDao = patientDao;
        this.patientService = patientService;
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
        return patientDao.getPatients(searchParameters.getIdentifier(),
                searchParameters.getName(),
                searchParameters.getCustomAttribute(),
                searchParameters.getAddressFieldName(),
                searchParameters.getAddressFieldValue(),
                searchParameters.getLength(),
                searchParameters.getStart(),
                searchParameters.getPatientAttributes(),
                searchParameters.getProgramAttributeFieldValue(),
                searchParameters.getProgramAttributeFieldName(),
                searchParameters.getAddressSearchResultFields(),
                searchParameters.getPatientSearchResultFields(),
                searchParameters.getLoginLocationUuid(),
                searchParameters.getFilterPatientsByLocation(), searchParameters.getFilterOnAllIdentifiers());
    }

    @Override
    public List<PatientResponse> luceneSearch(PatientSearchParameters searchParameters) {
        return patientDao.getPatientsUsingLuceneSearch(searchParameters.getIdentifier(),
                searchParameters.getName(),
                searchParameters.getCustomAttribute(),
                searchParameters.getAddressFieldName(),
                searchParameters.getAddressFieldValue(),
                searchParameters.getLength(),
                searchParameters.getStart(),
                searchParameters.getPatientAttributes(),
                searchParameters.getProgramAttributeFieldValue(),
                searchParameters.getProgramAttributeFieldName(),
                searchParameters.getAddressSearchResultFields(),
                searchParameters.getPatientSearchResultFields(),
                searchParameters.getLoginLocationUuid(),
                searchParameters.getFilterPatientsByLocation(), searchParameters.getFilterOnAllIdentifiers());
    }

    @Override
    public List<Patient> get(String partialIdentifier, boolean shouldMatchExactPatientId) {
        return patientDao.getPatients(partialIdentifier, shouldMatchExactPatientId);
    }

    @Override
    public List<RelationshipType> getByAIsToB(String aIsToB) {
        return patientDao.getByAIsToB(aIsToB);
    }

    @Override
    public List<PatientResponse> luceneHibernateSearch(PatientSearchParameters searchParameters) {
        List<PatientResponse> luceneHibernateResponse = new ArrayList<>();
        if(StringUtils.isNotEmpty(searchParameters.getIdentifier()) || StringUtils.isNotEmpty(searchParameters.getName()) || StringUtils.isNotEmpty(searchParameters.getCustomAttribute())){
            List<PatientResponse> luceneResponse = luceneSearch(searchParameters);

            for(PatientResponse patientResponse: luceneResponse){
                Patient patient = patientService.getPatient(patientResponse.getPersonId());
                searchParameters.setIdentifier(patient.getPatientIdentifier().getIdentifier());
                if(StringUtils.isNotEmpty(searchParameters.getName())){
                    searchParameters.setName("");
                }
                if( StringUtils.isNotEmpty(searchParameters.getCustomAttribute())){
                    searchParameters.setCustomAttribute("");
                }
                luceneHibernateResponse.addAll(search(searchParameters));
            }
        }else{
            luceneHibernateResponse = search(searchParameters);
        }



        return luceneHibernateResponse;
    }

}
