package org.modelio.module.attacktreedesigner.property;

import java.util.List;
import org.modelio.api.module.IModule;
import org.modelio.api.module.propertiesPage.AbstractModulePropertyPage;
import org.modelio.api.module.propertiesPage.IModulePropertyTable;
import org.modelio.metamodel.uml.infrastructure.ModelElement;
import org.modelio.vcore.smkernel.mapi.MObject;

public class AttackTreePropertyPage extends AbstractModulePropertyPage {
    public static final String NAME_PROPERTY = "Name";

    private static final String REFERENCING_A_TREE_LABEL = "Referencing a tree";

    private static final String REFERENCED_TREE_LABEL = "Referenced tree";

    public AttackTreePropertyPage(IModule module, String name, String label, String bitmap) {
        super (module, name, label, bitmap);
    }

    /**
     * This method is called when the current selection changes and that the property box contents requires an update.
     * The ?selectedElements? parameter contains the list of the newly selected elements. The ?table? parameter is the
     * table that must be filled with the updated contents of the property box before returning.
     */
    @Override
    public void update(List<MObject> selectedElements, IModulePropertyTable table) {
        if ((selectedElements != null) && (selectedElements.size() > 0)
                && (selectedElements.get(0) != null)
                && (selectedElements.get(0) instanceof ModelElement)){
        
            ModelElement element = ((ModelElement) selectedElements.get(0));
            
            AttackTreePropertyManager attackTreePage = new AttackTreePropertyManager();
            attackTreePage.update(element, table);
        
        }
    }

    /**
     * This method is called when a value has been edited in the property box in the row ?row?. The ?selectedElements?
     * parameter contains the list of the currently selected elements. The ?row? parameter is the row number of the
     * modified value. The ?value? parameter is the new value the user has set to the given row.
     */
    @Override
    public void changeProperty(List<MObject> selectedElements, int row, String value) {
        if ((selectedElements != null) && (selectedElements.size() > 0) && (selectedElements.get(0) instanceof ModelElement)){
            
            ModelElement element = ((ModelElement) selectedElements.get (0));
        
            AttackTreePropertyManager attackTreePage = new AttackTreePropertyManager();
            attackTreePage.changeProperty(element, row, value);
            
        }
    }

}
