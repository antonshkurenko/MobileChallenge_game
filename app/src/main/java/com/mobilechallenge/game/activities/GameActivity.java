package com.mobilechallenge.game.activities;

import android.app.ActivityManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ConfigurationInfo;
import android.graphics.Typeface;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
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
import timber.log.Timber;

public class GameActivity extends AppCompatActivity
    implements GameThread.EventsCallback, SeekBar.OnSeekBarChangeListener {

  @Bind(R.id.gl_surface) GameGlSurfaceView mGlSurfaceView;
  @Bind(R.id.start_button) Button mStart;
  @Bind(R.id.timer) TextView mTimerText;
  @Bind(R.id.difficulty_level_bar) SeekBar mDifficultyBar;
  @Bind(R.id.difficulty_text) TextView mDifficultyText;
  @Bind(R.id.score_label) TextView mScoreLabel;
  @Bind(R.id.score) TextView mScore;
  @Bind(R.id.score_layout) LinearLayout mScoreLayout;

  @BindString(R.string.start) String mStartString;
  @BindString(R.string.resume) String mResumeString;

  private Gyroscope mGyroscope;
  private GameThread mGameThread;
  private GameRenderer mGameRenderer;

  private SharedPreferences mSharedPreferences;

  private boolean mRenderSet = false;

  private boolean mScoreOpened = false;

  @OnClick(R.id.gl_surface) void toggleState() {
    if (mScoreOpened) {
      closeScore();
      return;
    }

    resumePause();
  }

  @OnClick(R.id.start_button) void resumePause() {
    if (mStart.getVisibility() == View.VISIBLE) {
      resume();
    } else {
      pause();
    }
  }

  @OnClick(R.id.score_layout) void closeScore() {
    mScoreLayout.setVisibility(View.GONE);
    mStart.setText(mStartString);
    mStart.setVisibility(View.VISIBLE);
    mDifficultyBar.setVisibility(View.VISIBLE);
    mDifficultyText.setVisibility(View.VISIBLE);
    mScoreOpened = false;
  }

  @Override public void onStartGame() {
    if (!mGameThread.isPreview()) {
      runOnUiThread(() -> mTimerText.setVisibility(View.VISIBLE));
    }
  }

  @Override public void onUpdate(String time) {
    if (!mGameThread.isPreview()) {
      runOnUiThread(() -> mTimerText.setText(time));
    }
  }

  @Override public void onLostGame(String finalTime) {

    mScoreOpened = true;
    runOnUiThread(() -> {
      mScore.setText(finalTime);
      mScoreLayout.setVisibility(View.VISIBLE);
    });

    Timber.d("Lost with result %s.", finalTime);
    mGameThread = getNewThread(true);
    mGameRenderer.setGameMechanics(mGameThread.getGameMechanics());
    mGameThread.start();
  }

  @Override public void onEndGame() {
    if (mGameThread.isPreview()) {
      runOnUiThread(() -> mTimerText.setVisibility(View.GONE));
    }
  }

  @Override public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
    mDifficultyText.setText((progress + 1) + "");
  }

  @Override public void onStartTrackingTouch(SeekBar seekBar) {
    // ignored
  }

  @Override public void onStopTrackingTouch(SeekBar seekBar) {
    mSharedPreferences.edit().putInt(GameMechanics.PREFS_LEVEL, seekBar.getProgress() + 1).apply();
  }

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_game);
    ButterKnife.bind(this);

    final Typeface typeface = Typeface.createFromAsset(getAssets(), "fonts/Lobster-Regular.ttf");
    mStart.setTypeface(typeface);
    mDifficultyText.setTypeface(typeface);
    mTimerText.setTypeface(typeface);
    mScoreLabel.setTypeface(typeface);
    mScore.setTypeface(typeface);
    mDifficultyBar.setOnSeekBarChangeListener(this);

    mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

    final int difficulty = mSharedPreferences.getInt(GameMechanics.PREFS_LEVEL, 1);

    mDifficultyBar.setProgress(difficulty - 1);
    mDifficultyText.setText(difficulty + "");

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

    Timber.d("OnResume called and mStart has this text: %s", mStart.getText().toString());

    if (mRenderSet) {
      mGlSurfaceView.onResume();
    }

    mGyroscope.start();
  }

  @Override protected void onPause() {
    if (mRenderSet) {
      mGlSurfaceView.onPause();
    }

    if(mScoreOpened) {
      closeScore();
    }

    pause();
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

      final boolean saved = mSharedPreferences.getBoolean(GameMechanics.PREFS_IS_SAVED, false);

      if (saved) {
        mGameThread = getNewThread(false); // new thread with saved data

        // because it sets to false, when restoring, and this isn't full restore, just preview
        mSharedPreferences.edit().putBoolean(GameMechanics.PREFS_IS_SAVED, true).apply();
      } else {
        mGameThread = getNewThread(true); // preview
      }
      mGameRenderer.setGameMechanics(mGameThread.getGameMechanics());
      mGlSurfaceView.setRenderer(mGameRenderer);
      mGlSurfaceView.setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);

      if (saved) {
        mGlSurfaceView.requestRender(); // render last frame, to see your position
        mStart.setText(mResumeString);
      } else {
        mGameThread.start(); // start preview
        mStart.setText(mStartString);
        mDifficultyBar.setVisibility(View.VISIBLE);
        mDifficultyText.setVisibility(View.VISIBLE);
      }

      Timber.d("InitSurface, game is saved: %b.", saved);
      mRenderSet = true;
    } else {
      Toast.makeText(this, "This device does not support OpenGL ES 2.0.", Toast.LENGTH_LONG).show();
      finish();
    }
  }

  private GameThread getNewThread(boolean preview) {
    return new GameThread(this, mGyroscope, mGlSurfaceView, mGameRenderer, preview);
  }

  private void pause() {
    if (mGameThread.isRunning()) {
      mGameThread.setIsRunning(false); // pause game
    }

    mStart.setText(mResumeString);
    mStart.setVisibility(View.VISIBLE);
  }

  private void resume() {
    if (mGameThread.isRunning()) {
      mGameThread.setIsRunning(false); // if preview is running
    }

    mStart.setVisibility(View.GONE);
    mDifficultyBar.setVisibility(View.GONE);
    mDifficultyText.setVisibility(View.GONE);
    if (mRenderSet) {
      mGameThread = getNewThread(false); // start new thread
      mGameRenderer.setGameMechanics(mGameThread.getGameMechanics());
      mGameThread.start();
    }
  }
}
