package org.modelio.module.attacktreedesigner.command.explorer;

import java.util.List;
import org.modelio.api.modelio.diagram.IDiagramHandle;
import org.modelio.api.modelio.diagram.IDiagramService;
import org.modelio.api.modelio.model.IModelingSession;
import org.modelio.api.modelio.model.ITransaction;
import org.modelio.api.module.IModule;
import org.modelio.api.module.command.DefaultModuleCommandHandler;
import org.modelio.api.module.context.IModuleContext;
import org.modelio.metamodel.diagrams.ClassDiagram;
import org.modelio.module.attacktreedesigner.api.AttackTreeStereotypes;
import org.modelio.module.attacktreedesigner.api.IAttackTreeDesignerPeerModule;
import org.modelio.module.attacktreedesigner.i18n.Messages;
import org.modelio.module.attacktreedesigner.impl.AttackTreeDesignerModule;
import org.modelio.module.attacktreedesigner.utils.elementmanager.representation.AutoLayoutManager;
import org.modelio.vcore.smkernel.mapi.MObject;

public class AutoLayoutCommand extends DefaultModuleCommandHandler {
    @Override
    public void actionPerformed(List<MObject> selectedElements, IModule module) {
        IModuleContext moduleContext = AttackTreeDesignerModule.getInstance().getModuleContext();
        IModelingSession session = moduleContext.getModelingSession();
        
        ClassDiagram selectedDiagram = (ClassDiagram) selectedElements.get(0);
        
        MObject rootElement = selectedDiagram.getCompositionOwner();
        
        
        IDiagramService diagramService = moduleContext.getModelioServices().getDiagramService();
        
        try( ITransaction transaction = session.createTransaction(Messages.getString ("Info.Session.UpdateModel"))){
        
        
        
            try(  IDiagramHandle diagramHandle = diagramService.getDiagramHandle(selectedDiagram);){
        
                AutoLayoutManager.autolayoutTree(rootElement, diagramHandle);
        
                
                diagramHandle.save();
                diagramHandle.close();
            }
        
            transaction.commit ();
        }
    }

    @Override
    public boolean accept(final List<MObject> selectedElements, final IModule module) {
        if ((selectedElements != null) && (selectedElements.size() == 1)){
            MObject selectedElement = selectedElements.get(0);
            return ((selectedElement != null) 
                    && (selectedElement instanceof ClassDiagram)
                    && ((ClassDiagram) selectedElement).isStereotyped(IAttackTreeDesignerPeerModule.MODULE_NAME, AttackTreeStereotypes.ATTACK_TREE_DIAGRAM)
                    && selectedElement.getStatus().isModifiable());
        }
        return false;
    }

}
