package com.mobilechallenge.game.objects;

import com.mobilechallenge.game.utils.Geometry;
import java.util.ArrayList;
import java.util.List;

import static android.opengl.GLES20.GL_TRIANGLE_FAN;
import static android.opengl.GLES20.GL_TRIANGLE_STRIP;
import static android.opengl.GLES20.glDrawArrays;

/**
 * Project: Game
 * Date: 10/18/15
 * Code style: SquareAndroid (https://github.com/square/java-code-styles)
 */
public class ObjectBuilder {

  private static final int FLOATS_PER_VERTEX = 3;

  private final float[] mVertexData;
  private final List<Drawable> mDrawList = new ArrayList<>();
  private int mOffset = 0;

  // creating image of the chip
  static GeneratedData createChip(Geometry.Point center, float radius, float height,
      int numPoints) {
    int size = sizeOfCircleInVertices(numPoints) + sizeOfOpenCylinderInVertices(numPoints);

    // visible top
    Geometry.Circle topCircle = new Geometry.Circle(center.translateY(-height), radius);

    // sides of the chip
    Geometry.Cylinder cylinder =
        new Geometry.Cylinder(topCircle.center.translateY(-height / 2f), radius, height);

    return new ObjectBuilder(size).appendCircle(topCircle, numPoints)
        .appendOpenCylinder(cylinder, numPoints)
        .build();
  }

  private static int sizeOfCircleInVertices(int numPoints) {
    return 1 + (numPoints + 1);
  }

  private static int sizeOfOpenCylinderInVertices(int numPoints) {
    return (numPoints + 1) * 2;
  }

  private ObjectBuilder(int sizeInVertices) {
    mVertexData = new float[sizeInVertices * FLOATS_PER_VERTEX];
  }

  private ObjectBuilder appendCircle(Geometry.Circle circle, int numPoints) {

    final int startVertex = mOffset / FLOATS_PER_VERTEX;
    final int numVertices = sizeOfCircleInVertices(numPoints);

    mVertexData[mOffset++] = circle.center.x;
    mVertexData[mOffset++] = circle.center.y;
    mVertexData[mOffset++] = circle.center.z;

    for (int i = 0; i <= numPoints; i++) {
      float angleInRadians = ((float) i / (float) numPoints) * ((float) Math.PI * 2f);

      mVertexData[mOffset++] = circle.center.x + circle.radius * (float) Math.cos(angleInRadians);

      mVertexData[mOffset++] = circle.center.y;

      mVertexData[mOffset++] = circle.center.z + circle.radius * (float) Math.sin(angleInRadians);
    }

    mDrawList.add(() -> glDrawArrays(GL_TRIANGLE_FAN, startVertex, numVertices));
    return this;
  }

  private ObjectBuilder appendOpenCylinder(Geometry.Cylinder cylinder, int numPoints) {
    final int startVertex = mOffset / FLOATS_PER_VERTEX;
    final int numVertices = sizeOfOpenCylinderInVertices(numPoints);
    final float yStart = cylinder.center.y - (cylinder.height / 2f);
    final float yEnd = cylinder.center.y + (cylinder.height / 2f);

    for (int i = 0; i <= numPoints; i++) {
      float angleInRadians = ((float) i / (float) numPoints) * ((float) Math.PI * 2f);

      float xPosition = cylinder.center.x + cylinder.radius * (float) Math.cos(angleInRadians);

      float zPosition = cylinder.center.z + cylinder.radius * (float) Math.sin(angleInRadians);

      mVertexData[mOffset++] = xPosition;
      mVertexData[mOffset++] = yStart;
      mVertexData[mOffset++] = zPosition;

      mVertexData[mOffset++] = xPosition;
      mVertexData[mOffset++] = yEnd;
      mVertexData[mOffset++] = zPosition;

      mDrawList.add(() -> glDrawArrays(GL_TRIANGLE_STRIP, startVertex, numVertices));
    }
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
