package org.bahmni.module.referencedata.labconcepts.model.event;

import org.ict4h.atomfeed.server.service.Event;
import org.joda.time.DateTime;
import org.openmrs.Concept;
import org.openmrs.Drug;

import java.net.URISyntaxException;
import java.util.List;
import java.util.UUID;

import static java.util.Arrays.asList;
import static org.bahmni.module.referencedata.labconcepts.model.event.SellableTypeEvent.isConceptWithSellableAttribute;

public class DrugEvent implements ConceptServiceOperationEvent {
    protected String url;
    protected String category;
    protected String title;

    public DrugEvent(String url, String category, String title) {
        this.url = url;
        this.category = category;
        this.title = title;
    }

    public DrugEvent() {
    }


    List<String> operations() {
        return asList("saveDrug", "purgeDrug");
    }

    private boolean isValid(Object[] arguments) {
        return arguments != null && arguments.length > 0;
    }

    @Override
    public Boolean isApplicable(String operation, Object[] arguments) {
        return this.operations().contains(operation) && isValid(arguments) && arguments[0] instanceof Drug && !isConceptWithSellableAttribute(((Drug) arguments[0]).getConcept());
    }

    @Override
    public Event asAtomFeedEvent(Object[] arguments) throws URISyntaxException {
        Drug drug = (Drug) arguments[0];
        String url = String.format(this.url, title, drug.getUuid());
        return new Event(UUID.randomUUID().toString(), title, DateTime.now(), url, url, category);
    }

}
