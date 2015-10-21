package com.mobilechallenge.game.objects;

import com.mobilechallenge.game.utils.Geometry;

/**
 * Project: Game
 * Date: 10/20/15
 * Code style: SquareAndroid (https://github.com/square/java-code-styles)
 */
public class ChipObject extends AbstractMovableObject {

  public static float RADIUS = 0.1f;

  public ChipObject(Geometry.Point startPosition, Geometry.Vector startSpeedVector) {
    super(startPosition, startSpeedVector);
  }

  public ChipObject(Drawable image, Geometry.Point startPosition,
      Geometry.Vector startSpeedVector) {
    super(image, startPosition, startSpeedVector);
  }
}
