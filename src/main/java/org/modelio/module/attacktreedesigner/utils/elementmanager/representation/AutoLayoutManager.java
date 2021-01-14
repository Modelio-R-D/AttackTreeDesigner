package org.modelio.module.attacktreedesigner.utils.elementmanager.representation;

import java.util.ArrayList;
import java.util.List;
import org.eclipse.draw2d.geometry.Rectangle;
import org.modelio.api.modelio.diagram.IDiagramGraphic;
import org.modelio.api.modelio.diagram.IDiagramHandle;
import org.modelio.api.modelio.diagram.IDiagramNode;
import org.modelio.metamodel.uml.infrastructure.ModelTree;
import org.modelio.metamodel.uml.infrastructure.Note;
import org.modelio.metamodel.uml.statik.Class;
import org.modelio.module.attacktreedesigner.api.AttackTreeStereotypes;
import org.modelio.module.attacktreedesigner.api.IAttackTreeDesignerPeerModule;
import org.modelio.vcore.smkernel.mapi.MObject;

public class AutoLayoutManager {
    public static final int VERTICAL_AUTOSPACING = 160;

    public static final int HORIZONTAL_AUTOSPACING = 80;

    public static final int VERTICAL_ADJUSTMENT_FOR_COUNTER_MEASURES = 10;

    private static int max(int n1, int n2) {
        if( n1 > n2)
            return n1;
        else 
            return n2;
    }

    private static void recursivelyAutolayoutSubTree(IDiagramHandle diagramHandle, Class element, IDiagramNode elementNodeGraphic, Rectangle elementBounds, List<Integer> levelsLeftLimitX, int level) {
        int childrenWidth = elementBounds.width;
        int firstChildrenX = elementBounds.x;
        
        List<Class> elementChildren = ((ModelTree)element).getOwnedElement(Class.class);
        int numberOfChildren = elementChildren.size();
        
        
        /*
         * loop children of element and recursively call current method
         */
        if(! elementChildren.isEmpty()) {
        
        
        
            /*
             * First Child
             */
            Class child = elementChildren.get(0);
        
            List<IDiagramGraphic> childDiagramGraphics = diagramHandle.getDiagramGraphics(child);
            if(! childDiagramGraphics.isEmpty()) {
                IDiagramNode childNodeGraphic = (IDiagramNode) childDiagramGraphics.get(0);
        
                Rectangle childNodeBounds = childNodeGraphic.getBounds();
        
                //childrenWidth = 
                updateChildBounds(diagramHandle, elementBounds, levelsLeftLimitX, level + 1, numberOfChildren, child, childNodeBounds);
        
                recursivelyAutolayoutSubTree(diagramHandle, child, childNodeGraphic, childNodeBounds, levelsLeftLimitX, level + 1);
                firstChildrenX = childNodeBounds.x;
                childrenWidth = childNodeBounds.width;
            }
        
        
        
            /*
             * Loop the rest of the children except the last one
             */
            int nbMiddleChildren = numberOfChildren - 2;
            for(int i = 1; i <= nbMiddleChildren; i++) {
        
                child = elementChildren.get(i);
                childDiagramGraphics = diagramHandle.getDiagramGraphics(child);
        
                if(! childDiagramGraphics.isEmpty()) {
                    IDiagramNode childNodeGraphic = (IDiagramNode) childDiagramGraphics.get(0);
        
                    Rectangle childNodeBounds = childNodeGraphic.getBounds();
        
                    updateChildBounds(diagramHandle, elementBounds, levelsLeftLimitX, level + 1, numberOfChildren, child, childNodeBounds);                    
        
                    recursivelyAutolayoutSubTree(diagramHandle, child, childNodeGraphic, childNodeBounds, levelsLeftLimitX, level + 1);
        
        
                }
            }
        
        
        
            /*
             * Last child
             */
             if(numberOfChildren>1) {
                 child = elementChildren.get(numberOfChildren - 1);
                 childDiagramGraphics = diagramHandle.getDiagramGraphics(child);
        
                 if(! childDiagramGraphics.isEmpty()) {
                     IDiagramNode childNodeGraphic = (IDiagramNode) childDiagramGraphics.get(0);
        
                     Rectangle childNodeBounds = childNodeGraphic.getBounds();
        
                     //int lastChildWidth = 
                     updateChildBounds(diagramHandle, elementBounds, levelsLeftLimitX, level + 1, numberOfChildren, child, childNodeBounds);                    
        
                     recursivelyAutolayoutSubTree(diagramHandle, child, childNodeGraphic, childNodeBounds, levelsLeftLimitX, level + 1);
        
                     childrenWidth = childNodeBounds.x + childNodeBounds.width - firstChildrenX; 
                 }
        
             }
        
        }
        
        
        
        /*
         * Update element and its notes after having positioned its children
         */
        if(! element.isStereotyped(IAttackTreeDesignerPeerModule.MODULE_NAME, AttackTreeStereotypes.ROOT)) {
        
        
            elementBounds.setX( firstChildrenX + ((childrenWidth - elementBounds.width)/2) );
            Rectangle lastBounds = elementBounds;
            for(Note note:element.getDescriptor()) {
                List<IDiagramGraphic> noteDiagramGraphics = diagramHandle.getDiagramGraphics(note);
                if(! noteDiagramGraphics.isEmpty()) {
                    IDiagramNode noteNodeGraphic = (IDiagramNode) noteDiagramGraphics.get(0);                       
                    Rectangle noteBounds = noteNodeGraphic.getBounds();
                    noteBounds.setX(lastBounds.x + lastBounds.width + HORIZONTAL_AUTOSPACING);
                    lastBounds = noteBounds;
        
                    noteNodeGraphic.setBounds(noteBounds);
                }
            }
        
            levelsLeftLimitX.set(level, lastBounds.x + lastBounds.width + HORIZONTAL_AUTOSPACING);
        
        }
        
        
        
        /*
         * update element node bounds
         */
        elementNodeGraphic.setBounds(elementBounds);
    }

