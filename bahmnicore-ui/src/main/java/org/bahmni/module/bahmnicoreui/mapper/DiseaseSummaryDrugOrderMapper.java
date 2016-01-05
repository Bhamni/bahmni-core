package org.bahmni.module.bahmnicoreui.mapper;

import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.log4j.Logger;
import org.bahmni.module.bahmnicoreui.constant.DiseaseSummaryConstants;
import org.bahmni.module.bahmnicoreui.contract.DiseaseSummaryMap;
import org.openmrs.Concept;
import org.openmrs.DrugOrder;

import java.io.IOException;
import java.util.*;
import java.lang.String;

public class DiseaseSummaryDrugOrderMapper{

    private Logger logger = Logger.getLogger(this.getClass());

    public DiseaseSummaryMap map(List<DrugOrder> drugOrders, String groupBy)  {
        DiseaseSummaryMap diseaseSummaryMap = new DiseaseSummaryMap();
        for (DrugOrder drugOrder : drugOrders) {
            String startDateTime = (DiseaseSummaryConstants.RESULT_TABLE_GROUP_BY_ENCOUNTER.equals(groupBy)) ?
                    DateFormatUtils.format(drugOrder.getEncounter().getEncounterDatetime(), DateFormatUtils.ISO_DATETIME_TIME_ZONE_FORMAT.getPattern()) : DateFormatUtils.format(drugOrder.getEncounter().getVisit().getStartDatetime(), DateFormatUtils.ISO_DATETIME_TIME_ZONE_FORMAT.getPattern());
            String conceptName = drugOrder.getDrug().getConcept().getName().getName();
            try {
                diseaseSummaryMap.put(startDateTime, conceptName, formattedDrugOrderValue(drugOrder), null, false);
            } catch (IOException e) {
                logger.error("Could not parse dosing instructions",e);
                throw new RuntimeException("Could not parse dosing instructions",e);
            }
        }
        return diseaseSummaryMap;
    }

    private String formattedDrugOrderValue(DrugOrder drugOrder) throws IOException {
        String strength = drugOrder.getDrug().getStrength();
        Concept doseUnitsConcept = drugOrder.getDoseUnits();
        String doseUnit = doseUnitsConcept == null ? "" : " " + doseUnitsConcept.getName().getName();
        String dose = drugOrder.getDose() == null ? "" : drugOrder.getDose() + doseUnit;
        String frequency = DoseInstructionMapper.getFrequency(drugOrder);
        String asNeeded = drugOrder.getAsNeeded() ? "SOS" : null;
        return strength + "," + dose + "," + frequency + "," + asNeeded;
    }
}
