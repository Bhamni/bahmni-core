package org.openmrs.module.bahmniemrapi.drugorder.dosinginstructions;

import org.openmrs.DosingInstructions;
import org.openmrs.DrugOrder;
import org.openmrs.api.APIException;
import org.openmrs.module.bahmniemrapi.drugorder.DrugOrderUtil;
import org.springframework.validation.Errors;

import java.util.Date;
import java.util.Locale;

public class FlexibleDosingInstructions implements DosingInstructions {

    @Override
    public String getDosingInstructionsAsString(Locale locale) {
        return null;
    }

    @Override
    public void setDosingInstructions(DrugOrder order) {
        order.setDosingType(this.getClass());
    }

    public DrugOrderUtil drugOrderUtil;

    @Override
    public DosingInstructions getDosingInstructions(DrugOrder order) {
        if (!order.getDosingType().equals(this.getClass())) {
            throw new APIException("Dosing type of drug order is mismatched. Expected:" + this.getClass() + " but received:"
                    + order.getDosingType());
        }
        return new FlexibleDosingInstructions();
    }

    @Override
    public void validate(DrugOrder order, Errors errors) {

    }

    @Override
    public Date getAutoExpireDate(DrugOrder drugOrder) {
        return drugOrderUtil.calculateAutoExpireDate(drugOrder.getDuration(), drugOrder.getDurationUnits(), drugOrder.getNumRefills(), drugOrder.getEffectiveStartDate(), drugOrder.getFrequency());
    }

}
