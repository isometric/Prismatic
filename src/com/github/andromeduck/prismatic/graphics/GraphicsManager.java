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

package com.github.andromeduck.prismatic.graphics;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.ByteBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import com.github.andromeduck.prismatic.R;
import com.github.andromeduck.prismatic.graphics.models.Cube;
import com.github.andromeduck.prismatic.levels.CubismModelBasic;
import com.github.andromeduck.prismatic.levels.CubismModelBitmap;
import com.github.andromeduck.prismatic.levels.CubismModelExplosion;
import com.github.andromeduck.prismatic.levels.CubismModelExplosionShadowVolume;
import com.github.andromeduck.prismatic.levels.CubismModelRandom;
import com.github.andromeduck.prismatic.Parser;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.opengl.GLES30;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

public final class GraphicsManager extends GLSurfaceView implements GLSurfaceView.Renderer {

	private final AnimationRunnable mAnimationRunnable = new AnimationRunnable();
	private ByteBuffer mBufferQuad;
	private Context mContext;
    private final FBO mFboCubeMap = new FBO();
    private final FBO mFboFull = new FBO();
    private final FBO mFboQuarter = new FBO();
    private int mInitCounter;
    private final float[] mMatrixExtrude = new float[16];
	private final float[] mMatrixProjection = new float[16];
	private final float[] mMatrixProjectionDepth = new float[16];
	private final float[] mMatrixRotate = new float[16];
	private final float[] mMatrixView = new float[16];
	private final float[] mMatrixViewExtrude = new float[16];
	private final float[] mMatrixViewLight = new float[16];
	private final float[] mMatrixViewProjection = new float[16];
	private MediaPlayer mMediaPlayer;
	private final Model[] mModels;
    private Parser mParser;
    private final Parser.Data mParserData = new Parser.Data();
    private final float[] mPlanes = new float[24];
    private final ShaderManager mShaderBloom1 = new ShaderManager();
    private final ShaderManager mShaderBloom2 = new ShaderManager();
    private final ShaderManager mShaderBloom3 = new ShaderManager();
    private final boolean[] mShaderCompilerSupport = new boolean[1];
    private final ShaderManager mShaderDefault = new ShaderManager();
    private final ShaderManager mShaderDepth = new ShaderManager();
    private final ShaderManager mShaderDepthMap = new ShaderManager();
    private final ShaderManager mShaderStencil = new ShaderManager();
    private final ShaderManager mShaderStencilMask = new ShaderManager();
    private final Cube mSkybox = new Cube();
    private int mWidth, mHeight;

    public GraphicsManager(Context context, Parser parser, MediaPlayer mediaPlayer) {
        super(context);

		mContext = context;
		mParser = parser;
		mMediaPlayer = mediaPlayer;

		setEGLContextClientVersion(3);
		setRenderer(this);
		setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);

		// Create full scene quad buffer.
		final byte FULL_QUAD_COORDS[] = { -1, 1, -1, -1, 1, 1, 1, -1 };
		mBufferQuad = ByteBuffer.allocateDirect(4 * 2);
		mBufferQuad.put(FULL_QUAD_COORDS).position(0);

		mSkybox.setScale(-10f);

		mModels = new Model[8];
        mModels[0] = new CubismModelExplosion(10);
        mModels[1] = new CubismModelExplosion(10);
        mModels[2] = new CubismModelRandom(6);
		mModels[3] = new CubismModelBitmap(BitmapFactory.decodeStream(mContext
				.getResources().openRawResource(R.raw.img_cubism)));
		mModels[4] = new CubismModelBitmap(BitmapFactory.decodeStream(mContext
				.getResources().openRawResource(R.raw.img_harism)));
		mModels[5] = new CubismModelBitmap(BitmapFactory.decodeStream(mContext
                .getResources().openRawResource(R.raw.img_heart)));
		mModels[6] = new CubismModelBitmap(BitmapFactory.decodeStream(mContext
				.getResources().openRawResource(R.raw.img_jinny)));
		mModels[7] = new CubismModelExplosionShadowVolume(6);

		queueEvent(mAnimationRunnable);
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

