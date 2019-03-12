package org.bahmni.module.referencedata.labconcepts.model.event;

import org.openmrs.Concept;
import org.ict4h.atomfeed.server.service.Event;
import org.joda.time.DateTime;
import org.openmrs.ConceptAttribute;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

public class SellableTypeEvent implements ConceptServiceOperationEvent {

    public static final String RESOURCE_TITLE = "reference data";
    public static final String SELLABLE_ATTR_NAME = "sellable";
    private final String url;
    private final String category;
    private List<String> supportedOperations = Arrays.asList("saveConcept", "updateConcept", "retireConcept", "purgeConcept");

    public SellableTypeEvent(String url, String category) {
        this.url = url;
        this.category = category;
    }

    @Override
    public Event asAtomFeedEvent(Object[] arguments) throws URISyntaxException {
        Concept concept = (Concept) arguments[0];
        String url = String.format(this.url, "resources", concept.getUuid());
        return new Event(UUID.randomUUID().toString(), RESOURCE_TITLE, DateTime.now(), new URI(url), url, this.category);
    }

    @Override
    public Boolean isApplicable(String operation, Object[] arguments) {
        if (supportedOperations.contains(operation)
                && arguments.length > 0 && arguments[0] instanceof Concept) {
            Concept concept = (Concept) arguments[0];
            Collection<ConceptAttribute> activeAttributes = concept.getActiveAttributes();
            return activeAttributes.stream().filter(a -> a.getAttributeType().getName().equalsIgnoreCase(SELLABLE_ATTR_NAME)).findFirst().isPresent();
        }
        return false;
    }
}
