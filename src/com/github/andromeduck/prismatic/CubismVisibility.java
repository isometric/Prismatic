/*
   Copyright 2012 James Deng

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

import android.util.FloatMath;

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

		for (int i = 0; i < 24; i += 4) {
			float lenInv = 1f / FloatMath.sqrt(result[i] * result[i]
					+ result[i + 1] * result[i + 1] + result[i + 2]
					* result[i + 2] + result[i + 3] * result[i + 3]);
			result[i] *= lenInv;
			result[i + 1] *= lenInv;
			result[i + 2] *= lenInv;
			result[i + 3] *= lenInv;
		}
	}

	public static boolean intersects(float[] planes, float[] sphere) {
		int k = 0;
		for (int i = 0; i < 6; ++i) {
			float dist = planes[k++] * sphere[0] + planes[k++] * sphere[1]
					+ planes[k++] * sphere[2] + planes[k++];
			if (dist <= -sphere[3]) {
				return false;
			}
			if (Math.abs(dist) < sphere[3]) {
				return true;
			}
		}
		return true;
	}

}
