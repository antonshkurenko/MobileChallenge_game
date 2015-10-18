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
  private final List<Drawable> drawList = new ArrayList<>();
  private int offset = 0;

  private ObjectBuilder(int sizeInVertices) {
    mVertexData = new float[sizeInVertices * FLOATS_PER_VERTEX];
  }

  private static int sizeOfCircleInVertices(int numPoints) {
    return 1 + (numPoints + 1);
  }

  private static int sizeOfOpenCylinderInVertices(int numPoints) {
    return (numPoints + 1) * 2;
  }

  static GeneratedData createPuck(Geometry.Cylinder puck, int numPoints) {
    int size = sizeOfCircleInVertices(numPoints) + sizeOfOpenCylinderInVertices(numPoints);

    ObjectBuilder builder = new ObjectBuilder(size);

    Geometry.Circle puckTop =
        new Geometry.Circle(puck.center.translateY(puck.height / 2f), puck.radius);

    builder.appendCircle(puckTop, numPoints);
    builder.appendOpenCylinder(puck, numPoints);

    return builder.build();
  }

  static GeneratedData createMallet(Geometry.Point center, float radius, float height,
      int numPoints) {

    int size = sizeOfCircleInVertices(numPoints) * 2 + sizeOfOpenCylinderInVertices(numPoints) * 2;

    ObjectBuilder builder = new ObjectBuilder(size);

    float baseHeight = height * 0.25f;

    Geometry.Circle baseCircle = new Geometry.Circle(center.translateY(-baseHeight), radius);

    Geometry.Cylinder baseCylinder =
        new Geometry.Cylinder(baseCircle.center.translateY(-baseHeight / 2f), radius, baseHeight);

    builder.appendCircle(baseCircle, numPoints);
    builder.appendOpenCylinder(baseCylinder, numPoints);

    float handleHeight = height * 0.75f;
    float handleRadius = radius / 3f;

    Geometry.Circle handleCircle =
        new Geometry.Circle(center.translateY(height * 0.5f), handleRadius);

    Geometry.Cylinder handleCylinder =
        new Geometry.Cylinder(handleCircle.center.translateY(-handleHeight / 2f), handleRadius,
            handleHeight);

    builder.appendCircle(handleCircle, numPoints);
    builder.appendOpenCylinder(handleCylinder, numPoints);

    return builder.build();
  }

  private void appendCircle(Geometry.Circle circle, int numPoints) {

    final int startVertex = offset / FLOATS_PER_VERTEX;
    final int numVertices = sizeOfCircleInVertices(numPoints);

    mVertexData[offset++] = circle.center.x;
    mVertexData[offset++] = circle.center.y;
    mVertexData[offset++] = circle.center.z;

    for (int i = 0; i <= numPoints; i++) {
      float angleInRadians = ((float) i / (float) numPoints) * ((float) Math.PI * 2f);

      mVertexData[offset++] = circle.center.x + circle.radius * (float) Math.cos(angleInRadians);

      mVertexData[offset++] = circle.center.y;

      mVertexData[offset++] = circle.center.z + circle.radius * (float) Math.sin(angleInRadians);
    }

    drawList.add(new Drawable() {
      @Override public void draw() {
        glDrawArrays(GL_TRIANGLE_FAN, startVertex, numVertices);
      }
    });
  }

  private void appendOpenCylinder(Geometry.Cylinder cylinder, int numPoints) {
    final int startVertex = offset / FLOATS_PER_VERTEX;
    final int numVertices = sizeOfOpenCylinderInVertices(numPoints);
    final float yStart = cylinder.center.y - (cylinder.height / 2f);
    final float yEnd = cylinder.center.y + (cylinder.height / 2f);

    for (int i = 0; i <= numPoints; i++) {
      float angleInRadians = ((float) i / (float) numPoints) * ((float) Math.PI * 2f);

      float xPosition = cylinder.center.x + cylinder.radius * (float) Math.cos(angleInRadians);

      float zPosition = cylinder.center.z + cylinder.radius * (float) Math.sin(angleInRadians);

      mVertexData[offset++] = xPosition;
      mVertexData[offset++] = yStart;
      mVertexData[offset++] = zPosition;

      mVertexData[offset++] = xPosition;
      mVertexData[offset++] = yEnd;
      mVertexData[offset++] = zPosition;

      drawList.add(new Drawable() {
        @Override public void draw() {
          glDrawArrays(GL_TRIANGLE_STRIP, startVertex, numVertices);
        }
      });
    }
  }

  private GeneratedData build() {
    return new GeneratedData(mVertexData, drawList);
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
