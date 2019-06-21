set @property = 'emr.primaryIdentifierType', @propertyValue = '';
select uuid from patient_identifier_type where name = 'Bahmni Id' into @propertyValue;
INSERT into global_property (uuid, property, property_value) VALUES (uuid(), @property, @propertyValue) ON DUPLICATE KEY UPDATE property_value = @propertyValue; 