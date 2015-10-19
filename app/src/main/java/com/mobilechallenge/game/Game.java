package com.mobilechallenge.game;

import android.app.Application;
import timber.log.Timber;

/**
 * Project: Game
 * Date: 10/19/15
 * Code style: SquareAndroid (https://github.com/square/java-code-styles)
 */
public class Game extends Application {

  @Override public void onCreate() {
    super.onCreate();
    Timber.plant(new Timber.DebugTree());
  }
}
