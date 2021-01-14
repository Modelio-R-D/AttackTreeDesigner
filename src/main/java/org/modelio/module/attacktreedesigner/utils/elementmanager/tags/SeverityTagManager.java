package org.modelio.module.attacktreedesigner.utils.elementmanager.tags;

import org.modelio.metamodel.uml.infrastructure.Dependency;
import org.modelio.metamodel.uml.statik.Class;
import org.modelio.module.attacktreedesigner.api.AttackTreeStereotypes;
import org.modelio.module.attacktreedesigner.api.AttackTreeTagTypes;
import org.modelio.module.attacktreedesigner.api.IAttackTreeDesignerPeerModule;
import org.modelio.module.attacktreedesigner.utils.elementmanager.ElementReferencing;

public class SeverityTagManager {
    public static int getMinSeverityIndex(Class node) {
        if(node.isStereotyped(IAttackTreeDesignerPeerModule.MODULE_NAME, AttackTreeStereotypes.ATTACK)) {
        
            // may have at most 1 child Operator (i.e. AND/OR)
            for(Dependency dependency: node.getDependsOnDependency()) {
                if(dependency.isStereotyped(IAttackTreeDesignerPeerModule.MODULE_NAME, AttackTreeStereotypes.CONNECTION)) {
        
                    return getMinSeverityIndex((Class) dependency.getDependsOn()); 
                }
            }
        
        } else if (node.isStereotyped(IAttackTreeDesignerPeerModule.MODULE_NAME, AttackTreeStereotypes.OR)) {
        
            return getMinSeverityIndexOROperator(node);
        
        } else if (node.isStereotyped(IAttackTreeDesignerPeerModule.MODULE_NAME, AttackTreeStereotypes.AND)) {
        
            return getMinSeverityIndexANDOperator(node);
        
        } else if (node.isStereotyped(IAttackTreeDesignerPeerModule.MODULE_NAME, AttackTreeStereotypes.TREE_REFERENCE)) {
        
            // return the minSeverityIndex of the reference
            Class referencedTree = ElementReferencing.getReferencedTree(node);
            if(referencedTree  != null)
                return getMinSeverityIndex(referencedTree);
            else
                return 0;
        }
        return 0;
    }

    private static int getMinSeverityIndexANDOperator(Class node) {
        int minSeverityLevel = 0;
        
        for(Dependency dependency: node.getDependsOnDependency()) {
            if(dependency.isStereotyped(IAttackTreeDesignerPeerModule.MODULE_NAME, AttackTreeStereotypes.CONNECTION)) {
                Class child = (Class) dependency.getDependsOn();
                int currentMinSeverityIndex;
        
                /*
                 * Calculate currentMinSeverityIndex depending on the child nature (Attack, reference, or else operator)
                 */
                if(child.isStereotyped(IAttackTreeDesignerPeerModule.MODULE_NAME, AttackTreeStereotypes.ATTACK)) {
                    String childSeverity = TagsManager.getElementTagParameter(child, AttackTreeStereotypes.ATTACK, AttackTreeTagTypes.SEVERITY);
                    currentMinSeverityIndex = 0;
                    for(int i=0; i<TagsManager.SEVERITY_VALUES.length; i++) {
                        if(childSeverity.equals(TagsManager.SEVERITY_VALUES[i])) {
                            currentMinSeverityIndex = i;
                            break;
                        }
                    }
                } else if(child.isStereotyped(IAttackTreeDesignerPeerModule.MODULE_NAME, AttackTreeStereotypes.TREE_REFERENCE)) {
                    currentMinSeverityIndex = 0;
                    Class childReference = ElementReferencing.getReferencedTree(child) ;
                    if(childReference != null) {
                        String childReferenceSeverity = TagsManager.getElementTagParameter(childReference, AttackTreeStereotypes.ATTACK, AttackTreeTagTypes.SEVERITY);
                        for(int i=0; i<TagsManager.SEVERITY_VALUES.length; i++) {
                            if(childReferenceSeverity.equals(TagsManager.SEVERITY_VALUES[i])) {
                                currentMinSeverityIndex = i;
                                break;
                            }
                        }
                    }
        
                } else {
                    currentMinSeverityIndex = getMinSeverityIndex(child);
                }
        
                /*
                 * update minSeverityIndex
                 */
                if( currentMinSeverityIndex > minSeverityLevel) 
                    minSeverityLevel = currentMinSeverityIndex;
        
            }
        }
        return minSeverityLevel;
    }

    private static int getMinSeverityIndexOROperator(Class node) {
        boolean nodeHasChildren = false;
        
        int minSeverityIndex = TagsManager.SEVERITY_VALUES.length-1;
        
        for(Dependency dependency: node.getDependsOnDependency()) {
            if(dependency.isStereotyped(IAttackTreeDesignerPeerModule.MODULE_NAME, AttackTreeStereotypes.CONNECTION)) {
                nodeHasChildren = true;
                Class child = (Class) dependency.getDependsOn();
        
                int currentMinSeverityIndex; 
        
                /*
                 * Calculate currentMinSeverityIndex depending on the child nature (Attack, reference, or else operator)
                 */
                if(child.isStereotyped(IAttackTreeDesignerPeerModule.MODULE_NAME, AttackTreeStereotypes.ATTACK)) {
                    String childSeverity = TagsManager.getElementTagParameter(child, AttackTreeStereotypes.ATTACK, AttackTreeTagTypes.SEVERITY);
                    currentMinSeverityIndex = 0;
                    for(int i=0; i<TagsManager.SEVERITY_VALUES.length; i++) {
                        if(childSeverity.equals(TagsManager.SEVERITY_VALUES[i])) {
                            currentMinSeverityIndex = i;
                            break;
                        }
                    }
                } else if(child.isStereotyped(IAttackTreeDesignerPeerModule.MODULE_NAME, AttackTreeStereotypes.TREE_REFERENCE)) {
                    Class childReference = ElementReferencing.getReferencedTree(child) ;
                    currentMinSeverityIndex = 0;
                    if(childReference != null) {
                        String childReferenceSeverity = TagsManager.getElementTagParameter(childReference, AttackTreeStereotypes.ATTACK, AttackTreeTagTypes.SEVERITY);
                        for(int i=0; i<TagsManager.SEVERITY_VALUES.length; i++) {
                            if(childReferenceSeverity.equals(TagsManager.SEVERITY_VALUES[i])) {
                                currentMinSeverityIndex = i;
                                break;
                            }
                        }
                    }
        
                } else {
                    currentMinSeverityIndex = getMinSeverityIndex(child);
                }
        
                /*
                 * update minSeverityIndex
                 */
                if( currentMinSeverityIndex < minSeverityIndex)
                    minSeverityIndex = currentMinSeverityIndex;
        
        
            }
        }
        
        if(nodeHasChildren)
            return minSeverityIndex;
        else
            return 0;
    }

}
