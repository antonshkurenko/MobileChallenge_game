package com.mobilechallenge.game.controllers;

import com.mobilechallenge.game.objects.ChipObject;
import com.mobilechallenge.game.objects.Drawable;
import com.mobilechallenge.game.objects.EnemyObject;
import com.mobilechallenge.game.utils.Geometry;
import com.mobilechallenge.game.utils.Gyroscope;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import timber.log.Timber;

/**
 * Project: Game
 * Date: 10/20/15
 * Code style: SquareAndroid (https://github.com/square/java-code-styles)
 */
public class GameState {

  private static final float RIGHT_BOUND = 0.95f;
  private static final float LEFT_BOUND = -0.95f;
  private static final float TOP_BOUND = 0.95f;
  private static final float BOTTOM_BOUND = -0.95f;

  private final Random mRandom;
  private final Gyroscope mGyroscope;

  // objects
  private ChipObject mChipObject;
  private List<EnemyObject> mEnemyObjects;

  private float mAspectRatio;

  public GameState(Gyroscope gyroscope) {

    mRandom = new Random();
    mGyroscope = gyroscope;
  }

  public GameState setAspectRatio(float aspectRatio) {
    mAspectRatio = aspectRatio;
    return this;
  }

  public GameState setChipView(Drawable chip) {
    mChipObject.setImage(chip);
    return this;
  }

  public GameState setEnemyView(Drawable enemy) {
    for(EnemyObject e : mEnemyObjects) {
      e.setImage(enemy);
    }
    return this;
  }

  public synchronized ChipObject getChipObject() {
    return mChipObject;
  }

  public synchronized List<EnemyObject> getEnemyObjects() {
    return mEnemyObjects;
  }

  public void initGame(float aspectRatio) {
    mAspectRatio = aspectRatio;

    mAspectRatio = aspectRatio;

    mChipObject = new ChipObject(new Geometry.Point(0f, 0f, 0f), new Geometry.Vector(0f, 0f, 0f));
    mEnemyObjects = new ArrayList<>();

    final float rightX = RIGHT_BOUND - EnemyObject.RADIUS / mAspectRatio;
    final float topY = TOP_BOUND - EnemyObject.RADIUS;
    final float leftX = LEFT_BOUND + EnemyObject.RADIUS / mAspectRatio;
    final float bottomY = BOTTOM_BOUND + EnemyObject.RADIUS;

    Timber.i("rightX = %f, leftX = %f, topY = %f, bottomY = %f", rightX, leftX, topY, bottomY);

    mEnemyObjects.add(
        new EnemyObject(new Geometry.Point(rightX, topY), new Geometry.Vector(-0.002f, -0.002f)));
    mEnemyObjects.add(
        new EnemyObject(new Geometry.Point(rightX, bottomY), new Geometry.Vector(-0.002f, 0.002f)));
    mEnemyObjects.add(
        new EnemyObject(new Geometry.Point(leftX, topY), new Geometry.Vector(0.002f, -0.002f)));
    mEnemyObjects.add(
        new EnemyObject(new Geometry.Point(leftX, bottomY), new Geometry.Vector(0.002f, 0.002f)));
  }

  public void step() {

    final float[] orientation = mGyroscope.getOrientationArray(); // 0 x, 1 y

    mChipObject.setSpeed(new Geometry.Vector(-orientation[0] / 100, -orientation[1] / 100));
    mChipObject.move();

    final Geometry.Point chipPosition = mChipObject.getPosition();

    if (chipPosition.x < LEFT_BOUND + ChipObject.RADIUS / mAspectRatio
        || chipPosition.x > RIGHT_BOUND - ChipObject.RADIUS / mAspectRatio) {
      mChipObject.stop();
    }
    if (chipPosition.y > TOP_BOUND - ChipObject.RADIUS
        || chipPosition.y < BOTTOM_BOUND + ChipObject.RADIUS) {
      mChipObject.stop();
    }

    mChipObject.setPosition(new Geometry.Point(
        clamp(chipPosition.x, LEFT_BOUND + ChipObject.RADIUS / mAspectRatio,
            RIGHT_BOUND - ChipObject.RADIUS / mAspectRatio),
        clamp(chipPosition.y, BOTTOM_BOUND + ChipObject.RADIUS, TOP_BOUND - ChipObject.RADIUS)));

    for (int i = 0; i < 4; i++) {

      final EnemyObject enemy = mEnemyObjects.get(i);
      enemy.move();
      enemy.scaleSpeed(1.002f);

      final Geometry.Point position = enemy.getPosition();

      if (position.x < LEFT_BOUND + EnemyObject.RADIUS / mAspectRatio
          || position.x > RIGHT_BOUND - EnemyObject.RADIUS / mAspectRatio) {
        enemy.rotateRandom(mRandom);
      }
      if (position.y > TOP_BOUND - EnemyObject.RADIUS
          || position.y < BOTTOM_BOUND + EnemyObject.RADIUS) {
        enemy.rotateRandom(mRandom);
      }

      enemy.setPosition(new Geometry.Point(
          clamp(position.x, LEFT_BOUND + EnemyObject.RADIUS / mAspectRatio,
              RIGHT_BOUND - EnemyObject.RADIUS / mAspectRatio),
          clamp(position.y, BOTTOM_BOUND + EnemyObject.RADIUS, TOP_BOUND - EnemyObject.RADIUS)));
    }
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
}
