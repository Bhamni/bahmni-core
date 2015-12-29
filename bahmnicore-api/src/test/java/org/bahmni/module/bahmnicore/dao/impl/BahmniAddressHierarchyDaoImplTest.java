package org.bahmni.module.bahmnicore.dao.impl;

import org.bahmni.module.bahmnicore.dao.BahmniAddressHierarchyDao;
import org.hibernate.Query;
import org.hibernate.SessionFactory;
import org.hibernate.classic.Session;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.openmrs.module.addresshierarchy.AddressHierarchyEntry;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

public class BahmniAddressHierarchyDaoImplTest {
    @InjectMocks
    private BahmniAddressHierarchyDao bahmniAddressHierarchyDao = new BahmniAddressHierarchyDaoImpl();

    @Mock
    private SessionFactory sessionFactory;

    @Mock
    private Session session;

    @Mock
    private Query query;

    @Before
    public void setUp() throws Exception {
        initMocks(this);
    }

    @Test
    public void shouldGetAddressHierarchyEntryByUuid() throws Exception {
        when(sessionFactory.getCurrentSession()).thenReturn(session);
        when(session.createQuery(anyString())).thenReturn(query);
        AddressHierarchyEntry addressHierarchyEntry = new AddressHierarchyEntry();
        addressHierarchyEntry.setName("test");
        when(query.uniqueResult()).thenReturn(addressHierarchyEntry);

        AddressHierarchyEntry hierarchyEntryByUuid = bahmniAddressHierarchyDao.getAddressHierarchyEntryByUuid("uuid");

        verify(sessionFactory, times(1)).getCurrentSession();
        verify(session, times(1)).createQuery(anyString());
        verify(query, times(1)).uniqueResult();
        assertEquals(addressHierarchyEntry.getName(), hierarchyEntryByUuid.getName());
    }
}