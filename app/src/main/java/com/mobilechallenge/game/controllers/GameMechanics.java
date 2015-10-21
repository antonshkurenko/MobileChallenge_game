package com.mobilechallenge.game.controllers;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.IntDef;
import android.support.annotation.Nullable;
import com.google.gson.Gson;
import com.mobilechallenge.game.objects.ChipObject;
import com.mobilechallenge.game.objects.Drawable;
import com.mobilechallenge.game.objects.EnemyObject;
import com.mobilechallenge.game.utils.FileWriter;
import com.mobilechallenge.game.utils.Geometry;
import com.mobilechallenge.game.utils.Gyroscope;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import timber.log.Timber;

/**
 * Project: Game
 * Date: 10/20/15
 * Code style: SquareAndroid (https://github.com/square/java-code-styles)
 */
public class GameMechanics {

  public static final String PREFS_IS_SAVED = "hey_i_just_met_you";
  public static final String PREFS_LEVEL = "relax_don_t_do_it";

  private static final float RIGHT_BOUND = 1f;
  private static final float LEFT_BOUND = -1f;
  private static final float TOP_BOUND = 1f;
  private static final float BOTTOM_BOUND = -1f;

  private final Context mContext;
  private final Gyroscope mGyroscope;
  private final Random mRandom;
  private final SharedPreferences mSharedPreferences;

  // objects
  @Nullable private ChipObject mChipObject;
  private List<EnemyObject> mEnemyObjects;

  // params
  private Geometry.Vector mStartEnemyVector;
  private float mMaxSpeed;
  private float mMultiplierSpeed;
  private int mDifficultyLevel = GameParams.LEVEL_PREVIEW;

  private float mAspectRatio = 1f;

  private boolean mIsInited = false;

