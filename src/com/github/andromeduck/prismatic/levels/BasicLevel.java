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

import com.github.andromeduck.prismatic.graphics.models.Cube;

public class BasicLevel implements Level {

    private final Cube[] mCubes = {new Cube(), new Cube(), new Cube()};

	@Override
    public Cube[] getCubes() {
        return mCubes;
    }

	@Override
	public int getRenderMode() {
		return MODE_SHADOWMAP;
	}

    @Override
    public void update(float t, float[] inputDir) {

        CameraTarget[0] = 0;
        CameraTarget[1] = 0;
        CameraTarget[2] = 0;

        CameraUp[0] = 0;
        CameraUp[1] = 1;
        CameraUp[2] = 0;

        CameraPosition[0] = 5;
        CameraPosition[1] = 5;
        CameraPosition[2] = 5;

        mCubes[0].setTranslate(inputDir[0], inputDir[1], 0);
        mCubes[1].setTranslate(2, 0, 0);
        mCubes[1].setScale(0.5f);
    }
}