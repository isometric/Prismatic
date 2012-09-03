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
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Comparator;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import android.content.Context;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.opengl.Visibility;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.widget.Toast;

public class CubismRenderer implements GLSurfaceView.Renderer {

	private static final int CUBE_DIV = 10;
	private static final int CUBE_SZ = CUBE_DIV * CUBE_DIV * 2 + CUBE_DIV
			* (CUBE_DIV - 2) * 2 + (CUBE_DIV - 2) * (CUBE_DIV - 2) * 2;

	private final AnimationRunnable mAnimationRunnable = new AnimationRunnable();
	private long mAnimationStart;
	private int mAnimationStep;
	private ByteBuffer mBufferQuad;
	private Context mContext;
	private StructCube[] mCubes;
	private float[] mCubesBoundingSpheres;
	private final CubismFbo mFboBloom = new CubismFbo();
	private final CubismFbo mFboMain = new CubismFbo();
	private final CubismFbo mFboShadowMap = new CubismFbo();
	private final CubismShader mShaderBloom1 = new CubismShader();
	private final CubismShader mShaderBloom2 = new CubismShader();
	private final CubismShader mShaderBloom3 = new CubismShader();
	private final boolean[] mShaderCompilerSupport = new boolean[1];
	private final CubismShader mShaderDepth = new CubismShader();
	private final CubismShader mShaderMain = new CubismShader();
	private final CubismCube mSkybox = new CubismCube();
	private int mWidth, mHeight;

