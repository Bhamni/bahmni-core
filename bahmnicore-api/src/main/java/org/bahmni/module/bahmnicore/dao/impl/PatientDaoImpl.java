package org.bahmni.module.bahmnicore.dao.impl;

import org.apache.commons.lang3.StringUtils;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.SortField;
import org.bahmni.module.bahmnicore.contract.patient.mapper.PatientResponseMapper;
import org.bahmni.module.bahmnicore.contract.patient.response.PatientResponse;
import org.bahmni.module.bahmnicore.contract.patient.search.PatientSearchBuilder;
import org.bahmni.module.bahmnicore.dao.PatientDao;
import org.bahmni.module.bahmnicore.model.bahmniPatientProgram.ProgramAttributeType;
import org.bahmni.module.bahmnicore.service.BahmniProgramWorkflowService;
import org.hibernate.Query;
import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Restrictions;
import org.hibernate.search.FullTextQuery;
import org.hibernate.search.FullTextSession;
import org.hibernate.search.Search;
import org.hibernate.search.query.dsl.BooleanJunction;
import org.hibernate.search.query.dsl.QueryBuilder;
import org.openmrs.Patient;
import org.openmrs.PatientIdentifier;
import org.openmrs.PatientIdentifierType;
import org.openmrs.RelationshipType;
import org.openmrs.api.PatientService;
import org.openmrs.api.context.Context;
import org.openmrs.module.bahmniemrapi.visitlocation.BahmniVisitLocationServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import static java.util.stream.Collectors.toList;

@Repository
public class PatientDaoImpl implements PatientDao {

    private SessionFactory sessionFactory;
    private PatientService patientService;
    private BahmniProgramWorkflowService bahmniProgramWorkflowService;

    @Autowired
    public PatientDaoImpl(SessionFactory sessionFactory, PatientService patientService, BahmniProgramWorkflowService bahmniProgramWorkflowService) {
        this.sessionFactory = sessionFactory;
        this.patientService = patientService;
        this.bahmniProgramWorkflowService = bahmniProgramWorkflowService;
    }

    @Override
    public List<PatientResponse> getPatients(String identifier, String name, String customAttribute,
                                             String addressFieldName, String addressFieldValue, Integer length,
                                             Integer offset, String[] customAttributeFields, String programAttributeFieldValue,
                                             String programAttributeFieldName, String[] addressSearchResultFields,
                                             String[] patientSearchResultFields, String loginLocationUuid, Boolean filterPatientsByLocation, Boolean filterOnAllIdentifiers) {

        validateSearchParams(customAttributeFields, programAttributeFieldName, addressFieldName);

        ProgramAttributeType programAttributeType = getProgramAttributeType(programAttributeFieldName);

        SQLQuery sqlQuery = new PatientSearchBuilder(sessionFactory)
                .withPatientName(name)
                .withPatientAddress(addressFieldName, addressFieldValue, addressSearchResultFields)
                .withPatientIdentifier(identifier, filterOnAllIdentifiers)
                .withPatientAttributes(customAttribute, getPersonAttributeIds(customAttributeFields), getPersonAttributeIds(patientSearchResultFields))
                .withProgramAttributes(programAttributeFieldValue, programAttributeType)
                .withLocation(loginLocationUuid, filterPatientsByLocation)
                .buildSqlQuery(length, offset);
        return sqlQuery.list();
    }

