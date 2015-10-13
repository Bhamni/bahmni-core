package org.bahmni.module.bahmnicore;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.api.context.Context;
import org.openmrs.module.BaseModuleActivator;

public class Activator extends BaseModuleActivator {

	private Log log = LogFactory.getLog(this.getClass());

	@Override
	public void started() {
		log.info("Started the Bahmni Core module");
        Context.updateSearchIndex();
    }

	@Override
	public void stopped() {
		log.info("Stopped the Bahmni Core module");
	}
}
