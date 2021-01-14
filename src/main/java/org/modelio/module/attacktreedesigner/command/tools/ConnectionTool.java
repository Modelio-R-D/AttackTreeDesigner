package org.modelio.module.attacktreedesigner.command.tools;

import org.modelio.api.modelio.diagram.IDiagramGraphic;
import org.modelio.api.modelio.diagram.IDiagramHandle;
import org.modelio.api.modelio.diagram.IDiagramLink.LinkRouterKind;
import org.modelio.api.modelio.diagram.ILinkPath;
import org.modelio.api.modelio.diagram.tools.DefaultLinkTool;
import org.modelio.api.modelio.model.IModelingSession;
import org.modelio.api.modelio.model.ITransaction;
import org.modelio.metamodel.uml.infrastructure.Dependency;
import org.modelio.metamodel.uml.infrastructure.ModelElement;
import org.modelio.metamodel.uml.infrastructure.ModelTree;
import org.modelio.metamodel.uml.statik.Class;
import org.modelio.module.attacktreedesigner.api.AttackTreeStereotypes;
import org.modelio.module.attacktreedesigner.api.IAttackTreeDesignerPeerModule;
import org.modelio.module.attacktreedesigner.i18n.Messages;
import org.modelio.module.attacktreedesigner.impl.AttackTreeDesignerModule;
import org.modelio.module.attacktreedesigner.utils.elementmanager.ElementCreationManager;
import org.modelio.module.attacktreedesigner.utils.elementmanager.ElementNavigationManager;
import org.modelio.vcore.smkernel.mapi.MObject;

public class ConnectionTool extends DefaultLinkTool {
    @Override
    public boolean acceptFirstElement(IDiagramHandle diagramHandle, IDiagramGraphic targetNode) {
        MObject firstElement = targetNode.getElement();
        if (firstElement instanceof Class 
                && ((Class) firstElement).isStereotyped(IAttackTreeDesignerPeerModule.MODULE_NAME, AttackTreeStereotypes.NODE)
                && ! ((Class) firstElement).isStereotyped(IAttackTreeDesignerPeerModule.MODULE_NAME, AttackTreeStereotypes.SUBTREE)
                && ! ((Class) firstElement).isStereotyped(IAttackTreeDesignerPeerModule.MODULE_NAME, AttackTreeStereotypes.TREE_REFERENCE)
                ){            
        
            // if it is an operator it is allowed to have many children
            if( ((Class) firstElement).isStereotyped(IAttackTreeDesignerPeerModule.MODULE_NAME, AttackTreeStereotypes.OPERATOR)){
                return true;
            }
            
            // It it is not an operator (only left is Attack), then it must have only one child
            for(Dependency dependency : (((ModelElement) firstElement).getDependsOnDependency())){
                if (dependency.isStereotyped(IAttackTreeDesignerPeerModule.MODULE_NAME, AttackTreeStereotypes.CONNECTION)) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    @Override
    public boolean acceptSecondElement(IDiagramHandle diagramHandle, IDiagramGraphic sourceNode, IDiagramGraphic targetNode) {
        MObject firstElement = sourceNode.getElement();
        MObject secondElement = targetNode.getElement();
        
        if (secondElement instanceof Class 
                && (((Class) secondElement).isStereotyped(IAttackTreeDesignerPeerModule.MODULE_NAME, AttackTreeStereotypes.NODE))
                && !(((Class) secondElement).isStereotyped(IAttackTreeDesignerPeerModule.MODULE_NAME, AttackTreeStereotypes.ROOT))
                && ! ElementNavigationManager.haveSameSecondLevelAncestor(sourceNode, targetNode)
                && (((Class) firstElement).isStereotyped(IAttackTreeDesignerPeerModule.MODULE_NAME, AttackTreeStereotypes.OPERATOR) 
                    || ((Class) secondElement).isStereotyped(IAttackTreeDesignerPeerModule.MODULE_NAME, AttackTreeStereotypes.OPERATOR))) {
        
            for(Dependency dependency : ((ModelElement) secondElement).getImpactedDependency()){
                if (dependency.isStereotyped(IAttackTreeDesignerPeerModule.MODULE_NAME, AttackTreeStereotypes.CONNECTION)) {
                    return false;
                }
            }
        
            return true;
        }
        return false;
    }

    @Override
    public void actionPerformed(IDiagramHandle diagramHandle, IDiagramGraphic originNode, IDiagramGraphic targetNode, LinkRouterKind touterType, ILinkPath path) {
        createConnection(diagramHandle, originNode, targetNode);
    }

    public static Dependency createConnection(IDiagramHandle diagramHandle, IDiagramGraphic originNode, IDiagramGraphic targetNode) {
        Dependency connectionElement = null;
        
        IModelingSession session = AttackTreeDesignerModule.getInstance().getModuleContext().getModelingSession();
        try( ITransaction transaction = session.createTransaction (Messages.getString ("Info.Session.Create", AttackTreeStereotypes.CONNECTION))){
        
        
            ModelTree firstElement = (ModelTree) originNode.getElement();
            ModelTree secondElement = (ModelTree) targetNode.getElement();
        
            secondElement.setOwner(firstElement);
        
            connectionElement = session.getModel().createDependency(firstElement, 
                    secondElement, 
                    IAttackTreeDesignerPeerModule.MODULE_NAME, 
                    AttackTreeStereotypes.CONNECTION); 
            diagramHandle.unmask(connectionElement, 0, 0);
        
        
            diagramHandle.save();
            diagramHandle.close();
        
        
            ElementCreationManager.renameElement(session, secondElement); 
        
            transaction.commit ();
        }
        return connectionElement;
    }

}
