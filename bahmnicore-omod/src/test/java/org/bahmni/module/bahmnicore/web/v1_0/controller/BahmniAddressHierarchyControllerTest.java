package org.bahmni.module.bahmnicore.web.v1_0.controller;

import org.bahmni.module.bahmnicore.service.BahmniAddressHierarchyService;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.openmrs.module.addresshierarchy.AddressHierarchyEntry;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.initMocks;

public class BahmniAddressHierarchyControllerTest {
    @Mock
    BahmniAddressHierarchyService bahmniAddressHierarchyService;

    private BahmniAddressHierarchyController bahmniAddressHierarchyController;

    @Before
    public void setUp() throws Exception {
        initMocks(this);
        bahmniAddressHierarchyController = new BahmniAddressHierarchyController(bahmniAddressHierarchyService);
    }

    @Test
    public void shouldGetAddressHierarchyEntryByUuid() throws Exception {
        AddressHierarchyEntry addressHierarchyEntry = new AddressHierarchyEntry();
        addressHierarchyEntry.setName("test");
        when(bahmniAddressHierarchyService.getAddressHierarchyEntryByUuid("uuid")).thenReturn(addressHierarchyEntry);
        AddressHierarchyEntry hierarchyEntry = bahmniAddressHierarchyController.get("uuid");

        verify(bahmniAddressHierarchyService, times(1)).getAddressHierarchyEntryByUuid("uuid");
        assertNotNull(hierarchyEntry);
        assertEquals("test", addressHierarchyEntry.getName());
    }

    @Test
    public void shouldReturnNullIfUuidIsNull() throws Exception {
        AddressHierarchyEntry hierarchyEntry = bahmniAddressHierarchyController.get(null);

        verify(bahmniAddressHierarchyService, never()).getAddressHierarchyEntryByUuid(anyString());
        assertNull(hierarchyEntry);
    }
}