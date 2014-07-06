package com.github.andromeduck.prismatic.graphics.blocks;


public interface Drawable {
    float[] getBoundingSphere();

    float[] getColor();

    float[] getModelM();

    void setParentModelM(float[] newParentModelMatrix);

    void setColor(float[] newColor);

    void setRotate(float rx, float ry, float rz);

    void setScale(float scale);

    float getScale();

    void setPosition(float[] newPosition);

    float[] getPosition();

}
