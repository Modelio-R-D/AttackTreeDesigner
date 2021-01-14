package org.modelio.module.attacktreedesigner.property;

import org.modelio.api.module.propertiesPage.IModulePropertyTable;
import org.modelio.metamodel.uml.infrastructure.ModelElement;
import org.modelio.metamodel.uml.infrastructure.Note;
import org.modelio.module.attacktreedesigner.api.AttackTreeNoteTypes;

public class NotePropertyPage implements IPropertyContent {
    @Override
    public void changeProperty(ModelElement element, int row, String value) {
        if (row == 1) {
            ((Note) element).setContent(value);
        }
    }

    @Override
    public void update(ModelElement element, IModulePropertyTable table) {
        table.addProperty (AttackTreeNoteTypes.COUNTER_MEASURE,((Note)element).getContent());
    }

}
