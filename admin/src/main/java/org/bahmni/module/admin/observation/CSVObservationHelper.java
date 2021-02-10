package org.bahmni.module.admin.observation;

import org.apache.commons.lang.StringUtils;
import org.bahmni.csv.KeyValue;
import org.openmrs.Concept;
import org.openmrs.ConceptName;
import org.openmrs.ConceptNumeric;
import org.openmrs.Obs;
import org.openmrs.api.APIException;
import org.openmrs.api.AdministrationService;
import org.openmrs.api.ConceptService;
import org.openmrs.api.context.Context;
import org.openmrs.module.emrapi.encounter.domain.EncounterTransaction;
import org.openmrs.module.emrapi.encounter.domain.EncounterTransaction.Observation;
import org.openmrs.module.emrapi.encounter.exception.ConceptNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.security.InvalidParameterException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static java.util.Arrays.asList;
import static java.util.Objects.nonNull;
import static org.apache.commons.lang.StringUtils.isNotBlank;
import static org.springframework.util.CollectionUtils.isEmpty;

@Component
public class CSVObservationHelper {
    private static final String FORM2_TYPE = "form2";
    private static final String OBS_PATH_SPLITTER_PROPERTY = "bahmni.admin.csv.upload.obsPath.splitter";
    private static final String MULTI_SELECT_OBS_SPLITTER_PROPERTY = "bahmni.admin.csv.upload.obs.multiSelect.splitter";
    private static final String DEFAULT_MULTI_SELECT_OBS_SPLITTER = "|";
    private static final String DEFAULT_OBSPATH_SPLITTER = ".";
    private final ConceptCache conceptCache;
    private final ConceptService conceptService;
    private AdministrationService administrationService;

    @Autowired
    CSVObservationHelper(ConceptService conceptService,
                         @Qualifier("adminService") AdministrationService administrationService) {
        this.conceptCache = new ConceptCache(conceptService);
        this.conceptService = conceptService;
        this.administrationService = administrationService;
    }

    public static <T> T getLastItem(List<T> items) {
        if (isEmpty(items)) {
            throw new InvalidParameterException("Empty items");
        }
        return items.get(items.size() - 1);
    }

    protected Concept getConcept(String conceptName) {
        return conceptCache.getConcept(conceptName);
    }

    public void createObservations(List<Observation> observations, Date encounterDate,
                                   List<KeyValue> obsRows, List<String> conceptNames) throws ParseException {
        Observation existingObservation = getRootObservationIfExists(observations, conceptNames, null);
        if (existingObservation == null) {
            for(KeyValue obsRow : obsRows)
                observations.add(createObservation(conceptNames, encounterDate, obsRow));
        } else {
            for(KeyValue obsRow : obsRows)
                updateObservation(conceptNames, existingObservation, encounterDate, obsRow);
        }
    }

    public void createObservations(List<Observation> observations, Date encounterDate,
                                   KeyValue obsRow, List<String> conceptNames) throws ParseException {
        Observation existingObservation = getRootObservationIfExists(observations, conceptNames, null);
        if (existingObservation == null) {
            observations.add(createObservation(conceptNames, encounterDate, obsRow));
        } else {
            updateObservation(conceptNames, existingObservation, encounterDate, obsRow);

        }
    }

    public void verifyNumericConceptValue(KeyValue obsRow, List<String> conceptNames) {
        String lastConceptName = getLastItem(conceptNames);
        Concept lastConcept = conceptService.getConceptByName(lastConceptName);
        if (lastConcept != null && lastConcept.isNumeric()) {
            ConceptNumeric cn = (ConceptNumeric) lastConcept;
            if (!cn.getAllowDecimal() && obsRow.getValue().contains(".")) {
                throw new APIException("Decimal is not allowed for " + cn.getName() + " concept");
            }
        }
    }

    private void updateObservation(List<String> conceptNames, Observation existingObservation,
                                   Date encounterDate, KeyValue obsRow) throws ParseException {
        existingObservation.addGroupMember(createObservation(conceptNames, encounterDate, obsRow));
    }

    private Observation getRootObservationIfExists(List<Observation> observations, List<String> conceptNames,
                                                   Observation existingObservation) {
        for (Observation observation : observations) {
            if (observation.getConcept().getName().equals(conceptNames.get(0))) {
                conceptNames.remove(0);
                if (conceptNames.size() == 0) {
                    conceptNames.add(observation.getConcept().getName());
                    return existingObservation;
                }
                existingObservation = observation;
                return getRootObservationIfExists(observation.getGroupMembers(), conceptNames, existingObservation);
            }
        }
        return existingObservation;
    }