    private static int updateChildBounds(IDiagramHandle diagramHandle, Rectangle elementBounds, List<Integer> levelsLeftLimitX, int level, int numberOfChildren, Class child, Rectangle childNodeBounds) {
        // Child SetY
        childNodeBounds.setY(elementBounds.y + VERTICAL_AUTOSPACING - (childNodeBounds.height/2));
        
        
        // child setX
        if (levelsLeftLimitX.size() <= level) {
            // leftLimitX has not been set yet in this level        
            childNodeBounds.setX(elementBounds.x + (elementBounds.width / 2) - 
                    ((HORIZONTAL_AUTOSPACING * (numberOfChildren - 1) + childNodeBounds.width * numberOfChildren) / 2));
        
            levelsLeftLimitX.add(level, childNodeBounds.x + childNodeBounds.width + HORIZONTAL_AUTOSPACING);
        } else {
        
            // leftLimitX has been set in this level
            childNodeBounds.setX( max( levelsLeftLimitX.get(level), elementBounds.x + (elementBounds.width / 2) - 
                    ((HORIZONTAL_AUTOSPACING * (numberOfChildren - 1) + childNodeBounds.width * numberOfChildren) / 2)));
            levelsLeftLimitX.set(level, childNodeBounds.x + childNodeBounds.width + HORIZONTAL_AUTOSPACING);
        }
        
        
        /*
         * Aoutolayout of Counter Measures next to the child
         */
        int counterMeasuresWitdth = updateCounterMeasuresBounds(diagramHandle, levelsLeftLimitX, level, child, childNodeBounds);
        return childNodeBounds.width + counterMeasuresWitdth;
    }

    public static void autolayoutTree(MObject rootElement, IDiagramHandle diagramHandle) {
        List<IDiagramGraphic> diagramGraphics = diagramHandle.getDiagramGraphics(rootElement);
        
        if(! diagramGraphics.isEmpty()) {
        
            IDiagramNode nodeGraphic = (IDiagramNode) diagramGraphics.get(0);
        
            Rectangle rootBounds = nodeGraphic.getBounds();
        
            List<Integer> levelsLeftLimitX = new ArrayList<>();
            levelsLeftLimitX.add(rootBounds.x + HORIZONTAL_AUTOSPACING);
            updateCounterMeasuresBounds(diagramHandle, levelsLeftLimitX, 0, (Class) rootElement, rootBounds);
        
            AutoLayoutManager.recursivelyAutolayoutSubTree(diagramHandle, (Class) rootElement, nodeGraphic, rootBounds, levelsLeftLimitX, 0);
        
        }
    }

    private static int updateCounterMeasuresBounds(IDiagramHandle diagramHandle, List<Integer> levelsLeftLimitX, int level, Class element, Rectangle elementBounds) {
        int counterMeasuresWidth = 0;
        
        
        
        // list element notes 
        int adjustedY = 0;
        for(Note note:element.getDescriptor()) {
        
            List<IDiagramGraphic> noteDiagramGraphics = diagramHandle.getDiagramGraphics(note);
            if(! noteDiagramGraphics.isEmpty()) {
        
                IDiagramNode noteNodeGraphic = (IDiagramNode) noteDiagramGraphics.get(0);                       
                Rectangle noteBounds = noteNodeGraphic.getBounds();
        
                // SET Y of Counter Measure
                noteBounds.setY(elementBounds.y + (elementBounds.height / 2) - (noteBounds.height/2) + adjustedY);
                adjustedY = adjustedY + VERTICAL_ADJUSTMENT_FOR_COUNTER_MEASURES;
        
                // SET X of Counter Measure
                if (levelsLeftLimitX.size() <= level) {
                    // leftLimitX has not been set yet in this level        
                    noteBounds.setX(elementBounds.x + elementBounds.width  + HORIZONTAL_AUTOSPACING );
        
                    levelsLeftLimitX.add(level, noteBounds.x + noteBounds.width + HORIZONTAL_AUTOSPACING);
                } else {
        
                    // leftLimitX has been set in this level
                    noteBounds.setX( max( levelsLeftLimitX.get(level), elementBounds.x + elementBounds.width  + HORIZONTAL_AUTOSPACING ));
                    levelsLeftLimitX.set(level, noteBounds.x + noteBounds.width + HORIZONTAL_AUTOSPACING);
                }
        
                noteNodeGraphic.setBounds(noteBounds);
        
                counterMeasuresWidth = counterMeasuresWidth + HORIZONTAL_AUTOSPACING +  noteBounds.width();
        
            }
        }
        return counterMeasuresWidth;
    }

}
