package org.modelio.module.attacktreedesigner.property;

import org.modelio.api.modelio.model.IModelingSession;
import org.modelio.api.module.propertiesPage.IModulePropertyTable;
import org.modelio.metamodel.uml.infrastructure.ModelElement;
import org.modelio.module.attacktreedesigner.api.AttackTreeStereotypes;
import org.modelio.module.attacktreedesigner.i18n.Messages;
import org.modelio.module.attacktreedesigner.impl.AttackTreeDesignerModule;
import org.modelio.module.attacktreedesigner.impl.AttackTreeDesignerPeerModule;

public class OperatorPropertyPage implements IPropertyContent {
    /**
     * This method handles the changes of the given property, identified by its row index, of a selected element
     * to a new value.
     * @param MObject : the selected element
     * 
     * @param row : the row of the changed property
     * @param value : the new value of the property
     */
    @Override
    public void changeProperty(ModelElement element, int row, String value) {
        if( row == 1 ) {
        
            IModelingSession session = AttackTreeDesignerModule.getInstance().getModuleContext().getModelingSession ();
        
            element.removeStereotypes(AttackTreeDesignerPeerModule.MODULE_NAME, AttackTreeStereotypes.AND);
            element.removeStereotypes(AttackTreeDesignerPeerModule.MODULE_NAME, AttackTreeStereotypes.OR);
        
            if (value.equals(AttackTreeStereotypes.AND)) {
                element.addStereotype(AttackTreeDesignerPeerModule.MODULE_NAME, AttackTreeStereotypes.AND);
                session.getModel().getDefaultNameService().setDefaultName(element, AttackTreeStereotypes.AND);
            }else if (value.equals(AttackTreeStereotypes.OR)) {
                element.addStereotype(AttackTreeDesignerPeerModule.MODULE_NAME, AttackTreeStereotypes.OR);
                session.getModel().getDefaultNameService().setDefaultName(element, AttackTreeStereotypes.OR);
            }
        }
    }

    /**
     * This method handles the construction of the property table of a selected element
     * @param MObject : the selected element
     * 
     * @param table : the property table to fulfill
     */
    @Override
    public void update(ModelElement element, IModulePropertyTable table) {
        String[] values = {AttackTreeStereotypes.AND, AttackTreeStereotypes.OR  };                                             
        
        String value = "";
        if (element.isStereotyped(AttackTreeDesignerPeerModule.MODULE_NAME, AttackTreeStereotypes.AND)) {
            value = AttackTreeStereotypes.AND;  
        }else if (element.isStereotyped(AttackTreeDesignerPeerModule.MODULE_NAME, AttackTreeStereotypes.OR)) {
            value = AttackTreeStereotypes.OR;
        }
        
        table.addProperty (Messages.getString("Ui.Property.TypeCondition.Name"),
                value,
                values);
    }

}
