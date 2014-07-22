package org.bahmni.module.bahmnicore.mapper.builder;

import org.bahmni.module.bahmnicore.model.BahmniDrugOrder;

import java.util.Date;
import java.util.UUID;

public class BahmniDrugOrderBuilder {

    private final BahmniDrugOrder bahmniDrugOrder;

    public BahmniDrugOrderBuilder() {
        bahmniDrugOrder = new BahmniDrugOrder();
        bahmniDrugOrder.setDosage(2.5);
        bahmniDrugOrder.setProductUuid(UUID.randomUUID().toString());
        bahmniDrugOrder.setQuantity(3.0);
        bahmniDrugOrder.setStartDate(new Date());
        bahmniDrugOrder.setUnit("ml");
    }

    public BahmniDrugOrderBuilder withProductUuid(String productUuid) {
        bahmniDrugOrder.setProductUuid(productUuid);
        return this;
    }

    public BahmniDrugOrder build() {
        return bahmniDrugOrder;
    }

    public BahmniDrugOrderBuilder withStartDate(Date date) {
        bahmniDrugOrder.setStartDate(date);
        return this;
    }

    public BahmniDrugOrderBuilder withNumberOfDaysAndDosage(int numberOfDays, Double dosage) {
        bahmniDrugOrder.setDosage(dosage);
        bahmniDrugOrder.setQuantity(bahmniDrugOrder.getDosage() * numberOfDays);
        return this;
    }
}
