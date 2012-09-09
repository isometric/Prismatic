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

import java.nio.ByteBuffer;

import android.opengl.Matrix;

public class CubismCube {

	private static ByteBuffer mBufferNormals;
	private static ByteBuffer mBufferNormalsInv;
	private static ByteBuffer mBufferVertices;
	private static final float SQRT_2 = 1.41421356237f;

	static {
		// Vertex and normal data plus indices arrays.
		final byte[][] CUBEVERTICES = { { -1, 1, 1 }, { -1, -1, 1 },
				{ 1, 1, 1 }, { 1, -1, 1 }, { -1, 1, -1 }, { -1, -1, -1 },
				{ 1, 1, -1 }, { 1, -1, -1 } };
		final byte[][] CUBENORMALS = { { 0, 0, 1 }, { 0, 0, -1 }, { -1, 0, 0 },
				{ 1, 0, 0 }, { 0, 1, 0 }, { 0, -1, 0 } };
		final int[][][] CUBEFILLED = {
				{ { 0, 1, 2, 1, 3, 2 }, { 0 }, { 1 }, { 0, 1, 3, 2 } },
				{ { 6, 7, 4, 7, 5, 4 }, { 1 }, { 0 }, { 6, 7, 5, 4 } },
				{ { 0, 4, 1, 4, 5, 1 }, { 2 }, { 3 }, { 0, 4, 5, 1 } },
				{ { 3, 7, 2, 7, 6, 2 }, { 3 }, { 2 }, { 3, 7, 6, 2 } },
				{ { 4, 0, 6, 0, 2, 6 }, { 4 }, { 5 }, { 4, 0, 2, 6 } },
				{ { 1, 5, 3, 5, 7, 3 }, { 5 }, { 4 }, { 1, 5, 7, 3 } } };

		mBufferVertices = ByteBuffer.allocateDirect(3 * 6 * 6);
		mBufferNormals = ByteBuffer.allocateDirect(3 * 6 * 6);
		mBufferNormalsInv = ByteBuffer.allocateDirect(3 * 6 * 6);

		final byte C = 1, P = 0;
		for (int i = 0; i < CUBEFILLED.length; ++i) {
			for (int j = 0; j < CUBEFILLED[i][0].length; ++j) {
				mBufferVertices.put(CUBEVERTICES[CUBEFILLED[i][0][j]]);
				mBufferNormals.put(CUBENORMALS[CUBEFILLED[i][1][0]]);
				mBufferNormalsInv.put(CUBENORMALS[CUBEFILLED[i][2][0]]);
			}
		}
		mBufferVertices.position(0);
		mBufferNormals.position(0);
		mBufferNormalsInv.position(0);

		mBufferVertices.position(0);
		mBufferNormals.position(0);
		mBufferNormalsInv.position(0);
	}

	public static ByteBuffer getNormals() {
		return mBufferNormals;
	}

	public static ByteBuffer getNormalsInv() {
		return mBufferNormalsInv;
	}

	public static ByteBuffer getVertices() {
		return mBufferVertices;
	}

	private final float[] mBoundingSphere = new float[4];
	private final float[] mColor = new float[3];
	private final float[] mMatrixModel = new float[16];
	private final float[] mMatrixRotate = new float[16];
	private final float[] mMatrixScale = new float[16];
	private final float[] mMatrixTranslate = new float[16];
	private boolean mRecalculate;

	public CubismCube() {
		Matrix.setIdentityM(mMatrixRotate, 0);
		Matrix.setIdentityM(mMatrixScale, 0);
		Matrix.setIdentityM(mMatrixTranslate, 0);
		Matrix.setIdentityM(mMatrixModel, 0);
		mBoundingSphere[3] = SQRT_2;
	}

	public float[] getBoundingSphere() {
		return mBoundingSphere;
	}

	public float[] getColor() {
		return mColor;
	}

	public float[] getModelM() {
		if (mRecalculate) {
			Matrix.multiplyMM(mMatrixModel, 0, mMatrixRotate, 0, mMatrixScale,
					0);
			Matrix.multiplyMM(mMatrixModel, 0, mMatrixTranslate, 0,
					mMatrixModel, 0);
			mRecalculate = false;
		}
		return mMatrixModel;
	}

	public void setColor(float r, float g, float b) {
		mColor[0] = r;
		mColor[1] = g;
		mColor[2] = b;
	}

	public void setRotate(float rx, float ry, float rz) {
		CubismUtils.setRotateM(mMatrixRotate, rx, ry, rz);
		mRecalculate = true;
	}

	public void setScale(float scale) {
		Matrix.setIdentityM(mMatrixScale, 0);
		Matrix.scaleM(mMatrixScale, 0, scale, scale, scale);
		mBoundingSphere[3] = SQRT_2 * scale;
		mRecalculate = true;
	}

	public void setTranslate(float tx, float ty, float tz) {
		Matrix.setIdentityM(mMatrixTranslate, 0);
		Matrix.translateM(mMatrixTranslate, 0, tx, ty, tz);
		mBoundingSphere[0] = tx;
		mBoundingSphere[1] = ty;
		mBoundingSphere[2] = tz;
		mRecalculate = true;
	}

}
