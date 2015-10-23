package com.mobilechallenge.game.utils;

/**
 * Project: Game
 * Date: 10/19/15
 * Code style: SquareAndroid (https://github.com/square/java-code-styles)
 */

import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.Display;
import android.view.Surface;
import android.view.WindowManager;
import timber.log.Timber;

public class Gyroscope
    implements SensorEventListener, SharedPreferences.OnSharedPreferenceChangeListener {

  public static final String PREFS_INVERT_X = "psychosocial";
  public static final String PREFS_INVERT_Y = "don_t_let_the_ones_that_want_to_steal_your_dreams";
  public static final String PREFS_SENSITIVITY = "but_the_most_special_are_the_most_lonely";

  public static final int STRAIGHT = 1;
  public static final int INVERSE = -1;

  private Context mContext;

  private SensorManager mSensorManager;

  private Display mDisplay;

  // @formatter:off
     private float[] mSensorX = new float[10];
    private float[  ] mSensorY = new float[10];
   private float[    ] mOrientationArray = new float[2];
  // @formatter:on

  private int mInvertX = STRAIGHT; // STRAIGHT or INVERSE
  private int mInvertY = STRAIGHT;

  private int mSensitivity = 50; // default

  private boolean mMessageShown = false;

  public Gyroscope(Context context) {
    mContext = context;
    mSensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
    mDisplay =
        ((WindowManager) context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
  }

  public void setInvertX(int invertX) {
    mInvertX = invertX;
  }

  public void setInvertY(int invertY) {
    mInvertY = invertY;
  }

  public void setSensitivity(int sensitivity) {
    mSensitivity = sensitivity;
  }

  /**
   * values[0]: X rotation
   * values[1]: Y rotation
   */

  public synchronized float[] getOrientationArray() {

    float avgX = 0, avgY = 0;
    for (int i = 0; i < 10; i++) {
      avgX += mSensorX[i];
      avgY += mSensorY[i];
    }
    avgX /= 10;
    avgY /= 10;
    mOrientationArray[0] = avgX * mInvertX / (100 - mSensitivity);
    mOrientationArray[1] = avgY * mInvertY / (100 - mSensitivity);

    return mOrientationArray;
  }

  public void start() {
    mSensorManager.registerListener(this,
        mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
        SensorManager.SENSOR_DELAY_GAME);
  }

  public void stop() {
    mSensorManager.unregisterListener(this);
  }

  @Override public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
    Timber.d("Preferences changed");
    switch (key) {
      case PREFS_INVERT_X:
        Timber.d("X inversion");
        mInvertX = sharedPreferences.getInt(key, STRAIGHT);
        break;
      case PREFS_INVERT_Y:
        Timber.d("Y inversion");
        mInvertY = sharedPreferences.getInt(key, STRAIGHT);
        break;
      case PREFS_SENSITIVITY:
        Timber.d("Sensitivity");
        mSensitivity = sharedPreferences.getInt(key, 50);
        break;
      default:
        //ignored
    }
  }

  @Override public void onAccuracyChanged(Sensor sensor, int accuracy) {
    // ignored
  }

  @Override public void onSensorChanged(SensorEvent event) {

    if (event.sensor.getType() != Sensor.TYPE_ACCELEROMETER) {
      return;
    }

    if (event.accuracy == SensorManager.SENSOR_STATUS_UNRELIABLE && !mMessageShown) {

      new WarningFragment().show(((AppCompatActivity) mContext).getFragmentManager(), "dialog");
      mMessageShown = true;
      return;
    }

    // fixme(me), 10/22/15: cool, but tablet inverts y
    switch (mDisplay.getRotation()) {
      case Surface.ROTATION_0:
        addSensorValue(event.values[0], -event.values[1]);
        break;
      case Surface.ROTATION_90:
        addSensorValue(-event.values[1], event.values[0]);
        break;
      case Surface.ROTATION_180:
        addSensorValue(-event.values[0], -event.values[1]);
        break;
      case Surface.ROTATION_270:
        addSensorValue(event.values[1], -event.values[0]);
        break;
    }
  }

  private void addSensorValue(float x, float y) {
    int length = mSensorX.length;
    int i = 0;
    while (i < length - 1) {
      mSensorX[i] = mSensorX[i + 1];
      mSensorY[i] = mSensorY[i + 1];
      i += 1;
    }
    mSensorX[9] = x;
    mSensorY[9] = y;
  }

  public static class WarningFragment extends DialogFragment {

    @Override public Dialog onCreateDialog(Bundle savedInstanceState) {
      return new AlertDialog.Builder(getActivity()).setTitle("Sensor is unreliable.")
          .setMessage("Game will use this accelerometer data,"
                  + " but, firstly for your good, you have to recalibrate it."
                  + "This message is shown just once per app launch.")
          .setIcon(android.R.drawable.ic_dialog_alert)
          .setPositiveButton("Ok :(", (dialog, which) -> dialog.dismiss())
          .show();
    }
  }
}