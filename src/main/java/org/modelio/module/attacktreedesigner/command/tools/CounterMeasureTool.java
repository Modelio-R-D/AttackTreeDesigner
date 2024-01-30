package org.modelio.module.attacktreedesigner.command.tools;

import java.util.List;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;
import org.modelio.api.modelio.diagram.IDiagramGraphic;
import org.modelio.api.modelio.diagram.IDiagramHandle;
import org.modelio.api.modelio.diagram.IDiagramLink.LinkRouterKind;
import org.modelio.api.modelio.diagram.IDiagramNode;
import org.modelio.api.modelio.diagram.ILinkRoute;
import org.modelio.api.modelio.diagram.tools.DefaultAttachedBoxTool;
import org.modelio.api.modelio.model.IModelingSession;
import org.modelio.api.modelio.model.ITransaction;
import org.modelio.api.modelio.model.IUmlModel;
import org.modelio.metamodel.uml.infrastructure.Note;
import org.modelio.metamodel.uml.statik.Class;
import org.modelio.module.attacktreedesigner.api.AttackTreeNoteTypes;
import org.modelio.module.attacktreedesigner.api.AttackTreeStereotypes;
import org.modelio.module.attacktreedesigner.api.AttackTreeTagTypes;
import org.modelio.module.attacktreedesigner.api.IAttackTreeDesignerPeerModule;
import org.modelio.module.attacktreedesigner.i18n.Messages;
import org.modelio.module.attacktreedesigner.impl.AttackTreeDesignerModule;
import org.modelio.module.attacktreedesigner.utils.elementmanager.representation.DiagramElementBounds;
import org.modelio.module.attacktreedesigner.utils.elementmanager.representation.ElementRepresentationManager;
import org.modelio.module.attacktreedesigner.utils.elementmanager.tags.TagsManager;
import org.modelio.vcore.smkernel.mapi.MObject;

public class CounterMeasureTool extends DefaultAttachedBoxTool {
    @Override
    public boolean acceptElement(IDiagramHandle diagramHandle, IDiagramGraphic targetNode) {
        MObject targetElement = targetNode.getElement();
        return (targetElement instanceof Class 
                && ((Class) targetElement).isStereotyped(IAttackTreeDesignerPeerModule.MODULE_NAME, AttackTreeStereotypes.ATTACK)
                && targetElement.getStatus().isModifiable ()
                );
    }

    @Override
    public void actionPerformed(IDiagramHandle diagramHandle, IDiagramGraphic originNode, LinkRouterKind routerType, ILinkRoute path, Point point) {
        createCounterMeasure(diagramHandle, originNode, point);
    }

    @Override
    public void actionPerformedInDiagram(final IDiagramHandle diagramHandle, final Rectangle rect) {
    }

    public static Note createCounterMeasure(IDiagramHandle diagramHandle, IDiagramGraphic originNode, Point point) {
        Note counterMeasure = null;
        
        IModelingSession session = AttackTreeDesignerModule.getInstance().getModuleContext().getModelingSession();
        IUmlModel model = session.getModel();                
        try( ITransaction transaction = session.createTransaction (Messages.getString("Info.Session.Create", "Counter Measure"))){
            Class counteredAttack = (Class) originNode.getElement();
            
            counterMeasure = model.createNote(IAttackTreeDesignerPeerModule.MODULE_NAME,AttackTreeNoteTypes.COUNTER_MEASURE, counteredAttack, AttackTreeNoteTypes.COUNTER_MEASURE);   
        
            List<IDiagramGraphic> nodeGraphics = diagramHandle.unmask(counterMeasure, point.x, point.y );
            
            // update note bounds
            if(! nodeGraphics.isEmpty()) {
                IDiagramNode diagramNode = (IDiagramNode) nodeGraphics.get(0);
                Rectangle nodeBounds = diagramNode.getBounds();
                nodeBounds.setHeight(DiagramElementBounds.COUNTER_MEASURE.getHeight());
                diagramNode.setBounds(nodeBounds);
            }
            
            // update countered Attack color
            ElementRepresentationManager.setClassColor(counteredAttack, diagramHandle, ElementRepresentationManager.COUNTERED_ATTACK_COLOR);
        
            
            // update countered Attack "Countered attack" tag
            TagsManager.setElementTagValue(counteredAttack, AttackTreeStereotypes.ATTACK, AttackTreeTagTypes.COUNTERED_ATTACK, "true");
            
            diagramHandle.save();
            diagramHandle.close();
            transaction.commit();
        }
        return counterMeasure;
    }

}
