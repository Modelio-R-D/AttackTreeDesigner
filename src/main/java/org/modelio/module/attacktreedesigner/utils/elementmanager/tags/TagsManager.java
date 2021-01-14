package org.modelio.module.attacktreedesigner.utils.elementmanager.tags;

import java.util.List;
import org.modelio.api.modelio.model.IModelingSession;
import org.modelio.metamodel.uml.infrastructure.ModelElement;
import org.modelio.metamodel.uml.infrastructure.TagParameter;
import org.modelio.metamodel.uml.infrastructure.TaggedValue;
import org.modelio.module.attacktreedesigner.api.AttackTreeStereotypes;
import org.modelio.module.attacktreedesigner.api.AttackTreeTagTypes;
import org.modelio.module.attacktreedesigner.api.IAttackTreeDesignerPeerModule;

public class TagsManager {
    public static final String DEFAULT_SEVERITY_VALUE = "MEDIUM";

    public static final String[] SEVERITY_VALUES = {"LOW", "MEDIUM", "HIGH"};

    public static final String DEFAULT_PROBABILITY_VALUE = "MEDIUM";

    public static final String DEFAULT_RISK_LEVEL_VALUE = "HIGH";

    public static final String[] PROBABILITY_VALUES = {"LOW", "MEDIUM", "HIGH"};

    public static final String DEFAULT_SECURITY_RELATED = "false";

    public static final String DEFAULT_SAFETY_RELATED = "false";

    public static final String DEFAULT_OUT_OF_SCOPE = "false";

    public static final String DEFAULT_COUNTERED_ATTACK = "false";

    public static String getTagParameter(TaggedValue tag) {
        if(tag!=null) {
            List<TagParameter> actuals = tag.getActual();
            if ((actuals != null) && (actuals.size() > 0)) {
                return actuals.get(0).getValue();
            }
        }
        return null;
    }

    public static void setElementTagValue(ModelElement element, String sterotypeName, String tagDefinitionName, String value) {
        TaggedValue tag = element.getTag(IAttackTreeDesignerPeerModule.MODULE_NAME, sterotypeName, tagDefinitionName);
        if(tag != null) {
            for(TagParameter actual:tag.getActual()) {
                actual.setValue(value);
            }
        }
    }

    public static void createAttackDefaultTags(IModelingSession session, ModelElement attackElement) {
        TaggedValue severityTag = session.getModel().createTaggedValue(IAttackTreeDesignerPeerModule.MODULE_NAME, AttackTreeTagTypes.SEVERITY, attackElement);
        TagsManager.createTagParameter(session, severityTag, TagsManager.DEFAULT_SEVERITY_VALUE);
        
        TaggedValue probabilityTag = session.getModel().createTaggedValue(IAttackTreeDesignerPeerModule.MODULE_NAME, AttackTreeTagTypes.PROBABILITY, attackElement);
        TagsManager.createTagParameter(session, probabilityTag, TagsManager.DEFAULT_PROBABILITY_VALUE);
        
        TaggedValue securityRelatedTag = session.getModel().createTaggedValue(IAttackTreeDesignerPeerModule.MODULE_NAME, AttackTreeTagTypes.SECURITY_RELATED, attackElement);
        TagsManager.createTagParameter(session, securityRelatedTag, TagsManager.DEFAULT_SECURITY_RELATED);
        
        TaggedValue safetyRelatedTag = session.getModel().createTaggedValue(IAttackTreeDesignerPeerModule.MODULE_NAME, AttackTreeTagTypes.SAFETY_RELATED, attackElement);
        TagsManager.createTagParameter(session, safetyRelatedTag, TagsManager.DEFAULT_SAFETY_RELATED);
        
        TaggedValue outOfScopeTag = session.getModel().createTaggedValue(IAttackTreeDesignerPeerModule.MODULE_NAME, AttackTreeTagTypes.OUT_OF_SCOPE, attackElement);
        TagsManager.createTagParameter(session, outOfScopeTag, TagsManager.DEFAULT_OUT_OF_SCOPE);
        
        /*
         * Create Countered Attack Default Tag
         */
        TaggedValue counteredAttackTag = session.getModel().createTaggedValue(IAttackTreeDesignerPeerModule.MODULE_NAME, AttackTreeTagTypes.COUNTERED_ATTACK, attackElement);
        TagsManager.createTagParameter(session, counteredAttackTag, TagsManager.DEFAULT_COUNTERED_ATTACK);
    }

    public static void createTagParameter(IModelingSession session, TaggedValue taggedValue, String value) {
        TagParameter tagParameter= session.getModel().createTagParameter(value, taggedValue);
        taggedValue.getActual().add(0,tagParameter);
    }

    public static String getElementTagParameter(ModelElement element, String sterotypeName, String tagDefinitionName) {
        TaggedValue tag =  element.getTag(IAttackTreeDesignerPeerModule.MODULE_NAME, sterotypeName, tagDefinitionName);
        if(tag != null) {
            return getTagParameter(tag);
        } else {
            return null;
        }
    }

    public static String getElementRiskLevel(ModelElement element) {
        String severityValue = TagsManager.getElementTagParameter(element, AttackTreeStereotypes.ATTACK, AttackTreeTagTypes.SEVERITY);
        String probabilityValue = TagsManager.getElementTagParameter(element, AttackTreeStereotypes.ATTACK, AttackTreeTagTypes.PROBABILITY);
        
        if(severityValue != null && probabilityValue != null) {
            int newRiskLevelOrder = Severity.valueOf(severityValue).ordinal() + Probability.valueOf(probabilityValue).ordinal();
        
            if(Severity.valueOf(severityValue).ordinal() > 0 &&  Probability.valueOf(probabilityValue).ordinal() > 0) 
                newRiskLevelOrder++;
        
            return RiskLevel.values()[newRiskLevelOrder].toString();
        }
        return null;
    }

    public static void addTagParameter(IModelingSession session, TaggedValue taggedValue, String value) {
        TagParameter tagParameter= session.getModel().createTagParameter(value, taggedValue);
        taggedValue.getActual().add(tagParameter);
    }

    public static void createCustomTag(IModelingSession modelingSession, ModelElement element, String customTagName, String customTagValue) {
        TaggedValue customTag = modelingSession.getModel().createTaggedValue(IAttackTreeDesignerPeerModule.MODULE_NAME, 
                AttackTreeTagTypes.CUSTOM_TAG, 
                element);
        TagsManager.createTagParameter(modelingSession, customTag, customTagName);
        TagsManager.addTagParameter(modelingSession, customTag, customTagValue);
    }

}
