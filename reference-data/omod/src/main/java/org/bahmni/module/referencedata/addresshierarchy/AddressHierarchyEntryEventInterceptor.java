package org.bahmni.module.referencedata.addresshierarchy;

import org.ict4h.atomfeed.server.repository.AllEventRecordsQueue;
import org.ict4h.atomfeed.server.repository.jdbc.AllEventRecordsQueueJdbcImpl;
import org.ict4h.atomfeed.server.service.Event;
import org.ict4h.atomfeed.server.service.EventService;
import org.ict4h.atomfeed.server.service.EventServiceImpl;
import org.ict4h.atomfeed.transaction.AFTransactionWorkWithoutResult;
import org.joda.time.DateTime;
import org.openmrs.api.context.Context;
import org.openmrs.module.addresshierarchy.AddressHierarchyEntry;
import org.openmrs.module.atomfeed.transaction.support.AtomFeedSpringTransactionManager;
import org.springframework.aop.AfterReturningAdvice;
import org.springframework.transaction.PlatformTransactionManager;

import java.lang.reflect.Method;
import java.net.URI;
import java.util.List;
import java.util.UUID;

import static java.util.Arrays.asList;

public class AddressHierarchyEntryEventInterceptor implements AfterReturningAdvice {

    private final AtomFeedSpringTransactionManager atomFeedSpringTransactionManager;
    private final EventService eventService;

    private static final List<String> SAVE_ADDRESS_HIERARCY_ENTRY_METHODS = asList("saveAddressHierarchyEntries", "saveAddressHierarchyEntry");
    private static final String TEMPLATE = "/openmrs/ws/rest/v1/addressHierarchy/%s";
    private static final String CATEGORY = "addressHierarchy";
    private static final String TITLE = "addressHierarchy";

    public AddressHierarchyEntryEventInterceptor() {
        atomFeedSpringTransactionManager = new AtomFeedSpringTransactionManager(getSpringPlatformTransactionManager());
        AllEventRecordsQueue allEventRecordsQueue = new AllEventRecordsQueueJdbcImpl(atomFeedSpringTransactionManager);
        this.eventService = new EventServiceImpl(allEventRecordsQueue);
    }

    @Override
    public void afterReturning(Object returnValue, Method method, Object[] arguments, Object target) throws Exception {
        if (SAVE_ADDRESS_HIERARCY_ENTRY_METHODS.contains(method.getName())) {
            createEvents(arguments);
        }
    }

    private void createEvents(Object[] arguments) {
        if (arguments == null) {
            return;
        }
        if (arguments[0] instanceof List) {
            List<AddressHierarchyEntry> entries = (List<AddressHierarchyEntry>) arguments[0];
            for (AddressHierarchyEntry entry : entries) {
                createAndNotifyEvent(entry);
            }
            return;
        }
        createAndNotifyEvent((AddressHierarchyEntry) arguments[0]);
    }

    private void createAndNotifyEvent(AddressHierarchyEntry entry) {
        if (entry == null) {
            return;
        }
        String contents = String.format(TEMPLATE, entry.getUuid());
        final Event event = new Event(UUID.randomUUID().toString(), TITLE, DateTime.now(), (URI) null, contents, CATEGORY);

        atomFeedSpringTransactionManager.executeWithTransaction(
                new AFTransactionWorkWithoutResult() {
                    @Override
                    protected void doInTransaction() {
                        eventService.notify(event);
                    }

                    @Override
                    public PropagationDefinition getTxPropagationDefinition() {
                        return PropagationDefinition.PROPAGATION_REQUIRED;
                    }
                }
        );
    }

    private PlatformTransactionManager getSpringPlatformTransactionManager() {
        List<PlatformTransactionManager> platformTransactionManagers = Context.getRegisteredComponents(PlatformTransactionManager.class);
        return platformTransactionManagers.get(0);
    }
}
