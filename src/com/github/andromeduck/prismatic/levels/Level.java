package com.github.andromeduck.prismatic.levels;

import android.opengl.Matrix;

import java.util.ArrayList;
import java.util.List;

import com.github.andromeduck.prismatic.graphics.blocks.Cube;
import com.github.andromeduck.prismatic.graphics.blocks.Drawable;
import com.github.andromeduck.prismatic.graphics.platforms.Platform;


public abstract class Level {
    public static final int MODE_SHADOWMAP = 1;
    public static final int MODE_SHADOWVOLUME = 2;

    public float[] cameraTarget = new float[3];
    public float[] cameraPosition = new float[3];
    public float[] cameraUp = new float[3];

    public float[] lightPosition = new float[3];
    public float[] foregroundColor = new float[4];
    public final List<Platform> platforms = new ArrayList<Platform>();

    // TODO: create player class
    public final Drawable player = new Cube();


    public List<Drawable> getDrawables() {
        List<Drawable> drawables = new ArrayList<Drawable>();

        drawables.add(player);

        for (Platform platform : platforms) {
            drawables.addAll(platform.getDrawables());
        }

        return drawables;
    }

    public abstract int getRenderMode();

    public void update(float deltaTime){
        for (Platform p : platforms){
            p.update();
        }
    }

    public void updateCamera() {
        float[] playerPos = player.getPosition();


        // TODO: figure out why camera perspective is changing
        cameraTarget[0] = playerPos[0];
        cameraTarget[1] = playerPos[1];
        cameraTarget[2] = playerPos[2];

        cameraPosition[0] = playerPos[0] + 8f;
        cameraPosition[1] = playerPos[1] + 8f;
        cameraPosition[2] = playerPos[2] + 8f;

        cameraUp[0] = 0.1f;
        cameraUp[1] = 1;
        cameraUp[2] = 0.1f;
    }
}
