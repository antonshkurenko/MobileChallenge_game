package com.mobilechallenge.game.ui;

import com.mobilechallenge.game.objects.BinderDrawable;
import com.mobilechallenge.game.programs.AbstractShaderProgram;
import com.mobilechallenge.game.programs.SimpleVaryingColorShaderProgram;
import com.mobilechallenge.game.utils.VertexArray;

import static android.opengl.GLES20.GL_TRIANGLE_FAN;
import static android.opengl.GLES20.glDrawArrays;
import static com.mobilechallenge.game.utils.Constants.BYTES_PER_FLOAT;

/**
 * Project: Game
 * Date: 10/18/15
 * Code style: SquareAndroid (https://github.com/square/java-code-styles)
 */
public class DeckView implements BinderDrawable {

  private static final int POSITION_COMPONENT_COUNT = 2;
  private static final int COLOR_COMPONENT_COUNT = 3;
  private static final int STRIDE =
      (POSITION_COMPONENT_COUNT + COLOR_COMPONENT_COUNT) * BYTES_PER_FLOAT;

  // @formatter:off
  private static final float[] VERTEX_DATA = {
      // Order of coordinates: X, Y, R, G, B
      // Triangle Fan
      0f, 0f, 1f, 1f, 1f,
      -0.95f, -0.95f, 0.7f, 0.7f, 0.7f,
      0.95f, -0.95f, 0.7f, 0.7f, 0.7f,
      0.95f, 0.95f, 0.7f, 0.7f, 0.7f,
      -0.95f, 0.95f, 0.7f, 0.7f, 0.7f,
      -0.95f, -0.95f, 0.7f, 0.7f, 0.7f,
  };
  // @formatter:on

  private final VertexArray mVertexArray;

  public DeckView() {
    mVertexArray = new VertexArray(VERTEX_DATA);
  }

  public void bindData(SimpleVaryingColorShaderProgram shaderProgram) {
    mVertexArray.setVertexAttribPointer(0, shaderProgram.getPositionLocation(),
        POSITION_COMPONENT_COUNT, STRIDE);

    mVertexArray.setVertexAttribPointer(POSITION_COMPONENT_COUNT, shaderProgram.getColorLocation(),
        COLOR_COMPONENT_COUNT, STRIDE);
  }

  @Override public void draw() {
    glDrawArrays(GL_TRIANGLE_FAN, 0, 6);
  }

  @Override public void bindData(AbstractShaderProgram program) {
    if (!(program instanceof SimpleVaryingColorShaderProgram)) {
      throw new IllegalArgumentException(
          "Wrong program, it needs to be SimpleVaryingColorShaderProgram");
    }

    SimpleVaryingColorShaderProgram shaderProgram = ((SimpleVaryingColorShaderProgram) program);

    mVertexArray.setVertexAttribPointer(0, shaderProgram.getPositionLocation(),
        POSITION_COMPONENT_COUNT, STRIDE);

    mVertexArray.setVertexAttribPointer(POSITION_COMPONENT_COUNT, shaderProgram.getColorLocation(),
        COLOR_COMPONENT_COUNT, STRIDE);
  }
}
