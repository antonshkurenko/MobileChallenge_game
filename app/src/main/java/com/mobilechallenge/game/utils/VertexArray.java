package com.mobilechallenge.game.utils;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import static android.opengl.GLES20.GL_FLOAT;
import static android.opengl.GLES20.glEnableVertexAttribArray;
import static android.opengl.GLES20.glVertexAttribPointer;
import static com.mobilechallenge.game.utils.Constants.BYTES_PER_FLOAT;

/**
 * Project: Game
 * Date: 10/18/15
 * Code style: SquareAndroid (https://github.com/square/java-code-styles)
 */
public class VertexArray {

  private final FloatBuffer mFloatBuffer;

  public VertexArray(float[] vertexData) {
    mFloatBuffer = ByteBuffer.allocateDirect(vertexData.length * BYTES_PER_FLOAT)
        .order(ByteOrder.nativeOrder())
        .asFloatBuffer()
        .put(vertexData);
  }

  public void setVertexAttribPointer(int dataOffset, int attributeLocation, int componentCount,
      int stride) {
    mFloatBuffer.position(dataOffset);
    glVertexAttribPointer(attributeLocation, componentCount, GL_FLOAT, false, stride, mFloatBuffer);
    glEnableVertexAttribArray(attributeLocation);
    mFloatBuffer.position(0);
  }

  public void updateBuffer(float[] vertexData, int start, int count) {
    mFloatBuffer.position(start);
    mFloatBuffer.put(vertexData, start, count);
    mFloatBuffer.position(0);
  }
}
