package com.mobilechallenge.game.programs;

import android.content.Context;
import com.mobilechallenge.game.R;

import static android.opengl.GLES20.GL_TEXTURE0;
import static android.opengl.GLES20.GL_TEXTURE_2D;
import static android.opengl.GLES20.glActiveTexture;
import static android.opengl.GLES20.glBindTexture;
import static android.opengl.GLES20.glGetAttribLocation;
import static android.opengl.GLES20.glGetUniformLocation;
import static android.opengl.GLES20.glUniform1i;
import static android.opengl.GLES20.glUniformMatrix4fv;

/**
 * Project: Game
 * Date: 10/18/15
 * Code style: SquareAndroid (https://github.com/square/java-code-styles)
 */
public class TextureProgram extends AbstractShaderProgram {

  //Uniforms
  private final int uMatrixLocation;
  private final int uTextureUnitLocation;

  //Attributes
  private final int aPositionLocation;
  private final int aTextureCoordinatesLocation;

  public TextureProgram(Context context) {
    super(context, R.raw.texture_vertex_shader, R.raw.texture_fragment_shader);

    uMatrixLocation = glGetUniformLocation(program, U_MATRIX);
    uTextureUnitLocation = glGetUniformLocation(program, U_TEXTURE_UNIT);

    aPositionLocation = glGetAttribLocation(program, A_POSITION);
    aTextureCoordinatesLocation = glGetAttribLocation(program, A_TEXTURE_COORDINATES);
  }

  public void setUniforms(float[] matrix, int textureId) {

    glUniformMatrix4fv(uMatrixLocation, 1, false, matrix, 0);

    glActiveTexture(GL_TEXTURE0);
    glBindTexture(GL_TEXTURE_2D, textureId);

    glUniform1i(uTextureUnitLocation, 0);
  }

  public int getPositionAttributeLocation() {
    return aPositionLocation;
  }

  public int getTextureCoordinatesAttributeLocation() {
    return aTextureCoordinatesLocation;
  }
}
