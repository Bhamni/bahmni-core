package org.bahmni.module.bahmnicore.web.v1_0.search;

import org.bahmni.module.bahmnicore.contract.entityMapping.Entity;
import org.bahmni.module.bahmnicore.dao.EntityDao;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.openmrs.Concept;
import org.openmrs.Program;
import org.openmrs.module.bahmnimapping.dao.EntityMappingDao;
import org.openmrs.module.bahmnimapping.model.EntityMapping;
import org.openmrs.module.bahmnimapping.model.EntityMappingType;
import org.openmrs.module.webservices.rest.web.RequestContext;
import org.openmrs.module.webservices.rest.web.resource.api.PageableResult;
import org.openmrs.module.webservices.rest.web.resource.impl.AlreadyPaged;
import org.openmrs.module.webservices.rest.web.resource.impl.EmptySearchResult;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class EntityMappingSearchHandlerTest {

    @Mock
    EntityMappingDao entityMappingDao;
    @Mock
    EntityDao entityDao;
    @Mock
    RequestContext requestContext;

    @InjectMocks
    EntityMappingSearchHandler entityMappingSearchHandler;

    String PROGRAM_OBS_TEMPLATE = "program_obsTemplate";
    String ENTITY1_UUID = "entity1-uuid";
    String ENTITY2_UUID = "entity2-uuid";
    String ENTITY3_UUID = "entity3-uuid";
    String ENTITY4_UUID = "entity4-uuid";
    EntityMappingType programObsTemplateMappingType;

    @Before
    public void setUp() throws Exception {
        when(requestContext.getParameter("mappingType")).thenReturn(PROGRAM_OBS_TEMPLATE);
        when(requestContext.getParameter("entityUuid")).thenReturn(ENTITY1_UUID);
        programObsTemplateMappingType = new EntityMappingType(1, null, PROGRAM_OBS_TEMPLATE, "org.openmrs.Program", "org.openmrs.Concept");
        when(entityMappingDao.getEntityMappingTypeByName(PROGRAM_OBS_TEMPLATE)).thenReturn(programObsTemplateMappingType);
    }

    @Test
    public void shouldGetEntityWithMappingsWhenThereAreEntityMappings() throws Exception {
        EntityMapping entityMapping = new EntityMapping(null, null, ENTITY1_UUID, ENTITY2_UUID, programObsTemplateMappingType);

        when(entityMappingDao.getMappingsOfEntity(ENTITY1_UUID, PROGRAM_OBS_TEMPLATE)).thenReturn(Collections.singletonList(entityMapping));

        Program program = new Program();
        Concept concept = new Concept();

        when(entityDao.getByUuid(ENTITY1_UUID, Program.class)).thenReturn(program);
        when(entityDao.getByUuid(ENTITY2_UUID, Concept.class)).thenReturn(concept);

        AlreadyPaged pageableResult = (AlreadyPaged) entityMappingSearchHandler.search(requestContext);
        Entity entityWithMappings = (Entity) pageableResult.getPageOfResults().get(0);

        assertNotNull(entityWithMappings);
        assertEquals(program, entityWithMappings.getEntity());
        assertEquals(Collections.singletonList(concept), entityWithMappings.getMappings());
    }

    @Test
    public void shouldGetEntityWithZeroMappingsWhenThereIsNoEntityMapping() throws Exception {
        when(entityMappingDao.getMappingsOfEntity(ENTITY1_UUID, PROGRAM_OBS_TEMPLATE)).thenReturn(new ArrayList<EntityMapping>());

        Program program = new Program();
        Concept concept = new Concept();

        when(entityDao.getByUuid(ENTITY1_UUID, Program.class)).thenReturn(program);
        when(entityDao.getByUuid(ENTITY2_UUID, Concept.class)).thenReturn(concept);

        AlreadyPaged pageableResult = (AlreadyPaged) entityMappingSearchHandler.search(requestContext);
        Entity entityWithMappings = (Entity) pageableResult.getPageOfResults().get(0);

        assertNotNull(entityWithMappings);
        assertEquals(program, entityWithMappings.getEntity());
        assertEquals(new ArrayList<EntityMapping>(), entityWithMappings.getMappings());

    }

    @Test
    public void shouldGetAllEntityMappingsGivenAnEntityMappingType() throws Exception {
        List<EntityMapping> entityMappingList = new ArrayList<EntityMapping>();
        when(requestContext.getParameter("entityUuid")).thenReturn(null);
        EntityMapping entityMappingOne = new EntityMapping(null, null, ENTITY1_UUID, ENTITY2_UUID, programObsTemplateMappingType);
        EntityMapping entityMappingTwo = new EntityMapping(null, null, ENTITY3_UUID, ENTITY4_UUID, programObsTemplateMappingType);
        entityMappingList.add(entityMappingOne);
        entityMappingList.add(entityMappingTwo);

        when(entityMappingDao.getAllEntityMappings(PROGRAM_OBS_TEMPLATE)).thenReturn(entityMappingList);

        Program programOne = new Program();
        Program programTwo = new Program();
        Concept concept = new Concept();

        when(entityDao.getByUuid(ENTITY1_UUID, Program.class)).thenReturn(programOne);
        when(entityDao.getByUuid(ENTITY2_UUID, Concept.class)).thenReturn(concept);
        when(entityDao.getByUuid(ENTITY3_UUID, Program.class)).thenReturn(programTwo);
        when(entityDao.getByUuid(ENTITY4_UUID, Concept.class)).thenReturn(concept);

        AlreadyPaged pageableResult = (AlreadyPaged) entityMappingSearchHandler.search(requestContext);
        List<Entity> entityWithMappings = new ArrayList<>();
        entityWithMappings.add((Entity) pageableResult.getPageOfResults().get(0));
        entityWithMappings.add((Entity) pageableResult.getPageOfResults().get(1));

        assertNotNull(entityWithMappings);
        assertEquals(programOne, entityWithMappings.get(0).getEntity());
        assertEquals(programTwo, entityWithMappings.get(1).getEntity());

    }

    @Test
    public void shouldGetWithZeroMappingsWhenThereIsNoEntityMappingType() throws Exception {

        when(entityMappingDao.getEntityMappingTypeByName(PROGRAM_OBS_TEMPLATE)).thenReturn(null);
        when(entityMappingDao.getMappingsOfEntity(ENTITY1_UUID, PROGRAM_OBS_TEMPLATE)).thenReturn(new ArrayList<EntityMapping>());

        PageableResult pageableResult = entityMappingSearchHandler.search(requestContext);

        assertTrue(pageableResult instanceof EmptySearchResult);
    }

}