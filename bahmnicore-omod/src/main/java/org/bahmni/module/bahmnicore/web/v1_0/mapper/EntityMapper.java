package org.bahmni.module.bahmnicore.web.v1_0.mapper;

import org.bahmni.module.bahmnicore.contract.entityMapping.Entity;
import org.bahmni.module.bahmnicore.dao.EntityDao;
import org.openmrs.module.bahmnimapping.model.EntityMapping;
import org.openmrs.module.bahmnimapping.model.EntityMappingType;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class EntityMapper {

    public List<Entity> map(Collection<EntityMapping> entityMappings, EntityDao entityDao, EntityMappingType entityMappingType,String entityUuid) {
        List<Entity> entityMappingList = new ArrayList<Entity>();
        Class entity1Class = null;
        Class entity2Class = null;
        try {
            entity1Class = Class.forName(entityMappingType.getEntity1Type());
            entity2Class = Class.forName(entityMappingType.getEntity2Type());
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
        if (entityMappings.isEmpty() && entityUuid!=null) {
            Object entity = entityDao.getByUuid(entityUuid, entity1Class);
            entityMappingList.add(new Entity(entity, new ArrayList<Object>()));
            return  entityMappingList;

        } else {

            for (EntityMapping entityMapping : entityMappings) {
                boolean foundEntity = false;
                List<Object> mappings = new ArrayList<>();
                Object entity = entityDao.getByUuid(entityMapping.getEntity1Uuid(), entity1Class);
                Object mappedEntity = entityDao.getByUuid(entityMapping.getEntity2Uuid(), entity2Class);
                for (Entity entityMap : entityMappingList) {
                    if (entityMap.getEntity().equals(entity)) {
                        entityMap.addMapping(mappedEntity);
                        foundEntity = true;
                        break;
                    }
                }
                if (!foundEntity) {
                    mappings.add(mappedEntity);
                    entityMappingList.add(new Entity(entity, mappings));
                }
            }
            return entityMappingList;
        }

    }
}
