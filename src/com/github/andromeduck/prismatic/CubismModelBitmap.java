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

import android.graphics.Bitmap;
import android.graphics.Color;

public class CubismModelBitmap implements CubismRenderer.Model {

	private Cube[] mCubes;

	public CubismModelBitmap(Bitmap bitmap) {
		int pixelCount = 0;
		for (int x = 0; x < bitmap.getWidth(); ++x) {
			for (int y = 0; y < bitmap.getHeight(); ++y) {
				if (Color.alpha(bitmap.getPixel(x, y)) != 0) {
					++pixelCount;
				}
			}
		}

		int cubeDiv = 0;
		while (6 * cubeDiv * cubeDiv - 12 * cubeDiv + 8 < pixelCount) {
			++cubeDiv;
		}
		int cubeCount = 6 * cubeDiv * cubeDiv - 12 * cubeDiv + 8;
		float scaleSource = 1.0f / cubeDiv;

		int idx = 0;
		mCubes = new Cube[cubeCount];
		for (int x = 0; x < cubeDiv; ++x) {
			for (int y = 0; y < cubeDiv; ++y) {
				for (int z = 0; z < cubeDiv;) {
					mCubes[idx] = new Cube();

					float t = 2.0f * scaleSource;
					float tx = x * t + scaleSource - 1f;
					float ty = y * t + scaleSource - 1f;
					float tz = z * t + scaleSource - 1f;

					mCubes[idx].mScaleSource = scaleSource;
					mCubes[idx].mPositionCtrl0[0] = tx;
					mCubes[idx].mPositionCtrl0[1] = ty;
					mCubes[idx].mPositionCtrl0[2] = tz;
					mCubes[idx].mPositionCtrl1[0] = (float) (Math.random() * 6 - 3);
					mCubes[idx].mPositionCtrl1[1] = (float) (Math.random() * 6 - 3);
					mCubes[idx].mPositionCtrl1[2] = (float) (Math.random() * 6 - 3);

					mCubes[idx].mRotationTarget[0] = 90 * (int) (Math.random() * 8 - 4);
					mCubes[idx].mRotationTarget[1] = 90 * (int) (Math.random() * 8 - 4);
					mCubes[idx].mRotationTarget[2] = 90 * (int) (Math.random() * 8 - 4);

					++idx;

					if (x > 0 && y > 0 && x < cubeDiv - 1 && y < cubeDiv - 1) {
						z += cubeDiv - 1;
					} else {
						++z;
					}
				}
			}
		}

		for (int i = 0; i < cubeCount; ++i) {
			int randIdx = (int) (Math.random() * cubeCount);
			Cube tmp = mCubes[i];
			mCubes[i] = mCubes[randIdx];
			mCubes[randIdx] = tmp;
		}

		idx = 0;
		float scaleTarget = 2.0f / Math.max(bitmap.getWidth(),
				bitmap.getHeight());
		float px = bitmap.getWidth() < bitmap.getHeight() ? (bitmap.getHeight() - bitmap
				.getWidth()) / 2.0f : 0;
		float py = bitmap.getHeight() < bitmap.getWidth() ? (bitmap.getWidth() - bitmap
				.getHeight()) / 2.0f : 0;
		for (int x = 0; x < bitmap.getWidth(); ++x) {
			for (int y = 0; y < bitmap.getHeight(); ++y) {
				if (Color.alpha(bitmap.getPixel(x, y)) != 0) {
					Cube cube = mCubes[idx++];
					float t = 2.0f * scaleTarget;
					float tx = (x + px) * t + scaleTarget - 2f;
					float ty = (y + py) * t + scaleTarget - 2f;
					cube.mScaleTarget = scaleTarget;
					cube.mPositionCtrl2[0] = tx;
					cube.mPositionCtrl2[1] = -ty;
					cube.mPositionCtrl2[2] = 0.0f;
				}
			}
		}
		for (; idx < mCubes.length; ++idx) {
			Cube cube = mCubes[idx];
			cube.mScaleTarget = 0.0f;
			cube.mPositionCtrl2[0] += cube.mPositionCtrl0[0]
					+ cube.mPositionCtrl0[0] * Math.random() * 3;
			cube.mPositionCtrl2[1] += cube.mPositionCtrl0[1]
					+ cube.mPositionCtrl0[1] * Math.random() * 3;
			cube.mPositionCtrl2[2] += cube.mPositionCtrl0[2]
					+ cube.mPositionCtrl0[2] * Math.random() * 3;
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
		for (Cube cube : mCubes) {
			float scale = cube.mScaleSource
					+ (cube.mScaleTarget - cube.mScaleSource) * t;

			CubismUtils.interpolateV(cube.mPosition, cube.mPositionCtrl0,
					cube.mPositionCtrl1, cube.mPositionCtrl2, t);
			CubismUtils.interpolateV(cube.mRotation, cube.mRotationSource,
					cube.mRotationTarget, t);

			cube.setScale(scale);
			cube.setTranslate(cube.mPosition[0], cube.mPosition[1],
					cube.mPosition[2]);
			cube.setRotate(cube.mRotation[0], cube.mRotation[1],
					cube.mRotation[2]);
		}
	}

	private class Cube extends CubismCube {
		public final float[] mPosition = new float[3];
		public final float[] mPositionCtrl0 = new float[3];
		public final float[] mPositionCtrl1 = new float[3];
		public final float[] mPositionCtrl2 = new float[3];
		public final float[] mRotation = new float[3];
		public final float[] mRotationSource = new float[3];
		public final float[] mRotationTarget = new float[3];
		public float mScaleSource;
		public float mScaleTarget;
	}

}
