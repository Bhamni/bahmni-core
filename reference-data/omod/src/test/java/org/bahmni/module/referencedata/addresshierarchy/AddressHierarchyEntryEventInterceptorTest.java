package org.bahmni.module.referencedata.addresshierarchy;

import org.ict4h.atomfeed.server.service.EventService;
import org.ict4h.atomfeed.transaction.AFTransactionWorkWithoutResult;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.openmrs.api.context.Context;
import org.openmrs.module.addresshierarchy.AddressHierarchyEntry;
import org.openmrs.module.addresshierarchy.service.AddressHierarchyService;
import org.openmrs.module.atomfeed.transaction.support.AtomFeedSpringTransactionManager;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.springframework.orm.hibernate3.HibernateTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;
import static org.powermock.api.mockito.PowerMockito.whenNew;

@PrepareForTest({Context.class, AddressHierarchyEntryEventInterceptor.class})
@RunWith(PowerMockRunner.class)
public class AddressHierarchyEntryEventInterceptorTest {
    @Mock
    private AtomFeedSpringTransactionManager atomFeedSpringTransactionManager;
    @Mock
    private EventService eventService;

    private AddressHierarchyEntryEventInterceptor publishedFeed;
    private AddressHierarchyEntry addressHierarchyEntry;

    @Before
    public void setUp() throws Exception {
        addressHierarchyEntry = new AddressHierarchyEntry();
        addressHierarchyEntry.setUuid("uuid");
        addressHierarchyEntry.setUserGeneratedId("707070");
        PowerMockito.mockStatic(Context.class);

        ArrayList<PlatformTransactionManager> platformTransactionManagers = new ArrayList<>();
        platformTransactionManagers.add(new HibernateTransactionManager());
        when(Context.getRegisteredComponents(PlatformTransactionManager.class)).thenReturn(platformTransactionManagers);
        whenNew(AtomFeedSpringTransactionManager.class).withAnyArguments().thenReturn(atomFeedSpringTransactionManager);
        publishedFeed = new AddressHierarchyEntryEventInterceptor();

    }

    @Test
    public void shouldPublishToFeedAfterSavingAddressHierarchyEntry() throws Throwable {
        Method method = AddressHierarchyService.class.getMethod("saveAddressHierarchyEntry", AddressHierarchyEntry.class);
        Object[] objects = new Object[]{addressHierarchyEntry};

        publishedFeed.afterReturning(null, method, objects, null);
        verify(atomFeedSpringTransactionManager).executeWithTransaction(any(AFTransactionWorkWithoutResult.class));
    }

    @Test
    public void shouldPublishToFeedAfterSavingAddressHierarchyEntries() throws Throwable {
        Method method = AddressHierarchyService.class.getMethod("saveAddressHierarchyEntries", List.class);
        ArrayList<Object> entries = new ArrayList<>();
        entries.add(addressHierarchyEntry);
        entries.add(addressHierarchyEntry);
        Object[] objects = new Object[]{entries};

        publishedFeed.afterReturning(null, method, objects, null);
        verify(atomFeedSpringTransactionManager, times(2)).executeWithTransaction(any(AFTransactionWorkWithoutResult.class));
    }

    @Test
    public void shouldNotCreateEventIfParameterIsNull() throws Exception {
        Method method = AddressHierarchyService.class.getMethod("saveAddressHierarchyEntries", List.class);

        publishedFeed.afterReturning(null, method, null, null);

        verify(atomFeedSpringTransactionManager, never()).executeWithTransaction(any(AFTransactionWorkWithoutResult.class));
    }

    @Test
    public void shouldNotCreateEventIfEntryInParameterIsNull() throws Exception {
        Method method = AddressHierarchyService.class.getMethod("saveAddressHierarchyEntries", List.class);
        ArrayList<Object> entries = new ArrayList<>();
        entries.add(null);

        Object[] objects = new Object[]{entries};

        publishedFeed.afterReturning(null, method, objects, null);

        verify(atomFeedSpringTransactionManager, never()).executeWithTransaction(any(AFTransactionWorkWithoutResult.class));
    }
}