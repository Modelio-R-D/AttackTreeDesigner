package org.modelio.module.attacktreedesigner.impl;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import org.eclipse.draw2d.geometry.Point;
import org.modelio.api.modelio.diagram.IDiagramGraphic;
import org.modelio.api.modelio.diagram.IDiagramHandle;
import org.modelio.api.modelio.diagram.IDiagramService;
import org.modelio.api.module.context.configuration.IModuleAPIConfiguration;
import org.modelio.metamodel.diagrams.ClassDiagram;
import org.modelio.metamodel.uml.infrastructure.Dependency;
import org.modelio.metamodel.uml.infrastructure.ModelElement;
import org.modelio.metamodel.uml.infrastructure.Note;
import org.modelio.metamodel.uml.statik.Class;
import org.modelio.metamodel.uml.statik.Package;
import org.modelio.module.attacktreedesigner.api.AttackTreeStereotypes;
import org.modelio.module.attacktreedesigner.api.IAttackTreeDesignerPeerModule;
import org.modelio.module.attacktreedesigner.command.explorer.AttackTreeDiagramCommand;
import org.modelio.module.attacktreedesigner.command.explorer.ExportCommand;
import org.modelio.module.attacktreedesigner.command.explorer.ImportCommand;
import org.modelio.module.attacktreedesigner.command.explorer.ImportPackageCommand;
import org.modelio.module.attacktreedesigner.command.tools.ConnectionTool;
import org.modelio.module.attacktreedesigner.command.tools.CounterMeasureTool;
import org.modelio.module.attacktreedesigner.command.tools.CreateAttackTool;
import org.modelio.module.attacktreedesigner.command.tools.CreateTreeReferenceTool;
import org.modelio.module.attacktreedesigner.utils.elementmanager.ElementCreationManager;
import org.modelio.module.attacktreedesigner.utils.elementmanager.ElementReferencing;
import org.modelio.module.attacktreedesigner.utils.elementmanager.representation.DiagramElementBounds;
import org.modelio.module.attacktreedesigner.utils.elementmanager.tags.TagsManager;
import org.modelio.vbasic.version.Version;
import org.modelio.vcore.smkernel.mapi.MObject;

public class AttackTreeDesignerPeerModule implements IAttackTreeDesignerPeerModule {
    private IModuleAPIConfiguration peerConfiguration;

    private AttackTreeDesignerModule module = null;

    public AttackTreeDesignerPeerModule(final AttackTreeDesignerModule module, final IModuleAPIConfiguration peerConfiguration) {
        this.module = module;
        this.peerConfiguration = peerConfiguration;
    }

    @Override
    public IModuleAPIConfiguration getConfiguration() {
        return this.peerConfiguration;
    }

    @Override
    public String getDescription() {
        return this.module.getDescription();
    }

    @Override
    public String getName() {
        return this.module.getName();
    }

    @Override
    public Version getVersion() {
        return this.module.getVersion();
    }

    void init() {
    }

    @Override
    public void exportModel(ModelElement selectedElement, String targetDirectoryPath) {
        // selectedElement is a Tree
        if(selectedElement instanceof Class) {
            Class modelTree = (Class) selectedElement;
            ExportCommand.exportTree(targetDirectoryPath, modelTree);        
        } 
        // selected element is a package
        else if (selectedElement instanceof Package) {
            Package pkg = (Package) selectedElement;
            ExportCommand.exportPackageTrees(targetDirectoryPath, pkg);
        }
    }

    @Override
    public void importModel(Package targetPackage, String sourceElementPath) {
        File file = new File(sourceElementPath);
        
        if(file.isDirectory()) {
            ImportPackageCommand.importDirectory(this.module, sourceElementPath, targetPackage);
        
        } else if(file.isFile()) {
            ImportCommand.importXMLFile( this.module, sourceElementPath, targetPackage);
        
        }
    }

    @Override
    public ModelElement createNewTree(ModelElement owner) {
        return AttackTreeDiagramCommand.createNewTree(this.module, owner);
    }

    @Override
    public Class createANDChild(Class owner, ClassDiagram diagram) {
        Class andChild = null;
        if(owner.isStereotyped(IAttackTreeDesignerPeerModule.MODULE_NAME, AttackTreeStereotypes.ATTACK)
                || owner.isStereotyped(IAttackTreeDesignerPeerModule.MODULE_NAME, AttackTreeStereotypes.OPERATOR)) {
            IDiagramService diagramService = this.module.getModuleContext().getModelioServices().getDiagramService();
            try(  IDiagramHandle diagramHandle = diagramService.getDiagramHandle(diagram);){
                List<IDiagramGraphic> diagramGraphics = diagramHandle.getDiagramGraphics(owner);
                if( !diagramGraphics.isEmpty()) {
                    List<IDiagramGraphic> andElementList = new ArrayList<>();
                    andElementList.add(diagramGraphics.get(0));
                    andChild = ElementCreationManager.createOperatorElement(diagramHandle, andElementList, DiagramElementBounds.ROOT.createRectangleBounds(), AttackTreeStereotypes.AND);        
                }
            }
        }
        return andChild;
    }

