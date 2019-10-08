package org.bahmni.module.bahmnicore.dao.impl;

import org.bahmni.module.bahmnicore.BaseIntegrationTest;
import org.bahmni.module.bahmnicore.contract.patient.response.PatientResponse;
import org.bahmni.module.bahmnicore.dao.PatientDao;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.openmrs.Patient;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static java.util.Arrays.asList;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertNull;
import static junit.framework.Assert.assertTrue;

public class BahmniPatientDaoImplLuceneIT extends BaseIntegrationTest {
    @Autowired
    private PatientDao patientDao;
    @Rule
    public ExpectedException expectedEx = ExpectedException.none();

    @Before
    public void setUp() throws Exception {
        executeDataSet("apiTestData.xml");
        updateSearchIndex();
    }

    @Test
    public void shouldSearchByPatientPrimaryIdentifier() {
        String[] addressResultFields = {"city_village"};
        List<PatientResponse> patients = patientDao.getPatientsUsingLuceneSearch("GAN200001", "", null, "city_village", "", 100, 0, null,"",null,addressResultFields,null, "c36006e5-9fbb-4f20-866b-0ece245615a1", false, false);
        assertEquals(1, patients.size());
        PatientResponse patient = patients.get(0);
        assertEquals("341b4e41-790c-484f-b6ed-71dc8da222db", patient.getUuid());
        assertEquals("GAN200001", patient.getIdentifier());
        assertEquals("Horatio", patient.getGivenName());
        assertEquals("Sinha", patient.getFamilyName());
        assertEquals("M", patient.getGender());
        assertEquals("1983-01-30 00:00:00.0", patient.getBirthDate().toString());
        assertEquals("{\"city_village\" : \"Ramgarh\"}", patient.getAddressFieldValue());
        assertEquals("2006-01-18 00:00:00.0", patient.getDateCreated().toString());
        assertEquals(null, patient.getDeathDate());
        assertEquals("{\"National ID\" : \"NAT100010\"}", patient.getExtraIdentifiers());
    }

    @Test
    public void shouldSearchByPatientExtraIdentifier() {
        String[] addressResultFields = {"city_village"};
        List<PatientResponse> patients = patientDao.getPatientsUsingLuceneSearch("100010", "", null, "city_village", "", 100, 0, null,"",null,addressResultFields,null, "c36006e5-9fbb-4f20-866b-0ece245615a1", false, true);
        assertEquals(1, patients.size());
        PatientResponse patient = patients.get(0);
        assertEquals("341b4e41-790c-484f-b6ed-71dc8da222db", patient.getUuid());
        assertEquals("GAN200001", patient.getIdentifier());
        assertEquals("Horatio", patient.getGivenName());
        assertEquals("Sinha", patient.getFamilyName());
        assertEquals("M", patient.getGender());
        assertEquals("1983-01-30 00:00:00.0", patient.getBirthDate().toString());
        assertEquals("{\"city_village\" : \"Ramgarh\"}", patient.getAddressFieldValue());
        assertEquals("2006-01-18 00:00:00.0", patient.getDateCreated().toString());
        assertEquals(null, patient.getDeathDate());
        assertEquals("{\"National ID\" : \"NAT100010\"}", patient.getExtraIdentifiers());
    }

    @Test
    public void shouldSearchByPartialPatientIdentifier() {
        List<PatientResponse> patients = patientDao.getPatientsUsingLuceneSearch("02", "", null, "city_village", "", 100, 0, null,"",null,null,null, "c36006e5-9fbb-4f20-866b-0ece245615a1", false, false);
        assertEquals(1, patients.size());
        PatientResponse patient = patients.get(0);

        assertEquals("GAN200002", patient.getIdentifier());
        assertNull(patient.getExtraIdentifiers());
    }

    @Test
    public void shouldReturnResultAfterGivenOffset() throws Exception {
        List<PatientResponse> patients = patientDao.getPatientsUsingLuceneSearch("300001", "", null, "city_village", "", 100, 1, null,"",null,null,null, "c36006e5-9fbb-4f20-866b-0ece245615a1", false, false);
        assertEquals(1, patients.size());

        patients = patientDao.getPatientsUsingLuceneSearch("300001", "", null, "city_village", "", 100, 2, null,"",null,null,null, "c36006e5-9fbb-4f20-866b-0ece245615a1", false, false);
        assertEquals(0, patients.size());
    }

