package com.mobilechallenge.game.objects;

/**
 * Project: Game
 * Date: 10/20/15
 * Code style: SquareAndroid (https://github.com/square/java-code-styles)
 */
public abstract class BaseObject implements Drawable {

  protected Drawable mImage; // kinda flyweight

  public BaseObject(Drawable image) {
    mImage = image;
  }

  public void setImage(Drawable image) {
    mImage = image;
  }

  @Override public void draw() {
    mImage.draw();
  }
}
