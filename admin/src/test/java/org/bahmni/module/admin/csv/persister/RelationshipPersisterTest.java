package org.bahmni.module.admin.csv.persister;

import org.bahmni.csv.Messages;
import org.bahmni.module.admin.csv.models.RelationshipRow;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class RelationshipPersisterTest {

    @Test
    public void shouldPassValidationIfAllRequiredFieldsAreProvided() throws Exception {
        Messages messages = getRelationshipPersister().validate(new RelationshipRow("GAN200012", "GAN200015", "Parent", "Child", "2014-02-01", "2015-01-01"));
        assertEquals(0, messages.size());
    }

    @Test
    public void shouldThrowExceptionIfPersonAIsNotSpecified() throws Exception {
        Messages messages = getRelationshipPersister().validate(new RelationshipRow(null, "GAN200015", "Parent", "Child", "2014-02-01", "2015-01-01"));
        assertTrue(messages.contains("Patient unique identifier not specified."));
    }

    @Test
    public void shouldThrowExceptionIfPersonBIsNotSpecified() throws Exception {
        Messages messages = getRelationshipPersister().validate(new RelationshipRow("GAN200015", null, "Parent", "Child", "2014-02-01", "2015-01-01"));
        assertTrue(messages.contains("Target relationship person identifier not specified."));
    }

    @Test
    public void shouldThrowExceptionIfRelationshipAIsToBNotSpecified() throws Exception {
        Messages messages = getRelationshipPersister().validate(new RelationshipRow("GAN200015", "GAN200016", null, "Child", "2014-02-01", "2015-01-01"));
        assertTrue(messages.contains("Relationship type A is to B is not specified."));
    }

    @Test
    public void shouldThrowExceptionIfRelationshipBIsToANotSpecified() throws Exception {
        Messages messages = getRelationshipPersister().validate(new RelationshipRow("GAN200015", "GAN200016", "Parent", null, "2014-02-01", "2015-01-01"));
        assertTrue(messages.contains("Relationship type B is to A is not specified."));
    }


    @Test
    public void shouldThrowExceptionIfEndDateIsBeforeStartDate() throws Exception {
        Messages messages = getRelationshipPersister().validate(new RelationshipRow("GAN200012", "GAN200015", "Parent", "Child", "2015-02-01", "2014-01-01"));
        assertTrue(messages.contains("Start date should be before end date."));
    }

    @Test
    public void shouldThrowExceptionIfTheStartDateFormatIsWrong() throws Exception {
        Messages messages = getRelationshipPersister().validate(new RelationshipRow("GAN200012", "GAN200015", "Parent", "Child", "02-01-2015", "2014-01-01"));
        assertTrue(messages.contains("Could not parse provided dates. Please provide date in format yyyy-mm-dd"));
    }

    @Test
    public void shouldThrowExceptionIfTheEndDateFormatIsWrong() throws Exception {
        Messages messages = getRelationshipPersister().validate(new RelationshipRow("GAN200012", "GAN200015", "Parent", "Child", "2015-02-01", "01-01-2014"));
        assertTrue(messages.contains("Could not parse provided dates. Please provide date in format yyyy-mm-dd"));
    }

    private RelationshipPersister getRelationshipPersister() {
        return new RelationshipPersister();
    }

}