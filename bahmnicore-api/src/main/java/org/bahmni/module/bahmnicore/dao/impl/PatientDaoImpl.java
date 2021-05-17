package org.bahmni.module.bahmnicore.dao.impl;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.SortField;
import org.bahmni.module.bahmnicore.contract.patient.PatientSearchParameters;
import org.bahmni.module.bahmnicore.contract.patient.mapper.PatientResponseMapper;
import org.bahmni.module.bahmnicore.contract.patient.response.PatientResponse;
import org.bahmni.module.bahmnicore.contract.patient.search.PatientSearchBuilder;
import org.bahmni.module.bahmnicore.contract.patient.search.PatientSearchQueryBuilder;
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
import org.openmrs.Location;
import org.openmrs.Patient;
import org.openmrs.PatientIdentifier;
import org.openmrs.PatientIdentifierType;
import org.openmrs.PersonAttributeType;
import org.openmrs.RelationshipType;
import org.openmrs.api.context.Context;
import org.openmrs.module.bahmniemrapi.visitlocation.BahmniVisitLocationServiceImpl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Supplier;

import static java.util.stream.Collectors.toList;

public class PatientDaoImpl implements PatientDao {

    public static final int MAX_NGRAM_SIZE = 20;
    private SessionFactory sessionFactory;
    private static final Logger log = Logger.getLogger(PatientDaoImpl.class);

    public PatientDaoImpl(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    private List<String> patientAddressFields = Arrays.asList("country", "state_province", "county_district", "city_village",
            "postal_code", "address1", "address2", "address3",
            "address4", "address5", "address6", "address7", "address8",
            "address9", "address10", "address11", "address12",
            "address13", "address14", "address15");

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
        try {
            return sqlQuery.list();
        } catch (Exception e) {
            log.error("Error occurred while trying to execute patient search query.", e);
            throw new RuntimeException("Error occurred while to perform patient search");
        }
    }


    @Override
    public List<PatientResponse> getPatients(PatientSearchParameters searchParameters, Supplier<Location> visitLocation, Supplier<List<String>> configuredAddressFields) {
        validateSearchParams(searchParameters.getPatientAttributes(), searchParameters.getProgramAttributeFieldName(), searchParameters.getAddressFieldName());
        ProgramAttributeType programAttributeType = getProgramAttributeType(searchParameters.getProgramAttributeFieldName());
        List<String> addressLevelFields = configuredAddressFields.get();
        Location location = visitLocation.get();
        validateLocation(location, searchParameters);

        List<PersonAttributeType> patientAttributes = getPersonAttributes(searchParameters.getPatientAttributes());
        List<PersonAttributeType> patientSearchResultAttributes = getPersonAttributes(searchParameters.getPatientSearchResultFields());

        SQLQuery sqlQuery = new PatientSearchQueryBuilder(sessionFactory)
                .withPatientName(searchParameters.getName())
                .withPatientAddress(searchParameters.getAddressFieldName(), searchParameters.getAddressFieldValue(), searchParameters.getAddressSearchResultFields(), addressLevelFields)
                .withPatientIdentifier(searchParameters.getIdentifier(), searchParameters.getFilterOnAllIdentifiers())
                .withPatientAttributes(searchParameters.getCustomAttribute(),
                        patientAttributes,
                        patientSearchResultAttributes)
                .withProgramAttributes(searchParameters.getProgramAttributeFieldValue(), programAttributeType)
                .withLocation(location, searchParameters.getFilterPatientsByLocation())
                .buildSqlQuery(searchParameters.getLength(), searchParameters.getStart());
        try {
            return sqlQuery.list();
        } catch (Exception e) {
            log.error("Error occurred while trying to execute patient search query.", e);
            throw new RuntimeException("Error occurred while to perform patient search");
        }
    }

    @Override
    public List<String> getConfiguredPatientAddressFields() {
        return this.patientAddressFields;
        /**
         * AbstractEntityPersister aep=((AbstractEntityPersister) sessionFactory.getClassMetadata(PersonAddress.class));
         *         String[] properties=aep.getPropertyNames();
         *         for(int nameIndex=0;nameIndex!=properties.length;nameIndex++){
         *             System.out.println("Property name: "+properties[nameIndex]);
         *             String[] columns=aep.getPropertyColumnNames(nameIndex);
         *             for(int columnIndex=0;columnIndex!=columns.length;columnIndex++){
         *                 System.out.println("Column name: "+columns[columnIndex]);
         *             }
         *         }
         */
    }

    private void validateLocation(Location location, PatientSearchParameters searchParameters) {
        if (searchParameters.getFilterPatientsByLocation() && location == null) {
            log.error(String.format("Invalid parameter Location: %s", searchParameters.getLoginLocationUuid()));
            throw new IllegalArgumentException("Invalid Location specified");
        }
    }

