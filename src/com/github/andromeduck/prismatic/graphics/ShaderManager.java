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

package com.github.andromeduck.prismatic.graphics;

import java.util.HashMap;

import android.opengl.GLES30;
import android.util.Log;

/**
 * Helper class for handling shaders.
 */
public final class ShaderManager {

	// Shader program handles.
	private int mIdProgram = 0;
	private int mIdShaderFragment = 0;
	private int mIdShaderVertex = 0;
	// HashMap for storing uniform/attribute handles.
	private final HashMap<String, Integer> mShaderHandleMap = new HashMap<String, Integer>();

	/**
	 * Deletes program and shaders associated with it.
	 */
	public void deleteProgram() {
		GLES30.glDeleteShader(mIdShaderFragment);
		GLES30.glDeleteShader(mIdShaderVertex);
		GLES30.glDeleteProgram(mIdProgram);
		mIdProgram = mIdShaderVertex = mIdShaderFragment = 0;
	}

	/**
	 * Get id for given handle name. This method checks for both attribute and
	 * uniform handles.
	 * 
	 * @param name
	 *            Name of handle.
	 * @return Id for given handle or -1 if none found.
	 */
	public int getHandle(String name) {
		if (mShaderHandleMap.containsKey(name)) {
			return mShaderHandleMap.get(name);
		}
		int handle = GLES30.glGetAttribLocation(mIdProgram, name);
		if (handle == -1) {
			handle = GLES30.glGetUniformLocation(mIdProgram, name);
		}
		if (handle == -1) {
			// One should never leave log messages but am not going to follow
			// this rule. This line comes handy if you see repeating 'not found'
			// messages on LogCat - usually for typos otherwise annoying to
			// spot from shader code.
			Log.d("GlslShader", "Could not get attrib location for " + name);
		} else {
			mShaderHandleMap.put(name, handle);
		}
		return handle;
	}

	/**
	 * Get array of ids with given names. Returned array is sized to given
	 * amount name elements.
	 * 
	 * @param names
	 *            List of handle names.
	 * @return array of handle ids.
	 */
	public int[] getHandles(String... names) {
		int[] res = new int[names.length];
		for (int i = 0; i < names.length; ++i) {
			res[i] = getHandle(names[i]);
		}
		return res;
	}

	/**
	 * Helper method for compiling a shader.
	 * 
	 * @param shaderType
	 *            Type of shader to compile
	 * @param source
	 *            String presentation for shader
	 * @return id for compiled shader
	 */
	private int loadShader(int shaderType, String source) throws Exception {
		int shader = GLES30.glCreateShader(shaderType);
		if (shader != 0) {
			GLES30.glShaderSource(shader, source);
			GLES30.glCompileShader(shader);
			int[] compiled = new int[1];
			GLES30.glGetShaderiv(shader, GLES30.GL_COMPILE_STATUS, compiled, 0);
			if (compiled[0] == 0) {
				String error = GLES30.glGetShaderInfoLog(shader);
				GLES30.glDeleteShader(shader);
				throw new Exception(error);
			}
		}
		return shader;
	}

	/**
	 * Compiles vertex and fragment shaders and links them into a program one
	 * can use for rendering. Once OpenGL context is lost and onSurfaceCreated
	 * is called, there is no need to reset existing GlslShader objects but one
	 * can simply reload shader.
	 * 
	 * @param vertexSource
	 *            String presentation for vertex shader
	 * @param fragmentSource
	 *            String presentation for fragment shader
	 */
	public void setProgram(String vertexSource, String fragmentSource)
			throws Exception {
		mIdShaderVertex = loadShader(GLES30.GL_VERTEX_SHADER, vertexSource);
		mIdShaderFragment = loadShader(GLES30.GL_FRAGMENT_SHADER,
				fragmentSource);
		int program = GLES30.glCreateProgram();
		if (program != 0) {
			GLES30.glAttachShader(program, mIdShaderVertex);
			GLES30.glAttachShader(program, mIdShaderFragment);
			GLES30.glLinkProgram(program);
			int[] linkStatus = new int[1];
			GLES30.glGetProgramiv(program, GLES30.GL_LINK_STATUS, linkStatus, 0);
			if (linkStatus[0] != GLES30.GL_TRUE) {
				String error = GLES30.glGetProgramInfoLog(program);
				deleteProgram();
				throw new Exception(error);
			}
		}
		mIdProgram = program;
		mShaderHandleMap.clear();
	}

	/**
	 * Activates this shader program.
	 */
	public void useProgram() {
		GLES30.glUseProgram(mIdProgram);
	}

}
