set @concept_id = 0;
set @answer_concept_id = 0;
set @concept_name_short_id = 0;
set @concept_name_full_id = 0;
set @concept_source_id = 0;
set @concept_map_type_id = 0;

SELECT concept_source_id INTO @concept_source_id FROM concept_reference_source where name = 'org.openmrs.module.emrapi';
SELECT concept_map_type_id INTO @concept_map_type_id FROM concept_map_type where name = 'SAME-AS';


call add_concept(@concept_id, @concept_name_short_id, @concept_name_full_id, 'Visit Diagnoses', 'Visit Diagnoses', 'N/A', 'ConvSet', true);
call add_concept_reference_map (@concept_id, @concept_source_id, 'Diagnosis Concept Set',@concept_map_type_id);
set @set_concept_id = @concept_id;


call add_concept(@concept_id, @concept_name_short_id, @concept_name_full_id,'Non-coded Diagnosis','Non-coded Diagnosis', 'Text', 'Question', false);
call add_concept_reference_map (@concept_id, @concept_source_id, 'Non-Coded Diagnosis',@concept_map_type_id);
call add_concept_set_members (@set_concept_id,@concept_id,1);

call add_concept(@concept_id, @concept_name_short_id, @concept_name_full_id,'Coded Diagnosis','Coded Diagnosis', 'Coded', 'Question', false);
call add_concept_reference_map (@concept_id, @concept_source_id, 'Coded Diagnosis',@concept_map_type_id);
call add_concept_set_members (@set_concept_id,@concept_id,1);

call add_concept(@concept_id, @concept_name_short_id, @concept_name_full_id,'Diagnosis Certainty','Diagnosis Certainty', 'Coded', 'Question', false);
call add_concept_reference_map (@concept_id, @concept_source_id, 'Diagnosis Certainty',@concept_map_type_id);
call add_concept_set_members (@set_concept_id,@concept_id,1);
set @parent_concept_id = @concept_id;

call add_concept(@concept_id, @concept_name_short_id, @concept_name_full_id,'Presumed','Presumed', 'N/A', 'Misc', false);
call add_concept_reference_map (@concept_id, @concept_source_id, 'Presumed',@concept_map_type_id);
set @child1_concept_id = @concept_id;

call add_concept(@concept_id, @concept_name_short_id, @concept_name_full_id,'Confirmed','Confirmed', 'N/A', 'Misc', false);
call add_concept_reference_map (@concept_id, @concept_source_id, 'Confirmed',@concept_map_type_id);
set @child2_concept_id = @concept_id;

call add_concept_answer (@parent_concept_id, @child1_concept_id, 1);
call add_concept_answer (@parent_concept_id, @child2_concept_id, 1);

call add_concept(@concept_id, @concept_name_short_id, @concept_name_full_id,'Diagnosis order','Diagnosis order', 'Coded', 'Question', false);
call add_concept_reference_map (@concept_id, @concept_source_id, 'Diagnosis Order',@concept_map_type_id);
call add_concept_set_members (@set_concept_id,@concept_id,1);
set @parent_concept_id = @concept_id;

call add_concept(@concept_id, @concept_name_short_id, @concept_name_full_id,'Secondary','Secondary', 'N/A', 'Misc', false);
call add_concept_reference_map (@concept_id, @concept_source_id, 'Secondary',@concept_map_type_id);
set @child1_concept_id = @concept_id;

call add_concept(@concept_id, @concept_name_short_id, @concept_name_full_id,'Primary','Primary', 'N/A', 'Misc', false);
call add_concept_reference_map (@concept_id, @concept_source_id, 'Primary',@concept_map_type_id);
set @child2_concept_id = @concept_id;

call add_concept_answer (@parent_concept_id, @child1_concept_id, 1);
call add_concept_answer (@parent_concept_id, @child2_concept_id, 1);