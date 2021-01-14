package org.modelio.module.attacktreedesigner.command.explorer;

import java.util.List;
import org.modelio.api.modelio.diagram.IDiagramGraphic;
import org.modelio.api.modelio.diagram.IDiagramHandle;
import org.modelio.api.modelio.diagram.IDiagramNode;
import org.modelio.api.modelio.diagram.IDiagramService;
import org.modelio.api.modelio.diagram.dg.IDiagramDG;
import org.modelio.api.modelio.diagram.style.IStyleHandle;
import org.modelio.api.modelio.model.IModelingSession;
import org.modelio.api.modelio.model.ITransaction;
import org.modelio.api.module.IModule;
import org.modelio.api.module.command.DefaultModuleCommandHandler;
import org.modelio.api.module.context.IModuleContext;
import org.modelio.metamodel.diagrams.ClassDiagram;
import org.modelio.metamodel.uml.infrastructure.ModelElement;
import org.modelio.metamodel.uml.infrastructure.Profile;
import org.modelio.metamodel.uml.infrastructure.Stereotype;
import org.modelio.metamodel.uml.statik.Class;
import org.modelio.metamodel.uml.statik.NameSpace;
import org.modelio.metamodel.uml.statik.Package;
import org.modelio.module.attacktreedesigner.api.AttackTreeStereotypes;
import org.modelio.module.attacktreedesigner.api.IAttackTreeDesignerPeerModule;
import org.modelio.module.attacktreedesigner.i18n.Messages;
import org.modelio.module.attacktreedesigner.utils.IAttackTreeCustomizerPredefinedField;
import org.modelio.module.attacktreedesigner.utils.elementmanager.Labels;
import org.modelio.module.attacktreedesigner.utils.elementmanager.representation.DiagramElementBounds;
import org.modelio.module.attacktreedesigner.utils.elementmanager.tags.TagsManager;
import org.modelio.vcore.smkernel.mapi.MClass;
import org.modelio.vcore.smkernel.mapi.MObject;

public class AttackTreeDiagramCommand extends DefaultModuleCommandHandler {
    private static final String DIAGRAM_DEFAULT_NAME = "Diagram";

    private static final String TREE = "Tree";

    @Override
    public void actionPerformed(List<MObject> selectedElements, IModule module) {
        ModelElement owner = (ModelElement) selectedElements.get(0);    
        
        createNewTree(module, owner);
    }

    @Override
    public boolean accept(List<MObject> selectedElements, IModule module) {
        if ((selectedElements != null) && (selectedElements.size() == 1)){
            MObject selectedElt = selectedElements.get(0);
            return ((selectedElt != null) &&
                    (((selectedElt instanceof Package) 
                            && !(selectedElt instanceof Profile)
                            && selectedElt.getStatus().isModifiable())));
        }
        return false;
    }

    public static Class createNewTree(IModule module, ModelElement owner) {
        IModuleContext moduleContext = module.getModuleContext();
        IModelingSession session = moduleContext.getModelingSession();
        
        Class rootElement = null;
        
        try( ITransaction transaction = session.createTransaction(Messages.getString ("Info.Session.Create", Messages.getString ("Ui.Command.AttackTreeDiagramExplorerCommand.Label")))){
        
            // create Root Class
            rootElement = session.getModel().createClass(Labels.DEFAULT_NAME.toString(), (NameSpace) owner, IAttackTreeDesignerPeerModule.MODULE_NAME, AttackTreeStereotypes.ROOT);
        
            // create Default tags
            TagsManager.createAttackDefaultTags(session, rootElement);
            
            MClass mclass = moduleContext.getModelioServices().getMetamodelService().getMetamodel().getMClass(ClassDiagram.class);
            Stereotype ster = session.getMetamodelExtensions().getStereotype(IAttackTreeDesignerPeerModule.MODULE_NAME, AttackTreeStereotypes.ATTACK_TREE_DIAGRAM, mclass);
            ClassDiagram diagram = session.getModel().createClassDiagram(Labels.DEFAULT_NAME.toString(), rootElement, ster);
        
        
            /*
             * Unmask default Root node in the diagram
             */
            IDiagramService diagramService = moduleContext.getModelioServices().getDiagramService();
            try(  IDiagramHandle diagramHandle = diagramService.getDiagramHandle(diagram);){
        
                IDiagramDG dg = diagramHandle.getDiagramNode();
        
                for (IStyleHandle style : diagramService.listStyles()){
                    if (style.getName().equals(IAttackTreeCustomizerPredefinedField.ATTACKTREE_STYLE_NAME)){
                        dg.setStyle(style);
                        break;
                    }
                }
        
                List<IDiagramGraphic> diagramGraphics = diagramHandle.unmask(rootElement, 0, 0);
                for (IDiagramGraphic diagramGraphic : diagramGraphics) {
                    if(diagramGraphic.getElement().equals(rootElement)){
                        ((IDiagramNode) diagramGraphic).setBounds(DiagramElementBounds.ROOT.createRectangleBounds());
                    }
                }
                diagramHandle.save();
                diagramHandle.close();
            }
        
            session.getModel().getDefaultNameService().setDefaultName(rootElement, TREE);
            session.getModel().getDefaultNameService().setDefaultName(diagram, rootElement.getName() + " " + DIAGRAM_DEFAULT_NAME);
            moduleContext.getModelioServices().getEditionService().openEditor(diagram);
            
            transaction.commit ();
        }
        return rootElement;
    }

}