	public CubismRenderer(Context context) {
		mContext = context;

		// Create full scene quad buffer.
		final byte FULL_QUAD_COORDS[] = { -1, 1, -1, -1, 1, 1, 1, -1 };
		mBufferQuad = ByteBuffer.allocateDirect(4 * 2);
		mBufferQuad.put(FULL_QUAD_COORDS).position(0);

		mSkybox.setScale(-6f);

		int idx = 0;
		mCubes = new StructCube[CUBE_SZ];
		mCubesBoundingSpheres = new float[CUBE_SZ * 4];
		for (int x = 0; x < CUBE_DIV; ++x) {
			for (int y = 0; y < CUBE_DIV; ++y) {
				for (int z = 0; z < CUBE_DIV;) {
					mCubes[idx] = new StructCube();
					mCubes[idx].mCube.setScale(1f / (CUBE_DIV - 1));

					float t = 2f / (CUBE_DIV - 1);
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

					if (x > 0 && y > 0 && x < CUBE_DIV - 1 && y < CUBE_DIV - 1) {
						z += CUBE_DIV - 1;
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
		InputStream is = mContext.getResources().openRawResource(rawId);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		byte[] buf = new byte[1024];
		int len;
		while ((len = is.read(buf)) != -1) {
			baos.write(buf, 0, len);
		}
		return baos.toString();
	}

	/**
	 * Calculates scene animation.
	 */
	private synchronized void onAnimateScene() {
		long time = SystemClock.uptimeMillis();
		long timeDiff = time - mAnimationStart;

		/**
		 * Camera and light movement.
		 */
		float tCamera = 0f;
		switch (mAnimationStep) {
		case 0: {
			for (int i = 0; i < 3; ++i) {
				Globals.mCameraPosTarget[i] = (float) (Math.random() * 6 - 3);
				Globals.mCameraPosTarget[i] += Globals.mCameraPosTarget[i] > 0 ? 3
						: -3;
				Globals.mLightPosTarget[i] = Globals.mCameraPosTarget[i]
						* (float) (.5 + Math.random());
			}
		}
		case 1: {
			for (int i = 0; i < 3; ++i) {
				Globals.mCameraPosSource[i] = Globals.mCameraPosTarget[i];
				Globals.mCameraPosTarget[i] = (float) (Math.random() * 6 - 3);
				Globals.mCameraPosTarget[i] += Globals.mCameraPosTarget[i] > 0 ? 3
						: -3;

				Globals.mLightPosSource[i] = Globals.mLightPosTarget[i];
				Globals.mLightPosTarget[i] = Globals.mCameraPosTarget[i]
						* (float) (.5 + Math.random());
			}

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
			mAnimationStep = 2;
			break;
		}
		case 2: {
			if (timeDiff < 7600) {
				tCamera = timeDiff / 7600f;
				tCamera = tCamera * tCamera * (3 - 2 * tCamera);
				break;
			}

			for (int i = 0; i < 3; ++i) {
				Globals.mCameraPosSource[i] = (float) (Math.random() * 6 - 3);
				Globals.mCameraPosSource[i] += Globals.mCameraPosSource[i] > 0 ? 3
						: -3;
				Globals.mCameraPosTarget[i] = (float) (Math.random() * 6 - 3);
				Globals.mCameraPosTarget[i] += Globals.mCameraPosTarget[i] > 0 ? 3
						: -3;

				Globals.mLightPosSource[i] = Globals.mCameraPosSource[i]
						* (float) (.5 + Math.random());
				Globals.mLightPosTarget[i] = Globals.mCameraPosTarget[i]
						* (float) (.5 + Math.random());
			}

			mAnimationStep = 3;
		}
		case 3: {
			tCamera = (timeDiff - 7600) / (11200f - 7600f);
			tCamera = tCamera * tCamera * (3 - 2 * tCamera);
			break;
		}
		}

		interpolateV(Globals.mCameraPos, Globals.mCameraPosSource,
				Globals.mCameraPosTarget, tCamera);
		interpolateV(Globals.mLightPos, Globals.mLightPosSource,
				Globals.mLightPosTarget, tCamera);

		Matrix.setLookAtM(Globals.mMatrixView, 0, Globals.mCameraPos[0],
				Globals.mCameraPos[1], Globals.mCameraPos[2], 0f, 0f, 0f, 0f,
				1f, 0f);
		Matrix.setIdentityM(Globals.mMatrixLightView, 0);
		Matrix.translateM(Globals.mMatrixLightView, 0, -Globals.mLightPos[0],
				-Globals.mLightPos[1], -Globals.mLightPos[2]);

		/**
		 * Big cube "explosion".
		 */
		float tExplode = 0f;
		if (timeDiff > 2000 && timeDiff <= 9000) {
			tExplode = (timeDiff - 2000) / 7000f;
		} else if (timeDiff > 9000) {
			tExplode = 1f - (timeDiff - 9000) / 2200f;
		}
		if (tExplode > 1)
			tExplode = 1;
		if (tExplode < 0)
			tExplode = 0;

		for (int i = 0; i < mCubes.length; ++i) {
			float t = 1f - (float) i / mCubes.length;
			t = tExplode * t;
			t = t * t * (3 - 2 * t);

			StructCube structCube = mCubes[i];

			interpolateV(structCube.mPosition, structCube.mPositionSource,
					structCube.mPositionTarget, t);
			interpolateV(structCube.mRotation, structCube.mRotationSource,
					structCube.mRotationTarget, t);

			structCube.mCube.setTranslate(structCube.mPosition[0],
					structCube.mPosition[1], structCube.mPosition[2]);
			structCube.mCube.setRotate(structCube.mRotation[0],
					structCube.mRotation[1], structCube.mRotation[2]);

			final float[] cubeVec = { 0, 0, 0, 1f };
			Matrix.multiplyMV(mCubesBoundingSpheres, i * 4,
					structCube.mCube.getModelM(), 0, cubeVec, 0);
			mCubesBoundingSpheres[i * 4 + 3] = 1.41421356237f / (CUBE_DIV - 1);
		}
	}

	@Override
	public synchronized void onDrawFrame(GL10 unused) {
		// onAnimateScene();

		final float[] viewRotationM = new float[16];

		// Render shadow map forward.
		mFboShadowMap.bindTexture(GLES20.GL_TEXTURE_CUBE_MAP_NEGATIVE_Z, 0);
		GLES20.glClear(GLES20.GL_DEPTH_BUFFER_BIT);
		Matrix.setIdentityM(viewRotationM, 0);
		renderShadowMap(viewRotationM);

		// Render shadow map right.
		mFboShadowMap.bindTexture(GLES20.GL_TEXTURE_CUBE_MAP_POSITIVE_X, 0);
		GLES20.glClear(GLES20.GL_DEPTH_BUFFER_BIT);
		CubismMatrix.setRotateM(viewRotationM, 0f, -90f, 0f);
		renderShadowMap(viewRotationM);

		// Render shadow map back.
		mFboShadowMap.bindTexture(GLES20.GL_TEXTURE_CUBE_MAP_POSITIVE_Z, 0);
		GLES20.glClear(GLES20.GL_DEPTH_BUFFER_BIT);
		CubismMatrix.setRotateM(viewRotationM, 0f, 180f, 0f);
		renderShadowMap(viewRotationM);

		// Render shadow map left.
		mFboShadowMap.bindTexture(GLES20.GL_TEXTURE_CUBE_MAP_NEGATIVE_X, 0);
		GLES20.glClear(GLES20.GL_DEPTH_BUFFER_BIT);
		CubismMatrix.setRotateM(viewRotationM, 0f, 90f, 0f);
		renderShadowMap(viewRotationM);

		// Render shadow map down.
		mFboShadowMap.bindTexture(GLES20.GL_TEXTURE_CUBE_MAP_NEGATIVE_Y, 0);
		GLES20.glClear(GLES20.GL_DEPTH_BUFFER_BIT);
		CubismMatrix.setRotateM(viewRotationM, -90f, 0f, 180f);
		renderShadowMap(viewRotationM);

		// Render shadow map up.
		mFboShadowMap.bindTexture(GLES20.GL_TEXTURE_CUBE_MAP_POSITIVE_Y, 0);
		GLES20.glClear(GLES20.GL_DEPTH_BUFFER_BIT);
		CubismMatrix.setRotateM(viewRotationM, 90f, 0f, 180f);
		renderShadowMap(viewRotationM);

		// Render final scene.
		boolean renderBloom = true;
		if (renderBloom) {
			mFboMain.bindTexture(GLES20.GL_TEXTURE_2D, 0);
			GLES20.glClear(GLES20.GL_DEPTH_BUFFER_BIT);
			renderCombine();
			renderBloom();
		} else {
			GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);
			GLES20.glViewport(0, 0, mWidth, mHeight);
			GLES20.glClear(GLES20.GL_DEPTH_BUFFER_BIT);
			renderCombine();
		}

		synchronized (mAnimationRunnable.mLock) {
			mAnimationRunnable.mLock.notifyAll();
		}
	}

	public void onMusicRepeat() {
		mAnimationStep = 1;
	}

	public void onPause() {
		mAnimationRunnable.mStop = true;
		synchronized (mAnimationRunnable.mLock) {
			mAnimationRunnable.mLock.notifyAll();
		}
	}

	public void onResume() {
		mAnimationRunnable.mStop = false;
		Thread t = new Thread(mAnimationRunnable);
		t.setPriority(Thread.MAX_PRIORITY);
		t.start();
	}

	@Override
	public void onSurfaceChanged(GL10 unused, int width, int height) {
		mWidth = width;
		mHeight = height;

		float aspectR = (float) mWidth / mHeight;
		CubismMatrix.setPerspectiveM(Globals.mMatrixPerspective, 45f, aspectR,
				.1f, 40f);
		CubismMatrix.setPerspectiveM(Globals.mMatrixLightPerspective, 90f, 1f,
				.1f, 40f);

		mFboShadowMap
				.init(512, 512, GLES20.GL_TEXTURE_CUBE_MAP, 1, true, false);
		mFboBloom.init(mWidth / 4, mHeight / 4, 2);
		mFboMain.init(mWidth, mHeight, GLES20.GL_TEXTURE_2D, 1, true, false);
	}

	@Override
	public void onSurfaceCreated(GL10 unused, EGLConfig config) {
		// Check if shader compiler is supported.
		GLES20.glGetBooleanv(GLES20.GL_SHADER_COMPILER, mShaderCompilerSupport,
				0);

		// If not, show user an error message and return immediately.
		if (mShaderCompilerSupport[0] == false) {
			String msg = mContext.getString(R.string.error_shader_compiler);
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
			vertexSource = loadRawString(R.raw.bloom_vs);
			fragmentSource = loadRawString(R.raw.bloom_pass1_fs);
			mShaderBloom1.setProgram(vertexSource, fragmentSource);
			vertexSource = loadRawString(R.raw.bloom_vs);
			fragmentSource = loadRawString(R.raw.bloom_pass2_fs);
			mShaderBloom2.setProgram(vertexSource, fragmentSource);
			vertexSource = loadRawString(R.raw.bloom_vs);
			fragmentSource = loadRawString(R.raw.bloom_pass3_fs);
			mShaderBloom3.setProgram(vertexSource, fragmentSource);
		} catch (Exception ex) {
			showError(ex.getMessage());
		}

	}

	private void renderBloom() {
		/**
		 * Instantiate variables for bloom filter.
		 */

		// Pixel sizes.
		float blurSizeH = 1f / mFboBloom.getWidth();
		float blurSizeV = 1f / mFboBloom.getHeight();

		// Calculate number of pixels from relative size.
		int numBlurPixelsPerSide = (int) (0.05f * Math.min(
				mFboBloom.getWidth(), mFboBloom.getHeight()));
		if (numBlurPixelsPerSide < 1)
			numBlurPixelsPerSide = 1;
		double sigma = 1.0 + numBlurPixelsPerSide * 0.5;

		// Values needed for incremental gaussian blur.
		double incrementalGaussian1 = 1.0 / (Math.sqrt(2.0 * Math.PI) * sigma);
		double incrementalGaussian2 = Math.exp(-0.5 / (sigma * sigma));
		double incrementalGaussian3 = incrementalGaussian2
				* incrementalGaussian2;

		GLES20.glDisable(GLES20.GL_DEPTH_TEST);

		/**
		 * First pass, blur texture horizontally.
		 */

		mFboBloom.bindTexture(GLES20.GL_TEXTURE_2D, 0);
		mShaderBloom1.useProgram();

		GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
		GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mFboMain.getTexture(0));
		GLES20.glUniform3f(mShaderBloom1.getHandle("uIncrementalGaussian"),
				(float) incrementalGaussian1, (float) incrementalGaussian2,
				(float) incrementalGaussian3);
		GLES20.glUniform1f(mShaderBloom1.getHandle("uNumBlurPixelsPerSide"),
				numBlurPixelsPerSide);
		GLES20.glUniform2f(mShaderBloom1.getHandle("uBlurOffset"), blurSizeH,
				0f);

		GLES20.glVertexAttribPointer(mShaderBloom1.getHandle("aPosition"), 2,
				GLES20.GL_BYTE, false, 0, mBufferQuad);
		GLES20.glEnableVertexAttribArray(mShaderBloom1.getHandle("aPosition"));
		GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);

		/**
		 * Second pass, blur texture vertically.
		 */
		mFboBloom.bindTexture(GLES20.GL_TEXTURE_2D, 1);
		mShaderBloom2.useProgram();

		GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
		GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mFboBloom.getTexture(0));
		GLES20.glUniform3f(mShaderBloom2.getHandle("uIncrementalGaussian"),
				(float) incrementalGaussian1, (float) incrementalGaussian2,
				(float) incrementalGaussian3);
		GLES20.glUniform1f(mShaderBloom2.getHandle("uNumBlurPixelsPerSide"),
				numBlurPixelsPerSide);
		GLES20.glUniform2f(mShaderBloom2.getHandle("uBlurOffset"), 0f,
				blurSizeV);

		GLES20.glVertexAttribPointer(mShaderBloom2.getHandle("aPosition"), 2,
				GLES20.GL_BYTE, false, 0, mBufferQuad);
		GLES20.glEnableVertexAttribArray(mShaderBloom2.getHandle("aPosition"));
		GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);

