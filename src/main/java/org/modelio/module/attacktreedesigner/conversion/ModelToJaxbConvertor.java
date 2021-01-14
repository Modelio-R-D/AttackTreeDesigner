package org.modelio.module.attacktreedesigner.conversion;

import java.util.Iterator;
import java.util.List;
import org.modelio.metamodel.diagrams.AbstractDiagram;
import org.modelio.metamodel.uml.infrastructure.Dependency;
import org.modelio.metamodel.uml.infrastructure.Note;
import org.modelio.metamodel.uml.infrastructure.TagParameter;
import org.modelio.metamodel.uml.infrastructure.TaggedValue;
import org.modelio.metamodel.uml.statik.Class;
import org.modelio.module.attacktreedesigner.api.AttackTreeNoteTypes;
import org.modelio.module.attacktreedesigner.api.AttackTreeStereotypes;
import org.modelio.module.attacktreedesigner.api.AttackTreeTagTypes;
import org.modelio.module.attacktreedesigner.api.IAttackTreeDesignerPeerModule;
import org.modelio.module.attacktreedesigner.conversion.schema.AttackTreeType;
import org.modelio.module.attacktreedesigner.conversion.schema.AttackTreeXMLObjectFactory;
import org.modelio.module.attacktreedesigner.conversion.schema.AttackType;
import org.modelio.module.attacktreedesigner.conversion.schema.CounterMeasureType;
import org.modelio.module.attacktreedesigner.conversion.schema.CustomTagType;
import org.modelio.module.attacktreedesigner.conversion.schema.OperationType;
import org.modelio.module.attacktreedesigner.conversion.schema.OperatorType;
import org.modelio.module.attacktreedesigner.conversion.schema.TagType;
import org.modelio.module.attacktreedesigner.conversion.schema.TreeDiagramType;
import org.modelio.module.attacktreedesigner.conversion.schema.TreeReferenceType;
import org.modelio.module.attacktreedesigner.utils.FileSystemManager;
import org.modelio.module.attacktreedesigner.utils.elementmanager.ElementNavigationManager;
import org.modelio.module.attacktreedesigner.utils.elementmanager.ElementReferencing;
import org.modelio.module.attacktreedesigner.utils.elementmanager.tags.TagsManager;
import org.modelio.vcore.smkernel.mapi.MObject;

public class ModelToJaxbConvertor {
    private static final String UNDEFINED_LABEL = "Undefined";

    private static AttackTreeXMLObjectFactory objectFactory = new AttackTreeXMLObjectFactory();

    private Class modelTree;

    public Class getModelTree() {
        return this.modelTree;
    }

    public void setModelTree(Class modelTree) {
        this.modelTree = modelTree;
    }

    public ModelToJaxbConvertor(Class modelTree) {
        this.modelTree = modelTree;
    }

    public AttackTreeType convertModelToJaxb() {
        /*
         * Create Tree
         */ 
        AttackTreeType tree = objectFactory.createAttackTreeType();
        
        
        /*
         * Set Diagram
         */
        for(MObject child : this.modelTree.getCompositionChildren()) {
            if(child instanceof AbstractDiagram) {
                TreeDiagramType diagram = objectFactory.createTreeDiagramType();
                diagram.setName(child.getName());
                tree.setTreeDiagram(diagram);
                break;
            }
        }
        
        
        /*
         * Set Root Node
         */
        AttackType root = convertAttack(this.modelTree);
        tree.setAttack(root);
        
        // Add children nodes 
        addChildrenNodes(this.modelTree, root);
        return tree;
    }

