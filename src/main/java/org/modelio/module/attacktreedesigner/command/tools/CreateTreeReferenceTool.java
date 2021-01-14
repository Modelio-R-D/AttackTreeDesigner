package org.modelio.module.attacktreedesigner.command.tools;

import org.eclipse.draw2d.geometry.Rectangle;
import org.modelio.api.modelio.diagram.IDiagramGraphic;
import org.modelio.api.modelio.diagram.IDiagramHandle;
import org.modelio.api.modelio.diagram.tools.DefaultBoxTool;
import org.modelio.api.modelio.model.IModelingSession;
import org.modelio.api.modelio.model.ITransaction;
import org.modelio.api.modelio.model.IUmlModel;
import org.modelio.metamodel.diagrams.AbstractDiagram;
import org.modelio.metamodel.uml.infrastructure.ModelTree;
import org.modelio.metamodel.uml.statik.Attribute;
import org.modelio.metamodel.uml.statik.Class;
import org.modelio.metamodel.uml.statik.NameSpace;
import org.modelio.module.attacktreedesigner.api.AttackTreeStereotypes;
import org.modelio.module.attacktreedesigner.api.IAttackTreeDesignerPeerModule;
import org.modelio.module.attacktreedesigner.i18n.Messages;
import org.modelio.module.attacktreedesigner.impl.AttackTreeDesignerModule;
import org.modelio.module.attacktreedesigner.utils.elementmanager.ElementReferencing;
import org.modelio.module.attacktreedesigner.utils.elementmanager.Labels;
import org.modelio.vcore.smkernel.mapi.MObject;

public class CreateTreeReferenceTool extends DefaultBoxTool {
    @Override
    public boolean acceptElement(final IDiagramHandle diagramHandle, final IDiagramGraphic targetNode) {
        MObject targetNodeElement = targetNode.getElement();
        return (targetNodeElement instanceof AbstractDiagram) && 
                ((Class)((AbstractDiagram) targetNodeElement).getOrigin())
                .isStereotyped(IAttackTreeDesignerPeerModule.MODULE_NAME, AttackTreeStereotypes.ROOT);
    }

    @Override
    public void actionPerformed(final IDiagramHandle diagramHandle, final IDiagramGraphic targetDiagramGraphic, final Rectangle rect) {
        createReference(diagramHandle, targetDiagramGraphic, rect);
    }

    public static Class createReference(final IDiagramHandle diagramHandle, final IDiagramGraphic targetDiagramGraphic, final Rectangle rect) {
        Class referenceTreeElement = null;
        
        IModelingSession session = AttackTreeDesignerModule.getInstance().getModuleContext().getModelingSession();
        try( ITransaction transaction = session.createTransaction (Messages.getString ("Info.Session.Create", AttackTreeStereotypes.TREE_REFERENCE))){
        
            IUmlModel model = session.getModel();
            
            MObject rootElement = diagramHandle.getDiagram().getOrigin().getCompositionOwner();
        
            // create stereotyped Tree Reference Class
            referenceTreeElement = model.createClass(
                    Labels.DEFAULT_NAME.toString(), 
                    (NameSpace) rootElement, 
                    IAttackTreeDesignerPeerModule.MODULE_NAME, 
                    AttackTreeStereotypes.TREE_REFERENCE);
            
            // Create new Attribute Reference Tree
            Attribute referenceTreeAttribute = model.createAttribute(
                    ElementReferencing.REF_DEFAULT_NAME, model.getUmlTypes().getUNDEFINED(), referenceTreeElement, IAttackTreeDesignerPeerModule.MODULE_NAME, 
                    AttackTreeStereotypes.TREE_REFERENCE_ATTRIBUTE); 
            
        
            
            // Update style to referencing tree attack style
            //ElementRepresentationManager.changeStyleToReferencedTree(referenceTreeElement, AttackTreeDesignerModule.getInstance().getModuleContext(), referenceTreeAttribute);
            
            
            // unmask Attack and save diagram
            diagramHandle.unmask(referenceTreeElement, rect.x, rect.y);            
            diagramHandle.unmask(referenceTreeAttribute, 0, 0);            
            
            diagramHandle.save();
            diagramHandle.close();
            
            referenceTreeElement.setOwner((ModelTree) targetDiagramGraphic.getElement().getCompositionOwner());
            
            model.getDefaultNameService()
            .setDefaultName(referenceTreeElement, AttackTreeStereotypes.TREE_REFERENCE); 
        
            transaction.commit ();
        }
        return referenceTreeElement;
    }

}
