package org.bahmni.module.bahmnicore.service.impl;

import org.apache.commons.io.FileUtils;
import org.apache.xerces.impl.dv.util.Base64;
import org.bahmni.module.bahmnicore.bahmniexceptions.FileTypeNotSupportedException;
import org.bahmni.module.bahmnicore.bahmniexceptions.VideoFormatNotSupportedException;
import org.bahmni.module.bahmnicore.model.VideoFormats;
import org.bahmni.module.bahmnicore.properties.BahmniCoreProperties;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.openmrs.Patient;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.springframework.http.ResponseEntity;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;

import static org.junit.Assert.*;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.verifyStatic;

@RunWith(PowerMockRunner.class)
@PrepareForTest({BahmniCoreProperties.class, FileInputStream.class, FileUtils.class, ImageIO.class})
public class PatientDocumentServiceImplTest {

    private PatientDocumentServiceImpl patientDocumentService;
    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Rule
    TemporaryFolder temporaryFolder = new TemporaryFolder();


    @Test
    public void shouldCreateRightDirectoryAccordingToPatientId() {
        PowerMockito.mockStatic(BahmniCoreProperties.class);
        when(BahmniCoreProperties.getProperty("bahmnicore.documents.baseDirectory")).thenReturn("");
        patientDocumentService = new PatientDocumentServiceImpl();

        String url = patientDocumentService.createFilePath(".", 280, "Radiology", "jpeg");

        assertFalse(url.isEmpty());
        assertTrue(url.startsWith("300/280-Radiology-"));
        assertTrue(url.endsWith(".jpeg"));

        File absoluteFileDirectory = new File("./300");
        absoluteFileDirectory.delete();
    }

    @Test
    public void shouldGetImageNotFoundForIfNoImageCapturedForPatientAndNoDefaultImageNotPresent() throws Exception {
        final FileInputStream fileInputStreamMock = PowerMockito.mock(FileInputStream.class);
        PowerMockito.whenNew(FileInputStream.class).withArguments(Matchers.anyString()).thenReturn(fileInputStreamMock);
        PowerMockito.mockStatic(BahmniCoreProperties.class);
        when(BahmniCoreProperties.getProperty("bahmnicore.images.directory")).thenReturn("");
        when(BahmniCoreProperties.getProperty("bahmnicore.images.directory.defaultImage")).thenReturn("");
        patientDocumentService = new PatientDocumentServiceImpl();

        ResponseEntity<Object> responseEntity = patientDocumentService.retriveImage("patientUuid");

        assertEquals(404, responseEntity.getStatusCode().value());
    }

    @Test
    public void shouldSaveVideo() throws Exception {
        PowerMockito.mockStatic(BahmniCoreProperties.class);
        when(BahmniCoreProperties.getProperty("bahmnicore.documents.baseDirectory")).thenReturn("");
        PowerMockito.mockStatic(FileUtils.class);

        Patient patient = new Patient();
        patient.setId(1);
        patient.setUuid("patient-uuid");

        patientDocumentService = new PatientDocumentServiceImpl();
        String url = patientDocumentService.saveDocument(1, "Consultation", "videoContent", "mp4", "video");

        assertTrue(url.matches(".*1-Consultation-.*.mp4"));
    }

    @Test
    public void shouldThrowExceptionWhenVideoFormatIsNotSupported() throws Exception {
        PowerMockito.mockStatic(BahmniCoreProperties.class);
        when(BahmniCoreProperties.getProperty("bahmnicore.documents.baseDirectory")).thenReturn("");
        PowerMockito.mockStatic(FileUtils.class);

        Patient patient = new Patient();
        patient.setId(1);
        patient.setUuid("patient-uuid");

        expectedException.expect(VideoFormatNotSupportedException.class);
        expectedException.expectMessage(String.format("The video format '%s' is not supported. Supported formats are %s",
                "xyz", Arrays.toString(VideoFormats.values())));

        patientDocumentService = new PatientDocumentServiceImpl();
        patientDocumentService.saveDocument(1, "Consultation", "videoContent", "xyz", "video");
    }

    @Test
    public void shouldSavePDF() throws Exception {
        PowerMockito.mockStatic(BahmniCoreProperties.class);
        when(BahmniCoreProperties.getProperty("bahmnicore.documents.baseDirectory")).thenReturn("");
        PowerMockito.mockStatic(FileUtils.class);

        Patient patient = new Patient();
        patient.setId(1);
        patient.setUuid("patient-uuid");

        patientDocumentService = new PatientDocumentServiceImpl();
        String url = patientDocumentService.saveDocument(1, "Consultation", "pdfContent", "pdf", "file");

        assertTrue(url.matches(".*1-Consultation-.*.pdf"));
    }

