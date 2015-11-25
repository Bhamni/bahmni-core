package org.openmrs.module.bahmniemrapi.drugorder.contract;

import org.openmrs.Visit;
import org.openmrs.module.bahmniemrapi.visit.contract.VisitData;
import org.openmrs.module.emrapi.CareSettingType;
import org.openmrs.module.emrapi.encounter.domain.EncounterTransaction;

import java.util.Date;
import java.util.List;


public class BahmniDrugOrder implements Comparable<BahmniDrugOrder>{

    private VisitData visit;
    private EncounterTransaction.DrugOrder drugOrder;
    private EncounterTransaction.Provider provider;
    private List<BahmniOrderAttribute> orderAttributes;

    private String creatorName;

    public String getAction() {
        return drugOrder.getAction();
    }

    public Date getAutoExpireDate() {
        return drugOrder.getAutoExpireDate();
    }

    public CareSettingType getCareSetting() {
        return drugOrder.getCareSetting();
    }

    public String getCommentToFulfiller() {
        return drugOrder.getCommentToFulfiller();
    }

    public Date getDateActivated() {
        return drugOrder.getDateActivated();
    }

    public Date getDateStopped() {
        return drugOrder.getDateStopped();
    }

    public EncounterTransaction.DosingInstructions getDosingInstructions() {
        return drugOrder.getDosingInstructions();
    }

    public String getDosingInstructionType() {
        return drugOrder.getDosingInstructionType();
    }

    public String getDrugNonCoded() {
        return drugOrder.getDrugNonCoded();
    }

    public EncounterTransaction.Drug getDrug() {
        return drugOrder.getDrug();
    }

    public Integer getDuration() {
        return drugOrder.getDuration();
    }

    public String getDurationUnits() {
        return drugOrder.getDurationUnits();
    }

    public Date getEffectiveStartDate() {
        return drugOrder.getEffectiveStartDate();
    }

    public Date getEffectiveStopDate() {
        return drugOrder.getEffectiveStopDate();
    }

    public String getPreviousOrderUuid() {
        return drugOrder.getPreviousOrderUuid();
    }

    public String getInstructions() {
        return drugOrder.getInstructions();
    }

    public EncounterTransaction.Concept getOrderReasonConcept() {
        return drugOrder.getConcept();
    }

    public String getOrderReasonText() {
        return drugOrder.getOrderReasonText();
    }

    public String getOrderType() {
        return drugOrder.getOrderType();
    }

    public Date getScheduledDate() {
        return drugOrder.getScheduledDate();
    }

    public String getUuid() {
        return drugOrder.getUuid();
    }

    public void setVisit(Visit visit) {
        this.visit = new VisitData(visit);
    }

    public VisitData getVisit() {
        return visit;
    }

    public void setDrugOrder(EncounterTransaction.DrugOrder drugOrder) {
        this.drugOrder = drugOrder;
    }

    public void setProvider(EncounterTransaction.Provider provider) {
        this.provider = provider;
    }

    public EncounterTransaction.Provider getProvider() {
        return provider;
    }

    public String getOrderNumber() {
        return drugOrder.getOrderNumber();
    }

    public List<BahmniOrderAttribute> getOrderAttributes() {
        return orderAttributes;
    }

    public void setOrderAttributes(List<BahmniOrderAttribute> orderAttributes) {
        this.orderAttributes = orderAttributes;
    }

    public String getCreatorName()
    {
        return creatorName;
    }

    public void setCreatorName(String creatorName) {
        this.creatorName = creatorName;
    }

    public void setOrderReasonConcept(EncounterTransaction.Concept concept ) {
        this.drugOrder.setOrderReasonConcept(concept);

    }
    public void  setOrderReasonText(String orderReasonText) {
        this.drugOrder.setOrderReasonText(orderReasonText);
    }

    @Override
    public boolean equals(Object otherOrder){
        if(otherOrder == null)                return false;
        if(!(otherOrder instanceof BahmniDrugOrder)) return false;

        BahmniDrugOrder other = (BahmniDrugOrder) otherOrder;
        return this.getUuid().equals(other.getUuid());
    }

    @Override
    public int compareTo(BahmniDrugOrder otherOrder) {
        return otherOrder.getEffectiveStartDate().compareTo(this.getEffectiveStartDate());
    }
}