package org.modelio.module.attacktreedesigner.utils.elementmanager.representation;

import java.util.List;
import org.eclipse.draw2d.geometry.Rectangle;
import org.modelio.api.modelio.diagram.IDiagramGraphic;
import org.modelio.api.modelio.diagram.IDiagramHandle;
import org.modelio.api.modelio.diagram.IDiagramLink;
import org.modelio.api.modelio.diagram.IDiagramNode;
import org.modelio.api.modelio.diagram.IDiagramService;
import org.modelio.api.module.context.IModuleContext;
import org.modelio.metamodel.diagrams.AbstractDiagram;
import org.modelio.metamodel.uml.infrastructure.Dependency;
import org.modelio.metamodel.uml.infrastructure.Element;
import org.modelio.metamodel.uml.infrastructure.ModelElement;
import org.modelio.metamodel.uml.infrastructure.ModelTree;
import org.modelio.metamodel.uml.infrastructure.Note;
import org.modelio.metamodel.uml.statik.Attribute;
import org.modelio.metamodel.uml.statik.Class;
import org.modelio.metamodel.uml.statik.Classifier;
import org.modelio.module.attacktreedesigner.api.AttackTreeNoteTypes;
import org.modelio.module.attacktreedesigner.api.AttackTreeStereotypes;
import org.modelio.module.attacktreedesigner.api.IAttackTreeDesignerPeerModule;
import org.modelio.module.attacktreedesigner.impl.AttackTreeDesignerModule;
import org.modelio.module.attacktreedesigner.utils.elementmanager.CounterMeasureManager;
import org.modelio.module.attacktreedesigner.utils.elementmanager.Labels;
import org.modelio.vcore.smkernel.mapi.MObject;

public class ElementRepresentationManager {
    public static final String DEFAULT_ATTACK_COLOR = "250, 240, 210";

    public static final String COUNTERED_ATTACK_COLOR = "220, 250, 210";

    private static final int ALTERNATE_LINE_PATTERN = 3;

    private static final int THREAT_ANALYSIS_DEPENDENCY_LINE_WIDTH = 2;

    public static void maskChildren(IModuleContext moduleContext, IDiagramService diagramService, MObject element, IDiagramHandle diagramHandle) {
        if(((Class)element).isStereotyped(IAttackTreeDesignerPeerModule.MODULE_NAME, AttackTreeStereotypes.ROOT)) {
        
            // get children of Root
            List<Dependency> elementDependencies = ((ModelTree) element).getDependsOnDependency();
        
            for(Dependency dependency:elementDependencies) {
                if(dependency.isStereotyped(IAttackTreeDesignerPeerModule.MODULE_NAME, AttackTreeStereotypes.CONNECTION)) {
                    ModelElement child = dependency.getDependsOn();
                    // recursively call maskChildren to children
                    maskChildren(moduleContext, diagramService, child, diagramHandle);
        
                    List<IDiagramGraphic> diagramGraphics = diagramHandle.getDiagramGraphics(child);
                    IDiagramGraphic diagramGraphic = diagramGraphics.get(0);
                    diagramGraphic.mask();
                }
            }
        
        } else {
            // get children of type Class
            List<Class> elementChildren = ((ModelTree) element).getOwnedElement(Class.class);
        
            for(Class child: elementChildren) {
                // recursively call maskChildren to children
                maskChildren(moduleContext, diagramService, child, diagramHandle);
        
                List<IDiagramGraphic> diagramGraphics = diagramHandle.getDiagramGraphics(child);
                IDiagramGraphic diagramGraphic = diagramGraphics.get(0);
                diagramGraphic.mask();
        
            }
        }
    }

    public static void unmaskChildren(IModuleContext moduleContext, IDiagramService diagramService, IDiagramHandle diagramHandle, MObject element, int x, int y) {
        int newX = x;
        int newY = y + AutoLayoutManager.VERTICAL_AUTOSPACING;
        
        List<Class> elementChildren = ((ModelTree) element).getOwnedElement(Class.class);            
        
        // unmask children
        for(MObject child: elementChildren) {
        
            // recursively call maskChildren to children
            unmaskChildren(moduleContext, diagramService, diagramHandle, child, newX, newY);     
        
            // unmask current child
            unmaskElement(diagramHandle, newX, newY, child);                       
        
            // increment x
            newX += AutoLayoutManager.HORIZONTAL_AUTOSPACING;
        
        }
        //        }
    }