    @Test
    public void shouldThrowErrorWhenPatientAttributesIsNotPresent() throws Exception {
        String[] patientAttributes = {"caste","nonExistingAttribute"};
        expectedEx.expect(IllegalArgumentException.class);
        expectedEx.expectMessage("Invalid Attribute In Patient Attributes [caste, nonExistingAttribute]");
        List<PatientResponse> patients = patientDao.getPatientsUsingLuceneSearch("", "", "testCaste1", "city_village", null, 100, 0, patientAttributes, "", null, null, null, "c36006e5-9fbb-4f20-866b-0ece245615a1", false, false);

    }

    @Test
    public void shouldThrowErrorWhenPatientAddressIsNotPresent() throws Exception {
        String[] patientAttributes = {"caste"};
        String addressField = "nonExistingAddressFiled";
        expectedEx.expect(IllegalArgumentException.class);
        expectedEx.expectMessage("Invalid Address Filed nonExistingAddressFiled");
        List<PatientResponse> patients = patientDao.getPatientsUsingLuceneSearch("", "", "testCaste1", addressField, null, 100, 0, patientAttributes, "", null, null, null, "c36006e5-9fbb-4f20-866b-0ece245615a1", false, false);

    }

    @Test
    public void shouldThrowErrorWhenProgramAttributesIsNotPresent() {
        String nonExistingAttribute = "nonExistingAttribute";
        expectedEx.expect(IllegalArgumentException.class);
        expectedEx.expectMessage("Invalid Program Attribute nonExistingAttribute");
        patientDao.getPatientsUsingLuceneSearch("", "", "", "city_village", null, 100, 0, null, "Stage1",nonExistingAttribute, null, null, "c36006e5-9fbb-4f20-866b-0ece245615a1", false, false);

    }

    @Test
    public void shouldReturnAdmissionStatus() throws Exception{
        List<PatientResponse> patients = patientDao.getPatientsUsingLuceneSearch("200000", null, null, "city_village", null, 10, 0, null, null, null,null,null, "c36006e5-9fbb-4f20-866b-0ece245615a1", false, false);
        assertEquals(1, patients.size());
        PatientResponse patient200000 = patients.get(0);
        assertFalse(patient200000.getHasBeenAdmitted());

        patients = patientDao.getPatientsUsingLuceneSearch("200002", null, null, "city_village", null, 10, 0, null, null, null,null,null, "8d6c993e-c2cc-11de-8d13-0040c6dffd0f", false, false);
        assertEquals(1, patients.size());
        PatientResponse patient200003 = patients.get(0);
        assertTrue(patient200003.getHasBeenAdmitted());
    }

    @Test
    public void shouldReturnAddressAndPatientAttributes() throws Exception{
        String[] addressResultFields = {"address3"};
        String[] patientResultFields = {"middleNameLocal"  ,  "familyNameLocal" ,"givenNameLocal"};
        List<PatientResponse> patients = patientDao.getPatientsUsingLuceneSearch("GAN200002", null, null, null, null, 100, 0, new String[]{"caste","givenNameLocal"},null,null,addressResultFields,patientResultFields, "c36006e5-9fbb-4f20-866b-0ece245615a1", false, false);
        assertEquals(1, patients.size());
        PatientResponse patient200002 = patients.get(0);
        assertTrue("{\"middleNameLocal\" : \"singh\",\"familyNameLocal\" : \"gond\",\"givenNameLocal\" : \"ram\"}".equals(patient200002.getCustomAttribute()));
        assertTrue("{\"address3\" : \"Dindori\"}".equals(patient200002.getAddressFieldValue()));
    }

    @Test
    public void shouldGiveAllThePatientsIfWeSearchWithPercentileAsIdentifier() throws Exception {
        List<PatientResponse> patients = patientDao.getPatientsUsingLuceneSearch("%", null, null, null, null, 10, 0, null, null, null, null, null, "c36006e5-9fbb-4f20-866b-0ece245615a1", false, false);

        assertEquals(10, patients.size());
    }

    @Test
    public void shouldFetchPatientsByPatientIdentifierWhenThereIsSingleQuoteInPatientIdentifier(){
        List<PatientResponse> patients = patientDao.getPatientsUsingLuceneSearch("51'0003", "", "", null, null, 100, 0, null,null, null,null,null, "c36006e5-9fbb-4f20-866b-0ece245615a1", false, false);

        PatientResponse response = patients.get(0);

        assertEquals(1, patients.size());
        assertEquals("SEV51'0003", response.getIdentifier());
    }

