package org.bahmni.module.admin.csv.models;

import java.util.ArrayList;
import java.util.List;

public class ConceptRows {
    private List<ConceptRow> conceptRows;
    private List<ConceptSetRow> conceptSetRows;

    public List<ConceptRow> getConceptRows() {
        return conceptRows == null ? new ArrayList<ConceptRow>() : conceptRows;
    }

    public void setConceptRows(List<ConceptRow> conceptRows) {
        this.conceptRows = conceptRows;
    }

    public List<ConceptSetRow> getConceptSetRows() {
        return conceptSetRows == null ? new ArrayList<ConceptSetRow>() : conceptSetRows;
    }

    public void setConceptSetRows(List<ConceptSetRow> conceptSetRows) {
        this.conceptSetRows = conceptSetRows;
    }

    public ConceptRows makeCSVReady() {
        int maxSynonyms = getMaxSynonyms();
        int maxAnswers = getMaxAnswers();
        int maxReferenceTerms = getMaxReferenceTerms();
        int maxConceptSetReferenceTerms = getMaxConceptSetReferenceTerms();
        int maxSetMembers = getMaxSetMembers();
        conceptRows.add(0, new ConceptRow());
        conceptSetRows.add(0, new ConceptSetRow());
        for (ConceptRow conceptRow : getConceptRows()) {
            conceptRow.adjust(maxSynonyms, maxAnswers, maxReferenceTerms);
        }
        for (ConceptSetRow conceptSetRow : getConceptSetRows()) {
            conceptSetRow.adjust(maxSetMembers, maxConceptSetReferenceTerms);
        }
        conceptRows.set(0, conceptRows.get(0).getHeaders());
        conceptSetRows.set(0, conceptSetRows.get(0).getHeaders());
        return this;
    }

    private int getMaxSetMembers() {
        int maxSetMembers = 0;
        for (ConceptSetRow conceptSetRow : getConceptSetRows()) {
            if (conceptSetRow.getChildren().size() > maxSetMembers) {
                maxSetMembers = conceptSetRow.getChildren().size();
            }
        }
        return maxSetMembers;
    }

    private int getMaxSynonyms() {
        int maxSynonyms = 0;
        for (ConceptRow conceptRow : getConceptRows()) {
            if (conceptRow.getSynonyms().size() > maxSynonyms) {
                maxSynonyms = conceptRow.getSynonyms().size();
            }
        }
        return maxSynonyms;
    }

    private int getMaxAnswers() {
        int maxAnswers = 0;
        for (ConceptRow conceptRow : getConceptRows()) {
            if (conceptRow.getAnswers().size() > maxAnswers) {
                maxAnswers = conceptRow.getAnswers().size();
            }
        }
        return maxAnswers;
    }

    private int getMaxReferenceTerms() {
        int maxReferenceTerms = 0;
        for (ConceptRow conceptRow : getConceptRows()) {
            if (conceptRow.getReferenceTerms().size() > maxReferenceTerms) {
                maxReferenceTerms = conceptRow.getReferenceTerms().size();
            }
        }
        return maxReferenceTerms;
    }

    private int getMaxConceptSetReferenceTerms() {
        int maxReferenceTerms = 0;
        for (ConceptSetRow conceptSetRow : getConceptSetRows()) {
            if (conceptSetRow.referenceTerms.size() > maxReferenceTerms) {
                maxReferenceTerms = conceptSetRow.referenceTerms.size();
            }
        }
        return maxReferenceTerms;
    }
}
