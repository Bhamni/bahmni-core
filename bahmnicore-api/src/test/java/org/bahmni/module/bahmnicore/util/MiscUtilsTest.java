package org.bahmni.module.bahmnicore.util;

import org.junit.Test;
import org.openmrs.Concept;
import org.openmrs.api.ConceptService;
import org.openmrs.module.bahmniemrapi.encountertransaction.contract.BahmniObservation;

import java.util.Arrays;
import java.util.Collection;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class MiscUtilsTest {

    @Test
    public void shouldReturnConceptsWhenTheyAreAvailable() {
        ConceptService conceptService = mock(ConceptService.class);
        String nonExistantConceptName = "doesNotExist";
        String sampleConceptName = "sampleConcept";
        when(conceptService.getConceptByName(nonExistantConceptName)).thenReturn(null);
        Concept sampleConcept = new Concept();
        when(conceptService.getConceptByName(sampleConceptName)).thenReturn(sampleConcept);
        Collection<Concept> concepts = MiscUtils.getConceptsForNames(Arrays.asList(sampleConceptName, nonExistantConceptName), conceptService);
        assertThat(concepts.size(), is(equalTo(1)));
        assertThat(concepts.iterator().next(), is(sampleConcept));
    }

    @Test
    public void shouldSetUuidForObservationNotHavingUuid() {
        BahmniObservation observation1 = new BahmniObservation();
        observation1.setUuid("123");
        BahmniObservation observation2 = mock(BahmniObservation.class);
        Collection<BahmniObservation> bahmniObservations = Arrays.asList(observation1, observation2);

        MiscUtils.setUuidsForObservations(bahmniObservations);

        assertThat(observation1.getUuid(), is("123"));
        verify(observation2, times(1)).setUuid(anyString());
    }

    @Test
    public void shouldReturnTrueForValidFormFieldPath() {
        boolean isValidFormFieldPath = MiscUtils.isValidFormFieldPath("Initial Medical Examination.2/8-0/139-0/140-34");
        assertTrue(isValidFormFieldPath);
    }

    @Test
    public void shouldReturnFalseForFormFieldPathWithHyphenFollowedByNoNumber() {
        boolean isValidFormFieldPath = MiscUtils.isValidFormFieldPath("Initial Medical Examination.2/8-0/139-/140-34");
        assertFalse(isValidFormFieldPath);
    }

    @Test
    public void shouldReturnTrueForFormFieldPathWithUnderscoresAndDigitsInTheFormName() {
        boolean isValidFormFieldPath = MiscUtils.isValidFormFieldPath("Initial_Medical_Examination_1.2/8-0/139-1/140-34");
        assertTrue(isValidFormFieldPath);
    }

    @Test
    public void shouldReturnTrueForFormFieldPathWithNoSpacesInTheFormName() {
        boolean isValidFormFieldPath = MiscUtils.isValidFormFieldPath("InitialMedicalExamination1.2/8-0/139-1/140-34");
        assertTrue(isValidFormFieldPath);
    }

    @Test
    public void shouldReturnFalseForFormFieldPathWithSlashAtTheEnd() {
        boolean isValidFormFieldPath = MiscUtils.isValidFormFieldPath("InitialMedicalExamination1.2/8-0/139-1/140-34/");
        assertFalse(isValidFormFieldPath);
    }

    @Test
    public void shouldReturnFalseForFormFieldPathWithSlashAtTheEndFollowedByANumber() {
        boolean isValidFormFieldPath = MiscUtils.isValidFormFieldPath("InitialMedicalExamination1.2/8-0/139-1/140-34/2");
        assertFalse(isValidFormFieldPath);
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowExceptionIfTheFormFieldPathIsNotValid() {
        BahmniObservation observation1 = new BahmniObservation();
        observation1.setUuid("123");
        observation1.setFormFieldPath("InitialMedicalExamination1.2/8-0/139-/140-34");
        BahmniObservation observation2 = mock(BahmniObservation.class);
        Collection<BahmniObservation> bahmniObservations = Arrays.asList(observation1, observation2);

        MiscUtils.validateFormFieldPath(bahmniObservations);
    }

}
