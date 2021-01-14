package org.modelio.module.attacktreedesigner.command.explorer;

import java.util.List;
import org.eclipse.draw2d.geometry.Rectangle;
import org.modelio.api.modelio.diagram.IDiagramGraphic;
import org.modelio.api.modelio.diagram.IDiagramHandle;
import org.modelio.api.modelio.diagram.IDiagramNode;
import org.modelio.api.modelio.diagram.IDiagramService;
import org.modelio.api.modelio.model.IModelingSession;
import org.modelio.api.modelio.model.ITransaction;
import org.modelio.api.module.IModule;
import org.modelio.api.module.command.DefaultModuleCommandHandler;
import org.modelio.api.module.context.IModuleContext;
import org.modelio.metamodel.diagrams.AbstractDiagram;
import org.modelio.metamodel.uml.infrastructure.Dependency;
import org.modelio.metamodel.uml.infrastructure.Element;
import org.modelio.metamodel.uml.infrastructure.ModelElement;
import org.modelio.metamodel.uml.infrastructure.ModelTree;
import org.modelio.metamodel.uml.statik.Class;
import org.modelio.module.attacktreedesigner.api.AttackTreeStereotypes;
import org.modelio.module.attacktreedesigner.api.IAttackTreeDesignerPeerModule;
import org.modelio.module.attacktreedesigner.i18n.Messages;
import org.modelio.module.attacktreedesigner.impl.AttackTreeDesignerModule;
import org.modelio.module.attacktreedesigner.utils.elementmanager.representation.AutoLayoutManager;
import org.modelio.module.attacktreedesigner.utils.elementmanager.representation.ElementRepresentationManager;
import org.modelio.vcore.smkernel.mapi.MObject;

public class UnmaskSubTreeCommand extends DefaultModuleCommandHandler {
    @Override
    public void actionPerformed(final List<MObject> selectedElements, final IModule module) {
        IModuleContext moduleContext = AttackTreeDesignerModule.getInstance().getModuleContext();
        IModelingSession session = moduleContext.getModelingSession();
        MObject selectedElement = selectedElements.get(0);  
        
        try( ITransaction transaction = session.createTransaction(Messages.getString ("Info.Session.UpdateModel"))){
        
            // Remove SubTree stereotype from "selectedElement" 
            ((Class) selectedElement).removeStereotypes(IAttackTreeDesignerPeerModule.MODULE_NAME, AttackTreeStereotypes.SUBTREE);
        
        
            /*
             * Unmask Children
             */
            IDiagramService diagramService = moduleContext.getModelioServices().getDiagramService();            
        
            List<AbstractDiagram> diagrams = ((Element) selectedElement).getDiagramElement(AbstractDiagram.class);
            for(AbstractDiagram diagram: diagrams) {
                try(  IDiagramHandle diagramHandle = diagramService.getDiagramHandle(diagram);){
        
        
                    MObject root = diagram.getCompositionOwner();
        
                    List<IDiagramGraphic> diagramGraphics = diagramHandle.getDiagramGraphics(selectedElement);
                    IDiagramGraphic diagramGraphic = diagramGraphics.get(0);
        
        
                    Rectangle elementBounds = ((IDiagramNode) diagramGraphic).getBounds();
        
        
                    List<Dependency> elementDependencies = ((ModelTree) selectedElement).getDependsOnDependency();
                    for(Dependency elementDependency:elementDependencies) {
                        if(elementDependency.isStereotyped(IAttackTreeDesignerPeerModule.MODULE_NAME, AttackTreeStereotypes.CONNECTION)) {
        
                            Class elementChild = (Class) elementDependency.getDependsOn();
                            
                            ElementRepresentationManager.unmaskChildren(moduleContext, diagramService, diagramHandle , elementChild, elementBounds.x, elementBounds.y + AutoLayoutManager.VERTICAL_AUTOSPACING);
        
        
                            /*
                             * setOwner of Operator element child of Selected element to update representation
                             */
                            MObject packageOwner = diagram.getOrigin().getCompositionOwner();
                            elementChild.setOwner((ModelTree) packageOwner);
        
        
                            /*
                             *  Unmask Operator element child of Selected element
                             */
                            List<IDiagramGraphic> graph = diagramHandle.unmask(elementChild, elementBounds.x, elementBounds.y + AutoLayoutManager.VERTICAL_AUTOSPACING);
        
        
                            /*
                             * Update Operator element child's style
                             */
                            if((graph != null) &&  (graph.size() > 0) && (graph.get(0) instanceof IDiagramNode)) {
                                IDiagramNode graphNode = (IDiagramNode)graph.get(0);
                                ElementRepresentationManager.updateNodeStyle(graphNode);
                            }
        
        
                            /*
                             *  unmask dependencies related to displaced operator element
                             */
                            List<Dependency> elementDependsOnDependencies = ((ModelElement) elementChild).getDependsOnDependency();
                            for(Dependency dependency: elementDependsOnDependencies) {
                                if(dependency.isStereotyped(IAttackTreeDesignerPeerModule.MODULE_NAME, AttackTreeStereotypes.CONNECTION)) {
                                    diagramHandle.unmask(dependency, 0, 0);
                                }
                            }
        //                            List<Dependency> elementImpactedDependency = ((ModelElement) elementChild).getImpactedDependency();
        //                            for(Dependency dependency: elementImpactedDependency) {
        //                                if(dependency.isStereotyped(IAttackTreeDesignerPeerModule.MODULE_NAME, AttackTreeStereotypes.CONNECTION)) {
        //                                    diagramHandle.unmask(dependency, 0, 0);
        //                                }
        //                            }
                            diagramHandle.unmask(elementDependency, 0, 0);
        
                            /*
                             * update owner of displaced operator element Child of selected element
                             */
                            elementChild.setOwner((ModelTree) selectedElement);
                        }
                    }
        
        
                    /*
                     * Autolayout Tree
                     */
                    AutoLayoutManager.autolayoutTree(root, diagramHandle);
        
        
        
                    diagramHandle.save();
                    diagramHandle.close();
                }
            }
        
        
            transaction.commit ();
        }
    }

    @Override
    public boolean accept(final List<MObject> selectedElements, final IModule module) {
        if ((selectedElements != null) && (selectedElements.size() == 1)){
            MObject selectedElement = selectedElements.get(0);
            return ((selectedElement != null) 
                    && (selectedElement instanceof Class)
                    && (((Class) selectedElement).isStereotyped(IAttackTreeDesignerPeerModule.MODULE_NAME, AttackTreeStereotypes.SUBTREE))
                    && selectedElement.getStatus().isModifiable());
        }
        return false;
    }

}
