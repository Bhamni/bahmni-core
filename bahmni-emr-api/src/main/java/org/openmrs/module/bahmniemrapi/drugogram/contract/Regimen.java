package org.openmrs.module.bahmniemrapi.drugogram.contract;

import org.openmrs.Concept;
import org.openmrs.module.emrapi.encounter.domain.EncounterTransaction;

import java.util.*;

public class Regimen {
    private Set<EncounterTransaction.Concept> headers = new LinkedHashSet<>();
    private SortedSet<RegimenRow> rows = new TreeSet<>();

    public Set<EncounterTransaction.Concept> getHeaders() {
        return headers;
    }

    public void setHeaders(Set<EncounterTransaction.Concept> headers) {
        this.headers = headers;
    }

    public SortedSet<RegimenRow> getRows() {
        return rows;
    }

    public void setRows(SortedSet<RegimenRow> rows) {
        this.rows = rows;
    }
}
