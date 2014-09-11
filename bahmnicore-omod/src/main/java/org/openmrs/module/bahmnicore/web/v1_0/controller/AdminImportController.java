package org.openmrs.module.bahmnicore.web.v1_0.controller;

import org.apache.commons.lang3.time.DateUtils;
import org.apache.log4j.Logger;
import org.bahmni.csv.CSVFile;
import org.bahmni.fileimport.FileImporter;
import org.bahmni.fileimport.ImportStatus;
import org.bahmni.fileimport.dao.ImportStatusDao;
import org.bahmni.fileimport.dao.JDBCConnectionProvider;
import org.bahmni.module.admin.csv.EncounterPersister;
import org.bahmni.module.admin.csv.PatientProgramPersister;
import org.bahmni.module.admin.csv.models.MultipleEncounterRow;
import org.bahmni.module.admin.csv.models.PatientProgramRow;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.engine.SessionImplementor;
import org.hibernate.impl.SessionImpl;
import org.openmrs.api.AdministrationService;
import org.openmrs.api.context.Context;
import org.openmrs.module.webservices.rest.web.RestConstants;
import org.openmrs.module.webservices.rest.web.v1_0.controller.BaseRestController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.commons.CommonsMultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

@Controller
public class AdminImportController extends BaseRestController {
    private final String baseUrl = "/rest/" + RestConstants.VERSION_1 + "/bahmnicore/admin/upload";
    private static Logger logger = Logger.getLogger(AdminImportController.class);

    public static final String YYYY_MM_DD_HH_MM_SS = "_yyyy-MM-dd_HH:mm:ss";
    private static final int DEFAULT_NUMBER_OF_DAYS = 30;

    public static final String PARENT_DIRECTORY_UPLOADED_FILES_CONFIG = "uploaded.files.directory";
    public static final String ENCOUNTER_FILES_DIRECTORY = "encounter/";
    private static final String PROGRAM_FILES_DIRECTORY = "program/";

    @Autowired
    private EncounterPersister encounterPersister;

    @Autowired
    private PatientProgramPersister patientProgramPersister;

    @Autowired
    private SessionFactory sessionFactory;

    @Autowired
    @Qualifier("adminService")
    private AdministrationService administrationService;

    @RequestMapping(value = baseUrl + "/encounter", method = RequestMethod.POST)
    @ResponseBody
    public boolean upload(@RequestParam(value = "file") MultipartFile file, @RequestParam(value = "patientMatchingAlgorithm", required = false) String patientMatchingAlgorithm) {
        try {
            CSVFile persistedUploadedFile = writeToLocalFile(file, ENCOUNTER_FILES_DIRECTORY);

            encounterPersister.init(Context.getUserContext(), patientMatchingAlgorithm);
            String uploadedOriginalFileName = ((CommonsMultipartFile) file).getFileItem().getName();
            String username = Context.getUserContext().getAuthenticatedUser().getUsername();
            boolean skipValidation = true;
            return new FileImporter<MultipleEncounterRow>().importCSV(uploadedOriginalFileName, persistedUploadedFile,
                    encounterPersister, MultipleEncounterRow.class, new NewMRSConnectionProvider(), username, skipValidation);
        } catch (Exception e) {
            logger.error("Could not upload file", e);
            return false;
        }
    }

    @RequestMapping(value = baseUrl + "/program", method = RequestMethod.POST)
    @ResponseBody
    public boolean uploadProgram(@RequestParam(value = "file") MultipartFile file, @RequestParam(value = "patientMatchingAlgorithm", required = false) String patientMatchingAlgorithm) {
        try {
            CSVFile persistedUploadedFile = writeToLocalFile(file, PROGRAM_FILES_DIRECTORY);

            patientProgramPersister.init(Context.getUserContext(), patientMatchingAlgorithm);
            String uploadedOriginalFileName = ((CommonsMultipartFile) file).getFileItem().getName();
            String username = Context.getUserContext().getAuthenticatedUser().getUsername();

            boolean skipValidation = true;
            return new FileImporter<PatientProgramRow>().importCSV(uploadedOriginalFileName, persistedUploadedFile,
                    patientProgramPersister, PatientProgramRow.class, new NewMRSConnectionProvider(), username, skipValidation);
        } catch (Exception e) {
            logger.error("Could not upload file", e);
            return false;
        }
    }

    @RequestMapping(value = baseUrl + "/status", method = RequestMethod.GET)
    @ResponseBody
    public List<ImportStatus> status(@RequestParam(required = false) Integer numberOfDays) throws SQLException {
        numberOfDays = numberOfDays == null ? DEFAULT_NUMBER_OF_DAYS : numberOfDays;

        ImportStatusDao importStatusDao = new ImportStatusDao(new JDBCConnectionProvider() {
            @Override
            public Connection getConnection() {
                //TODO: ensure that only connection associated with current thread current transaction is given
                SessionImplementor session = (SessionImpl) sessionFactory.getCurrentSession();
                return session.connection();
            }

            @Override
            public void closeConnection() {

            }
        });
        return importStatusDao.getImportStatusFromDate(DateUtils.addDays(new Date(), (numberOfDays * -1)));
    }

    private CSVFile writeToLocalFile(MultipartFile file, String filesDirectory) throws IOException {
        String uploadedOriginalFileName = ((CommonsMultipartFile) file).getFileItem().getName();
        byte[] fileBytes = file.getBytes();
        CSVFile uploadedFile = getFile(uploadedOriginalFileName, filesDirectory);
        FileOutputStream uploadedFileStream = null;
        try {
            uploadedFileStream = new FileOutputStream(new File(uploadedFile.getAbsolutePath()));
            uploadedFileStream.write(fileBytes);
            uploadedFileStream.flush();
        } catch (Exception e) {
            logger.error(e);
            // TODO : handle errors for end users. Give some good message back to users.
        } finally {
            if (uploadedFileStream != null)
                try {
                    uploadedFileStream.close();
                } catch (IOException e) {
                    logger.error(e);
                }
        }
        return uploadedFile;
    }

    private CSVFile getFile(String fileName, String filesDirectory) {
        String fileNameWithoutExtension = fileName.substring(0, fileName.lastIndexOf("."));
        String fileExtension = fileName.substring(fileName.lastIndexOf("."));

        String timestampForFile = new SimpleDateFormat(YYYY_MM_DD_HH_MM_SS).format(new Date());

        String uploadDirectory = administrationService.getGlobalProperty(PARENT_DIRECTORY_UPLOADED_FILES_CONFIG);
        String relativePath = filesDirectory + fileNameWithoutExtension + timestampForFile + fileExtension;
        return new CSVFile(uploadDirectory, relativePath);
    }

    private class NewMRSConnectionProvider implements JDBCConnectionProvider {
        private ThreadLocal<Session> session = new ThreadLocal<>();

        @Override
        public Connection getConnection() {
            if (session.get() == null || !session.get().isOpen())
                session.set(sessionFactory.openSession());

            return session.get().connection();
        }

        @Override
        public void closeConnection() {
            session.get().close();
        }
    }

}