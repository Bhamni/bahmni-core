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
import org.openmrs.*;
import org.openmrs.api.context.Context;
import org.openmrs.module.bahmniemrapi.visitlocation.BahmniVisitLocationServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.stream.Stream;

import static java.util.stream.Collectors.reducing;
import static java.util.stream.Collectors.toList;

@Repository
public class PatientDaoImpl implements PatientDao {

    private SessionFactory sessionFactory;

    @Autowired
    public PatientDaoImpl(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
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
        if (!identifier.isEmpty()) {
            return getPatientUsingLuceneSearchIdentifier(identifier, name, customAttribute, addressFieldName, addressFieldValue, length, offset, customAttributeFields, programAttributeFieldValue, programAttributeFieldName, addressSearchResultFields, patientSearchResultFields, loginLocationUuid, filterPatientsByLocation, filterOnAllIdentifiers);
        } else if (!name.isEmpty()) {
            return getPatientsUsingLuceneSearchName(name, length, offset, programAttributeFieldName, addressSearchResultFields, patientSearchResultFields, loginLocationUuid);
        }
        return null;
    }

    private List<PatientResponse> getPatientUsingLuceneSearchIdentifier(String identifier, String name, String customAttribute,
                                                                        String addressFieldName, String addressFieldValue, Integer length,
                                                                        Integer offset, String[] customAttributeFields, String programAttributeFieldValue,
                                                                        String programAttributeFieldName, String[] addressSearchResultFields,
                                                                        String[] patientSearchResultFields, String loginLocationUuid,
                                                                        Boolean filterPatientsByLocation, Boolean filterOnAllIdentifiers) {
        List<PatientIdentifier> patientIdentifiers = getPatientIdentifiers(identifier, filterOnAllIdentifiers, offset, length);
        List<Integer> patientIds = patientIdentifiers.stream().map(patientIdentifier -> patientIdentifier.getPatient().getPatientId()).collect(toList());
        Map<Object, Object> programAttributes = Context.getService(BahmniProgramWorkflowService.class).getPatientProgramAttributeByAttributeName(patientIds, programAttributeFieldName);
        PatientResponseMapper patientResponseMapper = new PatientResponseMapper(Context.getVisitService(), new BahmniVisitLocationServiceImpl(Context.getLocationService()));
        Set<Integer> uniquePatientIds = new HashSet<>();
        List<PatientResponse> patientResponses = patientIdentifiers.stream()
                .map(patientIdentifier -> {
                    Patient patient = patientIdentifier.getPatient();
                    if (!uniquePatientIds.contains(patient.getPatientId())) {
                        PatientResponse patientResponse = patientResponseMapper.map(patient, loginLocationUuid, patientSearchResultFields, addressSearchResultFields,
                                programAttributes.get(patient.getPatientId()));
                        uniquePatientIds.add(patient.getPatientId());
                        return patientResponse;
                    } else
                        return null;
                }).filter(Objects::nonNull)
                .collect(toList());
        return patientResponses;
    }

    private List<PatientResponse> getPatientsUsingLuceneSearchName(String name, Integer length, Integer offset, String programAttributeFieldName, String[] addressSearchResultFields, String[] patientSearchResultFields, String loginLocationUuid) {

        List<PersonName> patientNames = getPatientNames(name, length);
        Set<Integer> uniqueIds = new HashSet<>();
        List<Integer> personIds = patientNames != null ? patientNames.stream()
                .map(patientName -> {
                    int person_id = patientName.getPerson().getId();
                    if (!uniqueIds.contains(person_id) && patientName.getPerson().getIsPatient()) {
                        uniqueIds.add(person_id);
                        return person_id;
                    } else {
                        return null;
                    }
                }).filter(Objects::nonNull).collect(toList()) : null;
        Map<Object, Object> programAttributes = Context.getService(BahmniProgramWorkflowService.class).getPatientProgramAttributeByAttributeName(personIds, programAttributeFieldName);
        PatientResponseMapper patientResponseMapper = new PatientResponseMapper(Context.getVisitService(), new BahmniVisitLocationServiceImpl(Context.getLocationService()));
        Set<Integer> uniquePatientIds = new HashSet<>();
        List<PatientIdentifier> patientIdentifiers = new ArrayList<>();

        for (int personId : personIds) {
            patientIdentifiers.addAll(getPatientIdentifiers(personId, offset, length));
        }
        List<PatientResponse> patientResponses = patientIdentifiers.stream()
                .map(patientIdentifier -> {
                    Patient patient = patientIdentifier.getPatient();
                    if (!uniquePatientIds.contains(patient.getPatientId())) {
                        PatientResponse patientResponse = patientResponseMapper.map(patient, loginLocationUuid, patientSearchResultFields, addressSearchResultFields,
                                programAttributes.get(patient.getPatientId()));
                        uniquePatientIds.add(patient.getPatientId());
                        return patientResponse;
                    } else
                        return null;
                }).filter(Objects::nonNull)
                .collect(toList());
        return patientResponses;
    }

