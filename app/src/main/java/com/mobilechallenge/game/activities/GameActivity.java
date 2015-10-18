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
import com.mobilechallenge.game.renderers.GameRenderer;
import com.mobilechallenge.game.views.GameGlSurfaceView;

public class GameActivity extends AppCompatActivity {

  @Bind(R.id.gl_surface) GameGlSurfaceView mGlSurfaceView;

  private boolean mRenderSet = false;

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_game);
    ButterKnife.bind(this);
    initSurface();
  }

  @Override
  protected void onResume() {
    super.onResume();

    if(mRenderSet) {
      mGlSurfaceView.onResume();
    }
  }

  @Override
  protected void onPause() {
    super.onPause();

    if(mRenderSet) {
      mGlSurfaceView.onPause();
    }
  }

  private void initSurface() {
    final ActivityManager activityManager =
        (ActivityManager)getSystemService(Context.ACTIVITY_SERVICE);
    final ConfigurationInfo configurationInfo =
        activityManager.getDeviceConfigurationInfo();
    final boolean supportEs2 =
        configurationInfo.reqGlEsVersion >= 0x20000;
    final GameRenderer renderer =
        new GameRenderer(this);

    if(supportEs2) {
      //Request an Open ES 2.0 compatible context.
      mGlSurfaceView.setEGLContextClientVersion(2);
      mGlSurfaceView.setRenderer(renderer);
      mRenderSet = true;
    } else {
      Toast.makeText(this, "This device does not support OpenGL ES 2.0.", Toast.LENGTH_LONG).show();
      finish();
    }
  }
}
