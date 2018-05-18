package org.bahmni.module.bahmnicore.contract.patient.search;

import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.type.Type;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.openmrs.Location;
import org.openmrs.api.LocationService;

import java.util.Arrays;

import static junit.framework.TestCase.assertNotNull;
import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

@RunWith(MockitoJUnitRunner.class)
public class PatientSearchBuilderTest {

    @Mock
    private SessionFactory sessionFactory;

    @Mock
    private Session session;

    @Mock
    private SQLQuery mockSqlQuery;

    @Mock
    private LocationService locationService;

    @Mock
    private Location location;

    @Captor
    ArgumentCaptor<String> queryCaptor;

    @Before
    public void setUp() throws Exception {
        initMocks(this);
        when(sessionFactory.getCurrentSession()).thenReturn(session);
        when(session.createSQLQuery(queryCaptor.capture())).thenReturn(mockSqlQuery);
        when(mockSqlQuery.addScalar(any(String.class))).thenReturn(mockSqlQuery);
        when(mockSqlQuery.addScalar(any(String.class),any(Type.class))).thenReturn(mockSqlQuery);
    }

    @Test
    public void ensurePatientSearchSqlQueryIsProperlyConstructed(){
        SQLQuery sqlQuery = new PatientSearchBuilder(sessionFactory)
                .withPatientName("Ram")
                .withPatientAddress(null, null, new String[]{"address3"})
                .withPatientIdentifier("GAN200002", false)
                .withPatientAttributes("caste", Arrays.asList(new Integer(1),new Integer(2)), Arrays.asList(new Integer(4)))
                .withProgramAttributes(null, null)
                .buildSqlQuery(10, 2);

        assertNotNull(sqlQuery);

        assertEquals("select p.uuid as uuid, p.person_id as personId, pn.given_name as givenName, pn.middle_name as middleName, pn.family_name as familyName, p.gender as gender, p.birthdate as birthDate, p.death_date as deathDate, p.date_created as dateCreated, v.uuid as activeVisitUuid, primary_identifier.identifier as identifier, extra_identifiers.identifiers as extraIdentifiers, (CASE va.value_reference WHEN 'Admitted' THEN TRUE ELSE FALSE END) as hasBeenAdmitted ,CONCAT ('{ \"address3\" : ' , '\"' , IFNULL(pa.address3 ,''), '\"' , '}') as addressFieldValue,concat('{',group_concat(DISTINCT (coalesce(concat('\"',attrt_results.name,'\":\"', REPLACE(REPLACE(coalesce(cn.name, def_loc_cn.name, pattr_results.value),'\\\\','\\\\\\\\'),'\"','\\\\\"'),'\"'))) SEPARATOR ','),'}') as customAttribute from person p  left join person_name pn on pn.person_id = p.person_id left join person_address pa on p.person_id=pa.person_id and pa.voided = 'false' JOIN (SELECT identifier, patient_id      FROM patient_identifier pi JOIN patient_identifier_type pit ON pi.identifier_type = pit.patient_identifier_type_id AND pi.voided IS FALSE AND pit.retired IS FALSE JOIN global_property gp ON gp.property = 'bahmni.primaryIdentifierType' AND gp.property_value = pit.uuid      GROUP BY pi.patient_id) as primary_identifier ON p.person_id = primary_identifier.patient_id LEFT JOIN (SELECT concat('{', group_concat((concat('\"', pit.name, '\":\"', pi.identifier, '\"')) SEPARATOR ','), '}') AS identifiers,        patient_id      FROM patient_identifier pi        JOIN patient_identifier_type pit ON pi.identifier_type = pit.patient_identifier_type_id AND pi.voided IS FALSE AND pit.retired IS FALSE  JOIN global_property gp ON gp.property = 'bahmni.primaryIdentifierType' AND gp.property_value != pit.uuid  GROUP BY pi.patient_id) as extra_identifiers ON p.person_id = extra_identifiers.patient_id left outer join visit v on v.patient_id = p.person_id and v.date_stopped is null  left outer join visit_attribute va on va.visit_id = v.visit_id    and va.attribute_type_id = (select visit_attribute_type_id from visit_attribute_type where name='Admission Status')    and va.voided = 0 JOIN (SELECT pi.patient_id FROM patient_identifier pi  JOIN patient_identifier_type pit ON pi.identifier_type = pit.patient_identifier_type_id AND pi.voided IS FALSE  JOIN global_property gp ON gp.property IN ('bahmni.primaryIdentifierType' ) AND gp.property_value LIKE concat('%', pit.uuid, '%') AND pi.identifier LIKE '%GAN200002%' GROUP BY pi.patient_id)  AS matched_patient ON matched_patient.patient_id = p.person_id LEFT OUTER JOIN person_attribute pattrln on pattrln.person_id = p.person_id and pattrln.person_attribute_type_id in (1,2)  LEFT OUTER JOIN person_attribute pattr_results on pattr_results.person_id = p.person_id and pattr_results.person_attribute_type_id in (4)  LEFT OUTER JOIN person_attribute_type attrt_results on attrt_results.person_attribute_type_id = pattr_results.person_attribute_type_id and pattr_results.voided = 0  LEFT OUTER JOIN concept_name cn on cn.concept_id = pattr_results.value and cn.concept_name_type = 'FULLY_SPECIFIED' and attrt_results.format = 'org.openmrs.Concept' and cn.locale = 'en_GB' LEFT OUTER JOIN concept_name def_loc_cn on def_loc_cn.concept_id = pattr_results.value and def_loc_cn.concept_name_type = 'FULLY_SPECIFIED' and attrt_results.format = 'org.openmrs.Concept' and def_loc_cn.locale = 'en'  where p.voided = 'false' and pn.voided = 'false' and pn.preferred=true  and ( concat_ws(' ',coalesce(given_name), coalesce(middle_name), coalesce(family_name)) like  '%Ram%') and ( pattrln.value like '%caste%') group by null,  p.person_id order by primary_identifier.identifier asc LIMIT :limit OFFSET :offset",queryCaptor.getValue());

    }

