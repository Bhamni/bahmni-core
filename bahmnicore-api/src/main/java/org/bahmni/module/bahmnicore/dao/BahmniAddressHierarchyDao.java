package org.bahmni.module.bahmnicore.dao;

import org.openmrs.module.addresshierarchy.AddressHierarchyEntry;

public interface BahmniAddressHierarchyDao {
    AddressHierarchyEntry getAddressHierarchyEntryByUuid(String uuid);
}
