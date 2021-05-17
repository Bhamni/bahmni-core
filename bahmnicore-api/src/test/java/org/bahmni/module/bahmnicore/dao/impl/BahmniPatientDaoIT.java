package org.bahmni.module.bahmnicore.dao.impl;

import org.bahmni.module.bahmnicore.BaseIntegrationTest;
import org.bahmni.module.bahmnicore.contract.patient.PatientSearchParameters;
import org.bahmni.module.bahmnicore.contract.patient.response.PatientResponse;
import org.bahmni.module.bahmnicore.dao.PatientDao;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.openmrs.Location;
import org.openmrs.Patient;
import org.openmrs.Person;
import org.openmrs.api.LocationService;
import org.openmrs.module.bahmniemrapi.visitlocation.BahmniVisitLocationServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import static java.util.Arrays.asList;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertNull;
import static junit.framework.Assert.assertTrue;

/**
 * This test class  is a replacement for BahmniPatientDaoImplIT (@see BahmniPatientDaoImplIT).
 * This suite uses the prepared statement approach in {@link PatientDaoImpl#getPatients(PatientSearchParameters, Supplier, Supplier)}
 * While many of the older dynamic query has been fixed, the previous PatientDao.getPatients(....) will be deprecated
 */
public class BahmniPatientDaoIT extends BaseIntegrationTest {
    @Autowired
    private PatientDao patientDao;
    @Autowired
    private LocationService locationService;
    @Rule
    public ExpectedException expectedEx = ExpectedException.none();

    @Before
    public void setUp() throws Exception {
        executeDataSet("apiTestData.xml");
    }

    @Test
    public void shouldSearchByPatientPrimaryIdentifier() {
        String[] addressResultFields = {"city_village"};
        PatientSearchParameters searchParameters = PatientSearchParametersBuilder
                .defaultValues()
                .withIdentifier("GAN200001")
                .withAddressFieldName("city_village")
                .withAddressSearchResultFields(addressResultFields)
                .withLoginLocationUuid("c36006e5-9fbb-4f20-866b-0ece245615a1")
                .build();
        List<PatientResponse> patients = fetchPatients(searchParameters);

        assertEquals(1, patients.size());
        PatientResponse patient = patients.get(0);
        assertEquals("341b4e41-790c-484f-b6ed-71dc8da222db", patient.getUuid());
        assertEquals("GAN200001", patient.getIdentifier());
        assertEquals("Horatio", patient.getGivenName());
        assertEquals("Sinha", patient.getFamilyName());
        assertEquals("M", patient.getGender());
        assertEquals("1983-01-30", patient.getBirthDate().toString());
        assertEquals("{ \"city_village\" : \"Ramgarh\"}", patient.getAddressFieldValue());
        assertEquals("2008-08-15 15:57:09.0", patient.getDateCreated().toString());
        assertEquals(null, patient.getDeathDate());
        assertEquals("{\"National ID\":\"NAT100010\"}", patient.getExtraIdentifiers());
    }

    @Test
    public void shouldSearchByPatientExtraIdentifier() {
        String[] addressResultFields = {"city_village"};
        PatientSearchParameters searchParameter = new PatientSearchParameters();
        searchParameter.setIdentifier("100010");
        searchParameter.setName("");
        searchParameter.setAddressFieldName("city_village");
        searchParameter.setAddressFieldValue("");
        searchParameter.setLength(100);
        searchParameter.setStart(0);
        searchParameter.setProgramAttributeFieldValue("");
        searchParameter.setAddressSearchResultFields(addressResultFields);
        searchParameter.setLoginLocationUuid("c36006e5-9fbb-4f20-866b-0ece245615a1");
        searchParameter.setFilterPatientsByLocation(false);
        searchParameter.setFilterOnAllIdentifiers(true);

        List<PatientResponse> patients = fetchPatients(searchParameter);

        assertEquals(1, patients.size());
        PatientResponse patient = patients.get(0);
        assertEquals("341b4e41-790c-484f-b6ed-71dc8da222db", patient.getUuid());
        assertEquals("GAN200001", patient.getIdentifier());
        assertEquals("Horatio", patient.getGivenName());
        assertEquals("Sinha", patient.getFamilyName());
        assertEquals("M", patient.getGender());
        assertEquals("1983-01-30", patient.getBirthDate().toString());
        assertEquals("{ \"city_village\" : \"Ramgarh\"}", patient.getAddressFieldValue());
        assertEquals("2008-08-15 15:57:09.0", patient.getDateCreated().toString());
        assertEquals(null, patient.getDeathDate());
        assertEquals("{\"National ID\":\"NAT100010\"}", patient.getExtraIdentifiers());
    }

    @Test
    public void shouldSearchByOnlyPatientPrimaryIdentifier() {
        String[] addressResultFields = {"city_village"};
        PatientSearchParameters searchParameter = new PatientSearchParameters();
        searchParameter.setIdentifier("100010");
        searchParameter.setName("");
        searchParameter.setAddressFieldName("city_village");
        searchParameter.setAddressFieldValue("");
        searchParameter.setLength(100);
        searchParameter.setStart(0);
        searchParameter.setProgramAttributeFieldValue("");
        searchParameter.setAddressSearchResultFields(addressResultFields);
        searchParameter.setLoginLocationUuid("c36006e5-9fbb-4f20-866b-0ece245615a1");
        searchParameter.setFilterPatientsByLocation(false);
        searchParameter.setFilterOnAllIdentifiers(false); //do not search all identifiers

        List<PatientResponse> patients = fetchPatients(searchParameter);
        assertEquals(0, patients.size());
    }

