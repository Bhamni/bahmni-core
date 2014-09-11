package org.bahmni.module.referencedata.web.contract.mapper;

import org.bahmni.module.referencedata.web.contract.Department;
import org.bahmni.module.referencedata.web.contract.Sample;
import org.bahmni.module.referencedata.web.contract.Test;
import org.openmrs.Concept;
import org.openmrs.ConceptClass;
import org.openmrs.ConceptDescription;
import org.openmrs.ConceptSet;
import org.openmrs.api.context.Context;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.bahmni.module.referencedata.model.event.DepartmentEvent.isDepartmentConcept;
import static org.bahmni.module.referencedata.model.event.SampleEvent.isSampleConcept;

public class MapperUtils {
    public static String getDescription(Concept concept) {
        ConceptDescription description = concept.getDescription();
        if (description != null) {
            return description.getDescription();
        }
        return null;
    }

    public static Set<ConceptDescription> constructDescription(String description) {
        if (description == null) return null;
        ConceptDescription conceptDescription = new ConceptDescription(description, Context.getLocale());
        Set<ConceptDescription> descriptions = new HashSet<>();
        descriptions.add(conceptDescription);
        return descriptions;
    }

    public static Department getDepartment(Concept concept) {
        List<ConceptSet> parentConcepts = Context.getConceptService().getSetsContainingConcept(concept);
        for (ConceptSet parentConcept : parentConcepts) {
            if (isDepartmentConcept(parentConcept.getConceptSet())) {
                DepartmentMapper departmentMapper = new DepartmentMapper();
                return departmentMapper.map(parentConcept.getConceptSet());
            }
        }
        return null;
    }


    public static Sample getSample(Concept concept) {
        List<ConceptSet> parentConcepts = Context.getConceptService().getSetsContainingConcept(concept);
        if (parentConcepts == null) return null;
        for (ConceptSet parentConcept : parentConcepts) {
            if (isSampleConcept(parentConcept.getConceptSet())) {
                SampleMapper sampleMapper = new SampleMapper();
                return sampleMapper.map(parentConcept.getConceptSet());
            }
        }
        return null;
    }


    public static List<Test> getTests(Concept concept) {
        List<Test> tests = new ArrayList<>();
        TestMapper testMapper = new TestMapper();
        List<Concept> setMembers = concept.getSetMembers();
        if (setMembers == null) return tests;
        for (Concept setMember : setMembers) {
            if (isTestConcept(setMember)) {
                tests.add(testMapper.map(setMember));
            }
        }
        return tests;
    }

    private static boolean isTestConcept(Concept concept) {
        return concept.getConceptClass() != null &&
                concept.getConceptClass().getUuid().equals(ConceptClass.TEST_UUID);
    }
}
