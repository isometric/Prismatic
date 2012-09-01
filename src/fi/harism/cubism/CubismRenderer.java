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

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Comparator;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.widget.Toast;

public class CubismRenderer implements GLSurfaceView.Renderer {

	private CubismActivity mActivity;
	private long mAnimationStart;
	private final StructCube mCube = new StructCube();
	private final StructCube[] mCubes;
	private CubismFbo mFbo = new CubismFbo();
	private final boolean[] mShaderCompilerSupport = new boolean[1];
	private final CubismShader mShaderDepth = new CubismShader();
	private final CubismShader mShaderMain = new CubismShader();
	private int mWidth, mHeight;

	public CubismRenderer(CubismActivity activity) {
		mActivity = activity;

		mCube.mCube.setScale(-6f);

		int idx = 0;
		int size = 10;
		mCubes = new StructCube[size * size * 2 + size * (size - 2) * 2
				+ (size - 2) * (size - 2) * 2];
		for (int x = 0; x < size; ++x) {
			for (int y = 0; y < size; ++y) {
				for (int z = 0; z < size;) {
					mCubes[idx] = new StructCube();
					mCubes[idx].mCube.setScale(1f / (size - 1));

					float t = 2f / (size - 1);
					float tx = x * t - 1;
					float ty = y * t - 1;
					float tz = z * t - 1;

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

					if (x > 0 && y > 0 && x < size - 1 && y < size - 1) {
						z += size - 1;
					} else {
						++z;
					}
				}
			}
		}

	}

	private void interpolateV(float[] out, float[] src, float[] dst, float t) {
		for (int i = 0; i < 3; ++i) {
			out[i] = src[i] + (dst[i] - src[i]) * t;
		}
	}

	/**
	 * Loads String from raw resources with given id.
	 */
	private String loadRawString(int rawId) throws Exception {
		InputStream is = mActivity.getResources().openRawResource(rawId);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		byte[] buf = new byte[1024];
		int len;
		while ((len = is.read(buf)) != -1) {
			baos.write(buf, 0, len);
		}
		return baos.toString();
	}

	@Override
	public void onDrawFrame(GL10 unused) {
		// Render shadow map.
		mFbo.bind();
		mFbo.bindTexture(0);
		GLES20.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
		GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);
		renderShadowMap();

