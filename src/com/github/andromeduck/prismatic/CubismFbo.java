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

import android.opengl.GLES30;

/**
 * Helper class for handling frame buffer objects.
 */
public final class CubismFbo {

	// Optional depth buffer handle.
	private int mDepthBufferHandle = -1;
	// FBO handle.
	private int mFrameBufferHandle = -1;
	// Optional stencil buffer handle.
	private int mStencilBufferHandle = -1;
	// Generated texture handles.
	private int[] mTextureHandles = {};
	// FBO textures and depth buffer size.
	private int mWidth, mHeight;

	/**
	 * Bind certain texture into target texture.
	 * 
	 * @param target
	 *            GL_TEXTURE_2D, GL_TEXTURE_CUBE_MAP_POSITIVE/NEGATIVE_X/Y_Z.
	 * @param index
	 *            Index of texture to bind.
	 */
	public void bindTexture(int target, int index) {
		GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER, mFrameBufferHandle);
		GLES30.glViewport(0, 0, mWidth, mHeight);
		GLES30.glFramebufferTexture2D(GLES30.GL_FRAMEBUFFER,
				GLES30.GL_COLOR_ATTACHMENT0, target, mTextureHandles[index], 0);
	}

	/**
	 * Getter for FBO height.
	 * 
	 * @return FBO height in pixels.
	 */
	public int getHeight() {
		return mHeight;
	}

	/**
	 * Getter for texture ids.
	 * 
	 * @param index
	 *            Index of texture.
	 * @return Texture id.
	 */
	public int getTexture(int index) {
		return mTextureHandles[index];
	}

	/**
	 * Getter for FBO width.
	 * 
	 * @return FBO width in pixels.
	 */
	public int getWidth() {
		return mWidth;
	}

	/**
	 * Initializes FBO with given parameters. Calls simply init(int, int,
	 * GLES30.GL_TEXTURE_2D, int, false, false) without stencil and depth buffer
	 * generations.
	 * 
	 * @param width
	 *            Width in pixels.
	 * @param height
	 *            Height in pixels.
	 * @param textureCount
	 *            Number of textures to generate.
	 */
	public void init(int width, int height, int textureCount) {
		init(width, height, GLES30.GL_TEXTURE_2D, textureCount, false);
	}

	/**
	 * Initializes FBO with given parameters. Width and height are used to
	 * generate textures out of which all are sized same to this FBO. If you
	 * give genRenderBuffer a value 'true', depth buffer will be generated also.
	 * 
	 * @param width
	 *            FBO width in pixels
	 * @param height
	 *            FBO height in pixels
	 * @param target
	 *            GL_TEXTURE_2D or GL_TEXTURE_CUBE_MAP
	 * @param textureCount
	 *            Number of textures to generate
	 * @param genDepthStencilBuffer
	 *            If true, depth and stencil buffers are allocated for this FBO
	 */
	public void init(int width, int height, int target, int textureCount,
			boolean genDepthStencilBuffer) {

		// Just in case.
		reset();

		// Store FBO size.
		mWidth = width;
		mHeight = height;

		// Genereta FBO.
		int handle[] = { 0 };
		GLES30.glGenFramebuffers(1, handle, 0);
		mFrameBufferHandle = handle[0];
		GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER, mFrameBufferHandle);

		// Generate textures.
		mTextureHandles = new int[textureCount];
		GLES30.glGenTextures(textureCount, mTextureHandles, 0);
		for (int texture : mTextureHandles) {
			GLES30.glBindTexture(target, texture);
			GLES30.glTexParameteri(target, GLES30.GL_TEXTURE_WRAP_S,
					GLES30.GL_CLAMP_TO_EDGE);
			GLES30.glTexParameteri(target, GLES30.GL_TEXTURE_WRAP_T,
					GLES30.GL_CLAMP_TO_EDGE);
			GLES30.glTexParameteri(target, GLES30.GL_TEXTURE_MIN_FILTER,
					GLES30.GL_NEAREST);
			GLES30.glTexParameteri(target, GLES30.GL_TEXTURE_MAG_FILTER,
					GLES30.GL_LINEAR);

			if (target == GLES30.GL_TEXTURE_CUBE_MAP) {
				GLES30.glTexParameteri(target, GLES30.GL_TEXTURE_WRAP_R,
						GLES30.GL_CLAMP_TO_EDGE);
				GLES30.glTexParameteri(target, GLES30.GL_TEXTURE_BASE_LEVEL, 0);
				GLES30.glTexParameteri(target, GLES30.GL_TEXTURE_MAX_LEVEL, 0);

				GLES30.glTexImage2D(GLES30.GL_TEXTURE_CUBE_MAP_POSITIVE_X, 0,
						GLES30.GL_RGBA8, mWidth, mHeight, 0, GLES30.GL_RGBA,
						GLES30.GL_UNSIGNED_BYTE, null);
				GLES30.glTexImage2D(GLES30.GL_TEXTURE_CUBE_MAP_NEGATIVE_X, 0,
						GLES30.GL_RGBA8, mWidth, mHeight, 0, GLES30.GL_RGBA,
						GLES30.GL_UNSIGNED_BYTE, null);
				GLES30.glTexImage2D(GLES30.GL_TEXTURE_CUBE_MAP_POSITIVE_Y, 0,
						GLES30.GL_RGBA8, mWidth, mHeight, 0, GLES30.GL_RGBA,
						GLES30.GL_UNSIGNED_BYTE, null);
				GLES30.glTexImage2D(GLES30.GL_TEXTURE_CUBE_MAP_NEGATIVE_Y, 0,
						GLES30.GL_RGBA8, mWidth, mHeight, 0, GLES30.GL_RGBA,
						GLES30.GL_UNSIGNED_BYTE, null);
				GLES30.glTexImage2D(GLES30.GL_TEXTURE_CUBE_MAP_POSITIVE_Z, 0,
						GLES30.GL_RGBA8, mWidth, mHeight, 0, GLES30.GL_RGBA,
						GLES30.GL_UNSIGNED_BYTE, null);
				GLES30.glTexImage2D(GLES30.GL_TEXTURE_CUBE_MAP_NEGATIVE_Z, 0,
						GLES30.GL_RGBA8, mWidth, mHeight, 0, GLES30.GL_RGBA,
						GLES30.GL_UNSIGNED_BYTE, null);
			} else {
				GLES30.glTexImage2D(target, 0, GLES30.GL_RGBA8, mWidth,
						mHeight, 0, GLES30.GL_RGBA, GLES30.GL_UNSIGNED_BYTE,
						null);
			}
		}

		// Generate depth and stencil buffer.
		if (genDepthStencilBuffer) {
			GLES30.glGenRenderbuffers(1, handle, 0);
			mDepthBufferHandle = handle[0];
			GLES30.glBindRenderbuffer(GLES30.GL_RENDERBUFFER,
					mDepthBufferHandle);
			GLES30.glRenderbufferStorage(GLES30.GL_RENDERBUFFER,
					GLES30.GL_DEPTH24_STENCIL8, mWidth, mHeight);
			GLES30.glFramebufferRenderbuffer(GLES30.GL_FRAMEBUFFER,
					GLES30.GL_DEPTH_STENCIL_ATTACHMENT, GLES30.GL_RENDERBUFFER,
					mDepthBufferHandle);
		}
	}

	/**
	 * Resets this FBO into its initial state, releasing all resources that were
	 * allocated during a call to init.
	 */
	public void reset() {
		int[] handle = { mFrameBufferHandle };
		GLES30.glDeleteFramebuffers(1, handle, 0);
		handle[0] = mDepthBufferHandle;
		GLES30.glDeleteRenderbuffers(1, handle, 0);
		handle[0] = mStencilBufferHandle;
		GLES30.glDeleteRenderbuffers(1, handle, 0);
		GLES30.glDeleteTextures(mTextureHandles.length, mTextureHandles, 0);
		mFrameBufferHandle = mDepthBufferHandle = mStencilBufferHandle = -1;
		mTextureHandles = new int[0];
	}

}
