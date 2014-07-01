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

import com.github.andromeduck.prismatic.graphics.SceneManager;

import android.app.Activity;
import android.content.res.Configuration;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;

public class MainActivity extends Activity {

    private MediaPlayer mediaPlayer;
    private SceneManager sceneManager;

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        // Create media player object.
        mediaPlayer = MediaPlayer.create(this, R.raw.music);
        mediaPlayer.setLooping(false);
        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                MainActivity.this.finish();
            }
        });


        // Create GLSurfaceView.
        sceneManager = new SceneManager(this, mediaPlayer);
        setContentView(sceneManager);

        sceneManager.setOnTouchListener(new View.OnTouchListener() {

            float[] downPos = new float[2];

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        downPos[0] = event.getX();
                        downPos[1] = event.getY();
                        break;
                    case MotionEvent.ACTION_MOVE:
                        float deltaX = (event.getX() - downPos[0]);
                        float deltaY = (event.getY() - downPos[1]);

                        sceneManager.inputDir[0] = (float) (deltaX * Math.sqrt(2) / 2 + deltaY * 1 / 2) / 128f;
                        sceneManager.inputDir[1] = (float) (-deltaY * Math.sqrt(2) / 2 + -deltaX * 1 / 2) / 128f;
                        sceneManager.inputDir[2] = (float) (-deltaX * Math.sqrt(2) / 2 + deltaY * 1 / 2) / 128f;
                        break;
                    case MotionEvent.ACTION_UP:
                        sceneManager.inputDir[0] = 0;
                        sceneManager.inputDir[1] = 0;
                        sceneManager.inputDir[2] = 0;
                        break;
                }
                return true;
            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mediaPlayer.release();
    }

    @Override
    public void onPause() {
        super.onPause();
        sceneManager.onPause();
        mediaPlayer.pause();
    }

    @Override
    public void onResume() {
        super.onResume();
        sceneManager.onResume();
        mediaPlayer.start();
    }

}
