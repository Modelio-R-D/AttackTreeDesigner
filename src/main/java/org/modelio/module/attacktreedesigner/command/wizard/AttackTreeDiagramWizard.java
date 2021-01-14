package org.modelio.module.attacktreedesigner.command.wizard;

import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;
import org.modelio.api.modelio.diagram.IDiagramHandle;
import org.modelio.api.modelio.diagram.IDiagramService;
import org.modelio.api.modelio.diagram.dg.IDiagramDG;
import org.modelio.api.modelio.diagram.style.IStyleHandle;
import org.modelio.api.modelio.model.IMetamodelExtensions;
import org.modelio.api.modelio.model.IModelingSession;
import org.modelio.api.modelio.model.ITransaction;
import org.modelio.api.module.context.IModuleContext;
import org.modelio.api.module.contributor.AbstractWizardContributor;
import org.modelio.api.module.contributor.ElementDescriptor;
import org.modelio.metamodel.diagrams.AbstractDiagram;
import org.modelio.metamodel.diagrams.StaticDiagram;
import org.modelio.metamodel.mmextensions.infrastructure.ExtensionNotFoundException;
import org.modelio.metamodel.uml.infrastructure.ModelElement;
import org.modelio.metamodel.uml.infrastructure.Profile;
import org.modelio.metamodel.uml.infrastructure.Stereotype;
import org.modelio.metamodel.uml.statik.Package;
import org.modelio.module.attacktreedesigner.api.AttackTreeStereotypes;
import org.modelio.module.attacktreedesigner.api.IAttackTreeDesignerPeerModule;
import org.modelio.module.attacktreedesigner.i18n.Messages;
import org.modelio.module.attacktreedesigner.impl.AttackTreeDesignerModule;
import org.modelio.module.attacktreedesigner.utils.AttackTreeResourcesManager;
import org.modelio.vcore.smkernel.mapi.MClass;
import org.modelio.vcore.smkernel.mapi.MMetamodel;
import org.modelio.vcore.smkernel.mapi.MObject;

/**
 * @author ebrosse
 */
public class AttackTreeDiagramWizard extends AbstractWizardContributor {
    @Override
    public AbstractDiagram actionPerformed(ModelElement owner, String diagramName, String description) {
        IModuleContext moduleContext = AttackTreeDesignerModule.getInstance().getModuleContext();
        IModelingSession session = moduleContext.getModelingSession();
        String name = Messages.getString ("Ui.Command.AttackTreeDiagramExplorerCommand.Label");
        StaticDiagram diagram = null;

        try( ITransaction transaction = session.createTransaction(Messages.getString ("Info.Session.Create", "AttackTree Diagram"))){

            diagram = session.getModel().createStaticDiagram(name, owner, IAttackTreeDesignerPeerModule.MODULE_NAME, AttackTreeStereotypes.ATTACK_TREE_DIAGRAM);

            if (diagram != null) {
                IDiagramService ds = moduleContext.getModelioServices().getDiagramService();
                try(  IDiagramHandle handler = ds.getDiagramHandle(diagram);){
                    IDiagramDG dg = handler.getDiagramNode();

                    for (IStyleHandle style : ds.listStyles()){
                        if (style.getName().equals("sysml")){
                            dg.setStyle(style);
                            break;
                        }
                    }

                    handler.save();
                    handler.close();
                }

                moduleContext.getModelioServices().getEditionService().openEditor(diagram);
            }

            transaction.commit ();
        } catch (ExtensionNotFoundException e) {
            moduleContext.getLogService().error(e);
        }
        return diagram;
    }

    @Override
    public String getLabel() {
        return Messages.getString ("Ui.Command.AttackTreeDiagramExplorerCommand.Label");
    }

    @Override
    public String getDetails() {
        return Messages.getString ("Ui.Command.AttackTreeDiagramExplorerCommand.Details");
    }

    @Override
    public String getHelpUrl() {
        return null;
    }

    public Image getIcon() {
        return new Image(Display.getDefault(),AttackTreeResourcesManager.getInstance().getImage("attacktreediagram.png"));
    }

    @Override
    public String getInformation() {
        return Messages.getString ("Ui.Command.AttackTreeDiagramExplorerCommand.Information");
    }

    public boolean accept(MObject selectedElt) {
        return  ((selectedElt != null)
                && (selectedElt.getStatus().isModifiable())
                &&  (((selectedElt instanceof Package)
                        && !(selectedElt instanceof Profile))));
    }

    public ElementDescriptor getCreatedElementType() {
        IModuleContext moduleContext = getModule().getModuleContext();
        MMetamodel metamodel = moduleContext.getModelioServices().getMetamodelService().getMetamodel();
        MClass mClass = metamodel.getMClass(StaticDiagram.class);
        IMetamodelExtensions extensions = moduleContext.getModelingSession().getMetamodelExtensions();
        Stereotype stereotype = extensions.getStereotype(IAttackTreeDesignerPeerModule.MODULE_NAME, AttackTreeStereotypes.ATTACK_TREE_DIAGRAM, mClass);
        return stereotype != null ? new ElementDescriptor(mClass, stereotype) : null;
    }

    @Override
    public void dispose() {
        // TODO Auto-generated method stub
    }

    @Override
    protected boolean checkCanCreateIn(ModelElement arg0) {
        // TODO Auto-generated method stub
        return false;
    }

}
