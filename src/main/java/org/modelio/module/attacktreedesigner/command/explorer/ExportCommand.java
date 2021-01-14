package org.modelio.module.attacktreedesigner.command.explorer;

import java.io.File;
import java.util.List;
import org.modelio.api.module.IModule;
import org.modelio.api.module.command.DefaultModuleCommandHandler;
import org.modelio.metamodel.uml.statik.Class;
import org.modelio.metamodel.uml.statik.Package;
import org.modelio.module.attacktreedesigner.api.AttackTreeStereotypes;
import org.modelio.module.attacktreedesigner.api.IAttackTreeDesignerPeerModule;
import org.modelio.module.attacktreedesigner.conversion.ModelToJaxbConvertor;
import org.modelio.module.attacktreedesigner.i18n.Messages;
import org.modelio.module.attacktreedesigner.utils.FileSystemManager;
import org.modelio.vcore.smkernel.mapi.MObject;

public class ExportCommand extends DefaultModuleCommandHandler {
    @Override
    public void actionPerformed(List<MObject> selectedElements, IModule module) {
        String directoryPath = FileSystemManager.getDialogDirectoryPath(Messages.getString("Ui.Dialog.SelectDirectoryExport.Label"));
        if(directoryPath == null)
            return;
        
        MObject selectedElement = selectedElements.get(0);
        
        // selectedElement is a Tree
        if(selectedElement instanceof Class) {
            Class modelTree = (Class) selectedElement;
            exportTree(directoryPath, modelTree);        
        } 
        // selected element is a package
        else if (selectedElement instanceof Package) {
            Package pkg = (Package) selectedElement;
            exportPackageTrees(directoryPath, pkg);
        }
    }

    @Override
    public boolean accept(final List<MObject> selectedElements, final IModule module) {
        if ((selectedElements != null) && (selectedElements.size() == 1)){
            MObject selectedElement = selectedElements.get(0);
            return (
                            ((selectedElement instanceof Class) 
                                    && ((Class)selectedElement).isStereotyped(IAttackTreeDesignerPeerModule.MODULE_NAME, AttackTreeStereotypes.ROOT)) 
                            || selectedElement instanceof Package
                    );
        }
        return false;
    }

    public static void exportPackageTrees(String directoryPath, Package pkg) {
        for(MObject element : pkg.getCompositionChildren()) {
            if(element instanceof Class) {
                Class node = (Class) element;
                if(node.isStereotyped(IAttackTreeDesignerPeerModule.MODULE_NAME, AttackTreeStereotypes.ROOT)) {
                    exportTree(directoryPath + FileSystemManager.PATH_SEPARATOR + pkg.getName(), node);
                }
            } else if (element instanceof Package) {
                Package childPackage = (Package) element;
                exportPackageTrees(directoryPath + FileSystemManager.PATH_SEPARATOR + pkg.getName(), childPackage);
            }
        }
    }

    public static void exportTree(String directoryPath, Class modelTree) {
        ModelToJaxbConvertor modelToJaxbConvertor = new ModelToJaxbConvertor(modelTree);
        File file = FileSystemManager.createFile(directoryPath, modelToJaxbConvertor.getModelTree().getName() + FileSystemManager.XML_FILE_EXTENSION);
        FileSystemManager.marshallJaxbContentInFile(file, modelToJaxbConvertor.convertModelToJaxb());
    }

}
