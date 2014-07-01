package com.github.andromeduck.prismatic.levels;

import java.util.List;

import com.github.andromeduck.prismatic.graphics.models.Drawable;


public interface Level {
    public static final int MODE_SHADOWMAP = 1;
    public static final int MODE_SHADOWVOLUME = 2;

    public float[] CameraTarget = new float[3];
    public float[] CameraPosition = new float[3];
    public float[] CameraUp = new float[3];

    public float[] LightPosition = new float[3];
    public float[] ForegroundColor = new float[4];

    public List<Drawable> getDrawables();

    public int getRenderMode();

    public void update(float t, float[] inputDir);
}
