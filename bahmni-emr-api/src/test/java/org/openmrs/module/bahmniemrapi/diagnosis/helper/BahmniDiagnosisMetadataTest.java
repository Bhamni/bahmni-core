package org.openmrs.module.bahmniemrapi.diagnosis.helper;

import org.bahmni.test.builder.DiagnosisBuilder;
import org.bahmni.test.builder.ObsBuilder;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.openmrs.*;
import org.openmrs.api.ConceptService;
import org.openmrs.api.ObsService;
import org.openmrs.api.context.Context;
import org.openmrs.module.bahmniemrapi.diagnosis.contract.BahmniDiagnosisRequest;
import org.openmrs.module.emrapi.EmrApiProperties;
import org.openmrs.module.emrapi.diagnosis.Diagnosis;
import org.openmrs.module.emrapi.diagnosis.DiagnosisMetadata;
import org.openmrs.module.emrapi.encounter.domain.EncounterTransaction;
import org.openmrs.util.LocaleUtility;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@PrepareForTest({Context.class,LocaleUtility.class})
@RunWith(PowerMockRunner.class)
public class BahmniDiagnosisMetadataTest {
    @Mock
    private ObsService obsService;
    @Mock
    private ConceptService conceptService;

    @Mock(answer= Answers.RETURNS_DEEP_STUBS)
    private EmrApiProperties properties;

    public static final String BOOLEAN_UUID = "8d4a5cca-c2cc-11de-8d13-0010c6dffd0f";
    private static final String BAHMNI_DIAGNOSIS_STATUS = "Bahmni Diagnosis Status";
    private static final String BAHMNI_DIAGNOSIS_REVISED = "Bahmni Diagnosis Revised";
    private static final String BAHMNI_INITIAL_DIAGNOSIS = "Bahmni Initial Diagnosis";

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void shouldUpdateComments() {
        String comments = "High fever and condition implies Pneumonia";
        BahmniDiagnosisRequest bahmniDiagnosis = new BahmniDiagnosisRequest();
        bahmniDiagnosis.setComments(comments);

        Encounter encounter = new Encounter(){{
            this.addObs(new Obs(){{
                setUuid("Diagnosis-Uuid");
                addGroupMember(new Obs());
            }});
        }};

        EncounterTransaction.Diagnosis diagnosis = new EncounterTransaction.Diagnosis(){{
            this.setExistingObs("Diagnosis-Uuid");
        }};

        when(conceptService.getConceptByName(BAHMNI_INITIAL_DIAGNOSIS)).thenReturn(new Concept());
        when(conceptService.getConceptByName(BAHMNI_DIAGNOSIS_STATUS)).thenReturn(new Concept());
        when(conceptService.getConceptByName(BAHMNI_DIAGNOSIS_REVISED)).thenReturn(new Concept() {{
            this.setDatatype(new ConceptDatatype() {{
                setUuid(BOOLEAN_UUID);
            }});
        }});

        when(conceptService.getTrueConcept()).thenReturn(new Concept());
        when(conceptService.getFalseConcept()).thenReturn(new Concept());


        BahmniDiagnosisMetadata diagnosisHelper = new BahmniDiagnosisMetadata(obsService, conceptService,properties, null);

        PowerMockito.mockStatic(Context.class);
        when(Context.getConceptService()).thenReturn(conceptService);

        diagnosisHelper.update(bahmniDiagnosis, diagnosis, encounter);

        assertEquals(encounter.getAllObs().iterator().next().getComment(), comments);
    }
}
