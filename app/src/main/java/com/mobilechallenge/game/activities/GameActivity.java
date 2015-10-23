package com.mobilechallenge.game.activities;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ConfigurationInfo;
import android.graphics.Typeface;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageButton;
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

/**
 * I didn't think a lot about views here
 * so here is view hell with sub-hells of visibility setters
 */
public class GameActivity extends AppCompatActivity
    implements GameThread.EventsCallback, SeekBar.OnSeekBarChangeListener {

  @Bind(R.id.gl_surface) GameGlSurfaceView mGlSurfaceView;
  @Bind(R.id.start_button) Button mStart;
  @Bind(R.id.timer) TextView mTimerText;
  @Bind(R.id.difficulty_level_bar) SeekBar mDifficultyBar;
  @Bind(R.id.difficulty_text) TextView mDifficultyText;
  @Bind(R.id.level_label) TextView mLevelLabel;
  @Bind(R.id.score_label) TextView mScoreLabel;
  @Bind(R.id.highscore_label) TextView mHighscoreLabel;
  @Bind(R.id.highscore) TextView mHighscore;
  @Bind(R.id.score) TextView mScore;
  @Bind(R.id.score_layout) LinearLayout mScoreLayout;
  @Bind(R.id.share) TextView mShare;
  @Bind(R.id.invert_x) CheckBox mInvertX;
  @Bind(R.id.invert_y) CheckBox mInvertY;
  @Bind(R.id.sensitivity_label) TextView mSensitivityLabel;
  @Bind(R.id.sensitivity) SeekBar mSensitivity;
  @Bind(R.id.settings) LinearLayout mSettings;
  @Bind(R.id.open_settings) ImageButton mOpenSettings;

  @BindString(R.string.start) String mStartString;
  @BindString(R.string.resume) String mResumeString;

  private static final int SHARE_REQ = 22;

  private Gyroscope mGyroscope;
  private GameThread mGameThread;
  private GameRenderer mGameRenderer;

  private SharedPreferences mSharedPreferences;

  private boolean mRenderSet = false;

  // todo(me), 10/23/15: seems like adding more views this way to this layout is a little problem
  // think about smth else
  // a) state pattern
  // b) transparent fragments
  private boolean mScoreOpened = false;
  private boolean mSettingsOpened = false;

  @OnClick(R.id.gl_surface) void toggleState() {
    if (mScoreOpened) {
      closeScore();
      return;
    }

    if (mSettingsOpened) {
      toggleSettings();
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
    mStart.setText(mStartString);
    mScoreLayout.setVisibility(View.GONE);
    mShare.setVisibility(View.GONE);
    mStart.setVisibility(View.VISIBLE);
    mDifficultyBar.setVisibility(View.VISIBLE);
    mDifficultyText.setVisibility(View.VISIBLE);
    mLevelLabel.setVisibility(View.VISIBLE);
    mOpenSettings.setVisibility(View.VISIBLE);
    mScoreOpened = false;
  }

  // god damn me in the past
  @OnClick(R.id.open_settings) void toggleSettings() {
    if(mSettings.getVisibility() == View.VISIBLE) {
      mSettingsOpened = false;
      mSettings.setVisibility(View.GONE);
      mStart.setVisibility(View.VISIBLE);

      // really, think about better architecture
      if(mStart.getText().toString().equals(mStartString)) {
        mDifficultyText.setVisibility(View.VISIBLE);
        mDifficultyBar.setVisibility(View.VISIBLE);
        mLevelLabel.setVisibility(View.VISIBLE);
      }
    } else {
      mStart.setVisibility(View.GONE);

      // unbelievable
      if(mStart.getText().toString().equals(mStartString)) {
        mDifficultyText.setVisibility(View.GONE);
        mDifficultyBar.setVisibility(View.GONE);
        mLevelLabel.setVisibility(View.GONE);
      }
      mSettings.setVisibility(View.VISIBLE);
      mSettingsOpened = true;
    }
  }

  @OnClick(R.id.share) void share() {
    final Intent sendIntent = new Intent();
    sendIntent.setAction(Intent.ACTION_SEND);
    final int lvl = mSharedPreferences.getInt(GameMechanics.PREFS_LEVEL,
        GameMechanics.GameParams.LEVEL_PREVIEW);
    sendIntent.putExtra(Intent.EXTRA_TEXT,
        "I held on for " + mScore.getText().toString() + " on " + lvl
            + " difficulty level in \"Game\". Can you beat my result?");
    sendIntent.setType("text/plain");
    startActivityForResult(sendIntent, SHARE_REQ);
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

  @Override public void onLostGame(String finalTime, String highscore) {

    mScoreOpened = true;
    runOnUiThread(() -> {
      mScore.setText(finalTime);
      mHighscore.setText(highscore);
      mScoreLayout.setVisibility(View.VISIBLE);
      mShare.setVisibility(View.VISIBLE);
      mOpenSettings.setVisibility(View.GONE);
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

    getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
        WindowManager.LayoutParams.FLAG_FULLSCREEN);

    final Typeface typeface = Typeface.createFromAsset(getAssets(), "fonts/Roboto-Regular.ttf");
    mStart.setTypeface(typeface);
    mDifficultyText.setTypeface(typeface);
    mLevelLabel.setTypeface(typeface);
    mTimerText.setTypeface(typeface);
    mScoreLabel.setTypeface(typeface);
    mHighscoreLabel.setTypeface(typeface);
    mHighscore.setTypeface(typeface);
    mScore.setTypeface(typeface);
    mShare.setTypeface(typeface);
    mInvertX.setTypeface(typeface);
    mInvertY.setTypeface(typeface);
    mSensitivityLabel.setTypeface(typeface);

    mDifficultyBar.setOnSeekBarChangeListener(this);

    mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

    final int difficulty =
        mSharedPreferences.getInt(GameMechanics.PREFS_LEVEL, GameMechanics.GameParams.LEVEL5);

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

    if (mRenderSet) {
      mGlSurfaceView.onResume();
    }

    mGyroscope.start();
  }

  @Override protected void onPause() {
    if (mRenderSet) {
      mGlSurfaceView.onPause();
    }

    if (mScoreOpened) {
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
        mLevelLabel.setVisibility(View.VISIBLE);
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

    if (mSharedPreferences.getBoolean(GameMechanics.PREFS_IS_SAVED, false)) {
      mStart.setText(mResumeString);
    } else {
      mStart.setText(mStartString);
    }

    mStart.setVisibility(View.VISIBLE);
    mOpenSettings.setVisibility(View.VISIBLE);
  }

  private void resume() {
    if (mGameThread.isRunning()) {
      mGameThread.setIsRunning(false); // if preview is running
    }

    mStart.setVisibility(View.GONE);
    mDifficultyBar.setVisibility(View.GONE);
    mDifficultyText.setVisibility(View.GONE);
    mLevelLabel.setVisibility(View.GONE);
    mOpenSettings.setVisibility(View.GONE);
    if (mRenderSet) {
      mGameThread = getNewThread(false); // start new thread
      mGameRenderer.setGameMechanics(mGameThread.getGameMechanics());
      mGameThread.start();
    }
  }
}
