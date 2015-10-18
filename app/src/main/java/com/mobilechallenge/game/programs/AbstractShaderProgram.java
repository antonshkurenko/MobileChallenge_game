package com.mobilechallenge.game.programs;

import android.content.Context;
import com.mobilechallenge.game.utils.ShaderHelper;
import com.mobilechallenge.game.utils.TextResourceReader;

import static android.opengl.GLES20.glUseProgram;

/**
 * Project: Game
 * Date: 10/18/15
 * Code style: SquareAndroid (https://github.com/square/java-code-styles)
 */
public abstract class AbstractShaderProgram {

  //Uniforms
  protected static final String U_MATRIX = "u_Matrix";
  protected static final String U_TEXTURE_UNIT = "u_TextureUnit";
  protected static final String U_COLOR = "u_Color";

  //Attributes
  protected static final String A_POSITION = "a_Position";
  protected static final String A_COLOR = "a_Color";
  protected static final String A_TEXTURE_COORDINATES = "a_TextureCoordinates";

  //Shader
  protected final int mProgram;

  protected AbstractShaderProgram(Context context, int vertexShaderResourceId,
      int fragmentShaderResourceId) {

    mProgram = ShaderHelper.buildProgram(
        TextResourceReader.readTextFileFromResource(context, vertexShaderResourceId),
        TextResourceReader.readTextFileFromResource(context, fragmentShaderResourceId));
  }

  public void useProgram() {
    glUseProgram(mProgram);
  }
}
