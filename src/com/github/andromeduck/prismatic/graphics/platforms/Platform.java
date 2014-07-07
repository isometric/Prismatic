package com.github.andromeduck.prismatic.graphics.platforms;

import android.opengl.Matrix;

import com.github.andromeduck.prismatic.graphics.blocks.Drawable;

import java.util.ArrayList;
import java.util.List;

public abstract class Platform {

    protected final List<Drawable> blocks = new ArrayList<Drawable>();
    protected final List<Drawable> decorations = new ArrayList<Drawable>();
    protected final List<Drawable> allDrawables = new ArrayList<Drawable>();

    private final float[] boundingSphere = new float[4];
    private final float[] matrixTranslate = new float[16];
    private final float[] position = new float[3];


    public Platform(){
        init();

        allDrawables.addAll(blocks);
        allDrawables.addAll(decorations);

        float[] maxPos = new float[3];
        for (Drawable d : allDrawables){
            float[] dPosition = d.getPosition();
            maxPos[0] = Math.max(maxPos[0], Math.abs(dPosition[0]));
            maxPos[1] = Math.max(maxPos[1], Math.abs(dPosition[1]));
            maxPos[2] = Math.max(maxPos[2], Math.abs(dPosition[2]));
        }
        boundingSphere[3] = Math.max(maxPos[0], Math.max(maxPos[1], maxPos[2])) + 0.5f;
    }

    public abstract void init();

    public List<Drawable> getDrawables() {
        return allDrawables;
    }

    public List<Drawable> getBlocks() { return blocks; }

    public List<Drawable> getDecorations() { return decorations; }

    public void update() {};

    public float[] getPosition() { return position;}

    public void setPosition(float[] newPosition) {
        System.arraycopy(newPosition, 0, position, 0, 3);
        System.arraycopy(position, 0, boundingSphere, 0, 3);

        Matrix.setIdentityM(matrixTranslate, 0);
        Matrix.translateM(matrixTranslate, 0, position[0], position[1], position[2]);

        for (Drawable d : allDrawables){
            d.setParentModelM(matrixTranslate);
        }
    }

    public float[] getBoundingSphere(){return boundingSphere; }

}
