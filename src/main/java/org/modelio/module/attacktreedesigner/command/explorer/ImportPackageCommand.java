package org.modelio.module.attacktreedesigner.command.explorer;

import java.io.File;
import java.util.List;
import org.modelio.api.modelio.model.IModelingSession;
import org.modelio.api.modelio.model.ITransaction;
import org.modelio.api.modelio.model.IUmlModel;
import org.modelio.api.module.IModule;
import org.modelio.api.module.command.DefaultModuleCommandHandler;
import org.modelio.metamodel.uml.statik.Package;
import org.modelio.module.attacktreedesigner.conversion.JaxbToModelConvertor;
import org.modelio.module.attacktreedesigner.conversion.schema.AttackTreeType;
import org.modelio.module.attacktreedesigner.i18n.Messages;
import org.modelio.module.attacktreedesigner.impl.AttackTreeDesignerModule;
import org.modelio.module.attacktreedesigner.utils.FileSystemManager;
import org.modelio.vcore.smkernel.mapi.MObject;

public class ImportPackageCommand extends DefaultModuleCommandHandler {
    @Override
    public void actionPerformed(final List<MObject> selectedElements, final IModule module) {
        Package destinationPackage = (Package) selectedElements.get(0);
        
        String directoryPath = FileSystemManager.getDialogDirectoryPath(Messages.getString("Ui.Dialog.SelectDirectoryImport.Label"));
        if(directoryPath == null)
            return;
        
        importDirectory(module, directoryPath, destinationPackage);
    }

    @Override
    public boolean accept(final List<MObject> selectedElements, final IModule module) {
        if ((selectedElements != null) && (selectedElements.size() == 1)){
            return selectedElements.get(0) instanceof Package ;
        }
        return false;
    }

    private static void walkDirectoryAndImportTree(File directory, IModule module, Package destinationPackage) {
        for(File file: directory.listFiles()){
            if(file.isDirectory()) {
        
                Package newPackage = destinationPackage;
        
                IModelingSession modellingSession = AttackTreeDesignerModule.getInstance().getModuleContext().getModelingSession ();
                IUmlModel model = modellingSession.getModel();                
        
                try( ITransaction transaction = modellingSession.createTransaction(Messages.getString ("Info.Session.Create", ""))){
        
                    newPackage = model.createPackage(file.getName(), destinationPackage);
        
                    transaction.commit ();
                }
        
        
                walkDirectoryAndImportTree(file, module, newPackage);
        
            } else if (file.isFile() && file.getName().endsWith(FileSystemManager.XML_FILE_EXTENSION)) {
                AttackTreeType jaxbTree = FileSystemManager.unmarshallFileToJaxb(file);
                JaxbToModelConvertor modelToJaxbConvertor = new JaxbToModelConvertor(jaxbTree);
        
                modelToJaxbConvertor.createTreeModel(module, destinationPackage);
            }
        }
    }

    public static void importDirectory(final IModule module, String directoryPath, Package destinationPackage) {
        File directory = new File (directoryPath);
        
        
        IModelingSession modellingSession = AttackTreeDesignerModule.getInstance().getModuleContext().getModelingSession ();
        IUmlModel model = modellingSession.getModel();                
        
        try( ITransaction transaction = modellingSession.createTransaction(Messages.getString ("Info.Session.Create", ""))){
        
            Package newPackage = model.createPackage(directory.getName(), destinationPackage);
        
            walkDirectoryAndImportTree(directory, module, newPackage);
        
            JaxbToModelConvertor.updatePostponedReferences();
            transaction.commit ();
        }
    }

}