    public static void updateNodeStyle(IDiagramNode graphNode) {
        Class nodeElement = (Class) graphNode.getElement();
        if(nodeElement.isStereotyped(IAttackTreeDesignerPeerModule.MODULE_NAME, AttackTreeStereotypes.ATTACK)) {
            graphNode.setProperty(Labels.CLASS_SHOWNAME.name(), DiagramElementStyle.ATTACK.getShowNameProperty());
            graphNode.setProperty(Labels.CLASS_REPRES_MODE.name(), DiagramElementStyle.ATTACK.getRepresentationMode());
            if(CounterMeasureManager.isCountered(nodeElement, true))
                graphNode.setFillColor(COUNTERED_ATTACK_COLOR);
            else
                graphNode.setFillColor(DEFAULT_ATTACK_COLOR);
            
        } else if(nodeElement.isStereotyped(IAttackTreeDesignerPeerModule.MODULE_NAME, AttackTreeStereotypes.OPERATOR)) {
            graphNode.setProperty(Labels.CLASS_SHOWNAME.name(), DiagramElementStyle.OPERATOR.getShowNameProperty());
            graphNode.setProperty(Labels.CLASS_REPRES_MODE.name(), DiagramElementStyle.OPERATOR.getRepresentationMode());
            
        } else if(nodeElement.isStereotyped(IAttackTreeDesignerPeerModule.MODULE_NAME, AttackTreeStereotypes.TREE_REFERENCE)) {
            graphNode.setProperty(Labels.CLASS_SHOWNAME.name(), DiagramElementStyle.TREE_REFERENCE.getShowNameProperty());
            graphNode.setProperty(Labels.CLASS_SHOWNAME.name(), DiagramElementStyle.TREE_REFERENCE.getShowNameProperty());
            if(CounterMeasureManager.isCountered(nodeElement, true))
                graphNode.setFillColor(COUNTERED_ATTACK_COLOR);
            else
                graphNode.setFillColor(DEFAULT_ATTACK_COLOR);
        }
    }

    public static void changeStyleToAttack(Classifier selectedElement, IModuleContext moduleContext) {
        IDiagramService diagramService = moduleContext.getModelioServices().getDiagramService();            
        
        List<AbstractDiagram> diagrams = ((Element) selectedElement).getDiagramElement(AbstractDiagram.class);
        
        for(AbstractDiagram diagram:diagrams) {
            try(  IDiagramHandle diagramHandle = diagramService.getDiagramHandle(diagram);){
        
                List<IDiagramGraphic> diagramGraphics = diagramHandle.getDiagramGraphics(selectedElement);
                if( diagramGraphics.size()>0 && diagramGraphics.get(0) instanceof IDiagramNode) {
        
                    IDiagramNode diagramNode = (IDiagramNode )diagramGraphics.get(0);
                    diagramNode.setProperty(Labels.CLASS_REPRES_MODE.name(), DiagramElementStyle.ATTACK.getRepresentationMode());
        
                }                        
        
                diagramHandle.save();
                diagramHandle.close();
            }
        
        }
    }

    public static void changeStyleToReferencedTree(Classifier selectedElement, IModuleContext moduleContext, Attribute referenceTreeAttribute) {
        IDiagramService diagramService = moduleContext.getModelioServices().getDiagramService();            
        
        List<AbstractDiagram> diagrams = ((Element) selectedElement).getDiagramElement(AbstractDiagram.class);
        
        for(AbstractDiagram diagram:diagrams) {
            try(  IDiagramHandle diagramHandle = diagramService.getDiagramHandle(diagram);){
        
                List<IDiagramGraphic> diagramGraphics = diagramHandle.getDiagramGraphics(selectedElement);
                if( diagramGraphics.size()>0 && diagramGraphics.get(0) instanceof IDiagramNode) {
        
                    IDiagramNode diagramNode = (IDiagramNode )diagramGraphics.get(0);
                    diagramNode.setProperty(Labels.CLASS_REPRES_MODE.name(), DiagramElementStyle.TREE_REFERENCE.getRepresentationMode());
        
                }                        
        
                diagramHandle.unmask(referenceTreeAttribute,0,0);
        
                diagramHandle.save();
                diagramHandle.close();
            }
        
        }
    }

