package org.modelio.module.attacktreedesigner.conversion;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.eclipse.draw2d.geometry.Rectangle;
import org.modelio.api.modelio.diagram.IDiagramGraphic;
import org.modelio.api.modelio.diagram.IDiagramHandle;
import org.modelio.api.modelio.diagram.IDiagramNode;
import org.modelio.api.modelio.diagram.IDiagramService;
import org.modelio.api.modelio.diagram.dg.IDiagramDG;
import org.modelio.api.modelio.diagram.style.IStyleHandle;
import org.modelio.api.modelio.model.IModelingSession;
import org.modelio.api.modelio.model.ITransaction;
import org.modelio.api.modelio.model.IUmlModel;
import org.modelio.api.module.IModule;
import org.modelio.api.module.context.IModuleContext;
import org.modelio.metamodel.diagrams.ClassDiagram;
import org.modelio.metamodel.uml.infrastructure.Dependency;
import org.modelio.metamodel.uml.infrastructure.ModelElement;
import org.modelio.metamodel.uml.infrastructure.Note;
import org.modelio.metamodel.uml.infrastructure.Stereotype;
import org.modelio.metamodel.uml.infrastructure.TaggedValue;
import org.modelio.metamodel.uml.statik.Attribute;
import org.modelio.metamodel.uml.statik.Class;
import org.modelio.metamodel.uml.statik.Package;
import org.modelio.module.attacktreedesigner.api.AttackTreeNoteTypes;
import org.modelio.module.attacktreedesigner.api.AttackTreeStereotypes;
import org.modelio.module.attacktreedesigner.api.AttackTreeTagTypes;
import org.modelio.module.attacktreedesigner.api.IAttackTreeDesignerPeerModule;
import org.modelio.module.attacktreedesigner.conversion.schema.AttackTreeType;
import org.modelio.module.attacktreedesigner.conversion.schema.AttackType;
import org.modelio.module.attacktreedesigner.conversion.schema.CounterMeasureType;
import org.modelio.module.attacktreedesigner.conversion.schema.OperationType;
import org.modelio.module.attacktreedesigner.conversion.schema.OperatorType;
import org.modelio.module.attacktreedesigner.conversion.schema.TagType;
import org.modelio.module.attacktreedesigner.conversion.schema.TreeReferenceType;
import org.modelio.module.attacktreedesigner.i18n.Messages;
import org.modelio.module.attacktreedesigner.impl.AttackTreeDesignerModule;
import org.modelio.module.attacktreedesigner.impl.AttackTreeModelChangeHandler;
import org.modelio.module.attacktreedesigner.utils.FileSystemManager;
import org.modelio.module.attacktreedesigner.utils.IAttackTreeCustomizerPredefinedField;
import org.modelio.module.attacktreedesigner.utils.elementmanager.CounterMeasureManager;
import org.modelio.module.attacktreedesigner.utils.elementmanager.ElementCreationManager;
import org.modelio.module.attacktreedesigner.utils.elementmanager.ElementReferencing;
import org.modelio.module.attacktreedesigner.utils.elementmanager.representation.AutoLayoutManager;
import org.modelio.module.attacktreedesigner.utils.elementmanager.representation.DiagramElementBounds;
import org.modelio.module.attacktreedesigner.utils.elementmanager.representation.ElementRepresentationManager;
import org.modelio.module.attacktreedesigner.utils.elementmanager.tags.TagsManager;
import org.modelio.vcore.smkernel.mapi.MClass;
import org.modelio.vcore.smkernel.mapi.MObject;

public class JaxbToModelConvertor {
    private static Map<Class, String> postponedReferences = new HashMap<>();

    private AttackTreeType jaxbTree;

    public JaxbToModelConvertor(AttackTreeType jaxbTree) {
        this.setJaxbTree(jaxbTree);
    }

    public AttackTreeType getJaxbTree() {
        return this.jaxbTree;
    }

