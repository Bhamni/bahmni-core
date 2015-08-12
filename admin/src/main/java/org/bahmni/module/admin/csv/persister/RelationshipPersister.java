package org.bahmni.module.admin.csv.persister;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.bahmni.csv.EntityPersister;
import org.bahmni.csv.Messages;
import org.bahmni.module.admin.csv.models.RelationshipRow;
import org.bahmni.module.admin.csv.service.CSVRelationshipService;
import org.openmrs.api.AdministrationService;
import org.openmrs.api.PatientService;
import org.openmrs.api.PersonService;
import org.openmrs.api.ProviderService;
import org.openmrs.api.context.Context;
import org.openmrs.api.context.UserContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
public class RelationshipPersister implements EntityPersister<RelationshipRow> {

    @Autowired
    private PatientService patientService;

    @Autowired
    private ProviderService providerService;

    @Autowired
    private PersonService personService;

    @Autowired
    private AdministrationService administrationService;

    private static final Logger log = Logger.getLogger(RelationshipPersister.class);
    private UserContext userContext;

    public void init(UserContext userContext) {
        this.userContext = userContext;
    }

    @Override
    public Messages persist(RelationshipRow relationshipRow) {
        try {
            Context.openSession();
            Context.setUserContext(userContext);

            new CSVRelationshipService(patientService, personService, providerService, administrationService).save(relationshipRow);

            return new Messages();
        } catch (Throwable e) {
            log.error(e.getMessage(), e);
            Context.clearSession();
            return new Messages(e);
        } finally {
            Context.flushSession();
            Context.closeSession();
        }

    }

    @Override
    public Messages validate(RelationshipRow relationshipRow) {
        Messages messages = new Messages();

        if (StringUtils.isEmpty(relationshipRow.getPersonA())) {
            messages.add("Patient unique identifier not specified\n");
        }

        if (StringUtils.isEmpty(relationshipRow.getPersonB())) {
            messages.add("Target relationship person identifier not specified\n");
        }

        if (StringUtils.isEmpty(relationshipRow.getRelationshipType())) {
            messages.add("Relationship type not specified\n");
        }

        if (StringUtils.isEmpty(relationshipRow.getStartDate())) {
            relationshipRow.setStartDate(new Date().toString());
        }

        if ((!StringUtils.isEmpty(relationshipRow.getStartDate()) && !StringUtils.isEmpty(relationshipRow.getEndDate()))) {
            if (new Date(relationshipRow.getStartDate()).after(new Date(relationshipRow.getEndDate())))
                messages.add("Start date should be before end date\n");
        }

        return messages;
    }


}

