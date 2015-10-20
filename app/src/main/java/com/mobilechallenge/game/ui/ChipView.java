package com.mobilechallenge.game.ui;

import com.mobilechallenge.game.objects.ChipObject;
import com.mobilechallenge.game.objects.Drawable;
import com.mobilechallenge.game.programs.SimpleSingleColorShaderProgram;
import com.mobilechallenge.game.utils.Geometry;
import com.mobilechallenge.game.utils.VertexArray;
import java.util.List;

/**
 * Project: Game
 * Date: 10/18/15
 * Code style: SquareAndroid (https://github.com/square/java-code-styles)
 */
public class ChipView implements Drawable {

  public final float radius;

  private static final int POSITION_COMPONENT_COUNT = 2;

  private final VertexArray mVertexArray;
  private final List<Drawable> mDrawList;

  public ChipView(int numPointsAroundChip, float aspectRatio) {

    this.radius = ChipObject.RADIUS;

    ViewObjectBuilder.GeneratedData generatedData =
        ViewObjectBuilder.createChip(new Geometry.Point(0f, 0f, 0f), radius, numPointsAroundChip,
            aspectRatio);

    mVertexArray = new VertexArray(generatedData.mVertexData);
    mDrawList = generatedData.mDrawableList;
  }

  @Override public void draw() {
    for (Drawable drawable : mDrawList) {
      drawable.draw();
    }
  }

  public void bindData(SimpleSingleColorShaderProgram program) {
    mVertexArray.setVertexAttribPointer(0,
        program.getPositionLocation(), POSITION_COMPONENT_COUNT,
        0);
  }
}
