package org.bahmni.module.openerpatomfeedclient.api.domain;

import org.apache.commons.lang3.time.DateUtils;
import org.bahmni.module.bahmnicore.mapper.builder.BahmniDrugOrderBuilder;
import org.bahmni.module.bahmnicore.mapper.builder.ConceptBuilder;
import org.bahmni.module.bahmnicore.mapper.builder.DrugOrderBuilder;
import org.bahmni.module.bahmnicore.mapper.builder.PatientBuilder;
import org.bahmni.module.bahmnicore.model.BahmniDrugOrder;
import org.bahmni.module.bahmnicore.service.BahmniDrugOrderService;
import org.bahmni.module.bahmnicore.service.BahmniPatientService;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.openmrs.Concept;
import org.openmrs.Drug;
import org.openmrs.DrugOrder;
import org.openmrs.Patient;
import org.openmrs.api.ConceptService;

import java.text.ParseException;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

public class AdjustDrugOrderStartDateFilterTest {

    @Mock
    private BahmniDrugOrderService bahmniDrugOrderService;

    @Mock
    private ConceptService conceptService;

    @Mock
    private BahmniPatientService bahmniPatientService;

    @Before
    public void setUp(){
        initMocks(this);
    }

    @Test
    public void shouldAdjustStartDateWhenActiveDrugOrderIsRepeated() throws ParseException {
        String patientId = "GAN123456";
        Patient patient = new PatientBuilder().build();
        String productUuid = UUID.randomUUID().toString();
        Date existingDrugStartDate = DateUtils.parseDate("21/07/2014", "dd/MM/yyyy");
        int numberOfDays = 5;
        Concept concept = new ConceptBuilder().build();
        Drug drug = createDrug(concept);
        DrugOrder existingDrugOrder = new DrugOrderBuilder().withConcept(concept).withStartDate(existingDrugStartDate).withAutoExpireDate(DateUtils.addDays(existingDrugStartDate, numberOfDays)).build();
        BahmniDrugOrder newDrugOrder = new BahmniDrugOrderBuilder().withProductUuid(productUuid).withStartDate(DateUtils.addDays(existingDrugStartDate, 3)).withNumberOfDaysAndDosage(2, 2.0).build();
        when(bahmniPatientService.getPatientByIdentifier(patientId)).thenReturn(patient);
        when(bahmniDrugOrderService.getActiveDrugOrders(patient.getUuid())).thenReturn(Arrays.asList(existingDrugOrder));
        when(conceptService.getDrugByUuid(productUuid)).thenReturn(drug);

        List<BahmniDrugOrder> adjustedDrugOrders = new AdjustDrugOrderStartDateFilter(bahmniDrugOrderService, conceptService, bahmniPatientService).execute(Arrays.asList(newDrugOrder), patientId);

        assertEquals(DateUtils.parseDate("27/07/2014", "dd/MM/yyyy"), adjustedDrugOrders.get(0).getStartDate());
    }

    private Drug createDrug(Concept concept) {
        Drug drug = new Drug();
        drug.setConcept(concept);
        return drug;
    }
}
