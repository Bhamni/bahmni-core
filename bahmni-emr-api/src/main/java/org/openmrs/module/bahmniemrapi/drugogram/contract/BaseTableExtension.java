package org.openmrs.module.bahmniemrapi.drugogram.contract;

public class BaseTableExtension<T> implements TableExtension<T> {

	@Override
	public void update(T table) {
		//Do nothing
	}

	@Override
	public void update(T table, String patientUuid) {
		//Do nothing
	}
}
