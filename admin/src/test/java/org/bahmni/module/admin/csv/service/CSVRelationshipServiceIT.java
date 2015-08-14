package org.bahmni.module.admin.csv.service;

import org.bahmni.module.admin.BaseIntegrationTest;
import org.bahmni.module.admin.csv.models.RelationshipRow;
import org.bahmni.module.admin.csv.utils.CSVUtils;
import org.bahmni.module.bahmnicore.service.BahmniPatientService;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.openmrs.Relationship;
import org.openmrs.api.AdministrationService;
import org.openmrs.api.PersonService;
import org.openmrs.api.ProviderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class CSVRelationshipServiceIT extends BaseIntegrationTest {
    @Autowired
    private BahmniPatientService patientService;

    @Autowired
    private PersonService personService;

    @Autowired
    private ProviderService providerService;

    @Autowired
    @Qualifier("adminService")
    private AdministrationService administrationService;

    @Rule
    public ExpectedException expectedEx = ExpectedException.none();

    private CSVRelationshipService csvRelationshipService;

    @Before
    public void setUp() throws Exception {
        executeDataSet("relationshipDataSetup.xml");
    }

    @Test
    public void shouldSavePatientRelationship() throws Exception {
        csvRelationshipService = new CSVRelationshipService(patientService, personService, providerService, administrationService);

        Relationship relationship = csvRelationshipService.save(new RelationshipRow("ABC123", "XYZ", "Parent", "Child", "2015-04-28", "2016-04-28"));

        assertNotNull("Should have saved the relationship", relationship);
        assertEquals(relationship.getPersonA().getPersonId().intValue(), 8);
        assertEquals(relationship.getPersonB().getPersonId().intValue(), 999);
        assertEquals(relationship.getRelationshipType().getId().intValue(), 2);
        assertEquals(relationship.getStartDate(), CSVUtils.getDateFromString("2015-04-28"));
        assertEquals(relationship.getEndDate(), CSVUtils.getDateFromString("2016-04-28"));
    }

    @Test
    public void shouldSavePatientRelationshipWithTodayDateIfStartDateIsNotSpecified() throws Exception {
        csvRelationshipService = new CSVRelationshipService(patientService, personService, providerService, administrationService);

        Relationship relationship = csvRelationshipService.save(new RelationshipRow("ABC123", "XYZ", "Parent", "Child", null, "2099-04-01"));

        assertNotNull("Should have saved the relationship", relationship);
        assertEquals(relationship.getPersonA().getPersonId().intValue(), 8);
        assertEquals(relationship.getPersonB().getPersonId().intValue(), 999);
        assertEquals(relationship.getRelationshipType().getId().intValue(), 2);
        assertEquals(relationship.getStartDate(), CSVUtils.getTodayDate());
    }

    @Test
    public void shouldNotSavePatientRelationshipThatExists() throws Exception {
        csvRelationshipService = new CSVRelationshipService(patientService, personService, providerService, administrationService);

        csvRelationshipService.save(new RelationshipRow("ABC123", "XYZ", "Parent", "Child", "2015-04-28", "2016-04-28"));

        expectedEx.expect(RuntimeException.class);
        expectedEx.expectMessage("Relationship with ABC123 and XYZ with the type Parent exists.");

        csvRelationshipService.save(new RelationshipRow("ABC123", "XYZ", "Parent", "Child", "2015-04-28", "2016-04-28"));
    }

    @Test
    public void shouldSaveProviderRelationship() throws Exception {
        csvRelationshipService = new CSVRelationshipService(patientService, personService, providerService, administrationService);

        Relationship relationship = csvRelationshipService.save(new RelationshipRow("ABC123", "Super User", "Patient", "Doctor", "2015-04-28", "2016-04-28"));

        assertNotNull("Should have saved the relationship", relationship);
        assertEquals(relationship.getPersonA().getPersonId().intValue(), 8);
        assertEquals(relationship.getPersonB().getPersonId().intValue(), 1);
        assertEquals(relationship.getRelationshipType().getId().intValue(), 10);
        assertEquals(relationship.getStartDate(), CSVUtils.getDateFromString("2015-04-28"));
        assertEquals(relationship.getEndDate(), CSVUtils.getDateFromString("2016-04-28"));
    }

    @Test
    public void shouldNotSaveProviderRelationshipThatExists() throws Exception {
        csvRelationshipService = new CSVRelationshipService(patientService, personService, providerService, administrationService);

        csvRelationshipService.save(new RelationshipRow("ABC123", "Super User", "Patient", "Doctor", "2015-04-28", "2016-04-28"));

        expectedEx.expect(RuntimeException.class);
        expectedEx.expectMessage("Relationship with ABC123 and Super User with the type Patient exists.");

        csvRelationshipService.save(new RelationshipRow("ABC123", "Super User", "Patient", "Doctor", "2015-04-28", "2016-04-28"));
    }
}