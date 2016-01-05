package org.bahmni.module.referencedata.labconcepts.mapper;

import org.bahmni.module.referencedata.labconcepts.contract.ConceptSet;
import org.bahmni.module.referencedata.labconcepts.model.ConceptMetaData;
import org.bahmni.test.builder.ConceptBuilder;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.openmrs.Concept;
import org.openmrs.ConceptClass;
import org.openmrs.api.context.Context;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

@RunWith(PowerMockRunner.class)
@PrepareForTest(Context.class)
public class ConceptSetMapperTest {

    private ConceptSetMapper conceptSetMapper;

    @Mock
    private ConceptMetaData conceptMetaData;

    @Before
    public void setUp() throws Exception {
        conceptSetMapper = new ConceptSetMapper();
        Locale defaultLocale = new Locale("en", "GB");
        PowerMockito.mockStatic(Context.class);
        when(Context.getLocale()).thenReturn(defaultLocale);
    }

    @Test
    public void map_concept_set_name_to_openmrs_conceptname() throws Exception {
        ConceptSet conceptSet = new ConceptSet();
        conceptSet.setUniqueName("Some");
        org.openmrs.Concept mappedConcept = conceptSetMapper.map(conceptSet, new ArrayList<Concept>() , conceptMetaData);
        assertEquals("Some", mappedConcept.getFullySpecifiedName(Context.getLocale()).getName());
    }

    @Test
    public void map_short_name() throws Exception {
        ConceptSet conceptSet = new ConceptSet();
        conceptSet.setDisplayName("ShortName");
        org.openmrs.Concept mappedConcept = conceptSetMapper.map(conceptSet, new ArrayList<Concept>(), conceptMetaData);
        assertEquals("ShortName", mappedConcept.getShortestName(Context.getLocale(), false).getName());
    }

    @Test
    public void map_description() throws Exception {
        ConceptSet conceptSet = new ConceptSet();
        conceptSet.setDescription("Description");
        org.openmrs.Concept mappedConcept = conceptSetMapper.map(conceptSet, new ArrayList<Concept>(), conceptMetaData);
        assertEquals("Description", mappedConcept.getDescription(Context.getLocale()).getDescription());
    }

    @Test
    public void map_concept_class() throws Exception {
        ConceptSet conceptSet = new ConceptSet();
        conceptSet.setClassName("ClassName");
        ConceptClass conceptClass = new ConceptClass();
        conceptClass.setName("ClassName");

        when(conceptMetaData.getConceptClass()).thenReturn(conceptClass);
        org.openmrs.Concept mappedConcept = conceptSetMapper.map(conceptSet, new ArrayList<Concept>(),conceptMetaData);
        assertEquals("ClassName", mappedConcept.getConceptClass().getName());
    }

    @Test
    public void map_set_members() throws Exception {
        ConceptSet conceptSet = new ConceptSet();
        List<String> children = new ArrayList<>();
        children.add("1");
        children.add("2");
        conceptSet.setChildren(children);
        Concept child1 = new ConceptBuilder().withName("1").build();
        Concept child2 = new ConceptBuilder().withName("2").build();
        ArrayList<Concept> childConcepts = new ArrayList<>();
        childConcepts.add(child1);
        childConcepts.add(child2);
        org.openmrs.Concept mappedConcept = conceptSetMapper.map(conceptSet, childConcepts,  conceptMetaData);
        List<org.openmrs.Concept> setMembers = mappedConcept.getSetMembers();
        assertEquals(2, setMembers.size());
        assertEquals("1", setMembers.get(0).getName(Context.getLocale()).getName());
        assertEquals("2", setMembers.get(1).getName(Context.getLocale()).getName());
    }

    @Test
    public void dont_map_short_name_if_does_not_exist() throws Exception {
        ConceptSet conceptSet = new ConceptSet();
        conceptSet.setDisplayName(null);
        conceptSet.setUniqueName("uniqueName");
        conceptSet.setClassName("conceptClass");
        org.openmrs.Concept mappedConcept = conceptSetMapper.map(conceptSet, new ArrayList<Concept>(), conceptMetaData);
        assertEquals(0, mappedConcept.getShortNames().size());
    }
}