    @Test
    public void shouldSearchByPartialPatientIdentifier() {
        String[] addressResultFields = {"city_village"};
        PatientSearchParameters searchParameters = new PatientSearchParameters();
        searchParameters.setIdentifier("02");
        searchParameters.setName("");
        searchParameters.setAddressFieldName("city_village");
        searchParameters.setAddressFieldValue("");
        searchParameters.setLength(100);
        searchParameters.setStart(0);
        searchParameters.setProgramAttributeFieldValue("");
        searchParameters.setAddressSearchResultFields(addressResultFields);
        searchParameters.setLoginLocationUuid("c36006e5-9fbb-4f20-866b-0ece245615a1");
        searchParameters.setFilterPatientsByLocation(false);
        searchParameters.setFilterOnAllIdentifiers(false); //do not search all identifiers

        List<PatientResponse> patients = fetchPatients(searchParameters);

        assertEquals(1, patients.size());
        PatientResponse patient = patients.get(0);
        assertEquals("GAN200002", patient.getIdentifier());
        assertNull(patient.getExtraIdentifiers());
    }

    @Test
    public void shouldSearchByName() {
        String[] addressResultFields = {"city_village"};
        PatientSearchParameters searchParameters = new PatientSearchParameters();
        searchParameters.setIdentifier("");
        searchParameters.setName("Horatio");
        searchParameters.setAddressFieldName("city_village");
        searchParameters.setAddressFieldValue("");
        searchParameters.setLength(100);
        searchParameters.setStart(0);
        searchParameters.setProgramAttributeFieldValue("");
        searchParameters.setAddressSearchResultFields(addressResultFields);
        searchParameters.setLoginLocationUuid("c36006e5-9fbb-4f20-866b-0ece245615a1");
        searchParameters.setFilterPatientsByLocation(false);
        searchParameters.setFilterOnAllIdentifiers(false);

        List<PatientResponse> patients = fetchPatients(searchParameters);

        assertEquals(3, patients.size());
        PatientResponse patient1 = patients.get(0);
        PatientResponse patient2 = patients.get(1);
        List<String> uuids = asList("341b4e41-790c-484f-b6ed-71dc8da222db", "86526ed5-3c11-11de-a0ba-001e378eb67a");

        assertTrue(uuids.contains(patient1.getUuid()));
        assertTrue(uuids.contains(patient2.getUuid()));

        assertEquals("Horatio", patient1.getGivenName());
        assertEquals("Horatio", patient2.getGivenName());
    }

    @Test
    public void shouldSearchAcrossFirstNameAndLastName() {
        String[] addressResultFields = {"city_village"};
        PatientSearchParameters searchParameters = new PatientSearchParameters();
        searchParameters.setIdentifier("");
        searchParameters.setName("Horati Sinha");
        searchParameters.setAddressFieldName("city_village");
        searchParameters.setAddressFieldValue("");
        searchParameters.setLength(100);
        searchParameters.setStart(0);
        searchParameters.setProgramAttributeFieldValue("");
        searchParameters.setAddressSearchResultFields(addressResultFields);
        searchParameters.setLoginLocationUuid("c36006e5-9fbb-4f20-866b-0ece245615a1");
        searchParameters.setFilterPatientsByLocation(false);
        searchParameters.setFilterOnAllIdentifiers(false);

        List<PatientResponse> patients = fetchPatients(searchParameters);

        assertEquals(1, patients.size());
        PatientResponse patient1 = patients.get(0);
        assertEquals("341b4e41-790c-484f-b6ed-71dc8da222db", patient1.getUuid());
        assertEquals("Horatio", patient1.getGivenName());
        assertEquals("Sinha", patient1.getFamilyName());
    }

    @Test
    public void shouldSearchByVillage() {
        String[] addressResultFields = {"city_village"};
        PatientSearchParameters searchParameters = new PatientSearchParameters();
        searchParameters.setIdentifier("");
        searchParameters.setName("");
        searchParameters.setAddressFieldName("city_village");
        searchParameters.setAddressFieldValue("Ramgarh");
        searchParameters.setLength(100);
        searchParameters.setStart(0);
        searchParameters.setProgramAttributeFieldValue("");
        searchParameters.setAddressSearchResultFields(addressResultFields);
        searchParameters.setLoginLocationUuid("c36006e5-9fbb-4f20-866b-0ece245615a1");
        searchParameters.setFilterPatientsByLocation(false);
        searchParameters.setFilterOnAllIdentifiers(false);

        List<PatientResponse> patients = fetchPatients(searchParameters);

        assertEquals(1, patients.size());
        PatientResponse patient = patients.get(0);
        assertEquals("341b4e41-790c-484f-b6ed-71dc8da222db", patient.getUuid());
        assertEquals("GAN200001", patient.getIdentifier());
        assertEquals("Horatio", patient.getGivenName());
        assertEquals("Sinha", patient.getFamilyName());
        assertEquals("M", patient.getGender());
        assertEquals("1983-01-30", patient.getBirthDate().toString());
        assertEquals("{ \"city_village\" : \"Ramgarh\"}", patient.getAddressFieldValue());
        assertEquals("2008-08-15 15:57:09.0", patient.getDateCreated().toString());
        assertEquals(null, patient.getDeathDate());
    }