    @Test
    public void shouldSaveImage() throws Exception {
        PowerMockito.mockStatic(BahmniCoreProperties.class);
        PowerMockito.mockStatic(ImageIO.class);
        when(BahmniCoreProperties.getProperty("bahmnicore.documents.baseDirectory")).thenReturn("");
        BufferedImage bufferedImage = new BufferedImage(1,2, 2);
        when(ImageIO.read(Matchers.any(ByteArrayInputStream.class))).thenReturn(bufferedImage);
        when(ImageIO.write(eq(bufferedImage),eq("jpg"), Matchers.any(File.class))).thenReturn(true);

        Patient patient = new Patient();
        patient.setId(1);
        patient.setUuid("patient-uuid");

        patientDocumentService = new PatientDocumentServiceImpl();
        String url = patientDocumentService.saveDocument(1, "Consultation", "imageContent", "jpg", "image");

        assertTrue(url.matches(".*1-Consultation-.*.jpg"));
    }

    @Test
    public void shouldThrowExceptionWhenFileTypeIsNotSupported() throws Exception {
        PowerMockito.mockStatic(BahmniCoreProperties.class);
        when(BahmniCoreProperties.getProperty("bahmnicore.documents.baseDirectory")).thenReturn("");
        PowerMockito.mockStatic(FileUtils.class);

        Patient patient = new Patient();
        patient.setId(1);
        patient.setUuid("patient-uuid");

        expectedException.expect(FileTypeNotSupportedException.class);
        expectedException.expectMessage("The file type is not supported. Supported types are image/video/pdf");

        patientDocumentService = new PatientDocumentServiceImpl();
        patientDocumentService.saveDocument(1, "Consultation", "otherfileContent", "xyz", "csv");
    }

    @Test
    public void shouldThrowExceptionWhenImageTypeOtherThanPngJpegGif() throws Exception {
        PowerMockito.mockStatic(BahmniCoreProperties.class);
        when(BahmniCoreProperties.getProperty("bahmnicore.documents.baseDirectory")).thenReturn("");
        PowerMockito.mockStatic(FileUtils.class);
        PowerMockito.mockStatic(ImageIO.class);
        BufferedImage bufferedImage = new BufferedImage(1,2, 2);
        when(ImageIO.read(Matchers.any(ByteArrayInputStream.class))).thenReturn(bufferedImage);
        when(ImageIO.write(eq(bufferedImage),eq("bmp"), Matchers.any(File.class))).thenReturn(false);

        Patient patient = new Patient();
        patient.setId(1);
        patient.setUuid("patient-uuid");

        expectedException.expect(FileTypeNotSupportedException.class);
        expectedException.expectMessage("The image format 'bmp' is not supported. Supported formats are [png, jpeg, gif]");

        patientDocumentService = new PatientDocumentServiceImpl();
        patientDocumentService.saveDocument(1, "Consultation", "otherfileContent", "bmp", "image");
    }
    
    @Test
    public void shouldCreateThumbnailForVideo() throws Exception {
        PowerMockito.mockStatic(BahmniCoreProperties.class);
        when(BahmniCoreProperties.getProperty("bahmnicore.documents.baseDirectory")).thenReturn(temporaryFolder.getRoot().getAbsolutePath());

        Patient patient = new Patient();
        patient.setId(1);
        patient.setUuid("patient-uuid");

        byte[] allBytes = Files.readAllBytes(Paths.get("src/test/resources/SampleVideo.mov"));
        String content = Base64.encode(allBytes);
        patientDocumentService = new PatientDocumentServiceImpl();
        String url = patientDocumentService.saveDocument(1, "Consultation", content, "mp4", "video");
        String videoUrl = temporaryFolder.getRoot().getAbsolutePath()  + "/" + url;
        String thumbnailUrl  = temporaryFolder.getRoot().getAbsolutePath()  + "/" + url.split("\\.")[0] + "_thumbnail.jpg";
        assertTrue(Files.exists(Paths.get(videoUrl)));
        assertTrue(Files.exists(Paths.get(thumbnailUrl)));
        verifyStatic(times(1));
    }
}