    public void setJaxbTree(AttackTreeType jaxbTree) {
        this.jaxbTree = jaxbTree;
    }

    public static void reInitializeListOfPostponedReferences() {
        if(!postponedReferences.isEmpty()) {
            postponedReferences  = new HashMap<>();
        }
    }

    public static void updatePostponedReferences() {
        postponedReferences.forEach((reference,path)->{
            
            
            Class referencedTree = getTreeByRelativePathName(reference, path);
            if(referencedTree != null) {
                Attribute attribute = ElementReferencing.getRefAttribute(reference);
                if(attribute != null) {
                    IModelingSession session = AttackTreeDesignerModule.getInstance().getModuleContext().getModelingSession ();
                    try( ITransaction transaction = session.createTransaction(Messages.getString ("Info.Session.UpdateModel"))){
                        attribute.setType(referencedTree);
        
                        /*
                         * Update color
                         */
                        if(CounterMeasureManager.isCountered(referencedTree, true)) {
                            ElementRepresentationManager.setClassColor(reference, ElementRepresentationManager.COUNTERED_ATTACK_COLOR);
        
        
                        } else {
                            ElementRepresentationManager.setClassColor(reference, ElementRepresentationManager.DEFAULT_ATTACK_COLOR);
                        }
                        
                        /*
                         * Propagate to ascendants of reference
                         */
                        for(Dependency parentDependency : reference.getImpactedDependency()) {
                            if(parentDependency.isStereotyped(IAttackTreeDesignerPeerModule.MODULE_NAME, AttackTreeStereotypes.CONNECTION)) {
                                Class elementParent = (Class) parentDependency.getImpacted();                
                                AttackTreeModelChangeHandler.updateAndPropagateAttackTags(elementParent, false, false, true);                         
                            }
                        }
                        
                        transaction.commit ();
                    }
                }
            }
            
        });
        reInitializeListOfPostponedReferences();
    }

    public void createTreeModel(IModule module, Package destinationPackage) {
        IModuleContext moduleContext = module.getModuleContext();
        IModelingSession modellingSession = moduleContext.getModelingSession();
        
        try( ITransaction transaction = modellingSession.createTransaction(Messages.getString ("Info.Session.Create", Messages.getString ("Ui.Command.AttackTreeDiagramExplorerCommand.Label")))){
        
            IUmlModel projectModel = modellingSession.getModel();
        
            // create Root Class and Default tags
            AttackType jaxbAttack = this.jaxbTree.getAttack();
            Class rootElement = projectModel.createClass(jaxbAttack.getName(), destinationPackage, IAttackTreeDesignerPeerModule.MODULE_NAME, AttackTreeStereotypes.ROOT);
        
            // Create Attack Tree Diagram
            MClass mclass = moduleContext.getModelioServices().getMetamodelService().getMetamodel().getMClass(ClassDiagram.class);
            Stereotype stereotype = modellingSession.getMetamodelExtensions().getStereotype(IAttackTreeDesignerPeerModule.MODULE_NAME, AttackTreeStereotypes.ATTACK_TREE_DIAGRAM, mclass);
            ClassDiagram diagram = modellingSession.getModel().createClassDiagram(this.jaxbTree.getTreeDiagram().getName(), rootElement, stereotype);
        
        
            // Unmask Attack Tree in diagram
            IDiagramService diagramService = moduleContext.getModelioServices().getDiagramService();
            try(  IDiagramHandle diagramHandle = diagramService.getDiagramHandle(diagram);){
        
                IDiagramDG diagramNode = diagramHandle.getDiagramNode();
        
                for (IStyleHandle style : diagramService.listStyles()){
                    if (style.getName().equals(IAttackTreeCustomizerPredefinedField.ATTACKTREE_STYLE_NAME)){
                        diagramNode.setStyle(style);
                        break;
                    }
                }
        
                List<IDiagramGraphic> diagramGraphics = diagramHandle.unmask(rootElement, 0, 0);
                for (IDiagramGraphic diagramGraphic : diagramGraphics) {
                    if(diagramGraphic.getElement().equals(rootElement)){
                        ((IDiagramNode) diagramGraphic).setBounds(DiagramElementBounds.ROOT.createRectangleBounds());
                    }
                }
        
                // update tags of Root
                updateAttackTags(modellingSession, diagramHandle, jaxbAttack, rootElement);
        
                // Create and unmask counter measures of Root
                updateAttackCounterMeasures(modellingSession, diagramHandle, jaxbAttack, rootElement);
        
                // Create Root children
                createNodeChildren (modellingSession, projectModel, diagramHandle, rootElement, rootElement, this.jaxbTree.getAttack());
        
                // autolayout tree
                AutoLayoutManager.autolayoutTree(rootElement, diagramHandle);
        
                diagramHandle.save();
                diagramHandle.close();
            }
        
        
            transaction.commit ();
        }
    }

