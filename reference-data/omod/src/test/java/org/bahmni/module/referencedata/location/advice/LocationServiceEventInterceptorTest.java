package org.bahmni.module.referencedata.location.advice;

import org.ict4h.atomfeed.server.service.EventService;
import org.ict4h.atomfeed.transaction.AFTransactionWorkWithoutResult;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.openmrs.Location;
import org.openmrs.api.LocationService;
import org.openmrs.api.context.Context;
import org.openmrs.module.atomfeed.transaction.support.AtomFeedSpringTransactionManager;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.springframework.orm.hibernate3.HibernateTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;

import java.lang.reflect.Method;
import java.util.ArrayList;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@PrepareForTest({Context.class, LocationServiceEventInterceptor.class})
@RunWith(PowerMockRunner.class)
public class LocationServiceEventInterceptorTest {
    @Mock
    private AtomFeedSpringTransactionManager atomFeedSpringTransactionManager;
    @Mock
    private EventService eventService;

    private LocationServiceEventInterceptor publishedFeed;
    private Location location;

    @Before
    public void setUp() throws Exception {
        location = new Location();
        location.setUuid("uuid");
        location.setCountyDistrict("District");
        PowerMockito.mockStatic(Context.class);

        ArrayList<PlatformTransactionManager> platformTransactionManagers = new ArrayList<>();
        platformTransactionManagers.add(new HibernateTransactionManager());
        when(Context.getRegisteredComponents(PlatformTransactionManager.class)).thenReturn(platformTransactionManagers);
        PowerMockito.whenNew(AtomFeedSpringTransactionManager.class).withAnyArguments().thenReturn(atomFeedSpringTransactionManager);
        publishedFeed = new LocationServiceEventInterceptor();

    }

    @Test
    public void shouldPublishUpdateEventToFeedAfterUpdateConceptOperation() throws Throwable {
        Method method = LocationService.class.getMethod("saveLocation", Location.class);
        Object[] objects = new Object[]{location};

        publishedFeed.afterReturning(location, method, objects, null);
        verify(atomFeedSpringTransactionManager).executeWithTransaction(any(AFTransactionWorkWithoutResult.class));
    }

}