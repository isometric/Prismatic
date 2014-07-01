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

package com.github.andromeduck.prismatic.graphics.blocks;

import java.nio.ByteBuffer;

import android.opengl.Matrix;

import com.github.andromeduck.prismatic.graphics.MathUtils;

public class Cube implements Drawable {

    private static ByteBuffer mBufferNormals;
    private static ByteBuffer mBufferNormalsInv;
    private static ByteBuffer mBufferNormalsShadow;
    private static ByteBuffer mBufferVertices;
    private static ByteBuffer mBufferVerticesShadow;
    private static final float SQRT_2 = 1.41421356237f;

    static {
        // Vertex and normal data plus indices arrays.
        final byte[][] CUBEVERTICES = {{-1, 1, 1}, {-1, -1, 1},
                {1, 1, 1}, {1, -1, 1}, {-1, 1, -1}, {-1, -1, -1},
                {1, 1, -1}, {1, -1, -1}};
        final byte[][] CUBENORMALS = {{0, 0, 1}, {0, 0, -1}, {-1, 0, 0},
                {1, 0, 0}, {0, 1, 0}, {0, -1, 0}};
        final int[][][] CUBEFILLED = {
                {{0, 1, 2, 1, 3, 2}, {0}, {1}, {0, 1, 3, 2}},
                {{6, 7, 4, 7, 5, 4}, {1}, {0}, {6, 7, 5, 4}},
                {{0, 4, 1, 4, 5, 1}, {2}, {3}, {0, 4, 5, 1}},
                {{3, 7, 2, 7, 6, 2}, {3}, {2}, {3, 7, 6, 2}},
                {{4, 0, 6, 0, 2, 6}, {4}, {5}, {4, 0, 2, 6}},
                {{1, 5, 3, 5, 7, 3}, {5}, {4}, {1, 5, 7, 3}}};

        mBufferVertices = ByteBuffer.allocateDirect(3 * 6 * 6);
        mBufferNormals = ByteBuffer.allocateDirect(3 * 6 * 6);
        mBufferNormalsInv = ByteBuffer.allocateDirect(3 * 6 * 6);
        mBufferVerticesShadow = ByteBuffer.allocateDirect(4 * 6 * 24);
        mBufferNormalsShadow = ByteBuffer.allocateDirect(3 * 6 * 24);

        final byte C = 1, P = 0;
        for (int i = 0; i < CUBEFILLED.length; ++i) {
            for (int j = 0; j < CUBEFILLED[i][0].length; ++j) {
                mBufferVertices.put(CUBEVERTICES[CUBEFILLED[i][0][j]]);
                mBufferNormals.put(CUBENORMALS[CUBEFILLED[i][1][0]]);
                mBufferNormalsInv.put(CUBENORMALS[CUBEFILLED[i][2][0]]);
            }

            mBufferVerticesShadow.put(CUBEVERTICES[CUBEFILLED[i][3][0]]).put(C);
            mBufferVerticesShadow.put(CUBEVERTICES[CUBEFILLED[i][3][0]]).put(P);
            mBufferVerticesShadow.put(CUBEVERTICES[CUBEFILLED[i][3][1]]).put(C);
            mBufferVerticesShadow.put(CUBEVERTICES[CUBEFILLED[i][3][0]]).put(P);
            mBufferVerticesShadow.put(CUBEVERTICES[CUBEFILLED[i][3][1]]).put(P);
            mBufferVerticesShadow.put(CUBEVERTICES[CUBEFILLED[i][3][1]]).put(C);

            mBufferVerticesShadow.put(CUBEVERTICES[CUBEFILLED[i][3][1]]).put(C);
            mBufferVerticesShadow.put(CUBEVERTICES[CUBEFILLED[i][3][1]]).put(P);
            mBufferVerticesShadow.put(CUBEVERTICES[CUBEFILLED[i][3][2]]).put(C);
            mBufferVerticesShadow.put(CUBEVERTICES[CUBEFILLED[i][3][1]]).put(P);
            mBufferVerticesShadow.put(CUBEVERTICES[CUBEFILLED[i][3][2]]).put(P);
            mBufferVerticesShadow.put(CUBEVERTICES[CUBEFILLED[i][3][2]]).put(C);

            mBufferVerticesShadow.put(CUBEVERTICES[CUBEFILLED[i][3][2]]).put(C);
            mBufferVerticesShadow.put(CUBEVERTICES[CUBEFILLED[i][3][2]]).put(P);
            mBufferVerticesShadow.put(CUBEVERTICES[CUBEFILLED[i][3][3]]).put(C);
            mBufferVerticesShadow.put(CUBEVERTICES[CUBEFILLED[i][3][2]]).put(P);
            mBufferVerticesShadow.put(CUBEVERTICES[CUBEFILLED[i][3][3]]).put(P);
            mBufferVerticesShadow.put(CUBEVERTICES[CUBEFILLED[i][3][3]]).put(C);

            mBufferVerticesShadow.put(CUBEVERTICES[CUBEFILLED[i][3][3]]).put(C);
            mBufferVerticesShadow.put(CUBEVERTICES[CUBEFILLED[i][3][3]]).put(P);
            mBufferVerticesShadow.put(CUBEVERTICES[CUBEFILLED[i][3][0]]).put(C);
            mBufferVerticesShadow.put(CUBEVERTICES[CUBEFILLED[i][3][3]]).put(P);
            mBufferVerticesShadow.put(CUBEVERTICES[CUBEFILLED[i][3][0]]).put(P);
            mBufferVerticesShadow.put(CUBEVERTICES[CUBEFILLED[i][3][0]]).put(C);

            for (int j = 0; j < 24; ++j) {
                mBufferNormalsShadow.put(CUBENORMALS[CUBEFILLED[i][1][0]]);
            }
        }
        mBufferVertices.position(0);
        mBufferNormals.position(0);
        mBufferNormalsInv.position(0);

        mBufferVertices.position(0);
        mBufferNormals.position(0);
        mBufferNormalsInv.position(0);

        mBufferVerticesShadow.position(0);
        mBufferNormalsShadow.position(0);
    }

