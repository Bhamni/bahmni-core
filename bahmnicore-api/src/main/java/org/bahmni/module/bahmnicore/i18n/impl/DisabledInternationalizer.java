package org.bahmni.module.bahmnicore.i18n.impl;

import java.util.Collections;
import java.util.List;

import org.bahmni.module.bahmnicore.i18n.Internationalizer;
import org.openmrs.annotation.OpenmrsProfile;

@OpenmrsProfile(modules = {"!exti18n"})
public class DisabledInternationalizer implements Internationalizer {

	@Override
	public boolean isEnabled() {
		return false;
	}
	
	@Override
	public String getMessageKey(String msg) {
		return msg;
	}

	@Override
	public List<String> getAddressMessageKeysByLikeName(String addressSearchString) {
		return Collections.emptyList();
	}
}