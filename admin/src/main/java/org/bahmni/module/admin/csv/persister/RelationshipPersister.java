package org.bahmni.module.admin.csv.persister;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.bahmni.csv.EntityPersister;
import org.bahmni.csv.Messages;
import org.bahmni.module.admin.csv.models.RelationshipRow;
import org.bahmni.module.admin.csv.service.CSVRelationshipService;
import org.bahmni.module.admin.csv.utils.CSVUtils;
import org.bahmni.module.bahmnicore.service.BahmniPatientService;
import org.openmrs.api.AdministrationService;
import org.openmrs.api.PersonService;
import org.openmrs.api.ProviderService;
import org.openmrs.api.context.Context;
import org.openmrs.api.context.UserContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.text.ParseException;
import java.util.Date;

@Component
public class RelationshipPersister implements EntityPersister<RelationshipRow> {

    @Autowired
    private BahmniPatientService patientService;

    @Autowired
    private ProviderService providerService;

    @Autowired
    private PersonService personService;

    @Autowired
    @Qualifier("adminService")
    private AdministrationService administrationService;

    private static final Logger log = Logger.getLogger(RelationshipPersister.class);
    private UserContext userContext;

    public void init(UserContext userContext) {
        this.userContext = userContext;
    }

    Messages messages = new Messages();

    @Override
    public Messages validate(RelationshipRow relationshipRow) {

        if (StringUtils.isEmpty(relationshipRow.getPersonA())) {
            messages.add("Patient unique identifier not specified.");
        }

        if (StringUtils.isEmpty(relationshipRow.getPersonB())) {
            messages.add("Target relationship person identifier not specified.");
        }

        if (StringUtils.isEmpty(relationshipRow.getaIsToB())) {
            messages.add("Relationship type A is to B is not specified.");
        }

        if (StringUtils.isEmpty(relationshipRow.getbIsToA())) {
            messages.add("Relationship type B is to A is not specified.");
        }

        if ((!StringUtils.isEmpty(relationshipRow.getStartDate()) && !StringUtils.isEmpty(relationshipRow.getEndDate()))) {
            try {
                if (CSVUtils.getDateFromString(relationshipRow.getStartDate()).after(CSVUtils.getDateFromString(relationshipRow.getEndDate()))){
                    messages.add("Start date should be before end date.");
                }
            } catch (ParseException e) {
                messages.add("Could not parse provided dates. Please provide date in format yyyy-mm-dd");
            }
        }

        return messages;
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

}

