package org.bahmni.module.bahmnicore.web.v1_0.mapper;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Predicate;
import org.openmrs.Concept;
import org.openmrs.DrugOrder;
import org.openmrs.Order;
import org.openmrs.module.bahmniemrapi.drugogram.contract.Regimen;
import org.openmrs.module.bahmniemrapi.drugogram.contract.RegimenRow;
import org.openmrs.module.emrapi.encounter.ConceptMapper;
import org.openmrs.module.emrapi.encounter.domain.EncounterTransaction;
import org.springframework.stereotype.Component;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

@Component
public class DrugOrderToRegimenMapper {
    public Regimen map(List<Order> drugOrders, Set<Concept> conceptsForDrugs) throws ParseException {
        Regimen regimen = new Regimen();
        Set<Concept> headers = new LinkedHashSet<>();
        SortedSet<RegimenRow> regimenRows = new TreeSet<>();
        constructRegimenRowsForDrugsWhichAreStartedAndStoppedOnSameDate(regimenRows, drugOrders, headers);
        for (Order order : drugOrders) {
            DrugOrder drugOrder = (DrugOrder) order;
            headers.add(drugOrder.getConcept());

            constructRegimenRows(drugOrders, regimenRows, drugOrder);
        }
        Set<EncounterTransaction.Concept> headersConcept = mapHeaders(conceptsForDrugs, headers);
        regimen.setHeaders(headersConcept);
        regimen.setRows(regimenRows);
        return regimen;
    }

