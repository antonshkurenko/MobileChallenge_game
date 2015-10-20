package com.mobilechallenge.game.objects;

import com.mobilechallenge.game.programs.AbstractShaderProgram;

/**
 * Project: Game
 * Date: 10/20/15
 * Code style: SquareAndroid (https://github.com/square/java-code-styles)
 */
public abstract class BaseObject implements BinderDrawable {

  protected BinderDrawable mImage; // kinda flyweight

  public BaseObject(BinderDrawable image) {
    mImage = image;
  }

  @Override public void draw() {
    mImage.draw();
  }

  @Override public void bindData(AbstractShaderProgram program) {
    mImage.bindData(program);
  }
}