    @Test
    public void ensurePatientSearchQueryIsProperlyConstructedWhenThereIsSingleQuoteInPatientAttribute(){
        SQLQuery sqlQuery = new PatientSearchBuilder(sessionFactory)
                .withPatientName("")
                .withPatientAddress(null, null, new String[]{"address3"})
                .withPatientIdentifier("", false)
                .withPatientAttributes("go'nd", Arrays.asList(new Integer(1),new Integer(2)), Arrays.asList(new Integer(4)))
                .withProgramAttributes(null, null)
                .buildSqlQuery(10, 2);

        assertNotNull(sqlQuery);
        assertEquals("select p.uuid as uuid, p.person_id as personId, pn.given_name as givenName, pn.middle_name as middleName, pn.family_name as familyName, p.gender as gender, p.birthdate as birthDate, p.death_date as deathDate, p.date_created as dateCreated, v.uuid as activeVisitUuid, primary_identifier.identifier as identifier, extra_identifiers.identifiers as extraIdentifiers, (CASE va.value_reference WHEN 'Admitted' THEN TRUE ELSE FALSE END) as hasBeenAdmitted ,CONCAT ('{ \"address3\" : ' , '\"' , IFNULL(pa.address3 ,''), '\"' , '}') as addressFieldValue,concat('{',group_concat(DISTINCT (coalesce(concat('\"',attrt_results.name,'\":\"', REPLACE(REPLACE(coalesce(cn.name, def_loc_cn.name, pattr_results.value),'\\\\','\\\\\\\\'),'\"','\\\\\"'),'\"'))) SEPARATOR ','),'}') as customAttribute from person p  left join person_name pn on pn.person_id = p.person_id left join person_address pa on p.person_id=pa.person_id and pa.voided = 'false' JOIN (SELECT identifier, patient_id      FROM patient_identifier pi JOIN patient_identifier_type pit ON pi.identifier_type = pit.patient_identifier_type_id AND pi.voided IS FALSE AND pit.retired IS FALSE JOIN global_property gp ON gp.property = 'bahmni.primaryIdentifierType' AND gp.property_value = pit.uuid      GROUP BY pi.patient_id) as primary_identifier ON p.person_id = primary_identifier.patient_id LEFT JOIN (SELECT concat('{', group_concat((concat('\"', pit.name, '\":\"', pi.identifier, '\"')) SEPARATOR ','), '}') AS identifiers,        patient_id      FROM patient_identifier pi        JOIN patient_identifier_type pit ON pi.identifier_type = pit.patient_identifier_type_id AND pi.voided IS FALSE AND pit.retired IS FALSE  JOIN global_property gp ON gp.property = 'bahmni.primaryIdentifierType' AND gp.property_value != pit.uuid  GROUP BY pi.patient_id) as extra_identifiers ON p.person_id = extra_identifiers.patient_id left outer join visit v on v.patient_id = p.person_id and v.date_stopped is null  left outer join visit_attribute va on va.visit_id = v.visit_id    and va.attribute_type_id = (select visit_attribute_type_id from visit_attribute_type where name='Admission Status')    and va.voided = 0 LEFT OUTER JOIN person_attribute pattrln on pattrln.person_id = p.person_id and pattrln.person_attribute_type_id in (1,2)  LEFT OUTER JOIN person_attribute pattr_results on pattr_results.person_id = p.person_id and pattr_results.person_attribute_type_id in (4)  LEFT OUTER JOIN person_attribute_type attrt_results on attrt_results.person_attribute_type_id = pattr_results.person_attribute_type_id and pattr_results.voided = 0  LEFT OUTER JOIN concept_name cn on cn.concept_id = pattr_results.value and cn.concept_name_type = 'FULLY_SPECIFIED' and attrt_results.format = 'org.openmrs.Concept' and cn.locale = 'en_GB' LEFT OUTER JOIN concept_name def_loc_cn on def_loc_cn.concept_id = pattr_results.value and def_loc_cn.concept_name_type = 'FULLY_SPECIFIED' and attrt_results.format = 'org.openmrs.Concept' and def_loc_cn.locale = 'en'  where p.voided = 'false' and pn.voided = 'false' and pn.preferred=true  and ( pattrln.value like '%go''nd%') group by null,  p.person_id order by primary_identifier.identifier asc LIMIT :limit OFFSET :offset",queryCaptor.getValue());
    }