    @Override
    public List<PatientResponse> getPatientsUsingLuceneSearch(String identifier, String name, String customAttribute,
                                                              String addressFieldName, String addressFieldValue, Integer length,
                                                              Integer offset, String[] customAttributeFields, String programAttributeFieldValue,
                                                              String programAttributeFieldName, String[] addressSearchResultFields,
                                                              String[] patientSearchResultFields, String loginLocationUuid,
                                                              Boolean filterPatientsByLocation, Boolean filterOnAllIdentifiers) {

        validateSearchParams(customAttributeFields, programAttributeFieldName, addressFieldName);

        List<PatientIdentifier> patientIdentifiers = getPatientIdentifiers(identifier, filterOnAllIdentifiers, offset, length);
        List<Integer> patientIds = patientIdentifiers.stream().map(patientIdentifier -> patientIdentifier.getPatient().getPatientId()).collect(toList());
        Map<Object, Object> programAttributes = Context.getService(BahmniProgramWorkflowService.class).getPatientProgramAttributeByAttributeName(patientIds, programAttributeFieldName);
        PatientResponseMapper patientResponseMapper = new PatientResponseMapper(Context.getVisitService(),new BahmniVisitLocationServiceImpl(Context.getLocationService()));
        Set<Integer> uniquePatientIds = new HashSet<>();
        List<PatientResponse> patientResponses = patientIdentifiers.stream()
                .map(patientIdentifier -> {
                    Patient patient = patientIdentifier.getPatient();
                    if(!uniquePatientIds.contains(patient.getPatientId())) {
                        PatientResponse patientResponse = patientResponseMapper.map(patient, loginLocationUuid, patientSearchResultFields, addressSearchResultFields,
                                programAttributes.get(patient.getPatientId()));
                        uniquePatientIds.add(patient.getPatientId());
                        return patientResponse;
                    }else
                        return null;
                }).filter(Objects::nonNull)
                .collect(toList());
        return patientResponses;
    }

    private List<PatientIdentifier> getPatientIdentifiers(String identifier, Boolean filterOnAllIdentifiers, Integer offset, Integer length) {
        FullTextSession fullTextSession = Search.getFullTextSession(sessionFactory.getCurrentSession());
        QueryBuilder queryBuilder = fullTextSession.getSearchFactory().buildQueryBuilder().forEntity(PatientIdentifier.class).get();
        identifier = identifier.replace('%','*');
        org.apache.lucene.search.Query identifierQuery;
        if(identifier.length() <= MAX_NGRAM_SIZE) {
            identifierQuery = queryBuilder.keyword()
                    .wildcard().onField("identifierAnywhere").matching("*" + identifier.toLowerCase() + "*").createQuery();
        } else {
            identifierQuery = queryBuilder.keyword()
                    .onField("identifierExact").matching(identifier.toLowerCase()).createQuery();
        }
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
            log.error(String.format("Invalid Patient Attribute(s) specified: [%s]", StringUtils.join(customAttributeFields, ", ")));
            //TODO, do not reveal information
            throw new IllegalArgumentException(String.format("Invalid Attribute In Patient Attributes [%s]", StringUtils.join(customAttributeFields, ", ")));
        }

        ProgramAttributeType programAttributeTypeId = getProgramAttributeType(programAttributeFieldName);
        if (!StringUtils.isBlank(programAttributeFieldName) && programAttributeTypeId == null) {
            log.error("Invalid Program Attribute specified, name: " + programAttributeFieldName);
            throw new IllegalArgumentException("Invalid Program Attribute");
        }


        if (!isValidAddressField(addressFieldName)) {
            log.error("Invalid address field:" + addressFieldName);
            throw new IllegalArgumentException(String.format("Invalid address parameter"));
        }
    }

    /**
     * This should not be querying the information schema at all.
     * Most of the time, the table columns that are fixed in nature should suffice.
     * If not, we can introduce external property.
     * Or worst case use Hibernate mappings to find column names. see {@link #getConfiguredPatientAddressFields()}.
     * @param addressFieldName
     * @return
     */
    private boolean isValidAddressField(String addressFieldName) {
        if (StringUtils.isBlank(addressFieldName)) return true;
        return patientAddressFields.contains(addressFieldName.toLowerCase());
    }

    private ProgramAttributeType getProgramAttributeType(String programAttributeField) {
        if (StringUtils.isEmpty(programAttributeField)) {
            return null;
        }

        return (ProgramAttributeType) sessionFactory.getCurrentSession().createCriteria(ProgramAttributeType.class).
                add(Restrictions.eq("name", programAttributeField)).uniqueResult();
    }

    private List<PersonAttributeType> getPersonAttributes(String[] patientAttributes) {
        if (patientAttributes == null || patientAttributes.length == 0) {
            return new ArrayList<>();
        }
        return sessionFactory.getCurrentSession().createCriteria(PersonAttributeType.class).
                add(Restrictions.in("name", patientAttributes)).list();
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
