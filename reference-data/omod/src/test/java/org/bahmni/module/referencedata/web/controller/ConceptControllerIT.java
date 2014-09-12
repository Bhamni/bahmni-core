package org.bahmni.module.referencedata.web.controller;

import org.bahmni.module.bahmnicore.web.v1_0.controller.BaseWebControllerTest;
import org.junit.Test;
import org.openmrs.Concept;
import org.openmrs.api.ConceptService;
import org.openmrs.api.context.Context;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@org.springframework.test.context.ContextConfiguration(locations = {"classpath:TestingApplicationContext.xml"}, inheritLocations = true)
public class ConceptControllerIT extends BaseWebControllerTest {
    @Autowired
    private ConceptController conceptController;
    @Autowired
    private ConceptService conceptService;

    @Test
    public void shouldCreateConcept() throws Exception {
        String uniqueName = "uniqueName";
        String displayName = "uniqueName";
        String className = "Finding";
        String description = "Sample basic concept being created";
        String dataType = "N/A";

        String conceptDataJson = "{" +
                "\"uniqueName\":\"" + uniqueName + "\"," +
                "\"displayName\":\"" + displayName + "\"," +
                "\"description\":\"" + description + "\"," +
                "\"className\":\"" + className + "\"," +
                "\"dataType\":\"" + dataType + "\"" +
                "}";

        MockHttpServletRequest request = newPostRequest("/rest/v1/reference-data/concept", conceptDataJson);
        MockHttpServletResponse response = handle(request);
        assertEquals(response.getStatus(), HttpStatus.CREATED.value());
        String conceptId = deserialize(response, String.class);

        assertNotNull(conceptId);

        Concept concept = conceptService.getConcept(conceptId);

        assertEquals(uniqueName, concept.getFullySpecifiedName(Context.getLocale()).getName());
        assertEquals(displayName, concept.getShortestName(Context.getLocale(), false).getName());
        assertEquals(className, concept.getConceptClass().getName());
        assertEquals(description, concept.getDescription(Context.getLocale()).getDescription());
        assertEquals(dataType, concept.getDatatype().getName());
    }

    @Test
    public void shouldCreateConceptWithoutDescription() throws Exception {
        String uniqueName = "uniqueName";
        String displayName = "uniqueName";
        String className = "Ramesh";
        String dataType = "N/A";

        String conceptDataJson = "{" +
                "\"uniqueName\":\"" + uniqueName + "\"," +
                "\"displayName\":\"" + displayName + "\"," +
                "\"className\":\"" + className + "\"," +
                "\"dataType\":\"" + dataType + "\"" +
                "}";

        MockHttpServletRequest request = newPostRequest("/rest/v1/reference-data/concept", conceptDataJson);
        MockHttpServletResponse response = handle(request);
        assertEquals(response.getStatus(), HttpStatus.BAD_REQUEST.value());
    }

    @Test
    public void shouldCreateConceptWithoutShortName() throws Exception {
        String uniqueName = "uniqueName";
        String description = "Sample basic concept being created";
        String className = "Ramesh";
        String dataType = "N/A";

        String conceptDataJson = "{" +
                "\"uniqueName\":\"" + uniqueName + "\"," +
                "\"description\":\"" + description + "\"," +
                "\"className\":\"" + className + "\"," +
                "\"dataType\":\"" + dataType + "\"" +
                "}";

        MockHttpServletRequest request = newPostRequest("/rest/v1/reference-data/concept", conceptDataJson);
        MockHttpServletResponse response = handle(request);
        assertEquals(response.getStatus(), HttpStatus.BAD_REQUEST.value());
    }

    @Test
    public void shouldNotCreateConceptForWrongDatatype() throws Exception {
        String uniqueName = "uniqueName";
        String displayName = "uniqueName";
        String className = "Finding";
        String description = "Sample basic concept being created";
        String dataType = "NA";

        String conceptDataJson = "{" +
                "\"uniqueName\":\"" + uniqueName + "\"," +
                "\"displayName\":\"" + displayName + "\"," +
                "\"description\":\"" + description + "\"," +
                "\"className\":\"" + className + "\"," +
                "\"dataType\":\"" + dataType + "\"" +
                "}";

        MockHttpServletRequest request = newPostRequest("/rest/v1/reference-data/concept", conceptDataJson);
        MockHttpServletResponse response = handle(request);
        assertEquals(response.getStatus(), HttpStatus.BAD_REQUEST.value());
    }

    @Test
    public void shouldNotCreateConceptForWrongConceptClass() throws Exception {
        String uniqueName = "uniqueName";
        String displayName = "uniqueName";
        String className = "Ramesh";
        String description = "Sample basic concept being created";
        String dataType = "N/A";

        String conceptDataJson = "{" +
                "\"uniqueName\":\"" + uniqueName + "\"," +
                "\"displayName\":\"" + displayName + "\"," +
                "\"description\":\"" + description + "\"," +
                "\"className\":\"" + className + "\"," +
                "\"dataType\":\"" + dataType + "\"" +
                "}";

        MockHttpServletRequest request = newPostRequest("/rest/v1/reference-data/concept", conceptDataJson);
        MockHttpServletResponse response = handle(request);
        assertEquals(response.getStatus(), HttpStatus.BAD_REQUEST.value());
    }

    @Test
    public void shouldNotCreateConceptForEmptyConceptUniqueName() throws Exception {
        String uniqueName = "";
        String displayName = "uniqueName";
        String className = "Ramesh";
        String description = "Sample basic concept being created";
        String dataType = "N/A";

        String conceptDataJson = "{" +
                "\"uniqueName\":\"" + uniqueName + "\"," +
                "\"displayName\":\"" + displayName + "\"," +
                "\"description\":\"" + description + "\"," +
                "\"className\":\"" + className + "\"," +
                "\"dataType\":\"" + dataType + "\"" +
                "}";

        MockHttpServletRequest request = newPostRequest("/rest/v1/reference-data/concept", conceptDataJson);
        MockHttpServletResponse response = handle(request);
        assertEquals(response.getStatus(), HttpStatus.BAD_REQUEST.value());
    }


    @Test
    public void shouldNotCreateConceptForEmptyConceptDataType() throws Exception {
        String uniqueName = "uniqueName";
        String displayName = "uniqueName";
        String className = "Ramesh";
        String description = "Sample basic concept being created";

        String conceptDataJson = "{" +
                "\"uniqueName\":\"" + uniqueName + "\"," +
                "\"displayName\":\"" + displayName + "\"," +
                "\"description\":\"" + description + "\"," +
                "\"className\":\"" + className + "\"" +
                "}";

        MockHttpServletRequest request = newPostRequest("/rest/v1/reference-data/concept", conceptDataJson);
        MockHttpServletResponse response = handle(request);
        assertEquals(response.getStatus(), HttpStatus.BAD_REQUEST.value());
    }

    @Test
    public void shouldNotCreateConceptForEmptyConceptClass() throws Exception {
        String uniqueName = "uniqueName";
        String displayName = "uniqueName";
        String description = "Sample basic concept being created";
        String dataType = "N/A";


        String conceptDataJson = "{" +
                "\"uniqueName\":\"" + uniqueName + "\"," +
                "\"displayName\":\"" + displayName + "\"," +
                "\"description\":\"" + description + "\"," +
                "\"className\":\"" + dataType + "\"" +
                "}";

        MockHttpServletRequest request = newPostRequest("/rest/v1/reference-data/concept", conceptDataJson);
        MockHttpServletResponse response = handle(request);
        assertEquals(response.getStatus(), HttpStatus.BAD_REQUEST.value());
    }
}