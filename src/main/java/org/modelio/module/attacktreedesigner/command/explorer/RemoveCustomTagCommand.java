package org.modelio.module.attacktreedesigner.command.explorer;

import java.util.List;
import org.eclipse.swt.widgets.Display;
import org.modelio.api.module.IModule;
import org.modelio.api.module.command.DefaultModuleCommandHandler;
import org.modelio.metamodel.uml.infrastructure.ModelElement;
import org.modelio.metamodel.uml.statik.Class;
import org.modelio.module.attacktreedesigner.api.AttackTreeStereotypes;
import org.modelio.module.attacktreedesigner.api.IAttackTreeDesignerPeerModule;
import org.modelio.module.attacktreedesigner.ui.RemoveCustomTagDialog;
import org.modelio.vcore.smkernel.mapi.MObject;

public class RemoveCustomTagCommand extends DefaultModuleCommandHandler {
    @Override
    public void actionPerformed(List<MObject> selectedElements, IModule module) {
        RemoveCustomTagDialog removeCustomTagDialog = new RemoveCustomTagDialog(Display.getCurrent().getActiveShell());
        removeCustomTagDialog.setSelectedElement((ModelElement)selectedElements.get(0));
        removeCustomTagDialog.open();
    }

    @Override
    public boolean accept(List<MObject> selectedElements, IModule module) {
        if ((selectedElements != null) && (selectedElements.size() == 1)){
            return selectedElements.get(0) instanceof Class
                    && ((Class) selectedElements.get(0))
                    .isStereotyped(IAttackTreeDesignerPeerModule.MODULE_NAME, AttackTreeStereotypes.ATTACK);
        }
        return false;
    }

}
