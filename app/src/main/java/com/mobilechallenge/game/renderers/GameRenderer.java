package com.mobilechallenge.game.renderers;

import android.content.Context;
import android.opengl.GLSurfaceView;
import com.mobilechallenge.game.objects.ChipObject;
import com.mobilechallenge.game.objects.DeckObject;
import com.mobilechallenge.game.objects.EnemyObject;
import com.mobilechallenge.game.programs.SimpleSingleColorShaderProgram;
import com.mobilechallenge.game.programs.SimpleVaryingColorShaderProgram;
import com.mobilechallenge.game.ui.ChipView;
import com.mobilechallenge.game.ui.DeckView;
import com.mobilechallenge.game.ui.EnemyView;
import com.mobilechallenge.game.utils.Geometry;
import com.mobilechallenge.game.utils.Gyroscope;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;
import timber.log.Timber;

import static android.opengl.GLES20.GL_COLOR_BUFFER_BIT;
import static android.opengl.GLES20.glClear;
import static android.opengl.GLES20.glClearColor;
import static android.opengl.GLES20.glViewport;
import static android.opengl.Matrix.multiplyMM;
import static android.opengl.Matrix.orthoM;
import static android.opengl.Matrix.setIdentityM;
import static android.opengl.Matrix.translateM;

/**
 * Project: Game
 * Date: 10/18/15
 * Code style: SquareAndroid (https://github.com/square/java-code-styles)
 */
public class GameRenderer implements GLSurfaceView.Renderer {

  private static final float RIGHT_BOUND = 0.95f;
  private static final float LEFT_BOUND = -0.95f;
  private static final float TOP_BOUND = 0.95f;
  private static final float BOTTOM_BOUND = -0.95f;

  // @formatter:off
    private final float[] mProjectionMatrix = new float[16];
   private final float[  ] mModelMatrix = new float[16];
  private final float[    ] mModelProjectionMatrix = new float[16];
  // @formatter:on
  private final Context mContext;
  private final Gyroscope mGyroscope;
  private float mAspectRatio; // w/h
  private SimpleVaryingColorShaderProgram mVaryingColorProgram;
  private SimpleSingleColorShaderProgram mSingleColorProgram;
  // objects
  private DeckObject mDeckObject;
  private ChipObject mChipObject;
  private List<EnemyObject> mEnemyObjects;

  // views
  private EnemyView mEnemyView;

  public GameRenderer(Context ctx, Gyroscope gyroscope) {
    mContext = ctx;
    mGyroscope = gyroscope;

    mEnemyObjects = new ArrayList<>();
  }

  @Override public void onSurfaceCreated(GL10 gl, EGLConfig config) {
    glClearColor(0.0f, 0.0f, 0.0f, 0.0f);

    mVaryingColorProgram = new SimpleVaryingColorShaderProgram(mContext);
    mSingleColorProgram = new SimpleSingleColorShaderProgram(mContext);
    mDeckObject =
        new DeckObject(new DeckView()); // since it doesn't need aspect ratio, it's initialized here
  }

  @Override public void onSurfaceChanged(GL10 gl, int width, int height) {
    glViewport(0, 0, width, height);

    // currently it's width / height
    mAspectRatio = (float) width / (float) height;

    Timber.i("Width is %d, height is %d, aspect is %f", width, height, mAspectRatio);

    ChipView chip = new ChipView(0.15f, 32, mAspectRatio);

    mChipObject =
        new ChipObject(chip, new Geometry.Point(0f, 0f, 0f), new Geometry.Vector(0f, 0f, 0f));

    mEnemyView = new EnemyView(0.075f, 32, mAspectRatio);

    final float rightX = RIGHT_BOUND - mEnemyView.radius / mAspectRatio;
    final float topY = TOP_BOUND - mEnemyView.radius;
    final float leftX = LEFT_BOUND + mEnemyView.radius / mAspectRatio;
    final float bottomY = BOTTOM_BOUND + mEnemyView.radius;

    Timber.i("rightX = %f, leftX = %f, topY = %f, bottomY = %f", rightX, leftX, topY, bottomY);

    mEnemyObjects.add(new EnemyObject(mEnemyView, new Geometry.Point(rightX, topY),
        new Geometry.Vector(-0.002f, -0.002f)));
    mEnemyObjects.add(new EnemyObject(mEnemyView, new Geometry.Point(rightX, bottomY),
        new Geometry.Vector(-0.002f, 0.002f)));
    mEnemyObjects.add(new EnemyObject(mEnemyView, new Geometry.Point(leftX, topY),
        new Geometry.Vector(0.002f, -0.002f)));
    mEnemyObjects.add(new EnemyObject(mEnemyView, new Geometry.Point(leftX, bottomY),
        new Geometry.Vector(0.002f, 0.002f)));

    // use aspect ratio not here, but later
    orthoM(mProjectionMatrix, 0, -1f, 1f, -1f, 1f, -1f, 1f);
  }

