package com.mobilechallenge.game.activities;

import android.app.ActivityManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ConfigurationInfo;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import butterknife.Bind;
import butterknife.BindString;
import butterknife.ButterKnife;
import butterknife.OnClick;
import com.mobilechallenge.game.R;
import com.mobilechallenge.game.controllers.GameMechanics;
import com.mobilechallenge.game.controllers.GameThread;
import com.mobilechallenge.game.renderers.GameRenderer;
import com.mobilechallenge.game.utils.Gyroscope;
import com.mobilechallenge.game.views.GameGlSurfaceView;

public class GameActivity extends AppCompatActivity implements GameThread.LostCallback {

  @Bind(R.id.gl_surface) GameGlSurfaceView mGlSurfaceView;
  @Bind(R.id.start_button) Button mStart;

  @BindString(R.string.start) String mStartString;
  @BindString(R.string.resume) String mResumeString;

  private Gyroscope mGyroscope;
  private GameThread mGameThread;
  private GameRenderer mGameRenderer;

  private SharedPreferences mSharedPreferences;

  private boolean mRenderSet = false;

  @OnClick(R.id.gl_surface) void onGlTouch() {

    if (mGameThread.isRunning()) {
      mGameThread.setIsRunning(false);
    }

    if (mStart.getVisibility() == View.VISIBLE) {

      mStart.setVisibility(View.GONE);
      if (mRenderSet) {
        mGameThread = getNewThread(false);
        mGameRenderer.setGameMechanics(mGameThread.getGameMechanics());
        mGlSurfaceView.onResume();
        mGameThread.start();
      }
    } else {
      mStart.setText(mResumeString);
      mStart.setVisibility(View.VISIBLE);
    }
  }

  @Override public void onLost() {
    runOnUiThread(() -> {
      mStart.setText(mStartString);
      mStart.setVisibility(View.VISIBLE);
    });

    mGameThread = getNewThread(true);
    mGameRenderer.setGameMechanics(mGameThread.getGameMechanics());
    mGlSurfaceView.onResume();
    mGameThread.start();
  }

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_game);
    ButterKnife.bind(this);
    mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

    mGyroscope = new Gyroscope(this);
    initSurface();
  }

  @Override protected void onResume() {
    super.onResume();

    if (mSharedPreferences.getBoolean(GameMechanics.PREFS_IS_SAVED, false)) {
      mStart.setText(mResumeString);
    } else {
      mStart.setText(mStartString);
    }

    mGyroscope.start();
  }

  @Override protected void onPause() {
    if (mRenderSet) {
      mGlSurfaceView.onPause();
    }

    if (mGameThread.isRunning()) {
      mGameThread.setIsRunning(false);
    }

    super.onPause();
  }

  @Override protected void onStop() {
    mGyroscope.stop();
    super.onStop();
  }

  private void initSurface() {
    final ActivityManager activityManager =
        (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
    final ConfigurationInfo configurationInfo = activityManager.getDeviceConfigurationInfo();
    final boolean supportEs2 = configurationInfo.reqGlEsVersion >= 0x20000;

    if (supportEs2) {
      //Request an Open ES 2.0 compatible context.
      mGlSurfaceView.setEGLContextClientVersion(2);
      mGameRenderer = new GameRenderer(this);
      mGameThread = getNewThread(true);
      mGameRenderer.setGameMechanics(mGameThread.getGameMechanics());
      mGlSurfaceView.setRenderer(mGameRenderer);
      mGlSurfaceView.setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
      mGameThread.start();
      mRenderSet = true;
    } else {
      Toast.makeText(this, "This device does not support OpenGL ES 2.0.", Toast.LENGTH_LONG).show();
      finish();
    }
  }

  private GameThread getNewThread(boolean preview) {
    return new GameThread(this, mGyroscope, mGlSurfaceView, mGameRenderer, preview);
  }
}