    private static AttackType convertAttack(Class modelNode) {
        AttackType attack = objectFactory.createAttackType();
        attack.setName(modelNode.getName());
        
        /*
         * Attack Tags
         */
        TagType severityTag = objectFactory.createTagType();
        severityTag.setName(AttackTreeTagTypes.SEVERITY);
        severityTag.setValue(TagsManager.getElementTagParameter(modelNode, AttackTreeStereotypes.ATTACK, AttackTreeTagTypes.SEVERITY));
        attack.getTag().add(severityTag);
        
        TagType probabilityTag = objectFactory.createTagType();
        probabilityTag.setName(AttackTreeTagTypes.PROBABILITY);
        probabilityTag.setValue(TagsManager.getElementTagParameter(modelNode, AttackTreeStereotypes.ATTACK, AttackTreeTagTypes.PROBABILITY));
        attack.getTag().add(probabilityTag);
        
        TagType securityRelatedTag = objectFactory.createTagType();
        securityRelatedTag.setName(AttackTreeTagTypes.SECURITY_RELATED);
        securityRelatedTag.setValue(TagsManager.getElementTagParameter(modelNode, AttackTreeStereotypes.ATTACK, AttackTreeTagTypes.SECURITY_RELATED));
        attack.getTag().add(securityRelatedTag);
        
        TagType safetyRelatedTag = objectFactory.createTagType();
        safetyRelatedTag.setName(AttackTreeTagTypes.SAFETY_RELATED);
        safetyRelatedTag.setValue(TagsManager.getElementTagParameter(modelNode, AttackTreeStereotypes.ATTACK, AttackTreeTagTypes.SAFETY_RELATED));
        attack.getTag().add(safetyRelatedTag);
        
        TagType outOfScopeTag = objectFactory.createTagType();
        outOfScopeTag.setName(AttackTreeTagTypes.OUT_OF_SCOPE);
        outOfScopeTag.setValue(TagsManager.getElementTagParameter(modelNode, AttackTreeStereotypes.ATTACK, AttackTreeTagTypes.OUT_OF_SCOPE));
        attack.getTag().add(outOfScopeTag);
        
        
        TagType counteredAttack = objectFactory.createTagType();
        counteredAttack.setName(AttackTreeTagTypes.COUNTERED_ATTACK);
        counteredAttack.setValue(TagsManager.getElementTagParameter(modelNode, AttackTreeStereotypes.ATTACK, AttackTreeTagTypes.COUNTERED_ATTACK));
        attack.getTag().add(counteredAttack);
        
        /*
         * Attack Custom Tags
         */
        List<TaggedValue> attackTags = modelNode.getTag();
        for(TaggedValue attackTag:attackTags) {
            String tagDefinitionName = attackTag.getDefinition().getName();
            if(tagDefinitionName.equals(AttackTreeTagTypes.CUSTOM_TAG)) {
                List<TagParameter> tagParameters = attackTag.getActual();
                if(tagParameters.size() >= 2) {
                    
                    // Add a custom tag
                    CustomTagType customTag = objectFactory.createCustomTagType();
                    customTag.setName(tagParameters.get(0).getValue());
                    customTag.setValue(tagParameters.get(1).getValue());
                    attack.getCustomTag().add(customTag);
                }
            }
        }
        
        /*
         * Attack Counter Measures
         */
        List<Note> nodeNotes = modelNode.getDescriptor();
        for(Note note:nodeNotes) {
            if(note.getModel().getName().equals(AttackTreeNoteTypes.COUNTER_MEASURE)) {
                CounterMeasureType counterMeasure = objectFactory.createCounterMeasureType();
                counterMeasure.setContent(note.getContent());
                attack.getCounterMeasure().add(counterMeasure);
            }
        }
        return attack;
    }

