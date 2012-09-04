package fi.harism.cubism;

import java.util.Arrays;
import java.util.Comparator;

import android.opengl.Matrix;
import android.util.FloatMath;

public class CubismScene {

	private static final int CUBE_DIV = 10;
	private static final float CUBE_SCALE = 1.41421356237f / (CUBE_DIV - 1);
	private static final int CUBE_SZ = CUBE_DIV * CUBE_DIV * 2 + CUBE_DIV
			* (CUBE_DIV - 2) * 2 + (CUBE_DIV - 2) * (CUBE_DIV - 2) * 2;

	private float mAnimateTime;
	private int mAnimationStep;
	private Cube[] mCubes;

	private final float[] mMatrixLightView = new float[16];
	private final float[] mMatrixView = new float[16];
	private final float[] mPosCamera = new float[3];
	private final float[] mPosCameraSource = new float[3];
	private final float[] mPosCameraTarget = new float[3];
	private final float[] mPosLight = new float[3];
	private final float[] mPosLightSource = new float[3];
	private final float[] mPosLightTarget = new float[3];

	public CubismScene() {
		int idx = 0;
		mCubes = new Cube[CUBE_SZ];
		for (int x = 0; x < CUBE_DIV; ++x) {
			for (int y = 0; y < CUBE_DIV; ++y) {
				for (int z = 0; z < CUBE_DIV;) {
					mCubes[idx] = new Cube();
					mCubes[idx].setScale(CUBE_SCALE);

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

	public void animate(float time) {
		if (time < mAnimateTime) {
			mAnimationStep = 1;
		}
		mAnimateTime = time;

		/**
		 * Camera and light movement.
		 */
		float tCamera = 0f;
		switch (mAnimationStep) {
		case 0: {
			for (int i = 0; i < 3; ++i) {
				mPosCameraTarget[i] = (float) (Math.random() * 6 - 3);
				mPosCameraTarget[i] += mPosCameraTarget[i] > 0 ? 3 : -3;
				mPosLightTarget[i] = mPosCameraTarget[i]
						* (float) (.5 + Math.random());
			}
		}
		case 1: {
			for (int i = 0; i < 3; ++i) {
				mPosCameraSource[i] = mPosCameraTarget[i];
				mPosCameraTarget[i] = (float) (Math.random() * 6 - 3);
				mPosCameraTarget[i] += mPosCameraTarget[i] > 0 ? 3 : -3;

				mPosLightSource[i] = mPosLightTarget[i];
				mPosLightTarget[i] = mPosCameraTarget[i]
						* (float) (.5 + Math.random());
			}

			float gravityX = (float) (Math.random() * 10 - 5);
			float gravityY = (float) (Math.random() * 10 - 5);
			float gravityZ = (float) (Math.random() * 10 - 5);
			for (Cube cube : mCubes) {
				float dx = cube.mPositionSource[0] - gravityX;
				float dy = cube.mPositionSource[1] - gravityY;
				float dz = cube.mPositionSource[2] - gravityZ;
				cube.mDistanceFromGravity = FloatMath.sqrt(dx * dx + dy * dy
						+ dz * dz);
			}
			Arrays.sort(mCubes, new Comparator<Cube>() {
				@Override
				public int compare(Cube c0, Cube c1) {
					return c0.mDistanceFromGravity < c1.mDistanceFromGravity ? -1
							: 1;
				}
			});

			mAnimationStep = 2;
			break;
		}
		case 2: {
			if (time < 7.6f) {
				tCamera = time / 7.6f;
				tCamera = tCamera * tCamera * (3 - 2 * tCamera);
				break;
			}

			for (int i = 0; i < 3; ++i) {
				mPosCameraSource[i] = (float) (Math.random() * 6 - 3);
				mPosCameraSource[i] += mPosCameraSource[i] > 0 ? 3 : -3;
				mPosCameraTarget[i] = (float) (Math.random() * 6 - 3);
				mPosCameraTarget[i] += mPosCameraTarget[i] > 0 ? 3 : -3;

				mPosLightSource[i] = mPosCameraSource[i]
						* (float) (.5 + Math.random());
				mPosLightTarget[i] = mPosCameraTarget[i]
						* (float) (.5 + Math.random());
			}

			mAnimationStep = 3;
		}
		case 3: {
			tCamera = (time - 7.6f) / (11.2f - 7.6f);
			tCamera = tCamera * tCamera * (3 - 2 * tCamera);
			break;
		}
		}

		CubismUtils.interpolateV(mPosCamera, mPosCameraSource,
				mPosCameraTarget, tCamera);
		CubismUtils.interpolateV(mPosLight, mPosLightSource, mPosLightTarget,
				tCamera);

		Matrix.setLookAtM(mMatrixView, 0, mPosCamera[0], mPosCamera[1],
				mPosCamera[2], 0f, 0f, 0f, 0f, 1f, 0f);
		Matrix.setIdentityM(mMatrixLightView, 0);
		Matrix.translateM(mMatrixLightView, 0, -mPosLight[0], -mPosLight[1],
				-mPosLight[2]);

		/**
		 * Big cube "explosion".
		 */
		float tExplode = 0f;
		if (time > 2.0f && time <= 9.0f) {
			tExplode = (time - 2.0f) / 7.0f;
		} else if (time > 9.0f) {
			tExplode = 1f - (time - 9.0f) / 2.2f;
		}
		if (tExplode > 1)
			tExplode = 1;
		if (tExplode < 0)
			tExplode = 0;

		for (int i = 0; i < mCubes.length; ++i) {
			float t = 1f - (float) i / mCubes.length;
			t = tExplode * t;
			t = t * t * (3 - 2 * t);

			Cube cube = mCubes[i];

			CubismUtils.interpolateV(cube.mPosition, cube.mPositionSource,
					cube.mPositionTarget, t);
			CubismUtils.interpolateV(cube.mRotation, cube.mRotationSource,
					cube.mRotationTarget, t);

			cube.setTranslate(cube.mPosition[0], cube.mPosition[1],
					cube.mPosition[2]);
			cube.setRotate(cube.mRotation[0], cube.mRotation[1],
					cube.mRotation[2]);
		}
	}

	public CubismCube[] getCubes() {
		return mCubes;
	}

	public float[] getLightPosition() {
		return mPosLight;
	}

	public float[] getLightViewM() {
		return mMatrixLightView;
	}

	public float[] getViewM() {
		return mMatrixView;
	}

	private class Cube extends CubismCube {
		public float mDistanceFromGravity;
		public final float[] mPosition = new float[3];
		public final float[] mPositionSource = new float[3];
		public final float[] mPositionTarget = new float[3];
		public final float[] mRotation = new float[3];
		public final float[] mRotationSource = new float[3];
		public final float[] mRotationTarget = new float[3];
	}

}
