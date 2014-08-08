package org.bahmni.module.admin.csv;

import org.apache.log4j.Logger;
import org.bahmni.csv.CSVFile;
import org.bahmni.csv.MigrateResult;
import org.bahmni.csv.MigratorBuilder;
import org.bahmni.csv.exception.MigrationException;
import org.bahmni.module.admin.csv.models.EncounterRow;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

public class EncounterImporter {
    private static Logger logger = Logger.getLogger(EncounterImporter.class);

    @Autowired
    private EncounterPersister encounterPersister;

    public void importEncounters(String filePath, String fileName) {
        org.bahmni.csv.Migrator migrator = new MigratorBuilder(EncounterRow.class)
                .readFrom(filePath, fileName)
                .persistWith(encounterPersister)
                .withMultipleValidators(5)
                .withMultipleMigrators(5)
                .build();
        try {
            MigrateResult migrateResult = migrator.migrate();
            logger.info("Migration was " + (migrateResult.hasFailed() ? "unsuccessful" : "successful"));
            logger.info("Stage : " + migrateResult.getStageName() + ". Success count : " + migrateResult.numberOfSuccessfulRecords() +
                    ". Fail count : " + migrateResult.numberOfFailedRecords());
        } catch (MigrationException e) {
            logger.error("There was an error during migration. " + e.getMessage());
        }
    }
}