    private void addChildrenNodes(Class parentNode, Object parentJaxbObject) {
        if (parentNode.isStereotyped(IAttackTreeDesignerPeerModule.MODULE_NAME, AttackTreeStereotypes.ATTACK)) {
            for(Dependency dependency : parentNode.getDependsOnDependency()) {
                if (dependency.isStereotyped(IAttackTreeDesignerPeerModule.MODULE_NAME, AttackTreeStereotypes.CONNECTION)) {
                    OperatorType operator = objectFactory.createOperatorType();
        
                    Class operatorNode = (Class) dependency.getDependsOn();
                    if(operatorNode.isStereotyped(IAttackTreeDesignerPeerModule.MODULE_NAME, AttackTreeStereotypes.OR)) {
                        operator.setType(OperationType.OR);
                    } else if (operatorNode.isStereotyped(IAttackTreeDesignerPeerModule.MODULE_NAME, AttackTreeStereotypes.AND)) {
                        operator.setType(OperationType.AND);
                    }                
                    ((AttackType) parentJaxbObject).setOperator(operator);
                    addChildrenNodes(operatorNode, operator);
                }
            }           
        } else if (parentNode.isStereotyped(IAttackTreeDesignerPeerModule.MODULE_NAME, AttackTreeStereotypes.OPERATOR)) {
            for(Dependency dependency : parentNode.getDependsOnDependency()) {
                if (dependency.isStereotyped(IAttackTreeDesignerPeerModule.MODULE_NAME, AttackTreeStereotypes.CONNECTION)) {
                    Class childNode = (Class) dependency.getDependsOn();
                    if (childNode.isStereotyped(IAttackTreeDesignerPeerModule.MODULE_NAME, AttackTreeStereotypes.ATTACK)) {
        
                        AttackType attack = convertAttack(childNode);
                        ((OperatorType) parentJaxbObject).getAttackOrTreeReferenceOrOperator().add(attack);
                        addChildrenNodes(childNode, attack);
        
                    } else if (childNode.isStereotyped(IAttackTreeDesignerPeerModule.MODULE_NAME, AttackTreeStereotypes.OR)) {
        
                        OperatorType operator = objectFactory.createOperatorType();
                        operator.setType(OperationType.OR);
                        ((OperatorType) parentJaxbObject).getAttackOrTreeReferenceOrOperator().add(operator);
                        addChildrenNodes(childNode, operator);
        
                    } else if (childNode.isStereotyped(IAttackTreeDesignerPeerModule.MODULE_NAME, AttackTreeStereotypes.AND)) {
        
                        OperatorType operator = objectFactory.createOperatorType();
                        operator.setType(OperationType.AND);
                        ((OperatorType) parentJaxbObject).getAttackOrTreeReferenceOrOperator().add(operator);
                        addChildrenNodes(childNode, operator);
        
                    } else if (childNode.isStereotyped(IAttackTreeDesignerPeerModule.MODULE_NAME, AttackTreeStereotypes.TREE_REFERENCE)) {
        
                        TreeReferenceType treeReference = objectFactory.createTreeReferenceType();
        
        
                        Class referencedTree = ElementReferencing.getReferencedTree(childNode);
                        if(referencedTree != null) {
        
                            List<MObject> referencedTreeListPath = ElementNavigationManager.getElementListPath(referencedTree);
                            Iterator<MObject> referencedTreeListPathIterator = referencedTreeListPath.iterator();
        
                            List<MObject> currentTreeListPath = ElementNavigationManager.getElementListPath(this.modelTree);
                            Iterator<MObject> currentTreeListPathIterator = currentTreeListPath.iterator();
        
                            String referencedTreePath = "";
        
                            MObject refrerencedTreePathCurrentElement;
                            while (referencedTreeListPathIterator.hasNext() && currentTreeListPathIterator.hasNext()) {
                                refrerencedTreePathCurrentElement = referencedTreeListPathIterator.next();
                                if(!currentTreeListPathIterator.next().equals(refrerencedTreePathCurrentElement)) {
                                    referencedTreePath = refrerencedTreePathCurrentElement.getName();
                                    break;                                
                                }
                            }
        
        
                            while(currentTreeListPathIterator.hasNext()) {
                                currentTreeListPathIterator.next();
                                referencedTreePath = FileSystemManager.PATH_PREDECESSOR + FileSystemManager.PATH_SEPARATOR + referencedTreePath;
                            
                            }
                            
                            while(referencedTreeListPathIterator.hasNext())
                                referencedTreePath = referencedTreePath + FileSystemManager.PATH_SEPARATOR + referencedTreeListPathIterator.next().getName();
        
                            treeReference.setRef(referencedTreePath + FileSystemManager.XML_FILE_EXTENSION);
        
                        } else {
                            treeReference.setRef(UNDEFINED_LABEL);
                        }
                        ((OperatorType) parentJaxbObject).getAttackOrTreeReferenceOrOperator().add(treeReference);
        
                    }
                }
            } 
        
        
        }
    }

}
