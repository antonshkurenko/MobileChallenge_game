package com.mobilechallenge.game.programs;

import android.content.Context;
import com.mobilechallenge.game.R;

import static android.opengl.GLES20.glGetAttribLocation;
import static android.opengl.GLES20.glGetUniformLocation;
import static android.opengl.GLES20.glUniformMatrix4fv;

/**
 * Project: Game
 * Date: 10/18/15
 * Code style: SquareAndroid (https://github.com/square/java-code-styles)
 */
public class SimpleVaryingColorShaderProgram extends AbstractShaderProgram {

  //Uniforms
  private final int uMatrixLocation;

  //Attributes
  private final int aPositionLocation;
  private final int aColorLocation;

  public SimpleVaryingColorShaderProgram(Context context) {
    super(context, R.raw.simple_vertex_shader, R.raw.simple_fragment_shader);

    uMatrixLocation = glGetUniformLocation(mProgram, U_MATRIX);

    aPositionLocation = glGetAttribLocation(mProgram, A_POSITION);
    aColorLocation = glGetAttribLocation(mProgram, A_COLOR);
  }

  public void setUniforms(float [] projectionMatrix) {
    glUniformMatrix4fv(uMatrixLocation, 1, false, projectionMatrix, 0);
  }

  public int getPositionLocation() {
    return aPositionLocation;
  }

  public int getColorLocation() {
    return aColorLocation;
  }
}
