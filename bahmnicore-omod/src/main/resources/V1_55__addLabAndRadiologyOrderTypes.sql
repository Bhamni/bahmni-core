INSERT INTO order_type (`name`,`description`,`creator`,`date_created`,`retired`,`retired_by`,`date_retired`,`retire_reason`,`uuid`,`java_class_name`)
VALUES ('Lab Order','An order for laboratory tests',1,NOW(),0,NULL,NULL,NULL,UUID(),'org.openmrs.Order');

INSERT INTO order_type (`name`,`description`,`creator`,`date_created`,`retired`,`retired_by`,`date_retired`,`retire_reason`,`uuid`,`java_class_name`)
VALUES ('Radiology Order','An order for radiology tests',1,NOW(),0,NULL,NULL,NULL,UUID(),'org.openmrs.Order');
