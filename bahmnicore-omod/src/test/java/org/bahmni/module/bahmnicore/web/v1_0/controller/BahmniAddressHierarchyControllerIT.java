package org.bahmni.module.bahmnicore.web.v1_0.controller;

import org.bahmni.module.bahmnicore.web.v1_0.BaseIntegrationTest;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.module.addresshierarchy.AddressHierarchyEntry;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class BahmniAddressHierarchyControllerIT extends BaseIntegrationTest {
    @Before
    public void setUp() throws Exception {
        executeDataSet("addressHierarchy.xml");
    }

    @Test
    public void shouldGetAddressHierarchyByUuid() throws Exception {
        AddressHierarchyEntry addressHierarchyEntry = deserialize(handle(newGetRequest("/rest/v1/addressHierarchy/" + "22e41146-e162-11df-9195-001e378eb67f")), AddressHierarchyEntry.class);
        assertNotNull(addressHierarchyEntry);
        assertEquals("United States", addressHierarchyEntry.getName());
    }
}