package org.modelio.module.attacktreedesigner.property;

import java.util.ArrayList;
import java.util.List;
import org.modelio.api.module.propertiesPage.IModulePropertyTable;
import org.modelio.metamodel.uml.infrastructure.ModelElement;
import org.modelio.metamodel.uml.infrastructure.TagParameter;
import org.modelio.metamodel.uml.infrastructure.TaggedValue;
import org.modelio.metamodel.uml.statik.Class;
import org.modelio.module.attacktreedesigner.api.AttackTreeStereotypes;
import org.modelio.module.attacktreedesigner.api.AttackTreeTagTypes;
import org.modelio.module.attacktreedesigner.api.IAttackTreeDesignerPeerModule;
import org.modelio.module.attacktreedesigner.i18n.Messages;
import org.modelio.module.attacktreedesigner.utils.elementmanager.tags.ProbabilityTagManager;
import org.modelio.module.attacktreedesigner.utils.elementmanager.tags.SeverityTagManager;
import org.modelio.module.attacktreedesigner.utils.elementmanager.tags.TagsManager;

public class AttackPropertyPage implements IPropertyContent {
    /**
     * This method handles the changes of the given property, identified by its row index, of a selected element
     * to a new value.
     * @param MObject : the selected element
     * 
     * @param row : the row of the changed property
     * @param value : the new value of the property
     */
    @Override
    public void changeProperty(ModelElement element, int row, String value) {
        if(row==1) {
            // row=1 -> Name property
            element.setName(value);
        
        } else if (row == 2) {
            // row=2 -> Severity property
            TagsManager.setElementTagValue(element, AttackTreeStereotypes.ATTACK, AttackTreeTagTypes.SEVERITY, value);
        
        } else if (row == 3) {
            // row=3 -> Probability property
            TagsManager.setElementTagValue(element, AttackTreeStereotypes.ATTACK, AttackTreeTagTypes.PROBABILITY, value);
        
        } else if (row == 5) {
            // row=5 -> Security related
            TagsManager.setElementTagValue(element, AttackTreeStereotypes.ATTACK, AttackTreeTagTypes.SECURITY_RELATED, value);
        
        
        } else if (row == 6) {
            // row=6 -> Safety related
            TagsManager.setElementTagValue(element, AttackTreeStereotypes.ATTACK, AttackTreeTagTypes.SAFETY_RELATED, value);
        
        
        } else if (row == 7) {
            // row=7 -> Out of scope
            TagsManager.setElementTagValue(element, AttackTreeStereotypes.ATTACK, AttackTreeTagTypes.OUT_OF_SCOPE, value);
        
        } else if (row > 7 && row <= (element.getTag().size() + 1)) {
        
            /*
             * Add Update custom tag
             */
            int customTagPosition = row - 7;
            int currentCustomTagPosition = 1;
            List<TaggedValue> attackTags = element.getTag();
            for(TaggedValue attackTag:attackTags) {
                String tagDefinitionName = attackTag.getDefinition().getName();
                if(tagDefinitionName.equals(AttackTreeTagTypes.CUSTOM_TAG)) {
                    List<TagParameter> tagParameters = attackTag.getActual();
                    if(currentCustomTagPosition == customTagPosition && tagParameters.size() >= 2) {
                        tagParameters.get(1).setValue(value);
                        break;
                    } else {
                        currentCustomTagPosition++;
                    }
                }
            }
        }
    }

    /**
     * This method handles the construction of the property table of a selected element
     * @param MObject : the selected element
     * 
     * @param table : the property table to fulfill
     */
    @Override
    public void update(ModelElement element, IModulePropertyTable table) {
        /*
         *  add Name property (row = 1)
         */
        table.addProperty(Messages.getString("Ui.Property.Name.Name"), element.getName());
        
        
        /*
         *  add tags properties
         */
        // row=2 -> Severity property
        TaggedValue severityTag = element.getTag(IAttackTreeDesignerPeerModule.MODULE_NAME, AttackTreeStereotypes.ATTACK, AttackTreeTagTypes.SEVERITY);
        int minSeverityIndex = SeverityTagManager.getMinSeverityIndex((Class) element);
        table.addProperty (AttackTreeTagTypes.SEVERITY, TagsManager.getTagParameter(severityTag), 
                subArray(TagsManager.SEVERITY_VALUES, minSeverityIndex, TagsManager.SEVERITY_VALUES.length-1)); 
        
        // row=3 -> Probability property
        TaggedValue probabilityTag = element.getTag(IAttackTreeDesignerPeerModule.MODULE_NAME, AttackTreeStereotypes.ATTACK, AttackTreeTagTypes.PROBABILITY);
        int [] probabilityIndexBounds =  ProbabilityTagManager.getProbabilityIndexBounds((Class) element);
        table.addProperty (AttackTreeTagTypes.PROBABILITY, TagsManager.getTagParameter(probabilityTag), 
                subArray(TagsManager.PROBABILITY_VALUES, probabilityIndexBounds[0], probabilityIndexBounds[1])); 
        
        // row=4 -> Risk Level Consult property (Read only, because it is calculated automatically based on severity and probability property)
        table.addConsultProperty (AttackTreeTagTypes.RISK_LEVEL, TagsManager.getElementRiskLevel(element));
        
        // row=5 -> Security related
        TaggedValue securityRelatedTag = element.getTag(IAttackTreeDesignerPeerModule.MODULE_NAME, AttackTreeStereotypes.ATTACK, AttackTreeTagTypes.SECURITY_RELATED);
        table.addProperty(AttackTreeTagTypes.SECURITY_RELATED, TagsManager.getTagParameter(securityRelatedTag).equals("true"));
        
        // row=6 -> Safety related
        TaggedValue safetyRelatedTag = element.getTag(IAttackTreeDesignerPeerModule.MODULE_NAME, AttackTreeStereotypes.ATTACK, AttackTreeTagTypes.SAFETY_RELATED);
        table.addProperty(AttackTreeTagTypes.SAFETY_RELATED, TagsManager.getTagParameter(safetyRelatedTag).equals("true"));
        
        // row=7 -> Out of scope
        TaggedValue outOfScopeTag = element.getTag(IAttackTreeDesignerPeerModule.MODULE_NAME, AttackTreeStereotypes.ATTACK, AttackTreeTagTypes.OUT_OF_SCOPE);
        table.addProperty(AttackTreeTagTypes.OUT_OF_SCOPE, TagsManager.getTagParameter(outOfScopeTag).equals("true"));
        
        /*
         * Add Custom tags
         */
        List<TaggedValue> attackTags = element.getTag();
        for(TaggedValue attackTag:attackTags) {
            String tagDefinitionName = attackTag.getDefinition().getName();
            if(tagDefinitionName.equals(AttackTreeTagTypes.CUSTOM_TAG)) {
                List<TagParameter> tagParameters = attackTag.getActual();
                if(tagParameters.size() >= 2) {
                    table.addProperty(tagParameters.get(0).getValue(), tagParameters.get(1).getValue());
                }
            }
        }
    }

    /**
     * Sub array of an array
     * 
     * @param indexStart should be superior to 0
     * @param indexEnd should be inferior than array.length
     * @return sub array of array that starts from index indexStart inclusive and ends at indexEnd (inclusive)
     */
    private static String[] subArray(String[] array, int indexStart, int indexEnd) {
        List<String> list = new ArrayList<>();
        for(int i=indexStart; i <= indexEnd; i++) {
            list.add(array[i]);
        }
        return list.toArray(new String[list.size()]);
    }

}