    @Override
    public List<PatientResponse> getPatientsUsingLuceneSearch(String identifier, String name, String customAttribute,
                                                              String addressFieldName, String addressFieldValue, Integer length,
                                                              Integer offset, String[] customAttributeFields, String programAttributeFieldValue,
                                                              String programAttributeFieldName, String[] addressSearchResultFields,
                                                              String[] patientSearchResultFields, String loginLocationUuid,
                                                              Boolean filterPatientsByLocation, Boolean filterOnAllIdentifiers) {
        validateSearchParams(customAttributeFields, programAttributeFieldName, addressFieldName);

        Set<Patient> patients = new HashSet<>();

        if(identifier != null && !identifier.isEmpty()){
            patients.addAll(patientService.getPatients(identifier, false, offset, length));
            if(name != null && !name.isEmpty()){
                patients.retainAll( patientService.getPatients(name, false, offset, length));
            }
            if(customAttribute != null && !customAttribute.isEmpty()){
                patients.retainAll( patientService.getPatients(customAttribute, false, offset, length));
            }
        }else if(name != null && !name.isEmpty()){
            patients.addAll(patientService.getPatients(name, false, offset, length));
            if(customAttribute!=null && !customAttribute.isEmpty()){
                patients.retainAll(patientService.getPatients(customAttribute, false, offset, length));
            }
        }else if((customAttribute != null && !customAttribute.isEmpty())){
            patients.addAll(patientService.getPatients(customAttribute, false, offset, length));
        }

        List<Integer> patientIds = patients.stream().map(Patient::getPatientId).collect(toList());
        PatientResponseMapper patientResponseMapper = new PatientResponseMapper(Context.getVisitService(),new BahmniVisitLocationServiceImpl(Context.getLocationService()));
        Map<Object, Object> programAttributes = bahmniProgramWorkflowService.getPatientProgramAttributeByAttributeName(patientIds, programAttributeFieldName);

        List<PatientResponse> patientResponses = patients.stream()
                .map(patient -> {
                    PatientResponse patientResponse = patientResponseMapper.map(patient,loginLocationUuid,patientSearchResultFields,addressSearchResultFields,programAttributes.get(patient.getPatientId()));
                    return patientResponse;
                }).filter(Objects::nonNull)
                .collect(toList());
        return patientResponses;
    }

    private List<PatientIdentifier> getPatientIdentifiers(String identifier, Boolean filterOnAllIdentifiers, Integer offset, Integer length) {
        FullTextSession fullTextSession = Search.getFullTextSession(sessionFactory.getCurrentSession());
        QueryBuilder queryBuilder = fullTextSession.getSearchFactory().buildQueryBuilder().forEntity(PatientIdentifier.class).get();
        identifier = identifier.replace('%','*');
        org.apache.lucene.search.Query identifierQuery = queryBuilder.keyword()
                .wildcard().onField("identifierAnywhere").matching("*" + identifier.toLowerCase() + "*").createQuery();
        org.apache.lucene.search.Query nonVoidedIdentifiers = queryBuilder.keyword().onField("voided").matching(false).createQuery();
        org.apache.lucene.search.Query nonVoidedPatients = queryBuilder.keyword().onField("patient.voided").matching(false).createQuery();
    
        List<String> identifierTypeNames = getIdentifierTypeNames(filterOnAllIdentifiers);

        BooleanJunction identifierTypeShouldJunction = queryBuilder.bool();
        for (String identifierTypeName:
                identifierTypeNames) {
            org.apache.lucene.search.Query identifierTypeQuery = queryBuilder.phrase().onField("identifierType.name").sentence(identifierTypeName).createQuery();
            identifierTypeShouldJunction.should(identifierTypeQuery);
        }

        org.apache.lucene.search.Query booleanQuery = queryBuilder.bool()
                .must(identifierQuery)
                .must(nonVoidedIdentifiers)
                .must(nonVoidedPatients)
                .must(identifierTypeShouldJunction.createQuery())
                .createQuery();

        Sort sort = new Sort( new SortField( "identifier", SortField.Type.STRING, false ) );
        FullTextQuery fullTextQuery = fullTextSession.createFullTextQuery(booleanQuery, PatientIdentifier.class);
        fullTextQuery.setSort(sort);
        fullTextQuery.setFirstResult(offset);
        fullTextQuery.setMaxResults(length);
        return (List<PatientIdentifier>) fullTextQuery.list();
    }
    
    private List<String> getIdentifierTypeNames(Boolean filterOnAllIdentifiers) {
        List<String> identifierTypeNames = new ArrayList<>();
        addIdentifierTypeName(identifierTypeNames,"bahmni.primaryIdentifierType");
        if(filterOnAllIdentifiers){
            addIdentifierTypeName(identifierTypeNames,"bahmni.extraPatientIdentifierTypes");
        }
        return identifierTypeNames;
    }

    private void addIdentifierTypeName(List<String> identifierTypeNames,String identifierProperty) {
        String identifierTypes = Context.getAdministrationService().getGlobalProperty(identifierProperty);
        if(StringUtils.isNotEmpty(identifierTypes)) {
            String[] identifierUuids = identifierTypes.split(",");
            for (String identifierUuid :
                    identifierUuids) {
                PatientIdentifierType patientIdentifierType = Context.getPatientService().getPatientIdentifierTypeByUuid(identifierUuid);
                if (patientIdentifierType != null) {
                    identifierTypeNames.add(patientIdentifierType.getName());
                }
            }
        }
    }

