package com.mobilechallenge.game.objects;

import com.mobilechallenge.game.programs.SimpleVaryingColorShaderProgram;
import com.mobilechallenge.game.ui.DeckView;

/**
 * Project: Game
 * Date: 10/20/15
 * Code style: SquareAndroid (https://github.com/square/java-code-styles)
 */
public class DeckObject extends BaseObject {

  public DeckObject(DeckView image) {
    super(image);
  }

  public void bindData(SimpleVaryingColorShaderProgram program) {
    ((DeckView)mImage).bindData(program);
  }
}
