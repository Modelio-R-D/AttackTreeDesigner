package org.modelio.module.attacktreedesigner.utils.elementmanager.representation;

import org.eclipse.draw2d.geometry.Rectangle;

public enum DiagramElementBounds {
    ROOT (300, 40, 140, 40),
    COUNTER_MEASURE (300,40,140,60);

    private int x;

    private int y;

    private int width;

    private int height;

    DiagramElementBounds(int x, int y, int width, int height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    public Rectangle createRectangleBounds() {
        return new Rectangle(this.x,this.y,this.width,this.height);
    }

    public int getX() {
        return this.x;
    }

    public int getY() {
        return this.y;
    }

    public int getWidth() {
        return this.width;
    }

    public int getHeight() {
        return this.height;
    }

}
