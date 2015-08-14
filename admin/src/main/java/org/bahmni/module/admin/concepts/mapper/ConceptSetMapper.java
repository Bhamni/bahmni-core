package org.bahmni.module.admin.concepts.mapper;

import org.apache.commons.lang3.StringUtils;
import org.bahmni.csv.KeyValue;
import org.bahmni.module.admin.csv.models.ConceptReferenceTermRow;
import org.bahmni.module.admin.csv.models.ConceptRow;
import org.bahmni.module.admin.csv.models.ConceptRows;
import org.bahmni.module.admin.csv.models.ConceptSetRow;
import org.bahmni.module.referencedata.labconcepts.contract.ConceptReferenceTerm;
import org.bahmni.module.referencedata.labconcepts.contract.ConceptSet;
import org.bahmni.module.referencedata.labconcepts.contract.Concepts;

import java.util.ArrayList;
import java.util.List;

import static org.bahmni.module.admin.csv.utils.CSVUtils.getKeyValueList;

public class ConceptSetMapper {

    private ConceptMapper conceptMapper;

    public ConceptSetMapper() {
        conceptMapper = new ConceptMapper();
    }

    public ConceptSet map(ConceptSetRow conceptSetRow) {
        ConceptSet conceptSet = new ConceptSet();
        conceptSet.setUuid(conceptSetRow.getUuid());
        conceptSet.setUniqueName(conceptSetRow.getName());
        conceptSet.setDisplayName(conceptSetRow.getShortName());
        conceptSet.setClassName(conceptSetRow.conceptClass);
        conceptSet.setDescription(conceptSetRow.description);
        conceptSet.setChildren(getChildren(conceptSetRow));

        List<ConceptReferenceTerm> conceptReferenceTerms = new ArrayList<>();
        for (ConceptReferenceTermRow term : conceptSetRow.referenceTerms) {
            conceptReferenceTerms.add(new ConceptReferenceTerm(term.getReferenceTermCode(), term.getReferenceTermRelationship(), term.getReferenceTermSource()));
        }

        conceptSet.setConceptReferenceTermsList(conceptReferenceTerms);
        return conceptSet;
    }

    private List<String> getChildren(ConceptSetRow conceptSetRow) {
        List<String> children = new ArrayList<>();
        for (KeyValue child : conceptSetRow.getChildren()) {
            if (!StringUtils.isEmpty(child.getValue())) {
                children.add(child.getValue());
            }
        }
        return children;
    }


    //    private ConceptReferenceTerm getConceptReferenceTerm(ConceptSetRow conceptSetRow) {
//        ConceptReferenceTerm conceptReferenceTerm = new ConceptReferenceTerm();
//        conceptReferenceTerm.setReferenceTermCode(conceptSetRow.referenceTermCode);
//        conceptReferenceTerm.setReferenceTermRelationship(conceptSetRow.referenceTermRelationship);
//        conceptReferenceTerm.setReferenceTermSource(conceptSetRow.referenceTermSource);
//        return conceptReferenceTerm;
//    }
//
    public ConceptSetRow map(ConceptSet conceptSet) {
        String name = conceptSet.getUniqueName();
        String description = conceptSet.getDescription();
        String shortName = conceptSet.getDisplayName();
        String conceptClass = conceptSet.getClassName();
        List<KeyValue> children = getKeyValueList("child", conceptSet.getChildren());

        List<ConceptReferenceTermRow> referenceTermRows = new ArrayList<>();
        for (ConceptReferenceTerm term : conceptSet.getConceptReferenceTermsList()) {
            referenceTermRows.add(new ConceptReferenceTermRow(term.getReferenceTermSource(), term.getReferenceTermCode(), term.getReferenceTermRelationship()));
        }
        String uuid = conceptSet.getUuid();
        ConceptSetRow conceptSetRow = new ConceptSetRow(uuid, name, description, conceptClass, shortName, referenceTermRows, children);
        return conceptSetRow;
    }


    public ConceptRows mapAll(Concepts concepts) {
        ConceptRows conceptRows = new ConceptRows();
        List<ConceptRow> conceptRowList = new ArrayList<>();
        List<ConceptSetRow> conceptSetRowList = new ArrayList<>();
        for (org.bahmni.module.referencedata.labconcepts.contract.Concept concept : concepts.getConceptList()) {
            conceptRowList.add(conceptMapper.map(concept));
        }
        for (ConceptSet conceptSet : concepts.getConceptSetList()) {
            conceptSetRowList.add(map(conceptSet));
        }
        conceptRows.setConceptRows(conceptRowList);
        conceptRows.setConceptSetRows(conceptSetRowList);
        return conceptRows;
    }
}
