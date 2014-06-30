package com.github.andromeduck.prismatic.levels;

import com.github.andromeduck.prismatic.graphics.models.Cube;

public interface Level {
    public static final int MODE_SHADOWMAP = 1;
    public static final int MODE_SHADOWVOLUME = 2;

    public float[] mCameraLookAt = new float[3];
    public float[] mCameraPosition = new float[3];
    public float[] mLightPosition = {0, 10, 2};
    public float[] mForegroundColor = new float[4];

    public Cube[] getCubes();

    public int getRenderMode();

    public void update(float t);
}
