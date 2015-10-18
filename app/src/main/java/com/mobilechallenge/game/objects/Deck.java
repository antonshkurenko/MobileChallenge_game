package com.mobilechallenge.game.objects;

import com.mobilechallenge.game.programs.DefaultTextureProgram;
import com.mobilechallenge.game.programs.SimpleShaderProgram;
import com.mobilechallenge.game.utils.VertexArray;

import static android.opengl.GLES20.GL_TRIANGLES;
import static android.opengl.GLES20.GL_TRIANGLE_FAN;
import static android.opengl.GLES20.glDrawArrays;
import static com.mobilechallenge.game.utils.Constants.BYTES_PER_FLOAT;

/**
 * Project: Game
 * Date: 10/18/15
 * Code style: SquareAndroid (https://github.com/square/java-code-styles)
 */
public class Deck implements Drawable {

  private static final int POSITION_COMPONENT_COUNT = 2;
  private static final int STRIDE = 0;
      //(POSITION_COMPONENT_COUNT) * BYTES_PER_FLOAT;

  private static final float[] VERTEX_DATA = {
      -0.5f, -0.5f,
      0.5f, 0.5f,
      -0.5f, 0.5f,
      -0.5f, -0.5f,
      0.5f, -0.5f,
      0.5f, 0.5f,
  };

  private final VertexArray mVertexArray;

  public Deck() {
    mVertexArray = new VertexArray(VERTEX_DATA);
  }

  public void bindData(SimpleShaderProgram shaderProgram) {
    mVertexArray.setVertexAttribPointer(0, shaderProgram.getPositionLocation(),
        POSITION_COMPONENT_COUNT, STRIDE);
  }

  @Override public void draw() {
    glDrawArrays(GL_TRIANGLES, 0, 6);
  }
}
