package org.bahmni.module.referencedatafeedclient.worker;

import org.bahmni.module.referencedatafeedclient.*;
import org.bahmni.module.referencedatafeedclient.domain.*;
import org.bahmni.module.referencedatafeedclient.service.*;
import org.bahmni.webclients.*;
import org.ict4h.atomfeed.client.domain.*;
import org.junit.*;
import org.junit.Test;
import org.junit.rules.*;
import org.mockito.*;
import org.openmrs.api.*;
import org.openmrs.web.test.*;
import org.springframework.beans.factory.annotation.*;

import java.io.*;

import static junit.framework.Assert.assertNotNull;
import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertTrue;
import static org.mockito.Mockito.*;

@org.springframework.test.context.ContextConfiguration(locations = {"classpath:TestingApplicationContext.xml"})
public class DrugEventWorkerIT extends BaseModuleWebContextSensitiveTest {
    @Rule
    public ExpectedException exception = ExpectedException.none();
    @Mock
    private HttpClient httpClient;
    @Mock
    private ReferenceDataFeedProperties referenceDataFeedProperties;
    @Autowired
    private ReferenceDataConceptService referenceDataConceptService;

    @Autowired
    private ConceptService conceptService;
    private final String referenceDataUri = "http://localhost";
    private DrugEventWorker drugEventWorker;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        when(referenceDataFeedProperties.getReferenceDataUri()).thenReturn(referenceDataUri);
        drugEventWorker = new DrugEventWorker(httpClient, referenceDataFeedProperties,referenceDataConceptService);
        executeDataSet("drugEventWorkerTestData.xml");
    }

    @Test
    @Ignore
    public void shouldCreateDrugConceptForDrug() throws IOException {
        Event event = new Event("xxxx-yyyyy", "/reference-data/drug/860e5278-b9f3-49cb-8830-952d89ec9871");
        Drug drug = new Drug("860e5278-b9f3-49cb-8830-952d89ec9871", "calpol", "Paracetamol",
                new DrugForm("a85c5035-8d85-11e3-9b86-0800271c1b75", "tablet"), "500", "mg", "oral", true);
        when(httpClient.get(referenceDataUri + event.getContent(), Drug.class)).thenReturn(drug);

        drugEventWorker.process(event);

        org.openmrs.Drug savedDrug = conceptService.getDrugByUuid(drug.getId());
        assertEquals(drug.getName(), savedDrug.getName());
        assertEquals(drug.getGenericName(), savedDrug.getConcept().getName().getName());
        assertEquals(drug.getForm().getName(), savedDrug.getDosageForm().getName().getName());
        assertEquals(drug.getRoute(), savedDrug.getRoute().getName().getName());
        assertEquals(Double.parseDouble(drug.getStrength()), savedDrug.getDoseStrength());
        assertEquals(drug.getStrengthUnits(), savedDrug.getUnits());
    }

    @Test
    @Ignore
    public void shouldCreateConceptsForGenericNameIfNotExists() throws IOException {
        Event event = new Event("xxxx-yyyyy", "/reference-data/drug/0ab1310c-8e27-11e3-9b86-0800271c1b75");
        Drug drug = new Drug("0ab1310c-8e27-11e3-9b86-0800271c1b75", "Amox", "Amoxycilin",
                new DrugForm("a85c5035-8d85-11e3-9b86-0800271c1b75", "tablet"), "500", "mg", "IV", true);
        when(httpClient.get(referenceDataUri + event.getContent(), Drug.class)).thenReturn(drug);

        drugEventWorker.process(event);

        org.openmrs.Drug savedDrug = conceptService.getDrugByUuid(drug.getId());
        assertEquals(drug.getName(), savedDrug.getName());
        assertNotNull(savedDrug.getConcept().getUuid());
        assertEquals(drug.getGenericName(), savedDrug.getConcept().getName().getName());
        assertNotNull(savedDrug.getRoute().getUuid());
        assertEquals(drug.getRoute(), savedDrug.getRoute().getName().getName());
        assertEquals(drug.getForm().getName(), savedDrug.getDosageForm().getName().getName());
        assertEquals(Double.parseDouble(drug.getStrength()), savedDrug.getDoseStrength());
        assertEquals(drug.getStrengthUnits(), savedDrug.getUnits());
    }

    @Test
    @Ignore
    public void shouldFailFeedIfDrugFormDoesNotExist() throws IOException {
        Event event = new Event("xxxx-yyyyy", "/reference-data/drug/0ab1310c-8e27-11e3-9b86-0800271c1b75");
        Drug drug = new Drug("0ab1310c-8e27-11e3-9b86-0800271c1b75", "Amox", "Amoxycilin",
                new DrugForm("a85c5035-8d85-11e3-9b86-0800271c1b7a", "syrup"), "500", "mg", "IV", true);
        exception.expect(Exception.class);
        exception.expectMessage(String.format("Could not find dosage form for %s", drug.getForm().getName()));
        when(httpClient.get(referenceDataUri + event.getContent(), Drug.class)).thenReturn(drug);

        drugEventWorker.process(event);
    }

    @Test
    @Ignore
    public void shouldInactivateDrug() throws IOException {
        Event event = new Event("xxxx-yyyyy", "/reference-data/drug/860e5278-b9f3-49cb-8830-952d89ec9871");
        Drug drug = new Drug("860e5278-b9f3-49cb-8830-952d89ec9871", "calpol", "Paracetamol",
                new DrugForm("a85c5035-8d85-11e3-9b86-0800271c1b75", "tablet"), "500", "mg", "oral", false);
        when(httpClient.get(referenceDataUri + event.getContent(), Drug.class)).thenReturn(drug);

        drugEventWorker.process(event);

        org.openmrs.Drug savedDrug = conceptService.getDrugByUuid(drug.getId());
        assertNotNull(savedDrug);
        assertTrue(savedDrug.isRetired());
    }
}
