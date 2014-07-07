package com.github.andromeduck.prismatic.graphics.blocks;


public interface Drawable {
    float[] getBoundingSphere();

    float[] getColor();

    float[] getModelM();

    void setParentModelM(float[] newParentModelMatrix);

    void setColor(float[] color);

    void setRotate(float[] rotation);

    void setScale(float[] scale);

    float[] getScale();

    void setPosition(float[] position);

    float[] getPosition();

}