		/**
		 * Initialize OpenGL context.
		 */
		switch (mInitCounter) {
		case 0: {
			// Check if shader compiler is supported.
			GLES30.glGetBooleanv(GLES30.GL_SHADER_COMPILER,
					mShaderCompilerSupport, 0);

			// If not, show user an error message and return immediately.
			if (!mShaderCompilerSupport[0]) {
				String msg = mContext.getString(R.string.error_shader_compiler);
				showError(msg);
			}
		}
		case 1: {
			GLES30.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
			GLES30.glClear(GLES30.GL_COLOR_BUFFER_BIT);
			if (!mShaderCompilerSupport[0]) {
				mInitCounter = 1;
				return;
			}
			mInitCounter = 2;
			return;
		}
		case 2: {

			// Load vertex and fragment shaders.
			try {
				String vertexSource, fragmentSource;
				vertexSource = loadRawString(R.raw.default_vs);
				fragmentSource = loadRawString(R.raw.default_fs);
				mShaderDefault.setProgram(vertexSource, fragmentSource);
				vertexSource = loadRawString(R.raw.depthmap_vs);
				fragmentSource = loadRawString(R.raw.depthmap_fs);
				mShaderDepthMap.setProgram(vertexSource, fragmentSource);
				vertexSource = loadRawString(R.raw.depth_vs);
				fragmentSource = loadRawString(R.raw.depth_fs);
				mShaderDepth.setProgram(vertexSource, fragmentSource);
				vertexSource = loadRawString(R.raw.stencil_vs);
				fragmentSource = loadRawString(R.raw.stencil_fs);
				mShaderStencil.setProgram(vertexSource, fragmentSource);
				vertexSource = loadRawString(R.raw.stencil_mask_vs);
				fragmentSource = loadRawString(R.raw.stencil_mask_fs);
				mShaderStencilMask.setProgram(vertexSource, fragmentSource);
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
		case 3: {
			float aspectR = (float) mWidth / mHeight;
            MathUtils.setPerspectiveM(mMatrixProjection, 45f, aspectR, .1f,
                    40f);
            MathUtils.setPerspectiveM(mMatrixProjectionDepth, 90f, 1f, .1f,
                    40f);
            MathUtils.setExtrudeM(mMatrixExtrude, 45f, aspectR, .1f);

			mFboCubeMap.init(512, 512, GLES30.GL_TEXTURE_CUBE_MAP, 1, true);
			mFboQuarter.init(mWidth / 4, mHeight / 4, 2);
			mFboFull.init(mWidth, mHeight, GLES30.GL_TEXTURE_2D, 1, true);

			mInitCounter = 4;
		}
		}

		/**
		 * Actual scene rendering.
		 */

		int renderMode = mModels[mParserData.mModelId].getRenderMode();
		switch (renderMode) {
		case Model.MODE_SHADOWMAP: {
			renderDepthMap();

			mFboFull.bindTexture(GLES30.GL_TEXTURE_2D, 0);
			GLES30.glClear(GLES30.GL_DEPTH_BUFFER_BIT);

			renderScene(renderMode);
			renderBloom();
			break;
		}
		case Model.MODE_SHADOWVOLUME: {
			mFboFull.bindTexture(GLES30.GL_TEXTURE_2D, 0);
			GLES30.glClear(GLES30.GL_DEPTH_BUFFER_BIT
					| GLES30.GL_STENCIL_BUFFER_BIT);

			renderScene(renderMode);
			renderShadowStencil();

			GLES30.glEnable(GLES30.GL_STENCIL_TEST);
			GLES30.glEnable(GLES30.GL_BLEND);
			GLES30.glStencilFunc(GLES30.GL_NOTEQUAL, 0x00, 0xFF);
			GLES30.glBlendFunc(GLES30.GL_SRC_ALPHA,
					GLES30.GL_ONE_MINUS_SRC_ALPHA);

			mShaderStencilMask.useProgram();
			GLES30.glVertexAttribPointer(
					mShaderStencilMask.getHandle("aPosition"), 2,
					GLES30.GL_BYTE, false, 0, mBufferQuad);
			GLES30.glEnableVertexAttribArray(mShaderStencilMask
					.getHandle("aPosition"));
			GLES30.glDrawArrays(GLES30.GL_TRIANGLE_STRIP, 0, 4);

			GLES30.glDisable(GLES30.GL_STENCIL_TEST);
			GLES30.glDisable(GLES30.GL_BLEND);

			renderBloom();
			break;
		}
		}

		queueEvent(mAnimationRunnable);
	}

	@Override
	public void onSurfaceChanged(GL10 unused, int width, int height) {
		mWidth = width;
		mHeight = height;

		if (mInitCounter > 3) {
			mInitCounter = 3;
		}
	}

	@Override
	public void onSurfaceCreated(GL10 unused, EGLConfig config) {
		mInitCounter = 0;
	}

	private void renderBloom() {
		/**
		 * Instantiate variables for bloom filter.
		 */

		// Pixel sizes.
		float blurSizeH = 1f / mFboQuarter.getWidth();
		float blurSizeV = 1f / mFboQuarter.getHeight();

		// Calculate number of pixels from relative size.
		int numBlurPixelsPerSide = (int) (0.05f * Math.min(
				mFboQuarter.getWidth(), mFboQuarter.getHeight()));
		if (numBlurPixelsPerSide < 1)
			numBlurPixelsPerSide = 1;
		double sigma = 1.0 + numBlurPixelsPerSide * 0.5;

		// Values needed for incremental gaussian blur.
		double incrementalGaussian1 = 1.0 / (Math.sqrt(2.0 * Math.PI) * sigma);
		double incrementalGaussian2 = Math.exp(-0.5 / (sigma * sigma));
		double incrementalGaussian3 = incrementalGaussian2
				* incrementalGaussian2;

		GLES30.glDisable(GLES30.GL_DEPTH_TEST);

		/**
		 * First pass, blur texture horizontally.
		 */

		mFboQuarter.bindTexture(GLES30.GL_TEXTURE_2D, 0);
		mShaderBloom1.useProgram();

		GLES30.glActiveTexture(GLES30.GL_TEXTURE0);
		GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, mFboFull.getTexture(0));
		GLES30.glUniform3f(mShaderBloom1.getHandle("uIncrementalGaussian"),
				(float) incrementalGaussian1, (float) incrementalGaussian2,
				(float) incrementalGaussian3);
		GLES30.glUniform1f(mShaderBloom1.getHandle("uNumBlurPixelsPerSide"),
				numBlurPixelsPerSide);
		GLES30.glUniform2f(mShaderBloom1.getHandle("uBlurOffset"), blurSizeH,
				0f);

		GLES30.glVertexAttribPointer(mShaderBloom1.getHandle("aPosition"), 2,
				GLES30.GL_BYTE, false, 0, mBufferQuad);
		GLES30.glEnableVertexAttribArray(mShaderBloom1.getHandle("aPosition"));
		GLES30.glDrawArrays(GLES30.GL_TRIANGLE_STRIP, 0, 4);

		/**
		 * Second pass, blur texture vertically.
		 */
		mFboQuarter.bindTexture(GLES30.GL_TEXTURE_2D, 1);
		mShaderBloom2.useProgram();

		GLES30.glActiveTexture(GLES30.GL_TEXTURE0);
		GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, mFboQuarter.getTexture(0));
		GLES30.glUniform3f(mShaderBloom2.getHandle("uIncrementalGaussian"),
				(float) incrementalGaussian1, (float) incrementalGaussian2,
				(float) incrementalGaussian3);
		GLES30.glUniform1f(mShaderBloom2.getHandle("uNumBlurPixelsPerSide"),
				numBlurPixelsPerSide);
		GLES30.glUniform2f(mShaderBloom2.getHandle("uBlurOffset"), 0f,
				blurSizeV);

