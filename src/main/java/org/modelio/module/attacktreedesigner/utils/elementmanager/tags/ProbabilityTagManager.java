package org.modelio.module.attacktreedesigner.utils.elementmanager.tags;

import org.modelio.metamodel.uml.infrastructure.Dependency;
import org.modelio.metamodel.uml.statik.Class;
import org.modelio.module.attacktreedesigner.api.AttackTreeStereotypes;
import org.modelio.module.attacktreedesigner.api.AttackTreeTagTypes;
import org.modelio.module.attacktreedesigner.api.IAttackTreeDesignerPeerModule;
import org.modelio.module.attacktreedesigner.utils.elementmanager.ElementReferencing;

public class ProbabilityTagManager {
    public static int[] getProbabilityIndexBounds(Class node) {
        int[] defaultBounds = {0, TagsManager.PROBABILITY_VALUES.length - 1};
        
        if(node.isStereotyped(IAttackTreeDesignerPeerModule.MODULE_NAME, AttackTreeStereotypes.ATTACK)) {
        
            // may have at most 1 child Operator (i.e. AND/OR)
            for(Dependency dependency: node.getDependsOnDependency()) {
                if(dependency.isStereotyped(IAttackTreeDesignerPeerModule.MODULE_NAME, AttackTreeStereotypes.CONNECTION)) {
        
                    return getProbabilityIndexBounds((Class) dependency.getDependsOn()); 
                }
            }
        
        } else if (node.isStereotyped(IAttackTreeDesignerPeerModule.MODULE_NAME, AttackTreeStereotypes.OR)) {
        
            return getProbabilityIndexBoundsOROperator(node);
        
        } else if (node.isStereotyped(IAttackTreeDesignerPeerModule.MODULE_NAME, AttackTreeStereotypes.AND)) {
        
            return getProbabilityIndexBoundsANDOperator(node);
        
        } else if (node.isStereotyped(IAttackTreeDesignerPeerModule.MODULE_NAME, AttackTreeStereotypes.TREE_REFERENCE)) {
        
            // return the minProbabilityIndex of the reference
            Class referencedTree = ElementReferencing.getReferencedTree(node);
            if(referencedTree  != null)
                return getProbabilityIndexBounds(referencedTree);
            else
                return defaultBounds;
        }
        return defaultBounds;
    }

    private static int[] getProbabilityIndexBoundsANDOperator(Class node) {
        // max probability is the min of the probabilities of the children
        int maxProbabilityIndex = TagsManager.PROBABILITY_VALUES.length - 1;
        
        for(Dependency dependency: node.getDependsOnDependency()) {
            if(dependency.isStereotyped(IAttackTreeDesignerPeerModule.MODULE_NAME, AttackTreeStereotypes.CONNECTION)) {
        
                Class child = (Class) dependency.getDependsOn();
        
                int currrentMaxProbabilityIndex ;
        
                if(child.isStereotyped(IAttackTreeDesignerPeerModule.MODULE_NAME, AttackTreeStereotypes.ATTACK)) {
                    String childProbability = TagsManager.getElementTagParameter(child, AttackTreeStereotypes.ATTACK, AttackTreeTagTypes.PROBABILITY);
                    currrentMaxProbabilityIndex = 0;
                    for(int i=0; i<TagsManager.PROBABILITY_VALUES.length; i++) {
                        if(childProbability.equals(TagsManager.PROBABILITY_VALUES[i])) {
                            currrentMaxProbabilityIndex = i;
                            break;
                        }
                    }
        
        
                } else if(child.isStereotyped(IAttackTreeDesignerPeerModule.MODULE_NAME, AttackTreeStereotypes.OR)) {
        
                    currrentMaxProbabilityIndex = getProbabilityIndexBounds(child)[0];
                } else if(child.isStereotyped(IAttackTreeDesignerPeerModule.MODULE_NAME, AttackTreeStereotypes.AND)) {
        
                    currrentMaxProbabilityIndex = getProbabilityIndexBounds(child)[1];
                } else if(child.isStereotyped(IAttackTreeDesignerPeerModule.MODULE_NAME, AttackTreeStereotypes.TREE_REFERENCE)) {
        
                    Class childReference = ElementReferencing.getReferencedTree(child) ;
                    currrentMaxProbabilityIndex = TagsManager.PROBABILITY_VALUES.length - 1;
                    if(childReference != null) {
                        String childReferenceProbability = TagsManager.getElementTagParameter(childReference, AttackTreeStereotypes.ATTACK, AttackTreeTagTypes.PROBABILITY);
                        for(int i=0; i<TagsManager.PROBABILITY_VALUES.length; i++) {
                            if(childReferenceProbability.equals(TagsManager.PROBABILITY_VALUES[i])) {
                                currrentMaxProbabilityIndex = i;
                                break;
                            }
                        }
                    }
        
                } else {
                    // this branch must never be reached (as the child node must have one of the 4 previously stated stereotypes)
                    currrentMaxProbabilityIndex = maxProbabilityIndex;
                }
        
                if(currrentMaxProbabilityIndex < maxProbabilityIndex) {
                    maxProbabilityIndex = currrentMaxProbabilityIndex;
                }
            }
        }
        
        int[] probabilityBouns = {0,maxProbabilityIndex};
        return probabilityBouns;
    }

