/*
   Copyright 2014 James Deng

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
 */

package com.github.andromeduck.prismatic.levels;

import android.graphics.Camera;

import com.github.andromeduck.prismatic.graphics.models.Cube;
import com.github.andromeduck.prismatic.graphics.models.Drawable;

import java.util.ArrayList;
import java.util.List;

public class BasicLevel implements Level {

    // TODO: create player class
    public final Drawable player = new Cube();
    public final List<List<Drawable>> platforms = new ArrayList<List<Drawable>>();

    public BasicLevel() {


        //TODO: generic platform dictionary
        List<Drawable> platform0 = new ArrayList<Drawable>();

        Drawable cube0 = new Cube();
        platform0.add(cube0);

        Drawable cube1 = new Cube();
        cube1.setPosition(0, 0, 1);
        platform0.add(cube1);

        platforms.add(platform0);
    }


    @Override
    public List<Drawable> getDrawables() {
        List<Drawable> drawables = new ArrayList<Drawable>();

        drawables.add(player);

        for (List<Drawable> platform : platforms) {
            drawables.addAll(platform);
        }

        return drawables;
    }

    @Override
    public int getRenderMode() {
        return MODE_SHADOWMAP;
    }

    @Override
    public void update(float t, float[] inputDir) {

        updateCamera();
        player.setPosition(inputDir[0], inputDir[1], inputDir[2]);

    }

    private void updateCamera() {
        float[] playerPos = player.getPosition();

        CameraTarget[0] = playerPos[0];
        CameraTarget[1] = playerPos[1];
        CameraTarget[2] = playerPos[2];


        CameraPosition[0] = playerPos[0] + 8f;
        CameraPosition[1] = playerPos[1] + 8f;
        CameraPosition[2] = playerPos[2] + 8f;

        CameraUp[0] = 0;
        CameraUp[1] = 1;
        CameraUp[2] = 0;
    }
}

