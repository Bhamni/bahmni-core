package org.openmrs.module.bahmniemrapi.drugogram.contract;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class RegimenRow implements Comparable<RegimenRow> {
    private Date date;
    private Map<String, String> drugs = new HashMap<>();

    public RegimenRow() {
    }

    public RegimenRow(Date date, Map<String, String> drugs) {
        this.date = date;
        this.drugs = drugs;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public Map<String, String> getDrugs() {
        return drugs;
    }

    public void setDrugs(Map<String, String> drugs) {
        this.drugs = drugs;
    }

    public void addDrugs(String name, String dose) {
        drugs.put(name, dose);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        RegimenRow that = (RegimenRow) o;

        if (!date.equals(that.date)) return false;
        return drugs.equals(that.drugs) && this.hashCode() == that.hashCode();

    }

    @Override
    public int hashCode() {
        int result = date.hashCode();
        result = 31 * result + drugs.hashCode();
        return result;
    }

    @Override
    public int compareTo(RegimenRow o) {
        if (date.after(o.date)) return 1;
        if (date.before(o.date)) return -1;
        return drugs.equals(o.drugs) && this.hashCode() == o.hashCode() ? 0 : 1;
    }
}
