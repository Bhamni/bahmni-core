package org.bahmni.module.bahmnicore.web.v1_0.controller;


import org.apache.commons.beanutils.ConversionException;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.codehaus.jackson.map.ObjectMapper;
import org.hibernate.NonUniqueObjectException;
import org.hibernate.exception.DataException;
import org.openmrs.Patient;
import org.openmrs.PatientIdentifier;
import org.openmrs.Person;
import org.openmrs.Relationship;
import org.openmrs.RelationshipType;
import org.openmrs.api.APIAuthenticationException;
import org.openmrs.api.ValidationException;
import org.openmrs.api.context.Context;
import org.openmrs.api.context.ContextAuthenticationException;
import org.openmrs.module.emrapi.encounter.DateMapper;
import org.openmrs.module.emrapi.patient.EmrPatientProfileService;
import org.openmrs.module.emrapi.patient.PatientProfile;
import org.openmrs.module.idgen.webservices.services.IdentifierSourceServiceWrapper;
import org.openmrs.module.webservices.rest.SimpleObject;
import org.openmrs.module.webservices.rest.web.ConversionUtil;
import org.openmrs.module.webservices.rest.web.RequestContext;
import org.openmrs.module.webservices.rest.web.RestConstants;
import org.openmrs.module.webservices.rest.web.RestUtil;
import org.openmrs.module.webservices.rest.web.api.RestService;
import org.openmrs.module.webservices.rest.web.representation.Representation;
import org.openmrs.module.webservices.rest.web.resource.impl.DelegatingCrudResource;
import org.openmrs.module.webservices.rest.web.resource.impl.DelegatingResourceDescription;
import org.openmrs.module.webservices.rest.web.response.ResourceDoesNotSupportOperationException;
import org.openmrs.module.webservices.rest.web.response.ResponseException;
import org.openmrs.module.webservices.rest.web.v1_0.resource.openmrs1_8.PatientResource1_8;
import org.openmrs.module.webservices.rest.web.v1_0.resource.openmrs1_8.PersonResource1_8;
import org.openmrs.module.webservices.rest.web.v1_0.resource.openmrs1_8.RelationShipTypeResource1_8;
import org.openmrs.module.webservices.rest.web.v1_0.resource.openmrs1_8.RelationshipResource1_8;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Controller for REST web service access to
 * the Search resource.
 */

@Controller
@RequestMapping(value = "/rest/" + RestConstants.VERSION_1 + "/bahmnicore/patientprofile")
public class BahmniPatientProfileResource extends DelegatingCrudResource<PatientProfile> {

    private EmrPatientProfileService emrPatientProfileService;
    private IdentifierSourceServiceWrapper identifierSourceServiceWrapper;

    @Autowired
    public BahmniPatientProfileResource(EmrPatientProfileService emrPatientProfileService, IdentifierSourceServiceWrapper identifierSourceServiceWrapper) {
        this.emrPatientProfileService = emrPatientProfileService;
        this.identifierSourceServiceWrapper = identifierSourceServiceWrapper;
    }

