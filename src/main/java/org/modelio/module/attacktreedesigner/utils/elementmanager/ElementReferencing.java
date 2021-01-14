package org.modelio.module.attacktreedesigner.utils.elementmanager;

import java.util.ArrayList;
import java.util.List;
import org.modelio.api.modelio.model.IUmlModel;
import org.modelio.metamodel.uml.infrastructure.Dependency;
import org.modelio.metamodel.uml.infrastructure.ModelTree;
import org.modelio.metamodel.uml.statik.Attribute;
import org.modelio.metamodel.uml.statik.Class;
import org.modelio.metamodel.uml.statik.Classifier;
import org.modelio.metamodel.uml.statik.GeneralClass;
import org.modelio.module.attacktreedesigner.api.AttackTreeStereotypes;
import org.modelio.module.attacktreedesigner.api.IAttackTreeDesignerPeerModule;
import org.modelio.module.attacktreedesigner.impl.AttackTreeDesignerModule;
import org.modelio.module.attacktreedesigner.impl.AttackTreeModelChangeHandler;
import org.modelio.module.attacktreedesigner.utils.elementmanager.representation.ElementRepresentationManager;
import org.modelio.vcore.smkernel.mapi.MObject;

public class ElementReferencing {
    public static final String REF_DEFAULT_NAME = "ref";

    private static final String PATH_SEPARATOR = "/";

    private static List<Class> _availableTrees = null;

    public static List<String> getAvailableTreesFullPath() {
        List<String> availableTreeNames = new ArrayList<>();
        for(Class tree : _availableTrees) {           
            //            availableTreeNames.add(tree.getOwner().getName() + "::" +  tree.getName());        
            availableTreeNames.add(getElementFullPath(tree));        
        }
        return availableTreeNames;
    }

    /**
     * @return List of trees roots contained in the same package as the root of the selectedElement
     */
    public static void updateListOfAvailableTrees(ModelTree selectedElement) {
        _availableTrees = new ArrayList<>();
        
        ModelTree rootElement = ElementNavigationManager.getRootElement(selectedElement);
        
        for(Class clazz: AttackTreeDesignerModule.getInstance().getModuleContext().getModelingSession().findByClass(Class.class)) {
            if(clazz.isStereotyped(IAttackTreeDesignerPeerModule.MODULE_NAME, AttackTreeStereotypes.ROOT) 
                    && (!(clazz.equals(rootElement)))) {
                _availableTrees.add(clazz);
            }
        }
    }

    public static void updateReference(Class element, String referencedTreeName) {
        IUmlModel model = AttackTreeDesignerModule.getInstance().getModuleContext().getModelingSession().getModel();
        
        Class referencedTree = null;       
        
        if (!(referencedTreeName.equals("")))
            referencedTree = getTreeByAbsolutePathName(referencedTreeName);
        
        Attribute referenceAttribute = getRefAttribute(element);
        
        
        
        if(referenceAttribute != null) {
            if (referencedTree != null) {            
        
                referenceAttribute.setType(referencedTree);       
        
                if(CounterMeasureManager.isCountered(referencedTree, true)) {
                    ElementRepresentationManager.setClassColor(element, ElementRepresentationManager.COUNTERED_ATTACK_COLOR);
        
        
                } else {
                    ElementRepresentationManager.setClassColor(element, ElementRepresentationManager.DEFAULT_ATTACK_COLOR);
                }
            } else {             
                referenceAttribute.setType(model.getUmlTypes().getUNDEFINED());
                ElementRepresentationManager.setClassColor(element, ElementRepresentationManager.DEFAULT_ATTACK_COLOR);
            }
        }
        /*
         * Propagate to ascendants of reference
         */
        for(Dependency parentDependency : element.getImpactedDependency()) {
            if(parentDependency.isStereotyped(IAttackTreeDesignerPeerModule.MODULE_NAME, AttackTreeStereotypes.CONNECTION)) {
                Class elementParent = (Class) parentDependency.getImpacted();                
                AttackTreeModelChangeHandler.updateAndPropagateAttackTags(elementParent, false, false, true);                         
            }
        }
    }

    private static Class getTreeByAbsolutePathName(String referencedTreeName) {
        for (Class tree : _availableTrees) {
            if (getElementFullPath(tree).equals(referencedTreeName))
                return tree;
        }
        return null;
    }

    public static Attribute getRefAttribute(Classifier leaf) {
        for (Attribute attribute : leaf.getOwnedAttribute()) {
            if (attribute.isStereotyped(IAttackTreeDesignerPeerModule.MODULE_NAME, AttackTreeStereotypes.TREE_REFERENCE_ATTRIBUTE))
                return attribute;
        }
        return null;
    }

    public static Class getReferencedTree(Class reference) {
        List<Attribute> attributes = reference.getOwnedAttribute();
        
        for(Attribute attribute : attributes) {
            if (attribute.isStereotyped(IAttackTreeDesignerPeerModule.MODULE_NAME, AttackTreeStereotypes.TREE_REFERENCE_ATTRIBUTE)) {
                GeneralClass type = attribute.getType();
                if ((type != null)
                        && (type.isStereotyped(IAttackTreeDesignerPeerModule.MODULE_NAME, AttackTreeStereotypes.ROOT))){
                    return (Class) type;
                }
            }
        }
        return null;
    }

    public static String getElementFullPath(MObject element) {
        String fullPath = element.getName();
        MObject owner = element.getCompositionOwner();
        if(owner != null) {
            return getElementFullPath(owner) + PATH_SEPARATOR + fullPath;
        } else {
            return fullPath;
        }
    }

}