    @Test
    public void shouldFetchPatientsByPatientIdentifierWhenThereIsJustOneSingleQuoteInPatientIdentifier() throws Exception {
        List<PatientResponse> patients = patientDao.getPatientsUsingLuceneSearch("'", "", "", null, null, 100, 0, null,null, null,null,null, "c36006e5-9fbb-4f20-866b-0ece245615a1", false, false);

        PatientResponse response = patients.get(0);

        assertEquals(1, patients.size());
        assertEquals("SEV51'0003", response.getIdentifier());
    }

    @Test
    public void shouldSearchPatientsByPatientIdentifierWhenThereAreMultipleSinglesInSearchString() throws Exception {

        List<PatientResponse> patients = patientDao.getPatientsUsingLuceneSearch("'''", "", "", null, null, 100, 0, null,null, null,null,null, "c36006e5-9fbb-4f20-866b-0ece245615a1", false, false);

        assertEquals(0, patients.size());
    }

    @Test
    public void shouldNotReturnDuplicatePatientsEvenIfThereAreMultipleVisitsForThePatients() {
        List<PatientResponse> patients = patientDao.getPatientsUsingLuceneSearch("HOS1225", "", null, "city_village", "", 100, 0, null,"",null,null,null, "8d6c993e-c2cc-11de-8d34-0010c6affd0f", false, false);

        assertEquals(1, patients.size());
        PatientResponse patient1 = patients.get(0);

        assertEquals("1058GivenName", patient1.getGivenName());
    }

    @Test
    public void shouldNotSearchExtraIdentifiersIfFilterOnAllIdenfiersIsFalse() {
        List<PatientResponse> patients = patientDao.getPatientsUsingLuceneSearch("100010", "", null, "city_village", "", 100, 0, null,"", null, null, null, "c36006e5-9fbb-4f20-866b-0ece245615a1", false, false);

        assertEquals(0, patients.size());
    }

    @Test
    public void shouldSearchAllIdentifiersIfFilterOnAllIdentifiersIsTrue() {
        List<PatientResponse> patients = patientDao.getPatientsUsingLuceneSearch("0001", "", null, "city_village", "", 100, 0, null,"", null, null, null, "c36006e5-9fbb-4f20-866b-0ece245615a1", false, true);

        assertEquals(3, patients.size());
        assertEquals("{\"National ID\" : \"NAT100010\"}", patients.get(0).getExtraIdentifiers());
        assertEquals("GAN300001",patients.get(1).getIdentifier());
    }

    @Test
    public void shouldNotReturnPatientsIfFilterOnAllIdenfiersIsTrueButNotAnExtraIdentifier() {
        List<PatientResponse> patients = patientDao.getPatientsUsingLuceneSearch("DLF200001", "", null, "city_village", "", 100, 0, null,"", null, null, null, "c36006e5-9fbb-4f20-866b-0ece245615a1", false, true);

        assertEquals(0, patients.size());
    }

    @Test
    public void shouldNotReturnDuplicatePatientsEvenIfTwoIdentifiersMatches() {
        List<PatientResponse> patients = patientDao.getPatientsUsingLuceneSearch("200006", "", null, "city_village", "", 100, 0, null,"", null, null, null, "c36006e5-9fbb-4f20-866b-0ece245615a1", false, true);

        assertEquals(1, patients.size());
        PatientResponse patient = patients.get(0);
        assertTrue(patient.getIdentifier().contains("200006"));
        assertTrue(patient.getExtraIdentifiers().contains("200006"));
    }

    @Test
    public void shouldSearchByName() {

        List<PatientResponse> patients = patientDao.getPatientsUsingLuceneSearch("", "Horatio", null, "city_village", "", 100, 0, null,"",null,null,null, "c36006e5-9fbb-4f20-866b-0ece245615a1", false, false);

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
        List<PatientResponse> patients = patientDao.getPatientsUsingLuceneSearch("", "Horati Sinha", null, "city_village", "", 100, 0, null,"",null,null,null, "c36006e5-9fbb-4f20-866b-0ece245615a1", false, false);

        assertEquals(1, patients.size());
        PatientResponse patient1 = patients.get(0);
        assertEquals("341b4e41-790c-484f-b6ed-71dc8da222db", patient1.getUuid());
        assertEquals("Horatio", patient1.getGivenName());
        assertEquals("Sinha", patient1.getFamilyName());
    }

