package fi.harism.cubism;

public final class CubismVisibility {

	public static void extractPlanes(float[] mvp, float[] result) {
		// Right
		result[0] = mvp[3] - mvp[0];
		result[1] = mvp[7] - mvp[4];
		result[2] = mvp[11] - mvp[8];
		result[3] = mvp[15] - mvp[12];

		// Left
		result[4] = mvp[3] + mvp[0];
		result[5] = mvp[7] + mvp[4];
		result[6] = mvp[11] + mvp[8];
		result[7] = mvp[15] + mvp[12];

		// Bottom
		result[8] = mvp[3] + mvp[1];
		result[9] = mvp[7] + mvp[5];
		result[10] = mvp[11] + mvp[9];
		result[11] = mvp[15] + mvp[13];

		// Top
		result[12] = mvp[3] - mvp[1];
		result[13] = mvp[7] - mvp[5];
		result[14] = mvp[11] - mvp[9];
		result[15] = mvp[15] - mvp[13];

		// Far
		result[16] = mvp[3] - mvp[2];
		result[17] = mvp[7] - mvp[6];
		result[18] = mvp[11] - mvp[10];
		result[19] = mvp[15] - mvp[14];

		// Near
		result[20] = mvp[3] + mvp[2];
		result[21] = mvp[7] + mvp[6];
		result[22] = mvp[11] + mvp[10];
		result[23] = mvp[15] + mvp[14];
	}

	public static boolean intersects(float[] planes, float[] sphere) {
		int k = 0;
		for (int i = 0; i < 6; ++i) {
			float dist = planes[k++] * sphere[0] + planes[k++] * sphere[1]
					+ planes[k++] * sphere[2] + planes[k++];
			if (dist <= -sphere[3]) {
				return false;
			}
		}
		return true;
	}

}
