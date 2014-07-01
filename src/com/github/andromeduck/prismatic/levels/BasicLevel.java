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

import com.github.andromeduck.prismatic.graphics.blocks.Cube;
import com.github.andromeduck.prismatic.graphics.blocks.Drawable;
import com.github.andromeduck.prismatic.graphics.platforms.CubeMap;
import com.github.andromeduck.prismatic.graphics.platforms.DebugAxis;

import java.util.ArrayList;
import java.util.List;

public class BasicLevel extends Level {


    public BasicLevel() {


        //TODO: generic platform dictionary
        List<Drawable> platform0 = new ArrayList<Drawable>();

        player.setColor(.4f, .4f, .4f);


        platforms.add(new DebugAxis());
        platforms.add(new CubeMap());
        }

    @Override
    public int getRenderMode() {
        return MODE_SHADOWMAP;
    }

    @Override
    public void update(float t, float[] inputDir) {
        float[] playerPosPrev = player.getPosition();


        player.setPosition(inputDir[0], inputDir[1], inputDir[2]);
        updateCamera();

    }
}

