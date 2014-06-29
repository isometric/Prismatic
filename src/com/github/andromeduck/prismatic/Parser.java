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

package com.github.andromeduck.prismatic;

import com.github.andromeduck.prismatic.graphics.MathUtils;

import java.io.IOException;
import java.io.InputStream;
import java.util.Vector;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.AttributeList;
import org.xml.sax.HandlerBase;
import org.xml.sax.SAXException;

public final class Parser {

	private static final int INTERPOLATION_HERMITE = 1;
	private static final int INTERPOLATION_LINEAR = 0;

	private final Vector<StructVector> mCameraLookAts = new Vector<StructVector>();
	private final Vector<StructVector> mCameraPositions = new Vector<StructVector>();
	private final Vector<StructVector> mForegroundColors = new Vector<StructVector>();
	private final Vector<StructVector> mLightPositions = new Vector<StructVector>();
	private final Vector<StructVector> mModels = new Vector<StructVector>();
	private final float[] mModelT = new float[1];

    public Parser(InputStream is) throws SAXException, IOException,
            ParserConfigurationException {
        SAXParserFactory factory = SAXParserFactory.newInstance();
		SAXParser parser = factory.newSAXParser();
		parser.parse(is, new XMLHandler());
	}

	private float calculateT(StructVector vector, float time) {
		float t = (time - vector.mTimeStart)
				/ (vector.mTimeEnd - vector.mTimeStart);
		switch (vector.mInterpolationType) {
		case INTERPOLATION_LINEAR:
			break;
		case INTERPOLATION_HERMITE:
			t = t * t * (3 - 2 * t);
			break;
		}
		return t;
	}

	private StructVector findVector(Vector<StructVector> vectors, float time) {
		for (StructVector vector : vectors) {
			if (vector.mTimeStart <= time && vector.mTimeEnd >= time) {
				return vector;
			}
		}
		return null;
	}

	public void interpolate(Data data, float time) {
		StructVector vector;

		vector = findVector(mModels, time);
		if (vector != null) {
			interpolate(mModelT, vector, time);
			data.mModelId = vector.mId;
			data.mModelT = mModelT[0];
		}

		vector = findVector(mCameraPositions, time);
		interpolate(data.mCameraPosition, vector, time);

		vector = findVector(mCameraLookAts, time);
		interpolate(data.mCameraLookAt, vector, time);

		vector = findVector(mLightPositions, time);
		interpolate(data.mLightPosition, vector, time);

		vector = findVector(mForegroundColors, time);
		interpolate(data.mForegroundColor, vector, time);
	}

	private void interpolate(float[] out, StructVector vector, float time) {
		if (vector != null) {
			float t = calculateT(vector, time);

			if (vector.mCtrl2.length > 0) {
                MathUtils.interpolateV(out, vector.mCtrl0, vector.mCtrl1,
                        vector.mCtrl2, t);
            } else if (vector.mCtrl1.length > 0) {
                MathUtils.interpolateV(out, vector.mCtrl0, vector.mCtrl1, t);
            } else {
                for (int i = 0; i < vector.mCtrl0.length; ++i) {
					out[i] = vector.mCtrl0[i];
				}
			}
		}
	}

	private float parseFloat(String str) {
		float retVal = 0.0f;
		if (str != null) {
			retVal = Float.parseFloat(str.trim());
		}
		return retVal;
	}

	private float[] parseFloatArray(String str) {
		if (str != null) {
			String[] floats = str.split(",");
			float[] retVal = new float[floats.length];
			for (int i = 0; i < floats.length; ++i) {
				retVal[i] = parseFloat(floats[i]);
			}
			return retVal;
		}
		return new float[0];
	}

	private int parseInt(String str) {
		int retVal = 0;
		if (str != null) {
			retVal = Integer.parseInt(str.trim());
		}
		return retVal;
	}

	private int parseInterpolationType(AttributeList attrs) {
		int retVal = INTERPOLATION_LINEAR;
		if ("hermite".equals(attrs.getValue("i"))) {
			retVal = INTERPOLATION_HERMITE;
		}
		return retVal;
	}

	private StructVector parseVector(AttributeList attrs) {
		StructVector vector = new StructVector();
		vector.mId = parseInt(attrs.getValue("id"));
		vector.mInterpolationType = parseInterpolationType(attrs);
		vector.mTimeStart = parseFloat(attrs.getValue("t0"));
		vector.mTimeEnd = parseFloat(attrs.getValue("t1"));
		vector.mCtrl0 = parseFloatArray(attrs.getValue("c0"));
		vector.mCtrl1 = parseFloatArray(attrs.getValue("c1"));
		vector.mCtrl2 = parseFloatArray(attrs.getValue("c2"));
		return vector;
	}

	public static class Data {
		public final float[] mCameraLookAt = new float[3];
		public final float[] mCameraPosition = new float[3];
		public final float[] mForegroundColor = new float[4];
		public final float[] mLightPosition = new float[3];
		public int mModelId;
		public float mModelT;
	}

	private class StructVector {
		public float[] mCtrl0;
		public float[] mCtrl1;
		public float[] mCtrl2;
		public int mId;
		public int mInterpolationType;
		public float mTimeEnd;
		public float mTimeStart;
	}

	private class XMLHandler extends HandlerBase {
		@Override
		public void startElement(String name, AttributeList attrs) {
			if (name.equals("m")) {
				mModels.add(parseVector(attrs));
			}
			if (name.equals("cp")) {
				mCameraPositions.add(parseVector(attrs));
			}
			if (name.equals("la")) {
				mCameraLookAts.add(parseVector(attrs));
			}
			if (name.equals("lp")) {
				mLightPositions.add(parseVector(attrs));
			}
			if (name.equals("fg")) {
				mForegroundColors.add(parseVector(attrs));
			}
		}
	}
}