		/**
		 * Third pass, combine source texture and calculated bloom texture into
		 * output texture.
		 */

		GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);
		GLES20.glViewport(0, 0, mWidth, mHeight);

		mShaderBloom3.useProgram();

		GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
		GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mFboBloom.getTexture(1));
		GLES20.glUniform1i(mShaderBloom3.getHandle("sTextureBloom"), 0);
		GLES20.glActiveTexture(GLES20.GL_TEXTURE1);
		GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mFboMain.getTexture(0));
		GLES20.glUniform1i(mShaderBloom3.getHandle("sTextureSource"), 1);

		GLES20.glVertexAttribPointer(mShaderBloom3.getHandle("aPosition"), 2,
				GLES20.GL_BYTE, false, 0, mBufferQuad);
		GLES20.glEnableVertexAttribArray(mShaderBloom3.getHandle("aPosition"));
		GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);
	}

	public void renderCombine() {
		// Render filled cube.
		mShaderMain.useProgram();
		int uModelM = mShaderMain.getHandle("uModelM");
		int uViewM = mShaderMain.getHandle("uViewM");
		int uProjM = mShaderMain.getHandle("uProjM");
		int uLightPos = mShaderMain.getHandle("uLightPos");
		int uColor = mShaderMain.getHandle("uColor");
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
		GLES20.glBindTexture(GLES20.GL_TEXTURE_CUBE_MAP,
				mFboShadowMap.getTexture(0));

		GLES20.glEnable(GLES20.GL_CULL_FACE);
		GLES20.glEnable(GLES20.GL_DEPTH_TEST);

		GLES20.glUniformMatrix4fv(uViewM, 1, false, Globals.mMatrixView, 0);
		GLES20.glUniformMatrix4fv(uProjM, 1, false, Globals.mMatrixPerspective,
				0);

		GLES20.glUniform3f(uColor, .4f, .6f, 1f);

		final int[] results = new int[CUBE_SZ];
		final float[] viewProjM = new float[16];

		Arrays.fill(results, -1);
		Matrix.multiplyMM(viewProjM, 0, Globals.mMatrixPerspective, 0,
				Globals.mMatrixView, 0);

		Visibility.frustumCullSpheres(viewProjM, 0, mCubesBoundingSpheres, 0,
				CUBE_SZ, results, 0, CUBE_SZ);

		for (int i = 0; i < CUBE_SZ && results[i] >= 0; ++i) {
			GLES20.glUniformMatrix4fv(uModelM, 1, false,
					mCubes[results[i]].mCube.getModelM(), 0);
			GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, 6 * 6);
		}

		GLES20.glUniformMatrix4fv(uModelM, 1, false, mSkybox.getModelM(), 0);
		GLES20.glUniform3f(uColor, .5f, .5f, .5f);

		GLES20.glVertexAttribPointer(aNormal, 3, GLES20.GL_BYTE, false, 0,
				CubismCube.getNormalsInv());
		GLES20.glEnableVertexAttribArray(aNormal);

		GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, 6 * 6);

		GLES20.glDisable(GLES20.GL_CULL_FACE);
	}

	public void renderShadowMap(float[] viewRotateM) {
		// Render filled cube.
		mShaderDepth.useProgram();

		int uModelM = mShaderDepth.getHandle("uModelM");
		int uViewM = mShaderDepth.getHandle("uViewM");
		int uProjM = mShaderDepth.getHandle("uProjM");
		int aPosition = mShaderDepth.getHandle("aPosition");

		final int[] results = new int[CUBE_SZ];
		final float[] viewM = new float[16];
		final float[] viewProjM = new float[16];

		Arrays.fill(results, -1);
		Matrix.multiplyMM(viewM, 0, viewRotateM, 0, Globals.mMatrixLightView, 0);
		Matrix.multiplyMM(viewProjM, 0, Globals.mMatrixLightPerspective, 0,
				viewM, 0);

		GLES20.glUniformMatrix4fv(uViewM, 1, false, viewM, 0);
		GLES20.glUniformMatrix4fv(uProjM, 1, false,
				Globals.mMatrixLightPerspective, 0);

		GLES20.glVertexAttribPointer(aPosition, 3, GLES20.GL_BYTE, false, 0,
				CubismCube.getVertices());
		GLES20.glEnableVertexAttribArray(aPosition);

		GLES20.glEnable(GLES20.GL_CULL_FACE);
		GLES20.glEnable(GLES20.GL_DEPTH_TEST);

		Visibility.frustumCullSpheres(viewProjM, 0, mCubesBoundingSpheres, 0,
				CUBE_SZ, results, 0, CUBE_SZ);

		for (int i = 0; i < CUBE_SZ && results[i] >= 0; ++i) {
			GLES20.glUniformMatrix4fv(uModelM, 1, false,
					mCubes[results[i]].mCube.getModelM(), 0);
			GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, 6 * 6);
		}

		GLES20.glUniformMatrix4fv(uModelM, 1, false, mSkybox.getModelM(), 0);

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
				Toast.makeText(mContext, errorMsg, Toast.LENGTH_LONG).show();
			}
		});
	}

	private class AnimationRunnable implements Runnable {

		private Object mLock = new Object();
		private boolean mStop;

		@Override
		public void run() {
			while (!mStop) {
				onAnimateScene();
				synchronized (mLock) {
					try {
						mLock.wait();
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		}

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

		public final float[] mPosition = new float[3];
		public final float[] mPositionSource = new float[3];
		public final float[] mPositionTarget = new float[3];

		public final float[] mRotation = new float[3];
		public final float[] mRotationSource = new float[3];
		public final float[] mRotationTarget = new float[3];
	}

}
