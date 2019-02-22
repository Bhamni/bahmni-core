package org.bahmni.module.referencedata.labconcepts.model.event;

import org.ict4h.atomfeed.server.service.Event;
import org.junit.Assert;
import org.openmrs.Concept;
import org.junit.Test;
import org.openmrs.ConceptAttribute;
import org.openmrs.ConceptAttributeType;

import static org.bahmni.module.referencedata.labconcepts.model.event.ConceptServiceEventFactory.CONCEPT_URL;
import static org.bahmni.module.referencedata.labconcepts.model.event.ConceptServiceEventFactory.SELLABLE;

public class SellableTypeEventTest {

    @Test
    public void shouldRaiseEventForConceptWithSellableAttribute() throws Exception {
        ConceptAttributeType cat = new ConceptAttributeType();
        cat.setDatatypeClassname("org.openmrs.customdatatype.datatype.BooleanDatatype");
        cat.setName("sellable");

        Concept procedureConcept = new org.bahmni.test.builder.ConceptBuilder()
                .withClass("Procedure")
                .withUUID("random-concept-uuid")
                .withName("Dressing Procedure")
                .build();

        ConceptAttribute ca = new ConceptAttribute();
        ca.setAttributeType(cat);
        ca.setVoided(false);
        ca.setValue(true);
        procedureConcept.addAttribute(ca);

        SellableTypeEvent sellableTypeEvent = new SellableTypeEvent(CONCEPT_URL, SELLABLE);
        Assert.assertEquals(true, sellableTypeEvent.isApplicable("saveConcept", new Object[]{procedureConcept}));

        Event event = sellableTypeEvent.asAtomFeedEvent(new Object[]{procedureConcept});
        Assert.assertNotNull(event);
        Assert.assertEquals(SELLABLE, event.getCategory());
        Assert.assertEquals("/openmrs/ws/rest/v1/reference-data/sellable/random-concept-uuid", event.getContents());
    }
}
