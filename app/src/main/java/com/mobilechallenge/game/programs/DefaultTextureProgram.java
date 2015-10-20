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
public class DefaultTextureProgram extends AbstractShaderProgram {

  //Uniforms
  private final int uMatrixLocation;
  private final int uTextureUnitLocation;

  //Attributes
  private final int aPositionLocation;
  private final int aTextureCoordinatesLocation;

  public DefaultTextureProgram(Context context) {
    super(context, R.raw.texture_vertex_shader, R.raw.texture_fragment_shader);

    uMatrixLocation = glGetUniformLocation(mProgram, U_MATRIX);
    uTextureUnitLocation = glGetUniformLocation(mProgram, U_TEXTURE_UNIT);

    aPositionLocation = glGetAttribLocation(mProgram, A_POSITION);
    aTextureCoordinatesLocation = glGetAttribLocation(mProgram, A_TEXTURE_COORDINATES);
  }

  /**
   *
   * @param matrix ModelViewProjection matrix
   * @param textureId texture id, received from gl
   * @param glTextureNum texture number, GL_TEXTURE0 + n
   */
  public void setUniforms(float[] matrix, int textureId, int glTextureNum) {

    glUniformMatrix4fv(uMatrixLocation, 1, false, matrix, 0);

    glActiveTexture(GL_TEXTURE0 + glTextureNum);

    glBindTexture(GL_TEXTURE_2D, textureId);

    glUniform1i(uTextureUnitLocation, glTextureNum);
  }

  public int getPositionAttributeLocation() {
    return aPositionLocation;
  }

  public int getTextureCoordinatesAttributeLocation() {
    return aTextureCoordinatesLocation;
  }
}
