package org.bahmni.module.admin.csv.service;

import org.bahmni.csv.KeyValue;
import org.bahmni.module.admin.csv.models.PatientRow;
import org.bahmni.module.admin.csv.utils.CSVUtils;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.openmrs.Patient;
import org.openmrs.PersonAddress;
import org.openmrs.PersonAttributeType;
import org.openmrs.api.AdministrationService;
import org.openmrs.api.ConceptService;
import org.openmrs.api.PatientService;
import org.openmrs.api.PersonService;
import org.openmrs.module.addresshierarchy.AddressField;
import org.openmrs.module.addresshierarchy.AddressHierarchyLevel;
import org.openmrs.module.addresshierarchy.service.AddressHierarchyService;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

public class CSVPatientServiceTest {

    @Mock
    private PatientService mockPatientService;
    @Mock
    private PersonService mockPersonService;
    @Mock
    private ConceptService conceptService;
    @Mock
    private AdministrationService mockAdminService;
    @Mock
    private AddressHierarchyService addressHierarchyService;
    @Mock
    private CSVAddressService csvAddressService;

    @Before
    public void setUp() throws Exception {
        initMocks(this);
    }

    @Rule
    public ExpectedException exception = ExpectedException.none();

    @Test
    public void save_patient_name() throws ParseException {
        PatientRow patientRow = new PatientRow();
        patientRow.firstName = "Romesh";
        patientRow.middleName = "Sharad";
        patientRow.lastName = "Powar";

        ArgumentCaptor<Patient> patientArgumentCaptor = ArgumentCaptor.forClass(Patient.class);
        CSVPatientService csvPatientService = new CSVPatientService(mockPatientService, mockPersonService, conceptService, mockAdminService, csvAddressService);

        csvPatientService.save(patientRow);

        verify(mockPatientService).savePatient(patientArgumentCaptor.capture());

        Patient capturedPatient = patientArgumentCaptor.getValue();
        assertEquals("Romesh", capturedPatient.getGivenName());
        assertEquals("Sharad", capturedPatient.getMiddleName());
        assertEquals("Powar", capturedPatient.getFamilyName());
    }

    @Test
    public void save_registrationNumber_birthdate_gender() throws ParseException {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(CSVUtils.ENCOUNTER_DATE_PATTERN);

        PatientRow patientRow = new PatientRow();
        patientRow.age = "34";
        patientRow.gender = "Male";
        patientRow.registrationNumber = "reg-no";
        patientRow.birthdate = "1998-07-07";

        ArgumentCaptor<Patient> patientArgumentCaptor = ArgumentCaptor.forClass(Patient.class);
        CSVPatientService csvPatientService = new CSVPatientService(mockPatientService, mockPersonService, conceptService, mockAdminService, csvAddressService);

        csvPatientService.save(patientRow);

        verify(mockPatientService).savePatient(patientArgumentCaptor.capture());

        Patient capturedPatient = patientArgumentCaptor.getValue();
        assertEquals("Male", capturedPatient.getGender());
        assertEquals("reg-no", capturedPatient.getPatientIdentifier().getIdentifier());
        assertEquals(simpleDateFormat.parse("1998-07-07"), capturedPatient.getBirthdate());

    }

    @Test
    public void save_registrationNumber_age_gender() throws ParseException {
        PatientRow patientRow = new PatientRow();
        patientRow.age = "34";
        patientRow.gender = "Male";
        patientRow.registrationNumber = "reg-no";


        ArgumentCaptor<Patient> patientArgumentCaptor = ArgumentCaptor.forClass(Patient.class);
        CSVPatientService csvPatientService = new CSVPatientService(mockPatientService, mockPersonService, conceptService, mockAdminService, csvAddressService);

        csvPatientService.save(patientRow);

        verify(mockPatientService).savePatient(patientArgumentCaptor.capture());

        Patient capturedPatient = patientArgumentCaptor.getValue();
        assertEquals("Male", capturedPatient.getGender());
        assertEquals("reg-no", capturedPatient.getPatientIdentifier().getIdentifier());
        assertEquals(new Integer(34), capturedPatient.getAge());

    }

