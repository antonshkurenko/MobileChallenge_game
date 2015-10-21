package com.mobilechallenge.game.activities;

import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.ConfigurationInfo;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;
import butterknife.Bind;
import butterknife.ButterKnife;
import com.mobilechallenge.game.R;
import com.mobilechallenge.game.controllers.GameMechanics;
import com.mobilechallenge.game.controllers.GameThread;
import com.mobilechallenge.game.utils.Gyroscope;
import com.mobilechallenge.game.views.GameGlSurfaceView;

public class GameActivity extends AppCompatActivity {

  @Bind(R.id.gl_surface) GameGlSurfaceView mGlSurfaceView;

  private Gyroscope mGyroscope;
  private GameThread mGameThread;

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

    if (mRenderSet) {
      mGlSurfaceView.onResume();
      mGameThread.setIsRunning(true);
    }

    mGyroscope.start();
  }

  @Override protected void onPause() {
    if (mRenderSet) {
      mGlSurfaceView.onPause();
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

      mGameThread = new GameThread(this, new GameMechanics(this, mGyroscope), mGlSurfaceView);
      mGameThread.start();
      mRenderSet = true;
    } else {
      Toast.makeText(this, "This device does not support OpenGL ES 2.0.", Toast.LENGTH_LONG).show();
      finish();
    }
  }
}
