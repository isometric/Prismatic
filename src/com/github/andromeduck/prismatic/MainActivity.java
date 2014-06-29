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

import com.github.andromeduck.prismatic.graphics.GraphicsManager;

import android.app.Activity;
import android.content.res.Configuration;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.widget.Toast;

public class MainActivity extends Activity {

	private MediaPlayer mMediaPlayer;
    private GraphicsManager mRenderer;

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);

		// Create media player object.
		mMediaPlayer = MediaPlayer.create(this, R.raw.music);
		mMediaPlayer.setLooping(false);
		mMediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
					@Override
					public void onCompletion(MediaPlayer mp) {
						MainActivity.this.finish();
					}
				});

        Parser parser;
        try {
            parser = new Parser(getResources().openRawResource(
                    R.raw.script));
        } catch (Exception ex) {
			ex.printStackTrace();
			Toast.makeText(this, ex.getMessage(), Toast.LENGTH_LONG).show();
			finish();
			return;
		}

		// Create GLSurfaceView.
        mRenderer = new GraphicsManager(this, parser, mMediaPlayer);
        setContentView(mRenderer);

		mRenderer.setOnTouchListener(new View.OnTouchListener() {
			float mPositionX;

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				switch (event.getAction()) {
				case MotionEvent.ACTION_DOWN:
					mPositionX = event.getX();
					break;
				case MotionEvent.ACTION_MOVE:
					int diff = (int) ((mPositionX - event.getX()) * 100.0f);
					int newPos = mMediaPlayer.getCurrentPosition() + diff;
					if (newPos < 0) {
						newPos = 0;
					}
					if (newPos >= mMediaPlayer.getDuration()) {
						newPos = mMediaPlayer.getDuration();
					}
					mMediaPlayer.seekTo(newPos);
					mPositionX = event.getX();
				}
				return true;
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
		mRenderer.onPause();
		mMediaPlayer.pause();
	}

	@Override
	public void onResume() {
		super.onResume();
		mRenderer.onResume();
		mMediaPlayer.start();
	}

}