    private void updateAttackCounterMeasures(IModelingSession modellingSession, IDiagramHandle diagramHandle, AttackType jaxbAttack, Class attackNode) {
        IUmlModel model = modellingSession.getModel();                
        for(CounterMeasureType jaxbCounterMeasure : jaxbAttack.getCounterMeasure()) {
        
            Note note = model.createNote(IAttackTreeDesignerPeerModule.MODULE_NAME,AttackTreeNoteTypes.COUNTER_MEASURE, attackNode, jaxbCounterMeasure.getContent());   
            List<IDiagramGraphic> nodeGraphics = diagramHandle.unmask(note, 0, 0);
        
            // update note bounds
            if(! nodeGraphics.isEmpty()) {
                IDiagramNode diagramNode = (IDiagramNode) nodeGraphics.get(0);
                Rectangle nodeBounds = diagramNode.getBounds();
                nodeBounds.setHeight(DiagramElementBounds.COUNTER_MEASURE.getHeight());
                diagramNode.setBounds(nodeBounds);
            }
            
            // update countered Attack color [Deprecated, treated in updateAttackTags]
            //ElementRepresentationManager.setClassColor(attackNode, diagramHandle, ElementRepresentationManager.COUNTERED_ATTACK_COLOR);
        
            // update countered Attack "Countered attack" tag
            TagsManager.setElementTagValue(attackNode, AttackTreeStereotypes.ATTACK, AttackTreeTagTypes.COUNTERED_ATTACK, "true");
        }
    }

    private void updateAttackTags(IModelingSession modellingSession, IDiagramHandle diagramHandle, AttackType jaxbAttack, Class attackNode) {
        for(TagType jaxbTag: jaxbAttack.getTag()) {
            String tagType = jaxbTag.getName();
            TaggedValue tag = modellingSession.getModel().createTaggedValue(IAttackTreeDesignerPeerModule.MODULE_NAME, tagType, attackNode);
            if(tagType.equals(AttackTreeTagTypes.COUNTERED_ATTACK) && jaxbTag.getValue().equals("true")) {
                ElementRepresentationManager.setClassColor(attackNode, diagramHandle, ElementRepresentationManager.COUNTERED_ATTACK_COLOR);
            }
            TagsManager.createTagParameter(modellingSession, tag, jaxbTag.getValue());
        }
        
        /*
         * [Deprecated]Create Countered Attack Default Tag
         */
        //        TaggedValue counteredAttackTag = modellingSession.getModel().createTaggedValue(IAttackTreeDesignerPeerModule.MODULE_NAME, 
        //                AttackTreeTagTypes.COUNTERED_ATTACK, attackNode);
        //        TagsManager.createTagParameter(modellingSession, counteredAttackTag, TagsManager.DEFAULT_COUNTERED_ATTACK);
    }

