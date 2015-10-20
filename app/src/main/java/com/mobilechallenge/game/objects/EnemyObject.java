package com.mobilechallenge.game.objects;

import com.mobilechallenge.game.ui.EnemyView;
import com.mobilechallenge.game.utils.Geometry;

/**
 * Project: Game
 * Date: 10/20/15
 * Code style: SquareAndroid (https://github.com/square/java-code-styles)
 */
public class EnemyObject extends AbstractMovableObject {

  private final float mRadius;

  public EnemyObject(EnemyView image, Geometry.Point startPosition,
      Geometry.Vector startSpeedVector) {
    super(image, startPosition, startSpeedVector);
    mRadius = image.radius;
  }

  public float getRadius() {
    return mRadius;
  }
}
