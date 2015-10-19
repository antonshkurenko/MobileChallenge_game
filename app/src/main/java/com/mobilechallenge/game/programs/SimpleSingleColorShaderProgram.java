package com.mobilechallenge.game.programs;

import android.content.Context;
import com.mobilechallenge.game.R;

import static android.opengl.GLES20.glGetAttribLocation;
import static android.opengl.GLES20.glGetUniformLocation;
import static android.opengl.GLES20.glUniform4f;
import static android.opengl.GLES20.glUniformMatrix4fv;

/**
 * Project: Game
 * Date: 10/19/15
 * Code style: SquareAndroid (https://github.com/square/java-code-styles)
 */
public class SimpleSingleColorShaderProgram extends AbstractShaderProgram {

  // Uniforms
  private int uColorLocation;
  private int uMatrixLocation;

  // Attributes
  private int aPositionLocation;

  public SimpleSingleColorShaderProgram(Context context) {
    super(context, R.raw.simple_vertex_shader, R.raw.single_color_fragment_shader);

    uColorLocation = glGetUniformLocation(mProgram, U_COLOR);
    uMatrixLocation = glGetUniformLocation(mProgram, U_MATRIX);

    aPositionLocation = glGetAttribLocation(mProgram, A_POSITION);
  }

  public void setUniforms(float[] matrix, float r, float g, float b) {
    glUniformMatrix4fv(uMatrixLocation, 1, false, matrix, 0);
    glUniform4f(uColorLocation, r, g, b, 1f);
  }

  public int getPositionLocation() {
    return aPositionLocation;
  }
}
