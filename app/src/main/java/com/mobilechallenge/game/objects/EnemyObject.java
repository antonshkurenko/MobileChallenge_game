package com.mobilechallenge.game.objects;

import com.mobilechallenge.game.utils.Geometry;

/**
 * Project: Game
 * Date: 10/20/15
 * Code style: SquareAndroid (https://github.com/square/java-code-styles)
 */
public class EnemyObject extends AbstractMovableObject {

  public static float RADIUS = 0.075f;

  public EnemyObject(Geometry.Point startPosition, Geometry.Vector startSpeedVector) {
    super(startPosition, startSpeedVector);
  }

  public EnemyObject(Drawable image, Geometry.Point startPosition,
      Geometry.Vector startSpeedVector) {
    super(image, startPosition, startSpeedVector);
  }
}