    private static int[] getProbabilityIndexBoundsOROperator(Class node) {
        // min probability is the max of the probabilities of the children
        int minProbabilityIndex = 0;
        
        for(Dependency dependency: node.getDependsOnDependency()) {
            if(dependency.isStereotyped(IAttackTreeDesignerPeerModule.MODULE_NAME, AttackTreeStereotypes.CONNECTION)) {
        
                Class child = (Class) dependency.getDependsOn();
        
                int currrentMinProbabilityIndex ;
        
                if(child.isStereotyped(IAttackTreeDesignerPeerModule.MODULE_NAME, AttackTreeStereotypes.ATTACK)) {
                    String childProbability = TagsManager.getElementTagParameter(child, AttackTreeStereotypes.ATTACK, AttackTreeTagTypes.PROBABILITY);
                    currrentMinProbabilityIndex = 0;
                    for(int i=0; i<TagsManager.PROBABILITY_VALUES.length; i++) {
                        if(childProbability.equals(TagsManager.PROBABILITY_VALUES[i])) {
                            currrentMinProbabilityIndex = i;
                            break;
                        }
                    }
        
        
                } else if(child.isStereotyped(IAttackTreeDesignerPeerModule.MODULE_NAME, AttackTreeStereotypes.TREE_REFERENCE)) {
        
                    Class childReference = ElementReferencing.getReferencedTree(child) ;
                    currrentMinProbabilityIndex = 0;
                    if(childReference != null) {
                        String childReferenceProbability = TagsManager.getElementTagParameter(childReference, AttackTreeStereotypes.ATTACK, AttackTreeTagTypes.PROBABILITY);
                        for(int i=0; i<TagsManager.PROBABILITY_VALUES.length; i++) {
                            if(childReferenceProbability.equals(TagsManager.PROBABILITY_VALUES[i])) {
                                currrentMinProbabilityIndex = i;
                                break;
                            }
                        }
                    }
        
                } else if(child.isStereotyped(IAttackTreeDesignerPeerModule.MODULE_NAME, AttackTreeStereotypes.OR)) {
        
                    currrentMinProbabilityIndex = getProbabilityIndexBounds(child)[0];
                } else if(child.isStereotyped(IAttackTreeDesignerPeerModule.MODULE_NAME, AttackTreeStereotypes.AND)) {
        
                    currrentMinProbabilityIndex = getProbabilityIndexBounds(child)[1];
                } else if(child.isStereotyped(IAttackTreeDesignerPeerModule.MODULE_NAME, AttackTreeStereotypes.TREE_REFERENCE)) {
        
                    Class childReference = ElementReferencing.getReferencedTree(child) ;
                    currrentMinProbabilityIndex = 0;
                    if(childReference != null) {
                        String childReferenceProbability = TagsManager.getElementTagParameter(childReference, AttackTreeStereotypes.ATTACK, AttackTreeTagTypes.PROBABILITY);
                        for(int i=0; i<TagsManager.PROBABILITY_VALUES.length; i++) {
                            if(childReferenceProbability.equals(TagsManager.PROBABILITY_VALUES[i])) {
                                currrentMinProbabilityIndex = i;
                                break;
                            }
                        }
                    }
        
                } else {
                    // this branch must never be executed (as the child node must have one of the 4 previously stated stereotypes)
                    currrentMinProbabilityIndex = minProbabilityIndex;
                }
        
                if(currrentMinProbabilityIndex > minProbabilityIndex) {
                    minProbabilityIndex = currrentMinProbabilityIndex;
                }
            }
        }
        
        
        
        int[] probabilityBouns = {minProbabilityIndex, TagsManager.PROBABILITY_VALUES.length - 1};
        return probabilityBouns;
    }

}
