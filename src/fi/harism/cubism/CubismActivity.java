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

import android.app.Activity;
import android.content.res.Configuration;
import android.media.MediaPlayer;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.Window;

public class CubismActivity extends Activity {

	private GLSurfaceView mGLSurfaceView;
	private MediaPlayer mMediaPlayer;
	private CubismRenderer mRenderer;

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);

		// Create GLSurfaceView.
		mRenderer = new CubismRenderer(this);
		mGLSurfaceView = new GLSurfaceView(this);
		mGLSurfaceView.setEGLContextClientVersion(2);
		mGLSurfaceView.setRenderer(mRenderer);
		mGLSurfaceView.setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);
		setContentView(mGLSurfaceView);

		// Create media player object.
		mMediaPlayer = MediaPlayer.create(this, R.raw.loop);
		mMediaPlayer.setLooping(true);
		mMediaPlayer
				.setOnSeekCompleteListener(new MediaPlayer.OnSeekCompleteListener() {
					@Override
					public void onSeekComplete(MediaPlayer arg0) {
						// Tell renderer music is looping from the start.
						mRenderer.onMusicRepeat();
					}
				});
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		mMediaPlayer.release();
	}

	@Override
	public void onPause() {
		super.onPause();
		mGLSurfaceView.onPause();
		mRenderer.onPause();
		mMediaPlayer.stop();
	}

	@Override
	public void onResume() {
		super.onResume();
		mGLSurfaceView.onResume();
		mRenderer.onResume();
		mMediaPlayer.setVolume(0, 0);
		new FadeInTimer(2000, 100).start();
		mMediaPlayer.start();
	}

	/**
	 * Private timer for fading in the music.
	 */
	private class FadeInTimer extends CountDownTimer {

		private long mMillisInFuture;

		public FadeInTimer(long millisInFuture, long countDownInterval) {
			super(millisInFuture, countDownInterval);
			mMillisInFuture = millisInFuture;
		}

		@Override
		public void onFinish() {
			mMediaPlayer.setVolume(0.5f, 0.5f);
		}

		@Override
		public void onTick(long millisUntilFinished) {
			float v = (mMillisInFuture - millisUntilFinished)
					/ (float) (mMillisInFuture * 2);
			mMediaPlayer.setVolume(v, v);
		}

	}

}
