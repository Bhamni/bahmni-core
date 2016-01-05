set @concept_id = 0;
set @answer_concept_id = 0;
set @concept_name_short_id = 0;
set @concept_name_full_id = 0;
set @concept_source_id = 0;
set @concept_map_type_id = 0;

SELECT concept_source_id INTO @concept_source_id FROM concept_reference_source where name = 'org.openmrs.module.emrapi';
SELECT concept_map_type_id INTO @concept_map_type_id FROM concept_map_type where name = 'SAME-AS';


call add_concept(@concept_id, @concept_name_short_id, @concept_name_full_id, 'Admit Patient', 'Admit Patient', 'N/A', 'misc', false);
call add_concept_reference_map (@concept_id, @concept_source_id, 'ADMIT',@concept_map_type_id);
set @child1_concept_id = @concept_id;

call add_concept(@concept_id, @concept_name_short_id, @concept_name_full_id, 'Discharge Patient', 'Discharge Patient', 'N/A', 'misc', false);
call add_concept_reference_map (@concept_id, @concept_source_id, 'DISCHARGE',@concept_map_type_id);
set @child2_concept_id = @concept_id;

call add_concept(@concept_id, @concept_name_short_id, @concept_name_full_id, 'Transfer Patient', 'Transfer Patient', 'N/A', 'misc', false);
call add_concept_reference_map (@concept_id, @concept_source_id, 'TRANSFER',@concept_map_type_id);
set @child3_concept_id = @concept_id;


call add_concept(@concept_id, @concept_name_short_id, @concept_name_full_id,'Disposition','Disposition', 'Coded', 'Question', false);
call add_concept_reference_map (@concept_id, @concept_source_id, 'Disposition',@concept_map_type_id);
set @disposition_concept_id = @concept_id;

call add_concept_answer (@disposition_concept_id, @child1_concept_id, 1);
call add_concept_answer (@disposition_concept_id, @child2_concept_id, 1);
call add_concept_answer (@disposition_concept_id, @child3_concept_id, 1);

call add_concept(@concept_id, @concept_name_short_id, @concept_name_full_id,'Disposition Set','Disposition Set', 'N/A', 'misc', true);
call add_concept_reference_map (@concept_id, @concept_source_id, 'Disposition Concept Set',@concept_map_type_id);
set @set_concept_id = @concept_id;
call add_concept_set_members (@set_concept_id,@disposition_concept_id,1);

call add_concept(@concept_id, @concept_name_short_id, @concept_name_full_id, 'Disposition Note', 'Disposition Note', 'Text', 'misc', false);
call add_concept_reference_map (@concept_id, @concept_source_id, 'DispositionNote',@concept_map_type_id);
call add_concept_set_members (@set_concept_id,@concept_id,1);
