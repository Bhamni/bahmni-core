package org.openmrs.module.bahmnimapping.dao;

import org.openmrs.module.bahmnimapping.model.EntityMapping;
import org.openmrs.module.bahmnimapping.model.EntityMappingType;

import java.util.List;

public interface EntityMappingDao {
    List<EntityMapping> getMappingsOfEntity(String entity1Uuid, String mappingTypeName);
    List<EntityMapping> getAllEntityMappings(String mappingTypeName);
    EntityMappingType getEntityMappingTypeByName(String name);

}
