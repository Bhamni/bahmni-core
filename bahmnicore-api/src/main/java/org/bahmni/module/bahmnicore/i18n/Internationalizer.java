package org.bahmni.module.bahmnicore.i18n;

import java.util.List;

/**
 * Internationalizer helps leverage and does provide an API to the internationalization caches of Ext I18N.
 */
public interface Internationalizer {

	/**
	 * Tells whether i18n support is enabled for metadata.
	 * This helps skip the use of the internationalizer altogether when it is marked as disabled.
	 */
	public boolean isEnabled();
	
	/**
	 * Returns the internationalized message key from a localized string.
	 * Eg. "កម្ពុជា" -> "addresshierarchy.cambodia". 
	 * @param A localized string.
	 * @return The i18n message key.
	 */
	public String getMessageKey(String msg);

	/**
	 * Returns the list of possible address hierarchy entries message keys
	 * when given a partial string out of the localized address entry.
	 * @param addressSearchString
	 * @return
	 */
	public List<String> getAddressMessageKeysByLikeName(String addressSearchString);
}