    @Override
    public Class createORChild(Class owner, ClassDiagram diagram) {
        Class orChild = null;
        if(owner.isStereotyped(IAttackTreeDesignerPeerModule.MODULE_NAME, AttackTreeStereotypes.ATTACK)
                || owner.isStereotyped(IAttackTreeDesignerPeerModule.MODULE_NAME, AttackTreeStereotypes.OPERATOR)) {
            IDiagramService diagramService = this.module.getModuleContext().getModelioServices().getDiagramService();
            try(  IDiagramHandle diagramHandle = diagramService.getDiagramHandle(diagram);){
                List<IDiagramGraphic> diagramGraphics = diagramHandle.getDiagramGraphics(owner);
                if( !diagramGraphics.isEmpty()) {
                    List<IDiagramGraphic> orElementList = new ArrayList<>();
                    orElementList.add(diagramGraphics.get(0));
                    orChild = ElementCreationManager.createOperatorElement(diagramHandle, orElementList, DiagramElementBounds.ROOT.createRectangleBounds(), AttackTreeStereotypes.OR);        
                }
            }
        }
        return orChild;
    }

    @Override
    public Class createAttack(ClassDiagram diagram) {
        Class attackElement = null;
        IDiagramService diagramService = this.module.getModuleContext().getModelioServices().getDiagramService();
        try(  IDiagramHandle diagramHandle = diagramService.getDiagramHandle(diagram);){
            attackElement =CreateAttackTool.createAttack(diagramHandle, diagramHandle.getDiagramNode(), DiagramElementBounds.ROOT.createRectangleBounds());
        }
        return attackElement;
    }

    @Override
    public Class createReference(ClassDiagram diagram) {
        Class referenceElement = null;
        IDiagramService diagramService = this.module.getModuleContext().getModelioServices().getDiagramService();
        try(  IDiagramHandle diagramHandle = diagramService.getDiagramHandle(diagram);){
            referenceElement = CreateTreeReferenceTool.createReference(diagramHandle, diagramHandle.getDiagramNode(), DiagramElementBounds.ROOT.createRectangleBounds());
        }
        return referenceElement;
    }

    @Override
    public Note createCounterMeasure(Class attack, ClassDiagram diagram) {
        Note counterMeasure = null;
        IDiagramService diagramService = this.module.getModuleContext().getModelioServices().getDiagramService();
        try(  IDiagramHandle diagramHandle = diagramService.getDiagramHandle(diagram);){
            List<IDiagramGraphic> diagramGraphics = diagramHandle.getDiagramGraphics(attack);
            if( !diagramGraphics.isEmpty()) {
                counterMeasure = CounterMeasureTool.createCounterMeasure(diagramHandle, diagramGraphics.get(0),  new Point(0, 0));
            }
        }
        return counterMeasure;
    }

    @Override
    public void updateTag(Class attack, String tagType, String TagValue) {
        TagsManager.setElementTagValue(attack, AttackTreeStereotypes.ATTACK, tagType, TagValue);
    }

    @Override
    public Dependency createConnection(Class source, Class target, ClassDiagram diagram) {
        Dependency connectionElement = null;
        IDiagramService diagramService = this.module.getModuleContext().getModelioServices().getDiagramService();
        try(  IDiagramHandle diagramHandle = diagramService.getDiagramHandle(diagram);){
        
            List<IDiagramGraphic> sourceGraphics = diagramHandle.getDiagramGraphics(source);
            List<IDiagramGraphic> targetGraphics = diagramHandle.getDiagramGraphics(target);
            if( !sourceGraphics.isEmpty() && !targetGraphics.isEmpty() ) {
                connectionElement = ConnectionTool.createConnection(diagramHandle, sourceGraphics.get(0), targetGraphics.get(0));        
            }
        }
        return connectionElement;
    }

    @Override
    public String getElementFullPath(MObject element) {
        return ElementReferencing.getElementFullPath(element);
    }

    @Override
    public void updateReference(Class reference, String referencedTreeFullPath) {
        ElementReferencing.updateListOfAvailableTrees(reference);
        ElementReferencing.updateReference(reference, referencedTreeFullPath);
    }

}