    @Test
    public void shouldSearchByNameAndVillage() {
        String[] addressResultFields = {"city_village"};
        PatientSearchParameters searchParameters = new PatientSearchParameters();
        searchParameters.setIdentifier("");
        searchParameters.setName("Sin");
        searchParameters.setAddressFieldName("city_village");
        searchParameters.setAddressFieldValue("Ramgarh");
        searchParameters.setLength(100);
        searchParameters.setStart(0);
        searchParameters.setProgramAttributeFieldValue("");
        searchParameters.setAddressSearchResultFields(addressResultFields);
        searchParameters.setLoginLocationUuid("c36006e5-9fbb-4f20-866b-0ece245615a1");
        searchParameters.setFilterPatientsByLocation(false);
        searchParameters.setFilterOnAllIdentifiers(false);

        List<PatientResponse> patients = fetchPatients(searchParameters);

        assertEquals(1, patients.size());
        PatientResponse patient = patients.get(0);
        assertEquals("341b4e41-790c-484f-b6ed-71dc8da222db", patient.getUuid());
        assertEquals("GAN200001", patient.getIdentifier());
        assertEquals("Horatio", patient.getGivenName());
        assertEquals("Sinha", patient.getFamilyName());
        assertEquals("M", patient.getGender());

        assertEquals("{ \"city_village\" : \"Ramgarh\"}", patient.getAddressFieldValue());
        assertEquals("2008-08-15 15:57:09.0", patient.getDateCreated().toString());
        assertEquals(null, patient.getDeathDate());
    }

    @Test
    public void shouldSortResultsByCreationDate() {
        PatientSearchParameters searchParameters = new PatientSearchParameters();
        searchParameters.setIdentifier("");
        searchParameters.setName("Sinha");
        searchParameters.setAddressFieldName("city_village");
        searchParameters.setAddressFieldValue("");
        searchParameters.setLength(100);
        searchParameters.setStart(0);
        searchParameters.setProgramAttributeFieldValue("");
        searchParameters.setAddressSearchResultFields(null);
        searchParameters.setLoginLocationUuid("c36006e5-9fbb-4f20-866b-0ece245615a1");
        searchParameters.setFilterPatientsByLocation(false);
        searchParameters.setFilterOnAllIdentifiers(false);

        List<PatientResponse> patients = fetchPatients(searchParameters);

        assertEquals(2, patients.size());
        assertEquals("Sinha", patients.get(0).getFamilyName());
        assertEquals("Sinha", patients.get(0).getFamilyName());
    }

    @Test
    public void shouldReturnResultAfterGivenOffset() throws Exception {
        PatientSearchParameters searchParameters = new PatientSearchParameters();
        searchParameters.setIdentifier("");
        searchParameters.setName("Sinha");
        searchParameters.setAddressFieldName("city_village");
        searchParameters.setAddressFieldValue("");
        searchParameters.setLength(100);
        searchParameters.setStart(1);  //offset 1
        searchParameters.setProgramAttributeFieldValue("");
        searchParameters.setAddressSearchResultFields(null);
        searchParameters.setLoginLocationUuid("c36006e5-9fbb-4f20-866b-0ece245615a1");
        searchParameters.setFilterPatientsByLocation(false);
        searchParameters.setFilterOnAllIdentifiers(false);

        List<PatientResponse> patients = fetchPatients(searchParameters);
        assertEquals(1, patients.size());

        searchParameters.setStart(2); //offset 2
        patients = fetchPatients(searchParameters);
        assertEquals(0, patients.size());
    }

    /**
     * ignored because of the NumberFormatException with h2 db memory
     * Most likely a data setup issue
     * @throws Exception
     */
    @Test
    @Ignore
    public void shouldFetchBasedOnPatientAttributeTypes() throws Exception {
        String[] patientAttributes = {"caste"};
        String[] patientResultFields = {"caste"};

        PatientSearchParameters searchParameters = new PatientSearchParameters();
        searchParameters.setIdentifier("");
        searchParameters.setName("");
        searchParameters.setAddressFieldName("city_village");
        searchParameters.setAddressFieldValue(null);
        searchParameters.setAddressSearchResultFields(null);
        searchParameters.setCustomAttribute("testCaste1");
        searchParameters.setPatientAttributes(patientAttributes);
        searchParameters.setPatientSearchResultFields(patientResultFields);
        searchParameters.setProgramAttributeFieldValue("");
        searchParameters.setLoginLocationUuid("c36006e5-9fbb-4f20-866b-0ece245615a1");
        searchParameters.setLength(100);
        searchParameters.setStart(0);
        searchParameters.setFilterPatientsByLocation(false);
        searchParameters.setFilterOnAllIdentifiers(false);

        List<PatientResponse> patients = fetchPatients(searchParameters);

        assertEquals(1, patients.size());
    }

    @Test
    public void shouldThrowErrorWhenPatientAttributesIsNotPresent() throws Exception {
        String[] patientAttributes = {"caste","nonExistingAttribute"};
        expectedEx.expect(IllegalArgumentException.class);
        expectedEx.expectMessage("Invalid Attribute In Patient Attributes [caste, nonExistingAttribute]");

        PatientSearchParameters searchParameters = new PatientSearchParameters();
        searchParameters.setIdentifier("");
        searchParameters.setName("");
        searchParameters.setCustomAttribute("testCaste1");
        searchParameters.setAddressFieldName("city_village");
        searchParameters.setAddressFieldValue(null);
        searchParameters.setLength(100);
        searchParameters.setStart(0);
        searchParameters.setPatientAttributes(patientAttributes);
        searchParameters.setProgramAttributeFieldValue("");
        searchParameters.setAddressSearchResultFields(null);
        searchParameters.setLoginLocationUuid("c36006e5-9fbb-4f20-866b-0ece245615a1");
        searchParameters.setPatientSearchResultFields(null);
        searchParameters.setFilterPatientsByLocation(false);
        searchParameters.setFilterOnAllIdentifiers(false);

        List<PatientResponse> patients = fetchPatients(searchParameters);
    }