  public GameMechanics(Context ctx, Gyroscope gyroscope) {
    mContext = ctx;
    mGyroscope = gyroscope;
    mRandom = new Random();
    mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);
  }

  public GameMechanics setGameLevel(int level) {
    mDifficultyLevel = level;
    return this;
  }

  public GameMechanics setAspectRatio(float aspectRatio) {
    Timber.i("Aspect ratio from setter is %f.", aspectRatio);
    mAspectRatio = aspectRatio;
    return this;
  }

  public GameMechanics setChipView(Drawable chip) {
    if (mChipObject != null) {
      mChipObject.setImage(chip);
    }
    return this;
  }

  public GameMechanics setEnemyView(Drawable enemy) {
    for (EnemyObject e : mEnemyObjects) {
      e.setImage(enemy);
    }
    return this;
  }

  public boolean isInited() {
    return mIsInited;
  }

  @Nullable public synchronized ChipObject getChipObject() {
    return mChipObject;
  }

  public synchronized List<EnemyObject> getEnemyObjects() {
    return mEnemyObjects;
  }

  public void initGame() {
    createGameFromParams(GameParams.level(mRandom, mDifficultyLevel, mAspectRatio));
  }

  public void restoreGame() {
    createGameFromParams(GameParams.restore(mContext));
    mSharedPreferences.edit().putBoolean(PREFS_IS_SAVED, false).apply();
  }

  public synchronized void save() {

    if (!mIsInited) {
      throw new IllegalStateException("Saving not initialized game.");
    }

    GameParams.Builder builder = new GameParams.Builder();

    if (mChipObject != null) {
      builder.setChipPosition(mChipObject.getPosition()).setChipSpeed(mChipObject.getSpeed());
    }

    builder.setStartEnemyVector(mStartEnemyVector)
        .setDifficultyLevel(mDifficultyLevel)
        .setMaxSpeed(mMaxSpeed)
        .setMultiplierSpeed(mMultiplierSpeed)
        .setAspectRatio(mAspectRatio);

    final List<Geometry.Point> enemyPositions = new ArrayList<>();
    final List<Geometry.Vector> enemyVectors = new ArrayList<>();
    for (EnemyObject enemy : mEnemyObjects) {
      enemyVectors.add(enemy.getSpeed());
      enemyPositions.add(enemy.getPosition());
    }

    builder.setEnemyPositions(enemyPositions)
        .setEnemyVectors(enemyVectors)
        .build()
        .save(mContext);  // and save

    mSharedPreferences.edit().putBoolean(PREFS_IS_SAVED, true).apply();
  }

  public void createGameFromParams(GameParams params) {

    if (params.getDifficultyLevel() != GameParams.LEVEL_PREVIEW) {
      mChipObject = new ChipObject(params.getChipPosition(), params.getChipSpeed());
    } else {
      mChipObject = null;
    }

    mEnemyObjects = new ArrayList<>();

    // @formatter:off
    for(int i = 4; i -->0 ;) {
    // @formatter:on
      mEnemyObjects.add(
          new EnemyObject(params.getEnemyPositions().get(i), params.getEnemyVectors().get(i)));
    }

    mMaxSpeed = params.getMaxSpeed();
    mMultiplierSpeed = params.getMultiplierSpeed();
    mStartEnemyVector = params.getStartEnemyVector();
    mDifficultyLevel = params.getDifficultyLevel();
    mAspectRatio = params.getAspectRatio();
    Timber.d("Aspect ration from params is %f.", mAspectRatio);

    mIsInited = true;
  }

  public synchronized boolean step() {

    if (!mIsInited) {
      return true; // skip step
    }

    final float[] orientation = mGyroscope.getOrientationArray(); // 0 x, 1 y

    final Geometry.Circle chipCircle;
    if (mChipObject != null) {
      mChipObject.setSpeed(new Geometry.Vector(-orientation[0] / 100, -orientation[1] / 100));
      mChipObject.move();

      final Geometry.Point chipPosition = mChipObject.getPosition();
      chipCircle = new Geometry.Circle(chipPosition, ChipObject.RADIUS);

      // if touch any side
      if (chipPosition.x < LEFT_BOUND + ChipObject.RADIUS / mAspectRatio
          || chipPosition.x > RIGHT_BOUND - ChipObject.RADIUS / mAspectRatio
          || chipPosition.y > TOP_BOUND - ChipObject.RADIUS
          || chipPosition.y < BOTTOM_BOUND + ChipObject.RADIUS) {
        Timber.d("Aspect ratio is %f", mAspectRatio);
        Timber.d("Lost by touching bounds. Pos is (%f,%f).", chipPosition.x, chipPosition.y);
        return false; // lose
      }

      mChipObject.setPosition(new Geometry.Point(
          clamp(chipPosition.x, LEFT_BOUND + ChipObject.RADIUS / mAspectRatio,
              RIGHT_BOUND - ChipObject.RADIUS / mAspectRatio),
          clamp(chipPosition.y, BOTTOM_BOUND + ChipObject.RADIUS, TOP_BOUND - ChipObject.RADIUS)));
    } else {
      chipCircle = null;
    }

    for (int i = 0; i < 4; i++) {

      final EnemyObject enemy = mEnemyObjects.get(i);

      enemy.move();
      enemy.scaleSpeed(1.002f);

      final Geometry.Point position = enemy.getPosition();

      // if touch any side
      if (position.x < LEFT_BOUND + EnemyObject.RADIUS / mAspectRatio
          || position.x > RIGHT_BOUND - EnemyObject.RADIUS / mAspectRatio
          || position.y > TOP_BOUND - EnemyObject.RADIUS
          || position.y < BOTTOM_BOUND + EnemyObject.RADIUS) {
        enemy.rotateRandom(mRandom);
      }

      enemy.setPosition(new Geometry.Point(
          clamp(position.x, LEFT_BOUND + EnemyObject.RADIUS / mAspectRatio,
              RIGHT_BOUND - EnemyObject.RADIUS / mAspectRatio),
          clamp(position.y, BOTTOM_BOUND + EnemyObject.RADIUS, TOP_BOUND - EnemyObject.RADIUS)));

      if (chipCircle != null) {
        final Geometry.Circle enemyCircle = new Geometry.Circle(position, EnemyObject.RADIUS);
        if (enemyCircle.softIntersects(chipCircle)) {
          Timber.d("Lost by touching enemies.");
          return false; // you lost
        }
      }
    }
    return true;
  }

  /**
   * Keep object in the bounds of the deck
   *
   * @param value current x/y
   * @param min deck min x/y
   * @param max deck max x/y
   * @return return x/y
   */
  private float clamp(float value, float min, float max) {
    return Math.min(max, Math.max(value, min));
  }

  /**
   * This class stands for saving, for restoring and for creating different difficulty
   */

  public static class GameParams {

    public static final String FILE_NAME = "save";

    public static final int LEVEL_PREVIEW = 0;
    public static final int LEVEL1 = 1;
    public static final int LEVEL2 = 2;
    public static final int LEVEL3 = 3;
    public static final int LEVEL4 = 4;
    public static final int LEVEL5 = 5;
    public static final int LEVEL6 = 6;
    public static final int LEVEL7 = 7;
    public static final int LEVEL8 = 8;
    public static final int LEVEL9 = 9;
    public static final int LEVEL10 = 10;

    private Geometry.Point mChipPosition;
    private Geometry.Vector mChipSpeed;
    private List<Geometry.Point> mEnemyPositions;
    private List<Geometry.Vector> mEnemyVectors;
    private Geometry.Vector mStartEnemyVector;
    private float mMaxSpeed;
    private float mMultiplierSpeed;
    private int mDifficultyLevel;
    private float mAspectRatio;

    /**
     * for Gson
     */
    public GameParams() {

    }

    /**
     * builder access
     */

    private GameParams(Geometry.Point chipPosition, Geometry.Vector chipSpeed,
        List<Geometry.Point> enemyPositions, List<Geometry.Vector> enemyVectors,
        Geometry.Vector startEnemyVector, float maxSpeed, float multiplierSpeed,
        int difficultyLevel, float aspectRatio) {
      this.mChipPosition = chipPosition;
      this.mChipSpeed = chipSpeed;
      this.mEnemyPositions = enemyPositions;
      this.mEnemyVectors = enemyVectors;
      this.mStartEnemyVector = startEnemyVector;
      this.mMaxSpeed = maxSpeed;
      this.mMultiplierSpeed = multiplierSpeed;
      this.mDifficultyLevel = difficultyLevel;
      this.mAspectRatio = aspectRatio;
    }

    public static GameParams level(Random rnd, @DifficultyLevel int level, float aspectRatio) {

      final float rightX = RIGHT_BOUND - EnemyObject.RADIUS / aspectRatio;
      final float topY = TOP_BOUND - EnemyObject.RADIUS;
      final float leftX = LEFT_BOUND + EnemyObject.RADIUS / aspectRatio;
      final float bottomY = BOTTOM_BOUND + EnemyObject.RADIUS;

      final Geometry.Vector startEnemyVector =
          new Geometry.Vector(0.005f, 0.005f).scale((10f + level) / 10f);

      return new Builder().setAspectRatio(aspectRatio)
          .setEnemyPositions(new ArrayList<Geometry.Point>() {{
            add(new Geometry.Point(rightX, topY));
            add(new Geometry.Point(rightX, bottomY));
            add(new Geometry.Point(leftX, topY));
            add(new Geometry.Point(leftX, bottomY));
          }})
          .setEnemyVectors(new ArrayList<Geometry.Vector>() {{
            add(startEnemyVector.rotateRandom(rnd));
            add(startEnemyVector.rotateRandom(rnd));
            add(startEnemyVector.rotateRandom(rnd));
            add(startEnemyVector.rotateRandom(rnd));
          }})
          .setStartEnemyVector(startEnemyVector)
          .setMaxSpeed(0.01f * level * level / 2f).setMultiplierSpeed(
              0.0001f * level) // todo(me), 10/21/15: calculate (30 sec / diffLevel)
          .setDifficultyLevel(level).build();
    }

    public static GameParams restore(Context ctx) {
      return new Gson().fromJson(FileWriter.read(ctx, FILE_NAME), GameParams.class);
    }

    public synchronized void save(Context ctx) {
      FileWriter.write(ctx, FILE_NAME, new Gson().toJson(this));
    }

    /**
     * LIST OF GETTERS
     */

    public Geometry.Point getChipPosition() {
      return mChipPosition;
    }

    public Geometry.Vector getChipSpeed() {
      return mChipSpeed;
    }

    public List<Geometry.Point> getEnemyPositions() {
      return mEnemyPositions;
    }

    public List<Geometry.Vector> getEnemyVectors() {
      return mEnemyVectors;
    }

    public Geometry.Vector getStartEnemyVector() {
      return mStartEnemyVector;
    }

    public float getMaxSpeed() {
      return mMaxSpeed;
    }

    public float getMultiplierSpeed() {
      return mMultiplierSpeed;
    }

    public int getDifficultyLevel() {
      return mDifficultyLevel;
    }

    public float getAspectRatio() {
      return mAspectRatio;
    }

    /*****************************************/

    @IntDef({
        LEVEL_PREVIEW, LEVEL1, LEVEL2, LEVEL3, LEVEL4, LEVEL5, LEVEL6, LEVEL7, LEVEL8, LEVEL9,
        LEVEL10
    }) @Retention(RetentionPolicy.SOURCE) public @interface DifficultyLevel {

    }

    public static class Builder {

      private Geometry.Point mChipPosition = new Geometry.Point(0f, 0f);
      private Geometry.Vector mChipSpeed = new Geometry.Vector(0f, 0f);
      private List<Geometry.Point> mEnemyPositions;
      private List<Geometry.Vector> mEnemyVectors;
      private Geometry.Vector mStartEnemyVector;
      private float mMaxSpeed;
      private float mMultiplierSpeed;
      private int mDifficultyLevel;
      private float mAspectRatio;

      public Builder setChipPosition(Geometry.Point chipPosition) {
        mChipPosition = chipPosition;
        return this;
      }

      public Builder setChipSpeed(Geometry.Vector chipSpeed) {
        mChipSpeed = chipSpeed;
        return this;
      }

      public Builder setEnemyPositions(List<Geometry.Point> enemyPositions) {
        mEnemyPositions = enemyPositions;
        return this;
      }

      public Builder setEnemyVectors(List<Geometry.Vector> enemyVectors) {
        mEnemyVectors = enemyVectors;
        return this;
      }

      public Builder setStartEnemyVector(Geometry.Vector startEnemyVector) {
        mStartEnemyVector = startEnemyVector;
        return this;
      }

      public Builder setMaxSpeed(float maxSpeed) {
        mMaxSpeed = maxSpeed;
        return this;
      }

      public Builder setMultiplierSpeed(float multiplierSpeed) {
        mMultiplierSpeed = multiplierSpeed;
        return this;
      }

      public Builder setDifficultyLevel(int difficultyLevel) {
        mDifficultyLevel = difficultyLevel;
        return this;
      }

      public Builder setAspectRatio(float aspectRatio) {
        mAspectRatio = aspectRatio;
        return this;
      }

      public GameParams build() {
        return new GameParams(mChipPosition, mChipSpeed, mEnemyPositions, mEnemyVectors,
            mStartEnemyVector, mMaxSpeed, mMultiplierSpeed, mDifficultyLevel, mAspectRatio);
      }
    }
  }
}
