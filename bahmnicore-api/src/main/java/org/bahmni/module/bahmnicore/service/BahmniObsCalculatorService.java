package org.bahmni.module.bahmnicore.service;

import org.openmrs.module.bahmniemrapi.encountertransaction.contract.BahmniObservation;
import java.util.List;

public interface BahmniObsCalculatorService {
    public String calculateObsFrom(List<BahmniObservation> bahmniObservations) throws Throwable;
}
