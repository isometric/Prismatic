package com.github.andromeduck.prismatic.graphics.blocks;

/**
 * Created by t-jadeng on 6/30/2014.
 */
public interface Drawable {
    float[] getBoundingSphere();

    float[] getColor();

    float[] getModelM();

    void setColor(float r, float g, float b);

    void setRotate(float rx, float ry, float rz);

    void setScale(float scale);

    float getScale();

    void setPosition(float tx, float ty, float tz);

    float[] getPosition();
}
