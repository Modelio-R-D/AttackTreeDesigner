package org.modelio.module.attacktreedesigner.impl;

import java.util.List;
import java.util.Set;
import org.modelio.api.modelio.model.IModelingSession;
import org.modelio.api.modelio.model.ITransaction;
import org.modelio.api.modelio.model.event.IElementDeletedEvent;
import org.modelio.api.modelio.model.event.IElementMovedEvent;
import org.modelio.api.modelio.model.event.IModelChangeEvent;
import org.modelio.api.modelio.model.event.IModelChangeHandler;
import org.modelio.metamodel.uml.infrastructure.Dependency;
import org.modelio.metamodel.uml.infrastructure.ModelElement;
import org.modelio.metamodel.uml.infrastructure.ModelTree;
import org.modelio.metamodel.uml.infrastructure.Note;
import org.modelio.metamodel.uml.infrastructure.TagParameter;
import org.modelio.metamodel.uml.infrastructure.TaggedValue;
import org.modelio.metamodel.uml.statik.Attribute;
import org.modelio.metamodel.uml.statik.Class;
import org.modelio.module.attacktreedesigner.api.AttackTreeNoteTypes;
import org.modelio.module.attacktreedesigner.api.AttackTreeStereotypes;
import org.modelio.module.attacktreedesigner.api.AttackTreeTagTypes;
import org.modelio.module.attacktreedesigner.api.IAttackTreeDesignerPeerModule;
import org.modelio.module.attacktreedesigner.i18n.Messages;
import org.modelio.module.attacktreedesigner.utils.elementmanager.CounterMeasureManager;
import org.modelio.module.attacktreedesigner.utils.elementmanager.ElementCreationManager;
import org.modelio.module.attacktreedesigner.utils.elementmanager.ElementNavigationManager;
import org.modelio.module.attacktreedesigner.utils.elementmanager.representation.ElementRepresentationManager;
import org.modelio.module.attacktreedesigner.utils.elementmanager.tags.ProbabilityTagManager;
import org.modelio.module.attacktreedesigner.utils.elementmanager.tags.SeverityTagManager;
import org.modelio.module.attacktreedesigner.utils.elementmanager.tags.TagsManager;
import org.modelio.vcore.smkernel.mapi.MObject;

public class AttackTreeModelChangeHandler implements IModelChangeHandler {
    @Override
    public void handleModelChange(IModelingSession session, IModelChangeEvent event) {
        /*
         * handle Created elements
         */
        Set<MObject> createdEvents = event.getCreationEvents();
        for ( MObject createdEvent : createdEvents){
            handleCreatedElement(session, createdEvent);
        }
        
        
        /*
         * Handle Updated elements
         */
        Set<MObject> updatedEvents = event.getUpdateEvents();
        for ( MObject updatedEvent : updatedEvents){
            handleUpdatedElement(session, updatedEvent);
        }
        
        
        /*
         * Handle Deleted Elements
         */
        List<IElementDeletedEvent> deleteEvents = event.getDeleteEvents();
        for (IElementDeletedEvent deleteEvent : deleteEvents){
            handleDeletedElement(session, deleteEvent);
        }
        
        
        
        /*
         * Handle Moved elements
         */
        List<IElementMovedEvent> movedEvents = event.getMoveEvents();
        for(IElementMovedEvent movedEvent:movedEvents) {
            handleMovedElement(session, movedEvent);
        }
    }

    private static void handleDeletedElement(IModelingSession session, IElementDeletedEvent deleteEvent) {
        try( ITransaction transaction = session.createTransaction (Messages.getString ("Info.Session.UpdateModel"))){
        
            MObject deletedElement = deleteEvent.getDeletedElement();
        
            if (deletedElement instanceof Dependency ) {
                Dependency deletedDependency = (Dependency) deletedElement;        
                handleDeletedDependency(session, deletedDependency); 
            }
        
            else if (deletedElement instanceof Class) {
                Class deletedClass = (Class) deletedElement;
                handleDeletedClass(session, deletedClass);
            }
        
            else if (deletedElement instanceof Note) {
                Note deletedNote = (Note) deletedElement;
                handleDeletedNote(deletedNote);
            }
        
            transaction.commit();
        }
    }