    private List<PatientIdentifier> getPatientIdentifiers(String identifier, Boolean filterOnAllIdentifiers, Integer offset, Integer length) {
        FullTextSession fullTextSession = Search.getFullTextSession(sessionFactory.getCurrentSession());
        QueryBuilder queryBuilder = fullTextSession.getSearchFactory().buildQueryBuilder().forEntity(PatientIdentifier.class).get();
        identifier = identifier.replace('%', '*');
        org.apache.lucene.search.Query identifierQuery = queryBuilder.keyword()
                .wildcard().onField("identifierAnywhere").matching("*" + identifier.toLowerCase() + "*").createQuery();
        org.apache.lucene.search.Query nonVoidedIdentifiers = queryBuilder.keyword().onField("voided").matching(false).createQuery();
        org.apache.lucene.search.Query nonVoidedPatients = queryBuilder.keyword().onField("patient.voided").matching(false).createQuery();

        List<String> identifierTypeNames = getIdentifierTypeNames(filterOnAllIdentifiers);
        BooleanJunction identifierTypeShouldJunction = queryBuilder.bool();
        for (String identifierTypeName :
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

        Sort sort = new Sort(new SortField("identifier", SortField.Type.STRING, false));
        FullTextQuery fullTextQuery = fullTextSession.createFullTextQuery(booleanQuery, PatientIdentifier.class);
        fullTextQuery.setSort(sort);
        fullTextQuery.setFirstResult(offset);
        fullTextQuery.setMaxResults(length);
        return (List<PatientIdentifier>) fullTextQuery.list();
    }

    private List<PersonName> getPatientNames(String name, int length) {
        String[] names = name.split("\\s+");
        switch (names.length) {
            case 0:
                String fields[] = {"givenNameExact", "middleNameExact", "familyNameExact"};
                return getSingleFieldPatients(name, fields, length);
            case 1:
                String fields2[] = {"givenNameExact", "middleNameExact", "familyNameExact"};
                return getSingleFieldPatients(name, fields2, length);
            case 2:
                return getDoubleFieldPatients(name, fieldCombinations(), length);
            case 3:
                return getTripleFieldPatients(name, fieldCombinations3D(), length);
        }
        return null;
    }

    private List<String> fieldCombinations() {
        String fields[] = {"givenNameExact", "middleNameExact", "familyNameExact"};
        List<String> combinations = new ArrayList<>();
        int index = 0;
        for (String field : fields) {
            for (int i = 0; i < fields.length; i++) {
                if (index != i) {
                    StringJoiner joiner = new StringJoiner(",");
                    joiner.add(field).add(fields[i]);
                    combinations.add(joiner.toString());
                }
            }
            index++;
        }
        return combinations;
    }

    private List<String> fieldCombinations3D() {
        String fields[] = {"givenNameExact", "middleNameExact", "familyNameExact"};
        List<String> combinations = new ArrayList<>();
        int index = 0;
        for (String field : fields) {
            for (int i = 0; i < fields.length; i++) {
                StringJoiner joiner = new StringJoiner(",");
                if (index != i) {
                    joiner.add(field).add(fields[i]);
                    for (int k = 0; k < fields.length; k++) {
                        if (index != k && k != i) {
                            joiner.add(fields[k]);
                        }
                    }
                    combinations.add(joiner.toString());
                }
            }
            index++;
        }
        return combinations;
    }

    private List<PersonName> getSingleFieldPatients(String name, String fields[], int length) {
        List<PersonName> patient_names = new ArrayList<>();
        for (String field : fields) {

            FullTextSession fullTextSession = Search.getFullTextSession(sessionFactory.getCurrentSession());
            QueryBuilder queryBuilder = fullTextSession.getSearchFactory().buildQueryBuilder().forEntity(PersonName.class).get();
            org.apache.lucene.search.Query nameQuery = queryBuilder.keyword()
                    .wildcard().onField(field).matching("*" + name.toLowerCase() + "*").createQuery();
            org.apache.lucene.search.Query noVoidedNames = queryBuilder.keyword().onField("voided").matching(false).createQuery();
            org.apache.lucene.search.Query booleanQuery = queryBuilder.bool()
                    .must(nameQuery)
                    .must(noVoidedNames)
                    .createQuery();

            Sort sort = new Sort(new SortField("givenName", SortField.Type.STRING, false));
            FullTextQuery fullTextQuery = fullTextSession.createFullTextQuery(booleanQuery, PersonName.class);
            fullTextQuery.setSort(sort);
            fullTextQuery.setMaxResults(length);
            List<PersonName> nameList = fullTextQuery.list();
            patient_names = Stream.concat(patient_names.stream(), nameList.stream()).collect(toList());
        }
        return patient_names;

    }

    private List<PersonName> getDoubleFieldPatients(String name, List<String> fieldsCombinations, int length) {
        List<PersonName> patient_names = new ArrayList<>();
        name = name.replace(".", "");
        for (String field : fieldsCombinations) {

            String[] fields = field.split(",");
            FullTextSession fullTextSession = Search.getFullTextSession(sessionFactory.getCurrentSession());
            QueryBuilder queryBuilder = fullTextSession.getSearchFactory().buildQueryBuilder().forEntity(PersonName.class).get();
            org.apache.lucene.search.Query nameQuery = queryBuilder.keyword()
                    .wildcard().onField(fields[0]).matching("*" + name.split("\\s+")[0].toLowerCase() + "*").createQuery();
            org.apache.lucene.search.Query secondNameQuery = queryBuilder.keyword()
                    .wildcard().onField(fields[1]).matching("*" + name.split("\\s+")[1].toLowerCase() + "*").createQuery();
            org.apache.lucene.search.Query noVoidedNames = queryBuilder.keyword().onField("voided").matching(false).createQuery();
            org.apache.lucene.search.Query booleanQuery = queryBuilder.bool()
                    .must(nameQuery)
                    .must(secondNameQuery)
                    .must(noVoidedNames)
                    .createQuery();

            Sort sort = new Sort(new SortField("givenName", SortField.Type.STRING, false));
            FullTextQuery fullTextQuery = fullTextSession.createFullTextQuery(booleanQuery, PersonName.class);
            fullTextQuery.setSort(sort);
            fullTextQuery.setMaxResults(length);
            List<PersonName> nameList = fullTextQuery.list();
            patient_names = Stream.concat(patient_names.stream(), nameList.stream()).collect(toList());
        }
        return patient_names;

    }

    private List<PersonName> getTripleFieldPatients(String name, List<String> fieldsCombinations, int length) {
        List<PersonName> patient_names = new ArrayList<>();
        for (String field : fieldsCombinations) {

            String[] fields = field.split(",");
            FullTextSession fullTextSession = Search.getFullTextSession(sessionFactory.getCurrentSession());
            QueryBuilder queryBuilder = fullTextSession.getSearchFactory().buildQueryBuilder().forEntity(PersonName.class).get();
            org.apache.lucene.search.Query nameQuery = queryBuilder.keyword()
                    .wildcard().onField(fields[0]).matching("*" + name.split("\\s+")[0].toLowerCase() + "*").createQuery();
            org.apache.lucene.search.Query secondNameQuery = queryBuilder.keyword()
                    .wildcard().onField(fields[1]).matching("*" + name.split("\\s+")[1].toLowerCase() + "*").createQuery();
            org.apache.lucene.search.Query thirdNameQuery = queryBuilder.keyword()
                    .wildcard().onField(fields[2]).matching("*" + name.split("\\s+")[2].toLowerCase() + "*").createQuery();
            org.apache.lucene.search.Query noVoidedNames = queryBuilder.keyword().onField("voided").matching(false).createQuery();
            org.apache.lucene.search.Query booleanQuery = queryBuilder.bool()
                    .must(nameQuery)
                    .must(secondNameQuery)
                    .must(thirdNameQuery)
                    .must(noVoidedNames)
                    .createQuery();

            Sort sort = new Sort(new SortField("givenName", SortField.Type.STRING, false));
            FullTextQuery fullTextQuery = fullTextSession.createFullTextQuery(booleanQuery, PersonName.class);
            fullTextQuery.setSort(sort);
            fullTextQuery.setMaxResults(length);
            List<PersonName> nameList = fullTextQuery.list();
            patient_names = Stream.concat(patient_names.stream(), nameList.stream()).collect(toList());
        }
        return patient_names;

    }

    private List<PersonName> getPatientNamesByMiddleName(String name, Integer offset, Integer length) {
        FullTextSession fullTextSession = Search.getFullTextSession(sessionFactory.getCurrentSession());
        QueryBuilder queryBuilder = fullTextSession.getSearchFactory().buildQueryBuilder().forEntity(PersonName.class).get();
        //name = name.replace(' ', '*');
        org.apache.lucene.search.Query nameQuery = queryBuilder.keyword()
                .wildcard().onField("middleNameAnywhere").matching(name.toLowerCase() + "*").createQuery();
        org.apache.lucene.search.Query noVoidedNames = queryBuilder.keyword().onField("voided").matching(false).createQuery();
        org.apache.lucene.search.Query booleanQuery = queryBuilder.bool()
                .must(nameQuery)
                .must(noVoidedNames)
                .createQuery();
        Sort sort = new Sort(new SortField("middleName", SortField.Type.STRING, false));
        FullTextQuery fullTextQuery = fullTextSession.createFullTextQuery(booleanQuery, PersonName.class);
        fullTextQuery.setSort(sort);
        fullTextQuery.setFirstResult(offset);
        fullTextQuery.setMaxResults(length);
        return (List<PersonName>) fullTextQuery.list();

    }

    private List<PersonName> getPatientNamesByFamilyName(String name, Integer offset, Integer length) {
        FullTextSession fullTextSession = Search.getFullTextSession(sessionFactory.getCurrentSession());
        QueryBuilder queryBuilder = fullTextSession.getSearchFactory().buildQueryBuilder().forEntity(PersonName.class).get();
        //name = name.replace(' ', '*');
        org.apache.lucene.search.Query nameQuery = queryBuilder.keyword()
                .wildcard().onField("familyNameAnywhere").matching(name.toLowerCase() + "*").createQuery();
        org.apache.lucene.search.Query noVoidedNames = queryBuilder.keyword().onField("voided").matching(false).createQuery();
        org.apache.lucene.search.Query booleanQuery = queryBuilder.bool()
                .must(nameQuery)
                .must(noVoidedNames)
                .createQuery();
        Sort sort = new Sort(new SortField("familyName", SortField.Type.STRING, false));
        FullTextQuery fullTextQuery = fullTextSession.createFullTextQuery(booleanQuery, PersonName.class);
        fullTextQuery.setSort(sort);
        fullTextQuery.setFirstResult(offset);
        fullTextQuery.setMaxResults(length);
        return (List<PersonName>) fullTextQuery.list();

    }

    private List<PatientIdentifier> getPatientIdentifiers(int patient_id, Integer offset, Integer length) {
        FullTextSession fullTextSession = Search.getFullTextSession(sessionFactory.getCurrentSession());
        QueryBuilder queryBuilder = fullTextSession.getSearchFactory().buildQueryBuilder().forEntity(PatientIdentifier.class).get();
        org.apache.lucene.search.Query identifierQuery = queryBuilder.keyword()
                .onField("patient.personId").matching(patient_id).createQuery();
        org.apache.lucene.search.Query nonVoideIdentifiers = queryBuilder.keyword().onField("voided").matching(false).createQuery();
        org.apache.lucene.search.Query isPatient = queryBuilder.keyword().onField("patient.isPatient").matching(true).createQuery();
        org.apache.lucene.search.Query nonVoidedPatients = queryBuilder.keyword().onField("patient.voided").matching(false).createQuery();

        org.apache.lucene.search.Query booleanQuery = queryBuilder.bool()
                .must(identifierQuery)
                .must(nonVoideIdentifiers)
                .must(nonVoidedPatients)
                .must(isPatient)
                .createQuery();

        Sort sort = new Sort(new SortField("identifier", SortField.Type.STRING, false));
        FullTextQuery fullTextQuery = fullTextSession.createFullTextQuery(booleanQuery, PatientIdentifier.class);
        fullTextQuery.setSort(sort);
        fullTextQuery.setFirstResult(offset);
        fullTextQuery.setMaxResults(length);
        return (List<PatientIdentifier>) fullTextQuery.list();
    }

    private List<String> getIdentifierTypeNames(Boolean filterOnAllIdentifiers) {
        List<String> identifierTypeNames = new ArrayList<>();
        addIdentifierTypeName(identifierTypeNames, "bahmni.primaryIdentifierType");
        if (filterOnAllIdentifiers) {
            addIdentifierTypeName(identifierTypeNames, "bahmni.extraPatientIdentifierTypes");
        }
        return identifierTypeNames;
    }

    private void addIdentifierTypeName(List<String> identifierTypeNames, String identifierProperty) {
        String identifierTypes = Context.getAdministrationService().getGlobalProperty(identifierProperty);
        if (StringUtils.isNotEmpty(identifierTypes)) {
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
