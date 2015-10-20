package com.mobilechallenge.game.ui;

import com.mobilechallenge.game.objects.Drawable;
import com.mobilechallenge.game.utils.Geometry;
import java.util.ArrayList;
import java.util.List;

import static android.opengl.GLES20.GL_TRIANGLE_FAN;
import static android.opengl.GLES20.glDrawArrays;

/**
 * Project: Game
 * Date: 10/18/15
 * Code style: SquareAndroid (https://github.com/square/java-code-styles)
 */
public class ViewObjectBuilder {

  private static final int FLOATS_PER_VERTEX = 2; // X, Y, R, G, B

  private final float[] mVertexData;
  private final float[] mTextureData;
  private final List<Drawable> mDrawList = new ArrayList<>();
  private int mOffset = 0;

  // creating image of the chip
  static GeneratedData createChip(Geometry.Point center, float radius, int numPoints,
      float aspectRatio) {

    int size = sizeOfCircleInVertices(numPoints);

    Geometry.Circle circle = new Geometry.Circle(center, radius);

    return new ViewObjectBuilder(size).appendCircle(circle, numPoints, aspectRatio).build();
  }

  // creating image of the enemy
  static GeneratedData createEnemy(Geometry.Point center, float radius, int numPoints,
      float aspectRatio) {

    int size = sizeOfCircleInVertices(numPoints);

    // visible top
    Geometry.Circle circle = new Geometry.Circle(center, radius);

    return new ViewObjectBuilder(size).appendCircle(circle, numPoints, aspectRatio).build();
  }

  private static int sizeOfCircleInVertices(int numPoints) {
    return 1 + (numPoints + 1);
  }

  private ViewObjectBuilder(int sizeInVertices) {
    mVertexData = new float[sizeInVertices * FLOATS_PER_VERTEX];
    mTextureData = new float[sizeInVertices * FLOATS_PER_VERTEX];
  }

  private ViewObjectBuilder appendCircle(Geometry.Circle circle, int numPoints, float aspectRatio) {

    final int startVertex = mOffset / FLOATS_PER_VERTEX;
    final int numVertices = sizeOfCircleInVertices(numPoints);

    mVertexData[mOffset] = circle.center.x / aspectRatio;
    mTextureData[mOffset++] = (circle.center.x + 1f) * 0.5f;
    mVertexData[mOffset] = circle.center.y;
    mTextureData[mOffset++] = (circle.center.y + 1f) * 0.5f * -1;

    for (int i = 0; i <= numPoints; i++) {
      float angleInRadians = ((float) i / (float) numPoints) * ((float) Math.PI * 2f);

      final float c = (float) Math.cos(angleInRadians);
      final float s = (float) Math.sin(angleInRadians);

      mVertexData[mOffset] =
          circle.center.x + circle.radius * c / aspectRatio;

      mTextureData[mOffset++] =
          (c + 1f) * 0.5f;

      mVertexData[mOffset] = circle.center.y + circle.radius * s;

      mTextureData[mOffset++] =
          (s + 1f) * 0.5f * -1;
    }

    mDrawList.add(() -> glDrawArrays(GL_TRIANGLE_FAN, startVertex, numVertices));
    return this;
  }

  private GeneratedData build() {
    return new GeneratedData(mVertexData, mTextureData, mDrawList);
  }

  static class GeneratedData {
    final float[] mVertexData;
    final float[] mTextureData;
    final List<Drawable> mDrawableList;

    GeneratedData(float[] vertexData, float[] textureData, List<Drawable> drawableList) {
      this.mVertexData = vertexData;
      this.mTextureData = textureData;
      this.mDrawableList = drawableList;
    }
  }
}
