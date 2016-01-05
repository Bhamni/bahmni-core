CREATE  PROCEDURE delete_concept (name_concept VARCHAR(255))
BEGIN
    DECLARE conceptId INT default 0;

    select concept_id INTO conceptId from concept_name where name = name_concept and locale_preferred = 1;
    delete from concept_set where concept_set = conceptId;
    delete from concept_name where concept_id = conceptId;
    delete from concept_numeric where concept_id = conceptId;
    delete from concept_answer where concept_id = conceptId;
    delete from concept where concept_id = conceptId;
END;