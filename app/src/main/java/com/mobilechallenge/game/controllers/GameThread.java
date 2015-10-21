package com.mobilechallenge.game.controllers;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import com.mobilechallenge.game.renderers.GameRenderer;
import com.mobilechallenge.game.utils.Gyroscope;
import com.mobilechallenge.game.views.GameGlSurfaceView;
import java.util.concurrent.TimeUnit;
import timber.log.Timber;

/**
 * Project: Game
 * Date: 10/20/15
 * Code style: SquareAndroid (https://github.com/square/java-code-styles)
 */
public class GameThread extends Thread {

  private static final int TICKS_PER_SECOND = 60;
  private static final int SKIP_TICKS = 1000 / TICKS_PER_SECOND;
  private static final int MAX_FRAMESKIP = 5;

  private GameMechanics mGameMechanics;
  private SharedPreferences mSharedPreferences;
  private GameGlSurfaceView mView;
  private GameRenderer mRenderer;

  private EventsCallback mEventsCallback;

  private boolean mIsRunning = false;
  private boolean mIsLost = false;
  private boolean mIsPreview = false;

  public GameThread(Context ctx, Gyroscope gyroscope, GameGlSurfaceView view,
      GameRenderer gameRenderer, boolean preview) {
    super();

    try {
      mEventsCallback = (EventsCallback) ctx;
    } catch (ClassCastException e) {
      throw new ClassCastException(ctx.toString() + " must implement LostCallback");
    }

    mGameMechanics = new GameMechanics(ctx, gyroscope);
    mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(ctx);

    mView = view;
    mRenderer = gameRenderer;

    mIsPreview = preview;

    initGameMechanics();
  }

  public GameMechanics getGameMechanics() {
    return mGameMechanics;
  }

  public synchronized void setIsRunning(boolean isRunning) {
    mIsRunning = isRunning;
    if (!isRunning && !mIsLost && !mIsPreview) {
      mGameMechanics.save();
    }
  }

  public boolean isRunning() {
    return mIsRunning;
  }

  public boolean isPreview() {
    return mIsPreview;
  }

  @Override public void run() {

    mIsRunning = true;
    mIsLost = false;

    mEventsCallback.onStartGame();

    long nextGameTick = System.currentTimeMillis(); // start time
    int loops;
    final long startTime = System.currentTimeMillis();
    final long startTimePassed = mGameMechanics.getTimePassed();
    long stepsTime;

    gameCycle:
    while (mIsRunning) {

      stepsTime = System.currentTimeMillis() - startTime;
      mGameMechanics.setTimePassed(stepsTime + startTimePassed);
      mEventsCallback.onUpdate(getFormattedTime(stepsTime + startTimePassed));

      loops = 0;
      while (System.currentTimeMillis() > nextGameTick && loops < MAX_FRAMESKIP) {

        if (!mGameMechanics.step()) {
          Timber.d("You lost");
          countInterpolation(nextGameTick);
          mView.requestRender();
          mIsLost = true;

          stepsTime = System.currentTimeMillis() - startTime;
          mGameMechanics.setTimePassed(stepsTime + startTimePassed);
          mEventsCallback.onLostGame(getFormattedTime(stepsTime + startTimePassed));
          break gameCycle; // You lost
        }
        nextGameTick += SKIP_TICKS;
        loops++;
      }
      countInterpolation(nextGameTick);
      mView.requestRender();
    }

    mEventsCallback.onEndGame();
    mIsRunning = false; // ready to run again
  }

  private String getFormattedTime(final long millis) {
    final long min = TimeUnit.MILLISECONDS.toMinutes(millis) % 60;
    final long sec = TimeUnit.MILLISECONDS.toSeconds(millis) % 60;
    final long mls = millis % 1000;
    return String.format("%02d:%02d:%03d", min, sec, mls);
  }

  private void initGameMechanics() {
    if (!mIsPreview) {
      if (mSharedPreferences.getBoolean(GameMechanics.PREFS_IS_SAVED, false)) {
        mGameMechanics.restoreGame();
        Timber.d("I'm restoring game.");
      } else {
        mGameMechanics.setGameLevel(
            mSharedPreferences.getInt(GameMechanics.PREFS_LEVEL, GameMechanics.GameParams.LEVEL1));
        mGameMechanics.initGame();
      }
    } else {
      mGameMechanics.setGameLevel(GameMechanics.GameParams.LEVEL_PREVIEW);
      mGameMechanics.initGame();
    }
  }

  private void countInterpolation(long nextGameTick) {
    final float interpolation =
        (float) (System.currentTimeMillis() + SKIP_TICKS - nextGameTick) / (float) SKIP_TICKS;

    mRenderer.setInterpolation(interpolation);
  }

  public interface EventsCallback {
    void onStartGame();

    void onUpdate(String time);

    void onLostGame(String finalTime);

    void onEndGame();
  }

}
