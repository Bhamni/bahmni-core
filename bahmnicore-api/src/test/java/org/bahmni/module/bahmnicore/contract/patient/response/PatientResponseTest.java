package org.bahmni.module.bahmnicore.contract.patient.response;

import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.openmrs.api.context.Context;
import org.openmrs.messagesource.MessageSourceService;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

@RunWith(PowerMockRunner.class)
@PrepareForTest(Context.class)
public class PatientResponseTest {

    @Mock
    private MessageSourceService messageSourceService;

    @Before
    public void setUp() {
        PowerMockito.mockStatic(Context.class);
        PowerMockito.when(Context.getMessageSourceService()).thenReturn(messageSourceService);
        when(Context.getMessageSourceService().getMessage(eq("Baker Street"))).thenReturn("Baker Street");
        when(Context.getMessageSourceService().getMessage(eq("address.highStreet"))).thenReturn("Rue Haute");
        when(Context.getMessageSourceService().getMessage(eq("address.oxfordCircus"))).thenReturn("Le Cirque d'Oxford");
        
        when(Context.getMessageSourceService().getMessage(eq("pit.id1"))).thenReturn("Identifiant primaire");
        when(Context.getMessageSourceService().getMessage(eq("pit.id2"))).thenReturn("Identifiant secondaire");
        when(Context.getMessageSourceService().getMessage(eq("pit.id3"))).thenReturn("pit.id3");
    }
    
    @Test
    public void shouldLocalizeAddressFieldValue() throws Exception {
      Assert.assertEquals("{\"addressField\":\"Rue Haute\"}", PatientResponse.localizeAddressFieldValue("{\"addressField\":\"address.highStreet\"}"));
      Assert.assertEquals("{\"addressField\":\"Baker Street\"}", PatientResponse.localizeAddressFieldValue("{\"addressField\":\"Baker Street\"}"));
      Assert.assertEquals("{\"addressField\":\"Rue Haute\",\"addressField2\":\"Le Cirque d'Oxford\"}", PatientResponse.localizeAddressFieldValue("{\"addressField\":\"address.highStreet\",\"addressField2\":\"address.oxfordCircus\"}"));
      Assert.assertEquals("{\"malformed\"_;\"json", PatientResponse.localizeAddressFieldValue("{\"malformed\"_;\"json"));
      Assert.assertEquals(null, PatientResponse.localizeAddressFieldValue(null));
      Assert.assertEquals("", PatientResponse.localizeAddressFieldValue(""));
    }
    
    @Test
    public void shouldLocalizePatientIdentifierTypes() {
      Assert.assertEquals("{\"Identifiant primaire\" : \"98765321\", \"Identifiant secondaire\" : \"MRS-1234\", \"pit.id3\" : \"foobar_666777\"}", PatientResponse.localizePatientIdentifierTypes("{\"pit.id1\" : \"98765321\", \"pit.id2\" : \"MRS-1234\", \"pit.id3\" : \"foobar_666777\"}"));
      Assert.assertEquals("{\"malformed\"_;\"json", PatientResponse.localizePatientIdentifierTypes("{\"malformed\"_;\"json"));
      Assert.assertEquals(null, PatientResponse.localizePatientIdentifierTypes(null));
      Assert.assertEquals("", PatientResponse.localizePatientIdentifierTypes(""));
    }
}