    @Test
    public void save_addressparts() throws ParseException {
        PatientRow patientRow = new PatientRow();

        List<KeyValue> addressParts = new ArrayList<KeyValue>() {{
            add(new KeyValue("Cities", "zhumri tallayya"));
            add(new KeyValue("States", "Timbaktu"));
            add(new KeyValue("Countries", "Bharat"));
            add(new KeyValue("ZipCode", "555555"));
        }};
        patientRow.addressParts = addressParts;

        AddressHierarchyLevel cityLevel = new AddressHierarchyLevel();
        cityLevel.setName("Cities");
        cityLevel.setAddressField(AddressField.CITY_VILLAGE);

        AddressHierarchyLevel stateLevel = new AddressHierarchyLevel();
        stateLevel.setName("States");
        stateLevel.setAddressField(AddressField.STATE_PROVINCE);

        AddressHierarchyLevel countryLevel = new AddressHierarchyLevel();
        countryLevel.setName("Countries");
        countryLevel.setAddressField(AddressField.COUNTRY);

        AddressHierarchyLevel postalCodeLevel = new AddressHierarchyLevel();
        postalCodeLevel.setName("ZipCode");
        postalCodeLevel.setAddressField(AddressField.POSTAL_CODE);

        ArrayList<AddressHierarchyLevel> addressHierarchyLevels = new ArrayList<>();
        addressHierarchyLevels.add(cityLevel);
        addressHierarchyLevels.add(stateLevel);
        addressHierarchyLevels.add(countryLevel);
        addressHierarchyLevels.add(postalCodeLevel);

        when(addressHierarchyService.getAddressHierarchyLevels()).thenReturn(addressHierarchyLevels);

        ArgumentCaptor<Patient> patientArgumentCaptor = ArgumentCaptor.forClass(Patient.class);
        CSVPatientService csvPatientService = new CSVPatientService(mockPatientService, mockPersonService, conceptService, mockAdminService, new CSVAddressService(addressHierarchyService));
        csvPatientService.save(patientRow);

        verify(mockPatientService).savePatient(patientArgumentCaptor.capture());

        Patient patient = patientArgumentCaptor.getValue();
        Set<PersonAddress> addresses = patient.getAddresses();
        PersonAddress capturedAddress = addresses.iterator().next();
        assertEquals("zhumri tallayya", capturedAddress.getCityVillage());
        assertEquals("Timbaktu", capturedAddress.getStateProvince());
        assertEquals("Bharat", capturedAddress.getCountry());
        assertEquals("555555", capturedAddress.getPostalCode());
    }

    @Test
    public void save_person_attributes() throws ParseException {
        when(mockPersonService.getAllPersonAttributeTypes(false)).thenReturn(Arrays.asList(
                createPersonAttributeType("familyNameLocal", "java.lang.String"),
                createPersonAttributeType("caste", "java.lang.String")
        ));
        PatientRow patientRow = new PatientRow();
        patientRow.attributes = new ArrayList<KeyValue>() {{
            add(new KeyValue("familyNameLocal", "ram"));
            add(new KeyValue("caste", "gond"));
        }};

        CSVPatientService csvPatientService = new CSVPatientService(mockPatientService, mockPersonService, conceptService, mockAdminService, csvAddressService);
        csvPatientService.save(patientRow);

        ArgumentCaptor<Patient> patientArgumentCaptor = ArgumentCaptor.forClass(Patient.class);
        verify(mockPatientService).savePatient(patientArgumentCaptor.capture());

        Patient patient = patientArgumentCaptor.getValue();
        assertEquals(patient.getAttribute("familyNameLocal").getValue(), "ram");
        assertEquals(patient.getAttribute("caste").getValue(), "gond");
    }

    @Test
    public void fails_whenNonExistingAttributeIsImported() throws ParseException {
        CSVPatientService csvPatientService = new CSVPatientService(mockPatientService, mockPersonService, conceptService, mockAdminService, csvAddressService);
        when(mockPersonService.getAllPersonAttributeTypes(false)).thenReturn(Arrays.asList(createPersonAttributeType("familyNameLocal", "java.lang.String")));

        PatientRow patientRow = new PatientRow();
        patientRow.attributes = Arrays.asList(new KeyValue("nonExisting", "someValue"));

        exception.expect(RuntimeException.class);
        exception.expectMessage("Person Attribute nonExisting not found");
        csvPatientService.save(patientRow);
    }

    private PersonAttributeType createPersonAttributeType(String name, String format) {
        PersonAttributeType personAttributeType = new PersonAttributeType();
        personAttributeType.setName(name);
        personAttributeType.setFormat(format);
        return personAttributeType;
    }
}