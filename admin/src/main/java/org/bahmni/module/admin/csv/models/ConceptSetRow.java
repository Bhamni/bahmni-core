package org.bahmni.module.admin.csv.models;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.bahmni.csv.CSVEntity;
import org.bahmni.csv.KeyValue;
import org.bahmni.csv.annotation.CSVHeader;
import org.bahmni.csv.annotation.CSVRegexHeader;
import org.bahmni.csv.annotation.CSVRepeatingHeaders;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.bahmni.module.admin.csv.utils.CSVUtils.getStringArray;

public class ConceptSetRow extends CSVEntity {
    @CSVHeader(name = "uuid", optional = true)
    public String uuid;

    @CSVHeader(name = "name")
    public String name;

    @CSVHeader(name = "description", optional = true)
    public String description;

    @CSVHeader(name = "class")
    public String conceptClass;

    @CSVHeader(name = "shortname")
    public String shortName;

    @CSVRepeatingHeaders(names = {"reference-term-source", "reference-term-code", "reference-term-relationship"}, type = ConceptReferenceTermRow.class)
    public List<ConceptReferenceTermRow> referenceTerms = new ArrayList<>();

    @CSVRegexHeader(pattern = "child.*")
    public List<KeyValue> children;

    public List<KeyValue> getChildren() {
        return children == null ? new ArrayList<KeyValue>() : children;
    }

    public String getShortName() {
        return (shortName != null && StringUtils.isEmpty(shortName.trim())) ? null : shortName;
    }

    public String getName() {
        return name == null ? null : name.trim();
    }

    public String getConceptClass() {
        return conceptClass == null ? null : conceptClass.trim();
    }

    public String getUuid() {
        try {
            UUID uuid = UUID.fromString(this.uuid.trim());
            return uuid.toString();
        } catch (Exception e) {
            return null;
        }
    }

    public ConceptSetRow getHeaders() {
        int childCount = 1;
        List<KeyValue> childHeaders = new ArrayList<>();
        for (KeyValue ignored : children) {
            childHeaders.add(new KeyValue("childHeader", "child." + childCount));
            childCount++;
        }

        List<ConceptReferenceTermRow> referenceTermHeaders = new ArrayList<>();
        for (ConceptReferenceTermRow referenceTerm : referenceTerms) {
            referenceTermHeaders.add(referenceTerm.getHeaders());
        }
        return new ConceptSetRow("uuid", "name", "description", "class", "shortname", referenceTermHeaders, childHeaders);
    }

    public ConceptSetRow(String uuid, String name, String description, String conceptClass, String shortName, List<ConceptReferenceTermRow> referenceTerms, List<KeyValue> children) {
        this.uuid = uuid;
        this.name = name;
        this.description = description;
        this.conceptClass = conceptClass;
        this.shortName = shortName;
        this.children = children;
        this.referenceTerms = referenceTerms;
        String[] aRow = {uuid, name, description, conceptClass, shortName};
        String[] childrenRow = getStringArray(children);
        aRow = ArrayUtils.addAll(aRow, childrenRow);
        aRow = ArrayUtils.addAll(aRow, getReferenceTermRowValues());
        originalRow(aRow);
    }

    public ConceptSetRow() {
    }

    public void adjust(int maxSetMembers, int maxConceptSetReferenceTerms) {
        addBlankChildren(maxSetMembers);
        addBlankReferenceTerms(maxConceptSetReferenceTerms);
        String[] aRow = {uuid, name, description, conceptClass, shortName};
        String[] childrenRow = getStringArray(children);
        aRow = ArrayUtils.addAll(aRow, childrenRow);
        aRow = ArrayUtils.addAll(aRow, getReferenceTermRowValues());
        originalRow(aRow);
    }

    private void addBlankChildren(int maxSetMembers) {
        int counter = this.getChildren().size();
        this.children = this.getChildren();
        while (counter <= maxSetMembers) {
            this.children.add(new KeyValue("child", ""));
            counter++;
        }
    }

    private void addBlankReferenceTerms(int maxReferenceTerms) {
        int counter = this.referenceTerms.size();
        while (counter <= maxReferenceTerms) {
            this.referenceTerms.add(new ConceptReferenceTermRow(null, null, null));
            counter++;
        }
    }

    private String[] getReferenceTermRowValues() {
        String[] aRow = new String[0];
        for (ConceptReferenceTermRow referenceTerm : referenceTerms) {
            aRow = ArrayUtils.addAll(aRow, referenceTerm.getRowValues());
        }
        return aRow;
    }
}
