package org.modelio.module.attacktreedesigner.utils;


public enum DiagramElementStyle {
    OPERATOR ("NONE", "IMAGE"),
    ATTACK ("SIMPLE", "SIMPLE"),
    TREE_REFERENCE ("SIMPLE", "STRUCTURED");

    private String showNameProperty;

    private String representationMode;

    DiagramElementStyle(String showNameProperty, String representationMode) {
        this.setShowNameProperty(showNameProperty);
        this.setRepresentationMode(representationMode);
    }

    public String getShowNameProperty() {
        return this.showNameProperty;
    }

    public void setShowNameProperty(String showNameProperty) {
        this.showNameProperty = showNameProperty;
    }

    public String getRepresentationMode() {
        return this.representationMode;
    }

    public void setRepresentationMode(String representationMode) {
        this.representationMode = representationMode;
    }

}
