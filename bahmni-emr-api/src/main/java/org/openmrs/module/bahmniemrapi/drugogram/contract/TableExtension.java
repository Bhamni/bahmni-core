package org.openmrs.module.bahmniemrapi.drugogram.contract;

public interface TableExtension<T> {

	void update(T table);
	void update(T table, String patientUuid);
}
