package fi.harism.cubism;

import android.app.Activity;
import android.media.MediaPlayer;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.Window;

public class CubismActivity extends Activity {

	private GLQueueRunnable mGLQueueRunnable = new GLQueueRunnable();
	private GLSurfaceView mGLSurfaceView;
	private MediaPlayer mMediaPlayer;
	private CubismRenderer mRenderer;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Force full screen view.
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		// getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
		// getWindow().clearFlags(
		// WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);

		// Create GLSurfaceView.
		mRenderer = new CubismRenderer(this);
		mGLSurfaceView = new GLSurfaceView(this);
		mGLSurfaceView.setEGLContextClientVersion(2);
		mGLSurfaceView.setRenderer(mRenderer);
		mGLSurfaceView.setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);
		setContentView(mGLSurfaceView);
		queueGLEvent();

		mMediaPlayer = MediaPlayer.create(this, R.raw.loop);
		mMediaPlayer.setLooping(true);
		mMediaPlayer
				.setOnSeekCompleteListener(new MediaPlayer.OnSeekCompleteListener() {
					@Override
					public void onSeekComplete(MediaPlayer arg0) {
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
		mMediaPlayer.stop();
	}

	@Override
	public void onResume() {
		super.onResume();
		mGLSurfaceView.onResume();
		mMediaPlayer.setVolume(0, 0);
		new FadeInTimer(2000, 100).start();
		mMediaPlayer.start();
	}

	public void queueGLEvent() {
		mGLSurfaceView.queueEvent(mGLQueueRunnable);
	}

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

	private class GLQueueRunnable implements Runnable {
		@Override
		public void run() {
			mRenderer.onGLEvent();
		}
	}

}