    private Observation createObservation(List<String> conceptNames, Date encounterDate, KeyValue obsRow) {
        Concept obsConcept = conceptCache.getConcept(conceptNames.get(0));
        EncounterTransaction.Concept concept = new EncounterTransaction.Concept(obsConcept.getUuid(),
                obsConcept.getName().getName());

        Observation observation = new Observation();
        observation.setConcept(concept);
        observation.setObservationDateTime(encounterDate);
        if (conceptNames.size() == 1) {
            observation.setValue(getValue(obsRow, obsConcept));
            if (obsConcept.getDatatype().isNumeric()) {
                validateAndUpdateObservationInterpretation(obsRow, obsConcept, observation);
            }
        } else {
            conceptNames.remove(0);
            observation.addGroupMember(createObservation(conceptNames, encounterDate, obsRow));
        }
        return observation;
    }

    private void validateAndUpdateObservationInterpretation(KeyValue obsRow, Concept obsConcept, Observation observation) {
        verifyNumericConceptValue(obsRow, Collections.singletonList(obsConcept.getName().getName()));
        Double recordedObsValue = Double.parseDouble(obsRow.getValue());
        Double hiNormal = ((ConceptNumeric) obsConcept).getHiNormal();
        Double lowNormal = ((ConceptNumeric) obsConcept).getLowNormal();
        if (nonNull(recordedObsValue) && ((nonNull(hiNormal) && recordedObsValue.compareTo(hiNormal) > 0) || (nonNull(lowNormal) && recordedObsValue.compareTo(lowNormal) < 0))) {
            observation.setInterpretation(String.valueOf(Obs.Interpretation.ABNORMAL));
        }
    }

    private Object getValue(KeyValue obsRow, Concept obsConcept) {
        Map<String, Object> valueConcept = null;
        if (obsConcept.getDatatype().isCoded()) {
            List<Concept> valueConcepts = conceptService.getConceptsByName(obsRow.getValue());
            for (Concept concept : valueConcepts) {
                ConceptName name = concept.getFullySpecifiedName(Context.getLocale()) != null ?
                        concept.getFullySpecifiedName(Context.getLocale()) : concept.getName();
                if (name.getName().equalsIgnoreCase(obsRow.getValue())) {
                    valueConcept = new LinkedHashMap<>();
                    Map<String, Object> conceptNameDetails = new HashMap<>();
                    conceptNameDetails.put("name", concept.getName().getName());
                    conceptNameDetails.put("uuid", concept.getName().getUuid());
                    conceptNameDetails.put("display", concept.getDisplayString());
                    conceptNameDetails.put("locale", concept.getName().getLocale());
                    conceptNameDetails.put("localePreferred", concept.getName().getLocalePreferred());
                    conceptNameDetails.put("conceptNameType", concept.getName().getConceptNameType());
                    valueConcept.put("uuid", concept.getUuid());
                    valueConcept.put("name", conceptNameDetails);
                    valueConcept.put("names", concept.getNames());
                    valueConcept.put("displayString", concept.getDisplayString());
                    valueConcept.put("translationKey", concept.getName());
                    break;
                }
            }
            if (valueConcept == null)
                throw new ConceptNotFoundException(obsRow.getValue() + " not found");
            return valueConcept;
        }
        return obsRow.getValue();
    }

    public List<String> getCSVHeaderParts(KeyValue csvObservation) {
        String key = csvObservation.getKey();
        return isNotBlank(key) ? new ArrayList<>(asList(key.split(String.format("\\%s", getObsPathSplitter()))))
                : new ArrayList<>();
    }

    private String getObsPathSplitter() {
        final String obsPathSplitter = administrationService.getGlobalProperty(OBS_PATH_SPLITTER_PROPERTY);
        return isNotBlank(obsPathSplitter) ? obsPathSplitter : DEFAULT_OBSPATH_SPLITTER;
    }

    public List<String> getMultiSelectObs(KeyValue csvObservation) {
        String multiSelectValue = csvObservation.getValue();
        return isNotBlank(multiSelectValue) ? new ArrayList<>(asList(multiSelectValue.split(String.format("\\%s", getMultiSelectObsSplitter()))))
                : new ArrayList<>();
    }

    private String getMultiSelectObsSplitter() {
        final String multiSelectObsSplitter = administrationService.getGlobalProperty(MULTI_SELECT_OBS_SPLITTER_PROPERTY);
        return isNotBlank(multiSelectObsSplitter) ? multiSelectObsSplitter : DEFAULT_MULTI_SELECT_OBS_SPLITTER;
    }

    public boolean isForm2Type(KeyValue obsRow) {
        String key = obsRow.getKey();
        if (StringUtils.isNotBlank(key)) {
            String[] csvHeaderParts = key.split((String.format("\\%s", getObsPathSplitter())));
            return csvHeaderParts[0].equalsIgnoreCase(FORM2_TYPE);
        }
        return false;
    }

    public boolean isForm1Type(KeyValue keyValue) {
        return !isForm2Type(keyValue);
    }
}
