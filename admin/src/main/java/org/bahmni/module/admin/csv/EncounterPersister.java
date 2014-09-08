package org.bahmni.module.admin.csv;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.bahmni.csv.EntityPersister;
import org.bahmni.csv.RowResult;
import org.bahmni.module.admin.csv.models.MultipleEncounterRow;
import org.bahmni.module.admin.csv.service.PatientMatchService;
import org.bahmni.module.admin.encounter.BahmniEncounterTransactionImportService;
import org.bahmni.module.admin.mapper.DiagnosisMapper;
import org.bahmni.module.admin.mapper.LabOrderMapper;
import org.bahmni.module.admin.mapper.ObservationMapper;
import org.bahmni.module.admin.retrospectiveEncounter.service.RetrospectiveEncounterTransactionService;
import org.openmrs.Patient;
import org.openmrs.Provider;
import org.openmrs.User;
import org.openmrs.api.ConceptService;
import org.openmrs.api.EncounterService;
import org.openmrs.api.OrderService;
import org.openmrs.api.ProviderService;
import org.openmrs.api.VisitService;
import org.openmrs.api.context.Context;
import org.openmrs.api.context.UserContext;
import org.openmrs.module.bahmniemrapi.encountertransaction.contract.BahmniEncounterTransaction;
import org.openmrs.module.bahmniemrapi.encountertransaction.service.BahmniEncounterTransactionService;
import org.openmrs.module.emrapi.encounter.domain.EncounterTransaction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component
public class EncounterPersister implements EntityPersister<MultipleEncounterRow> {
    @Autowired
    private PatientMatchService patientMatchService;
    @Autowired
    private BahmniEncounterTransactionService bahmniEncounterTransactionService;
    @Autowired
    private ConceptService conceptService;
    @Autowired
    private EncounterService encounterService;
    @Autowired
    private VisitService visitService;
    @Autowired
    private OrderService orderService;
    @Autowired
    private ProviderService providerService;

    private UserContext userContext;
    private String patientMatchingAlgorithmClassName;
    protected DiagnosisMapper diagnosisMapper;
    protected LabOrderMapper labOrderMapper;

    private static final Logger log = Logger.getLogger(EncounterPersister.class);
    private Set<EncounterTransaction.Provider> authenticatedProviders;

    public void init(UserContext userContext, String patientMatchingAlgorithmClassName) {
        this.userContext = userContext;
        this.patientMatchingAlgorithmClassName = patientMatchingAlgorithmClassName;

        //Ideally there should be only one concept caching. both diagnosis and labOrder mapper do it separately as of now
        // Diagnosis Service caches the diagnoses concept. Better if there is one instance of it for the one file import.
        diagnosisMapper = new DiagnosisMapper(conceptService);
        labOrderMapper = new LabOrderMapper(conceptService, orderService);
    }

    @Override
    public RowResult<MultipleEncounterRow> validate(MultipleEncounterRow multipleEncounterRow) {
        return new RowResult<>(multipleEncounterRow);
    }

    @Override
    public RowResult<MultipleEncounterRow> persist(MultipleEncounterRow multipleEncounterRow) {
        // This validation is needed as patientservice get returns all patients for empty patient identifier
        if (StringUtils.isEmpty(multipleEncounterRow.patientIdentifier)) {
            return noMatchingPatients(multipleEncounterRow);
        }

        try {
            Context.openSession();
            Context.setUserContext(userContext);

            User authenticatedUser = userContext.getAuthenticatedUser();

            if (getProviders(authenticatedUser) == null)
                return new RowResult<>(multipleEncounterRow, "Provider not set for User:" + authenticatedUser.getUsername());


            Patient patient = patientMatchService.getPatient(patientMatchingAlgorithmClassName, multipleEncounterRow.patientAttributes, multipleEncounterRow.patientIdentifier);
            if (patient == null) {
                return noMatchingPatients(multipleEncounterRow);
            }

            BahmniEncounterTransactionImportService transactionImportService =
                    new BahmniEncounterTransactionImportService(encounterService, new ObservationMapper(conceptService), diagnosisMapper,labOrderMapper );
            List<BahmniEncounterTransaction> bahmniEncounterTransactions =
                    transactionImportService.getBahmniEncounterTransaction(multipleEncounterRow, patient, authenticatedUser, getProviders(authenticatedUser));

            RetrospectiveEncounterTransactionService retrospectiveEncounterTransactionService =
                    new RetrospectiveEncounterTransactionService(bahmniEncounterTransactionService, visitService);

            for (BahmniEncounterTransaction bahmniEncounterTransaction : bahmniEncounterTransactions) {
                retrospectiveEncounterTransactionService.save(bahmniEncounterTransaction, patient);
            }

            return new RowResult<>(multipleEncounterRow);
        } catch (Exception e) {
            log.error(e);
            Context.clearSession();
            return new RowResult<>(multipleEncounterRow, e);
        } finally {
            Context.flushSession();
            Context.closeSession();
        }
    }

    private Set<EncounterTransaction.Provider> getProviders(User authenticatedUser) {
        if (authenticatedProviders == null || authenticatedProviders.isEmpty()) {
            Collection<Provider> providersByPerson = providerService.getProvidersByPerson(authenticatedUser.getPerson());
            if (providersByPerson == null && providersByPerson.isEmpty())
                return null;

            Set<EncounterTransaction.Provider> authenticatedProviders = new HashSet<>();
            for (Provider provider : providersByPerson) {
                EncounterTransaction.Provider etProvider = new EncounterTransaction.Provider();
                etProvider.setName(provider.getName());
                etProvider.setUuid(provider.getUuid());
                authenticatedProviders.add(etProvider);
            }

        }
        return authenticatedProviders;
    }

    private RowResult<MultipleEncounterRow> noMatchingPatients(MultipleEncounterRow multipleEncounterRow) {
        return new RowResult<>(multipleEncounterRow, "No matching patients found with ID:'" + multipleEncounterRow.patientIdentifier + "'");
    }
}