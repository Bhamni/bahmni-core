package org.bahmni.module.bahmnicore.service.impl;

import groovy.lang.GroovyClassLoader;
import org.apache.log4j.Logger;
import org.bahmni.module.bahmnicore.service.BahmniObsCalculatorService;
import org.openmrs.module.bahmniemrapi.encountertransaction.contract.BahmniObservation;
import org.openmrs.module.bahmniemrapi.obscalculator.BahmniObsValueCalculator;
import org.openmrs.util.OpenmrsUtil;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.List;

@Service
public class BahmniObsCalculatorServiceImpl implements BahmniObsCalculatorService{

    private static Logger logger = Logger.getLogger(BahmniObsCalculatorServiceImpl.class);

    @Override
    public String calculateObsFrom(List<BahmniObservation> bahmniObservations) throws Throwable {
        logger.info("BahmniObsCalculatorServiceImpl : Start");
        GroovyClassLoader gcl = new GroovyClassLoader();
        Class clazz = gcl.parseClass(new File(OpenmrsUtil.getApplicationDataDirectory() + "obscalculator/BahmniObsCalculator.groovy"));
        logger.info("BahmniObsCalculatorServiceImpl : Using rules in " + clazz.getName());
        BahmniObsValueCalculator bahmniObsValueCalculator = (BahmniObsValueCalculator) clazz.newInstance();
        String computedObservation = bahmniObsValueCalculator.run(bahmniObservations);
        logger.info("BahmniObsCalculatorServiceImpl : Done");
        return computedObservation;
    }
}
