package org.bahmni.module.admin.csv;

import org.junit.Before;
import org.junit.Test;
import org.openmrs.api.PatientService;
import org.openmrs.api.VisitService;
import org.openmrs.test.BaseModuleContextSensitiveTest;
import org.springframework.beans.factory.annotation.Autowired;

@org.springframework.test.context.ContextConfiguration(locations = {"classpath:TestingApplicationContext.xml"}, inheritLocations = true)
public class EncounterImporterIT extends BaseModuleContextSensitiveTest {
    @Autowired
    private EncounterPersister encounterPersister;

    @Before
    public void setUp() throws Exception {
        executeDataSet("dataSetup.xml");
    }

    @Test
    public void should_read_sample_csv_and_create_entity_list() throws Exception {
        String filePath = EncounterImporterIT.class.getResource("/").getPath();
        EncounterImporter encounterImporter = new EncounterImporter();
        encounterImporter.importEncounters(filePath, "sample.csv");
    }
}