    private static void handleUpdatedElement(IModelingSession session, MObject updatedEvent) {
        if( updatedEvent instanceof Class) {
            Class updatedClass = (Class)updatedEvent;
            handleUpdatedClass(session, updatedClass);
        } else if (updatedEvent instanceof TagParameter) {            
            TagParameter updatedTagParameter = (TagParameter) updatedEvent;
            handleUpdatedTagParameter(session, updatedTagParameter);
        } else if (updatedEvent instanceof Dependency) {
            Dependency updatedDependency =  (Dependency) updatedEvent;
            handleUpdatedDependency(session, updatedDependency);
        }
    }

    private static void handleUpdatedTagParameter(IModelingSession session, TagParameter updatedTagParameter) {
        TaggedValue updatedTag = updatedTagParameter.getAnnoted();
        String tagName = updatedTag.getDefinition().getName();
        
        boolean severityTagIsUpdated = tagName.equals(AttackTreeTagTypes.SEVERITY);
        boolean probabilityTagIsUpdated = tagName.equals(AttackTreeTagTypes.PROBABILITY);
        boolean counteredTagIsUpdated = tagName.equals(AttackTreeTagTypes.COUNTERED_ATTACK);
        
        
        Class attack = (Class) updatedTag.getAnnoted();
        
        if(attack.isStereotyped(IAttackTreeDesignerPeerModule.MODULE_NAME, AttackTreeStereotypes.ROOT)) {
            // if the attack is a Root
            for(Attribute attribute : attack.getObject()) {
                if(attribute.isStereotyped(IAttackTreeDesignerPeerModule.MODULE_NAME, AttackTreeStereotypes.TREE_REFERENCE_ATTRIBUTE)) {
                    Class reference = (Class) attribute.getOwner();
                    if(!reference.isDeleted()) {
        
                        /*
                         * Update reference Color to Countered
                         */
                        ElementRepresentationManager.setClassColor(reference, ElementRepresentationManager.COUNTERED_ATTACK_COLOR);                        
        
                        // if reference has a parent
                        for(Dependency parentDependency: reference.getImpactedDependency()) {
                            if(parentDependency.isStereotyped(IAttackTreeDesignerPeerModule.MODULE_NAME, AttackTreeStereotypes.CONNECTION)) {
                                Class firstAttackAscendant = getFirstNonDeletedAttackAscendant(parentDependency);
                                if(firstAttackAscendant != null) {
                                    try( ITransaction transaction = session.createTransaction (Messages.getString ("Info.Session.UpdateModel"))){
                                        updateAndPropagateAttackTags(firstAttackAscendant, severityTagIsUpdated, probabilityTagIsUpdated, counteredTagIsUpdated);
                                        transaction.commit();
                                    }
                                }
                            }
                        }
                    }
                }
            }
        
        
        } else {
            // if the attack has a parent
            for(Dependency parentDependency : attack.getImpactedDependency()) {
                if(parentDependency.isStereotyped(IAttackTreeDesignerPeerModule.MODULE_NAME, AttackTreeStereotypes.CONNECTION)) {
                    Class firstAttackAscendant = getFirstNonDeletedAttackAscendant(parentDependency);
                    if(firstAttackAscendant != null) {
                        try( ITransaction transaction = session.createTransaction (Messages.getString ("Info.Session.UpdateModel"))){
                            updateAndPropagateAttackTags(firstAttackAscendant, severityTagIsUpdated, probabilityTagIsUpdated, counteredTagIsUpdated);
                            transaction.commit();
                        }
                    }
                }
            }
        }
    }

