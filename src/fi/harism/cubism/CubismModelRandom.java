package fi.harism.cubism;

public class CubismModelRandom implements CubismRenderer.Model {

	private static final int CUBE_DIV = 6;
	private static final float CUBE_SCALE = 1f / CUBE_DIV;
	private static final int CUBE_SZ = CUBE_DIV * CUBE_DIV * 2 + CUBE_DIV
			* (CUBE_DIV - 2) * 2 + (CUBE_DIV - 2) * (CUBE_DIV - 2) * 2;

	private Cube[] mCubes;

	public CubismModelRandom() {
		int idx = 0;
		mCubes = new Cube[CUBE_SZ];
		for (int x = 0; x < CUBE_DIV; ++x) {
			for (int y = 0; y < CUBE_DIV; ++y) {
				for (int z = 0; z < CUBE_DIV;) {
					mCubes[idx] = new Cube();
					mCubes[idx].setScale(CUBE_SCALE);

					float t = 2.0f * CUBE_SCALE;
					float tx = x * t + CUBE_SCALE - 1f;
					float ty = y * t + CUBE_SCALE - 1f;
					float tz = z * t + CUBE_SCALE - 1f;

					mCubes[idx].mPositionSource[0] = tx;
					mCubes[idx].mPositionSource[1] = ty;
					mCubes[idx].mPositionSource[2] = tz;

					mCubes[idx].mPositionTarget[0] = (float) (Math.random() * 6 - 3);
					mCubes[idx].mPositionTarget[1] = (float) (Math.random() * 6 - 3);
					mCubes[idx].mPositionTarget[2] = (float) (Math.random() * 6 - 3);

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

	@Override
	public CubismCube[] getCubes() {
		return mCubes;
	}

	@Override
	public void interpolate(float t) {
		for (int i = 0; i < mCubes.length; ++i) {
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

	private class Cube extends CubismCube {
		public final float[] mPosition = new float[3];
		public final float[] mPositionSource = new float[3];
		public final float[] mPositionTarget = new float[3];
		public final float[] mRotation = new float[3];
		public final float[] mRotationSource = new float[3];
		public final float[] mRotationTarget = new float[3];
	}

}
