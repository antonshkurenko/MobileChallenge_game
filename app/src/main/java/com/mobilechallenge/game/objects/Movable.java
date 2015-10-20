package com.mobilechallenge.game.objects;

import com.mobilechallenge.game.utils.Geometry;

/**
 * Project: Game
 * Date: 10/20/15
 * Code style: SquareAndroid (https://github.com/square/java-code-styles)
 */
public interface Movable {
  /**
   * change position with inner speed vector
   */
  void move();

  /**
   * get position moved with inner speed vector multiplied to the interpolation
   * @param interpolation
   */
  Geometry.Point getInterpolatedPosition(float interpolation);

  /**
   * make speed equal zero
   */
  void stop();
}