    private static void handleUpdatedClass(IModelingSession session, Class updatedClass) {
        // if change in the type of the Operator (AND to OR or vice versa)
        if (updatedClass.isStereotyped(IAttackTreeDesignerPeerModule.MODULE_NAME, AttackTreeStereotypes.OPERATOR)) {
            Class firstAttackAscendant = getFirstNonDeletedAttackAscendant(updatedClass);
            if(firstAttackAscendant != null) {
                try( ITransaction transaction = session.createTransaction (Messages.getString ("Info.Session.UpdateModel"))){
                    updateAndPropagateAttackTags(firstAttackAscendant, true, true, true);
                    transaction.commit();
                }
            }
        }
    }

    private static void handleCreatedElement(IModelingSession session, MObject createdEvent) {
        // If Created Dependency
        if (createdEvent instanceof Dependency) {
            Dependency createdDependency = (Dependency) createdEvent;
            if(createdDependency.isStereotyped(IAttackTreeDesignerPeerModule.MODULE_NAME, AttackTreeStereotypes.CONNECTION)) {
                Class firstAttackAscendant = getFirstNonDeletedAttackAscendant(createdDependency);
                if(firstAttackAscendant != null) { 
        
                    try( ITransaction transaction = session.createTransaction (Messages.getString ("Info.Session.UpdateModel"))){
                        updateAndPropagateAttackTags(firstAttackAscendant, true, true, true);
                        transaction.commit();
                    }
                }
            }
        }
    }

    public static void updateAndPropagateAttackTags(Class attack, boolean updateSeverity, boolean updateProbability, boolean updateCountered) {
        if(updateSeverity) {
        
            int minSeverityIndex = SeverityTagManager.getMinSeverityIndex(attack);
            String attackSeverity = TagsManager.getElementTagParameter(attack, AttackTreeStereotypes.ATTACK, AttackTreeTagTypes.SEVERITY);
            int attackSeverityIndex = minSeverityIndex;
        
            for(int i=0; i < TagsManager.SEVERITY_VALUES.length; i++) {
                if(attackSeverity.equals(TagsManager.SEVERITY_VALUES[i])) {
                    attackSeverityIndex = i;
                    break;
                }
            }
        
            if(attackSeverityIndex < minSeverityIndex) {
                TagsManager.setElementTagValue(attack, AttackTreeStereotypes.ATTACK, AttackTreeTagTypes.SEVERITY, TagsManager.SEVERITY_VALUES[minSeverityIndex]);
            }
        }
        
        if(updateProbability) {
        
            int[] probabilityIndexBounds = ProbabilityTagManager.getProbabilityIndexBounds(attack);
            String attackProbability = TagsManager.getElementTagParameter(attack, AttackTreeStereotypes.ATTACK, AttackTreeTagTypes.PROBABILITY);
        
        
            for(int i=0; i < TagsManager.PROBABILITY_VALUES.length; i++) {
                if(attackProbability.equals(TagsManager.PROBABILITY_VALUES[i])) {
                    int attackProbabilityIndex = i;
                    if(attackProbabilityIndex < probabilityIndexBounds[0]) {
                        TagsManager.setElementTagValue(attack, AttackTreeStereotypes.ATTACK, AttackTreeTagTypes.PROBABILITY, TagsManager.PROBABILITY_VALUES[probabilityIndexBounds[0]]);
                    } else if(attackProbabilityIndex > probabilityIndexBounds[1]) {
                        TagsManager.setElementTagValue(attack, AttackTreeStereotypes.ATTACK, AttackTreeTagTypes.PROBABILITY, TagsManager.PROBABILITY_VALUES[probabilityIndexBounds[1]]);
                    }
        
                    break;
                }
            }            
        }
        
        if(updateCountered) {
        
            if(CounterMeasureManager.isCountered(attack, false)) {
                ElementRepresentationManager.setClassColor(attack, ElementRepresentationManager.COUNTERED_ATTACK_COLOR);
                TagsManager.setElementTagValue(attack, AttackTreeStereotypes.ATTACK, AttackTreeTagTypes.COUNTERED_ATTACK, "true");
            } else {
                ElementRepresentationManager.setClassColor(attack, ElementRepresentationManager.DEFAULT_ATTACK_COLOR);
                TagsManager.setElementTagValue(attack, AttackTreeStereotypes.ATTACK, AttackTreeTagTypes.COUNTERED_ATTACK, "false");                    
            }
        
        }
        
        
        if(attack.isStereotyped(IAttackTreeDesignerPeerModule.MODULE_NAME, AttackTreeStereotypes.ROOT)) {
            /*
             * Propagate to references and their ascendants if it is a Referenced Root
             */
            for(Attribute attribute : attack.getObject()) {
                if(attribute.isStereotyped(IAttackTreeDesignerPeerModule.MODULE_NAME, AttackTreeStereotypes.TREE_REFERENCE_ATTRIBUTE)) {
                    Class reference = (Class) attribute.getOwner();
                    if(!reference.isDeleted()) {
        
                        if(updateCountered) {
                            // update reference color
                            if(CounterMeasureManager.isCountered(reference, true)) {
                                ElementRepresentationManager.setClassColor(reference, ElementRepresentationManager.COUNTERED_ATTACK_COLOR);
                            } else {
                                ElementRepresentationManager.setClassColor(reference, ElementRepresentationManager.DEFAULT_ATTACK_COLOR);
                            }
                        }
        
        
        
                        // propagate to references ascendants
                        for(Dependency parentDependency : reference.getImpactedDependency()) {
                            if(parentDependency.isStereotyped(IAttackTreeDesignerPeerModule.MODULE_NAME, AttackTreeStereotypes.CONNECTION)) {
                                Class firstAscendantAttack = getFirstNonDeletedAttackAscendant(parentDependency);
                                if(firstAscendantAttack != null)
                                    updateAndPropagateAttackTags(firstAscendantAttack, updateSeverity, updateProbability, updateCountered);
                            }
                        }
        
                    }
                }
            }
        } else {
            /*
             * Propagate to ascendants
             */
            for(Dependency parentDependency : attack.getImpactedDependency()) {
                if(parentDependency.isStereotyped(IAttackTreeDesignerPeerModule.MODULE_NAME, AttackTreeStereotypes.CONNECTION)) {
                    Class firstAscendantAttack = getFirstNonDeletedAttackAscendant(parentDependency);
                    if(firstAscendantAttack != null)
                        updateAndPropagateAttackTags(firstAscendantAttack, updateSeverity, updateProbability, updateCountered);
        
                }
            }
        }
    }