    @Test
    public void shouldThrowErrorWhenPatientAddressIsNotPresent() throws Exception {
        PatientSearchParameters searchParameters = new PatientSearchParameters();
        searchParameters.setIdentifier("");
        searchParameters.setName("");
        searchParameters.setAddressFieldName("nonExistingAddressField");
        searchParameters.setAddressFieldValue(null);
        searchParameters.setAddressSearchResultFields(null);
        searchParameters.setCustomAttribute("testCaste1");
        searchParameters.setPatientAttributes(new String[]{"caste"});
        searchParameters.setPatientSearchResultFields(new String[]{"caste"});
        searchParameters.setProgramAttributeFieldValue("");
        searchParameters.setLoginLocationUuid("c36006e5-9fbb-4f20-866b-0ece245615a1");
        searchParameters.setLength(100);
        searchParameters.setStart(0);
        searchParameters.setFilterPatientsByLocation(false);
        searchParameters.setFilterOnAllIdentifiers(false);

        expectedEx.expect(IllegalArgumentException.class);
        expectedEx.expectMessage("Invalid address parameter");

        List<PatientResponse> patients = fetchPatients(searchParameters);

    }
    @Test
    public void shouldFetchPatientsWithPartialIdentifierMatch() throws Exception {
        List<Patient> patients = patientDao.getPatients("300001", false);
        assertEquals(2, patients.size());
        List<Person> persons = new ArrayList<>();
        Person person1 = new Person();
        Person person2 = new Person();
        person1.setUuid("df877447-6745-45be-b859-403241d991dd");
        person2.setUuid("df888447-6745-45be-b859-403241d991dd");
        persons.add(person1);
        persons.add(person2);
        assertTrue(persons.contains(patients.get(0)));
        assertTrue(persons.contains(patients.get(1)));
    }

    @Test
    public void shouldReturnEmptyListForNoIdentifierMatch() throws Exception {
        List<Patient> patients = patientDao.getPatients("3000001", false);
        assertEquals(0, patients.size());
    }

    @Test
    public void shouldFetchPatientsByProgramAttributes() {
        PatientSearchParameters searchParameters = PatientSearchParametersBuilder
                .defaultValues()
                .withAddressFieldName("city_village")
                .withProgramAttributeFieldName("stage")
                .withProgramAttributeFieldValue("Stage1")
                .withLoginLocationUuid("c36006e5-9fbb-4f20-866b-0ece245615a1")
                .build();

        List<PatientResponse> patients = fetchPatients(searchParameters);

        assertEquals(1, patients.size());
        PatientResponse response = patients.get(0);
        assertEquals("GAN200002",response.getIdentifier());
        assertEquals("John",response.getGivenName());
        assertEquals("{\"stage\":\"Stage1\"}",response.getPatientProgramAttributeValue());
    }

    @Test
    public void shouldThrowErrorWhenProgramAttributesIsNotPresent() {
        expectedEx.expect(IllegalArgumentException.class);
        expectedEx.expectMessage("Invalid Program Attribute");
        PatientSearchParameters searchParameters = PatientSearchParametersBuilder
                .defaultValues()
                .withAddressFieldName("city_village")
                .withProgramAttributeFieldName("nonExistingAttribute")
                .withProgramAttributeFieldValue("Stage1")
                .withLoginLocationUuid("c36006e5-9fbb-4f20-866b-0ece245615a1")
                .build();
        fetchPatients(searchParameters);
    }

    @Test
    @Ignore //ignored because of the NumberFormatException with h2 db memory
    public void shouldFetchPatientsByAllSearchParametersExceptIdentifier(){
        String[] addressResultFields = {"city_village"};
        String[] patientResultFields = {"caste"};

        PatientSearchParameters searchParameters = PatientSearchParametersBuilder
                .defaultValues()
                .withName("John")
                .withAddressFieldName("city_village")
                .withAddressFieldValue("Bilaspur")
                .withAddressSearchResultFields(addressResultFields)
                //.withCustomAttribute("testCaste1")
                .withPatientAttributes(new String[]{"caste", "givenNameLocal"})
                .withPatientSearchResultFields(patientResultFields)
                .withProgramAttributeFieldName("stage")
                .withProgramAttributeFieldValue("Stage1")
                .withLoginLocationUuid("c36006e5-9fbb-4f20-866b-0ece245615a1")
                .build();

        List<PatientResponse> patients = fetchPatients(searchParameters);

        //List<PatientResponse> patients = patientDao.getPatients("", "John", "testCaste1", "city_village", "Bilaspur", 100, 0, new String[]{"caste","givenNameLocal"},"Stage1","stage",addressResultFields,patientResultFields, "c36006e5-9fbb-4f20-866b-0ece245615a1", false, false);
        assertEquals(1, patients.size());
        PatientResponse response = patients.get(0);
        assertEquals("GAN200002",response.getIdentifier());
        assertEquals("df8ae447-6745-45be-b859-403241d9913d",response.getUuid());
        assertEquals(1026,response.getPersonId());
        assertEquals("GAN200002",response.getIdentifier());
        assertEquals("{ \"city_village\" : \"Bilaspur\"}",response.getAddressFieldValue());
        assertEquals("John",response.getGivenName());
        assertEquals("Peeter",response.getMiddleName());
        assertEquals("Sinha",response.getFamilyName());
        assertEquals("F",response.getGender());
        assertEquals("{\"stage\":\"Stage1\"}",response.getPatientProgramAttributeValue());
    }


