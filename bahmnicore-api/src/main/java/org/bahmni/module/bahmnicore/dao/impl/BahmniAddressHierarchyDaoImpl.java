package org.bahmni.module.bahmnicore.dao.impl;

import org.bahmni.module.bahmnicore.dao.BahmniAddressHierarchyDao;
import org.hibernate.Query;
import org.hibernate.SessionFactory;
import org.openmrs.module.addresshierarchy.AddressHierarchyEntry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class BahmniAddressHierarchyDaoImpl implements BahmniAddressHierarchyDao {
    @Autowired
    private SessionFactory sessionFactory;

    @Override
    public AddressHierarchyEntry getAddressHierarchyEntryByUuid(String uuid) {
        Query query = sessionFactory.getCurrentSession().createQuery(
                "select ahe from AddressHierarchyEntry as ahe where ahe.uuid=:uuid");
        query.setString("uuid", uuid);
        return (AddressHierarchyEntry) query.uniqueResult();
    }
}
