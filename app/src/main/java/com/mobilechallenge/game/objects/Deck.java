package com.mobilechallenge.game.objects;

import com.mobilechallenge.game.programs.DefaultTextureProgram;
import com.mobilechallenge.game.utils.VertexArray;

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
  private static final int TEXTURE_COORDINATES_COMPONENT_COUNT = 2;
  private static final int STRIDE =
      (POSITION_COMPONENT_COUNT + TEXTURE_COORDINATES_COMPONENT_COUNT) * BYTES_PER_FLOAT;

  private static final float[] VERTEX_DATA = {
      0f, 0f, 0.5f, 0.5f, -0.5f, -0.8f, 0f, 0.9f, 0.5f, -0.8f, 1f, 0.9f, 0.5f, 0.8f, 1f, 0.1f,
      -0.5f, 0.8f, 0f, 0.1f, -0.5f, -0.8f, 0f, 0.9f
  };

  private final VertexArray vertexArray;

  public Deck() {
    vertexArray = new VertexArray(VERTEX_DATA);
  }

  public void bindData(DefaultTextureProgram textureProgram) {
    vertexArray.setVertexAttribPointer(0, textureProgram.getPositionAttributeLocation(),
        POSITION_COMPONENT_COUNT, STRIDE);

    vertexArray.setVertexAttribPointer(POSITION_COMPONENT_COUNT,
        textureProgram.getTextureCoordinatesAttributeLocation(),
        TEXTURE_COORDINATES_COMPONENT_COUNT, STRIDE);
  }

  @Override public void draw() {
    glDrawArrays(GL_TRIANGLE_FAN, 0, 6);
  }
}