    @Test
    @Ignore
    public void shouldFetchPatientsByCodedConcepts(){

        List<PatientResponse> patients = patientDao.getPatients("", "John", "testCaste1", "city_village", "Bilaspur", 100, 0, new String[]{"caste"}, "Fac", "facility",null,null, "c36006e5-9fbb-4f20-866b-0ece245615a1", false, false);
        assertEquals(1, patients.size());
        PatientResponse response = patients.get(0);
        assertEquals("GAN200002",response.getIdentifier());
        assertEquals("df8ae447-6745-45be-b859-403241d9913d",response.getUuid());
        assertEquals(1026,response.getPersonId());
        assertEquals("GAN200002",response.getIdentifier());
        assertEquals("Bilaspur",response.getAddressFieldValue());
        assertEquals("John",response.getGivenName());
        assertEquals("Peeter",response.getMiddleName());
        assertEquals("Sinha",response.getFamilyName());
        assertEquals("F",response.getGender());
        assertEquals("{\"caste\":\"testCaste1\"}",response.getCustomAttribute());
        assertEquals("{\"facility\":\"Facility1, City1, Country1\"}",response.getPatientProgramAttributeValue());
    }

    @Test
    public void shouldFetchPatientsByOnlyOneProgramAttribute() {
        String[] addressResultFields = {"city_village"};
        PatientSearchParameters searchParameters = PatientSearchParametersBuilder
                .defaultValues()
                .withName("John")
                .withAddressSearchResultFields(addressResultFields)
                .withProgramAttributeFieldName("stage")
                .withProgramAttributeFieldValue("Stage1")
                .withLoginLocationUuid("c36006e5-9fbb-4f20-866b-0ece245615a1")
                .build();
        List<PatientResponse> patients = fetchPatients(searchParameters);
        assertEquals(1, patients.size());
        PatientResponse response = patients.get(0);
        assertEquals("GAN200002",response.getIdentifier());
        assertEquals("df8ae447-6745-45be-b859-403241d9913d",response.getUuid());
        assertEquals(1026,response.getPersonId());
        assertEquals("GAN200002",response.getIdentifier());
        assertEquals("{ \"city_village\" : \"Bilaspur\"}",response.getAddressFieldValue());
        assertEquals("John",response.getGivenName());
        assertEquals("Peeter",response.getMiddleName());
        assertEquals("Sinha",response.getFamilyName());
        assertEquals("F",response.getGender());
        assertEquals("{\"stage\":\"Stage1\"}",response.getPatientProgramAttributeValue());
    }

    @Test
    public void shouldSearchByPatientIdentifierWithAttributes() {
        PatientSearchParameters searchParameters = PatientSearchParametersBuilder
                .defaultValues()
                .withName("John")
                .withAddressSearchResultFields(new String[] {"city_village"})
                .withLoginLocationUuid("c36006e5-9fbb-4f20-866b-0ece245615a1")
                .build();
        List<PatientResponse> patients = fetchPatients(searchParameters);
        assertEquals(2, patients.size());
    }

    @Test
    public void shouldReturnAdmissionStatus() throws Exception {
        PatientSearchParameters searchParameters = PatientSearchParametersBuilder
                .defaultValues()
                .withIdentifier("200000")
                .withAddressSearchResultFields(new String[] {"city_village"})
                .withLoginLocationUuid("c36006e5-9fbb-4f20-866b-0ece245615a1")
                .build();
        List<PatientResponse> patients = fetchPatients(searchParameters);
        assertEquals(1, patients.size());
        PatientResponse patient200000 = patients.get(0);
        assertFalse(patient200000.getHasBeenAdmitted());

        searchParameters.setIdentifier("200002");
        searchParameters.setLoginLocationUuid("8d6c993e-c2cc-11de-8d13-0040c6dffd0f");
        patients = fetchPatients(searchParameters);
        assertEquals(1, patients.size());
        PatientResponse patient200003 = patients.get(0);
        assertTrue(patient200003.getHasBeenAdmitted());
    }

    @Test
    @Ignore //ignored because of the NumberFormatException with h2 db memory
    public void shouldReturnAddressAndPatientAttributes() throws Exception{
        String[] addressResultFields = {"address3"};
        String[] patientResultFields = {"middleNameLocal"  ,  "familyNameLocal" ,"givenNameLocal"};
        List<PatientResponse> patients = patientDao.getPatients("GAN200002", null, null, null, null, 100, 0, new String[]{"caste","givenNameLocal"},null,null,addressResultFields,patientResultFields, "c36006e5-9fbb-4f20-866b-0ece245615a1", false, false);
        assertEquals(1, patients.size());
        PatientResponse patient200002 = patients.get(0);
        assertTrue("{\"givenNameLocal\":\"ram\",\"middleNameLocal\":\"singh\",\"familyNameLocal\":\"gond\"}".equals(patient200002.getCustomAttribute()));
        assertTrue("{ \"address3\" : \"Dindori\"}".equals(patient200002.getAddressFieldValue()));
    }

