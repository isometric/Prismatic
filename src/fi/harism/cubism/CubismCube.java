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

	static {
		// Vertex and normal data plus indices arrays.
		final byte[][] CUBEVERTICES = { { -1, 1, 1 }, { -1, -1, 1 },
				{ 1, 1, 1 }, { 1, -1, 1 }, { -1, 1, -1 }, { -1, -1, -1 },
				{ 1, 1, -1 }, { 1, -1, -1 } };
		final byte[][] CUBENORMALS = { { 0, 0, 1 }, { 0, 0, -1 }, { -1, 0, 0 },
				{ 1, 0, 0 }, { 0, 1, 0 }, { 0, -1, 0 } };
		final int[][][] CUBEFILLED = { { { 0, 1, 2, 1, 3, 2 }, { 0 }, { 1 } },
				{ { 6, 7, 4, 7, 5, 4 }, { 1 }, { 0 } },
				{ { 0, 4, 1, 4, 5, 1 }, { 2 }, { 3 } },
				{ { 3, 7, 2, 7, 6, 2 }, { 3 }, { 2 } },
				{ { 4, 0, 6, 0, 2, 6 }, { 4 }, { 5 } },
				{ { 1, 5, 3, 5, 7, 3 }, { 5 }, { 4 } } };

		// Generate cube buffer.
		mBufferVertices = ByteBuffer.allocateDirect(3 * 6 * 6);
		mBufferNormals = ByteBuffer.allocateDirect(3 * 6 * 6);
		mBufferNormalsInv = ByteBuffer.allocateDirect(3 * 6 * 6);
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

	private final float[] mMatrixModel = new float[16];
	private final float[] mMatrixModelView = new float[16];
	private final float[] mMatrixRotate = new float[16];
	private final float[] mMatrixScale = new float[16];
	private final float[] mMatrixTranslate = new float[16];
	private final float[] mMatrixView = new float[16];

	public CubismCube() {
		Matrix.setIdentityM(mMatrixRotate, 0);
		Matrix.setIdentityM(mMatrixScale, 0);
		Matrix.setIdentityM(mMatrixTranslate, 0);
		Matrix.setIdentityM(mMatrixModel, 0);
	}

	public void calculate() {
		Matrix.multiplyMM(mMatrixModel, 0, mMatrixRotate, 0, mMatrixScale, 0);
		Matrix.multiplyMM(mMatrixModel, 0, mMatrixTranslate, 0, mMatrixModel, 0);
		Matrix.multiplyMM(mMatrixModelView, 0, mMatrixModel, 0, mMatrixView, 0);
	}

	public float[] getModelM() {
		Matrix.multiplyMM(mMatrixModel, 0, mMatrixRotate, 0, mMatrixScale, 0);
		Matrix.multiplyMM(mMatrixModel, 0, mMatrixTranslate, 0, mMatrixModel, 0);
		return mMatrixModel;
	}

	public void setLookAt(float lx, float ly, float lz) {
		Matrix.setLookAtM(mMatrixView, 0, lx, ly, lz, 0f, 0f, 0f, 0f, 1f, 0f);
	}

	public void setRotate(float rx, float ry, float rz) {
		CubismMatrix.setRotateM(mMatrixRotate, rx, ry, rz);
	}

	public void setScale(float scale) {
		Matrix.setIdentityM(mMatrixScale, 0);
		Matrix.scaleM(mMatrixScale, 0, scale, scale, scale);
	}

	public void setTranslate(float tx, float ty, float tz) {
		Matrix.setIdentityM(mMatrixTranslate, 0);
		Matrix.translateM(mMatrixTranslate, 0, tx, ty, tz);
	}

}
