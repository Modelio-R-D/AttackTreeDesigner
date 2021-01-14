package org.modelio.module.attacktreedesigner.utils.elementmanager;

import java.util.List;
import org.modelio.metamodel.uml.infrastructure.Dependency;
import org.modelio.metamodel.uml.infrastructure.Note;
import org.modelio.metamodel.uml.statik.Class;
import org.modelio.module.attacktreedesigner.api.AttackTreeNoteTypes;
import org.modelio.module.attacktreedesigner.api.AttackTreeStereotypes;
import org.modelio.module.attacktreedesigner.api.AttackTreeTagTypes;
import org.modelio.module.attacktreedesigner.api.IAttackTreeDesignerPeerModule;
import org.modelio.module.attacktreedesigner.utils.elementmanager.tags.TagsManager;

public class CounterMeasureManager {
    public static boolean attackHasCounterMeasure(Class attack) {
        List<Note> attackNotes = attack.getDescriptor();
        for(Note note:attackNotes) {
            if(note.getModel().getName().equals(AttackTreeNoteTypes.COUNTER_MEASURE)) {
                return true;
            }
        }
        return false;
    }

    /**
     * @param node : can be attack, operator or reference (node should not be Deleted
     * @return return whether it is Countered (has counter measures attached to it or countered by its children).
     * Countered Tag of this node will be ignored but the tags of its children will be taken into account
     */
    public static boolean isCountered(Class node, boolean trustCounteredTag) {
        if(node.isStereotyped(IAttackTreeDesignerPeerModule.MODULE_NAME, AttackTreeStereotypes.ATTACK)) {
            
            // if the tag is updated and can be trusted
            if(trustCounteredTag)
                return TagsManager.getElementTagParameter(node, AttackTreeStereotypes.ATTACK, AttackTreeTagTypes.COUNTERED_ATTACK)
                        .equals("true");        
            // check if it had a counter measure
            if(attackHasCounterMeasure(node))
                return true;
            // Check if countered by its Operator Child if it has one
            for(Dependency dependency:node.getDependsOnDependency()) {
                if(dependency.isStereotyped(IAttackTreeDesignerPeerModule.MODULE_NAME, AttackTreeStereotypes.CONNECTION)) {
                    Class childOperator = (Class) dependency.getDependsOn();
                    if(!childOperator.isDeleted())
                        return isCountered(childOperator, true);
                }
            }
        
        
        } else if(node.isStereotyped(IAttackTreeDesignerPeerModule.MODULE_NAME, AttackTreeStereotypes.AND)) {            
            // Check if at least obne child is countered
            for(Dependency dependency:node.getDependsOnDependency()) {
                if(dependency.isStereotyped(IAttackTreeDesignerPeerModule.MODULE_NAME, AttackTreeStereotypes.CONNECTION)) {
                    Class childNode = (Class) dependency.getDependsOn();
        
                    if(!childNode.isDeleted() && isCountered(childNode, true))
                        return true;
                }
            }
        
        
        } else if(node.isStereotyped(IAttackTreeDesignerPeerModule.MODULE_NAME, AttackTreeStereotypes.OR)) {            
            // Check if has children and if all are countered
            boolean allChildrenAreCountered = true;
            boolean hasChildren = false;
            for(Dependency dependency:node.getDependsOnDependency()) {
                if(dependency.isStereotyped(IAttackTreeDesignerPeerModule.MODULE_NAME, AttackTreeStereotypes.CONNECTION)) {
        
        
                    Class childNode = (Class) dependency.getDependsOn();
                    if(!childNode.isDeleted()) {
                        hasChildren = true;
                        if(!isCountered(childNode, true))
                            allChildrenAreCountered = false;
                    }
        
                }
            }
            return (hasChildren && allChildrenAreCountered);
        
        
        } else if (node.isStereotyped(IAttackTreeDesignerPeerModule.MODULE_NAME, AttackTreeStereotypes.TREE_REFERENCE)) {            
            // check if referenced tree is countered
            Class referencedTree = ElementReferencing.getReferencedTree(node);
            if(referencedTree != null)
                return isCountered(referencedTree, true);
            else
                return false;
        }
        
        
        /*
         * has no counter measures nor Countered children
         */
        return false;
    }

}