		GLES30.glVertexAttribPointer(mShaderBloom2.getHandle("aPosition"), 2,
				GLES30.GL_BYTE, false, 0, mBufferQuad);
		GLES30.glEnableVertexAttribArray(mShaderBloom2.getHandle("aPosition"));
		GLES30.glDrawArrays(GLES30.GL_TRIANGLE_STRIP, 0, 4);

		/**
		 * Third pass, combine source texture and calculated bloom texture into
		 * output texture.
		 */

		GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER, 0);
		GLES30.glViewport(0, 0, mWidth, mHeight);

		mShaderBloom3.useProgram();

		GLES30.glActiveTexture(GLES30.GL_TEXTURE0);
		GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, mFboQuarter.getTexture(1));
		GLES30.glUniform1i(mShaderBloom3.getHandle("sTextureBloom"), 0);
		GLES30.glActiveTexture(GLES30.GL_TEXTURE1);
		GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, mFboFull.getTexture(0));
		GLES30.glUniform1i(mShaderBloom3.getHandle("sTextureSource"), 1);
		GLES30.glUniform4fv(mShaderBloom3.getHandle("uForegroundColor"), 1,
				mParserData.mForegroundColor, 0);

		GLES30.glVertexAttribPointer(mShaderBloom3.getHandle("aPosition"), 2,
				GLES30.GL_BYTE, false, 0, mBufferQuad);
		GLES30.glEnableVertexAttribArray(mShaderBloom3.getHandle("aPosition"));
		GLES30.glDrawArrays(GLES30.GL_TRIANGLE_STRIP, 0, 4);
	}

	public void renderDepthMap() {
		// Render shadow map forward.
		mFboCubeMap.bindTexture(GLES30.GL_TEXTURE_CUBE_MAP_NEGATIVE_Z, 0);
		GLES30.glClear(GLES30.GL_DEPTH_BUFFER_BIT);
        MathUtils.setRotateM(mMatrixRotate, 0f, 0f, 180f);
        renderDepthMapFace(mMatrixRotate);

		// Render shadow map right.
		mFboCubeMap.bindTexture(GLES30.GL_TEXTURE_CUBE_MAP_POSITIVE_X, 0);
		GLES30.glClear(GLES30.GL_DEPTH_BUFFER_BIT);
        MathUtils.setRotateM(mMatrixRotate, 0f, 90f, 180f);
        renderDepthMapFace(mMatrixRotate);

		// Render shadow map back.
		mFboCubeMap.bindTexture(GLES30.GL_TEXTURE_CUBE_MAP_POSITIVE_Z, 0);
		GLES30.glClear(GLES30.GL_DEPTH_BUFFER_BIT);
        MathUtils.setRotateM(mMatrixRotate, 0f, 180f, 180f);
        renderDepthMapFace(mMatrixRotate);

		// Render shadow map left.
		mFboCubeMap.bindTexture(GLES30.GL_TEXTURE_CUBE_MAP_NEGATIVE_X, 0);
		GLES30.glClear(GLES30.GL_DEPTH_BUFFER_BIT);
        MathUtils.setRotateM(mMatrixRotate, 0f, -90f, 180f);
        renderDepthMapFace(mMatrixRotate);

		// Render shadow map down.
		mFboCubeMap.bindTexture(GLES30.GL_TEXTURE_CUBE_MAP_POSITIVE_Y, 0);
		GLES30.glClear(GLES30.GL_DEPTH_BUFFER_BIT);
        MathUtils.setRotateM(mMatrixRotate, -90f, 0f, 0f);
        renderDepthMapFace(mMatrixRotate);

		// Render shadow map up.
		mFboCubeMap.bindTexture(GLES30.GL_TEXTURE_CUBE_MAP_NEGATIVE_Y, 0);
		GLES30.glClear(GLES30.GL_DEPTH_BUFFER_BIT);
        MathUtils.setRotateM(mMatrixRotate, 90f, 0f, 0f);
        renderDepthMapFace(mMatrixRotate);
    }

	public void renderDepthMapFace(float[] viewRotateM) {
		// Render filled cube.
		mShaderDepth.useProgram();

		int uModelM = mShaderDepth.getHandle("uModelM");
		int uViewM = mShaderDepth.getHandle("uViewM");
		int uProjM = mShaderDepth.getHandle("uProjM");
		int aPosition = mShaderDepth.getHandle("aPosition");

		Matrix.multiplyMM(mMatrixViewProjection, 0, viewRotateM, 0,
				mMatrixViewLight, 0);

		GLES30.glUniformMatrix4fv(uViewM, 1, false, mMatrixViewProjection, 0);
		GLES30.glUniformMatrix4fv(uProjM, 1, false, mMatrixProjectionDepth, 0);

		GLES30.glVertexAttribPointer(aPosition, 3, GLES30.GL_BYTE, false, 0,
                Cube.getVertices());
        GLES30.glEnableVertexAttribArray(aPosition);

		GLES30.glEnable(GLES30.GL_CULL_FACE);
		GLES30.glEnable(GLES30.GL_DEPTH_TEST);

		Matrix.multiplyMM(mMatrixViewProjection, 0, mMatrixProjectionDepth, 0,
				mMatrixViewProjection, 0);
        Visibility.extractPlanes(mMatrixViewProjection, mPlanes);

        for (Cube cube : mModels[mParserData.mModelId].getCubes()) {
            if (Visibility.intersects(mPlanes, cube.getBoundingSphere())) {
                GLES30.glUniformMatrix4fv(uModelM, 1, false, cube.getModelM(), 0);
                GLES30.glDrawArrays(GLES30.GL_TRIANGLES, 0, 6 * 6);
			}
		}

		GLES30.glUniformMatrix4fv(uModelM, 1, false, mSkybox.getModelM(), 0);

		GLES30.glDrawArrays(GLES30.GL_TRIANGLES, 0, 6 * 6);

		GLES30.glDisable(GLES30.GL_CULL_FACE);
	}

	public void renderScene(int renderMode) {
        ShaderManager shader;
        if (renderMode == Model.MODE_SHADOWMAP) {
            shader = mShaderDepthMap;
		} else {
			shader = mShaderDefault;
		}

		shader.useProgram();
		int uModelM = shader.getHandle("uModelM");
		int uViewM = shader.getHandle("uViewM");
		int uProjM = shader.getHandle("uProjM");
		int uLightPos = shader.getHandle("uLightPos");
		int uColor = shader.getHandle("uColor");
		int aPosition = shader.getHandle("aPosition");
		int aNormal = shader.getHandle("aNormal");

		GLES30.glUniform3fv(uLightPos, 1, mParserData.mLightPosition, 0);

		GLES30.glVertexAttribPointer(aPosition, 3, GLES30.GL_BYTE, false, 0,
                Cube.getVertices());
        GLES30.glEnableVertexAttribArray(aPosition);

		GLES30.glVertexAttribPointer(aNormal, 3, GLES30.GL_BYTE, false, 0,
                Cube.getNormals());
        GLES30.glEnableVertexAttribArray(aNormal);

		if (renderMode == Model.MODE_SHADOWMAP) {
			GLES30.glActiveTexture(GLES30.GL_TEXTURE0);
			GLES30.glBindTexture(GLES30.GL_TEXTURE_CUBE_MAP,
					mFboCubeMap.getTexture(0));
		}

		GLES30.glEnable(GLES30.GL_CULL_FACE);
		GLES30.glEnable(GLES30.GL_DEPTH_TEST);

		GLES30.glUniformMatrix4fv(uViewM, 1, false, mMatrixView, 0);
		GLES30.glUniformMatrix4fv(uProjM, 1, false, mMatrixProjection, 0);
		GLES30.glUniform3f(uColor, .4f, .6f, 1f);

		Matrix.multiplyMM(mMatrixViewProjection, 0, mMatrixProjection, 0,
				mMatrixView, 0);
        Visibility.extractPlanes(mMatrixViewProjection, mPlanes);

        for (Cube cube : mModels[mParserData.mModelId].getCubes()) {
            if (Visibility.intersects(mPlanes, cube.getBoundingSphere())) {
                GLES30.glUniformMatrix4fv(uModelM, 1, false, cube.getModelM(), 0);
                GLES30.glDrawArrays(GLES30.GL_TRIANGLES, 0, 6 * 6);
			}
		}

		GLES30.glUniformMatrix4fv(uModelM, 1, false, mSkybox.getModelM(), 0);
		GLES30.glUniform3f(uColor, .5f, .5f, .5f);

		GLES30.glVertexAttribPointer(aNormal, 3, GLES30.GL_BYTE, false, 0,
                Cube.getNormalsInv());
        GLES30.glEnableVertexAttribArray(aNormal);

		GLES30.glDrawArrays(GLES30.GL_TRIANGLES, 0, 6 * 6);

		GLES30.glDisable(GLES30.GL_CULL_FACE);
	}

	public void renderShadowStencil() {
		Matrix.multiplyMM(mMatrixViewProjection, 0, mMatrixProjection, 0,
				mMatrixView, 0);
		Matrix.multiplyMM(mMatrixViewExtrude, 0, mMatrixExtrude, 0,
				mMatrixView, 0);

		mShaderStencil.useProgram();
		int uModelM = mShaderStencil.getHandle("uModelM");
		int uViewProjectionM = mShaderStencil.getHandle("uViewProjectionM");
		int uViewExtrudeM = mShaderStencil.getHandle("uViewExtrudeM");
		int uLightPosition = mShaderStencil.getHandle("uLightPosition");
		int aPosition = mShaderStencil.getHandle("aPosition");
		int aNormal = mShaderStencil.getHandle("aNormal");

		GLES30.glUniform3fv(uLightPosition, 1, mParserData.mLightPosition, 0);

		GLES30.glVertexAttribPointer(aPosition, 4, GLES30.GL_BYTE, false, 0,
                Cube.getVerticesShadow());
        GLES30.glEnableVertexAttribArray(aPosition);

		GLES30.glVertexAttribPointer(aNormal, 3, GLES30.GL_BYTE, false, 0,
                Cube.getNormalsShadow());
        GLES30.glEnableVertexAttribArray(aNormal);

		GLES30.glDisable(GLES30.GL_CULL_FACE);
		GLES30.glEnable(GLES30.GL_DEPTH_TEST);
		GLES30.glEnable(GLES30.GL_STENCIL_TEST);
		// GLES30.glEnable(GLES30.GL_BLEND);

		GLES30.glUniformMatrix4fv(uViewProjectionM, 1, false,
				mMatrixViewProjection, 0);
		GLES30.glUniformMatrix4fv(uViewExtrudeM, 1, false, mMatrixViewExtrude,
				0);

		GLES30.glDepthMask(false);
		GLES30.glColorMask(false, false, false, false);
		// GLES30.glBlendFunc(GLES30.GL_SRC_ALPHA,
		// GLES30.GL_ONE_MINUS_SRC_ALPHA);

		GLES30.glStencilFunc(GLES30.GL_ALWAYS, 0x00, 0xFF);
		GLES30.glStencilOpSeparate(GLES30.GL_FRONT, GLES30.GL_KEEP,
				GLES30.GL_KEEP, GLES30.GL_INCR_WRAP);
		GLES30.glStencilOpSeparate(GLES30.GL_BACK, GLES30.GL_KEEP,
				GLES30.GL_KEEP, GLES30.GL_DECR_WRAP);

        Cube[] cubes = mModels[mParserData.mModelId].getCubes();
        for (Cube cube : mModels[mParserData.mModelId].getCubes()) {
            GLES30.glUniformMatrix4fv(uModelM, 1, false, cube.getModelM(), 0);
            GLES30.glDrawArrays(GLES30.GL_TRIANGLES, 0, 6 * 24);
		}

		GLES30.glDepthMask(true);
		GLES30.glColorMask(true, true, true, true);

		GLES30.glDisable(GLES30.GL_CULL_FACE);
		GLES30.glDisable(GLES30.GL_DEPTH_TEST);
		GLES30.glDisable(GLES30.GL_STENCIL_TEST);
		// GLES30.glDisable(GLES30.GL_BLEND);
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

		@Override
		public void run() {
			mParser.interpolate(mParserData,
					mMediaPlayer.getCurrentPosition() / 1000f);
			mModels[mParserData.mModelId].setInterpolation(mParserData.mModelT);

			final float[] pos = mParserData.mCameraPosition;
			final float[] la = mParserData.mCameraLookAt;
			final float[] lpos = mParserData.mLightPosition;
			Matrix.setLookAtM(mMatrixView, 0, pos[0], pos[1], pos[2], la[0],
					la[1], la[2], 0f, 1f, 0f);
			Matrix.setIdentityM(mMatrixViewLight, 0);
			Matrix.translateM(mMatrixViewLight, 0, -lpos[0], -lpos[1], -lpos[2]);
		}
	}

	public interface Model {
		public static final int MODE_SHADOWMAP = 1;
		public static final int MODE_SHADOWVOLUME = 2;

        public Cube[] getCubes();

		public int getRenderMode();

		public void setInterpolation(float t);
	}

}
