package org.bahmni.module.bahmnicore.service;

import org.openmrs.module.addresshierarchy.AddressHierarchyEntry;

public interface BahmniAddressHierarchyService {
    AddressHierarchyEntry getAddressHierarchyEntryByUuid(String uuid);
}
