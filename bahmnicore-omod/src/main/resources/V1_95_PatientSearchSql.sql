DELETE FROM global_property
WHERE property IN (
  'emrapi.sqlSearch.activePatients',
  'emrapi.sqlSearch.activePatientsByProvider',
  'emrapi.sqlSearch.patientsToAdmit',
  'emrapi.sqlSearch.admittedPatients',
  'emrapi.sqlSearch.patientsToDischarge',
  'emrapi.sqlSearch.activePatientsByLocation',
  'emrapi.sqlSearch.highRiskPatients',
  'emrapi.sqlSearch.patientsHasPendingOrders',
  'emrapi.sqlGet.wardsListDetails'
);

INSERT INTO global_property (`property`, `property_value`, `description`, `uuid`)
VALUES ('emrapi.sqlSearch.activePatients',
        'select distinct
          concat(pn.given_name,\' \', pn.family_name) as name,
          pi.identifier as identifier,
          concat("",p.uuid) as uuid,
          concat("",v.uuid) as activeVisitUuid,
          IF(va.value_reference = "Admitted", "true", "false") as hasBeenAdmitted
        from visit v
        join person_name pn on v.patient_id = pn.person_id and pn.voided = 0 AND pn.preferred= 1
        join patient_identifier pi on v.patient_id = pi.patient_id and pi.preferred = 1 and pi.voided = 0
        join patient_identifier_type pit on pi.identifier_type = pit.patient_identifier_type_id
        join global_property gp on gp.property="emr.primaryIdentifierType" and gp.property_value=pit.uuid
        join person p on p.person_id = v.patient_id
        join location l on l.uuid = ${visit_location_uuid} and v.location_id = l.location_id
        left outer join visit_attribute va on va.visit_id = v.visit_id and va.attribute_type_id = (
          select visit_attribute_type_id from visit_attribute_type where name="Admission Status"
        ) and va.voided = 0
        where v.date_stopped is null AND v.voided = 0',
        'Sql query to get list of active patients',
        uuid()
);

insert into global_property (`property`, `property_value`, `description`, `uuid`)
values ('emrapi.sqlSearch.activePatientsByProvider','
  select distinct concat(pn.given_name," ", pn.family_name) as name,
  pi.identifier as identifier,
  concat("",p.uuid) as uuid,
  concat("",v.uuid) as activeVisitUuid,
  IF(va.value_reference = "Admitted", "true", "false") as hasBeenAdmitted
  from
    visit v join person_name pn on v.patient_id = pn.person_id and pn.voided = 0 and v.voided=0  AND pn.preferred= 1
    join patient_identifier pi on v.patient_id = pi.patient_id and pi.preferred = 1 and pi.voided = 0
    join patient_identifier_type pit on pi.identifier_type = pit.patient_identifier_type_id
    join global_property gp on gp.property="emr.primaryIdentifierType" and gp.property_value=pit.uuid
    join person p on p.person_id = v.patient_id  and p.voided=0
    join encounter en on en.visit_id = v.visit_id and en.voided=0
    join encounter_provider ep on ep.encounter_id = en.encounter_id  and ep.voided=0
    join provider pr on ep.provider_id=pr.provider_id and pr.retired=0
    join person per on pr.person_id=per.person_id and per.voided=0
    join location l on l.uuid=${visit_location_uuid} and l.location_id = v.location_id
    left outer join visit_attribute va on va.visit_id = v.visit_id and va.voided = 0 and va.attribute_type_id = (
				select visit_attribute_type_id from visit_attribute_type where name="Admission Status"
			)
  where
    v.date_stopped is null and
    pr.uuid=${provider_uuid}
    order by en.encounter_datetime desc',
    'Sql query to get list of active patients by provider uuid',
  uuid()
);

INSERT INTO global_property (`property`, `property_value`, `description`, `uuid`)
VALUES ('emrapi.sqlSearch.patientsToAdmit',
        'select distinct concat(pn.given_name,\' \', pn.family_name) as name,
        pi.identifier as identifier,
        concat("",p.uuid) as uuid,
        concat("",v.uuid) as activeVisitUuid
        from visit v
        join person_name pn on v.patient_id = pn.person_id and pn.voided = 0 AND v.voided = 0 AND pn.preferred = 1
        join patient_identifier pi on v.patient_id = pi.patient_id and pi.preferred = 1 and pi.voided = 0
        join patient_identifier_type pit on pi.identifier_type = pit.patient_identifier_type_id
        join global_property gp on gp.property="emr.primaryIdentifierType" and gp.property_value=pit.uuid
        join person p on v.patient_id = p.person_id
        join encounter e on v.visit_id = e.visit_id
        join obs o on e.encounter_id = o.encounter_id and o.voided = 0
        join concept c on o.value_coded = c.concept_id
        join concept_name cn on c.concept_id = cn.concept_id
        join location l on l.uuid=${visit_location_uuid} and v.location_id = l.location_id
        where v.date_stopped is null and cn.name = \'Admit Patient\' and v.visit_id not in (select visit_id
        from encounter ie join encounter_type iet
        on iet.encounter_type_id = ie.encounter_type
        where iet.name = \'ADMISSION\')',
        'Sql query to get list of patients to be admitted',
        uuid()
);

INSERT INTO global_property (`property`, `property_value`, `description`, `uuid`)
VALUES ('emrapi.sqlSearch.admittedPatients',
        'select distinct
          concat(pn.given_name," ", pn.family_name) as name,
          pi.identifier as identifier,
          concat("",p.uuid) as uuid,
          concat("",v.uuid) as activeVisitUuid,
          IF(va.value_reference = "Admitted", "true", "false") as hasBeenAdmitted
        from visit v
        join person_name pn on v.patient_id = pn.person_id and pn.voided = 0 AND pn.preferred = 1
        join patient_identifier pi on v.patient_id = pi.patient_id and pi.preferred = 1 and pi.voided = 0
        join patient_identifier_type pit on pi.identifier_type = pit.patient_identifier_type_id
        join global_property gp on gp.property="emr.primaryIdentifierType" and gp.property_value=pit.uuid
        join person p on v.patient_id = p.person_id
        join visit_attribute va on v.visit_id = va.visit_id and va.value_reference = "Admitted" and va.voided = 0
        join visit_attribute_type vat on vat.visit_attribute_type_id = va.attribute_type_id and vat.name = "Admission Status"
        join location l on l.uuid=${visit_location_uuid} and v.location_id = l.location_id
        where v.date_stopped is null AND v.voided = 0',
        'Sql query to get list of admitted patients',
        uuid()
);

INSERT INTO global_property (`property`, `property_value`, `description`, `uuid`)
VALUES ('emrapi.sqlSearch.patientsToDischarge',
        'SELECT DISTINCT
          concat(pn.given_name, \' \', pn.family_name) AS name,
          pi.identifier AS identifier,
          concat("", p.uuid) AS uuid,
          concat("", v.uuid) AS activeVisitUuid,
          IF(va.value_reference = "Admitted", "true", "false") as hasBeenAdmitted
        FROM visit v
        INNER JOIN person_name pn ON v.patient_id = pn.person_id and pn.voided is FALSE AND pn.preferred = 1
        INNER JOIN patient_identifier pi ON v.patient_id = pi.patient_id and pi.preferred = 1 and pi.voided = 0
        INNER JOIN patient_identifier_type pit on pi.identifier_type = pit.patient_identifier_type_id
        INNER JOIN global_property gp on gp.property="emr.primaryIdentifierType" and gp.property_value=pit.uuid
        INNER JOIN person p ON v.patient_id = p.person_id
        Inner Join (SELECT DISTINCT v.visit_id
          FROM encounter en
          INNER JOIN visit v ON v.visit_id = en.visit_id AND en.encounter_type =
            (SELECT encounter_type_id
              FROM encounter_type
            WHERE name = "ADMISSION")) v1 on v1.visit_id = v.visit_id
        INNER JOIN encounter e ON v.visit_id = e.visit_id
        INNER JOIN obs o ON e.encounter_id = o.encounter_id
        INNER JOIN concept_name cn ON o.value_coded = cn.concept_id AND cn.concept_name_type = "FULLY_SPECIFIED" AND cn.voided is FALSE
        JOIN location l on l.uuid=${visit_location_uuid} and v.location_id = l.location_id
        left outer join visit_attribute va on va.visit_id = v.visit_id and va.attribute_type_id =
          (select visit_attribute_type_id from visit_attribute_type where name="Admission Status")
        LEFT OUTER JOIN encounter e1 ON e1.visit_id = v.visit_id AND e1.encounter_type = (
          SELECT encounter_type_id
            FROM encounter_type
          WHERE name = "DISCHARGE") AND e1.voided is FALSE
        WHERE v.date_stopped IS NULL AND v.voided = 0 AND o.voided = 0 AND cn.name = "Discharge Patient" AND e1.encounter_id IS NULL',
        'Sql query to get list of patients to discharge',
        uuid()
);

INSERT INTO global_property (`property`, `property_value`, `description`, `uuid`)
VALUES ('emrapi.sqlSearch.activePatientsByLocation',
        'select distinct concat(pn.given_name," ", pn.family_name) as name,
 pi.identifier as identifier,
 concat("",p.uuid) as uuid,
 concat("",v.uuid) as activeVisitUuid,
 IF(va.value_reference = "Admitted", "true", "false") as hasBeenAdmitted
 from
   visit v join person_name pn on v.patient_id = pn.person_id and pn.voided = 0 and v.voided=0  AND pn.preferred= 1
   join patient_identifier pi on v.patient_id = pi.patient_id and pi.preferred = 1 and pi.voided = 0
   join patient_identifier_type pit on pi.identifier_type = pit.patient_identifier_type_id
   join global_property gp on gp.property="emr.primaryIdentifierType" and gp.property_value=pit.uuid
   join person p on p.person_id = v.patient_id  and p.voided=0
   join encounter en on en.visit_id = v.visit_id and en.voided=0
   left outer join location loc on en.location_id = loc.location_id
   join encounter_provider ep on ep.encounter_id = en.encounter_id  and ep.voided=0
   join provider pr on ep.provider_id=pr.provider_id and pr.retired=0
   join person per on pr.person_id=per.person_id and per.voided=0
   left outer join visit_attribute va on va.visit_id = v.visit_id and va.attribute_type_id = (
                select visit_attribute_type_id from visit_attribute_type where name="Admission Status"
            )
 where
   v.date_stopped is null and
   loc.uuid=${location_uuid}
   order by en.encounter_datetime desc',
        'SQL query to get list of active patients by location',
        uuid()
);

INSERT INTO global_property (`property`, `property_value`, `description`, `uuid`)
VALUES ('emrapi.sqlSearch.highRiskPatients',
        'SELECT DISTINCT
  concat(pn.given_name, " ", pn.family_name)           AS name,
  pi.identifier                                        AS identifier,
  concat("", p.uuid)                                   AS uuid,
  concat("", v.uuid)                                   AS activeVisitUuid,
  IF(va.value_reference = "Admitted", "true", "false") AS hasBeenAdmitted
FROM person p
  INNER JOIN person_name pn ON pn.person_id = p.person_id  AND pn.preferred= 1
  INNER JOIN patient_identifier pi ON pn.person_id = pi.patient_id and pi.preferred = 1 and pi.voided = 0
  INNER JOIN patient_identifier_type pit on pi.identifier_type = pit.patient_identifier_type_id
  INNER JOIN global_property gp on gp.property="emr.primaryIdentifierType" and gp.property_value=pit.uuid
  INNER JOIN visit v ON v.patient_id = p.person_id AND v.date_stopped IS NULL AND v.voided = 0
  INNER JOIN (SELECT
                max(test_obs.obs_group_id) AS max_id,
                test_obs.concept_id,
                test_obs.person_id
              FROM obs test_obs
                INNER JOIN concept c ON c.concept_id = test_obs.concept_id AND test_obs.voided = 0
                INNER JOIN concept_name cn
                  ON c.concept_id = cn.concept_id AND cn.concept_name_type = "FULLY_SPECIFIED" AND
                     cn.name IN (${testName})
              GROUP BY test_obs.person_id, test_obs.concept_id) AS tests ON tests.person_id = v.patient_id
  INNER JOIN obs abnormal_obs
    ON abnormal_obs.obs_group_id = tests.max_id AND abnormal_obs.value_coded = 1 AND abnormal_obs.voided = 0
  INNER JOIN concept abnormal_concept ON abnormal_concept.concept_id = abnormal_obs.concept_id
  INNER JOIN concept_name abnormal_concept_name
    ON abnormal_concept.concept_id = abnormal_concept_name.concept_id AND
       abnormal_concept_name.concept_name_type = "FULLY_SPECIFIED" AND
       abnormal_concept_name.name IN ("LAB_ABNORMAL")
  LEFT OUTER JOIN visit_attribute va ON va.visit_id = v.visit_id AND va.attribute_type_id =
                                                                     (SELECT visit_attribute_type_id
                                                                      FROM visit_attribute_type
                                                                      WHERE name = "Admission Status")',
        'SQL QUERY TO get LIST of patients with high risk',
        uuid()
);


INSERT INTO global_property (`property`, `property_value`, `description`, `uuid`)
VALUES ('emrapi.sqlSearch.patientsHasPendingOrders',
        'select distinct
          concat(pn.given_name, " ", pn.family_name) as name,
          pi.identifier as identifier,
          concat("",p.uuid) as uuid,
          concat("",v.uuid) as activeVisitUuid,
          IF(va.value_reference = "Admitted", "true", "false") as hasBeenAdmitted
        from visit v
        join person_name pn on v.patient_id = pn.person_id and pn.voided = 0  AND pn.preferred= 1
        join patient_identifier pi on v.patient_id = pi.patient_id and pi.preferred = 1 and pi.voided = 0
        join patient_identifier_type pit on pi.identifier_type = pit.patient_identifier_type_id
        join global_property gp on gp.property="emr.primaryIdentifierType" and gp.property_value=pit.uuid
        join person p on p.person_id = v.patient_id
        join orders on orders.patient_id = v.patient_id
        join order_type on orders.order_type_id = order_type.order_type_id and order_type.name != "Order" and order_type.name != "Drug Order"
        left outer join visit_attribute va on va.visit_id = v.visit_id and va.voided = 0 and va.attribute_type_id =
          (select visit_attribute_type_id from visit_attribute_type where name="Admission Status")
        where v.date_stopped is null AND v.voided = 0 and order_id not in
          (select obs.order_id
            from obs
          where person_id = pn.person_id and order_id = orders.order_id)',
        'Sql query to get list of patients who has pending orders',
        uuid()
);
INSERT INTO global_property (`property`, `property_value`, `description`, `uuid`)
VALUES ('emrapi.sqlGet.wardsListDetails',
        'SELECT
  b.bed_number AS ''Bed'',
  concat(pn.given_name, '' '', pn.family_name) AS ''Name'',
  pv.uuid AS ''Patient Uuid'',
  pi.identifier AS ''Id'',
  pv.gender AS ''Gender'',
  TIMESTAMPDIFF(YEAR, pv.birthdate, CURDATE()) AS ''Age'',
  pa.county_district AS ''District'',
  pa.city_village AS ''Village'',
  admission_provider_name.given_name AS ''Admission By'',
  cast(DATE_FORMAT(latestAdmissionEncounter.max_encounter_datetime, ''%d %b %y %h:%i %p'') AS CHAR) AS ''Admission Time'',
  diagnosis.diagnosisConcept AS ''Diagnosis'',
  diagnosis.certainty AS ''Diagnosis Certainty'',
  diagnosis.diagnosisOrder AS ''Diagnosis Order'',
  diagnosis.status AS ''Diagnosis Status'',
  diagnosis.diagnosis_provider AS ''Diagnosis Provider'',
  cast(DATE_FORMAT(diagnosis.diagnosis_datetime, ''%d %b %y %h:%i %p'') AS
       CHAR) AS ''Diagnosis Datetime'',
  dispositionInfo.providerName AS ''Disposition By'',
  cast(DATE_FORMAT(dispositionInfo.providerDate, ''%d %b %y %h:%i %p'') AS CHAR) AS ''Disposition Time'',
  adtNotes.value_text AS ''ADT Notes'',
  v.uuid AS ''Visit Uuid''
FROM bed_location_map blm
  INNER JOIN bed b
    ON blm.bed_id = b.bed_id AND
       b.status = ''OCCUPIED'' AND
       blm.location_id IN (SELECT child_location.location_id
                           FROM location child_location JOIN
                             location parent_location
                               ON parent_location.location_id =
                                  child_location.parent_location
                           WHERE
                             parent_location.name = ${location_name})
  INNER JOIN bed_patient_assignment_map bpam ON b.bed_id = bpam.bed_id AND date_stopped IS NULL
  INNER JOIN person pv ON pv.person_id = bpam.patient_id
  INNER JOIN person_name pn ON pn.person_id = pv.person_id and pn.preferred = 1
  INNER JOIN patient_identifier pi ON pv.person_id = pi.patient_id  and pi.preferred = 1 and pi.voided = 0
  INNER JOIN patient_identifier_type pit on pi.identifier_type = pit.patient_identifier_type_id
  INNER JOIN global_property gp on gp.property=''emr.primaryIdentifierType'' and gp.property_value=pit.uuid
  LEFT JOIN person_address pa ON pa.person_id = pv.person_id
  INNER JOIN (SELECT
                patient_id,
                max(encounter_datetime) AS max_encounter_datetime,
                max(visit_id) as visit_id,
                max(encounter_id) AS encounter_id
              FROM encounter
                INNER JOIN encounter_type ON encounter_type.encounter_type_id = encounter.encounter_type
              WHERE encounter_type.name = ''ADMISSION''
              GROUP BY patient_id) latestAdmissionEncounter ON pv.person_id = latestAdmissionEncounter.patient_id
  INNER JOIN visit v ON latestAdmissionEncounter.visit_id = v.visit_id
  LEFT OUTER JOIN obs adtNotes
    ON adtNotes.encounter_id = latestAdmissionEncounter.encounter_id AND adtNotes.voided = 0 AND
       adtNotes.concept_id = (SELECT concept_id
                              FROM concept_name
                              WHERE name = ''Adt Notes'' AND concept_name_type = ''FULLY_SPECIFIED'')
  LEFT OUTER JOIN encounter_provider ep ON ep.encounter_id = latestAdmissionEncounter.encounter_id
  LEFT OUTER JOIN provider admission_provider ON admission_provider.provider_id = ep.provider_id
  LEFT OUTER JOIN person_name admission_provider_name
    ON admission_provider_name.person_id = admission_provider.person_id
  LEFT OUTER JOIN (
                    SELECT
                      bpam.patient_id AS person_id,
                      concept_name.name AS disposition,
                      latestDisposition.obs_datetime AS providerDate,
                      person_name.given_name AS providerName
                    FROM bed_patient_assignment_map bpam
                      INNER JOIN (SELECT
                                    person_id,
                                    max(obs_id) obs_id
                                  FROM obs
                                  WHERE concept_id = (SELECT concept_id
                                                      FROM concept_name
                                                      WHERE
                                                        name = ''Disposition'' AND concept_name_type = ''FULLY_SPECIFIED'')
                                  GROUP BY person_id) maxObsId ON maxObsId.person_id = bpam.patient_id
                      INNER JOIN obs latestDisposition
                        ON maxObsId.obs_id = latestDisposition.obs_id AND latestDisposition.voided = 0
                      INNER JOIN concept_name ON latestDisposition.value_coded = concept_name.concept_id AND
                                                 concept_name_type = ''FULLY_SPECIFIED''
                      LEFT OUTER JOIN encounter_provider ep ON latestDisposition.encounter_id = ep.encounter_id
                      LEFT OUTER JOIN provider disp_provider ON disp_provider.provider_id = ep.provider_id
                      LEFT OUTER JOIN person_name ON person_name.person_id = disp_provider.person_id
                    WHERE bpam.date_stopped IS NULL
                  ) dispositionInfo ON pv.person_id = dispositionInfo.person_id
  LEFT OUTER JOIN (
                    SELECT
                      diagnosis.person_id AS person_id,
                      diagnosis.obs_id AS obs_id,
                      diagnosis.obs_datetime AS diagnosis_datetime,
                      if(diagnosisConceptName.name IS NOT NULL, diagnosisConceptName.name,
                         diagnosis.value_text) AS diagnosisConcept,
                      certaintyConceptName.name AS certainty,
                      diagnosisOrderConceptName.name AS diagnosisOrder,
                      diagnosisStatusConceptName.name AS status,
                      person_name.given_name AS diagnosis_provider
                    FROM bed_patient_assignment_map bpam
                      INNER JOIN visit latestVisit
                        ON latestVisit.patient_id = bpam.patient_id AND latestVisit.date_stopped IS NULL AND
                           bpam.date_stopped IS NULL
                      INNER JOIN encounter ON encounter.visit_id = latestVisit.visit_id
                      INNER JOIN obs diagnosis ON bpam.patient_id = diagnosis.person_id AND diagnosis.voided = 0 AND
                                                  diagnosis.encounter_id = encounter.encounter_id AND
                                                  diagnosis.concept_id IN (SELECT concept_id
                                                                           FROM concept_name
                                                                           WHERE name IN
                                                                                 (''Coded Diagnosis'', ''Non-Coded Diagnosis'')
                                                                                 AND
                                                                                 concept_name_type = ''FULLY_SPECIFIED'')
                      LEFT OUTER JOIN concept_name diagnosisConceptName
                        ON diagnosis.value_coded IS NOT NULL AND diagnosis.value_coded = diagnosisConceptName.concept_id
                           AND diagnosisConceptName.concept_name_type = ''FULLY_SPECIFIED''
                      LEFT OUTER JOIN encounter_provider ep ON diagnosis.encounter_id = ep.encounter_id
                      LEFT OUTER JOIN provider diagnosis_provider ON diagnosis_provider.provider_id = ep.provider_id
                      LEFT OUTER JOIN person_name ON person_name.person_id = diagnosis_provider.person_id
                      INNER JOIN obs certainty
                        ON diagnosis.obs_group_id = certainty.obs_group_id AND certainty.voided = 0 AND
                           certainty.concept_id = (SELECT concept_id
                                                   FROM concept_name
                                                   WHERE name = ''Diagnosis Certainty'' AND
                                                         concept_name_type = ''FULLY_SPECIFIED'')
                      LEFT OUTER JOIN concept_name certaintyConceptName
                        ON certainty.value_coded IS NOT NULL AND certainty.value_coded = certaintyConceptName.concept_id
                           AND certaintyConceptName.concept_name_type = ''FULLY_SPECIFIED''
                      INNER JOIN obs diagnosisOrder
                        ON diagnosis.obs_group_id = diagnosisOrder.obs_group_id AND diagnosisOrder.voided = 0 AND
                           diagnosisOrder.concept_id = (SELECT concept_id
                                                        FROM concept_name
                                                        WHERE name = ''Diagnosis order'' AND
                                                              concept_name_type = ''FULLY_SPECIFIED'')
                      LEFT OUTER JOIN concept_name diagnosisOrderConceptName ON diagnosisOrder.value_coded IS NOT NULL
                                                                                AND diagnosisOrder.value_coded =
                                                                                    diagnosisOrderConceptName.concept_id
                                                                                AND
                                                                                diagnosisOrderConceptName.concept_name_type
                                                                                = ''FULLY_SPECIFIED''
                      LEFT JOIN obs diagnosisStatus
                        ON diagnosis.obs_group_id = diagnosisStatus.obs_group_id AND diagnosisStatus.voided = 0 AND
                           diagnosisStatus.concept_id = (SELECT concept_id
                                                         FROM concept_name
                                                         WHERE name = ''Bahmni Diagnosis Status'' AND
                                                               concept_name_type = ''FULLY_SPECIFIED'')
                      LEFT OUTER JOIN concept_name diagnosisStatusConceptName ON diagnosisStatus.value_coded IS NOT NULL
                                                                                 AND diagnosisStatus.value_coded =
                                                                                     diagnosisStatusConceptName.concept_id
                                                                                 AND
                                                                                 diagnosisStatusConceptName.concept_name_type
                                                                                 = ''FULLY_SPECIFIED''
                  ) diagnosis ON diagnosis.person_id = pv.person_id',
        'Sql query to get list of wards',
        uuid()
);
