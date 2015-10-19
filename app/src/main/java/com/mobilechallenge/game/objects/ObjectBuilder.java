package com.mobilechallenge.game.objects;

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
public class ObjectBuilder {

  private static final int FLOATS_PER_VERTEX = 2; // X, Y, R, G, B

  private final float[] mVertexData;
  private final List<Drawable> mDrawList = new ArrayList<>();
  private int mOffset = 0;

  // creating image of the chip
  static GeneratedData createChip(Geometry.Point center, float radius, int numPoints) {

    int size = sizeOfCircleInVertices(numPoints);

    // visible top
    Geometry.Circle topCircle =
        new Geometry.Circle(center, radius);

    return new ObjectBuilder(size).appendCircle(topCircle, numPoints).build();
  }

  private static int sizeOfCircleInVertices(int numPoints) {
    return 1 + (numPoints + 1);
  }

  private ObjectBuilder(int sizeInVertices) {
    mVertexData = new float[sizeInVertices * FLOATS_PER_VERTEX];
  }

  private ObjectBuilder appendCircle(Geometry.Circle circle, int numPoints) {

    final int startVertex = mOffset / FLOATS_PER_VERTEX;
    final int numVertices = sizeOfCircleInVertices(numPoints);

    mVertexData[mOffset++] = circle.center.x;
    mVertexData[mOffset++] = circle.center.y;

    for (int i = 0; i <= numPoints; i++) {
      float angleInRadians = ((float) i / (float) numPoints) * ((float) Math.PI * 2f);

      mVertexData[mOffset++] = circle.center.x + circle.radius * (float) Math.cos(angleInRadians);

      mVertexData[mOffset++] = circle.center.y + circle.radius * (float) Math.sin(angleInRadians);
    }

    mDrawList.add(() -> glDrawArrays(GL_TRIANGLE_FAN, startVertex, numVertices));
    return this;
  }

  private GeneratedData build() {
    return new GeneratedData(mVertexData, mDrawList);
  }

  static class GeneratedData {
    final float[] mVertexData;
    final List<Drawable> mDrawableList;

    GeneratedData(float[] vertexData, List<Drawable> drawableList) {
      this.mVertexData = vertexData;
      this.mDrawableList = drawableList;
    }
  }
}
