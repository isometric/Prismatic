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
import com.github.andromeduck.prismatic.graphics.GraphicsManager;

public class BasicLevel implements Level {

    private final Cube[] mCubes = {new Cube()};

	@Override
    public Cube[] getCubes() {
        return mCubes;
    }

	@Override
	public int getRenderMode() {
		return MODE_SHADOWMAP;
	}

    @Override
    public void update(float t) {
        mCubes[0].setTranslate((float) Math.sin(t), (float) Math.cos(t), (float) Math.cos(t));
    }
}