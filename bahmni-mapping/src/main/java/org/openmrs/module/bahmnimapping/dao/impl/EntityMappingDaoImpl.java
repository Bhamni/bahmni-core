package org.openmrs.module.bahmnimapping.dao.impl;

import org.hibernate.Query;
import org.hibernate.SessionFactory;
import org.hibernate.classic.Session;
import org.openmrs.module.bahmnimapping.dao.EntityMappingDao;
import org.openmrs.module.bahmnimapping.model.EntityMapping;
import org.openmrs.module.bahmnimapping.model.EntityMappingType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class EntityMappingDaoImpl implements EntityMappingDao {

    @Autowired
    private SessionFactory sessionFactory;

    @Override
    public List<EntityMapping> getMappingsOfEntity(String entity1Uuid, String mappingTypeName) {
        Session currentSession = sessionFactory.getCurrentSession();

        Query query = currentSession.createQuery(
                "select em " +
                        "from EntityMapping em, EntityMappingType emt " +
                "where em.entityMappingType = emt.id " +
                        "and emt.name = :mappingTypeName " +
                        "and em.entity1Uuid = :entity1Uuid "
        );
        query.setParameter("mappingTypeName", mappingTypeName);
        query.setParameter("entity1Uuid", entity1Uuid);
        return (List<EntityMapping>)query.list();
    }

    @Override
    public List<EntityMapping> getAllEntityMappings(String mappingTypeName) {
        Session currentSession = sessionFactory.getCurrentSession();

        Query query = currentSession.createQuery(
                "select em " +
                        "from EntityMapping em, EntityMappingType emt " +
                        "where em.entityMappingType = emt.id " +
                        "and emt.name = :mappingTypeName "

        );
        query.setParameter("mappingTypeName", mappingTypeName);
        return (List<EntityMapping>)query.list();
    }

    @Override
    public EntityMappingType getEntityMappingTypeByName(String name) {
        Session currentSession = sessionFactory.getCurrentSession();
        Query query = currentSession.createQuery("from EntityMappingType where name = :name");
        query.setParameter("name",name);
        List list = query.list();
        return list.size()>0? (EntityMappingType) list.get(0) :null;
    }
}
