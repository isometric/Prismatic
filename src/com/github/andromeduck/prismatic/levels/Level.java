package com.github.andromeduck.prismatic.levels;

import java.util.ArrayList;
import java.util.List;

import com.github.andromeduck.prismatic.graphics.blocks.Drawable;
import com.github.andromeduck.prismatic.graphics.blocks.PlayerBlock;
import com.github.andromeduck.prismatic.graphics.platforms.Platform;


public abstract class Level {
    public static final int MODE_SHADOWMAP = 1;
    public static final int MODE_SHADOWVOLUME = 2;

    public float[] cameraTarget = new float[3];
    public float[] cameraPosition = new float[3];
    public float[] cameraUp = {0,1,0};

    public float[] lightPosition = new float[3];
    public float[] foregroundColor = new float[4];
    public final List<Platform> platforms = new ArrayList<Platform>();

    public final PlayerBlock playerBlock = new PlayerBlock();


    public List<Drawable> getDrawables() {
        List<Drawable> drawables = new ArrayList<Drawable>();
        drawables.add(playerBlock);
        for (Platform p : platforms) { drawables.addAll(p.getDrawables()); }
        return drawables;
    }

    public abstract int getRenderMode();

    public void update(float deltaTime, float[] inputDir){
        // Update platforms
        for (Platform p : platforms){
            p.update();
        }


        // Handles collisions/rolling
        playerBlock.update(deltaTime, inputDir, platforms);

        //Update Camera
        // TODO: remove camera bounce once roll is implemented
        float[] playerPos = playerBlock.getPosition();
        cameraTarget[0] = playerPos[0];
        cameraTarget[1] = playerPos[1];
        cameraTarget[2] = playerPos[2];

        cameraPosition[0] = playerPos[0] + 8f;
        cameraPosition[1] = playerPos[1] + 8f;
        cameraPosition[2] = playerPos[2] + 8f;

    }

}