    @Test
    public void ensurePatientSearchQueryIsProperlyConstructedToGetAddressAndPatientAttributes(){
        SQLQuery sqlQuery = new PatientSearchBuilder(sessionFactory)
                .withPatientName(null)
                .withPatientAddress(null, null, new String[]{"address3"})
                .withPatientIdentifier("GAN200002", false)
                .withPatientAttributes(null, Arrays.asList(new Integer(1),new Integer(2)), Arrays.asList(new Integer(4)))
                .withProgramAttributes(null, null)
                .buildSqlQuery(10, 2);

        assertNotNull(sqlQuery);
        assertEquals("select p.uuid as uuid, p.person_id as personId, pn.given_name as givenName, pn.middle_name as middleName, pn.family_name as familyName, p.gender as gender, p.birthdate as birthDate, p.death_date as deathDate, p.date_created as dateCreated, v.uuid as activeVisitUuid, primary_identifier.identifier as identifier, extra_identifiers.identifiers as extraIdentifiers, (CASE va.value_reference WHEN 'Admitted' THEN TRUE ELSE FALSE END) as hasBeenAdmitted ,CONCAT ('{ \"address3\" : ' , '\"' , IFNULL(pa.address3 ,''), '\"' , '}') as addressFieldValue,concat('{',group_concat(DISTINCT (coalesce(concat('\"',attrt_results.name,'\":\"', REPLACE(REPLACE(coalesce(cn.name, def_loc_cn.name, pattr_results.value),'\\\\','\\\\\\\\'),'\"','\\\\\"'),'\"'))) SEPARATOR ','),'}') as customAttribute from person p  left join person_name pn on pn.person_id = p.person_id left join person_address pa on p.person_id=pa.person_id and pa.voided = 'false' JOIN (SELECT identifier, patient_id      FROM patient_identifier pi JOIN patient_identifier_type pit ON pi.identifier_type = pit.patient_identifier_type_id AND pi.voided IS FALSE AND pit.retired IS FALSE JOIN global_property gp ON gp.property = 'bahmni.primaryIdentifierType' AND gp.property_value = pit.uuid      GROUP BY pi.patient_id) as primary_identifier ON p.person_id = primary_identifier.patient_id LEFT JOIN (SELECT concat('{', group_concat((concat('\"', pit.name, '\":\"', pi.identifier, '\"')) SEPARATOR ','), '}') AS identifiers,        patient_id      FROM patient_identifier pi        JOIN patient_identifier_type pit ON pi.identifier_type = pit.patient_identifier_type_id AND pi.voided IS FALSE AND pit.retired IS FALSE  JOIN global_property gp ON gp.property = 'bahmni.primaryIdentifierType' AND gp.property_value != pit.uuid  GROUP BY pi.patient_id) as extra_identifiers ON p.person_id = extra_identifiers.patient_id left outer join visit v on v.patient_id = p.person_id and v.date_stopped is null  left outer join visit_attribute va on va.visit_id = v.visit_id    and va.attribute_type_id = (select visit_attribute_type_id from visit_attribute_type where name='Admission Status')    and va.voided = 0 JOIN (SELECT pi.patient_id FROM patient_identifier pi  JOIN patient_identifier_type pit ON pi.identifier_type = pit.patient_identifier_type_id AND pi.voided IS FALSE  JOIN global_property gp ON gp.property IN ('bahmni.primaryIdentifierType' ) AND gp.property_value LIKE concat('%', pit.uuid, '%') AND pi.identifier LIKE '%GAN200002%' GROUP BY pi.patient_id)  AS matched_patient ON matched_patient.patient_id = p.person_id LEFT OUTER JOIN person_attribute pattrln on pattrln.person_id = p.person_id and pattrln.person_attribute_type_id in (1,2)  LEFT OUTER JOIN person_attribute pattr_results on pattr_results.person_id = p.person_id and pattr_results.person_attribute_type_id in (4)  LEFT OUTER JOIN person_attribute_type attrt_results on attrt_results.person_attribute_type_id = pattr_results.person_attribute_type_id and pattr_results.voided = 0  LEFT OUTER JOIN concept_name cn on cn.concept_id = pattr_results.value and cn.concept_name_type = 'FULLY_SPECIFIED' and attrt_results.format = 'org.openmrs.Concept' and cn.locale = 'en_GB' LEFT OUTER JOIN concept_name def_loc_cn on def_loc_cn.concept_id = pattr_results.value and def_loc_cn.concept_name_type = 'FULLY_SPECIFIED' and attrt_results.format = 'org.openmrs.Concept' and def_loc_cn.locale = 'en'  where p.voided = 'false' and pn.voided = 'false' and pn.preferred=true  group by null,  p.person_id order by primary_identifier.identifier asc LIMIT :limit OFFSET :offset",queryCaptor.getValue());
    }
}