    @Test
    public void shouldSearchPatientByNameWithSingleQuote() throws Exception {
        PatientSearchParameters searchParameters = PatientSearchParametersBuilder
                .defaultValues()
                .withName("na'me")
                .withAddressSearchResultFields(null)
                .withLoginLocationUuid("c36006e5-9fbb-4f20-866b-0ece245615a1")
                .build();
        List<PatientResponse> patients = fetchPatients(searchParameters);
        PatientResponse patient = patients.get(0);
        assertEquals(1, patients.size());
        assertEquals("na'me",patient.getFamilyName());

    }

    @Test
    public void shouldSearchPatientByNameWithOneSingleQuoteInSearchString() throws Exception {
        PatientSearchParameters searchParameters = PatientSearchParametersBuilder
                .defaultValues()
                .withName("'")
                .withLoginLocationUuid("c36006e5-9fbb-4f20-866b-0ece245615a1")
                .build();
        List<PatientResponse> patients = fetchPatients(searchParameters);
        PatientResponse patientSearchWithJustSingleQuote = patients.get(0);
        assertEquals(1, patients.size());
        assertEquals("na'me",patientSearchWithJustSingleQuote.getFamilyName());
    }

    @Test
    public void shouldSearchPatientNameByMultipleSingleQuotesInSearchString() throws Exception {
        PatientSearchParameters searchParameters = PatientSearchParametersBuilder
                .defaultValues()
                .withName("'''")
                .withLoginLocationUuid("c36006e5-9fbb-4f20-866b-0ece245615a1")
                .build();
        List<PatientResponse> patients = fetchPatients(searchParameters);
        assertEquals(0, patients.size());
    }

    @Test
    public void shouldGiveEmptyResultIfPatientDoesnotExistWithGivenPatientName() throws Exception {
        PatientSearchParameters searchParameters = PatientSearchParametersBuilder
                .defaultValues()
                .withName("ab'me")
                .withLoginLocationUuid("c36006e5-9fbb-4f20-866b-0ece245615a1")
                .build();
        List<PatientResponse> patients = fetchPatients(searchParameters);
        assertEquals(0, patients.size());
    }

    @Test
    public void shouldGiveAllThePatientsIfWeSearchWithPercentile() throws Exception {
        PatientSearchParameters searchParameters = PatientSearchParametersBuilder
                .defaultValues()
                .withName("%")
                .withLoginLocationUuid("c36006e5-9fbb-4f20-866b-0ece245615a1")
                .withStart(0)
                .withLength(20)
                .build();
        List<PatientResponse> patients = fetchPatients(searchParameters);
        assertEquals(13, patients.size());
    }

    @Test
    public void shouldGiveAllThePatientsIfWeSearchWithPercentileAsIdentifier() throws Exception {
        PatientSearchParameters searchParameters = PatientSearchParametersBuilder
                .defaultValues()
                .withIdentifier("%")
                .withLoginLocationUuid("c36006e5-9fbb-4f20-866b-0ece245615a1")
                .withStart(0)
                .withLength(20)
                .build();
        List<PatientResponse> patients = fetchPatients(searchParameters);
        assertEquals(13, patients.size());
    }

    @Test
    public void shouldGiveThePatientsIfWeSearchBySpaceSeperatedString() throws Exception {
        PatientSearchParameters searchParameters = PatientSearchParametersBuilder
                .defaultValues()
                .withName("special character")
                .withLoginLocationUuid("c36006e5-9fbb-4f20-866b-0ece245615a1")
                .build();
        List<PatientResponse> patients = fetchPatients(searchParameters);
        assertEquals(2, patients.size());
    }

    @Test
    @Ignore //ignored because of the NumberFormatException with h2 db memory
    public void shouldFetchBasedOnPatientAttributeTypesWhenThereIsSingleQuoteInPatientAttribute() throws Exception {
        String[] patientAttributes = { "givenNameLocal"};
        String[] patientResultFields = {"caste", "givenNameLocal"}; //fails when "givenNameLocal" a non-concept fielld in  the list.
        String[] addressResultFields = {"address3"};

        PatientSearchParameters searchParameters = PatientSearchParametersBuilder
                .defaultValues()
                .withCustomAttribute("maximus")
                .withPatientAttributes(patientAttributes)
                .withPatientSearchResultFields(patientResultFields)
                .withAddressSearchResultFields(addressResultFields)
                .withLoginLocationUuid("c36006e5-9fbb-4f20-866b-0ece245615a1")
                .build();
        List<PatientResponse> patients = fetchPatients(searchParameters);

        //List<PatientResponse> patients = patientDao.getPatients("", "", "go'nd", null, null, 100, 0, patientAttributes,null,null,addressResultFields, patientResultFields, "c36006e5-9fbb-4f20-866b-0ece245615a1", false, false);

        assertEquals(1, patients.size());

        assertEquals("{\"caste\":\"go'nd\"}", patients.get(0).getCustomAttribute());

        assertTrue("{ \"address3\" : \"Dindori\"}".equals(patients.get(0).getAddressFieldValue()));


        patients = patientDao.getPatients("", "", "'", null, null, 100, 0, patientAttributes,null,null,addressResultFields, patientResultFields, "c36006e5-9fbb-4f20-866b-0ece245615a1", false, false);

        PatientResponse patientWithSingleQuoteInSearch = patients.get(0);

        assertEquals(1, patients.size());
        assertEquals("{\"caste\":\"go'nd\"}", patientWithSingleQuoteInSearch.getCustomAttribute());
        assertTrue("{ \"address3\" : \"Dindori\"}".equals(patientWithSingleQuoteInSearch.getAddressFieldValue()));


        patients = patientDao.getPatients("", "", "'''", null, null, 100, 0, patientAttributes,null,null,addressResultFields, patientResultFields, "c36006e5-9fbb-4f20-866b-0ece245615a1", false, false);

        assertEquals(0, patients.size());
    }

