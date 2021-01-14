package org.modelio.module.attacktreedesigner.command.explorer;

import java.io.File;
import java.util.List;
import org.modelio.api.module.IModule;
import org.modelio.api.module.command.DefaultModuleCommandHandler;
import org.modelio.metamodel.uml.statik.Package;
import org.modelio.module.attacktreedesigner.conversion.JaxbToModelConvertor;
import org.modelio.module.attacktreedesigner.conversion.schema.AttackTreeType;
import org.modelio.module.attacktreedesigner.utils.FileSystemManager;
import org.modelio.vcore.smkernel.mapi.MObject;

public class ImportCommand extends DefaultModuleCommandHandler {
    @Override
    public void actionPerformed(List<MObject> selectedElements, IModule module) {
        String xmlFilePath = FileSystemManager.getXMLFileDialogPath();
        if(xmlFilePath == null)
            return;       
        
        Package destinationPackage = (Package) selectedElements.get(0); 
        
        importXMLFile(module, xmlFilePath, destinationPackage );
    }

    @Override
    public boolean accept(List<MObject> selectedElements, IModule module) {
        if ((selectedElements != null) && (selectedElements.size() == 1)){
            return selectedElements.get(0) instanceof Package ;
        }
        return false;
    }

    public static void importXMLFile(IModule module, String xmlFilePath, Package destinationPackage) {
        AttackTreeType jaxbTree = FileSystemManager.unmarshallFileToJaxb(new File(xmlFilePath));
        JaxbToModelConvertor modelToJaxbConvertor = new JaxbToModelConvertor(jaxbTree);
        
        modelToJaxbConvertor.createTreeModel(module, destinationPackage);
        JaxbToModelConvertor.updatePostponedReferences();
    }

}
