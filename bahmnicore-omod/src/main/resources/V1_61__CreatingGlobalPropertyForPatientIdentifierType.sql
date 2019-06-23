select uuid from patient_identifier_type where name = 'Bahmni Id' into @uuid;
UPDATE global_property SET property_value = @uuid WHERE property='emr.primaryIdentifierType';