    @Test
    public void shouldFetchPatientsByProgramAttributesWhenThereIsSingleQuoteInProgramAttribute(){
        PatientSearchParameters searchParameters = PatientSearchParametersBuilder
                .defaultValues()
                .withProgramAttributeFieldName("stage")
                .withProgramAttributeFieldValue("Stage'12")
                .withLoginLocationUuid("c36006e5-9fbb-4f20-866b-0ece245615a1")
                .withStart(0)
                .withLength(20)
                .build();
        List<PatientResponse> patients = fetchPatients(searchParameters);
        PatientResponse response = patients.get(0);
        assertEquals(1, patients.size());
        assertEquals("{\"stage\":\"Stage'12\"}",response.getPatientProgramAttributeValue());
    }

    @Test
    public void shouldFetchPatientsByProgramAttributeWhenThereIsJustOneSingleQuoteInSearchString() throws Exception {
        PatientSearchParameters searchParameters = PatientSearchParametersBuilder
                .defaultValues()
                .withProgramAttributeFieldName("stage")
                .withProgramAttributeFieldValue("'")
                .withLoginLocationUuid("c36006e5-9fbb-4f20-866b-0ece245615a1")
                .withStart(0)
                .withLength(20)
                .build();
        List<PatientResponse> patients = fetchPatients(searchParameters);
        PatientResponse response = patients.get(0);
        assertEquals(1, patients.size());
        assertEquals("{\"stage\":\"Stage'12\"}",response.getPatientProgramAttributeValue());
    }

    @Test
    public void shouldFetchPatientsByParogramAttributeWhenThreAreMultipleSingleQuotesInSearchString() throws Exception {
        PatientSearchParameters searchParameters = PatientSearchParametersBuilder
                .defaultValues()
                .withProgramAttributeFieldName("stage")
                .withProgramAttributeFieldValue("''''")
                .withLoginLocationUuid("c36006e5-9fbb-4f20-866b-0ece245615a1")
                .withStart(0)
                .withLength(20)
                .build();
        List<PatientResponse> patients = fetchPatients(searchParameters);
        assertEquals(0, patients.size());
    }

    @Test
    public void shouldFetchPatientsByPatientIdentifierWhenThereIsSingleQuoteInPatientIdentifier(){
        PatientSearchParameters searchParameters = PatientSearchParametersBuilder
                .defaultValues()
                .withIdentifier("51'0003")
                .withLoginLocationUuid("c36006e5-9fbb-4f20-866b-0ece245615a1")
                .withStart(0)
                .withLength(20)
                .build();
        List<PatientResponse> patients = fetchPatients(searchParameters);
        PatientResponse response = patients.get(0);
        assertEquals(1, patients.size());
        assertEquals("SEV51'0003", response.getIdentifier());
    }

    @Test
    public void shouldFetchPatientsByPatientIdentifierWhenThereIsJustOneSingleQuoteInPatientIdentifier() throws Exception {
        PatientSearchParameters searchParameters = PatientSearchParametersBuilder
                .defaultValues()
                .withIdentifier("'")
                .withLoginLocationUuid("c36006e5-9fbb-4f20-866b-0ece245615a1")
                .build();
        List<PatientResponse> patients = fetchPatients(searchParameters);
        PatientResponse response = patients.get(0);
        assertEquals(1, patients.size());
        assertEquals("SEV51'0003", response.getIdentifier());
    }

    @Test
    public void shouldSearchPatientsByPatientIdentifierWhenThereAreMultipleSinglesInSearchString() throws Exception {
        PatientSearchParameters searchParameters = PatientSearchParametersBuilder
                .defaultValues()
                .withIdentifier("'''")
                .withLoginLocationUuid("c36006e5-9fbb-4f20-866b-0ece245615a1")
                .build();
        List<PatientResponse> patients = fetchPatients(searchParameters);
        assertEquals(0, patients.size());
    }

    @Test
    public void shouldNotReturnDuplicatePatientsEvenIfThereAreMultipleVisitsForThePatients() {
        PatientSearchParameters searchParameters = PatientSearchParametersBuilder
                .defaultValues()
                .withName("1058GivenName")
                .withAddressFieldName("city_village")
                .withLoginLocationUuid("c36006e5-9fbb-4f20-866b-0ece245615a1")
                .build();
        List<PatientResponse> patients = fetchPatients(searchParameters);
        assertEquals(1, patients.size());
        PatientResponse patient1 = patients.get(0);
        assertEquals("1058GivenName", patient1.getGivenName());
    }

    @Test
    public void shouldReturnPatientEvenIfThereIsNoVisitForThePatientWhenFilterByVisitLocationIsFalse() {
        PatientSearchParameters searchParameters = PatientSearchParametersBuilder
                .defaultValues()
                .withName("1059NoVisit")
                .withAddressFieldName("city_village")
                .withLoginLocationUuid("8d6c993e-c2cc-11de-8d34-0010c6affd0f")
                .build();
        List<PatientResponse> patients = fetchPatients(searchParameters);
        assertEquals(1, patients.size());
        PatientResponse patient1 = patients.get(0);
        assertEquals("1059NoVisit", patient1.getGivenName());
    }