    public static Class getFirstNonDeletedAttackAscendant(ModelElement element) {
        if(element instanceof Dependency) {
            return getFirstNonDeletedAttackAscendant( ((Dependency)element).getImpacted() );
        
        } else if (element instanceof Class && !element.isDeleted()) {
            Class node = (Class) element;
            if(node.isStereotyped(IAttackTreeDesignerPeerModule.MODULE_NAME, AttackTreeStereotypes.ATTACK)){
                return node;
            } else if (node.isStereotyped(IAttackTreeDesignerPeerModule.MODULE_NAME, AttackTreeStereotypes.OPERATOR)
                    || node.isStereotyped(IAttackTreeDesignerPeerModule.MODULE_NAME, AttackTreeStereotypes.TREE_REFERENCE)){
                for(Dependency dependency: node.getImpactedDependency()) {
                    if(dependency.isStereotyped(IAttackTreeDesignerPeerModule.MODULE_NAME, AttackTreeStereotypes.CONNECTION)){
                        return getFirstNonDeletedAttackAscendant(dependency);
                    }
                }
            }
        }
        return null;
    }

    private static void handleDeletedDependency(IModelingSession session, Dependency deletedDependency) {
        if( deletedDependency.isStereotyped(IAttackTreeDesignerPeerModule.MODULE_NAME, AttackTreeStereotypes.CONNECTION)){
        
        
            MObject rootElement = deletedDependency.getDiagramElement().get(0).getOrigin(); 
        
        
            /*
             * Move Destination to Root (set Owner to Root)
             */
            ModelElement dependencyDependsOn = deletedDependency.getDependsOn();
            if  (!dependencyDependsOn.isDeleted()) {
        
                Class destinationNode = (Class) dependencyDependsOn;
                destinationNode.setOwner((ModelTree) rootElement);
                ElementCreationManager.renameElement(session, destinationNode); 
            }    
        
        
            /*
             * Update tags of ascendants
             */
            Class firstAttackAscendant = getFirstNonDeletedAttackAscendant(deletedDependency);
            if(firstAttackAscendant != null) 
                updateAndPropagateAttackTags(firstAttackAscendant, true, true, true);
        
        
        }
    }

