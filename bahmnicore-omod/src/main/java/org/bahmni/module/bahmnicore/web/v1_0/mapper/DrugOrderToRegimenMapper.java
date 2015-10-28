package org.bahmni.module.bahmnicore.web.v1_0.mapper;

import org.openmrs.DrugOrder;
import org.openmrs.Order;
import org.openmrs.module.bahmniemrapi.drugogram.contract.Regimen;
import org.openmrs.module.bahmniemrapi.drugogram.contract.RegimenRow;
import org.springframework.stereotype.Component;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

@Component
public class DrugOrderToRegimenMapper {
    public Regimen map(List<Order> drugOrders) throws ParseException {
        Regimen regimen = new Regimen();
        Set<String> headers = new HashSet<>();
        SortedSet<RegimenRow> regimenRows = new TreeSet<>();
        for (Order order : drugOrders) {
            DrugOrder drugOrder = (DrugOrder) order;
            headers.add(drugOrder.getConcept().getName().getName());

            constructRegimenRows(drugOrders, regimenRows, drugOrder);
        }
        regimen.setHeaders(headers);
        regimen.setRows(regimenRows);
        return regimen;
    }

    private void constructRegimenRows(List<Order> drugOrders, SortedSet<RegimenRow> regimenRows, DrugOrder drugOrder) throws ParseException {
        RegimenRow dateActivatedRow = findOrCreateRowFor(regimenRows, getOnlyDate(drugOrder.getDateActivated()));
        RegimenRow dateStoppedRow = findOrCreateRowFor(regimenRows, drugOrder.getDateStopped() != null ? getOnlyDate(drugOrder.getDateStopped()) : getOnlyDate(drugOrder.getAutoExpireDate()));

        for (Order order1 : drugOrders) {
            DrugOrder drugOrder1 = (DrugOrder) order1;
            constructRowForDateActivated(dateActivatedRow, drugOrder1);
            constructRowForDateStopped(dateStoppedRow, drugOrder1);
        }
        regimenRows.add(dateActivatedRow);
        regimenRows.add(dateStoppedRow);
    }

    private void constructRowForDateStopped(RegimenRow dateStoppedRow, DrugOrder drugOrder1) throws ParseException {
        if (orderCrossDate(drugOrder1, dateStoppedRow.getDate())) {
            Date stoppedDate = drugOrder1.getDateStopped() != null ? drugOrder1.getDateStopped() : drugOrder1.getAutoExpireDate();
            if (getOnlyDate(stoppedDate).equals(dateStoppedRow.getDate()))
                dateStoppedRow.addDrugs(drugOrder1.getConcept().getName().getName(), "STOP");
            else
                dateStoppedRow.addDrugs(drugOrder1.getConcept().getName().getName(), drugOrder1.getDose().toString());
        }
    }

    private void constructRowForDateActivated(RegimenRow dateActivatedRow, DrugOrder drugOrder1) throws ParseException {
        if (orderCrossDate(drugOrder1, dateActivatedRow.getDate()))
            dateActivatedRow.addDrugs(drugOrder1.getConcept().getName().getName(), drugOrder1.getDose().toString());
//            if (!"STOP".equals(dateActivatedRow.getDrug(drugOrder1.getConcept().getName().getName()))) {
//            }
    }

    private boolean orderCrossDate(DrugOrder drugOrder, Date date) throws ParseException {
        Date autoExpiryDate = drugOrder.getDateStopped() != null ? getOnlyDate(drugOrder.getDateStopped()) : getOnlyDate(drugOrder.getAutoExpireDate());
        Date dateActivated = getOnlyDate(drugOrder.getDateActivated());
        return dateActivated.equals(date)
                || autoExpiryDate.equals(date)
                || dateActivated.before(date) && autoExpiryDate.after(date);
    }

    private RegimenRow findOrCreateRowFor(Collection<RegimenRow> regimenRows, Date date) throws ParseException {
        RegimenRow existingRegimenRowForDate = getRegimenRow(regimenRows, date);
        if (existingRegimenRowForDate != null) {
            return existingRegimenRowForDate;
        }
        RegimenRow regimenRow = new RegimenRow();
        regimenRow.setDate(date);
        return regimenRow;
    }

    private RegimenRow getRegimenRow(Collection<RegimenRow> regimenRows, Date date) throws ParseException {
        for (RegimenRow regimenRow : regimenRows) {
            if (regimenRow.getDate().equals(date)) {
                return regimenRow;
            }
        }
        return null;
    }

    private Date getOnlyDate(Date date) throws ParseException {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        return sdf.parse(sdf.format(date));
    }
}