    @Test
    public void shouldNotReturnPatientIfThereIsNoVisitForThePatientAndFilterByVisitLocationIsTrue() {
        PatientSearchParameters searchParameters = PatientSearchParametersBuilder
                .defaultValues()
                .withName("1059NoVisit")
                .withAddressFieldName("city_village")
                .withLoginLocationUuid("8d6c993e-c2cc-11de-8d34-0010c6affd0f")
                .withFilterPatientsByLocation(true)
                .build();
        List<PatientResponse> patients = fetchPatients(searchParameters);
        assertEquals(0, patients.size());
    }

    @Test
    public void shouldReturnPatientsWithinVisitLocationOfGivenLoginLocationWhenFilterByVisitLocationIsTrue() {
        PatientSearchParameters searchParameters = PatientSearchParametersBuilder
                .defaultValues()
                .withName("someUnique")
                .withAddressFieldName("city_village")
                .withLoginLocationUuid("8d6c993e-c2cc-11de-8d34-0010c6affd0f")
                .withFilterPatientsByLocation(true)
                .build();
        List<PatientResponse> patients = fetchPatients(searchParameters);
        assertEquals(1, patients.size());
        PatientResponse patient = patients.get(0);
        assertEquals("someUniqueName", patient.getGivenName());
    }

    @Test
    public void shouldReturnAllMatchingPatientsIrrespectiveOfVisitsWhenFilterByVisitLocationIsFalse() {
        PatientSearchParameters searchParameters = PatientSearchParametersBuilder
                .defaultValues()
                .withName("someUnique")
                .withAddressFieldName("city_village")
                .withLoginLocationUuid("8d6c993e-c2cc-11de-8d34-0010c6affd0f")
                .build();
        List<PatientResponse> patients = fetchPatients(searchParameters);
        assertEquals(2, patients.size());
        PatientResponse patient1 = patients.get(0);
        PatientResponse patient2 = patients.get(1);
        assertEquals("someUniqueName", patient1.getGivenName());
        assertEquals("someUniqueOtherName", patient2.getGivenName());
    }

    @Test
    public void shouldReturnPatientsWithinVisitLocationWhenLocationProvidedIsChildLocationAndFilterByLocationIsTrue() {
        PatientSearchParameters searchParameters = PatientSearchParametersBuilder
                .defaultValues()
                .withName("someUnique")
                .withAddressFieldName("city_village")
                .withLoginLocationUuid("8d6c993e-c2cc-11de-8d13-0010c6addd0f")
                .withFilterPatientsByLocation(true)
                .build();
        List<PatientResponse> patients = fetchPatients(searchParameters);
        assertEquals(1, patients.size());
        PatientResponse patient = patients.get(0);
        assertEquals("someUniqueName", patient.getGivenName());
    }

    @Test
    public void shouldReturnPatientsWithinTheVisitLocationWhenTheLocationPassedIsVisitLocationAndFilterByVisitLocationIsTrue() {
        PatientSearchParameters searchParameters = PatientSearchParametersBuilder
                .defaultValues()
                .withName("someUnique")
                .withAddressFieldName("city_village")
                .withLoginLocationUuid("8d6c993e-c2cc-11de-8d13-0010c6aff12f")
                .withFilterPatientsByLocation(true)
                .build();
        List<PatientResponse> patients = fetchPatients(searchParameters);
        assertEquals(1, patients.size());
        PatientResponse patient = patients.get(0);
        assertEquals("someUniqueName", patient.getGivenName());
    }

    @Test
    public void shouldReturnPersonAttributeConceptName() throws Exception {
        String[] patientResultFields = {"caste"};
        PatientSearchParameters searchParameters = PatientSearchParametersBuilder
                .defaultValues()
                .withIdentifier("SEV500003")
                .withAddressFieldName("city_village")
                //.withPatientAttributes(patientResultFields)
                .withPatientSearchResultFields(patientResultFields)
                .withLoginLocationUuid("8d6c993e-c2cc-11de-8d13-0010c6addd0f")
                .build();
        List<PatientResponse> patients = fetchPatients(searchParameters);
        assertEquals(1, patients.size());
        PatientResponse patient200002 = patients.get(0);
        assertEquals("{\"caste\":\"General\"}",patient200002.getCustomAttribute());
    }


    /**
     * This test does not seem to really cover the expectations.
     * Ideally, as per expectations this should return all matching
     * ignoring the location is the location uuid is either null or empty string
     * @see BahmniPatientDaoImplIT#shouldReturnAllMatchingPatientsWhenLoginLocationIsNull
     */
    @Test
    public void shouldReturnAllMatchingPatientsWhenLoginLocationIsNull() {
        PatientSearchParameters searchParameters = PatientSearchParametersBuilder
                .defaultValues()
                .withName("someUnique")
                .withAddressFieldName("city_village")
                .withLoginLocationUuid(null)
                .build();
        expectedEx.expect(IllegalArgumentException.class);
        //expectedEx.expectMessage("Invalid Attribute In Patient Attributes [caste, nonExistingAttribute]");
        fetchPatients(searchParameters);
    }

    private Location getVisitLocation(String locationUuid) {
        BahmniVisitLocationServiceImpl bahmniVisitLocationService = new BahmniVisitLocationServiceImpl(locationService);
        return bahmniVisitLocationService.getVisitLocation(locationUuid);
    }

    private List<PatientResponse> fetchPatients(PatientSearchParameters searchParameters) {
        Supplier<Location> fetchLocation = () -> getVisitLocation(searchParameters.getLoginLocationUuid());
        Supplier<List<String>> configuredAddressFields = () -> patientDao.getConfiguredPatientAddressFields();
        return patientDao.getPatients(searchParameters, fetchLocation, configuredAddressFields);
    }

}