    private void constructRegimenRowsForDrugsWhichAreStartedAndStoppedOnSameDate(SortedSet<RegimenRow> regimenRows, List<Order> drugOrders, Set<Concept> headers) throws ParseException {
        Collection drugOrdersStartedAndStoppedOnSameDate = CollectionUtils.select(drugOrders, new Predicate() {
            @Override
            public boolean evaluate(Object o) {
                DrugOrder drugOrder = (DrugOrder) o;
                try {
                    Date startDate = drugOrder.getScheduledDate() != null ? getOnlyDate(drugOrder.getScheduledDate()) : getOnlyDate(drugOrder.getDateActivated());
                    Date stopDate = drugOrder.getDateStopped() != null ? getOnlyDate(drugOrder.getDateStopped()) : getOnlyDate(drugOrder.getAutoExpireDate());
                    return startDate.equals(stopDate);
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                return false;
            }
        });

        for (int i = 0; i < drugOrdersStartedAndStoppedOnSameDate.size(); i++) {
            DrugOrder drugOrder = (DrugOrder) CollectionUtils.get(drugOrdersStartedAndStoppedOnSameDate, i);
            headers.add(drugOrder.getConcept());
            SortedSet<RegimenRow> dateActivatedRow = findOrCreateRowForDateActivated(regimenRows, drugOrder);
            SortedSet<RegimenRow> dateStoppedRow = findOrCreateRowForForDateStopped(regimenRows, drugOrder);

            Date stoppedDate = drugOrder.getDateStopped() != null ? drugOrder.getDateStopped() : drugOrder.getAutoExpireDate();
            if (i > 0 && dateActivatedRow.iterator().next().getDate().equals(getOnlyDate(stoppedDate))) {
                constructRowForDateActivated(drugOrder, dateActivatedRow.iterator().next());
                constructRowForDateStopped(drugOrder, stoppedDate, (RegimenRow) CollectionUtils.get(dateStoppedRow, 1));
            } else {
                constructRowsForDateActivated(dateActivatedRow, drugOrder);
                constructRowsForDateStopped(dateStoppedRow, drugOrder);
            }

            regimenRows.addAll(dateActivatedRow);
            regimenRows.addAll(dateStoppedRow);
        }

        drugOrders.removeAll(drugOrdersStartedAndStoppedOnSameDate);
    }

    private Set<EncounterTransaction.Concept> mapHeaders(Set<Concept> conceptsForDrugs, Set<Concept> headers) {
        Set<EncounterTransaction.Concept> headersConcept = new LinkedHashSet<>();
        if (CollectionUtils.isEmpty(conceptsForDrugs)) {
            for (Concept header : headers) {
                headersConcept.add(new ConceptMapper().map(header));
            }
            return headersConcept;
        }
        for (Concept header : conceptsForDrugs) {
            headersConcept.add(new ConceptMapper().map(header));
        }
        return headersConcept;
    }

    private void constructRegimenRows(List<Order> drugOrders, SortedSet<RegimenRow> regimenRows, DrugOrder drugOrder) throws ParseException {
        SortedSet<RegimenRow> dateActivatedRow = findOrCreateRowForDateActivated(regimenRows, drugOrder);
        SortedSet<RegimenRow> dateStoppedRow = findOrCreateRowForForDateStopped(regimenRows, drugOrder);

        for (Order order1 : drugOrders) {
            DrugOrder drugOrder1 = (DrugOrder) order1;

            constructRowsForDateActivated(dateActivatedRow, drugOrder1);
            constructRowsForDateStopped(dateStoppedRow, drugOrder1);

        }
        regimenRows.addAll(dateActivatedRow);
        regimenRows.addAll(dateStoppedRow);
    }

    private void constructRowsForDateStopped(SortedSet<RegimenRow> dateStoppedRow, DrugOrder drugOrder1) throws ParseException {
        Date stoppedDate = drugOrder1.getDateStopped() != null ? drugOrder1.getDateStopped() : drugOrder1.getAutoExpireDate();
        if (dateStoppedRow.iterator().next().getDate().equals(getOnlyDate(stoppedDate))) {
            dateStoppedRow.iterator().next().addDrugs(drugOrder1.getConcept().getName().getName(), "STOP");
            return;
        }

        for (RegimenRow regimenRow : dateStoppedRow) {
            constructRowForDateStopped(drugOrder1, stoppedDate, regimenRow);
        }
    }

    private void constructRowForDateStopped(DrugOrder drugOrder1, Date stoppedDate, RegimenRow regimenRow) throws ParseException {
        if (orderCrossDate(drugOrder1, regimenRow.getDate())) {

            if (getOnlyDate(stoppedDate).equals(regimenRow.getDate()))
                regimenRow.addDrugs(drugOrder1.getConcept().getName().getName(), "STOP");
            else
                regimenRow.addDrugs(drugOrder1.getConcept().getName().getName(), drugOrder1.getDose().toString());
        }
    }

    private void constructRowsForDateActivated(SortedSet<RegimenRow> dateActivatedRow, DrugOrder drugOrder1) throws ParseException {
        for (RegimenRow regimenRow : dateActivatedRow) {
            constructRowForDateActivated(drugOrder1, regimenRow);
        }
    }

    private void constructRowForDateActivated(DrugOrder drugOrder1, RegimenRow regimenRow) throws ParseException {
        if (orderCrossDate(drugOrder1, regimenRow.getDate()))
            regimenRow.addDrugs(drugOrder1.getConcept().getName().getName(), drugOrder1.getDose().toString());
    }

    private boolean orderCrossDate(DrugOrder drugOrder, Date date) throws ParseException {
        Date autoExpiryDate = drugOrder.getDateStopped() != null ? getOnlyDate(drugOrder.getDateStopped()) : getOnlyDate(drugOrder.getAutoExpireDate());
        Date dateActivated = drugOrder.getScheduledDate() != null ? getOnlyDate(drugOrder.getScheduledDate()) : getOnlyDate(drugOrder.getDateActivated());
        return dateActivated.equals(date)
                || autoExpiryDate.equals(date)
                || dateActivated.before(date) && autoExpiryDate.after(date);
    }

    private SortedSet<RegimenRow> findOrCreateRowForDateActivated(SortedSet<RegimenRow> regimenRows, DrugOrder drugOrder) throws ParseException {
        Date date = drugOrder.getScheduledDate() != null ? getOnlyDate(drugOrder.getScheduledDate()) : getOnlyDate(drugOrder.getDateActivated());

        return getRegimenRowFor(regimenRows, date);
    }

    private SortedSet<RegimenRow> findOrCreateRowForForDateStopped(SortedSet<RegimenRow> regimenRows, DrugOrder drugOrder) throws ParseException {
        Date date = drugOrder.getDateStopped() != null ? getOnlyDate(drugOrder.getDateStopped()) : getOnlyDate(drugOrder.getAutoExpireDate());

        return getRegimenRowFor(regimenRows, date);
    }

    private SortedSet<RegimenRow> getRegimenRowFor(SortedSet<RegimenRow> regimenRows, Date date) {
        SortedSet<RegimenRow> foundRows = new TreeSet<>();
        for (RegimenRow regimenRow : regimenRows) {
            if (regimenRow.getDate().equals(date)) {
                foundRows.add(regimenRow);
            }
        }
        if (CollectionUtils.isNotEmpty(foundRows)) {
            return foundRows;
        }

        RegimenRow regimenRow = new RegimenRow();
        regimenRow.setDate(date);
        foundRows.add(regimenRow);
        return foundRows;
    }

    private Date getOnlyDate(Date date) throws ParseException {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        return sdf.parse(sdf.format(date));
    }
}
