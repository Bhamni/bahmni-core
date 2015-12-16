package org.bahmni.module.bahmnicore.web.v1_0.controller.display.controls;

import org.apache.commons.collections.CollectionUtils;
import org.apache.log4j.Logger;
import org.bahmni.module.bahmnicore.extensions.BahmniExtensions;
import org.bahmni.module.bahmnicore.service.BahmniObsService;
import org.bahmni.module.bahmnicore.web.v1_0.mapper.BahmniObservationsToTabularViewMapper;
import org.openmrs.Concept;
import org.openmrs.api.ConceptService;
import org.openmrs.module.bahmniemrapi.drugogram.contract.BaseTableExtension;
import org.openmrs.module.bahmniemrapi.encountertransaction.contract.BahmniObservation;
import org.openmrs.module.bahmniemrapi.pivottable.contract.PivotTable;
import org.openmrs.module.emrapi.encounter.ConceptMapper;
import org.openmrs.module.emrapi.encounter.domain.EncounterTransaction;
import org.openmrs.module.webservices.rest.web.RestConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.*;

@Controller
@RequestMapping(value = "/rest/" + RestConstants.VERSION_1 + "/bahmnicore/observations/flowSheet")
public class ObsToObsTabularFlowSheetController {

    public static final String CONCEPT_DETAILS = "Concept Details";
    private BahmniObsService bahmniObsService;
    private ConceptService conceptService;
    private BahmniObservationsToTabularViewMapper bahmniObservationsToTabularViewMapper;
    private ConceptMapper conceptMapper;
    private BahmniExtensions bahmniExtensions;

    private static Logger logger = Logger.getLogger(ObsToObsTabularFlowSheetController.class);

    @Autowired
    public ObsToObsTabularFlowSheetController(BahmniObsService bahmniObsService, ConceptService conceptService,
                                              BahmniObservationsToTabularViewMapper bahmniObservationsToTabularViewMapper, BahmniExtensions bahmniExtensions) {
        this.bahmniObsService = bahmniObsService;
        this.conceptService = conceptService;
        this.bahmniObservationsToTabularViewMapper = bahmniObservationsToTabularViewMapper;
        this.conceptMapper = new ConceptMapper();
        this.bahmniExtensions = bahmniExtensions;
    }

    @RequestMapping(method = RequestMethod.GET)
    @ResponseBody
    public PivotTable constructPivotTableFor(
            @RequestParam(value = "patientUuid", required = true) String patientUuid,
            @RequestParam(value = "numberOfVisits", required = false) Integer numberOfVisits,
            @RequestParam(value = "conceptSet", required = true) String conceptSet,
            @RequestParam(value = "groupByConcept", required = true) String groupByConcept,
            @RequestParam(value = "conceptNames", required = false) List<String> conceptNames,
            @RequestParam(value = "initialCount", required = false) Integer initialCount,
            @RequestParam(value = "latestCount", required = false) Integer latestCount,
            @RequestParam(value = "name", required = false) String groovyExtension) {

        Concept rootConcept = conceptService.getConceptByName(conceptSet);
        Concept childConcept = conceptService.getConceptByName(groupByConcept);
        validate(conceptSet, groupByConcept, rootConcept, childConcept);

        Collection<BahmniObservation> bahmniObservations = bahmniObsService.observationsFor(patientUuid, rootConcept, childConcept, numberOfVisits);

        Set<EncounterTransaction.Concept> leafConcepts = new LinkedHashSet<>();
        if (CollectionUtils.isEmpty(conceptNames)) {
            getAllLeafConcepts(rootConcept, leafConcepts);
        } else {
            getSpecifiedLeafConcepts(rootConcept, conceptNames, leafConcepts);
        }
        if (!CollectionUtils.isEmpty(conceptNames)) {
            leafConcepts = sortConcepts(conceptNames, leafConcepts);
        }
        if (conceptNames != null && !conceptNames.contains(groupByConcept)) {
            leafConcepts.add(conceptMapper.map(childConcept));
        }
        bahmniObservations = filterDataByCount(bahmniObservations, initialCount, latestCount);
        PivotTable pivotTable = bahmniObservationsToTabularViewMapper.constructTable(leafConcepts, bahmniObservations, groupByConcept);
        BaseTableExtension<PivotTable> extension = bahmniExtensions.getExtension(groovyExtension + ".groovy");
        extension.update(pivotTable, patientUuid);
        return pivotTable;
    }

