package org.bahmni.module.bahmnicore.web.v1_0.search;

import org.bahmni.module.bahmnicore.contract.entityMapping.Entity;
import org.bahmni.module.bahmnicore.dao.EntityDao;
import org.bahmni.module.bahmnicore.web.v1_0.mapper.EntityMapper;
import org.openmrs.module.bahmnimapping.dao.EntityMappingDao;
import org.openmrs.module.bahmnimapping.model.EntityMapping;
import org.openmrs.module.bahmnimapping.model.EntityMappingType;
import org.openmrs.module.webservices.rest.web.RequestContext;
import org.openmrs.module.webservices.rest.web.RestConstants;
import org.openmrs.module.webservices.rest.web.resource.api.PageableResult;
import org.openmrs.module.webservices.rest.web.resource.api.SearchConfig;
import org.openmrs.module.webservices.rest.web.resource.api.SearchHandler;
import org.openmrs.module.webservices.rest.web.resource.api.SearchQuery;
import org.openmrs.module.webservices.rest.web.resource.impl.AlreadyPaged;
import org.openmrs.module.webservices.rest.web.resource.impl.EmptySearchResult;
import org.openmrs.module.webservices.rest.web.response.ResponseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Component
public class EntityMappingSearchHandler implements SearchHandler {
    private EntityMappingDao entityMappingDao;

    private EntityDao entityDao;

    @Autowired
    public EntityMappingSearchHandler(EntityMappingDao entityMappingDao, EntityDao entityDao) {
        this.entityMappingDao = entityMappingDao;
        this.entityDao = entityDao;
    }

    @Override
    public SearchConfig getSearchConfig() {
        return new SearchConfig("byEntityAndMappingType", RestConstants.VERSION_1 + "/entitymapping", Arrays.asList("1.9.*", "1.10.*", "1.11.*", "1.12.*"),
                new SearchQuery.Builder("Allows you to find entity relationships of entity with specific mapping type")
                        .withOptionalParameters("entityUuid")
                        .withRequiredParameters("mappingType")
                        .build());
    }

    @Override
    public PageableResult search(RequestContext requestContext) throws ResponseException {
        EntityMapper entityMapper = new EntityMapper();
        String entityMappingTypeName = requestContext.getParameter("mappingType");
        String entityUuid = requestContext.getParameter("entityUuid");
        EntityMappingType entityMappingType = entityMappingDao.getEntityMappingTypeByName(entityMappingTypeName);
        List<EntityMapping> entityMappings = new ArrayList<EntityMapping>();
        if (entityMappingType == null) {
            return new EmptySearchResult();
        }
        if (entityUuid != null) {
            entityMappings = entityMappingDao.getMappingsOfEntity(entityUuid, entityMappingTypeName);
        }
        else {
            entityMappings = entityMappingDao.getAllEntityMappings(entityMappingTypeName);
        }
        List<Entity> entityList = entityMapper.map(entityMappings, entityDao, entityMappingType, entityUuid);
        return new AlreadyPaged<>(requestContext, entityList, false);

    }
}
