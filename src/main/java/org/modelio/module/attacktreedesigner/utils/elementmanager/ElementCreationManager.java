package org.modelio.module.attacktreedesigner.utils.elementmanager;

import java.util.List;
import org.eclipse.draw2d.geometry.Rectangle;
import org.modelio.api.modelio.diagram.IDiagramGraphic;
import org.modelio.api.modelio.diagram.IDiagramHandle;
import org.modelio.api.modelio.diagram.IDiagramNode;
import org.modelio.api.modelio.model.IModelingSession;
import org.modelio.api.modelio.model.ITransaction;
import org.modelio.metamodel.uml.infrastructure.ModelElement;
import org.modelio.metamodel.uml.infrastructure.ModelTree;
import org.modelio.metamodel.uml.statik.Attribute;
import org.modelio.metamodel.uml.statik.Class;
import org.modelio.metamodel.uml.statik.NameSpace;
import org.modelio.module.attacktreedesigner.api.AttackTreeStereotypes;
import org.modelio.module.attacktreedesigner.api.IAttackTreeDesignerPeerModule;
import org.modelio.module.attacktreedesigner.i18n.Messages;
import org.modelio.module.attacktreedesigner.impl.AttackTreeDesignerModule;
import org.modelio.module.attacktreedesigner.utils.elementmanager.representation.DiagramElementStyle;
import org.modelio.vcore.smkernel.mapi.MObject;

public class ElementCreationManager {
    public static Class createOperatorElement(IDiagramHandle diagramHandle, List<IDiagramGraphic> otherNodes, Rectangle rectangle, String stereotypeName) {
        Class operatorElement = null;
        IModelingSession session = AttackTreeDesignerModule.getInstance().getModuleContext().getModelingSession();
        try( ITransaction transaction = session.createTransaction (Messages.getString ("Info.Session.Create", AttackTreeStereotypes.OPERATOR))){
        
            ModelTree parentElement = (ModelTree) otherNodes.get(0).getElement();
            MObject rootParent = diagramHandle.getDiagram().getOrigin().getCompositionOwner();       
        
            /*
             * Create and unmask OPERATOR Element
             */
            operatorElement = createAndUnmaskOperatorElement(diagramHandle, rectangle, session, stereotypeName, parentElement, rootParent);
        
        
            /*
             * Create Connections
             */
            ModelElement connectionFirstElement = session.getModel().createDependency(parentElement, 
                    operatorElement, 
                    IAttackTreeDesignerPeerModule.MODULE_NAME, 
                    AttackTreeStereotypes.CONNECTION); 
            diagramHandle.unmask(connectionFirstElement, 0, 0);
        
        
            int nodesSize = otherNodes.size();
            for (int i = 1; i < nodesSize; i++) {
                MObject element = otherNodes.get(i).getElement();
                ((ModelTree)element).setOwner(operatorElement);
                ModelElement connection = session.getModel().createDependency(operatorElement, 
                        (ModelElement) element, 
                        IAttackTreeDesignerPeerModule.MODULE_NAME, 
                        AttackTreeStereotypes.CONNECTION);
        
                ElementCreationManager.renameElement(session, (ModelTree) element); 
        
                diagramHandle.unmask(connection, 0, 0);
            }
        
        
            diagramHandle.save();
            diagramHandle.close();
        
            session.getModel().getDefaultNameService()
            .setDefaultName(operatorElement, stereotypeName);
            transaction.commit ();
        }
        return operatorElement;
    }

    public static void renameElement(IModelingSession session, ModelTree targetElement) {
        String targetElementName = targetElement.getName();
        targetElement.setName(Labels.DEFAULT_NAME.toString());
        session.getModel().getDefaultNameService().setDefaultName(targetElement, targetElementName);
    }

    public static Class createAndUnmaskOperatorElement(IDiagramHandle diagramHandle, Rectangle rectangle, IModelingSession session, String stereotypeName, ModelTree parentElement, MObject rootParent) {
        Class operatorElement = session.getModel().createClass(
                Labels.DEFAULT_NAME.toString(), 
                (NameSpace) rootParent, 
                IAttackTreeDesignerPeerModule.MODULE_NAME, 
                stereotypeName);
        List<IDiagramGraphic> graph = diagramHandle.unmask(operatorElement, rectangle.x, rectangle.y);
        if((graph != null) &&  (graph.size() > 0) && (graph.get(0) instanceof IDiagramNode)) {
            IDiagramNode graphNode = (IDiagramNode)graph.get(0);
            graphNode.setProperty(Labels.CLASS_SHOWNAME.name(), DiagramElementStyle.OPERATOR.getShowNameProperty());
            graphNode.setProperty(Labels.CLASS_REPRES_MODE.name(), DiagramElementStyle.OPERATOR.getRepresentationMode());
        }
        operatorElement.setOwner(parentElement);
        return operatorElement;
    }

    public static Class createAndUnmaskAttackElement(IDiagramHandle diagramHandle, Rectangle rectangle, IModelingSession session, String stereotypeName, ModelTree parentElement, MObject rootParent) {
        Class attackElement = session.getModel().createClass(
                Labels.DEFAULT_NAME.toString(), 
                (NameSpace) rootParent, 
                IAttackTreeDesignerPeerModule.MODULE_NAME, 
                stereotypeName);
        List<IDiagramGraphic> graph = diagramHandle.unmask(attackElement, rectangle.x, rectangle.y);
        if((graph != null) &&  (graph.size() > 0) && (graph.get(0) instanceof IDiagramNode)) {
            IDiagramNode graphNode = (IDiagramNode)graph.get(0);
            graphNode.setProperty(Labels.CLASS_SHOWNAME.name(), DiagramElementStyle.ATTACK.getShowNameProperty());
            graphNode.setProperty(Labels.CLASS_REPRES_MODE.name(), DiagramElementStyle.ATTACK.getRepresentationMode());
        }
        attackElement.setOwner(parentElement);
        return attackElement;
    }

    public static Class createAndUnmaskTreeReferenceElement(IDiagramHandle diagramHandle, Rectangle rectangle, IModelingSession session, ModelTree parentElement, MObject rootParent) {
        Class treeReferenceElement = session.getModel().createClass(
                Labels.DEFAULT_NAME.toString(), 
                (NameSpace) rootParent, 
                IAttackTreeDesignerPeerModule.MODULE_NAME, 
                AttackTreeStereotypes.TREE_REFERENCE);
        Attribute referenceTreeAttribute = session.getModel().createAttribute(
                ElementReferencing.REF_DEFAULT_NAME, session.getModel().getUmlTypes().getUNDEFINED(), treeReferenceElement, IAttackTreeDesignerPeerModule.MODULE_NAME, 
                AttackTreeStereotypes.TREE_REFERENCE_ATTRIBUTE); 
        List<IDiagramGraphic> graph = diagramHandle.unmask(treeReferenceElement, rectangle.x, rectangle.y);
        if((graph != null) &&  (graph.size() > 0) && (graph.get(0) instanceof IDiagramNode)) {
            IDiagramNode graphNode = (IDiagramNode)graph.get(0);
            graphNode.setProperty(Labels.CLASS_SHOWNAME.name(), DiagramElementStyle.TREE_REFERENCE.getShowNameProperty());
            graphNode.setProperty(Labels.CLASS_REPRES_MODE.name(), DiagramElementStyle.TREE_REFERENCE.getRepresentationMode());
        }
        diagramHandle.unmask(referenceTreeAttribute, 0, 0);            
        
        treeReferenceElement.setOwner(parentElement);
        return treeReferenceElement;
    }

}