    @RequestMapping(method = RequestMethod.POST)
    @ResponseBody
    public ResponseEntity<Object> create(@RequestHeader(value = "Jump-Accepted", required = false) boolean jumpAccepted, @RequestBody SimpleObject propertiesToCreate) throws Exception {
        List identifiers = ((ArrayList) ((LinkedHashMap) propertiesToCreate.get("patient")).get("identifiers"));
        List<Object> jumpSizes = new ArrayList<>();

        for (final Object patientIdentifier : identifiers) {
            LinkedHashMap identifierProperties = (LinkedHashMap) patientIdentifier;
            Object identifierSource = identifierProperties.get("identifierSourceUuid");

            if (identifierSource != null) {
                String identifierPrefix = String.valueOf(identifierProperties.get("identifierPrefix"));
                String identifierSourceUuid = String.valueOf(identifierProperties.get("identifierSourceUuid"));
                identifierProperties.remove("identifierSourceUuid");
                identifierProperties.remove("identifierPrefix");

                final String identifier = String.valueOf(identifierProperties.get("identifier"));
                boolean isRegistrationIDNumeric = identifier.replace(identifierPrefix, "").matches("[0-9]+");

                if (identifierProperties.get("identifier") != null && !Objects.equals(identifierPrefix, "") && isRegistrationIDNumeric) {
                    long givenRegistrationNumber = Long.parseLong(identifier.replace(identifierPrefix, ""));
                    long latestRegistrationNumber = Long.parseLong(identifierSourceServiceWrapper.getSequenceValueUsingIdentifierSourceUuid(identifierSourceUuid));
                    if (!jumpAccepted) {
                        final long sizeOfJump = givenRegistrationNumber - latestRegistrationNumber;
                        if (sizeOfJump > 0) {
                            jumpSizes.add(new HashMap<String, Object>() {{
                                put("identifierType", ((HashMap) patientIdentifier).get("identifierType"));
                                put("sizeOfJump", sizeOfJump);
                            }});
                        }
                    } else if (latestRegistrationNumber < (givenRegistrationNumber + 1)) {
                        try {
                            identifierSourceServiceWrapper.saveSequenceValueUsingIdentifierSourceUuid(givenRegistrationNumber + 1, identifierSourceUuid);
                        } catch (DataException e) {
                            return getIdentifierErrorMessageResponseEntity();
                        }
                    }
                } else if (identifierProperties.get("identifier") == null) {
                    String generatedIdentifier = identifierSourceServiceWrapper.generateIdentifierUsingIdentifierSourceUuid(identifierSourceUuid, "");
                    identifierProperties.put("identifier", generatedIdentifier);
                }
            }
        }

        if (jumpSizes.size() > 0) {
            return new ResponseEntity<Object>(new ObjectMapper().writeValueAsString(jumpSizes), HttpStatus.PRECONDITION_FAILED);
        }

        PatientProfile delegate = mapForCreatePatient(propertiesToCreate);

        setConvertedProperties(delegate, propertiesToCreate, getCreatableProperties(), true);
        try {
            delegate = emrPatientProfileService.save(delegate);
            setRelationships(delegate);
            return new ResponseEntity<>(ConversionUtil.convertToRepresentation(delegate, Representation.FULL), HttpStatus.OK);
        } catch (ContextAuthenticationException e) {
            return new ResponseEntity<Object>(RestUtil.wrapErrorResponse(e, e.getMessage()), HttpStatus.FORBIDDEN);
        } catch (NonUniqueObjectException e) {
            return new ResponseEntity<Object>(RestUtil.wrapErrorResponse(e, e.getMessage()), HttpStatus.BAD_REQUEST);
        } catch (ValidationException e) {
            return new ResponseEntity<Object>(RestUtil.wrapErrorResponse(e, ""), HttpStatus.BAD_REQUEST);
        } catch (DataIntegrityViolationException e) {
            return new ResponseEntity<Object>(RestUtil.wrapErrorResponse(e, e.getRootCause().getMessage()), HttpStatus.BAD_REQUEST);
        } catch (DataException e) {
            return new ResponseEntity<Object>(RestUtil.wrapErrorResponse(e, e.getMessage()), HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            return new ResponseEntity<Object>(RestUtil.wrapErrorResponse(e, e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private ResponseEntity<Object> getIdentifierErrorMessageResponseEntity() throws IOException {
        Map<String, Object> message = new LinkedHashMap<>();
        message.put("message", "Entered numeric patient identifier is too large");
        Map<String, Object> error = new LinkedHashMap<>();
        error.put("error", message);
        return new ResponseEntity<>(new ObjectMapper().writeValueAsString(error), HttpStatus.BAD_REQUEST);
    }

    @RequestMapping(method = RequestMethod.POST, value = "/{uuid}")
    @ResponseBody
    public ResponseEntity<Object> update(@PathVariable("uuid") String uuid, @RequestBody SimpleObject propertiesToUpdate) throws Exception {
        PatientProfile delegate = null;
        try {
            delegate = mapForUpdatePatient(uuid, propertiesToUpdate);
        } catch (APIAuthenticationException e) {
            return new ResponseEntity<Object>(RestUtil.wrapErrorResponse(e, "User is logged in but doesn't have the relevant privilege "), HttpStatus.FORBIDDEN);
        }
        setConvertedProperties(delegate, propertiesToUpdate, getUpdatableProperties(), true);
        delegate.setRelationships(getRelationships(propertiesToUpdate, delegate.getPatient()));
        try {
            delegate = emrPatientProfileService.save(delegate);
            setRelationships(delegate);
            return new ResponseEntity<>(ConversionUtil.convertToRepresentation(delegate, Representation.FULL), HttpStatus.OK);
        } catch (ContextAuthenticationException e) {
            return new ResponseEntity<Object>(RestUtil.wrapErrorResponse(e, e.getMessage()), HttpStatus.FORBIDDEN);
        } catch (ValidationException e) {
            return new ResponseEntity<Object>(RestUtil.wrapErrorResponse(e, e.getMessage()), HttpStatus.BAD_REQUEST);
        } catch (DataIntegrityViolationException e) {
            return new ResponseEntity<Object>(RestUtil.wrapErrorResponse(e, e.getRootCause().getMessage()), HttpStatus.BAD_REQUEST);
        } catch (DataException e) {
            return new ResponseEntity<Object>(RestUtil.wrapErrorResponse(e, e.getMessage()), HttpStatus.BAD_REQUEST);
        } catch (APIAuthenticationException e) {
            return new ResponseEntity<Object>(RestUtil.wrapErrorResponse(e, "Unable to save. "), HttpStatus.FORBIDDEN);
        } catch (Exception e) {
            return new ResponseEntity<Object>(RestUtil.wrapErrorResponse(e, e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private void setRelationships(PatientProfile patientProfile) {
        Person person = Context.getPersonService().getPersonByUuid(patientProfile.getPatient().getUuid());
        List<Relationship> relationships = Context.getPersonService().getRelationshipsByPerson(person);
        patientProfile.setRelationships(relationships);
    }

    private PatientProfile mapForCreatePatient(SimpleObject propertiesToCreate) {
        final Object patientProperty = propertiesToCreate.get("patient");
        if (propertiesToCreate.get("patient") == null || !(propertiesToCreate.get("patient") instanceof Map)) {
            throw new ConversionException("The patient property is missing");
        }

        PatientProfile delegate = new PatientProfile();
        PatientResource1_8 patientResource1_9 = (PatientResource1_8) Context.getService(RestService.class).getResourceBySupportedClass(Patient.class);
        delegate.setPatient(patientResource1_9.getPatient(new SimpleObject() {{
            putAll((Map<String, Object>) patientProperty);
        }}));
        propertiesToCreate.removeProperty("patient");
        delegate.setRelationships(getRelationships(propertiesToCreate, delegate.getPatient()));
        propertiesToCreate.removeProperty("relationships");
        return delegate;
    }

    private PatientProfile mapForUpdatePatient(String uuid, SimpleObject propertiesToUpdate) {
        if (propertiesToUpdate.get("patient") == null || !(propertiesToUpdate.get("patient") instanceof Map)) {
            throw new ConversionException("The patient property is missing");
        }

        PatientProfile delegate = new PatientProfile();

        PatientResource1_8 patientResource1_9 = (PatientResource1_8) Context.getService(RestService.class).getResourceBySupportedClass(Patient.class);
        Patient patient = patientResource1_9.getPatientForUpdate(uuid, (Map<String, Object>) propertiesToUpdate.get("patient"));
        List<Object> identifiers = (List<Object>) ((Map) propertiesToUpdate.get("patient")).get("identifiers");
        for (Object identifier : identifiers) {
            LinkedHashMap identifierProperties = (LinkedHashMap) identifier;
            identifierProperties.remove("identifierSourceUuid");
            identifierProperties.remove("identifierPrefix");
            PatientIdentifier patientIdentifier = (PatientIdentifier) ConversionUtil.convert(identifierProperties, PatientIdentifier.class);
            patient.addIdentifier(patientIdentifier);
        }
        delegate.setPatient(patient);

        propertiesToUpdate.removeProperty("patient");
        return delegate;
    }

    private List<Relationship> getRelationships(SimpleObject propertiesToCreate, Person currentPerson) {
        Object relationshipsList = propertiesToCreate.get("relationships");
        List<Relationship> relationships = new ArrayList<Relationship>();
        List<Map<String, Object>> relationshipProperties = (List<Map<String, Object>>) relationshipsList;
        for (final Map<String, Object> relationshipProperty : relationshipProperties) {
            String uuid = getValueFromMap(relationshipProperty, "uuid");
            Relationship relationship;
            if (StringUtils.isBlank(uuid)) {
                relationship = createRelationship(currentPerson, relationshipProperty);
            } else {
                relationship = updateRelationship(relationshipProperty);
            }
            relationships.add(relationship);
        }
        return relationships;
    }

    private String getValueFromMap(Map<String, Object> jsonMap, String key) {
        Object value = jsonMap.get(key);
        return ObjectUtils.toString(value);
    }

    private Relationship createRelationship(Person currentPerson, Map<String, Object> relationshipJson) {
        Relationship relationship = new Relationship(currentPerson,
                getPerson((Map<String, Object>) relationshipJson.get("personB")),
                getRelationshipType((Map<String, Object>) relationshipJson.get("relationshipType")));
        relationship.setEndDate(new DateMapper().convertUTCToDate(getValueFromMap(relationshipJson, "endDate")));

        return relationship;
    }

    private Person getPerson(Map<String, Object> personJson) {
        String personUuid = getValueFromMap(personJson, "uuid");

        if (StringUtils.isBlank(personUuid)) {
            throw new ConversionException("The personUuid is not present.");
        }

        return getPersonFromUuid(personUuid);
    }

    private Person getPersonFromUuid(String personUuid) {
        PersonResource1_8 personResource = (PersonResource1_8) Context.getService(RestService.class).getResourceBySupportedClass(Person.class);
        Person person = personResource.getByUniqueId(personUuid);

        if (person == null) {
            throw new ConversionException("The person does not exist.");
        }
        return person;
    }

    private RelationshipType getRelationshipType(Map<String, Object> relationshipTypeJson) {

        String relationshipTypeUuid = getValueFromMap(relationshipTypeJson, "uuid");

        if (StringUtils.isBlank(relationshipTypeUuid)) {
            throw new ConversionException("The relationshipTypeUuid is not present");
        }

        RelationShipTypeResource1_8 relationshipResource = (RelationShipTypeResource1_8) Context.getService(RestService.class).getResourceBySupportedClass(RelationshipType.class);
        RelationshipType relationshipType = relationshipResource.getByUniqueId(relationshipTypeUuid);

        if (relationshipType == null) {
            throw new ConversionException("The relationship type does not exist.");
        }

        return relationshipType;
    }

    private Relationship updateRelationship(final Map<String, Object> relationshipJson) {
        String relationshipUuid = getValueFromMap(relationshipJson, "uuid");

        if (StringUtils.isBlank(relationshipUuid)) {
            throw new ConversionException("The relationshipUuid is not present");
        }

        RelationshipResource1_8 relationshipResource = (RelationshipResource1_8) Context.getService(RestService.class).getResourceBySupportedClass(Relationship.class);
        Relationship relationship = relationshipResource.getByUniqueId(relationshipUuid);

        if (null == relationship) {
            throw new ConversionException("Invalid relationship for relationshipUuid " + relationshipUuid);
        }

        relationshipResource.setConvertedProperties(relationship, relationshipJson, relationshipResource.getUpdatableProperties(), true);

        RelationshipType updatedRelationshipType = getRelationshipType((Map<String, Object>) relationshipJson.get("relationshipType"));
        relationship.setRelationshipType(updatedRelationshipType);

        return relationship;
    }

    @Override
    public PatientProfile getByUniqueId(String s) {
        return null;
    }

    @Override
    protected void delete(PatientProfile patientProfile, String s, RequestContext requestContext) throws ResponseException {

    }

    @Override
    public PatientProfile newDelegate() {
        return null;
    }

    @Override
    public PatientProfile save(PatientProfile patientProfile) {
        return null;
    }

    @Override
    public void purge(PatientProfile patientProfile, RequestContext requestContext) throws ResponseException {

    }

    public DelegatingResourceDescription getCreatableProperties() throws ResourceDoesNotSupportOperationException {
        DelegatingResourceDescription description = new DelegatingResourceDescription();
        description.addProperty("patient", Representation.DEFAULT);
        description.addProperty("image", Representation.DEFAULT);
        description.addProperty("relationships", Representation.DEFAULT);
        return description;
    }

    @Override
    public DelegatingResourceDescription getRepresentationDescription(Representation representation) {
        return null;
    }
}
