package org.bahmni.module.referencedata.web.controller;

import org.bahmni.module.bahmnicore.web.v1_0.controller.BaseWebControllerTest;
import org.bahmni.module.referencedata.web.contract.RequestConcept;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.Concept;
import org.openmrs.Visit;
import org.openmrs.api.ConceptService;
import org.openmrs.web.test.BaseModuleWebContextSensitiveTest;
import org.springframework.beans.factory.annotation.Autowired;
import sun.jvm.hotspot.utilities.Assert;

import static org.junit.Assert.assertNotNull;

@org.springframework.test.context.ContextConfiguration(locations = {"classpath:TestingApplicationContext.xml"}, inheritLocations = true)
public class ConceptControllerIT extends BaseWebControllerTest {
    @Autowired
    private ConceptController conceptController;
    @Autowired
    private ConceptService conceptService;

    @Before
    public void setUp() throws Exception {
//        conceptController = new ConceptController(conceptService);
    }

    @Test
    public void shouldCreateConcept() throws Exception {
        String uniqueName = "uniqueName";
        String displayName = "uniqueName";
        String className = "Finding";
        String description = "Sample basic concept being created";
        String dataType = "N/A";

        String json = "{" +
                "\"uniqueName\":\"" + uniqueName + "\"," +
                "\"displayName\":\"" + displayName + "\"," +
                "\"description\":\"" + description + "\"," +
                "\"className\":\"" + className + "\"," +
                "\"dataType\":\"" + dataType +
                "}";

        Concept concept = deserialize(handle(newPostRequest("/rest/v1/reference-data/concept", json)), Concept.class);
        assertNotNull(concept);


//        Concept savedConcept = conceptController.create(concept);

//        Assert.assertNotNull(savedConcept.getUuid());
    }
}