package org.bahmni.module.bahmnicore.extensions;

import groovy.lang.GroovyClassLoader;
import org.apache.log4j.Logger;
import org.bahmni.module.bahmnicore.dao.ApplicationDataDirectory;
import org.bahmni.module.bahmnicore.dao.impl.ApplicationDataDirectoryImpl;
import org.openmrs.module.bahmniemrapi.drugogram.contract.BaseTableExtension;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;

@Component
public class BahmniExtensions {

	private static final Logger log = Logger.getLogger(BahmniExtensions.class);

	private GroovyClassLoader groovyClassLoader;

	private ApplicationDataDirectory applicationDataDirectory;

	public BahmniExtensions() {
		groovyClassLoader = new GroovyClassLoader();
		applicationDataDirectory = new ApplicationDataDirectoryImpl();
	}

	public <T> BaseTableExtension<T> getExtension(String groovyExtensionFileName) {
		File treatmentRegimenExtensionGroovyPath = applicationDataDirectory
				.getFileFromConfig("openmrs"+ File.separator +"treatmentRegimenExtension" + File.separator + groovyExtensionFileName);
		if (!treatmentRegimenExtensionGroovyPath.exists()) {
			return new BaseTableExtension<T>();
		}

		try {
			Class clazz = groovyClassLoader.parseClass(treatmentRegimenExtensionGroovyPath);
			return (BaseTableExtension<T>)clazz.newInstance();
		}
		catch (IOException e) {
			log.error("Problem with the groovy class " + treatmentRegimenExtensionGroovyPath, e);
		}
		catch (InstantiationException e) {
			log.error("The groovy class " + treatmentRegimenExtensionGroovyPath + " cannot be instantiated", e);
		}
		catch (IllegalAccessException e) {
			log.error("Problem with the groovy class " + treatmentRegimenExtensionGroovyPath, e);
		}
		return new BaseTableExtension<T>();
	}

}
