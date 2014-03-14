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

package com.github.andromeduck.prismatic;

import android.opengl.Matrix;
import android.util.FloatMath;

public final class CubismUtils {

	public static void interpolateV(float[] out, float[] p0, float[] p1, float t) {
		int len = Math.min(p0.length, p1.length);
		len = Math.min(len, out.length);
		for (int i = 0; i < len; ++i) {
			out[i] = p0[i] + (p1[i] - p0[i]) * t;
		}
	}

	public static void interpolateV(float[] out, float[] p0, float[] p1,
			float[] p2, float t) {
		int len = Math.min(p0.length, p1.length);
		len = Math.min(len, p2.length);
		len = Math.min(len, out.length);

		float t0 = (1.0f - t) * (1.0f - t);
		float t1 = 2.0f * (1.0f - t) * t;
		float t2 = t * t;

		for (int i = 0; i < len; ++i) {
			out[i] = t0 * p0[i] + t1 * p1[i] + t2 * p2[i];
		}
	}

	/**
	 * Fast inverse-transpose matrix calculation. See
	 * http://content.gpwiki.org/index.php/MathGem:Fast_Matrix_Inversion for
	 * more information. Only difference is that we do transpose at the same
	 * time and therefore we don't transpose upper-left 3x3 matrix leaving it
	 * intact. Also T is written into lowest row of destination matrix instead
	 * of last column.
	 * 
	 * @param dst
	 *            Destination matrix
	 * @param dstOffset
	 *            Destination matrix offset
	 * @param src
	 *            Source matrix
	 * @param srcOffset
	 *            Source matrix offset
	 */
	public static void invTransposeM(float[] dst, float[] src) {
		android.opengl.Matrix.setIdentityM(dst, 0);

		// Copy top-left 3x3 matrix into dst matrix.
		dst[0] = src[0];
		dst[1] = src[1];
		dst[2] = src[2];
		dst[4] = src[4];
		dst[5] = src[5];
		dst[6] = src[6];
		dst[8] = src[8];
		dst[9] = src[9];
		dst[10] = src[10];

		// Calculate -(Ri dot T) into last row.
		dst[3] = -(src[0] * src[12] + src[1] * src[13] + src[2] * src[14]);
		dst[7] = -(src[4] * src[12] + src[5] * src[13] + src[6] * src[14]);
		dst[11] = -(src[8] * src[12] + src[9] * src[13] + src[10] * src[14]);
	}

	public static void setExtrudeM(float[] m, float fovy, float aspect,
			float zNear) {

		Matrix.setIdentityM(m, 0);
		float h = zNear * (float) Math.tan(fovy * Math.PI / 360);
		float w = h * aspect;

		m[0] = zNear / w;
		m[5] = zNear / h;
		m[10] = 0.001f - 1;
		m[11] = -1;
		m[14] = zNear * (0.001f - 2);
		m[15] = 0;
	}

	/**
	 * Initializes given matrix as perspective projection matrix.
	 * 
	 * @param m
	 *            Matrix for writing, should be float[16], or bigger.
	 * @param fovy
	 *            Field of view in degrees.
	 * @param aspect
	 *            Aspect ratio.
	 * @param zNear
	 *            Near clipping plane.
	 * @param zFar
	 *            Far clipping plane.
	 */
	public static void setPerspectiveM(float[] m, float fovy, float aspect,
			float zNear, float zFar) {

		Matrix.setIdentityM(m, 0);
		float h = zNear * (float) Math.tan(fovy * Math.PI / 360);
		float w = h * aspect;
		float d = zFar - zNear;

		m[0] = zNear / w;
		m[5] = zNear / h;
		m[10] = -(zFar + zNear) / d;
		m[11] = -1;
		m[14] = (-2 * zNear * zFar) / d;
		m[15] = 0;
	}

	/**
	 * Calculates rotation matrix into given matrix array.
	 * 
	 * @param m
	 *            Matrix float array
	 * @param offset
	 *            Matrix start offset
	 * @param rx
	 *            Rotation around x axis
	 * @param ry
	 *            Rotation around y axis
	 * @param rz
	 *            Rotation around z axis
	 */
	public static void setRotateM(float[] m, float rx, float ry, float rz) {
		float toRadians = (float) (Math.PI * 2 / 360);
		rx *= toRadians;
		ry *= toRadians;
		rz *= toRadians;
		float sin0 = FloatMath.sin(rx);
		float cos0 = FloatMath.cos(rx);
		float sin1 = FloatMath.sin(ry);
		float cos1 = FloatMath.cos(ry);
		float sin2 = FloatMath.sin(rz);
		float cos2 = FloatMath.cos(rz);

		android.opengl.Matrix.setIdentityM(m, 0);

		float sin1_cos2 = sin1 * cos2;
		float sin1_sin2 = sin1 * sin2;

		m[0] = cos1 * cos2;
		m[1] = cos1 * sin2;
		m[2] = -sin1;

		m[4] = (-cos0 * sin2) + (sin0 * sin1_cos2);
		m[5] = (cos0 * cos2) + (sin0 * sin1_sin2);
		m[6] = sin0 * cos1;

		m[8] = (sin0 * sin2) + (cos0 * sin1_cos2);
		m[9] = (-sin0 * cos2) + (cos0 * sin1_sin2);
		m[10] = cos0 * cos1;
	}

}