		// Copy offscreen buffer to screen.
		GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);
		GLES20.glViewport(0, 0, mWidth, mHeight);
		GLES20.glClearColor(0.4f, 0.5f, 0.6f, 1.0f);
		GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);
		renderCombine();

		mActivity.queueGLEvent();
	}

	public void onGLEvent() {
		// Global rotation values.
		long time = SystemClock.uptimeMillis();
		if (mAnimationStart == 0 || time - mAnimationStart > 5900) {
			for (int i = 0; i < 3; ++i) {
				Globals.mCameraPosSource[i] = (float) (Math.random() * 6 - 3);
				Globals.mCameraPosSource[i] += Globals.mCameraPosSource[i] > 0 ? 3
						: -3;
				Globals.mCameraPosTarget[i] = (float) (Math.random() * 6 - 3);
				Globals.mCameraPosTarget[i] += Globals.mCameraPosTarget[i] > 0 ? 3
						: -3;

				Globals.mLightPosSource[i] = Globals.mCameraPosSource[i] * 1.2f;
				Globals.mLightPosTarget[i] = Globals.mCameraPosTarget[i] * 1.2f;
			}
			Globals.mLightPosSource[1] += 1;
			Globals.mLightPosTarget[1] += 1;

			final double sortX = Math.random() * 10 - 5;
			final double sortY = Math.random() * 10 - 5;
			final double sortZ = Math.random() * 10 - 5;
			Arrays.sort(mCubes, new Comparator<StructCube>() {
				@Override
				public int compare(StructCube c0, StructCube c1) {
					double dx0 = c0.mPosition[0] - sortX;
					double dy0 = c0.mPosition[1] - sortY;
					double dz0 = c0.mPosition[2] - sortZ;
					double dx1 = c1.mPosition[0] - sortX;
					double dy1 = c1.mPosition[1] - sortY;
					double dz1 = c1.mPosition[2] - sortZ;
					double dist0 = Math.sqrt(dx0 * dx0 + dy0 * dy0 + dz0 * dz0);
					double dist1 = Math.sqrt(dx1 * dx1 + dy1 * dy1 + dz1 * dz1);
					return dist0 < dist1 ? -1 : 1;
				}
			});

			mAnimationStart = time;
		}

		float t = (time - mAnimationStart) / 11200f;

		interpolateV(Globals.mCameraPos, Globals.mCameraPosSource,
				Globals.mCameraPosTarget, t);
		interpolateV(Globals.mLightPos, Globals.mLightPosSource,
				Globals.mLightPosTarget, t);

		Matrix.setLookAtM(Globals.mMatrixView, 0, Globals.mCameraPos[0],
				Globals.mCameraPos[1], Globals.mCameraPos[2], 0f, 0f, 0f, 0f,
				1f, 0f);
		Matrix.setLookAtM(Globals.mMatrixLightView, 0, Globals.mLightPos[0],
				Globals.mLightPos[1], Globals.mLightPos[2], 0f, 0f, 0f, 0f, 1f,
				0f);

		float rx = ((time % 20000) / 20000f) * 360f;
		float ry = ((time % 22000) / 22000f) * 360f;
		float rz = ((time % 25000) / 25000f) * 360f;

		final float[] rotateM = new float[16];
		CubismMatrix.setRotateM(rotateM, rx, ry, rz);

		for (int i = 0; i < mCubes.length; ++i) {
			float tt = 1f - (float) i / mCubes.length;
			tt = 2 * ((t * t * (3 - 2 * t)) - 0.1f) * (tt * tt * (3 - 2 * tt));
			if (tt < 0) {
				tt = 0;
			} else if (tt > 1) {
				tt = 1;
			}

			StructCube cube = mCubes[i];

			interpolateV(cube.mPosition, cube.mPositionSource,
					cube.mPositionTarget, tt);
			cube.mCube.setTranslate(cube.mPosition[0], cube.mPosition[1],
					cube.mPosition[2]);

			interpolateV(cube.mRotation, cube.mRotationSource,
					cube.mRotationTarget, tt);
			cube.mCube.setRotate(cube.mRotation[0], cube.mRotation[1],
					cube.mRotation[2]);

			Matrix.multiplyMM(cube.mMatrixModel, 0, rotateM, 0,
					cube.mCube.getModelM(), 0);
		}
		System.arraycopy(mCube.mCube.getModelM(), 0, mCube.mMatrixModel, 0, 16);
	}

	public void onMusicRepeat() {
		mAnimationStart = 0;
	}

	@Override
	public void onSurfaceChanged(GL10 unused, int width, int height) {
		mWidth = width;
		mHeight = height;

		float aspectR = (float) mWidth / mHeight;
		CubismMatrix.setPerspectiveM(Globals.mMatrixPerspective, 45f, aspectR,
				.1f, 20f);
		CubismMatrix.setPerspectiveM(Globals.mMatrixLightPerspective, 45f, 1f,
				.1f, 20f);

		mFbo.init(512, 512, 1, true, false);
	}

	@Override
	public void onSurfaceCreated(GL10 unused, EGLConfig config) {
		// Check if shader compiler is supported.
		GLES20.glGetBooleanv(GLES20.GL_SHADER_COMPILER, mShaderCompilerSupport,
				0);

		// If not, show user an error message and return immediately.
		if (mShaderCompilerSupport[0] == false) {
			String msg = mActivity.getString(R.string.error_shader_compiler);
			showError(msg);
			return;
		}

		// Load vertex and fragment shaders.
		try {
			String vertexSource, fragmentSource;
			vertexSource = loadRawString(R.raw.main_vs);
			fragmentSource = loadRawString(R.raw.main_fs);
			mShaderMain.setProgram(vertexSource, fragmentSource);
			vertexSource = loadRawString(R.raw.depth_vs);
			fragmentSource = loadRawString(R.raw.depth_fs);
			mShaderDepth.setProgram(vertexSource, fragmentSource);
		} catch (Exception ex) {
			showError(ex.getMessage());
		}

	}

	public void renderCombine() {
		// Render filled cube.
		mShaderMain.useProgram();
		int uModelM = mShaderMain.getHandle("uModelM");
		int uViewM = mShaderMain.getHandle("uViewM");
		int uProjM = mShaderMain.getHandle("uProjM");
		int uViewLightM = mShaderMain.getHandle("uViewLightM");
		int uProjLightM = mShaderMain.getHandle("uProjLightM");
		int uLightPos = mShaderMain.getHandle("uLightPos");
		int aPosition = mShaderMain.getHandle("aPosition");
		int aNormal = mShaderMain.getHandle("aNormal");

		GLES20.glUniform3fv(uLightPos, 1, Globals.mLightPos, 0);

		GLES20.glVertexAttribPointer(aPosition, 3, GLES20.GL_BYTE, false, 0,
				CubismCube.getVertices());
		GLES20.glEnableVertexAttribArray(aPosition);

		GLES20.glVertexAttribPointer(aNormal, 3, GLES20.GL_BYTE, false, 0,
				CubismCube.getNormals());
		GLES20.glEnableVertexAttribArray(aNormal);

		GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
		GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mFbo.getTexture(0));

		GLES20.glEnable(GLES20.GL_CULL_FACE);
		GLES20.glEnable(GLES20.GL_DEPTH_TEST);

		GLES20.glUniformMatrix4fv(uViewM, 1, false, Globals.mMatrixView, 0);
		GLES20.glUniformMatrix4fv(uProjM, 1, false, Globals.mMatrixPerspective,
				0);
		GLES20.glUniformMatrix4fv(uViewLightM, 1, false,
				Globals.mMatrixLightView, 0);
		GLES20.glUniformMatrix4fv(uProjLightM, 1, false,
				Globals.mMatrixLightPerspective, 0);

		for (StructCube cube : mCubes) {
			GLES20.glUniformMatrix4fv(uModelM, 1, false, cube.mMatrixModel, 0);

			GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, 6 * 6);
		}

		GLES20.glUniformMatrix4fv(uModelM, 1, false, mCube.mMatrixModel, 0);

		GLES20.glVertexAttribPointer(aNormal, 3, GLES20.GL_BYTE, false, 0,
				CubismCube.getNormalsInv());
		GLES20.glEnableVertexAttribArray(aNormal);

		GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, 6 * 6);

		GLES20.glDisable(GLES20.GL_CULL_FACE);
	}

	public void renderShadowMap() {
		// Render filled cube.
		mShaderDepth.useProgram();

		int uModelM = mShaderDepth.getHandle("uModelM");
		int uViewM = mShaderDepth.getHandle("uViewM");
		int uProjM = mShaderDepth.getHandle("uProjM");
		int aPosition = mShaderDepth.getHandle("aPosition");

		GLES20.glUniformMatrix4fv(uViewM, 1, false, Globals.mMatrixLightView, 0);
		GLES20.glUniformMatrix4fv(uProjM, 1, false,
				Globals.mMatrixLightPerspective, 0);

		GLES20.glVertexAttribPointer(aPosition, 3, GLES20.GL_BYTE, false, 0,
				CubismCube.getVertices());
		GLES20.glEnableVertexAttribArray(aPosition);

		GLES20.glEnable(GLES20.GL_CULL_FACE);
		GLES20.glEnable(GLES20.GL_DEPTH_TEST);

		for (StructCube cube : mCubes) {
			GLES20.glUniformMatrix4fv(uModelM, 1, false, cube.mMatrixModel, 0);

			GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, 6 * 6);
		}

		GLES20.glUniformMatrix4fv(uModelM, 1, false, mCube.mMatrixModel, 0);

		GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, 6 * 6);

		GLES20.glDisable(GLES20.GL_CULL_FACE);
	}

	/**
	 * Shows Toast on screen with given message.
	 */
	private void showError(final String errorMsg) {
		new Handler(Looper.getMainLooper()).post(new Runnable() {
			@Override
			public void run() {
				Toast.makeText(mActivity, errorMsg, Toast.LENGTH_LONG).show();
			}
		});
	}

	private static class Globals {
		public static final float[] mCameraPos = new float[3];
		public static final float[] mCameraPosSource = new float[3];
		public static final float[] mCameraPosTarget = new float[3];
		public static final float[] mLightPos = new float[3];
		public static final float[] mLightPosSource = new float[3];
		public static final float[] mLightPosTarget = new float[3];
		public static final float[] mMatrixLightPerspective = new float[16];
		public static final float[] mMatrixLightView = new float[16];
		public static final float[] mMatrixPerspective = new float[16];
		public static final float[] mMatrixView = new float[16];
	}

	private class StructCube {
		public final CubismCube mCube = new CubismCube();
		public final float[] mMatrixModel = new float[16];

		public final float[] mPosition = new float[3];
		public final float[] mPositionSource = new float[3];
		public final float[] mPositionTarget = new float[3];

		public final float[] mRotation = new float[3];
		public final float[] mRotationSource = new float[3];
		public final float[] mRotationTarget = new float[3];
	}

}
