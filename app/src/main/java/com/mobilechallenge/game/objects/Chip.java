package com.mobilechallenge.game.objects;

import com.mobilechallenge.game.programs.DefaultColorProgram;
import com.mobilechallenge.game.utils.Geometry;
import com.mobilechallenge.game.utils.VertexArray;
import java.util.List;

/**
 * Project: Game
 * Date: 10/18/15
 * Code style: SquareAndroid (https://github.com/square/java-code-styles)
 */
public class Chip implements Drawable {

  public final float radius;
  public final float height;

  private static final int POSITION_COMPONENT_COUNT = 3;

  private final VertexArray mVertexArray;
  private final List<Drawable> mDrawList;

  public Chip(float radius, float height, int numPointsAroundChip) {

    ObjectBuilder.GeneratedData generatedData =
        ObjectBuilder.createChip(new Geometry.Point(0f, 0f, 0f), radius, height,
            numPointsAroundChip);

    this.radius = radius;
    this.height = height;

    mVertexArray = new VertexArray(generatedData.mVertexData);
    mDrawList = generatedData.mDrawableList;
  }

  public void bindData(DefaultColorProgram colorProgram) {
    mVertexArray.setVertexAttribPointer(0, colorProgram.getPositionAttributeLocation(),
        POSITION_COMPONENT_COUNT, 0);
  }

  @Override public void draw() {
    for (Drawable drawable : mDrawList) {
      drawable.draw();
    }
  }
}
