package org.bahmni.module.bahmnicore;

import org.codehaus.jackson.map.ObjectMapper;
import org.junit.Assert;
import org.openmrs.web.test.BaseModuleWebContextSensitiveTest;

import static org.junit.Assert.assertEquals;

@org.springframework.test.context.ContextConfiguration(locations = {"classpath:TestingApplicationContext.xml"}, inheritLocations = true)
public class BaseIntegrationTest extends BaseModuleWebContextSensitiveTest {

    /**
     * Asserts that two Json strings represent same object.
     * @param expectedJson The expected Json string.
     * @param actualJson The actual Json string.
     */
    public static void assertJsonEquals(String expectedJson, String actualJson) {
        try {
            assertEquals(new ObjectMapper().readTree(expectedJson), new ObjectMapper().readTree(actualJson));
        } catch (Exception e) {
            Assert.fail();
        }
    }
}
