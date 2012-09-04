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

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import android.content.Context;
import android.media.MediaPlayer;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

public final class CubismRenderer implements GLSurfaceView.Renderer {

	private final AnimationRunnable mAnimationRunnable = new AnimationRunnable();
	private ByteBuffer mBufferQuad;
	private Context mContext;
	private final CubismFbo mFboBloom = new CubismFbo();
	private final CubismFbo mFboMain = new CubismFbo();
	private final CubismFbo mFboShadowMap = new CubismFbo();
	private final float[] mMatrixLightPerspective = new float[16];
	private final float[] mMatrixPerspective = new float[16];
	private MediaPlayer mMediaPlayer;
	private final float[] mPlanes = new float[24];
	private final CubismScene mScene = new CubismScene();
	private final CubismShader mShaderBloom1 = new CubismShader();
	private final CubismShader mShaderBloom2 = new CubismShader();
	private final CubismShader mShaderBloom3 = new CubismShader();
	private final boolean[] mShaderCompilerSupport = new boolean[1];
	private final CubismShader mShaderDepth = new CubismShader();
	private final CubismShader mShaderMain = new CubismShader();
	private final CubismCube mSkybox = new CubismCube();
	private int mWidth, mHeight;

	public CubismRenderer(Context context, MediaPlayer mediaPlayer) {
		mMediaPlayer = mediaPlayer;
		mContext = context;

		// Create full scene quad buffer.
		final byte FULL_QUAD_COORDS[] = { -1, 1, -1, -1, 1, 1, 1, -1 };
		mBufferQuad = ByteBuffer.allocateDirect(4 * 2);
		mBufferQuad.put(FULL_QUAD_COORDS).position(0);

		mSkybox.setScale(-6f);
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

	@Override
	public void onDrawFrame(GL10 unused) {
		if (mAnimationRunnable.mRunning) {
			synchronized (mAnimationRunnable.mLock) {
				try {
					mAnimationRunnable.mLock.wait();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}

		final float[] viewRotationM = new float[16];

		// Render shadow map forward.
		mFboShadowMap.bindTexture(GLES20.GL_TEXTURE_CUBE_MAP_NEGATIVE_Z, 0);
		GLES20.glClear(GLES20.GL_DEPTH_BUFFER_BIT);
		Matrix.setIdentityM(viewRotationM, 0);
		renderShadowMap(viewRotationM);

		// Render shadow map right.
		mFboShadowMap.bindTexture(GLES20.GL_TEXTURE_CUBE_MAP_POSITIVE_X, 0);
		GLES20.glClear(GLES20.GL_DEPTH_BUFFER_BIT);
		CubismUtils.setRotateM(viewRotationM, 0f, -90f, 0f);
		renderShadowMap(viewRotationM);

		// Render shadow map back.
		mFboShadowMap.bindTexture(GLES20.GL_TEXTURE_CUBE_MAP_POSITIVE_Z, 0);
		GLES20.glClear(GLES20.GL_DEPTH_BUFFER_BIT);
		CubismUtils.setRotateM(viewRotationM, 0f, 180f, 0f);
		renderShadowMap(viewRotationM);

		// Render shadow map left.
		mFboShadowMap.bindTexture(GLES20.GL_TEXTURE_CUBE_MAP_NEGATIVE_X, 0);
		GLES20.glClear(GLES20.GL_DEPTH_BUFFER_BIT);
		CubismUtils.setRotateM(viewRotationM, 0f, 90f, 0f);
		renderShadowMap(viewRotationM);

		// Render shadow map down.
		mFboShadowMap.bindTexture(GLES20.GL_TEXTURE_CUBE_MAP_NEGATIVE_Y, 0);
		GLES20.glClear(GLES20.GL_DEPTH_BUFFER_BIT);
		CubismUtils.setRotateM(viewRotationM, -90f, 0f, 180f);
		renderShadowMap(viewRotationM);

		// Render shadow map up.
		mFboShadowMap.bindTexture(GLES20.GL_TEXTURE_CUBE_MAP_POSITIVE_Y, 0);
		GLES20.glClear(GLES20.GL_DEPTH_BUFFER_BIT);
		CubismUtils.setRotateM(viewRotationM, 90f, 0f, 180f);
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
		CubismUtils.setPerspectiveM(mMatrixPerspective, 45f, aspectR, .1f, 40f);
		CubismUtils.setPerspectiveM(mMatrixLightPerspective, 90f, 1f, .1f, 40f);

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

		GLES20.glUniform3fv(uLightPos, 1, mScene.getLightPosition(), 0);

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

		GLES20.glUniformMatrix4fv(uViewM, 1, false, mScene.getViewM(), 0);
		GLES20.glUniformMatrix4fv(uProjM, 1, false, mMatrixPerspective, 0);

		GLES20.glUniform3f(uColor, .4f, .6f, 1f);

		final float[] viewProjM = new float[16];
		Matrix.multiplyMM(viewProjM, 0, mMatrixPerspective, 0,
				mScene.getViewM(), 0);

		CubismVisibility.extractPlanes(viewProjM, mPlanes);

		CubismCube[] cubes = mScene.getCubes();
		for (int i = 0; i < cubes.length; ++i) {
			if (CubismVisibility.intersects(mPlanes,
					cubes[i].getBoundingSphere())) {
				GLES20.glUniformMatrix4fv(uModelM, 1, false,
						cubes[i].getModelM(), 0);
				GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, 6 * 6);
			}
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

		final float[] viewM = new float[16];
		final float[] viewProjM = new float[16];

		Matrix.multiplyMM(viewM, 0, viewRotateM, 0, mScene.getLightViewM(), 0);
		Matrix.multiplyMM(viewProjM, 0, mMatrixLightPerspective, 0, viewM, 0);

		GLES20.glUniformMatrix4fv(uViewM, 1, false, viewM, 0);
		GLES20.glUniformMatrix4fv(uProjM, 1, false, mMatrixLightPerspective, 0);

		GLES20.glVertexAttribPointer(aPosition, 3, GLES20.GL_BYTE, false, 0,
				CubismCube.getVertices());
		GLES20.glEnableVertexAttribArray(aPosition);

		GLES20.glEnable(GLES20.GL_CULL_FACE);
		GLES20.glEnable(GLES20.GL_DEPTH_TEST);

		CubismVisibility.extractPlanes(viewProjM, mPlanes);

		CubismCube[] cubes = mScene.getCubes();
		for (int i = 0; i < cubes.length; ++i) {
			if (CubismVisibility.intersects(mPlanes,
					cubes[i].getBoundingSphere())) {
				GLES20.glUniformMatrix4fv(uModelM, 1, false,
						cubes[i].getModelM(), 0);
				GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, 6 * 6);
			}
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
		private boolean mRunning;
		private boolean mStop;

		@Override
		public void run() {
			while (!mStop) {
				mRunning = true;
				mScene.animate(mMediaPlayer.getCurrentPosition() / 1000f);
				mRunning = false;
				synchronized (mLock) {
					try {
						mLock.notifyAll();
						mLock.wait();
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		}

	}

}
