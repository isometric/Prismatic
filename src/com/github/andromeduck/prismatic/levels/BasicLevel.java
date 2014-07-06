package com.github.andromeduck.prismatic.levels;

import com.github.andromeduck.prismatic.graphics.SceneManager;

import com.github.andromeduck.prismatic.graphics.platforms.CubeMap;
import com.github.andromeduck.prismatic.graphics.platforms.DebugAxis;

public class BasicLevel extends Level {


    public BasicLevel() {

        player.setColor(new float[]{.4f, .4f, .4f});

        platforms.add(new DebugAxis());
        platforms.add(new CubeMap());
        }

    @Override
    public int getRenderMode() {
        return MODE_SHADOWMAP;
    }

}
