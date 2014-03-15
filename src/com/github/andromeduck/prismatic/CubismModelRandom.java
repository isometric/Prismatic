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

package com.github.andromeduck.prismatic;

public class CubismModelRandom implements CubismRenderer.Model {

	private Cube[] mCubes;

	public CubismModelRandom(int cubeSize) {
		float cubeScale = 1f / cubeSize;
		int cubeCount = cubeSize * cubeSize * 2 + cubeSize * (cubeSize - 2) * 2
				+ (cubeSize - 2) * (cubeSize - 2) * 2;

		int idx = 0;
		mCubes = new Cube[cubeCount];
		for (int x = 0; x < cubeSize; ++x) {
			for (int y = 0; y < cubeSize; ++y) {
				for (int z = 0; z < cubeSize;) {
					mCubes[idx] = new Cube();
					mCubes[idx].setScale(cubeScale);

					float t = 2.0f * cubeScale;
					float tx = x * t + cubeScale - 1f;
					float ty = y * t + cubeScale - 1f;
					float tz = z * t + cubeScale - 1f;

					mCubes[idx].mPositionSource[0] = tx;
					mCubes[idx].mPositionSource[1] = ty;
					mCubes[idx].mPositionSource[2] = tz;

					mCubes[idx].mPositionTarget[0] = (float) (Math.random() * 6 - 3);
					mCubes[idx].mPositionTarget[1] = (float) (Math.random() * 6 - 3);
					mCubes[idx].mPositionTarget[2] = (float) (Math.random() * 6 - 3);

					mCubes[idx].mRotationTarget[0] = (float) (Math.random() * 720 - 360);
					mCubes[idx].mRotationTarget[1] = (float) (Math.random() * 720 - 360);
					mCubes[idx].mRotationTarget[2] = (float) (Math.random() * 720 - 360);

					++idx;

					if (x > 0 && y > 0 && x < cubeSize - 1 && y < cubeSize - 1) {
						z += cubeSize - 1;
					} else {
						++z;
					}
				}
			}
		}
	}

	@Override
	public CubismCube[] getCubes() {
		return mCubes;
	}

	@Override
	public int getRenderMode() {
		return MODE_SHADOWMAP;
	}

	@Override
	public void setInterpolation(float t) {
		for (int i = 0; i < mCubes.length; ++i) {
			Cube cube = mCubes[i];

			CubismUtils.interpolateV(cube.mPosition, cube.mPositionSource,
					cube.mPositionTarget, t);
			CubismUtils.interpolateV(cube.mRotation, cube.mRotationSource,
					cube.mRotationTarget, t);

			cube.setTranslate(cube.mPosition[0], cube.mPosition[1],
					cube.mPosition[2]);
			cube.setRotate(cube.mRotation[0], cube.mRotation[1],
					cube.mRotation[2]);
		}
	}

	private class Cube extends CubismCube {
		public final float[] mPosition = new float[3];
		public final float[] mPositionSource = new float[3];
		public final float[] mPositionTarget = new float[3];
		public final float[] mRotation = new float[3];
		public final float[] mRotationSource = new float[3];
		public final float[] mRotationTarget = new float[3];
	}

}
