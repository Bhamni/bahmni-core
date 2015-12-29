package org.openmrs.module.bahmniemrapi.encountertransaction.mapper;

import org.openmrs.Concept;
import org.openmrs.api.ConceptService;
import org.openmrs.module.bahmniemrapi.encountertransaction.contract.BahmniObservation;
import org.openmrs.module.bahmniemrapi.encountertransaction.mapper.parameters.AdditionalBahmniObservationFields;
import org.openmrs.module.emrapi.encounter.domain.EncounterTransaction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Component
public class ETObsToBahmniObsMapper {

    public static final String CONCEPT_DETAILS_CONCEPT_CLASS = "Concept Details";
    public static final String ABNORMAL_CONCEPT_CLASS = "Abnormal";
    public static final String DURATION_CONCEPT_CLASS = "Duration";
    private ConceptService conceptService;

    @Autowired
    public ETObsToBahmniObsMapper(ConceptService conceptService) {
        this.conceptService = conceptService;
    }

    public List<BahmniObservation> create(List<EncounterTransaction.Observation> allObservations, AdditionalBahmniObservationFields additionalBahmniObservationFields) {
        List<BahmniObservation> bahmniObservations = new ArrayList<>();
        for (EncounterTransaction.Observation observation : allObservations) {
            bahmniObservations.add(create(observation, additionalBahmniObservationFields));
        }
        return bahmniObservations;
    }

    public BahmniObservation create(EncounterTransaction.Observation observation, AdditionalBahmniObservationFields additionalBahmniObservationFields) {
        return map(observation, additionalBahmniObservationFields,
                Arrays.asList(conceptService.getConceptByUuid(observation.getConceptUuid())),
                false);
    }

    BahmniObservation map(EncounterTransaction.Observation observation, AdditionalBahmniObservationFields additionalBahmniObservationFields, List<Concept> rootConcepts, boolean flatten) {

        BahmniObservation bahmniObservation = new BahmniObservation();
        bahmniObservation.setEncounterTransactionObservation(observation);
        bahmniObservation.setEncounterDateTime(additionalBahmniObservationFields.getEncounterDateTime());
        bahmniObservation.setVisitStartDateTime(additionalBahmniObservationFields.getVisitDateTime());
        bahmniObservation.setConceptSortWeight(ConceptSortWeightUtil.getSortWeightFor(bahmniObservation.getConcept().getName(), rootConcepts));
        bahmniObservation.setEncounterUuid(additionalBahmniObservationFields.getEncounterUuid());
        bahmniObservation.setObsGroupUuid(additionalBahmniObservationFields.getObsGroupUuid());
        if (CONCEPT_DETAILS_CONCEPT_CLASS.equals(observation.getConcept().getConceptClass()) && flatten) {
            for (EncounterTransaction.Observation member : observation.getGroupMembers()) {
                if (member.getVoided()) {
                    continue;
                }
                if (member.getConcept().getConceptClass().equals(ABNORMAL_CONCEPT_CLASS)) {
                    if (member.getValue() instanceof Boolean) {
                        bahmniObservation.setAbnormal((Boolean) member.getValue());
                    } else {
                        if (member.getValue() != null) {
                            bahmniObservation.setAbnormal(Boolean.parseBoolean(((EncounterTransaction.Concept) member.getValue()).getName()));
                        }
                    }
                } else if (member.getConcept().getConceptClass().equals(DURATION_CONCEPT_CLASS)) {
                    bahmniObservation.setDuration(new Double(member.getValue().toString()).longValue());
                } else {
                    bahmniObservation.setValue(member.getValue());
                    bahmniObservation.setType(member.getConcept().getDataType());
                    bahmniObservation.getConcept().setUnits(member.getConcept().getUnits());
                    bahmniObservation.setHiNormal(member.getConcept().getHiNormal());
                    bahmniObservation.setLowNormal(member.getConcept().getLowNormal());
                }
            }
        } else if (observation.getGroupMembers().size() > 0) {
            for (EncounterTransaction.Observation groupMember : observation.getGroupMembers()) {
                AdditionalBahmniObservationFields additionalFields = (AdditionalBahmniObservationFields) additionalBahmniObservationFields.clone();
                additionalFields.setObsGroupUuid(observation.getUuid());
                bahmniObservation.addGroupMember(map(groupMember, additionalFields, rootConcepts, flatten));
            }
        } else {
            bahmniObservation.setValue(observation.getValue());
            bahmniObservation.setType(observation.getConcept().getDataType());
            bahmniObservation.setHiNormal(observation.getConcept().getHiNormal());
            bahmniObservation.setLowNormal(observation.getConcept().getLowNormal());
        }

        for (EncounterTransaction.Provider provider : additionalBahmniObservationFields.getProviders()) {
            bahmniObservation.addProvider(provider);
        }
        if (observation.getCreator() != null) {
            bahmniObservation.setCreatorName(observation.getCreator().getPersonName());
        }
        return bahmniObservation;
    }

}
