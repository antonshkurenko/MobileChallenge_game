package com.mobilechallenge.game.ui;

import com.mobilechallenge.game.objects.Drawable;
import com.mobilechallenge.game.objects.EnemyObject;
import com.mobilechallenge.game.programs.DefaultTextureProgram;
import com.mobilechallenge.game.programs.SimpleSingleColorShaderProgram;
import com.mobilechallenge.game.utils.Geometry;
import com.mobilechallenge.game.utils.VertexArray;
import java.util.List;

/**
 * Project: Game
 * Date: 10/19/15
 * Code style: SquareAndroid (https://github.com/square/java-code-styles)
 */
public class EnemyView implements Drawable {

  public final float radius;

  private static final int POSITION_COMPONENT_COUNT = 2;
  private static final int TEXTURE_COORDINATES_COMPONENT_COUNT = 2;

  private final VertexArray mVertexArray;
  private final VertexArray mTextureVertexArray;
  private final List<Drawable> mDrawList;

  private int mTexture;

  public EnemyView(int numPointsAroundChip, float aspectRatio) {

    this.radius = EnemyObject.RADIUS;

    ViewObjectBuilder.GeneratedData generatedData =
        ViewObjectBuilder.createEnemy(new Geometry.Point(0f, 0f, 0f), radius, numPointsAroundChip,
            aspectRatio);

    mVertexArray = new VertexArray(generatedData.mVertexData);
    mTextureVertexArray = new VertexArray(generatedData.mTextureData);
    mDrawList = generatedData.mDrawableList;
  }

  public int getTexture() {
    return mTexture;
  }

  public void setTexture(int texture) {
    mTexture = texture;
  }

  @Override public void draw() {
    for (Drawable drawable : mDrawList) {
      drawable.draw();
    }
  }

  public void bindData(SimpleSingleColorShaderProgram program) {
    mVertexArray.setVertexAttribPointer(0, program.getPositionLocation(), POSITION_COMPONENT_COUNT,
        0);
  }

  public void bindData(DefaultTextureProgram program) {

    mVertexArray.setVertexAttribPointer(0, program.getPositionAttributeLocation(),
        POSITION_COMPONENT_COUNT, 0);

    mTextureVertexArray.setVertexAttribPointer(0, program.getTextureCoordinatesAttributeLocation(),
        TEXTURE_COORDINATES_COMPONENT_COUNT, 0);
  }
}