    private void createNodeChildren(IModelingSession modellingSession, IUmlModel projectModel, IDiagramHandle diagramHandle, Class node, Class rootElement, Object jaxbElement) {
        if(jaxbElement instanceof AttackType) {
            AttackType jaxbAttack = (AttackType) jaxbElement;
            OperatorType jaxbOperatorChild = jaxbAttack.getOperator();
            if(jaxbOperatorChild != null) {
                createChildOperatorNode(modellingSession, projectModel, diagramHandle, node, rootElement, jaxbOperatorChild);
            }
        
        } else if (jaxbElement instanceof OperatorType) {
        
            OperatorType jaxbOperator = (OperatorType) jaxbElement;
            for(Object jaxbChildElement : jaxbOperator.getAttackOrTreeReferenceOrOperator()) {
                if(jaxbChildElement instanceof OperatorType) {
                    createChildOperatorNode(modellingSession, projectModel, diagramHandle, node, rootElement, (OperatorType) jaxbChildElement);
                } else if (jaxbChildElement instanceof AttackType) {
                    createChildAttackNode(modellingSession, projectModel, diagramHandle, node, rootElement, (AttackType) jaxbChildElement);
                } else if (jaxbChildElement instanceof TreeReferenceType) {
                    createChildTreeReferenceNode(modellingSession, projectModel, diagramHandle, node, rootElement, (TreeReferenceType) jaxbChildElement);
                }
            }
        
        }
    }

    private void createChildTreeReferenceNode(IModelingSession modellingSession, IUmlModel projectModel, IDiagramHandle diagramHandle, Class node, Class rootElement, TreeReferenceType jaxbTreeReference) {
        Class treeReferenceChildNode = ElementCreationManager.createAndUnmaskTreeReferenceElement(diagramHandle, 
                DiagramElementBounds.ROOT.createRectangleBounds(), 
                modellingSession, node, rootElement.getCompositionOwner());
        
        postponedReferences.put(treeReferenceChildNode, jaxbTreeReference.getRef());
        
        projectModel.getDefaultNameService().setDefaultName(treeReferenceChildNode, AttackTreeStereotypes.TREE_REFERENCE); 
        
        Dependency dependency = modellingSession.getModel().createDependency(node, treeReferenceChildNode, IAttackTreeDesignerPeerModule.MODULE_NAME, AttackTreeStereotypes.CONNECTION); 
        diagramHandle.unmask(dependency, 0, 0);
    }

    private void createChildAttackNode(IModelingSession modellingSession, IUmlModel projectModel, IDiagramHandle diagramHandle, Class node, Class rootElement, AttackType jaxbAttackChild) {
        Class attackChildNode = ElementCreationManager.createAndUnmaskAttackElement(diagramHandle, 
                DiagramElementBounds.ROOT.createRectangleBounds(), 
                modellingSession, AttackTreeStereotypes.ATTACK, node, rootElement.getCompositionOwner());
        
        attackChildNode.setName(jaxbAttackChild.getName());
        updateAttackTags(modellingSession, diagramHandle, jaxbAttackChild, attackChildNode);
        updateAttackCounterMeasures(modellingSession, diagramHandle, jaxbAttackChild, attackChildNode);
        
        Dependency dependency = modellingSession.getModel().createDependency(node, attackChildNode, IAttackTreeDesignerPeerModule.MODULE_NAME, AttackTreeStereotypes.CONNECTION); 
        diagramHandle.unmask(dependency, 0, 0);
        
        createNodeChildren(modellingSession, projectModel, diagramHandle, attackChildNode, rootElement, jaxbAttackChild);
    }