    private Set<EncounterTransaction.Concept> sortConcepts(List<String> conceptNames, Set<EncounterTransaction.Concept> leafConcepts) {
        Set<EncounterTransaction.Concept> sortedConcepts = new LinkedHashSet<>();
        for (String conceptName: conceptNames){
            for (EncounterTransaction.Concept leafConcept : leafConcepts) {
                if (conceptName.equals(leafConcept.getName())) {
                    sortedConcepts.add(leafConcept);
                }
            }
        }
        return sortedConcepts;
    }

    private Collection<BahmniObservation> filterDataByCount(Collection<BahmniObservation> bahmniObservations, Integer initialCount, Integer latestCount) {
        if (initialCount == null && latestCount == null) return bahmniObservations;
        Collection<BahmniObservation> bahmniObservationCollection = new ArrayList<>();

        if (bahmniObservations.size() < (getIntegerValue(initialCount) + getIntegerValue(latestCount))) {
            latestCount = bahmniObservations.size();
            initialCount = 0;
        }
        bahmniObservationCollection.addAll(filter(bahmniObservations, 0, getIntegerValue(initialCount)));
        bahmniObservationCollection.addAll(filter(bahmniObservations, bahmniObservations.size() - getIntegerValue(latestCount), bahmniObservations.size()));

        return bahmniObservationCollection;
    }

    private Collection<BahmniObservation> filter(Collection<BahmniObservation> bahmniObservations, int fromIndex, int toIndex) {
        Collection<BahmniObservation> bahmniObservationCollection = new ArrayList<>();
        fromIndex = (fromIndex > bahmniObservations.size() || fromIndex < 0) ? 0 : fromIndex;
        toIndex = (toIndex > bahmniObservations.size()) ? bahmniObservations.size() : toIndex;
        for (int index = fromIndex; index < toIndex; index++) {
            bahmniObservationCollection.add((BahmniObservation) CollectionUtils.get(bahmniObservations, index));
        }
        return bahmniObservationCollection;
    }

    private int getIntegerValue(Integer value) {
        if (value == null) return 0;
        return value;
    }

    private void getSpecifiedLeafConcepts(Concept rootConcept, List<String> conceptNames, Set<EncounterTransaction.Concept> leafConcepts) {
        for (Concept concept : rootConcept.getSetMembers()) {
            if (conceptNames.contains(concept.getName().getName())) {
                getAllLeafConcepts(concept, leafConcepts);
            } else {
                getSpecifiedLeafConcepts(concept, conceptNames, leafConcepts);
            }
        }
    }

    private void getAllLeafConcepts(Concept rootConcept, Set<EncounterTransaction.Concept> leafConcepts) {
        if (!rootConcept.isSet() || rootConcept.getConceptClass().getName().equals(CONCEPT_DETAILS)) {
            leafConcepts.add(conceptMapper.map(rootConcept));
        } else {
            for (Concept concept : rootConcept.getSetMembers()) {
                getAllLeafConcepts(concept, leafConcepts);
            }
        }
    }

    private void validate(String conceptSet, String groupByConcept, Concept rootConcept, Concept childConcept) {
        if (rootConcept == null) {
            logger.error("Root concept not found for the name:  " + conceptSet);
            throw new RuntimeException("Root concept not found for the name:  " + conceptSet);
        }

        if (!rootConcept.getSetMembers().contains(childConcept)) {
            logger.error("GroupByConcept: " + groupByConcept + " doesn't belong to the Root concept:  " + conceptSet);
            throw new RuntimeException("GroupByConcept: " + groupByConcept + " doesn't belong to the Root concept:  " + conceptSet);
        }
    }
}