    @Test
    public void shouldFetchBasedOnPatientAttributeTypes() throws Exception {
        String[] patientAttributes = { "caste"};
        String[] patientResultFields = {"caste"};
        List<PatientResponse> patients = patientDao.getPatients("", "", "testCaste1", "city_village", null, 100, 0, patientAttributes,"",null,null,patientResultFields, "c36006e5-9fbb-4f20-866b-0ece245615a1", false, false);

        assertEquals(1, patients.size());
    }

    @Test
    public void shouldReturnPatientsWhenMatchingIdentifierAndName(){
        List<PatientResponse> patients = patientDao.getPatientsUsingLuceneSearch("GAN200001", "Horatio Peeter Sinha", "", "city_village", "", 100, 0, null,"",null,null,null, "c36006e5-9fbb-4f20-866b-0ece245615a1", false, false);
        assertEquals(1, patients.size());
        PatientResponse patient = patients.get(0);
        assertEquals("GAN200001", patient.getIdentifier());
        assertEquals("Horatio", patient.getGivenName());
        assertEquals("Sinha", patient.getFamilyName());
    }

    @Test
    public void shouldNotReturnPatientsWhenNotMatchingNameAndIdentifier(){
        List<PatientResponse> patients = patientDao.getPatientsUsingLuceneSearch("GAN200001", "Not Name", "city_village", "city_village", "", 100, 0, null,"",null,null,null, "c36006e5-9fbb-4f20-866b-0ece245615a1", false, false);
        assertEquals(0, patients.size());
    }

    @Test
    public void shouldSearchWhenMatchingIdentifierAndPersonAttribute(){
        List<PatientResponse> patients = patientDao.getPatientsUsingLuceneSearch("GAN200002", "Horati Sinha", "testCaste1", "city_village", "", 100, 0, null,"",null,null,null, "c36006e5-9fbb-4f20-866b-0ece245615a1", false, false);
        assertEquals(1, patients.size());
        PatientResponse patient = patients.get(0);
        assertEquals("GAN200002", patient.getIdentifier());
        //assert for patient attribute
        assertEquals("Horatio", patient.getGivenName());
    }

    @Test
    public void shouldSearchWhenMatchingNameAndPersonAttribute(){
        List<PatientResponse> patients = patientDao.getPatientsUsingLuceneSearch("", "John Peeter Sinha", "testCaste1testCaste1", "city_village", "", 100, 0, null,"",null,null,null, "c36006e5-9fbb-4f20-866b-0ece245615a1", false, false);

        assertEquals(1, patients.size());
        PatientResponse patient = patients.get(0);
        assertEquals("df8ae447-6745-45be-b859-403241d9913d", patient.getUuid());
        assertEquals("GAN200002", patient.getIdentifier());
        assertEquals("John", patient.getGivenName());
        assertEquals("Sinha", patient.getFamilyName());
        assertEquals("F", patient.getGender());
    }

    @Test
    public void shouldNotSearchWhenNotMatchingNameAndPersonAttribute(){
        List<PatientResponse> patients = patientDao.getPatientsUsingLuceneSearch("", "John Peeter Sinha", "testCaste1", "city_village", "", 100, 0, null,"",null,null,null, "c36006e5-9fbb-4f20-866b-0ece245615a1", false, false);
        assertEquals(0, patients.size());

    }

    @Test
    public void shouldReturnPatientsWhenMatchingIdentifierNameAndPersonAttribute(){
        List<PatientResponse> patients = patientDao.getPatientsUsingLuceneSearch("GAN200001", "Horatio Peeter Sinha", "caste1", "city_village", "", 100, 0, null,"",null,null,null, "c36006e5-9fbb-4f20-866b-0ece245615a1", false, false);
        assertEquals(1, patients.size());
        PatientResponse patient = patients.get(0);
        assertEquals("GAN200001", patient.getIdentifier());
    }

    @Test
    public void shouldNotReturnPatientsWhenNotMatchingIdentifierNameAndPersonAttribute(){
        List<PatientResponse> patients = patientDao.getPatientsUsingLuceneSearch("GAN200002", "Horatio Peeter Sinha", "caste1", "city_village", "", 100, 0, null,"",null,null,null, "c36006e5-9fbb-4f20-866b-0ece245615a1", false, false);
        assertEquals(0, patients.size());
    }

}