package org.bahmni.module.admin.csv;

import org.bahmni.csv.RowResult;
import org.bahmni.module.admin.csv.models.EncounterRow;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.test.BaseModuleContextSensitiveTest;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;


@org.springframework.test.context.ContextConfiguration(locations = {"classpath:TestingApplicationContext.xml"}, inheritLocations = true)
public class EncounterPersisterTest extends BaseModuleContextSensitiveTest {

    @Autowired
    private EncounterPersister encounterPersister;

    @Before
    public void setUp() throws Exception {
        executeDataSet("dataSetup.xml");
    }

    @Test
    public void should_fail_validation_for_encounter_type_not_found() throws Exception {
        EncounterRow encounterRow = new EncounterRow();
        RowResult<EncounterRow> validate = encounterPersister.validate(encounterRow);
        assertEquals("Encounter Type null not found", validate.getErrorMessage());
    }

    @Test
    public void should_fail_validation_for_visit_type_not_found() throws Exception {
        EncounterRow encounterRow = new EncounterRow();
        encounterRow.encounterType = "OPD";
        RowResult<EncounterRow> validate = encounterPersister.validate(encounterRow);
        assertEquals("Visit Type null not found", validate.getErrorMessage());
    }

    @Test
    public void should_fail_validation_for_patient_not_found() throws Exception {
        EncounterRow encounterRow = new EncounterRow();
        encounterRow.encounterType = "OPD";
        encounterRow.visitType = "OPD";
        RowResult<EncounterRow> validate = encounterPersister.validate(encounterRow);
        assertEquals("Patient with identifier null not found", validate.getErrorMessage());
    }

    @Test
    public void should_pass_validation_for_correct_entries() throws Exception {
        EncounterRow encounterRow = new EncounterRow();
        encounterRow.encounterType = "OPD";
        encounterRow.visitType = "OPD";
        encounterRow.patientIdentifier = "GAN200000";
        RowResult<EncounterRow> validate = encounterPersister.validate(encounterRow);
        assertNull(validate.getErrorMessage());
    }

}