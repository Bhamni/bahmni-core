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
package org.bahmni.module.bahmnicore.mapper.builder;

import org.openmrs.Concept;
import org.openmrs.Drug;
import org.openmrs.DrugOrder;

import java.util.Date;
import java.util.UUID;

public class DrugOrderBuilder {
    private DrugOrder order;

    public DrugOrderBuilder() {
        this.order = new DrugOrder();
        this.order.setUuid(UUID.randomUUID().toString());
        this.order.setDateCreated(null);
        this.order.setDrug(new Drug(123));
        this.order.setStartDate(new Date());
    }

    public DrugOrderBuilder withUuid(UUID uuid) {
        order.setUuid(String.valueOf(uuid));
        return this;
    }

    public DrugOrderBuilder withId(Integer id) {
        order.setId(id);
        return this;
    }

    public DrugOrder build() {
        return order;
    }

    public DrugOrderBuilder withConcept(Concept concept) {
        order.getDrug().setConcept(concept);
        return this;
    }

    public DrugOrderBuilder withStartDate(Date startDate) {
        order.setStartDate(startDate);
        return this;
    }

    public DrugOrderBuilder withAutoExpireDate(Date date) {
        order.setAutoExpireDate(date);
        return this;
    }
}
