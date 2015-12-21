package org.bahmni.module.bahmnicoreui.contract;

import java.util.Date;
import java.util.List;

public class DiseaseDataParams {

    private Integer numberOfVisits;
    private Integer initialCount;
    private Integer latestCount;
    private List<String> obsConcepts;
    private List<String> drugConcepts;
    private List<String> labConcepts;
    private String groupBy;
    private String visitUuid;
    private Date startDate;
    private Date endDate;

    public Integer getNumberOfVisits() {
        return numberOfVisits;
    }

    public void setNumberOfVisits(Integer numberOfVisits) {
        this.numberOfVisits = numberOfVisits;
    }

    public Integer getLatestCount() {
        return latestCount;
    }

    public void setLatestCount(Integer latestCount) {
        this.latestCount = latestCount;
    }

    public Integer getInitialCount() {
        return initialCount;
    }

    public void setInitialCount(Integer initialCount) {
        this.initialCount = initialCount;
    }

    public List<String> getObsConcepts() {
        return obsConcepts;
    }

    public void setObsConcepts(List<String> obsConcepts) {
        this.obsConcepts = obsConcepts;
    }

    public List<String> getLabConcepts() {
        return labConcepts;
    }

    public void setLabConcepts(List<String> labConcepts) {
        this.labConcepts = labConcepts;
    }

    public List<String> getDrugConcepts() {
        return drugConcepts;
    }

    public void setDrugConcepts(List<String> drugConcepts) {
        this.drugConcepts = drugConcepts;
    }

    public String getGroupBy() {
        return groupBy;
    }

    public void setGroupBy(String groupBy) {
        this.groupBy = groupBy;
    }

    public String getVisitUuid() {
        return visitUuid;
    }

    public void setVisitUuid(String visitUuid) {
        this.visitUuid = visitUuid;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }
}