    private static void handleDeletedClass(IModelingSession session, Class deletedClass) {
        if( (deletedClass).isStereotyped(IAttackTreeDesignerPeerModule.MODULE_NAME, AttackTreeStereotypes.ROOT)) {
        
        
            // Change attribute references type to undefined
            List<Attribute> referenceAttributesList = deletedClass.getObject();
            for(Attribute referenceAttribute:referenceAttributesList) {
                if(! referenceAttribute.isDeleted()) 
                    referenceAttribute.setType(session.getModel().getUmlTypes().getUNDEFINED());
        
                /*
                 *  update tags to ascendants
                 */
                Class reference = (Class) referenceAttribute.getOwner();
        
                // Update reference Color to Default
                ElementRepresentationManager.setClassColor(reference, ElementRepresentationManager.DEFAULT_ATTACK_COLOR);                        
        
                // Propagate to ascendants of reference
                for(Dependency parentDependency : reference.getImpactedDependency()) {
                    if(parentDependency.isStereotyped(IAttackTreeDesignerPeerModule.MODULE_NAME, AttackTreeStereotypes.CONNECTION)) {
                        Class firstAttackAscendant = getFirstNonDeletedAttackAscendant(parentDependency);
                        if(firstAttackAscendant != null) 
                            updateAndPropagateAttackTags(firstAttackAscendant, false, false, true);                         
                    }
                }
        
            }
        }
    }

