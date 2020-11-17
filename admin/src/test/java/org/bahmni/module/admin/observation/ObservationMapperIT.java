package org.bahmni.module.admin.observation;

import org.bahmni.csv.KeyValue;
import org.bahmni.module.admin.BaseIntegrationTest;
import org.bahmni.module.admin.csv.models.EncounterRow;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.module.emrapi.encounter.domain.EncounterTransaction;
import org.springframework.beans.factory.annotation.Autowired;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;


public class ObservationMapperIT extends BaseIntegrationTest {

    @Autowired
    private ObservationMapper observationMapper;

    @Before
    public void setUp() throws Exception {
        executeDataSet("conceptSetup.xml");
        executeDataSet("form2DataSetup.xml");
    }

    @Test
    public void shouldCreateForm1AndForm2Observations() throws ParseException {
        EncounterRow anEncounter = new EncounterRow();
        anEncounter.obsRows = new ArrayList<>();

        anEncounter.obsRows.add(new KeyValue("Pulse", "150"));
        anEncounter.obsRows.add(new KeyValue("form2.Vitals.Section.Temperature", "100"));
        anEncounter.encounterDateTime = "2019-09-19";

        final List<EncounterTransaction.Observation> observations = observationMapper.getObservations(anEncounter);

        assertEquals(2, observations.size());

        final EncounterTransaction.Observation temperatureObsInForm2 = observations.get(0);
        assertEquals("Temperature", temperatureObsInForm2.getConcept().getName());
        assertEquals(100, Integer.parseInt((String) temperatureObsInForm2.getValue()));
        assertEquals("Vitals.1/2-0", temperatureObsInForm2.getFormFieldPath());

        final EncounterTransaction.Observation pulseObs = observations.get(1);
        assertEquals("Pulse", pulseObs.getConcept().getName());
        assertEquals("150", pulseObs.getValue());
    }
}
