package com.github.andromeduck.prismatic.levels;

import android.opengl.Matrix;

import java.util.ArrayList;
import java.util.List;

import com.github.andromeduck.prismatic.graphics.SceneManager;
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

        for (Platform p : platforms) {
            drawables.addAll(p.getDrawables());
        }

        return drawables;
    }

    public abstract int getRenderMode();

    public void update(float deltaTime){
        // Update platforms
        for (Platform p : platforms){
            p.update();
        }


        float[] playerPos = player.getPosition();
        playerPos[0] += SceneManager.inputDir[0];
        playerPos[1] += SceneManager.inputDir[1];
        playerPos[2] += SceneManager.inputDir[2];
        player.setPosition(playerPos);


        // TODO: remove this when matrix bug fixed
        platforms.get(0).setPosition(new float[]{5,0,0});
        platforms.get(1).setPosition(SceneManager.inputDir);

        //TODO: implement collision block vs decoration

        //TODO: implement roll mechanic

        //Update Camera
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
