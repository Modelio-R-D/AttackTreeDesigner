package org.modelio.module.attacktreedesigner.property;

import java.util.ArrayList;
import java.util.List;
import org.modelio.api.module.propertiesPage.IModulePropertyTable;
import org.modelio.metamodel.uml.infrastructure.ModelElement;
import org.modelio.metamodel.uml.infrastructure.Note;
import org.modelio.metamodel.uml.infrastructure.TagParameter;
import org.modelio.metamodel.uml.infrastructure.TaggedValue;
import org.modelio.metamodel.uml.statik.Class;
import org.modelio.module.attacktreedesigner.api.AttackTreeNoteTypes;
import org.modelio.module.attacktreedesigner.api.AttackTreeStereotypes;
import org.modelio.module.attacktreedesigner.api.AttackTreeTagTypes;
import org.modelio.module.attacktreedesigner.api.IAttackTreeDesignerPeerModule;
import org.modelio.module.attacktreedesigner.i18n.Messages;
import org.modelio.module.attacktreedesigner.utils.elementmanager.ElementReferencing;
import org.modelio.module.attacktreedesigner.utils.elementmanager.tags.TagsManager;

public class TreeReferencePropertyPage implements IPropertyContent {
    @Override
    public void changeProperty(ModelElement element, int row, String value) {
        if (row == 1) {
            ElementReferencing.updateReference((Class) element, value);
        }
    }

    @Override
    public void update(ModelElement element, IModulePropertyTable table) {
        Class referenceTree = (Class) element;
        ElementReferencing.updateListOfAvailableTrees(referenceTree);
        
        
        
        /*
         * add referenced tree and show available trees
         */
        List<String> trees = new ArrayList<>();
        trees.add("");
        trees.addAll(ElementReferencing.getAvailableTreesFullPath());
        
        String[] availableTreeNames = trees.toArray(new String[0]);
        
        Class referencedTree = ElementReferencing.getReferencedTree(referenceTree);
        String value = "";
        if(referencedTree != null) {
            value = ElementReferencing.getElementFullPath(referencedTree);
        }
        table.addProperty (Messages.getString("Ui.Property.Reference.Name"), value, availableTreeNames);
               
        
        
        /*
         * Show referenced tree Root tags an counter measures (Show only)
         */
        if(referencedTree != null) {
        
            // Name
            table.addConsultProperty(Messages.getString("Ui.Property.Name.Name"), referencedTree.getName());
        
            // Severity property
            TaggedValue severityTag = referencedTree.getTag(IAttackTreeDesignerPeerModule.MODULE_NAME, AttackTreeStereotypes.ATTACK, AttackTreeTagTypes.SEVERITY);
            table.addConsultProperty (AttackTreeTagTypes.SEVERITY, TagsManager.getTagParameter(severityTag)); 
            
            // Probability property
            TaggedValue probabilityTag = referencedTree.getTag(IAttackTreeDesignerPeerModule.MODULE_NAME, AttackTreeStereotypes.ATTACK, AttackTreeTagTypes.PROBABILITY);
            table.addConsultProperty (AttackTreeTagTypes.PROBABILITY, TagsManager.getTagParameter(probabilityTag)); 
            
            // Risk Level Consult property (Read only, because it is calculated automatically based on severity and probability property)
            table.addConsultProperty (AttackTreeTagTypes.RISK_LEVEL, TagsManager.getElementRiskLevel(referencedTree));
            
            // Security related
            TaggedValue securityRelatedTag = referencedTree.getTag(IAttackTreeDesignerPeerModule.MODULE_NAME, AttackTreeStereotypes.ATTACK, AttackTreeTagTypes.SECURITY_RELATED);
            table.addConsultProperty(AttackTreeTagTypes.SECURITY_RELATED, TagsManager.getTagParameter(securityRelatedTag));
                    
            // Safety related
            TaggedValue safetyRelatedTag = referencedTree.getTag(IAttackTreeDesignerPeerModule.MODULE_NAME, AttackTreeStereotypes.ATTACK, AttackTreeTagTypes.SAFETY_RELATED);
            table.addConsultProperty(AttackTreeTagTypes.SAFETY_RELATED, TagsManager.getTagParameter(safetyRelatedTag));
            
            // Out of scope
            TaggedValue outOfScopeTag = referencedTree.getTag(IAttackTreeDesignerPeerModule.MODULE_NAME, AttackTreeStereotypes.ATTACK, AttackTreeTagTypes.OUT_OF_SCOPE);
            table.addConsultProperty(AttackTreeTagTypes.OUT_OF_SCOPE, TagsManager.getTagParameter(outOfScopeTag));
         
            // Counter measures
            List<Note> rootNotes = referencedTree.getDescriptor();
            for(Note rootNote:rootNotes) {
                if(rootNote.getModel().getName().equals(AttackTreeNoteTypes.COUNTER_MEASURE)) {
                    table.addConsultProperty (AttackTreeNoteTypes.COUNTER_MEASURE,rootNote.getContent());
                }
            }
            
            /*
             * Add Custom tags
             */
            List<TaggedValue> attackTags = referencedTree.getTag();
            for(TaggedValue attackTag:attackTags) {
                String tagDefinitionName = attackTag.getDefinition().getName();
                if(tagDefinitionName.equals(AttackTreeTagTypes.CUSTOM_TAG)) {
                    List<TagParameter> tagParameters = attackTag.getActual();
                    if(tagParameters.size() >= 2) {
                        table.addConsultProperty(tagParameters.get(0).getValue(), tagParameters.get(1).getValue());
                    }
                }
            }
        }
    }

}
