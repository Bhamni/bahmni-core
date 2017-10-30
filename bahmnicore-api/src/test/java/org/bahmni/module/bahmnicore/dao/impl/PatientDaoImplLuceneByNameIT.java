package org.bahmni.module.bahmnicore.dao.impl;

import org.bahmni.module.bahmnicore.BaseIntegrationTest;
import org.bahmni.module.bahmnicore.contract.patient.response.PatientResponse;
import org.bahmni.module.bahmnicore.dao.PatientDao;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.Patient;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static java.util.Arrays.asList;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;

public class PatientDaoImplLuceneByNameIT extends BaseIntegrationTest {
    @Autowired
    private PatientDao patientDao;

    @Before
    public void setUp() throws Exception {
        executeDataSet("apiTestData.xml");
        updateSearchIndex();
    }

    @Test
    public void shouldSearchByName() {

        List<PatientResponse> patients = patientDao.getPatients("", "Horatio", null, "city_village", "", 100, 0, null, "", null, null, null, "c36006e5-9fbb-4f20-866b-0ece245615a1", false, false);

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
    public void shouldSearchWithGivenNamePartial() {
        String[] addressResultFields = {"city_village"};
        List<PatientResponse> patients = patientDao.getPatientsUsingLuceneSearch("", "Hora", null, "city_village", "", 100, 0, null, "", null, addressResultFields, null, "c36006e5-9fbb-4f20-866b-0ece245615a1", false, true);
        PatientResponse patient = patients.get(0);
        assertEquals(3, patients.size());
        assertEquals("Horatio", patient.getGivenName());
        assertEquals("Test", patient.getMiddleName());
        assertEquals("Banka", patient.getFamilyName());
        assertEquals(1024, patient.getPersonId());
        assertEquals("GAN200000", patient.getIdentifier());
    }

    @Test
    public void shouldSearchWithGivenName() {
        String[] addressResultFields = {"city_village"};
        List<PatientResponse> patients = patientDao.getPatientsUsingLuceneSearch("", "Horatio", null, "city_village", "", 100, 0, null, "", null, addressResultFields, null, "c36006e5-9fbb-4f20-866b-0ece245615a1", false, true);
        PatientResponse patient = patients.get(0);
        assertEquals(3, patients.size());
        assertEquals("Horatio", patient.getGivenName());
        assertEquals("Test", patient.getMiddleName());
        assertEquals("Banka", patient.getFamilyName());
        assertEquals(1024, patient.getPersonId());
        assertEquals("GAN200000", patient.getIdentifier());
    }

    @Test
    public void shouldSearchAcrossFirstNameAndLastName() {
        String[] addressResultFields = {"city_village"};
        List<PatientResponse> patients = patientDao.getPatientsUsingLuceneSearch("", "Horati Sinha", null, "city_village", "", 100, 0, null, "", null, addressResultFields, null, "c36006e5-9fbb-4f20-866b-0ece245615a1", false, true);
        assertEquals(1, patients.size());
        PatientResponse patient1 = patients.get(0);
        assertEquals("341b4e41-790c-484f-b6ed-71dc8da222db", patient1.getUuid());
        assertEquals("Horatio", patient1.getGivenName());
        assertEquals("Sinha", patient1.getFamilyName());
    }

    @Test
    public void shouldSearchWithMiddleName() {
        String[] addressResultFields = {"city_village"};
        List<PatientResponse> patients = patientDao.getPatientsUsingLuceneSearch("", "Number", null, "city_village", "", 100, 0, null, "", null, addressResultFields, null, "c36006e5-9fbb-4f20-866b-0ece245615a1", false, true);
        assertEquals(2, patients.size());
        PatientResponse patient1 = patients.get(0);
        assertEquals("df877447-6745-45be-b859-403241d991dd", patient1.getUuid());
        assertEquals("Patient", patient1.getGivenName());
        assertEquals("1", patient1.getFamilyName());
    }


    @Test
    public void shouldSearchWithFamilyName() {
        String[] addressResultFields = {"city_village"};
        List<PatientResponse> patients = patientDao.getPatientsUsingLuceneSearch("", "Sinha", null, "city_village", "", 100, 0, null, "", null, addressResultFields, null, "c36006e5-9fbb-4f20-866b-0ece245615a1", false, true);
        assertEquals(2, patients.size());
        PatientResponse patient1 = patients.get(0);
        assertEquals("341b4e41-790c-484f-b6ed-71dc8da222db", patient1.getUuid());
        assertEquals("Horatio", patient1.getGivenName());
        assertEquals("Sinha", patient1.getFamilyName());
    }

    @Test
    public void shouldSearchWithFamilyNamePartial() {
        String[] addressResultFields = {"city_village"};
        List<PatientResponse> patients = patientDao.getPatientsUsingLuceneSearch("", "Sin", null, "city_village", "", 100, 0, null, "", null, addressResultFields, null, "c36006e5-9fbb-4f20-866b-0ece245615a1", false, true);
        assertEquals(2, patients.size());
        PatientResponse patient1 = patients.get(0);
        assertEquals("341b4e41-790c-484f-b6ed-71dc8da222db", patient1.getUuid());
        assertEquals("Horatio", patient1.getGivenName());
        assertEquals("Sinha", patient1.getFamilyName());
    }

    @Test
    public void shouldSearchWithGivenAndMiddle() {
        String[] addressResultFields = {"city_village"};
        List<PatientResponse> patients = patientDao.getPatientsUsingLuceneSearch("", "Peeter Sinha", null, "city_village", "", 100, 0, null, "", null, addressResultFields, null, "c36006e5-9fbb-4f20-866b-0ece245615a1", false, true);
        assertEquals(2, patients.size());
        PatientResponse patient1 = patients.get(0);
        assertEquals("341b4e41-790c-484f-b6ed-71dc8da222db", patient1.getUuid());
        assertEquals("Horatio", patient1.getGivenName());
        assertEquals("Sinha", patient1.getFamilyName());
    }

    @Test
    public void shouldSearchWithGivenAndFamily() {
        String[] addressResultFields = {"city_village"};
        List<PatientResponse> patients = patientDao.getPatientsUsingLuceneSearch("", "Horatio Sinha", null, "city_village", "", 100, 0, null, "", null, addressResultFields, null, "c36006e5-9fbb-4f20-866b-0ece245615a1", false, true);
        assertEquals(1, patients.size());
        PatientResponse patient1 = patients.get(0);
        assertEquals("341b4e41-790c-484f-b6ed-71dc8da222db", patient1.getUuid());
        assertEquals("Horatio", patient1.getGivenName());
        assertEquals("Sinha", patient1.getFamilyName());

    }
    @Test
    public void shouldOnlyReturnPatientsGivenSharedNames(){
        String[] addressResultFields = {"city_village"};
        List<PatientResponse> patients = patientDao.getPatientsUsingLuceneSearch("", "Peeter Sin", null, "city_village", "", 100, 0, null, "", null, addressResultFields, null, "c36006e5-9fbb-4f20-866b-0ece245615a1", false, true);
        assertEquals(2, patients.size());
        PatientResponse patient1 = patients.get(0);
        assertEquals("341b4e41-790c-484f-b6ed-71dc8da222db", patient1.getUuid());
        assertEquals("Horatio", patient1.getGivenName());
        assertEquals("Sinha", patient1.getFamilyName());
    }
    @Test
    public void shouldNotReturnNonPatients(){
        String[] addressResultFields = {"city_village"};
        List<PatientResponse> patients = patientDao.getPatientsUsingLuceneSearch("", "John testlast21", null, "city_village", "", 100, 0, null, "", null, addressResultFields, null, "c36006e5-9fbb-4f20-866b-0ece245615a1", false, true);
        assertEquals(0, patients.size());
    }
}