    private static void handleDeletedNote(Note deletedNote) {
        if(deletedNote.getModel().getName().equals(AttackTreeNoteTypes.COUNTER_MEASURE)) {
            Class counteredAttack = (Class) deletedNote.getSubject();
            if(! counteredAttack.isDeleted()) {
        
                /*
                 * Update attack Tag to false and Color to Default
                 */
                ElementRepresentationManager.setClassColor(counteredAttack, ElementRepresentationManager.DEFAULT_ATTACK_COLOR);                        
                TagsManager.setElementTagValue(counteredAttack, AttackTreeStereotypes.ATTACK, AttackTreeTagTypes.COUNTERED_ATTACK, "false");
        
                /*
                 * Propagate to ascendants
                 */
                for(Dependency parentDependency : counteredAttack.getImpactedDependency()) {
                    if(parentDependency.isStereotyped(IAttackTreeDesignerPeerModule.MODULE_NAME, AttackTreeStereotypes.CONNECTION)) {
                        Class firstAttackAscendant = getFirstNonDeletedAttackAscendant(parentDependency);
                        if(firstAttackAscendant != null) 
                            updateAndPropagateAttackTags(firstAttackAscendant, false, false, true);                         
                    }
                }
        
                /*
                 * If counteredAttack is ROOT and is referenced
                 */
                if(counteredAttack.isStereotyped(IAttackTreeDesignerPeerModule.MODULE_NAME, AttackTreeStereotypes.ROOT)) {
                    for(Attribute attribute : counteredAttack.getObject()) {
                        if(attribute.isStereotyped(IAttackTreeDesignerPeerModule.MODULE_NAME, AttackTreeStereotypes.TREE_REFERENCE_ATTRIBUTE)) {
                            Class reference = (Class) attribute.getOwner();
        
                            /*
                             * Update reference Color to Default
                             */
                            ElementRepresentationManager.setClassColor(reference, ElementRepresentationManager.DEFAULT_ATTACK_COLOR);                        
        
                            /*
                             * Propagate to ascendants of reference
                             */
                            for(Dependency parentDependency : reference.getImpactedDependency()) {
                                if(parentDependency.isStereotyped(IAttackTreeDesignerPeerModule.MODULE_NAME, AttackTreeStereotypes.CONNECTION)) {
                                    Class firstAttackAscendant = getFirstNonDeletedAttackAscendant(parentDependency);
                                    if(firstAttackAscendant != null) 
                                        updateAndPropagateAttackTags(firstAttackAscendant, false, false, true);                         
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private static void handleUpdatedDependency(IModelingSession session, Dependency updatedDependency) {
        if( updatedDependency.isStereotyped(IAttackTreeDesignerPeerModule.MODULE_NAME, AttackTreeStereotypes.CONNECTION)){
        
        
            Class rootNode = (Class) updatedDependency.getDiagramElement().get(0).getOrigin(); 
            Class originNode = (Class) updatedDependency.getImpacted();
            ModelElement newDestinationNode = updatedDependency.getDependsOn();
        
            try( ITransaction transaction = session.createTransaction (Messages.getString ("Info.Session.UpdateModel"))){
        
        
        
                /*
                 *  Move Old Destination Node under the Root
                 *  By moving every node under originNode which is not related with an outgoing Dependency to the root
                 */
                if(! originNode.isStereotyped(IAttackTreeDesignerPeerModule.MODULE_NAME, AttackTreeStereotypes.ROOT)){
                    for( MObject child : originNode.getCompositionChildren()) {
                        if(child instanceof Class) {
                            Class childNode = (Class) child;
        
                            boolean childHasStereotypedConnectionIngoingDependency = false;
        
                            for(Dependency dependency : childNode.getImpactedDependency()) {
                                if(dependency.isStereotyped(IAttackTreeDesignerPeerModule.MODULE_NAME, AttackTreeStereotypes.CONNECTION)){
                                    childHasStereotypedConnectionIngoingDependency = true;
                                    break;
                                }
                            }
        
                            if(! childHasStereotypedConnectionIngoingDependency){
                                // Move childNode under rootNode
                                childNode.setOwner(rootNode);
                                ElementCreationManager.renameElement(session, childNode); 
                            }
                        }
                    }
                }
        
        
                /*
                 * Check if destination node is already a target of an existing dependency
                 * If it is the case then delete the updatedDependency
                 */
                for(Dependency dependency : newDestinationNode.getImpactedDependency()) {
                    if(dependency.isStereotyped(IAttackTreeDesignerPeerModule.MODULE_NAME, AttackTreeStereotypes.CONNECTION)
                            && ! dependency.equals(updatedDependency)){
                        updatedDependency.delete();
                        break;
                    }
                }
        
        
                /*
                 * Check for forbidden connections in  originNode--(Dep)-->newDestinationNode
                 * If it is the case then delete the updatedDependency
                 */
                if(! updatedDependency.isDeleted()) {
                    if(!(newDestinationNode instanceof Class)
                            ||newDestinationNode.isStereotyped(IAttackTreeDesignerPeerModule.MODULE_NAME, AttackTreeStereotypes.ROOT)
                            || newDestinationNode.isStereotyped(IAttackTreeDesignerPeerModule.MODULE_NAME, AttackTreeStereotypes.SUBTREE)
                            || (originNode.isStereotyped(IAttackTreeDesignerPeerModule.MODULE_NAME, AttackTreeStereotypes.ATTACK)
                                    && !newDestinationNode.isStereotyped(IAttackTreeDesignerPeerModule.MODULE_NAME, AttackTreeStereotypes.OPERATOR))
                            ||(ElementNavigationManager.getSecondLevelAncestor(originNode)
                                    .equals(ElementNavigationManager.getSecondLevelAncestor((Class) newDestinationNode))) ){
        
                        updatedDependency.delete();
                    }
                }
        
        
                /*
                 * Move new destination node under originNode
                 */
                if(! updatedDependency.isDeleted()) {
                    ((Class )newDestinationNode).setOwner(originNode);
                    ElementCreationManager.renameElement(session, (Class) newDestinationNode); 
                }
        
                // propagate tags to ascendants
                Class firstAttackAscendant = getFirstNonDeletedAttackAscendant(updatedDependency);
                if(firstAttackAscendant != null) {
                    updateAndPropagateAttackTags(firstAttackAscendant, true, true, true);
                }
        
                transaction.commit();
            }
        }
    }

    private void handleMovedElement(IModelingSession session, IElementMovedEvent movedEvent) {
        MObject movedElement = movedEvent.getMovedElement(); 
        
        /*
         * handle moved dependency
         */
        if(movedElement instanceof Dependency) {
        
            Dependency movedDependency = (Dependency) movedElement;
            Class rootNode = (Class) movedDependency.getDiagramElement().get(0).getOrigin(); 
            ModelElement newOriginNode = movedDependency.getImpacted();
            Class destinationNode = (Class) movedDependency.getDependsOn();
            Class oldOriginNode = (Class) destinationNode.getCompositionOwner();
        
            try( ITransaction transaction = session.createTransaction (Messages.getString ("Info.Session.UpdateModel"))){
        
                /*
                 * Check if newOriginNode node is already an origin of an existing connection dependency
                 * If it is the case then delete the movedDependency
                 */
                for(Dependency dependency : newOriginNode.getDependsOnDependency()) {
                    if(dependency.isStereotyped(IAttackTreeDesignerPeerModule.MODULE_NAME, AttackTreeStereotypes.CONNECTION)
                            && ! dependency.equals(movedDependency)){
                        movedDependency.delete();
                        break;
                    }
                }
        
        
                /*
                 * Check for forbidden connections in  newOriginNode--(Dep)-->destinationNode
                 * If it is the case then delete the movedDependency
                 */
                if(! movedDependency.isDeleted()) {
                    if(!(newOriginNode instanceof Class)
                            ||(! destinationNode.isStereotyped(IAttackTreeDesignerPeerModule.MODULE_NAME, AttackTreeStereotypes.OPERATOR) 
                            && ! newOriginNode.isStereotyped(IAttackTreeDesignerPeerModule.MODULE_NAME, AttackTreeStereotypes.OPERATOR) )
                            || newOriginNode.isStereotyped(IAttackTreeDesignerPeerModule.MODULE_NAME, AttackTreeStereotypes.SUBTREE) 
                            || newOriginNode.isStereotyped(IAttackTreeDesignerPeerModule.MODULE_NAME, AttackTreeStereotypes.TREE_REFERENCE) 
                            || (ElementNavigationManager.getSecondLevelAncestor((Class)newOriginNode)
                                    .equals(ElementNavigationManager.getSecondLevelAncestor(destinationNode)))) {
        
                        movedDependency.delete();
                    } 
        
        
                }
        
        
                /*
                 * Move destination node and propagate tags
                 */
                if(movedDependency.isDeleted()) {
                    destinationNode.setOwner(rootNode);
                } else {
                    destinationNode.setOwner((Class)newOriginNode);
        
                    // if movedDependency not deleted propagate tags to ascendants of newOriginNode
                    Class firstAttackAscendant = getFirstNonDeletedAttackAscendant(movedDependency);
                    if(firstAttackAscendant != null) {
                        updateAndPropagateAttackTags(firstAttackAscendant, true, true, true);
                    }
                }
                ElementCreationManager.renameElement(session, destinationNode); 
        
        
                /*
                 * Propagate tags to ascendants of oldOriginNode
                 */
                Class firstAttackAscendant = getFirstNonDeletedAttackAscendant(oldOriginNode);
                if(firstAttackAscendant != null) {
                    updateAndPropagateAttackTags(firstAttackAscendant, true, true, true);
                }
        
                transaction.commit();
            }
        }
    }

}
