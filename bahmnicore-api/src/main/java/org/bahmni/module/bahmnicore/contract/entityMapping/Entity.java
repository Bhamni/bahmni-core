package org.bahmni.module.bahmnicore.contract.entityMapping;

import java.util.List;

public class Entity <T1,T2> {
    public T1 entity;

    public List<T2> mappings;

    public Entity () {
    }

    public Entity(T1 entity, List<T2> mappings) {
        this.entity = entity;
        this.mappings = mappings;
    }

    public T1 getEntity() {
        return entity;
    }

    public void setEntity(T1 entity) {
        this.entity = entity;
    }

    public List<T2> getMappings() {
        return mappings;
    }

    public void setMappings(List<T2> mappings) {
        this.mappings = mappings;
    }

    public void addMapping(Object mappedEntity) {
        mappings.add((T2) mappedEntity);
    }
}
