package org.modelio.module.attacktreedesigner.property;

import java.util.List;
import org.modelio.api.module.propertiesPage.IModulePropertyTable;
import org.modelio.metamodel.uml.infrastructure.ModelElement;
import org.modelio.metamodel.uml.infrastructure.Note;
import org.modelio.metamodel.uml.statik.Attribute;
import org.modelio.metamodel.uml.statik.Class;
import org.modelio.module.attacktreedesigner.api.AttackTreeNoteTypes;
import org.modelio.module.attacktreedesigner.api.AttackTreeStereotypes;
import org.modelio.module.attacktreedesigner.api.IAttackTreeDesignerPeerModule;

/**
 * @author ebrosse
 */
public class AttackTreePropertyManager {
    /**
     * @param MObject
     * : the selected MObject
     * 
     * @param row : the row of the property
     * @param value : the new value of the property
     * @return the new value of the row
     */
    public int changeProperty(ModelElement element, int row, String value) {
        IPropertyContent propertyPage = null;
        
        int currentRow = row;
        
        if(element instanceof Class) {
            if(element.isStereotyped(IAttackTreeDesignerPeerModule.MODULE_NAME, AttackTreeStereotypes.ATTACK)) {
                
                // Add Attack Name and Tags to Property Table
                propertyPage = new AttackPropertyPage();
                propertyPage.changeProperty(element, currentRow, value);
                currentRow= currentRow - (element.getTag().size() + 1);
        
                // Add Attack Counter Measures to Property Table
                List<Note> attackNotes = element.getDescriptor();
                for(Note attackNote:attackNotes) {
                    if(attackNote.getModel().getName().equals(AttackTreeNoteTypes.COUNTER_MEASURE)) {
                        propertyPage = new NotePropertyPage();
                        propertyPage.changeProperty(attackNote, currentRow, value);
                        currentRow = currentRow - 1;
                    }
                }
        
            } else if (element.isStereotyped(IAttackTreeDesignerPeerModule.MODULE_NAME, AttackTreeStereotypes.OPERATOR)) {
                
                propertyPage = new OperatorPropertyPage();
                propertyPage.changeProperty(element, currentRow, value);
                currentRow = currentRow - 1;
        
            } else if (element.isStereotyped(IAttackTreeDesignerPeerModule.MODULE_NAME, AttackTreeStereotypes.TREE_REFERENCE)) {
              
                propertyPage = new TreeReferencePropertyPage();
                propertyPage.changeProperty(element, currentRow, value);
                currentRow = currentRow - 1;
            }
            
            
        } else if (element instanceof Note 
                && ((Note) element).getModel().getName().equals(AttackTreeNoteTypes.COUNTER_MEASURE)) {
            
                propertyPage = new NotePropertyPage();
                propertyPage.changeProperty(element, currentRow, value);
                currentRow = currentRow - 1;
            
                
        } else if (element instanceof Attribute
                && element.isStereotyped(IAttackTreeDesignerPeerModule.MODULE_NAME, AttackTreeStereotypes.TREE_REFERENCE_ATTRIBUTE)) {
            
          propertyPage = new TreeReferencePropertyPage();
          propertyPage.changeProperty(((Attribute)element).getOwner(), currentRow, value);
          currentRow = currentRow - 1;            
        }
        return currentRow;
    }

    /**
     * build the property table of the selected Elements
     * 
     * @param element : the selected element
     * @param table : the property table
     */
    public void update(ModelElement element, IModulePropertyTable table) {
        IPropertyContent propertyPage = null;
        
        
        
        if(element instanceof Class) {
            
            if(element.isStereotyped(IAttackTreeDesignerPeerModule.MODULE_NAME, AttackTreeStereotypes.ATTACK)) {
                
                
                // add attack's tags
                propertyPage = new AttackPropertyPage();
                propertyPage.update(element, table);
                
                
                // add attack's counter-measures
                List<Note> attackNotes = element.getDescriptor();
                for(Note attackNote:attackNotes) {
                    if(attackNote.getModel().getName().equals(AttackTreeNoteTypes.COUNTER_MEASURE)) {
                        propertyPage = new NotePropertyPage();
                        propertyPage.update(attackNote, table);
                    }
                }
                
                
            } else if (element.isStereotyped(IAttackTreeDesignerPeerModule.MODULE_NAME, AttackTreeStereotypes.OPERATOR)) {
                
                propertyPage = new OperatorPropertyPage();
                propertyPage.update(element, table);
                
            } else if (element.isStereotyped(IAttackTreeDesignerPeerModule.MODULE_NAME, AttackTreeStereotypes.TREE_REFERENCE)) {
                
                propertyPage = new TreeReferencePropertyPage();
                propertyPage.update(element, table);
            }
        
            
        } else if (element instanceof Note
                && ((Note) element).getModel().getName().equals(AttackTreeNoteTypes.COUNTER_MEASURE)) {
            
                propertyPage = new NotePropertyPage();
                propertyPage.update(element, table);
            
        } else if (element instanceof Attribute
                && element.isStereotyped(IAttackTreeDesignerPeerModule.MODULE_NAME, AttackTreeStereotypes.TREE_REFERENCE_ATTRIBUTE)) {
            
            propertyPage = new TreeReferencePropertyPage();
            propertyPage.update(((Attribute)element).getOwner(), table);
        }
    }

}
