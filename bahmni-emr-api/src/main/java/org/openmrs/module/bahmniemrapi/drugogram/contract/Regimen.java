package org.openmrs.module.bahmniemrapi.drugogram.contract;

import java.util.*;

public class Regimen {
    private Set<String> headers = new HashSet<>();
    private SortedSet<RegimenRow> rows = new TreeSet<>();

    public Set<String> getHeaders() {
        return headers;
    }

    public void setHeaders(Set<String> headers) {
        this.headers = headers;
    }

    public SortedSet<RegimenRow> getRows() {
        return rows;
    }

    public void setRows(SortedSet<RegimenRow> rows) {
        this.rows = rows;
    }
}
