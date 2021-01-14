package org.modelio.module.attacktreedesigner.utils;

import java.io.File;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.modelio.module.attacktreedesigner.conversion.schema.AttackTreeType;
import org.modelio.module.attacktreedesigner.i18n.Messages;

public class FileSystemManager {
    public static final String XML_FILE_EXTENSION = ".xml";

    public static final String PATH_SEPARATOR = "/";

    public static final String PATH_PREDECESSOR = "..";

    public static final String XML_FILE_WILDCARD = "*.xml";

    public static String getXMLFileDialogPath() {
        FileDialog fileDialog = new FileDialog(Display.getDefault().getActiveShell());
        fileDialog.setText(Messages.getString("Ui.Dialog.SelectXMLFileImport.Label"));
        String[] extension = { XML_FILE_WILDCARD };
        fileDialog.setFilterExtensions(extension);
        return fileDialog.open();
    }

    /**
     * @return directory path or null if open dialog failed
     */
    public static String getDialogDirectoryPath(String dialogText) {
        DirectoryDialog directoryDialog = new DirectoryDialog(Display.getDefault().getActiveShell());
        directoryDialog.setText(dialogText);
        return directoryDialog.open();
    }

    public static File createFile(String directoryPath, String fileName) {
        File directory = new File(directoryPath);
        directory.mkdirs();
        File file = new File(directory, fileName);
        //File file = new File(directoryPath+PATH_SEPARATOR+fileName);
        return file;
    }

    public static void marshallJaxbContentInFile(File file, AttackTreeType tree) {
        JAXBContext jaxbContext;
        try {
            jaxbContext = JAXBContext.newInstance(AttackTreeType.class);
            Marshaller jaxbMarshaller = jaxbContext.createMarshaller();
            jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
        
        
            jaxbMarshaller.marshal(tree, file);
        
        } catch (JAXBException e) {
            e.printStackTrace();
        }
    }

    public static AttackTreeType unmarshallFileToJaxb(File file) {
        JAXBContext jaxbContext;
        try {
            jaxbContext = JAXBContext.newInstance(AttackTreeType.class);
            Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
            return (AttackTreeType) jaxbUnmarshaller.unmarshal(file);
            
        } catch (JAXBException e) {
            e.printStackTrace();
        }
        return null;
    }

}
