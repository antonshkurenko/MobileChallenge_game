package com.mobilechallenge.game.activities;

import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.ConfigurationInfo;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;
import butterknife.Bind;
import butterknife.ButterKnife;
import com.mobilechallenge.game.R;
import com.mobilechallenge.game.controllers.GameThread;
import com.mobilechallenge.game.renderers.GameRenderer;
import com.mobilechallenge.game.utils.Gyroscope;
import com.mobilechallenge.game.views.GameGlSurfaceView;

public class GameActivity extends AppCompatActivity {

  @Bind(R.id.gl_surface) GameGlSurfaceView mGlSurfaceView;

  private Gyroscope mGyroscope;
  private GameThread mGameThread;
  private GameRenderer mGameRenderer;

  private boolean mRenderSet = false;

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_game);
    ButterKnife.bind(this);

    mGyroscope = new Gyroscope(this);
    initSurface();
  }

  @Override protected void onResume() {
    super.onResume();

    mGyroscope.start();
    if (mRenderSet) {
      mGameThread = new GameThread(this, mGyroscope, mGlSurfaceView, mGameRenderer);
      mGameRenderer.setGameMechanics(mGameThread.getGameMechanics());
      mGlSurfaceView.onResume();
      mGameThread.start();
    }
  }

  @Override protected void onPause() {
    if (mRenderSet) {
      mGlSurfaceView.onPause();
    }

    if(mGameThread.isRunning()) {
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
      mGlSurfaceView.setRenderer(mGameRenderer);
      mGlSurfaceView.setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
      mRenderSet = true;
    } else {
      Toast.makeText(this, "This device does not support OpenGL ES 2.0.", Toast.LENGTH_LONG).show();
      finish();
    }
  }
}
