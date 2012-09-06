/*
   Copyright 2012 Harri Smatt

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

package fi.harism.cubism;

import java.util.Arrays;
import java.util.Comparator;

import android.util.FloatMath;

public class CubismModelExplosion implements CubismRenderer.Model {

	private static final int CUBE_DIV = 10;
	private static final float CUBE_SCALE = 1f / CUBE_DIV;
	private static final int CUBE_SZ = CUBE_DIV * CUBE_DIV * 2 + CUBE_DIV
			* (CUBE_DIV - 2) * 2 + (CUBE_DIV - 2) * (CUBE_DIV - 2) * 2;

	private Cube[] mCubes;

	public CubismModelExplosion() {
		int idx = 0;
		mCubes = new Cube[CUBE_SZ];
		for (int x = 0; x < CUBE_DIV; ++x) {
			for (int y = 0; y < CUBE_DIV; ++y) {
				for (int z = 0; z < CUBE_DIV;) {
					mCubes[idx] = new Cube();
					mCubes[idx].setScale(CUBE_SCALE);

					float t = 2.0f * CUBE_SCALE;
					float tx = x * t + CUBE_SCALE - 1f;
					float ty = y * t + CUBE_SCALE - 1f;
					float tz = z * t + CUBE_SCALE - 1f;

					mCubes[idx].mPositionSource[0] = tx;
					mCubes[idx].mPositionSource[1] = ty;
					mCubes[idx].mPositionSource[2] = tz;

					mCubes[idx].mPositionTarget[0] = tx + tx
							* (float) (3 * Math.random());
					mCubes[idx].mPositionTarget[1] = ty + ty
							* (float) (3 * Math.random());
					mCubes[idx].mPositionTarget[2] = tz + tz
							* (float) (3 * Math.random());

					mCubes[idx].mRotationTarget[0] = (float) (Math.random() * 720 - 360);
					mCubes[idx].mRotationTarget[1] = (float) (Math.random() * 720 - 360);
					mCubes[idx].mRotationTarget[2] = (float) (Math.random() * 720 - 360);

					++idx;

					if (x > 0 && y > 0 && x < CUBE_DIV - 1 && y < CUBE_DIV - 1) {
						z += CUBE_DIV - 1;
					} else {
						++z;
					}
				}
			}
		}

		float gravityX = 3.0f;
		float gravityY = 3.0f;
		float gravityZ = 3.0f;
		for (Cube cube : mCubes) {
			float dx = cube.mPositionSource[0] - gravityX;
			float dy = cube.mPositionSource[1] - gravityY;
			float dz = cube.mPositionSource[2] - gravityZ;
			cube.mDistanceFromGravity = FloatMath.sqrt(dx * dx + dy * dy + dz
					* dz);
		}
		Arrays.sort(mCubes, new Comparator<Cube>() {
			@Override
			public int compare(Cube c0, Cube c1) {
				return c0.mDistanceFromGravity < c1.mDistanceFromGravity ? -1
						: 1;
			}
		});
	}

	@Override
	public CubismCube[] getCubes() {
		return mCubes;
	}

	@Override
	public void interpolate(float t) {
		for (int i = 0; i < mCubes.length; ++i) {
			float tt = t * (1f - (float) i / mCubes.length);
			tt = tt * tt * (3 - 2 * tt);

			Cube cube = mCubes[i];

			CubismUtils.interpolateV(cube.mPosition, cube.mPositionSource,
					cube.mPositionTarget, tt);
			CubismUtils.interpolateV(cube.mRotation, cube.mRotationSource,
					cube.mRotationTarget, tt);

			cube.setTranslate(cube.mPosition[0], cube.mPosition[1],
					cube.mPosition[2]);
			cube.setRotate(cube.mRotation[0], cube.mRotation[1],
					cube.mRotation[2]);
		}
	}

	private class Cube extends CubismCube {
		public float mDistanceFromGravity;
		public final float[] mPosition = new float[3];
		public final float[] mPositionSource = new float[3];
		public final float[] mPositionTarget = new float[3];
		public final float[] mRotation = new float[3];
		public final float[] mRotationSource = new float[3];
		public final float[] mRotationTarget = new float[3];
	}

}