  @Override public void onDrawFrame(GL10 gl) {
    glClear(GL_COLOR_BUFFER_BIT);

    final Random rnd = new Random();

    final float[] orientation = mGyroscope.getOrientationArray(); // 0 x, 1 y

    Timber.d("Orientation x is %f, y is %f", orientation[0], orientation[1]);

    mChipObject.setSpeed(new Geometry.Vector(-orientation[0] / 100, -orientation[1] / 100));
    mChipObject.move();

    final Geometry.Point chipPosition = mChipObject.getPosition();
    float radius = mChipObject.getRadius();

    if (chipPosition.x < LEFT_BOUND + radius / mAspectRatio
        || chipPosition.x > RIGHT_BOUND - radius / mAspectRatio) {
      mChipObject.stop();
    }
    if (chipPosition.y > TOP_BOUND - radius || chipPosition.y < BOTTOM_BOUND + radius) {
      mChipObject.stop();
    }

    mChipObject.setPosition(new Geometry.Point(
        clamp(chipPosition.x, LEFT_BOUND + radius / mAspectRatio,
            RIGHT_BOUND - radius / mAspectRatio),
        clamp(chipPosition.y, BOTTOM_BOUND + radius, TOP_BOUND - radius)));

    for (int i = 0; i < 4; i++) {

      final EnemyObject enemy = mEnemyObjects.get(i);
      enemy.move();
      enemy.scaleSpeed(1.002f);

      final Geometry.Point position = enemy.getPosition();
      radius = enemy.getRadius();

      if (position.x < LEFT_BOUND + radius / mAspectRatio
          || position.x > RIGHT_BOUND - radius / mAspectRatio) {
        enemy.rotateRandom(rnd);
      }
      if (position.y > TOP_BOUND - radius || position.y < BOTTOM_BOUND + radius) {
        enemy.rotateRandom(rnd);
      }

      enemy.setPosition(new Geometry.Point(clamp(position.x, LEFT_BOUND + radius / mAspectRatio,
          RIGHT_BOUND - radius / mAspectRatio),
          clamp(position.y, BOTTOM_BOUND + radius, TOP_BOUND - radius)));
    }

    mVaryingColorProgram.useProgram();
    mDeckObject.bindData(mVaryingColorProgram);
    mVaryingColorProgram.setUniforms(mProjectionMatrix);
    mDeckObject.draw();

    mSingleColorProgram.useProgram();
    mChipObject.bindData(mSingleColorProgram);
    positionObjectInScene(mChipObject.getPosition().x, mChipObject.getPosition().y);
    mSingleColorProgram.setUniforms(mModelProjectionMatrix, 1f, 0f, 0f);
    mChipObject.draw();

    mEnemyView.bindData(mSingleColorProgram);
    for (EnemyObject enemy : mEnemyObjects) {
      positionObjectInScene(enemy.getPosition().x, enemy.getPosition().y);
      mSingleColorProgram.setUniforms(mModelProjectionMatrix, 0f, 0f, 1f);
      enemy.draw();
    }
  }

  private void positionObjectInScene(float x, float y) {
    setIdentityM(mModelMatrix, 0);
    translateM(mModelMatrix, 0, x, y, 0f);
    multiplyMM(mModelProjectionMatrix, 0, mProjectionMatrix, 0, mModelMatrix, 0);
  }

  /**
   * Keep object in the bounds of the deck
   *
   * @param value current x/y
   * @param min deck min x/y
   * @param max deck max x/y
   * @return return x/y
   */
  private float clamp(float value, float min, float max) {
    return Math.min(max, Math.max(value, min));
  }
}
