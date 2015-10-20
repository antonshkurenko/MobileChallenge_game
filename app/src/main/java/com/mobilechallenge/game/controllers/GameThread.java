package com.mobilechallenge.game.controllers;

import android.content.Context;
import android.opengl.GLSurfaceView;
import com.mobilechallenge.game.renderers.GameRenderer;
import com.mobilechallenge.game.views.GameGlSurfaceView;

/**
 * Project: Game
 * Date: 10/20/15
 * Code style: SquareAndroid (https://github.com/square/java-code-styles)
 */
public class GameThread extends Thread {

  private static final int TICKS_PER_SECOND = 60;
  private static final int SKIP_TICKS = 1000 / TICKS_PER_SECOND;
  private static final int MAX_FRAMESKIP = 5;

  private GameState mGameState;
  private GameGlSurfaceView mView;

  private GameRenderer mRenderer;

  boolean mIsRunning = true;

  public GameThread(Context ctx, GameState state, GameGlSurfaceView view) {
    super();
    mGameState = state;
    mView = view;
    mRenderer = new GameRenderer(ctx, state);
    mView.setRenderer(mRenderer);
    mView.setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
  }

  public void setIsRunning(boolean isRunning) {
    mIsRunning = isRunning;
  }

  @Override public void run() {

    long nextGameTick = System.currentTimeMillis(); // start time
    int loops;
    float interpolation;

    while (mIsRunning) {

      loops = 0;
      while (System.currentTimeMillis() > nextGameTick && loops < MAX_FRAMESKIP) {

        mGameState.step();
        nextGameTick += SKIP_TICKS;
        loops++;
      }

      interpolation =
          (float) (System.currentTimeMillis() + SKIP_TICKS - nextGameTick) / (float) SKIP_TICKS;

      mRenderer.setInterpolation(interpolation);
      mView.requestRender();
    }
  }
}
