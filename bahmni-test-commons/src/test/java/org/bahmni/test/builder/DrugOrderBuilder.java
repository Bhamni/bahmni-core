/**
 * The contents of this file are subject to the OpenMRS Public License
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://license.openmrs.org
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
 *
 * Copyright (C) OpenMRS, LLC.  All Rights Reserved.
 */
package org.bahmni.test.builder;

import org.openmrs.Concept;
import org.openmrs.ConceptName;
import org.openmrs.Drug;
import org.openmrs.DrugOrder;
import org.openmrs.Order;
import org.openmrs.OrderFrequency;
import org.openmrs.OrderType;
import org.openmrs.Person;
import org.openmrs.PersonName;
import org.openmrs.User;
import org.openmrs.Visit;
import org.springframework.util.ReflectionUtils;

import java.util.Date;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

public class DrugOrderBuilder {
    private DrugOrder order;

    public DrugOrderBuilder() {
        this.order = new DrugOrder();
        this.order.setUuid(UUID.randomUUID().toString());
        this.order.setDateCreated(null);
        this.order.setDrug(new Drug(123));
        this.order.setOrderType(new OrderType());
    }

    public DrugOrderBuilder withUuid(UUID uuid) {
        order.setUuid(String.valueOf(uuid));
        return this;
    }

    public DrugOrderBuilder withId(Integer id) {
        order.setId(id);
        return this;
    }
    public DrugOrderBuilder withDrugName(String drugName) {
        order.getDrug().setName(drugName);
        return this;
    }

    public DrugOrderBuilder withDosingType(java.lang.Class<? extends org.openmrs.DosingInstructions> dosingType){
        order.setDosingType(dosingType);
        return this;
    }

    public DrugOrderBuilder withDose(Double dose){
        order.setDose(dose);
        return this;
    }

    public DrugOrderBuilder withDrugForm(String form){
        Concept dosageForm = new Concept();
        dosageForm.setFullySpecifiedName(new ConceptName(form, Locale.getDefault()));
        order.getDrug().setDosageForm(dosageForm);
        return this;
    }

    public DrugOrderBuilder withDosingInstructions(String dosingInstructions){
        order.setDosingInstructions(dosingInstructions);
        return this;
    }

    public DrugOrderBuilder withDateActivated(Date date){
        order.setDateActivated(date);
        return this;
    }

    public DrugOrderBuilder withScheduledDate(Date date){
        order.setUrgency(Order.Urgency.ON_SCHEDULED_DATE);
        order.setScheduledDate(date);
        return this;
    }

    public DrugOrder build() {
        return order;
    }

    public DrugOrderBuilder withDoseUnits(String doseUnitsString) {
        Concept doseUnits = new Concept();
        doseUnits.setFullySpecifiedName(new ConceptName(doseUnitsString, Locale.getDefault()));
        order.setDoseUnits(doseUnits);
        return this;
    }

    public DrugOrderBuilder withDuration(int duration) {
        order.setDuration(duration);
        return this;
    }

    public DrugOrderBuilder withDurationUnits(String unit) {
        Concept durationUnit = new Concept();
        durationUnit.setFullySpecifiedName(new ConceptName(unit, Locale.getDefault()));
        order.setDurationUnits(durationUnit);
        return this;
    }

    public DrugOrderBuilder withAutoExpireDate(Date date) {
        order.setAutoExpireDate(date);
        return this;
    }

    public DrugOrderBuilder withFrequency(String frequency) {
        final Concept frequencyConcept = new Concept();
        frequencyConcept.setFullySpecifiedName(new ConceptName(frequency, Locale.getDefault()));
        order.setFrequency(new OrderFrequency() {{
            setConcept(frequencyConcept);
        }});
        return this;
    }

    public DrugOrderBuilder withRoute(String route) {
        final Concept routeConcept = new Concept();
        routeConcept.setFullySpecifiedName(new ConceptName(route, Locale.getDefault()));
        order.setRoute(routeConcept);
        return this;
    }


    public DrugOrderBuilder withVisit(Visit visit) {
        order.setEncounter(visit.getEncounters().iterator().next());
        order.getEncounter().setVisit(visit);
        return this;
    }

    public DrugOrderBuilder withCreator(String personNameValue){
        Person personObj = new Person();
        PersonName personName = new PersonName();
        personName.setGivenName(personNameValue);
        Set<PersonName> personNames = new HashSet<>();
        personNames.add(personName);
        personObj.setNames(personNames);
        User user = new User(personObj);
        order.setCreator(user);
        return this;
    }

    public DrugOrderBuilder withPreviousOrder(DrugOrder previousOrder){
        order.setPreviousOrder(previousOrder);
        return this;
    }

    public DrugOrderBuilder withConcept(Concept concept) {
        order.setConcept(concept);
        return this;
    }

    public DrugOrderBuilder withOrderAction(Order.Action action) {
        order.setAction(action);
        return this;
    }

    public DrugOrderBuilder withOrderNumber(String orderNum) {
        java.lang.reflect.Field orderNumberField = ReflectionUtils.findField(DrugOrder.class, "orderNumber");
        orderNumberField.setAccessible(true);
        ReflectionUtils.setField(orderNumberField,order,orderNum);
        return this;
    }
}
