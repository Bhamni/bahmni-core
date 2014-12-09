package org.openmrs.module.bahmniemrapi.obscalculator;

import org.openmrs.module.bahmniemrapi.encountertransaction.contract.BahmniObservation;

import java.util.List;

public interface BahmniObsValueCalculator {
    public String run(List<BahmniObservation> bahmniObservations);
}
