package org.bahmni.module.bahmnicore.model;

import org.apache.commons.lang3.time.DateUtils;

import java.util.Date;

public class BahmniDrugOrder {
    private int numberOfDays;
    private String productUuid;
    private Double quantity;
    private Double dosage;
    private String unit;
    private Date startDate;

    public int getNumberOfDays() {
        if(dosage == 0.0){
            return quantity.intValue();
        }
        return (int) (quantity / dosage);
    }

    public Date getAutoExpireDate() {
        return DateUtils.addDays(getStartDate(), getNumberOfDays());
    }

    public String getProductUuid() {
        return productUuid;
    }

    public Double getQuantity() {
        return quantity;
    }

    public Double getDosage() {
        return dosage;
    }

    public String getUnit() {
        return unit;
    }

    public BahmniDrugOrder() {
    }

    public BahmniDrugOrder(String productUuid, Double dosage, Double quantity, String unit, Date startDate) {
        this.productUuid = productUuid;
        this.quantity = quantity;
        this.dosage = dosage;
        this.unit = unit;
        this.startDate = startDate;
    }

    public void setNumberOfDays(int numberOfDays) {
        this.numberOfDays = numberOfDays;
    }

    public void setProductUuid(String productUuid) {
        this.productUuid = productUuid;
    }

    public void setQuantity(Double quantity) {
        this.quantity = quantity;
    }

    public void setDosage(Double dosage) {
        this.dosage = dosage;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Date getStartDate() {
        return startDate;
    }
}
