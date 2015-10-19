package com.mobilechallenge.game.objects;

import com.mobilechallenge.game.programs.SimpleSingleColorShaderProgram;
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

  private static final int POSITION_COMPONENT_COUNT = 2;

  private final VertexArray mVertexArray;
  private final List<Drawable> mDrawList;

  public Chip(float radius, int numPointsAroundChip, float aspectRatio) {

    ObjectBuilder.GeneratedData generatedData =
        ObjectBuilder.createChip(new Geometry.Point(0f, 0f, 0f), radius,
            numPointsAroundChip, aspectRatio);

    this.radius = radius;

    mVertexArray = new VertexArray(generatedData.mVertexData);
    mDrawList = generatedData.mDrawableList;
  }

  public void bindData(SimpleSingleColorShaderProgram colorProgram) {
    mVertexArray.setVertexAttribPointer(0, colorProgram.getPositionLocation(),
        POSITION_COMPONENT_COUNT, 0);
  }

  @Override public void draw() {
    for (Drawable drawable : mDrawList) {
      drawable.draw();
    }
  }
}