    public static void unmaskElement(IDiagramHandle diagramHandle, int newX, int newY, MObject child) {
        List<IDiagramGraphic> graph = diagramHandle.unmask(child, newX, newY);
        
        
        
        // update node style
        if((graph != null) &&  (graph.size() > 0) && (graph.get(0) instanceof IDiagramNode)) {
            IDiagramNode graphNode = (IDiagramNode)graph.get(0);
            updateNodeStyle(graphNode);
        }
        
        // unmask dependencies
        List<Dependency> elementDependsOnDependencies = ((ModelElement) child).getDependsOnDependency();
        for(Dependency dependency: elementDependsOnDependencies) {
            if(dependency.isStereotyped(IAttackTreeDesignerPeerModule.MODULE_NAME, AttackTreeStereotypes.CONNECTION)) {
                diagramHandle.unmask(dependency, 0, 0);
            }
        }
        
        // unmask counter measure
        List<Note> elementNotes = ((ModelElement) child).getDescriptor();
        int noteSpacingX = newX;
        int noteSpacingY = newY;
        for(Note note:elementNotes) {
            if(note.getModel().getName().equals(AttackTreeNoteTypes.COUNTER_MEASURE)) {
                List<IDiagramGraphic> nodeGraphics = diagramHandle.unmask(note, noteSpacingX += (AutoLayoutManager.HORIZONTAL_AUTOSPACING / 4), noteSpacingY);
                if(! nodeGraphics.isEmpty()) {
                    IDiagramNode noteNode = (IDiagramNode) nodeGraphics.get(0);
                    Rectangle nodeBounds = noteNode.getBounds();
                    nodeBounds.setHeight(DiagramElementBounds.COUNTER_MEASURE.getHeight());
                    noteNode.setBounds(nodeBounds);
        
                }
        
            }
        }
        
        // unmask attribute Referenced tree
        List<Attribute> elementAttributes = ((Class) child).getOwnedAttribute();
        for(Attribute attribute:elementAttributes) {
            if(attribute.isStereotyped(IAttackTreeDesignerPeerModule.MODULE_NAME, AttackTreeStereotypes.TREE_REFERENCE_ATTRIBUTE)) {
                diagramHandle.unmask(attribute,0,0);
            }
        }
    }

    public static void setClassColor(Class element, IDiagramHandle diagramHandle, String color) {
        List<IDiagramGraphic> diagramGraphics = diagramHandle.getDiagramGraphics(element);
        if(! diagramGraphics.isEmpty()) {
            IDiagramNode diagramNode = (IDiagramNode) diagramGraphics.get(0);
            diagramNode.setFillColor(color);
        }
    }

    public static void setClassColor(Class element, String color) {
        IDiagramService diagramService = AttackTreeDesignerModule.getInstance().getModuleContext().getModelioServices().getDiagramService();                    
        List<AbstractDiagram> diagrams = element.getDiagramElement(AbstractDiagram.class);
        for(AbstractDiagram diagram: diagrams) {
            try(  IDiagramHandle diagramHandle = diagramService.getDiagramHandle(diagram);){
                ElementRepresentationManager.setClassColor(element, diagramHandle, color);
        
                diagramHandle.save();
                diagramHandle.close();
            }
        }
    }

    public static void setThreatAnalysisDependencyStyle(Dependency dependency) {
        IDiagramService diagramService = AttackTreeDesignerModule.getInstance().getModuleContext().getModelioServices().getDiagramService();                    
        List<AbstractDiagram> diagrams = dependency.getDiagramElement(AbstractDiagram.class);
        for(AbstractDiagram diagram: diagrams) {
            
            try(  IDiagramHandle diagramHandle = diagramService.getDiagramHandle(diagram);){
                
                List<IDiagramGraphic> diagramGraphics = diagramHandle.getDiagramGraphics(dependency);
        
                IDiagramLink diagramLink = (IDiagramLink )diagramGraphics.get(0);
                diagramLink.setProperty(Labels.CLASS_REPRES_MODE.name(), DiagramElementStyle.TREE_REFERENCE.getRepresentationMode());
                diagramLink.setLinePattern(ALTERNATE_LINE_PATTERN);
                diagramLink.setLineWidth(THREAT_ANALYSIS_DEPENDENCY_LINE_WIDTH);
                diagramLink.setProperty(Labels.DEPENDENCY_SHOWLABEL.toString(), "true");
                
                diagramHandle.save();
                diagramHandle.close();
            }
        }
    }

}