    private void createChildOperatorNode(IModelingSession modellingSession, IUmlModel projectModel, IDiagramHandle diagramHandle, Class node, Class rootElement, OperatorType jaxbOperatorChild) {
        Class operatorNode;
        if(jaxbOperatorChild.getType().equals(OperationType.OR)) {
            operatorNode = ElementCreationManager.createAndUnmaskOperatorElement(diagramHandle, 
                    DiagramElementBounds.ROOT.createRectangleBounds(), 
                    modellingSession, AttackTreeStereotypes.OR, node, rootElement.getCompositionOwner());
            modellingSession.getModel().getDefaultNameService().setDefaultName(operatorNode, AttackTreeStereotypes.OR); 
        } else  {
            operatorNode = ElementCreationManager.createAndUnmaskOperatorElement(diagramHandle, 
                    DiagramElementBounds.ROOT.createRectangleBounds(), 
                    modellingSession, AttackTreeStereotypes.AND, node, rootElement.getCompositionOwner());
            modellingSession.getModel().getDefaultNameService().setDefaultName(operatorNode, AttackTreeStereotypes.AND); 
        }
        
        Dependency dependency = modellingSession.getModel().createDependency(node, operatorNode, IAttackTreeDesignerPeerModule.MODULE_NAME, AttackTreeStereotypes.CONNECTION); 
        diagramHandle.unmask(dependency, 0, 0);
        
        createNodeChildren(modellingSession, projectModel, diagramHandle, operatorNode, rootElement, jaxbOperatorChild);
    }

    private static Class getTreeByRelativePathName(Class reference, String referencedTreeRelativePath) {
        if(reference.isStereotyped(IAttackTreeDesignerPeerModule.MODULE_NAME, AttackTreeStereotypes.TREE_REFERENCE)) {
            ModelElement currentElement = reference;
            while (currentElement!=null && !currentElement.isStereotyped(IAttackTreeDesignerPeerModule.MODULE_NAME, AttackTreeStereotypes.ROOT )) {
                currentElement = (ModelElement) currentElement.getCompositionOwner();
            }
        
            String currentPath = referencedTreeRelativePath;
            int i = currentPath.indexOf(FileSystemManager.PATH_PREDECESSOR + FileSystemManager.PATH_SEPARATOR);
        
            if(currentElement != null) {
                currentElement = (ModelElement) currentElement.getCompositionOwner();
            }
        
            while(i ==0 && currentElement != null) {
                currentPath = currentPath.substring(3);
                currentElement = (ModelElement) currentElement.getCompositionOwner();
        
                i = currentPath.indexOf(FileSystemManager.PATH_PREDECESSOR + FileSystemManager.PATH_SEPARATOR);
            }
        
            currentPath = currentPath.substring(0, currentPath.lastIndexOf(FileSystemManager.XML_FILE_EXTENSION));
            i = currentPath.indexOf(FileSystemManager.PATH_SEPARATOR);
            while(i>=0 && currentElement != null) {
                boolean elementFound = false;
                String elementName = currentPath.substring(0,i);
                for(MObject child:currentElement.getCompositionChildren()) {
                    if(child.getName().equals(elementName)) {
                        elementFound = true;
                        currentElement = (ModelElement) child;
                        currentPath = currentPath.substring(i+1);
                        i = currentPath.indexOf(FileSystemManager.PATH_SEPARATOR);
                        break;
                    }
                }
        
                if(!elementFound) {
                    return null;
                }
            }
        
            //            if(currentElement != null 
            //                    && currentElement instanceof Class
            //                    && currentElement.isStereotyped(IAttackTreeDesignerPeerModule.MODULE_NAME, AttackTreeStereotypes.ROOT)) {
            //                
            //
            //                return (Class) currentElement;
            //            }
        
            if(currentElement != null ) {
                for(MObject child:currentElement.getCompositionChildren()) {
                    if(child instanceof Class 
                            && child.getName().equals(currentPath)
                            && ((Class) child).isStereotyped(IAttackTreeDesignerPeerModule.MODULE_NAME, AttackTreeStereotypes.ROOT)) {
        
                        return (Class) child;
                    }
                }
            }
        
        }
        return null;
    }

}
