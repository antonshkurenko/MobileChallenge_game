package com.mobilechallenge.game.programs;

import android.content.Context;
import com.mobilechallenge.game.R;

import static android.opengl.GLES20.glGetAttribLocation;
import static android.opengl.GLES20.glGetUniformLocation;
import static android.opengl.GLES20.glUniform4f;

/**
 * Project: Game
 * Date: 10/18/15
 * Code style: SquareAndroid (https://github.com/square/java-code-styles)
 */
public class SimpleShaderProgram extends AbstractShaderProgram {

  //Uniforms
  private final int uColorLocation;

  //Attributes
  private final int aPositionLocation;

  public SimpleShaderProgram(Context context) {
    super(context, R.raw.simple_vertex_shader, R.raw.simple_fragment_shader);

    uColorLocation = glGetUniformLocation(mProgram, U_COLOR);
    aPositionLocation = glGetAttribLocation(mProgram, A_POSITION);
  }

  public void setUniforms(float r, float g, float b) {
    glUniform4f(uColorLocation, r, g, b, 1f);
  }

  public int getPositionLocation() {
    return aPositionLocation;
  }
}
