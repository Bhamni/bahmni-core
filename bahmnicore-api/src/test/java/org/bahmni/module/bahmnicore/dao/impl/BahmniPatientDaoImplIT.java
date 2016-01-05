package org.bahmni.module.bahmnicore.dao.impl;

import org.bahmni.module.bahmnicore.BaseIntegrationTest;
import org.bahmni.module.bahmnicore.contract.patient.response.PatientResponse;
import org.bahmni.module.bahmnicore.dao.PatientDao;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.openmrs.Patient;
import org.openmrs.Person;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;

import static java.util.Arrays.asList;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;

public class BahmniPatientDaoImplIT extends BaseIntegrationTest {
    @Autowired
    private PatientDao patientDao;

    @Before
    public void setup() throws Exception {
        executeDataSet("apiTestData.xml");
    }

    @Test
    public void shouldSearchByPatientIdentifier() {
        List<PatientResponse> patients = patientDao.getPatients("200001", "GAN", "", null, "city_village", "", 100, 0, null);
        assertEquals(1, patients.size());
        PatientResponse patient = patients.get(0);
        assertEquals("341b4e41-790c-484f-b6ed-71dc8da222db", patient.getUuid());
        assertEquals("GAN200001", patient.getIdentifier());
        assertEquals("Horatio", patient.getGivenName());
        assertEquals("Sinha", patient.getFamilyName());
        assertEquals("M", patient.getGender());
        assertEquals("1983-01-30", patient.getBirthDate().toString());
        assertEquals("Ramgarh", patient.getAddressFieldValue());
        assertEquals("2008-08-15 15:57:09.0", patient.getDateCreated().toString());
        assertEquals(null, patient.getDeathDate());
    }

    @Test
    public void shouldSearchByPartialPatientIdentifier() {
        List<PatientResponse> patients = patientDao.getPatients("02", "GAN", "", null, "city_village", "", 100, 0, null);
        assertEquals(1, patients.size());
        PatientResponse patient = patients.get(0);

        assertEquals("GAN200002", patient.getIdentifier());
    }

    @Test
    public void shouldSearchByName() {

        List<PatientResponse> patients = patientDao.getPatients("", null, "Horatio", null, "city_village", "", 100, 0, null);

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
        List<PatientResponse> patients = patientDao.getPatients("", null, "Horati Sinha", null, "city_village", "", 100, 0, null);

        assertEquals(1, patients.size());
        PatientResponse patient1 = patients.get(0);
        assertEquals("341b4e41-790c-484f-b6ed-71dc8da222db", patient1.getUuid());
        assertEquals("Horatio", patient1.getGivenName());
        assertEquals("Sinha", patient1.getFamilyName());
    }

    @Test
    public void shouldSearchByVillage() {
        List<PatientResponse> patients = patientDao.getPatients("", null, "", null, "city_village", "Ramgarh", 100, 0, null);
        assertEquals(1, patients.size());
        PatientResponse patient = patients.get(0);
        assertEquals("341b4e41-790c-484f-b6ed-71dc8da222db", patient.getUuid());
        assertEquals("GAN200001", patient.getIdentifier());
        assertEquals("Horatio", patient.getGivenName());
        assertEquals("Sinha", patient.getFamilyName());
        assertEquals("M", patient.getGender());
        assertEquals("1983-01-30", patient.getBirthDate().toString());
        assertEquals("Ramgarh", patient.getAddressFieldValue());
        assertEquals("2008-08-15 15:57:09.0", patient.getDateCreated().toString());
        assertEquals(null, patient.getDeathDate());
    }

    @Test
    public void shouldSearchByNameAndVillage() {
        List<PatientResponse> patients = patientDao.getPatients("", null, "Sin", null, "city_village", "Ramgarh", 100, 0, null);
        assertEquals(1, patients.size());
        PatientResponse patient = patients.get(0);
        assertEquals("341b4e41-790c-484f-b6ed-71dc8da222db", patient.getUuid());
        assertEquals("GAN200001", patient.getIdentifier());
        assertEquals("Horatio", patient.getGivenName());
        assertEquals("Sinha", patient.getFamilyName());
        assertEquals("M", patient.getGender());

        assertEquals("Ramgarh", patient.getAddressFieldValue());
        assertEquals("2008-08-15 15:57:09.0", patient.getDateCreated().toString());
        assertEquals(null, patient.getDeathDate());
    }

    @Test
    public void shouldSortResultsByCreationDate() {
        List<PatientResponse> patients = patientDao.getPatients("", null, "Sinha", null, "city_village", "", 100, 0, null);
        assertEquals(2, patients.size());
        assertEquals("Sinha", patients.get(0).getFamilyName());
        assertEquals("Sinha", patients.get(0).getFamilyName());
    }

    @Test
    public void shouldReturnResultAfterGivenOffset() throws Exception {
        List<PatientResponse> patients = patientDao.getPatients("", null, "Sinha", null, "city_village", "", 100, 1, null);
        assertEquals(1, patients.size());

        patients = patientDao.getPatients("", null, "Sinha", null, "city_village", "", 100, 2, null);
        assertEquals(0, patients.size());
    }

    @Test
    public void shouldFetchBasedOnLocalName() throws Exception {
        List<PatientResponse> patients = patientDao.getPatients("", null, "", "testCaste1", "city_village", null, 100, 0, null);
        assertEquals(1, patients.size());
    }

    @Test
    @Ignore
    public void shouldFetchBasedOnPatientAttributeTypes() throws Exception {
        String[] patientAttributes = { "caste"};
        List<PatientResponse> patients = patientDao.getPatients("", null, "", "testCaste1", "city_village", null, 100, 0, patientAttributes);

        assertEquals(1, patients.size());
        assertEquals("", patients.get(0).getCustomAttribute());
    }

    @Test
    public void shouldFetchPatientsWithPartialIdentifierMatch() throws Exception {
        String partialIdentifier = "300001";
        boolean shouldMatchExactPatientId = false;
        List<Patient> patients = patientDao.getPatients(partialIdentifier, shouldMatchExactPatientId);
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
        String partialIdentifier = "3000001";
        boolean shouldMatchExactPatientId = false;
        List<Patient> patients = patientDao.getPatients(partialIdentifier, shouldMatchExactPatientId);
        assertEquals(0, patients.size());
    }
}