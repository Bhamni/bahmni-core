select concept.concept_id from concept, concept_name where concept_name.concept_id = concept.concept_id and concept_name.name = 'REGISTRATION_CONCEPTS' into @concept_id;
update concept set is_set = 1 where concept_id = @concept_id;
delete from concept_set where concept_id = concept_set;