    public static ByteBuffer getNormals() {
        return mBufferNormals;
    }

    public static ByteBuffer getNormalsInv() {
        return mBufferNormalsInv;
    }

    public static ByteBuffer getNormalsShadow() {
        return mBufferNormalsShadow;
    }

    public static ByteBuffer getVertices() {
        return mBufferVertices;
    }

    public static ByteBuffer getVerticesShadow() {
        return mBufferVerticesShadow;
    }

    private final float[] mBoundingSphere = new float[4];
    private final float[] mColor = new float[3];
    private final float[] mMatrixModel = new float[16];
    private final float[] mMatrixRotate = new float[16];
    private final float[] mMatrixScale = new float[16];
    private final float[] mMatrixTranslate = new float[16];
    private boolean mRecalculate;
    private final float[] position = new float[3];
    private float scale = 0;

    public Cube() {
        Matrix.setIdentityM(mMatrixRotate, 0);
        Matrix.setIdentityM(mMatrixScale, 0);
        Matrix.setIdentityM(mMatrixTranslate, 0);
        Matrix.setIdentityM(mMatrixModel, 0);
        mBoundingSphere[3] = SQRT_2;

        // scale cube to size 1
        setScale(1);
    }

    @Override
    public float[] getBoundingSphere() {
        return mBoundingSphere;
    }

    @Override
    public float[] getColor() {
        return mColor;
    }

    @Override
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

    @Override
    public void setColor(float r, float g, float b) {
        mColor[0] = r;
        mColor[1] = g;
        mColor[2] = b;
    }

    @Override
    public void setRotate(float rx, float ry, float rz) {
        MathUtils.setRotateM(mMatrixRotate, rx, ry, rz);
        mRecalculate = true;
    }

    @Override
    public void setScale(float scale) {
        this.scale = scale;

        // scale base cube to size 1 instead of 2
        scale *= 0.5;

        Matrix.setIdentityM(mMatrixScale, 0);
        Matrix.scaleM(mMatrixScale, 0, scale, scale, scale);
        mBoundingSphere[3] = SQRT_2 * scale;
        mRecalculate = true;
    }

    @Override
    public float getScale() {
        return scale;
    }

    @Override
    public void setPosition(float tx, float ty, float tz) {
        position[0] = tx;
        position[1] = ty;
        position[2] = tz;

        Matrix.setIdentityM(mMatrixTranslate, 0);
        Matrix.translateM(mMatrixTranslate, 0, tx, ty, tz);
        mBoundingSphere[0] = tx;
        mBoundingSphere[1] = ty;
        mBoundingSphere[2] = tz;
        mRecalculate = true;
    }

    @Override
    public float[] getPosition() {
        return position;
    }
}
