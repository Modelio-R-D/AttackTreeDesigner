package org.modelio.module.attacktreedesigner.api;

import org.modelio.api.module.IPeerModule;
import org.modelio.metamodel.diagrams.ClassDiagram;
import org.modelio.metamodel.uml.infrastructure.Dependency;
import org.modelio.metamodel.uml.infrastructure.ModelElement;
import org.modelio.metamodel.uml.infrastructure.Note;
import org.modelio.metamodel.uml.statik.Class;
import org.modelio.metamodel.uml.statik.Package;
import org.modelio.vcore.smkernel.mapi.MObject;

public interface IAttackTreeDesignerPeerModule extends IPeerModule {
    public static final String MODULE_NAME = "AttackTreeDesigner";

    void exportModel(ModelElement selectedElement, String targetDirectoryPath);

    void importModel(Package targetPackage, String sourceElementPath);

    ModelElement createNewTree(ModelElement owner);

    Class createANDChild(Class owner, ClassDiagram diagram);

    Class createORChild(Class owner, ClassDiagram diagram);

    Class createAttack(ClassDiagram diagram);

    Class createReference(ClassDiagram diagram);

    Note createCounterMeasure(Class attack, ClassDiagram diagram);

    void updateTag(Class attack, String tagType, String TagValue);

    Dependency createConnection(Class source, Class target, ClassDiagram diagram);

    String getElementFullPath(MObject element);

    void updateReference(Class reference, String referencedTreeFullPath);

}
