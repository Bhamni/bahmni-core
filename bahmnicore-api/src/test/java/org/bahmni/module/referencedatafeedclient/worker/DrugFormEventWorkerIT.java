package org.bahmni.module.referencedatafeedclient.worker;

import org.bahmni.module.referencedatafeedclient.*;
import org.bahmni.module.referencedatafeedclient.domain.*;
import org.bahmni.module.referencedatafeedclient.service.*;
import org.bahmni.webclients.*;
import org.ict4h.atomfeed.client.domain.*;
import org.junit.*;
import org.junit.Test;
import org.mockito.*;
import org.openmrs.*;
import org.openmrs.api.*;
import org.openmrs.web.test.*;
import org.springframework.beans.factory.annotation.*;

import java.io.*;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

@org.springframework.test.context.ContextConfiguration(locations = {"classpath:TestingApplicationContext.xml"})
public class DrugFormEventWorkerIT extends BaseModuleWebContextSensitiveTest {
    @Mock
    private HttpClient httpClient;
    @Mock
    private ReferenceDataFeedProperties referenceDataFeedProperties;
    @Autowired
    private ReferenceDataConceptService referenceDataConceptService;
    @Autowired
    private ConceptService conceptService;

    private final String referenceDataUri = "http://localhost";
    private DrugFormEventWorker drugFormEventWorker;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        when(referenceDataFeedProperties.getReferenceDataUri()).thenReturn(referenceDataUri);
        drugFormEventWorker = new DrugFormEventWorker(httpClient, referenceDataFeedProperties, referenceDataConceptService);
    }

    @Test
    public void shouldCreateConceptForDrugForm() throws IOException {
        Event event = new Event("xxxx-yyyyy", "/reference-data/drug_form/412fa577-5ed0-4738-b550-a83ec144a84b");
        DrugForm drugForm = new DrugForm("412fa577-5ed0-4738-b550-a83ec144a84b", "Tablet");
        when(httpClient.get(referenceDataUri + event.getContent(), DrugForm.class)).thenReturn(drugForm);

        drugFormEventWorker.process(event);

        Concept drugFormConcept = conceptService.getConceptByUuid(drugForm.getId());

        assertEquals(drugForm.getName(), drugFormConcept.getName().getName());
    }
}