    private void validateSearchParams(String[] customAttributeFields, String programAttributeFieldName, String addressFieldName) {
        List<Integer> personAttributeIds = getPersonAttributeIds(customAttributeFields);
        if (customAttributeFields != null && personAttributeIds.size() != customAttributeFields.length) {
            throw new IllegalArgumentException(String.format("Invalid Attribute In Patient Attributes [%s]", StringUtils.join(customAttributeFields, ", ")));
        }

        ProgramAttributeType programAttributeTypeId = getProgramAttributeType(programAttributeFieldName);
        if (programAttributeFieldName != null && programAttributeTypeId == null) {
            throw new IllegalArgumentException(String.format("Invalid Program Attribute %s", programAttributeFieldName));
        }


        if (!isValidAddressField(addressFieldName)) {
            throw new IllegalArgumentException(String.format("Invalid Address Filed %s", addressFieldName));
        }
    }

    private boolean isValidAddressField(String addressFieldName) {
        if (addressFieldName == null) return true;
        String query = "SELECT DISTINCT COLUMN_NAME FROM information_schema.columns WHERE\n" +
                "LOWER (TABLE_NAME) ='person_address' and LOWER(COLUMN_NAME) IN " +
                "( :personAddressField)";
        Query queryToGetAddressFields = sessionFactory.getCurrentSession().createSQLQuery(query);
        queryToGetAddressFields.setParameterList("personAddressField", Arrays.asList(addressFieldName.toLowerCase()));
        List list = queryToGetAddressFields.list();
        return list.size() > 0;
    }

    private ProgramAttributeType getProgramAttributeType(String programAttributeField) {
        if (StringUtils.isEmpty(programAttributeField)) {
            return null;
        }

        return (ProgramAttributeType) sessionFactory.getCurrentSession().createCriteria(ProgramAttributeType.class).
                add(Restrictions.eq("name", programAttributeField)).uniqueResult();
    }

    private List<Integer> getPersonAttributeIds(String[] patientAttributes) {
        if (patientAttributes == null || patientAttributes.length == 0) {
            return new ArrayList<>();
        }

        String query = "select person_attribute_type_id from person_attribute_type where name in " +
                "( :personAttributeTypeNames)";
        Query queryToGetAttributeIds = sessionFactory.getCurrentSession().createSQLQuery(query);
        queryToGetAttributeIds.setParameterList("personAttributeTypeNames", Arrays.asList(patientAttributes));
        List list = queryToGetAttributeIds.list();
        return (List<Integer>) list;
    }

    @Override
    public Patient getPatient(String identifier) {
        Session currentSession = sessionFactory.getCurrentSession();
        List<PatientIdentifier> ident = currentSession.createQuery("from PatientIdentifier where identifier = :ident").setString("ident", identifier).list();
        if (!ident.isEmpty()) {
            return ident.get(0).getPatient();
        }
        return null;
    }

    @Override
    public List<Patient> getPatients(String patientIdentifier, boolean shouldMatchExactPatientId) {
        if (!shouldMatchExactPatientId) {
            String partialIdentifier = "%" + patientIdentifier;
            Query querytoGetPatients = sessionFactory.getCurrentSession().createQuery(
                    "select pi.patient " +
                            " from PatientIdentifier pi " +
                            " where pi.identifier like :partialIdentifier ");
            querytoGetPatients.setString("partialIdentifier", partialIdentifier);
            return querytoGetPatients.list();
        }

        Patient patient = getPatient(patientIdentifier);
        List<Patient> result = (patient == null ? new ArrayList<Patient>() : Arrays.asList(patient));
        return result;
    }

    @Override
    public List<RelationshipType> getByAIsToB(String aIsToB) {
        Query querytoGetPatients = sessionFactory.getCurrentSession().createQuery(
                "select rt " +
                        " from RelationshipType rt " +
                        " where rt.aIsToB = :aIsToB ");
        querytoGetPatients.setString("aIsToB", aIsToB);
        return querytoGetPatients.list();
    }
}
